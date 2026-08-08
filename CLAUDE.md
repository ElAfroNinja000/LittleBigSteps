# LittleBigSteps — app de pratique créative quotidienne

Application Android de découverte et pratique créative gamifiée, destinée au grand public curieux non-initié. Ce document sert de référence produit et technique pour le développement avec Claude Code.

## 1. Vision & positionnement

**Problème.** Beaucoup de gens ont envie de se lancer dans une pratique créative (dessin, écriture, artisanat, photo...) mais ne savent pas par où commencer, et abandonnent vite faute de structure, de retour visible sur leur progression, ou de discipline pour s'y tenir seuls.

**Cible.** Le grand public curieux, pas encore engagé dans une pratique créative régulière — pas des artistes confirmés qui cherchent à maintenir une discipline existante, mais des gens qui "ont toujours voulu essayer" sans jamais s'y mettre vraiment.

**Promesse.** Une app simple, sans compte, qui propose régulièrement de petits défis créatifs — médium choisi par l'utilisateur, seul ou en variant — gamifie la régularité pour donner envie de continuer, et construit un portfolio visible de sa progression dans le temps.

**Différenciation.** Sketch a Day et 4TheWords sont mono-médium et s'adressent à un public déjà engagé dans sa pratique. Habitica et Streaks sont génériques et n'offrent aucun contenu créatif propre. Le mouvement 100 Day Project a la demande mais aucun outil dédié. Cette app se positionne sur le créneau encore vide : découverte + gamification + suivi de progression, pensé pour un débutant, sans dépendance à un compte ou à une IA de génération de contenu.

## 2. Cible & personas

**Persona A — "L'envieux sans élan".** 30-45 ans, vie professionnelle chargée, a toujours dit "j'aimerais bien me mettre au dessin/à l'écriture" sans jamais commencer. Bloqué par la page blanche et le sentiment de ne pas savoir par où commencer. Cherche un point d'entrée à très faible friction — pas de matériel à acheter, pas de cours à suivre. Motivé par les petites victoires visibles plus que par la performance.

**Persona B — "Le zappeur créatif".** A déjà essayé plusieurs hobbies créatifs (carnet, feutres, cours en ligne) mais abandonne systématiquement après quelques jours, faute de structure ou de retour sur sa progression. Aime la variété, se lasse vite d'un seul médium. A besoin d'un cadre externe (gamification, streak) pour compenser une motivation qui s'essouffle vite seul.

Les deux personas partagent le point commun central : aucun n'a de pratique établie, contrairement aux utilisateurs actuels de 4TheWords ou du 100 Day Project.

## 3. Parcours utilisateur clé

