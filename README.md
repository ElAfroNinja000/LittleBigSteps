# LittleBigSteps

App Android de pratique créative quotidienne, gamifiée, sans compte. Voir
[CLAUDE.md](CLAUDE.md) pour la vision produit complète et
[docs/data-model.md](docs/data-model.md) pour le schéma de données.

## Stack

Kotlin + Jetpack Compose, architecture MVVM, stockage local Room, contenu servi
via JSON statique (voir `/content`). Détails dans CLAUDE.md §10.

## Structure du projet

```
app/src/main/java/com/littlebigsteps/app/
├── LittleBigStepsApplication.kt   # service locator manuel (pas de framework DI)
├── domain/
│   ├── model/            # enums partagés (MediumType, ChallengeLevel, Frequency)
│   └── GamificationRules.kt  # formule XP/niveau, source unique de vérité
├── data/
│   ├── local/             # Room : entités, DAOs, base, convertisseurs
│   ├── remote/             # ContentApiService (Retrofit) + DTOs du JSON distant
│   └── repository/         # ChallengeRepository, ProgressRepository,
│                            # UserPreferencesRepository, ContentSyncRepository
└── ui/
    ├── theme/            # thème Compose (Material3)
    └── MainActivity.kt

content/                  # contenu JSON servi tel quel via raw.githubusercontent.com
                           # (10 défis placeholder par médium — à remplacer par le
                           # catalogue final de 30-50/médium, CLAUDE.md §5)
docs/data-model.md         # référence du schéma de données
```

## Notifications

`NotificationScheduler` (WorkManager, pas de FCM) programme un rappel périodique
juste après l'onboarding, selon la fréquence et l'heure choisies. La permission
`POST_NOTIFICATIONS` (API 33+) est demandée à ce moment-là ; un refus reste
silencieux, sans blocage ni message culpabilisant.

## Photo de souvenir

`SouvenirPhotoStore` écrit les photos dans le stockage interne de l'app
(`filesDir/souvenirs`, référencées par chemin — CLAUDE.md §10, aucune synchro
cloud). Deux sources : `TakePicture` (délègue à l'app caméra externe, aucune
permission `CAMERA` requise) ou `PickVisualMedia` (sélecteur photo système,
aucune permission de stockage requise). Un fichier orphelin (photo prise/choisie
puis abandonnée sans compléter le défi) est nettoyé automatiquement.

## Premium (Google Play Billing)

`BillingRepository` (implémenté par `PlayBillingRepository`) gère l'abonnement
premium via la Play Billing Library directement — un seul store à gérer
(CLAUDE.md §7, §10). La connexion démarre dès `Application.onCreate()` pour
restaurer un abonnement existant avant même que l'utilisateur ouvre l'écran
Premium. Un achat confirmé débloque tous les médiums
(`ProgressRepository.ensureMediumRowsExist`) ; l'absence d'abonnement actif
reverrouille sur le seul médium gratuit choisi à l'onboarding.

⚠️ L'ID de produit (`premium_subscription` dans `PlayBillingRepository.kt`) est
un placeholder : il doit correspondre à un abonnement réellement créé dans
Play Console pour que `queryPremiumProductDetails()` renvoie une offre.

## Packs thématiques & badges (premium)

Les packs (`ChallengePackEntity`, synchronisés depuis `content/packs.json`,
optionnel) se parcourent en entier une fois débloqués — leurs défis
(`ChallengeEntity.packId` non-null) sont exclus du tirage aléatoire du jour.
Un pack verrouillé redirige vers l'écran Premium au lieu de s'ouvrir.

Les badges (`Badge`, `UnlockedBadgeEntity`) sont des cosmétiques exclusifs
premium — évalués à chaque complétion (`domain/BadgeEvaluator.kt`) uniquement
pour les utilisateurs premium, et débloqués une fois pour toutes (jamais
retirés, même si le streak qui les a déclenchés redescend ensuite).

## Export

`ProgressExportGenerator` dessine le résumé de progression sur un Canvas natif
partagé entre image PNG, PDF et story (`ExportRenderer`), écrit dans le cache
de l'app, puis le partage via `FileProvider` + l'Intent système
(`ACTION_SEND`) — pas de feed, pas de compte, juste un export autonome
(CLAUDE.md §6). Boutons sur l'écran Progression.

**Formats enrichis premium (§7)** : gratuit = 5 derniers souvenirs (texte
seul) ; premium = jusqu'à 10 souvenirs avec miniatures photo intégrées, section
badges, et un 3ᵉ format exclusif **Story** (ratio 9:16, mise en page dédiée
pour le partage réseaux sociaux — pas un simple redimensionnement du résumé
classique). Le tap sur "Story" en gratuit redirige vers Premium au lieu
d'exporter.

## Tests

Suite E2E en Jetpack Compose UI Testing (`app/src/androidTest`) : parcours
complet onboarding → sélection → complétion avec souvenir → portfolio →
progression, plus le skip d'onboarding au relancement. Room en mémoire + fakes
pour Billing/notifications/analytics uniquement — le reste (repositories,
ViewModels, écrans) est le vrai code de l'app. Détails et rationale dans
[CLAUDE.md](CLAUDE.md) §11.

```bash
./gradlew connectedAndroidTest
```

Nécessite un appareil ou émulateur connecté.

## Analytics

`AnalyticsTracker` (implémenté par `PostHogAnalyticsTracker`) suit les métriques
du §8 sans PII, via l'ID anonyme géré par le SDK PostHog (auto-hébergé
recommandé) — pas de compte, pas d'identifiant lié à une personne. Événements
suivis : `onboarding_completed`, `challenge_completed` (avec `has_souvenir` et
`current_streak`), `premium_purchase_started`, `premium_unlocked` (uniquement
sur une vraie conversion, pas à chaque restauration d'abonnement au lancement),
`progress_exported`. Rétention J1/J7/J30 vient des événements de cycle de vie
automatiques du SDK, pas d'un event dédié.

⚠️ `AnalyticsConfig.API_KEY`/`HOST` sont des placeholders — sans instance
PostHog déployée, le SDK n'envoie simplement rien (pas de crash).

## Notes sur la couche repository

- Toutes les écritures multi-tables (compléter un défi, mettre à jour le streak)
  passent par `AppDatabase.withTransaction` pour rester atomiques.

## Relancement de l'app

`LittleBigStepsNavGraph` vérifie `UserPreferencesRepository` avant de créer le
`NavHost` : si l'onboarding a déjà été complété (`onboardingCompletedAt`
non-null), l'app démarre directement sur l'écran de sélection de défi au lieu
de repasser par l'onboarding.

## Ouvrir le projet

Ouvrir le dossier racine dans Android Studio (Koala ou plus récent) — le
wrapper Gradle sera généré automatiquement au premier sync. Le projet cible
`compileSdk 34`, `minSdk 26`.

## État actuel

Core loop complet (onboarding → sélection/complétion → portfolio →
progression) + notifications, export enrichi (image/PDF/story), capture photo,
Google Play Billing, packs thématiques, badges premium, analytics et suite de
tests E2E. Reste : catalogue final de défis (30-50/médium), config externe à
finaliser (Play Console, PostHog, CDN de prod), CI.
