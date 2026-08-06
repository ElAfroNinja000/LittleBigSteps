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
├── LittleBigStepsApplication.kt
├── domain/model/       # enums partagés (MediumType, ChallengeLevel, Frequency)
├── data/
│   ├── local/           # Room : entités, DAOs, base, convertisseurs
│   └── remote/dto/      # DTOs du contenu JSON distant
└── ui/
    ├── theme/            # thème Compose (Material3)
    └── MainActivity.kt

content/                  # exemples de fichiers JSON statiques (manifest + par médium)
docs/data-model.md         # référence du schéma de données
```

## Ouvrir le projet

Ouvrir le dossier racine dans Android Studio (Koala ou plus récent) — le
wrapper Gradle sera généré automatiquement au premier sync. Le projet cible
`compileSdk 34`, `minSdk 26`.

## État actuel

Squelette : structure Gradle, entités/DAOs Room réels, thème Compose et écran
placeholder. Pas encore implémenté : onboarding, sélection de défi,
synchronisation du contenu distant, notifications, export, billing.