1. **Onboarding (sans compte).** Ouverture directe de l'app, pas d'inscription. Questions rapides : mono-médium (lequel) ou multi-médium (découverte), fréquence souhaitée. Configuration en moins d'une minute, données stockées en local dès le départ.
2. **Découverte du défi.** L'app propose **toujours plusieurs défis** (2-3) à chaque itération, filtrés selon le(s) médium(s) choisis. L'utilisateur en sélectionne un.
3. **Réalisation & complétion.** L'utilisateur fait le défi à son rythme, puis marque "terminé". Il peut ajouter un **"souvenir"** (photo/texte/note) — purement optionnel et personnel, **aucune vérification implémentée**.
4. **Suivi & progression.** Mise à jour immédiate du streak et des points de gamification. Vue portfolio montrant l'historique dans le temps, par médium.
5. **Relance.** Notification périodique (selon fréquence choisie à l'onboarding) pour rappeler le défi du jour — sans culpabilisation en cas d'oubli.

## 4. Fonctionnalités MVP (core loop)

- **Sélection du défi** : plusieurs défis proposés à chaque session, filtrés par médium(s) choisis.
- **Complétion** : marquage "terminé" + souvenir optionnel non vérifié.
- **Gamification** : chaque défi complété rapporte des points dans la catégorie du médium concerné, avec un niveau qui progresse par catégorie. Streak global de régularité, **sans pénalité** en cas d'oubli — juste une remise à zéro du compteur, sans message culpabilisant.
- **Portfolio** : vue chronologique de tous les défis complétés et souvenirs associés, filtrable par médium et par date.
- **Notifications** : rappel périodique pour proposer les défis du jour/de la session.
- **Export** : génération d'un résumé/visuel de la progression (streak, niveaux, souvenirs) exportable en image ou PDF pour partage externe — pas de feed ni de compte, juste un export autonome.

## 5. Modèle de contenu

**Médiums au lancement (4) :**
- Photographie — barrière matérielle nulle (le téléphone suffit), résultat immédiat, souvenir natif.
- Dessin/croquis — papier et stylo suffisent, forte culture existante (Inktober, 100 Day Project), très visuel pour le portfolio.
- Écriture courte (micro-fiction, poésie, journaling créatif) — zéro matériel, résultat rapide, souvenir simple à stocker.
- Petit artisanat manuel simple (origami, collage, recyclage créatif) — matériel du quotidien, résultat tangible rapide.

Exclus au lancement : musique et danse (barrière d'accès trop haute, souvenir difficile à capturer). Cuisine créative envisagée en v2 (dilue le positionnement art/artisanat).

**Production.** Rédaction manuelle des défis, **pas de génération IA**. Volume cible : environ 30-50 défis par médium au lancement pour éviter la répétition ressentie. Chaque défi : titre, description courte, temps estimé, niveau (débutant par défaut).

**Renouvellement.** Contenu servi via un **backend JSON statique** (voir section Architecture). Pas de soumission communautaire de contenu au MVP — tout le contenu est éditorial.

## 6. Fonctionnalités communautaires

MVP = **uniquement export/partage de la progression** (image/PDF). Pas de feed, pas de likes, pas de commentaires, pas de compte — cohérent avec l'absence d'authentification.

## 7. Modèle économique

**Freemium**, avec une frontière naturelle alignée sur le choix fait à l'onboarding :

- **Gratuit** : accès complet à **un seul médium** au choix (défis, gamification, portfolio, export).
- **Premium (abonnement récurrent)** : déblocage des autres médiums + packs de défis thématiques/saisonniers + badges/cosmétiques exclusifs + formats d'export enrichis.

Abonnement plutôt qu'achat unique : le contenu se renouvelle en continu, un modèle récurrent correspond mieux à une valeur qui continue d'arriver dans le temps.

## 8. Métriques de succès & hors scope

**Métriques clés :**
- Activation : % de nouveaux utilisateurs complétant leur premier défi sous 48h.
- Rétention : J1/J7/J30, avec un focus sur la survie du streak à J7 (signal prédictif fort).
- Engagement : défis complétés par semaine par utilisateur actif.
- Conversion freemium → premium, en particulier chez les utilisateurs multi-médium.
- Croissance du portfolio (nombre de souvenirs ajoutés) comme proxy d'attachement émotionnel.

**Explicitement hors scope pour le MVP :**
- Pas de compte, pas de synchronisation cloud.
- Pas de génération de contenu par IA.
- Pas de feed communautaire, ni likes, ni commentaires.
- Pas de vérification/preuve des défis complétés.
- Musique et danse comme médiums.
- Suggestions personnalisées par IA.

## 9. Risques & hypothèses à tester

1. **Rétention sans levier social** : la gamification seule (streak, XP, portfolio) suffit-elle à retenir un utilisateur sans compte ni feed communautaire ?
2. **Production de contenu sans IA** : la charge de rédaction manuelle est-elle soutenable dans la durée ?
3. **Perte de progression perçue** : sans compte ni cloud, un changement d'appareil efface tout — risque non résolu, à mitiger a minima par un export/sauvegarde manuelle réimportable.
4. **Appétit multi-médium** : résolu en le laissant au choix de l'utilisateur plutôt qu'en le présumant.
5. **Barrière de conversion freemium** : un seul médium gratuit suffit-il à convaincre de payer pour débloquer les autres ?

## 10. Architecture technique

**Plateforme : Android uniquement.** Stack native plutôt que cross-platform, puisqu'il n'y a qu'une seule plateforme à couvrir.

- **Client** : Kotlin + Jetpack Compose, architecture MVVM.
- **Stockage local** : Room (couche SQLite officielle Android) pour les données structurées (préférences, défis complétés, streaks, XP/niveaux par médium, métadonnées des souvenirs). Fichiers médias (photos) dans le stockage interne de l'app, référencés par chemin dans Room. Aucune synchro cloud.
- **Contenu = JSON statique** : fichiers JSON hébergés sur un hébergement statique simple (GitHub Pages, Cloudflare Pages, ou bucket + CDN) — aucun serveur applicatif, aucune base de données côté serveur, aucun CMS. Édition manuelle des fichiers, déploiement statique. L'app télécharge le JSON au lancement + périodiquement, avec un champ de version pour détecter les mises à jour sans tout retélécharger, et met en cache local (Room) pour l'usage hors-ligne.
- **Notifications** : WorkManager/AlarmManager natif Android pour les rappels programmés localement — pas de Firebase Cloud Messaging nécessaire (rien n'est personnalisé à distance).
- **Paiement freemium** : Google Play Billing Library directement — un seul store à gérer.
- **Export** : génération d'image/PDF localement via les APIs natives Android (Canvas/Bitmap).
- **Analytics** : SDK Android d'un outil respectueux de la vie privée avec ID anonyme (PostHog auto-hébergé recommandé), pour suivre les métriques de la section 8. Aucune collecte de PII, cohérent avec l'absence de compte.

### Résumé des composants

| Composant | Rôle |
|---|---|
| App Android (Kotlin/Compose) | Cœur du produit — logique de jeu et données utilisateur en local |
| Fichiers JSON statiques (hébergement CDN) | Édition et diffusion des défis uniquement, aucune donnée utilisateur |
| Google Play Billing | Gestion de l'abonnement premium |
| PostHog (ou équivalent) | Analytics anonyme pour piloter le produit |

## 11. Stratégie de test automatisé

**Approche.** Priorité aux tests bout-en-bout (E2E) sur le parcours cœur
(onboarding → sélection de défi → complétion avec souvenir → portfolio →
progression, voir §3) plutôt qu'une pyramide de tests classique complète : la
valeur immédiate est de garantir que le parcours critique ne casse pas à
chaque itération. Des tests unitaires ciblés (règles de gamification, logique
de streak) viendront compléter au fur et à mesure que cette logique se
stabilise et se complexifie.

**Framework retenu : Jetpack Compose UI Testing** (`androidx.compose.ui.test` +
`AndroidJUnit4`), pas Espresso — toute l'UI de l'app est en Compose, Espresso
n'apporterait qu'une couche d'interop inutile pour zéro bénéfice ici. Les tests
sont instrumentés (`app/src/androidTest`), exécutés sur émulateur/appareil réel
via `./gradlew connectedAndroidTest` ou directement depuis Android Studio.

**Ce qui est réel vs simulé dans les tests E2E :**
- **Réel** : Room (base en mémoire, fraîche à chaque test), tous les
  repositories, tous les ViewModels, la navigation Compose, les écrans
  eux-mêmes. Le but est d'exercer le vrai code de l'app, pas une maquette.
- **Simulé (fakes légers, pas de mocks)** : uniquement les vraies frontières
  externes que l'environnement de test ne peut pas exercer de façon fiable —
  `BillingRepository` (pas de vrai Play Store en test), `NotificationScheduler`
  (pas de vrai WorkManager programmé), `AnalyticsTracker` (pas de vrai réseau
  PostHog). Le contenu des défis est injecté directement dans Room via les DAOs
  plutôt que synchronisé depuis le réseau, pour ne pas faire dépendre les tests
  d'une connexion.

**Où :** `app/src/androidTest/java/com/littlebigsteps/app/` —
`TestAppGraph` construit le même graphe de dépendances que
`LittleBigStepsApplication` (voir ce fichier) mais avec les fakes ci-dessus ;
les fakes vivent dans `fakes/`. Un test = un parcours utilisateur complet, pas
un test par écran isolé.

**Hors scope pour l'instant** (nécessite un environnement réel, pas
automatisable simplement) : flux d'achat Play Billing réel (nécessite une
piste de test Play Console), livraison réelle de notifications, synchronisation
réseau réelle du contenu JSON. À couvrir manuellement ou via des tests de
contrat séparés si le besoin se confirme plus tard.

**CI.** Pas encore de pipeline CI dans ce repo — les tests tournent en local
pour l'instant (Android Studio ou `./gradlew connectedAndroidTest` sur un
appareil/émulateur connecté).

