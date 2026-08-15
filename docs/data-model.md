# Modèle de données

Document de référence pour le schéma local (Room) et le contenu distant (JSON
statique). Voir aussi [CLAUDE.md](../CLAUDE.md) pour le contexte produit complet.

Décisions actées :
- Un défi peut être refait plusieurs fois (pas de contrainte d'unicité sur les complétions).
- Le souvenir d'une complétion peut combiner photo **et** note texte librement (deux champs nullable indépendants).
- Fréquence de rappel : nombre de fois par semaine (1 à 7), choisi via une roue de sélection numérique à l'onboarding — pas de presets.
- Une activité passe par un statut auto-déclaré (Brouillon/En cours/Terminé, jauge tappable) entre le choix et la finalisation — aucune vérification (CLAUDE.md §9).

## Enums

- **MediumType** : `PHOTO`, `DRAWING`, `WRITING`, `CRAFT`
- **ChallengeLevel** : `BEGINNER` (seul niveau au lancement), `INTERMEDIATE`, `ADVANCED` (réservés au futur)
- **ChallengeStatus** : `DRAFT`, `IN_PROGRESS`, `DONE` — statut d'une activité "En cours" (voir ChallengeProgressEntity)

**Frequency** n'est plus un enum mais une classe (`timesPerWeek: Int`, 1 à 7),
stockée telle quelle (Int) en base.

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
| isPremiumOnly | Boolean | exclu des suggestions du jour tant que non premium |
| tags | List\<String\>? | saisonnier, thème... |
| packId | String? | null = catalogue de base ; sinon FK logique vers `ChallengePackEntity.id` |

### ChallengePackEntity — pack thématique/saisonnier premium (CLAUDE.md §7)
| champ | type | note |
|---|---|---|
| id | String (PK) | |
| mediumType | MediumType | |
| title / description | String | |
| isPremiumOnly | Boolean | `true` par défaut |

Un pack se parcourt en entier (pas de tirage aléatoire) une fois débloqué ;
ses défis sont exclus du tirage `pickDailyOptions` (voir ChallengeRepositoryImpl).

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

### ChallengeProgressEntity — une activité "En cours" (PK = challengeId)
Au plus une ligne par défi : on ne peut pas démarrer deux fois la même
activité sans d'abord la finaliser.
| champ | type | note |
|---|---|---|
| challengeId | String (PK, FK → ChallengeEntity.id, `CASCADE` on delete) | |
| mediumType | MediumType | dénormalisé, pour filtrer sans jointure |
| status | ChallengeStatus | Brouillon/En cours/Terminé, avancé par tap direct sur la jauge |
| startedAt | Instant | fixé au `startChallenge`, jamais modifié par un changement de statut |

Créée par `ChallengeRepositoryImpl.startChallenge` ("Choisir" sur une nouvelle
activité), supprimée par `completeChallenge` (finalisation, même transaction
que l'enregistrement dans `CompletedChallengeEntity`). Exclut le défi des
prochains tirages `pickDailyOptions` tant qu'elle existe.

### ContentManifestEntity — singleton
Suit la version synchronisée de `packs.json` (contenu additionnel, pas lié à
un médium en particulier — le suivi par médium se fait via
`MediumContentVersionEntity`).
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
      "tags": ["object", "still-life"],
      "packId": null
    }
  ]
}
```

`packs.json` — packs thématiques/saisonniers premium (§7), synchronisés à part,
optionnel (son absence ne fait pas échouer la synchro du catalogue de base) :

```json
{
  "version": "1",
  "packs": [
    {
      "id": "pack_rentree_creative",
      "mediumId": "drawing",
      "title": "Pack Rentrée créative",
      "description": "...",
      "isPremiumOnly": true
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

### InProgressChallengeEntity — projection de requête (pas une table)
Jointure `challenge_progress` + `challenges` (défi complet), utilisée par la
section "En cours" de l'écran Mes activités.
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
