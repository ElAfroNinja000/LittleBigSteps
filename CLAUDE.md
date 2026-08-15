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
- **Premium (abonnement récurrent)** : déblocage des autres médiums + packs de défis thématiques/saisonniers + formats d'export enrichis.

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

## 11. Stratégie de test

**Décision (2026-08-15) : pas de tests automatisés.** L'app a eu un temps un
test E2E Compose UI Testing (`app/src/androidTest`, `CoreLoopE2ETest` +
`TestAppGraph` + fakes) couvrant le parcours cœur. Supprimé à la demande de
l'utilisateur : il teste systématiquement chaque build manuellement sur
appareil réel, ce qui suffit à détecter les régressions à ce stade du projet.
Le test n'avait de toute façon jamais tourné une seule fois depuis son
écriture (voir §14 — l'émulateur/appareil n'est lancé que sur consigne
explicite), et ses sélecteurs avaient déjà dérivé du code réel (ex. taps sur
des roues remplacées depuis par des steppers) sans que rien ne le signale :
un filet de sécurité qu'on ne fait jamais tourner n'en est pas un.

**Conséquence directe pour la suite du travail** : aucune régression n'est
détectée avant que l'utilisateur ne teste manuellement le build. Compiler
(`assembleFreeDebug`/`assemblePremiumDebug`, toujours autorisé sans consigne
explicite, voir §14) reste donc la seule vérification disponible avant retour
utilisateur — elle attrape les erreurs de compilation, jamais les erreurs de
comportement. D'où une prudence particulière sur les changements à risque
runtime non couvrables par la compilation (ex. `AppCompatActivity`/thème,
migrations Room destructives, logique de synchro asynchrone) : les signaler
explicitement plutôt que les considérer validés par un simple build vert.

**Si le besoin resurgit** (croissance de l'équipe, régressions répétées sur le
parcours cœur) : Jetpack Compose UI Testing (`androidx.compose.ui.test` +
`AndroidJUnit4`) reste le choix naturel, toute l'UI étant en Compose — a déjà
fait ses preuves ici pour ce que ça vaut. Repartir du dernier commit contenant
`app/src/androidTest` dans l'historique git plutôt que de zéro.

## 12. État d'implémentation (2026-08-13)

Le core loop complet (§3) est implémenté et fonctionnel côté code : onboarding
→ sélection de défi → complétion avec souvenir → portfolio → progression.
Tout le MVP (§4) et le modèle économique (§7, packs inclus) sont codés.
**Jamais testé sur émulateur/appareil réel** — voir §14.

**Refonte UX/DA appliquée le 2026-08-13** (onboarding, Mes activités,
Portfolio, Progression) : palette mint/blanc cassé cohérente par médium via
`mediumColors()`, onboarding en tuiles + roues numériques doubles (plus de
cadran), popups d'activité communes (`MediumTintedPopup`, blanc cassé + croix
de fermeture, boutons monochromes), contrôle de statut segmenté à 3 cases
(`SegmentedProgressControl`, remplace l'ancienne jauge à pastilles), grille
2 colonnes pour "Mes activités" (En cours avant Nouvelles activités, plusieurs
activités en cours possibles en simultané), notification "Activité terminée !
+N XP", Progression sans badges/export (retirés de cette vue, code conservé
ailleurs), carte streak à contour mint, médium free trié en premier. Compile
sur les deux flavors (`assembleFreeDebug`/`assemblePremiumDebug`) et les
sources de test (`compileFreeDebugAndroidTestKotlin`) — non exécuté sur
émulateur, voir §14.