## 12. État d'implémentation (2026-08-08)

Le core loop complet (§3) est implémenté et fonctionnel côté code : onboarding
→ sélection de défi → complétion avec souvenir → portfolio → progression.
Tout le MVP (§4) et le modèle économique (§7, packs et badges inclus) sont
codés. **Jamais testé sur émulateur/appareil réel** — voir §14.

| Domaine | État | Fichiers clés |
|---|---|---|
| Modèle de données (Room) | ✅ | `data/local/entity/`, `data/local/dao/`, [docs/data-model.md](docs/data-model.md) |
| Onboarding | ✅ | `ui/onboarding/` |
| Sélection/complétion de défi | ✅ | `ui/challenge/` |
| Portfolio | ✅ | `ui/portfolio/` |
| Streak/XP/niveaux | ✅ | `data/repository/ProgressRepository.kt`, `domain/GamificationRules.kt` |
| Souvenir photo (caméra + galerie) | ✅ | `data/media/SouvenirPhotoStore.kt` |
| Notifications de rappel | ✅ | `notification/` (WorkManager, pas de FCM) |
| Export image/PDF/story | ✅ | `export/` — enrichi en premium (photos, badges, format story) |
| Google Play Billing | ✅ code, ⚠️ produit à créer en Play Console (§13) |
| Packs thématiques/saisonniers | ✅ code, ⚠️ 1 seul pack placeholder dans `/content` |
| Badges premium | ✅ | `domain/BadgeEvaluator.kt`, `domain/model/Badge.kt` |
| Analytics (PostHog) | ✅ code, ⚠️ instance à créer (§13) |
| Synchro contenu JSON | ✅ | `data/repository/ContentSyncRepository.kt` — pointe sur `raw.githubusercontent.com` de ce repo (provisoire, voir §13) |
| Skip onboarding au relancement | ✅ | `ui/navigation/NavGraph.kt` |
| Tests E2E (Compose UI Testing) | ✅ | `app/src/androidTest/`, voir §11 |
| Catalogue de défis final (30-50/médium) | ❌ placeholder only (10/médium) | `/content` |
| CI | ❌ | — |

