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

content/                  # exemples de fichiers JSON statiques (manifest + par médium)
docs/data-model.md         # référence du schéma de données
```

## Notifications

`NotificationScheduler` (WorkManager, pas de FCM) programme un rappel périodique
juste après l'onboarding, selon la fréquence et l'heure choisies. La permission
`POST_NOTIFICATIONS` (API 33+) est demandée à ce moment-là ; un refus reste
silencieux, sans blocage ni message culpabilisant.

## Export

`ProgressExportGenerator` dessine le résumé de progression (streak, niveaux,
derniers souvenirs) sur un Canvas natif partagé entre image PNG et PDF
(`ExportRenderer`), écrit dans le cache de l'app, puis le partage via
`FileProvider` + l'Intent système (`ACTION_SEND`) — pas de feed, pas de compte,
juste un export autonome (CLAUDE.md §6). Boutons sur l'écran Progression.

## Notes sur la couche repository

- Toutes les écritures multi-tables (compléter un défi, mettre à jour le streak)
  passent par `AppDatabase.withTransaction` pour rester atomiques.
- `NetworkConfig.CONTENT_BASE_URL` est un placeholder — à remplacer par l'URL
  CDN réelle une fois le contenu déployé.
- Pas encore branché à l'UI : les ViewModels restent à écrire.

## Ouvrir le projet

Ouvrir le dossier racine dans Android Studio (Koala ou plus récent) — le
wrapper Gradle sera généré automatiquement au premier sync. Le projet cible
`compileSdk 34`, `minSdk 26`.

## État actuel

Squelette : structure Gradle, entités/DAOs Room réels, thème Compose et écran
placeholder. Pas encore implémenté : onboarding, sélection de défi,
synchronisation du contenu distant, notifications, export, billing.