**Corrections/ajouts du 2026-08-14** : thème clair forcé quel que soit le
thème système (`LittleBigStepsTheme`, plus de `isSystemInDarkTheme()`) ;
fix du sélecteur d'heure d'onboarding qui défilait tout seul (fermeture
obsolète dans `VerticalNumberWheel`) et du bouton AM/PM invisible
(`WheelSelectionWindow` sans largeur propre) ; calendrier de renouvellement
(`domain/RenewalSchedule.kt`) permettant d'accumuler plusieurs activités en
cours à la fois selon la fréquence choisie à l'onboarding, plutôt qu'un
simple filtrage local ; popup de montée de niveau avec confettis
(`konfetti-compose`) à la finalisation d'une activité qui fait passer un
médium au niveau supérieur, remplaçant le snackbar XP pour cette
complétion ; photo obligatoire pour "Finaliser" dans "Bien joué !" (déroge à
l'optionalité totale du souvenir décrite en §3.3) ; commentaire souvenir
limité à 200 caractères ; popup de détail sur les cartes du Portfolio
(titre/médium/date/description/photo/commentaire, description ajoutée à
`PortfolioEntryEntity`) ; carte streak retirée de Progression (pression
"ne pas casser la chaîne" jugée à contre-courant du §4, le streak reste
suivi en base) ; **système de badges supprimé entièrement** (plus une
feature de l'app — `domain/model/Badge.kt`, `domain/BadgeEvaluator.kt`,
`UnlockedBadgeEntity`/`BadgeDao` retirés, `AppDatabase` passé en version 3) ;
**palette/thème sombre supprimés** (`DarkColors`, `Navy*`, `InkOnDark*`,
`Pastel*Dark`, `ErrorRedLight` — code mort depuis le thème clair forcé,
`mediumColors()` n'est plus `@Composable` puisqu'il n'a plus besoin de lire
le thème) ; **format d'export "Story" supprimé** (`ExportFormat.STORY`,
`drawStory`, `exportAsStory` — câblé mais plus jamais déclenché depuis que
le bouton a été retiré de l'écran Progression) ; bug de lisibilité corrigé
au passage sur `PremiumScreen` (texte en couleur thème sombre sur fond
clair, résidu du retrait du bloc navy).

**Localisation fr/en ajoutée le 2026-08-14** : tout le texte de l'UI
(écrans, popups, notifications, export Canvas) extrait vers
`res/values/strings.xml` (français, défaut) et `res/values-en/strings.xml`
(anglais) — langue suivie automatiquement depuis la langue système par
défaut, sélecteur manuel ajouté depuis dans la vue Paramètres (voir
ci-dessous). `MediumType.label()` a deux variantes (`@Composable` pour l'UI,
avec `Context` explicite pour `ReminderWorker`/`ExportRenderer` qui ne sont
pas en contexte Compose). `ui/common/DateFormatting.kt` bascule aussi le
format de date selon la langue (jour-mois-année en fr, mois-jour-année en
en). Catalogue de défis restructuré en `/content/{fr,en}/` (les ~40 défis
placeholder + le pack traduits en anglais), `NetworkConfig.contentBaseUrl()`
choisit le sous-dossier depuis la langue système, replie sur `fr` si non
prise en charge.

**Vue Paramètres ajoutée le 2026-08-14** (`ui/settings/`, option C validée
en maquette : liste compacte, icône mint par ligne, pas d'entête de
section) — accessible depuis une icône engrenage commune aux 3 onglets
principaux (`ui/navigation/NavGraph.kt`, nouvelle `topBar` du `Scaffold`).
Réglages : fréquence/heure de rappel (roues/stepper factorisés depuis
l'onboarding vers `ui/common/WheelPickers.kt`, réutilisés dans les deux
écrans), interrupteur rappels on/off (indépendant de fréquence/heure,
`UserPreferencesEntity.notificationsEnabled`), sélecteur de langue
(Système/Français/English via `AppCompatDelegate.setApplicationLocales()`,
nouvelle dépendance `androidx.appcompat` — **corrigé le 2026-08-15** : il
avait été écrit ici que cela fonctionnait sur `ComponentActivity` sans
hériter d'`AppCompatActivity` ni changer de thème, c'est faux et le
sélecteur ne faisait rien ; voir le correctif plus bas), gestion de
l'abonnement (lien Play Store) + restauration des achats, opt-out
analytics anonymes (`AnalyticsTracker.setEnabled`, `PostHog.optOut()`/
`optIn()`, appliqué à chaque lancement dans
`LittleBigStepsApplication.onCreate`), réinitialisation de la progression
(confirmation obligatoire, `ProgressRepository.resetProgress` +
`ChallengeRepository.clearHistory`, ne touche pas aux préférences
onboarding), version de l'app. `AppDatabase` passé en version 5
(`notificationsEnabled`/`analyticsEnabled`). Export/sauvegarde des données
explicitement exclu de cette vue (demandé par l'utilisateur).
**Non compilé après ce lot** — voir §14.

**Popup "Conseils" ajoutée le 2026-08-14** (`ChallengeEntity.tips: List<String>?`,
`ChallengeDto.tips`) : icône ampoule dans la popup "En cours", visible
seulement si des conseils sont renseignés pour ce défi précis (masquée
sinon plutôt que d'ouvrir une popup vide), ouvre `TipsDialog` (liste à
puces façon checklist). Contenu éditorial rédigé par l'utilisateur
lui-même dans `/content/{fr,en}` — jamais généré automatiquement, gratuit
pour tous les médiums. `AppDatabase` passé en version 6.

**Logo/icône de l'app finalisés le 2026-08-14** : direction retenue après
plusieurs maquettes (nom conservé — LittleBigSteps) — une "rosace"
organique de 4 tuiles arrondies (une par médium) en teintes vives
(dérivées de la palette mais saturées pour rester lisibles en icône)
disposées autour d'un centre, sur fond blanc chaud identique au reste de
l'UI (`WarmBackground`). Implémenté comme adaptive icon Android :
`drawable/ic_launcher_foreground.xml` (vector drawable, 4 groupes tournés
autour de leur propre pivot), `values/colors.xml` →
`ic_launcher_background`. Non compilé — voir §14.

**Correctifs du 2026-08-15** :

- **Catalogue vide après l'onboarding** : le code pointait sur
  `/content/{fr,en}/` (lot localisation) alors que le contenu distant était
  resté à la racine — 404 silencieux, catalogue vide, aucune activité
  proposée. Contenu restructuré poussé sur `master`. Deux causes aggravantes
  corrigées au passage : `ChallengeSelectionViewModel` relit désormais les
  propositions quand le catalogue arrive (`catalogJob`, la synchro étant
  asynchrone et `pickDailyOptions` un one-shot), et l'échec de synchro est
  loggé au lieu d'être avalé par `runCatching`.
- **Sélecteur d'heure** : roues de défilement remplacées par des steppers
  +/- (`ui/common/NumberSteppers.kt`, `WheelPickers.kt` supprimé) après deux
  bugs successifs de scroll/snap. Plus aucune mécanique de défilement.
- **Onboarding premium** : le statut premium était déduit de
  `BuildConfig.FORCE_PREMIUM` (le flavor) et non de `prefs.isPremium`. En
  premium l'étape "quel médium gratuit ?" s'affichait alors que tout est
  débloqué ; en gratuit un abonné réel (achat restauré) était bloqué en
  sélection unique ; et `submit()` reverrouillait les médiums qu'un
  abonnement restauré venait d'ouvrir. Statut premium désormais observé
  depuis les préférences, et **étape `FREE_MEDIUM_CHOICE` supprimée**
  (inatteignable dans les deux cas).
- **Sélecteur de langue inopérant** : `AppCompatDelegate.setApplicationLocales()`
  ne faisait rien et le choix n'était pas conservé. Vérifié dans le bytecode
  d'AppCompat 1.7.0 : `getLocaleManagerForApplication()` parcourt
  `sActivityDelegates` et renvoie `null` si aucun délégué AppCompat n'existe,
  auquel cas la méthode retourne sans rien faire — y compris sur API 33+ ;
  sous API 33 `applyLocalesToActiveDelegates()` parcourt le même ensemble
  vide. Trois pièces ajoutées : `MainActivity` hérite d'`AppCompatActivity`,
  `Theme.LittleBigSteps` passe sous `Theme.AppCompat.Light.NoActionBar`
  (obligatoire, sinon `AppCompatActivity` refuse de démarrer), et le service
  `AppLocalesMetadataHolderService` + `autoStoreLocales` est déclaré au
  manifeste pour la persistance sous API 33 (minSdk 26).
- **Réglages** : "Restaurer mes achats" et "Statistiques anonymes" retirés de
  la vue (la restauration reste automatique au lancement via
  `PlayBillingRepository.startConnection`). La colonne `analyticsEnabled` et
  `AnalyticsTracker.setEnabled` subsistent mais ne sont plus pilotables :
  **il n'y a plus d'opt-out analytics dans l'app**, à réévaluer au regard du §10.
- **Conseils** : 3 conseils par défi rédigés pour les 43 défis fr/en et
  poussés, versions du manifeste incrémentées. Contenu **généré**, ce qui
  déroge au §5 ("pas de génération IA") — à trancher dans ce document.

| Domaine | État | Fichiers clés |
|---|---|---|
| Modèle de données (Room) | ✅ | `data/local/entity/`, `data/local/dao/`, [docs/data-model.md](docs/data-model.md) |
| Onboarding | ✅ | `ui/onboarding/` |
| Sélection/complétion de défi | ✅ | `ui/challenge/` |
| Portfolio | ✅ | `ui/portfolio/` |
| Streak/XP/niveaux | ✅ | `data/repository/ProgressRepository.kt`, `domain/GamificationRules.kt` |
| Souvenir photo (caméra + galerie) | ✅ | `data/media/SouvenirPhotoStore.kt` |
| Notifications de rappel | ✅ | `notification/` (WorkManager, pas de FCM) |
| Export image/PDF | ✅ | `export/` — enrichi en premium (photos) |
| Google Play Billing | ✅ code, ⚠️ produit à créer en Play Console (§13) |
| Packs thématiques/saisonniers | ✅ code, ⚠️ 1 seul pack placeholder dans `/content` |
| Analytics (PostHog) | ✅ code, ⚠️ instance à créer (§13) |
| Synchro contenu JSON | ✅ | `data/repository/ContentSyncRepository.kt` — pointe sur `raw.githubusercontent.com` de ce repo (provisoire, voir §13) |
| Skip onboarding au relancement | ✅ | `ui/navigation/NavGraph.kt` |
| Catalogue de défis final (30-50/médium) | ❌ placeholder only (10/médium) | `/content/{fr,en}` |
| Localisation fr/en (UI + contenu) | ✅ | `res/values{,-en}/strings.xml`, `/content/{fr,en}` — auto via langue système + sélecteur manuel |
| Vue Paramètres | ✅ | `ui/settings/` — fréquence, heure, rappels on/off, langue, abonnement, analytics, réinitialisation |
| Popup "Conseils" par défi | ✅ code, ⚠️ contenu à rédiger (`tips` vide partout dans `/content`) | `ChallengeEntity.tips`, `ui/challenge/ChallengeSelectionScreen.kt` (`TipsDialog`) |
| Logo / icône de l'app | ✅ | `drawable/ic_launcher_foreground.xml`, `values/colors.xml` |
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
3. **CDN de contenu** — `NetworkConfig.contentBaseUrl()` dans
   [NetworkConfig.kt](app/src/main/java/com/littlebigsteps/app/data/remote/NetworkConfig.kt)
   pointe sur `raw.githubusercontent.com/ElAfroNinja000/LittleBigSteps/master/content/{fr,en}/`
   (fonctionnel, testé, mais couplé au repo de code). GitHub Pages sur ce même
   repo (Settings → Pages → branche `master`, dossier `/content`) est l'option
   la plus simple pour en sortir sans nouveau compte.

## 14. Pour reprendre ce projet

- **Build réel effectué le 2026-08-08** : JDK 17, Android SDK (platform 34,
  build-tools 34.0.0) et un émulateur (AVD `3dps_test`, API 34 x86_64) sont
  disponibles sur `D:\Dev\android-sdk`, wrapper Gradle généré et committé. Le
  code compile (`./gradlew assembleDebug`) et le core loop a été vérifié
  fonctionnel sur émulateur (voir historique de session pour le détail des
  bugs trouvés/corrigés à cette occasion).
- **Ne jamais lancer l'émulateur Android de sa propre initiative** — seulement
  si l'utilisateur le demande explicitement (build/compilation seuls ne le
  nécessitent pas). Si un émulateur est déjà démarré par l'utilisateur, on peut
  s'y connecter, mais ne pas en démarrer un nouveau sans consigne explicite.
- **Pas de tests automatisés (voir §11)** — supprimés le 2026-08-15, tout se
  vérifie désormais par test manuel de l'utilisateur sur appareil réel après
  chaque build. Ne jamais réintroduire de suite de tests de sa propre
  initiative.
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
