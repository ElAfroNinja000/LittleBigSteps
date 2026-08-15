---
name: defi
description: Rédiger, modifier ou relire des défis du catalogue LittleBigSteps. À utiliser dès qu'il s'agit d'ajouter des défis, d'étoffer un médium, de créer un pack thématique, ou de traduire/corriger du contenu de /content. Couvre les règles éditoriales, le format exact et la procédure de publication.
---

# Rédiger un défi

Le catalogue est l'unique chantier de contenu ouvert du projet : ~10 défis par
médium aujourd'hui, cible 30-50 (CLAUDE.md §3, §9).

**L'IA rédige, l'utilisateur relit avant commit** (décision §8). Ne jamais
committer du contenu non relu, même si tout le reste est vert.

## 1. Règles éditoriales — ce qui fait qu'un défi appartient à cette app

La cible n'a **aucune pratique établie** (§1). Chaque défi passe ces cinq tests :

| Test | Formulation | Contre-exemple à rejeter |
|---|---|---|
| **Matériel nul** | Réalisable avec ce qu'on a déjà chez soi ou dans la poche. | « Utilise un objectif macro », « prends du papier aquarelle 300g ». |
| **Aucun prérequis** | Aucun vocabulaire ni geste d'expert à connaître. | « Travaille la règle des tiers en contre-jour », « fais un dégradé en hachures croisées ». |
| **Souvenir photographiable** | Le résultat se photographie — la photo est **obligatoire** pour finaliser (§8), y compris en écriture et en artisanat. | « Écris un texte dans ta tête », « observe un paysage ». |
| **Petit** | Tient dans le temps annoncé, sans préparation. | « Réalise une série de 10 portraits ». |
| **Ton non culpabilisant** | Une invitation, jamais une exigence ni une évaluation. Pas de « tu dois », pas de « réussis à ». | « Ne t'arrête pas avant d'avoir réussi », « refais-le jusqu'à ce que ce soit bon ». |

**Voix** : tutoiement, phrase courte, verbe d'action en tête. Le titre nomme
l'objet du défi, pas la performance attendue (« Une ombre intéressante », pas
« Maîtrise les ombres »).

**Les 3 conseils (`tips`)** débloquent, ils n'évaluent pas. Un bon trio :
1. quand / où s'y prendre pour que ce soit facile ;
2. un cadrage ou un angle d'attaque concret ;
3. une variante pour qui veut aller un peu plus loin.

## 2. Format

Un défi vit dans `content/fr/<medium>.json` **et** `content/en/<medium>.json`.
Médiums : `photo`, `drawing`, `writing`, `craft` — la liste est fermée
(musique et danse sont hors scope, §3).

```json
{
  "id": "photo_011",
  "title": "Une ombre intéressante",
  "description": "Trouve et photographie une ombre qui te plaît, où que tu sois.",
  "estimatedMinutes": 10,
  "level": "beginner",
  "isPremiumOnly": false,
  "tags": ["lumière", "quotidien"],
  "tips": ["…", "…", "…"],
  "packId": "pack_rentree_creative"
}
```

| Champ | Règle |
|---|---|
| `id` | `<medium>_NNN` sur 3 chiffres, séquentiel. Défi de pack : `<medium>_pack_<slug>_NNN`. **Identique en fr et en.** |
| `title` | Court, concret, traduit. |
| `description` | 1-2 phrases. Dit quoi faire, pas comment bien le faire. |
| `estimatedMinutes` | 5, 10, 15 ou 20. Rien au-delà : le format est « petit défi ». |
| `level` | `beginner` partout aujourd'hui. `intermediate`/`advanced` existent mais ne sont pas utilisés — ne pas en introduire sans décision produit. |
| `isPremiumOnly` | `false` par défaut. `true` uniquement pour les défis de pack. |
| `tags` | 1-3 tags, traduits, en minuscules. |
| `tips` | Exactement 3, traduits. |
| `packId` | Absent hors pack. Doit exister dans `packs.json`. |

**L'`id` est la clé de tout.** Il relie un défi aux souvenirs du portfolio et aux
activités en cours. Renommer un `id` existant, c'est détacher les souvenirs déjà
créés par les utilisateurs : on ajoute, on ne renumérote jamais.

## 3. Publication — la partie qu'on oublie

Sans cette étape, le contenu est écrit mais n'atteint personne : les gens qui
ont déjà l'app ne le verront jamais.

1. Écrire le défi dans `content/fr/<medium>.json` **et** `content/en/<medium>.json`,
   même `id`, même ordre.
2. Incrémenter `mediums[].version` du médium concerné dans
   `content/fr/manifest.json` **et** `content/en/manifest.json` — **même numéro
   des deux côtés**.
3. Pour un pack : incrémenter aussi le champ `version` de `packs.json`
   (il a le sien, le manifeste ne le référence pas).
4. Soumettre à relecture. Attendre la validation.
5. Committer et pousser.

Un garde-fou alerte si les étapes 2-3 sont oubliées ou si fr et en divergent,
mais il ne dispense pas de les faire.

## 4. Avant de rendre

- [ ] Autant de défis en fr qu'en en, mêmes `id`, même ordre
- [ ] Aucun `id` en double dans le fichier ni réutilisé d'un défi supprimé
- [ ] 3 `tips` par défi, des deux côtés
- [ ] Les cinq tests éditoriaux passés sur chaque défi
- [ ] Versions incrémentées, fr et en alignées
- [ ] JSON valide (`ConvertFrom-Json` sur les fichiers touchés)

Présenter le résultat à la relecture sous forme lisible — titre, durée,
description, conseils — pas sous forme de JSON brut.
