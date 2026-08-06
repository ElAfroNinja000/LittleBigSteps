# Modèle de données

Document de référence pour le schéma local (Room) et le contenu distant (JSON
statique). Voir aussi [CLAUDE.md](../CLAUDE.md) pour le contexte produit complet.

Décisions actées :
- Un défi peut être refait plusieurs fois (pas de contrainte d'unicité sur les complétions).
- Le souvenir d'une complétion peut combiner photo **et** note texte librement (deux champs nullable indépendants).
- Fréquence de rappel : trois presets à l'onboarding — quotidien, quelques fois par semaine, hebdomadaire.

## Enums

- **MediumType** : `PHOTO`, `DRAWING`, `WRITING`, `CRAFT`
- **ChallengeLevel** : `BEGINNER` (seul niveau au lancement), `INTERMEDIATE`, `ADVANCED` (réservés au futur)
- **Frequency** : `DAILY`, `FEW_TIMES_WEEK`, `WEEKLY`

## Entités Room (données locales utilisateur)

### UserPreferencesEntity — singleton (id fixé)
| champ | type | note |
|---|---|---|
| selectedMediums | List\<MediumType\> | médiums choisis à l'onboarding (mono ou multi) |
| freeMedium | MediumType | le médium gratuit débloqué sans premium |
| reminderFrequency | Frequency | |
| reminderTime | LocalTime | heure du rappel |
| isPremium | Boolean | |
| onboardingCompletedAt | Instant? | |

### GlobalProgressEntity — singleton
Le streak est **global**, pas par médium.
| champ | type |
|---|---|
| currentStreak | Int |
| longestStreak | Int |
| lastCompletionDate | LocalDate? |
| totalChallengesCompleted | Int |

### MediumProgressEntity — une ligne par médium (PK = mediumType)
| champ | type | note |
|---|---|---|
| xp | Int | |
| level | Int | dérivé d'xp via une formule en couche service, pas stocké en dur |
| challengesCompletedCount | Int | |
| isUnlocked | Boolean | `true` si `freeMedium == ce médium` OU `isPremium` |

### ChallengeEntity — cache local du JSON distant
| champ | type | note |
|---|---|---|
| id | String (PK) | id stable venant du JSON, jamais généré côté app |
| mediumType | MediumType | |
| title / description | String | |
| estimatedMinutes | Int | |
| level | ChallengeLevel | |
| isPremiumOnly | Boolean | packs thématiques premium |
| tags | List\<String\>? | saisonnier, thème... |

### CompletedChallengeEntity — une ligne par complétion
Un défi peut être refait : pas de contrainte d'unicité sur `challengeId`.
| champ | type | note |
|---|---|---|
| id | Long (PK autogen) | |
| challengeId | String? (FK → ChallengeEntity.id, `SET_NULL` on delete) | l'historique survit si le défi disparaît du catalogue |
| mediumType | MediumType | dénormalisé, pour filtrer le portfolio sans jointure |
| completedAt | Instant | |
| souvenirPhotoPath | String? | chemin stockage interne |
| souvenirNote | String? | photo et note coexistent librement |
| xpEarned | Int | snapshot au moment du complete (robuste si les règles XP changent) |

### ContentManifestEntity — singleton
| champ | type |
|---|---|
| contentVersion | String |
| lastSyncAt | Instant |

## Contenu distant (JSON statique, hors Room)

Voir `/content` à la racine du repo pour des exemples. Hébergé sur un CDN
statique (GitHub Pages, Cloudflare Pages...), édité manuellement, aucun serveur
applicatif.

`manifest.json` — référence chaque fichier médium + sa version, pour ne
retélécharger que ce qui a changé :

```json
{
  "version": "2026.08.1",
  "mediums": [
    { "id": "drawing", "file": "drawing.json", "version": "3" },
    { "id": "photo", "file": "photo.json", "version": "2" }
  ]
}
```

Un fichier par médium (ex. `drawing.json`) :

```json
{
  "mediumId": "drawing",
  "challenges": [
    {
      "id": "drawing_001",
      "title": "...",
      "description": "...",
      "estimatedMinutes": 15,
      "level": "beginner",
      "isPremiumOnly": false,
      "tags": ["object", "still-life"]
    }
  ]
}
```

## Relations

`ChallengeEntity` 1—N `CompletedChallengeEntity` (via `challengeId`).
`MediumProgressEntity` s'incrémente à chaque complétion (xp, count).
`GlobalProgressEntity` gère le streak transversalement, indépendamment du médium.

### PortfolioEntryEntity — projection de requête (pas une table)
Jointure `completed_challenges` + `challenges` (titre), utilisée par le portfolio.
`challengeTitle` est `null` si le défi a depuis disparu du catalogue.

### MediumContentVersionEntity — une ligne par médium
Dernière version de contenu synchronisée, comparée à `manifest.json` pour ne
retélécharger que ce qui a changé.
| champ | type |
|---|---|
| mediumType | MediumType (PK) |
| syncedVersion | String |

## Correspondance code

- Entités Room : `app/src/main/java/com/littlebigsteps/app/data/local/entity/`
- DAOs : `app/src/main/java/com/littlebigsteps/app/data/local/dao/`
- Base de données : `app/src/main/java/com/littlebigsteps/app/data/local/AppDatabase.kt`
- DTOs contenu distant : `app/src/main/java/com/littlebigsteps/app/data/remote/dto/`
- Service HTTP : `app/src/main/java/com/littlebigsteps/app/data/remote/ContentApiService.kt`
- Repositories : `app/src/main/java/com/littlebigsteps/app/data/repository/`
  (`ChallengeRepository`, `ProgressRepository`, `UserPreferencesRepository`, `ContentSyncRepository`)
- Règles de gamification (XP/niveau) : `app/src/main/java/com/littlebigsteps/app/domain/GamificationRules.kt`
- Enums domaine : `app/src/main/java/com/littlebigsteps/app/domain/model/`
