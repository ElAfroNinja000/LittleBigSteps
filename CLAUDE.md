# App de pratique créative quotidienne (nom à définir)

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

---

*Document de travail — à faire évoluer au fil des itérations (nom de l'app, détail des personas, catalogue initial de défis, etc.)*