**Pas de framework DI** (Hilt etc.) — service locator manuel dans
`LittleBigStepsApplication.kt`. Chaque écran a un `XxxViewModelFactory` construit
à la main dans `ui/MainActivity.kt` et passé à `LittleBigStepsNavGraph`.

## 13. Config externe en attente

Trois placeholders bloquent la mise en prod réelle — aucun n'est codable
depuis un agent seul (comptes/paiement/consoles externes) :

1. **Play Console** — `PREMIUM_SUBSCRIPTION_ID` dans
   [PlayBillingRepository.kt](app/src/main/java/com/littlebigsteps/app/billing/PlayBillingRepository.kt)
   (`"premium_subscription"`) doit correspondre à un abonnement réellement créé
   dans Play Console (app buildée avec `applicationId = "com.littlebigsteps.app"`).
2. **PostHog** — `AnalyticsConfig.API_KEY`/`HOST` dans
   [AnalyticsConfig.kt](app/src/main/java/com/littlebigsteps/app/analytics/AnalyticsConfig.kt)
   sont des placeholders `TODO-...`. Sans clé valide, le SDK n'envoie rien
   silencieusement (pas de crash).
3. **CDN de contenu** — `NetworkConfig.CONTENT_BASE_URL` dans
   [NetworkConfig.kt](app/src/main/java/com/littlebigsteps/app/data/remote/NetworkConfig.kt)
   pointe sur `raw.githubusercontent.com/ElAfroNinja000/LittleBigSteps/master/content/`
   (fonctionnel, testé, mais couplé au repo de code). GitHub Pages sur ce même
   repo (Settings → Pages → branche `master`, dossier `/content`) est l'option
   la plus simple pour en sortir sans nouveau compte.

## 14. Pour reprendre ce projet

- **Jamais buildé/lancé réellement** : pas de JDK 17+/SDK Android/Android
  Studio disponibles sur la machine où ce projet a été développé jusqu'ici. Le
  code n'a donc jamais été compilé — traiter tout comme non-vérifié tant qu'un
  premier build réel n'a pas eu lieu.
- **Repo distant** : `https://github.com/ElAfroNinja000/LittleBigSteps`,
  branche `master`. Tout est poussé au fil de l'eau, un commit par
  fonctionnalité (voir `git log` pour l'historique détaillé des décisions).
- **Point d'entrée pour explorer le code** : `LittleBigStepsApplication.kt`
  (service locator, tout est construit là) → `ui/MainActivity.kt` (câblage des
  ViewModels) → `ui/navigation/NavGraph.kt` (parcours de navigation complet).
- **Avant d'ajouter une fonctionnalité** : vérifier §12 pour l'état actuel, et
  chercher si un repository/DAO existant peut être étendu plutôt que dupliqué
  (le pattern DAO one-shot `getOnce()`/`getAllX()` + Flow `observeX()` est
  systématique, voir n'importe quel fichier dans `data/local/dao/`).
- **README.md** contient un miroir plus court de cet état d'implémentation,
  organisé par fonctionnalité plutôt que par section produit — utile en
  complément rapide.

---

*Document de travail — à faire évoluer au fil des itérations (détail des personas, catalogue final de défis, etc.). §12-14 à tenir à jour à chaque session de travail significative.*
