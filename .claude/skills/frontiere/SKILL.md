---
name: frontiere
description: Analyser une frontière externe avant de coder — réseau/CDN, Play Billing, système Android, base de données, permissions, export/partage. À utiliser dès qu'une fonctionnalité dépend de quelque chose que l'app ne contrôle pas, ou avant de modifier du code qui touche déjà l'un de ces systèmes.
---

# Analyser une frontière externe

Constat de ce projet : **aucun bug sérieux n'a été attrapé par la compilation**,
et les plus graves sont tous nés au contact d'un système externe (CLAUDE.md
§0 bis). C'est l'étape 3 du process de production, à faire **avant** d'écrire du
code, par écrit.

## Le piège central

Les deux pires bugs du projet ont la même forme : **une panne a été confondue
avec un résultat légitime.**

- La restauration d'achats ne distinguait pas « Google ne répond pas » de
  « cette personne n'a rien acheté ». Résultat : un abonné payant rétrogradé en
  gratuit après une simple coupure réseau.
- La synchro de contenu avalait ses erreurs en silence. Résultat : un
  dysfonctionnement invisible, donc impossible à diagnostiquer.

Une frontière externe a toujours **trois** issues, jamais deux : ça marche, ça
répond « rien », ça ne répond pas. Les deux dernières ne doivent jamais être
traitées pareil.

## Les six questions — à répondre par écrit avant de coder

1. **Quelles sont les pannes possibles ?** Réseau coupé, lenteur, réponse
   malformée, service absent, refus de permission, version d'Android
   différente, ressource supprimée entre-temps.
2. **Comment distingue-t-on une panne d'un résultat vide légitime ?** Si la
   réponse est « on ne peut pas », c'est un défaut de conception à corriger
   avant de continuer, pas un cas limite.
3. **Que voit l'utilisateur dans chaque cas ?** Un échec silencieux est un
   choix, et presque toujours le mauvais.
4. **Est-ce qu'une panne peut dégrader un état acquis ?** Perdre le premium,
   perdre un souvenir, remettre une progression à zéro, réinitialiser des
   préférences. Par défaut, une panne ne retire jamais rien : elle ne fait rien.
5. **Est-ce qu'une écriture destructive se produit sur le chemin d'échec ?**
   Supprimer puis réinsérer entraîne la disparition des activités en cours et
   détache les souvenirs du portfolio. La règle du projet est : mettre à jour,
   puis élaguer — jamais tout effacer d'abord.
6. **Est-ce que ce changement rend fréquent un chemin jusque-là rare ?** Le bug
   de perte de données n'est apparu que parce qu'un correctif rendait la
   resynchronisation fréquente. Un chemin rare qui devient courant doit être
   réaudité comme s'il était neuf.

## Vérifier, ne pas supposer

Toute affirmation du type « cette bibliothèque fait X » est soit **vérifiée**
(source, documentation officielle, comportement observé), soit **marquée
explicitement comme hypothèse** dans la réponse.

Une hypothèse fausse sur le comportement d'Android a déjà coûté ici : une
fonctionnalité morte, deux fichiers de documentation faux, deux cycles de
correction. La vérification aurait pris trois commandes.

## Les frontières connues du projet

| Frontière | Ce qui a déjà mal tourné, ou ce qui guette |
|---|---|
| **CDN de contenu** | Erreurs avalées en silence ; contenu jamais retéléchargé faute de version incrémentée. |
| **Google Play Billing** | Panne confondue avec « aucun achat » → abonné rétrogradé. Le produit n'existe pas encore côté Play : tout se teste en simulation. |
| **Système Android** | Le sélecteur de langue impose un socle technique précis ; s'en écarter fait planter l'app au démarrage — invisible à la compilation. |
| **Base de données locale** | Les suppressions se propagent en cascade : effacer un défi efface l'activité en cours et détache les souvenirs. |
| **Permissions** | Notifications et appareil photo peuvent être refusées, et révoquées après coup. |
| **Export / partage** | L'app cible peut ne pas exister, ou refuser le fichier. |
| **Analytics** | Sans clé valide, rien n'est envoyé — silencieusement. |

## Ce que produit cette analyse

Un tableau court, dans la réponse à l'utilisateur, **avant** le code :

| Panne | Détectée comment | Ce que fait l'app | Ce que voit l'utilisateur |
|---|---|---|---|

Puis, à la fin de l'implémentation, la liste de ce qui **reste non vérifié** —
une compilation réussie ne prouve rien du comportement.
