---
name: checklist
description: Produire la checklist de test manuel après une fonctionnalité ou un correctif LittleBigSteps. À utiliser au moment de livrer quelque chose à tester sur appareil, ou quand l'utilisateur demande quoi tester, comment vérifier, ou s'apprête à installer un APK.
---

# Livrer une checklist de test

Le projet n'a **aucun test automatisé** et n'en aura pas (CLAUDE.md §8). Le test
manuel est donc la seule vérification réelle, et « teste l'app » n'en est pas
une. C'est l'étape 7 du process de production.

## Règle des préconditions

**Sans précondition, un test ne prouve rien.** Le correctif contre la perte de
souvenirs ne pouvait être validé que sur des données créées *après*
l'installation : testé sur une app fraîche, il passait toujours, même cassé.

Toute checklist s'ouvre donc sur un bloc de préconditions explicites :

- **Installation** : app neuve (désinstallée au préalable) ou mise à jour
  par-dessus l'existant ? Les deux se comportent différemment dès qu'il est
  question de données ou de contenu.
- **Version à installer** : gratuite ou premium. Les deux cohabitent sur le même
  appareil, ce sont deux icônes distinctes — vérifier qu'on teste la bonne.
- **Données déjà présentes** : quels défis en cours, quels souvenirs, créés
  quand. Préciser ce qu'il faut créer *avant* de déclencher le test.
- **Réseau** : connecté, coupé (mode avion), ou coupé en cours d'opération.
- **Langue** : français ou anglais, et faut-il en changer pendant le test.
- **Compte Google** : nécessaire dès qu'on touche à l'abonnement.

## Ordre : par risque, pas par parcours

Ce qui casse le plus gravement se teste en premier — inutile de vérifier un
alignement si l'app ne démarre pas.

1. **L'app démarre-t-elle ?** Après installation neuve *et* après mise à jour.
2. **Les données survivent-elles ?** Souvenirs, défis en cours, progression,
   après relance et après mise à jour.
3. **L'état payant survit-il ?** Notamment après une coupure réseau et après
   redémarrage.
4. **La fonctionnalité livrée fait-elle ce qui est attendu ?** Cas nominal.
5. **Que se passe-t-il quand ça se passe mal ?** Réseau coupé, permission
   refusée, annulation en cours de route.
6. **Le reste** : mise en page, textes, traductions.

## Forme de chaque test

Trois colonnes, jamais une consigne seule :

| # | Faire | Résultat attendu | Signe que c'est cassé |
|---|---|---|---|

Le troisième point est le plus utile : il évite de valider par défaut un test
dont on n'a pas su lire le résultat.

## Clôture obligatoire

Terminer par **« ce que cette checklist ne prouve pas »**. Une liste honnête,
courte : ce qui n'a pas pu être testé sans compte Play réel, sans deuxième
appareil, sans attendre plusieurs jours, sans contenu publié sur le CDN.

## Formulation

L'utilisateur est product manager : décrire ce qu'il voit à l'écran et ce qu'il
touche, jamais des noms de fichiers ou d'écrans internes. « Ouvre l'onglet
Progression », pas « lance ProgressScreen ».
