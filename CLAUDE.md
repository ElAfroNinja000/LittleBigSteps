# LittleBigSteps — référence projet

> **Tu es un expert en production/design d'app Android.**

App Android de pratique créative quotidienne gamifiée, pour le grand public
curieux non-initié. Sans compte, sans cloud, sans IA générative.

Ce fichier est la référence produit + technique. **Le code fait foi** : en cas
de divergence, corriger ce fichier plutôt que le supposer juste.

---

## 0. Contraintes de travail — à respecter en priorité

| Règle | Détail |
|---|---|
| **Réponses** | **Toujours succinct et direct.** Aller au fait, pas de préambule ni de reformulation de la demande. |
| **Commit + push** | **Automatiques en fin de chaque fonctionnalité**, sans demander. Un commit par fonctionnalité, message expliquant le *pourquoi*. Branche `master`, remote `github.com/ElAfroNinja000/LittleBigSteps`. |
| **Compilation / APK** | **Jamais de sa propre initiative** — l'utilisateur donne le feu vert. Conséquence assumée : du code non compilé peut être poussé ; le signaler dans ces cas-là. |
| Émulateur | **Ne jamais le lancer de sa propre initiative.** Si l'utilisateur en a démarré un, s'y connecter est OK. |
| Tests | **Aucun test automatisé, ne jamais en réintroduire** sans consigne explicite. Voir §8. |
| Portée de la compilation | Attrape la syntaxe, le typage, les ressources. **Jamais** le comportement : crash au démarrage, migration destructive, race async, mise en page. Un build vert n'est pas un signal de qualité. |
| Risques runtime | Signaler explicitement tout changement non couvrable par la compilation (thème/`AppCompatActivity`, migration Room destructive, synchro asynchrone) plutôt que le déclarer validé par un build vert. |

**Build** (JDK 17 + SDK platform 34 déjà installés sur `D:\Dev\android-sdk`) :

```bash
JAVA_HOME="D:/Dev/android-sdk/jdk17" ./gradlew assembleFreeDebug assemblePremiumDebug
```

Deux flavors de **test manuel** (pas un modèle de distribution) : `free` et
`premium` (`BuildConfig.FORCE_PREMIUM=true`, `applicationId` suffixé
`.premiumtest` pour cohabiter sur le même appareil). APK dans
`app/build/outputs/apk/{free,premium}/debug/`.

---

## 0 bis. Process de production — l'agent doit le guider

Dérivé des bugs réels de ce projet : **aucun n'a été détecté par la
compilation**, et les plus graves sont tous nés à une **frontière externe**
(CDN, Play Billing, AppCompat/OS, contraintes FK Room, flavor de build).

| Étape | Ce que l'agent fait | Pourquoi (constat projet) |
|---|---|---|
| **1. Cadrer** | Rattacher la demande à une décision produit (§1-5) avant de coder. | Le ton « non culpabilisant » a tranché seul le retrait du streak de l'UI. |
| **2. Maquetter** | Proposer 2-3 directions visuelles/parcours **avant** tout code. | Itération quasi gratuite vs build + test manuel. Utilisé 3× ici (logo, Paramètres, Conseils). |
| **3. Frontières** | Pour chaque système externe touché, répondre par écrit : que se passe-t-il en cas d'échec ? comment distingue-t-on un échec d'un résultat vide ? | `restorePurchases()` confondait les deux → abonné payant rétrogradé. `syncIfNeeded()` avalait ses erreurs → bug indiagnosticable. |
| **4. Vérifier les tiers** | Toute affirmation « la lib fait X » est vérifiée (source/bytecode/doc) ou **explicitement marquée comme hypothèse**. | Une hypothèse fausse sur AppCompat a produit une feature morte + 2 fichiers de doc faux + 2 cycles de correction. Vérification réelle : 3 commandes. |
| **5. Implémenter + committer** | Une fonctionnalité = un commit, poussé aussitôt. | 15 features accumulées → 77 fichiers, 3-4 sujets par fichier, découpage a posteriori impossible. |
| **6. Compiler** | **Sur feu vert uniquement.** Puis énoncer ce qui reste non vérifié. | Plusieurs APK « BUILD SUCCESSFUL » coexistaient avec un crash potentiel au démarrage. |
| **7. Livrer le test** | Fournir une **checklist ordonnée par risque** + les **préconditions** (installation propre, mode avion…), jamais « teste l'app ». | Sans précondition, un test ne prouve rien : le correctif anti-`CASCADE` exigeait des données créées après installation. |
| **8. Corriger** | Chercher la cause, pas le symptôme. Puis auditer ce que ce chemin touche **et ce qu'il rend fréquent**. | 3× le symptôme n'était pas le problème. Le bug de perte de données n'a émergé que parce qu'un correctif rendait la re-synchro fréquente. |
| **9. Consigner** | Mettre à jour §8/§9 : décisions et pièges, **pas** de journal daté. Purger les marqueurs périmés. | 178 lignes de journal ont fini supprimées comme bruit, en noyant des décisions structurantes. Un fait faux en doc se réplique. |

---

## 1. Produit

**Problème** — Beaucoup ont envie d'une pratique créative mais ne savent pas
par où commencer, et abandonnent faute de structure et de progression visible.

**Cible** — Grand public curieux, **sans pratique établie**. Pas les artistes
confirmés cherchant à maintenir une discipline.

**Promesse** — App simple, sans compte, qui propose de petits défis créatifs
(médium au choix), gamifie la régularité et construit un portfolio visible.

**Différenciation** — Sketch a Day / 4TheWords : mono-médium, public déjà
engagé. Habitica / Streaks : génériques, zéro contenu créatif. 100 Day
Project : demande réelle, aucun outil dédié. Créneau visé : découverte +
gamification + progression, pensé débutant.

**Personas** (point commun : aucune pratique établie)
- **A — l'envieux sans élan** : 30-45 ans, chargé, n'a jamais commencé,
  bloqué par la page blanche. Exige une friction quasi nulle (pas de matériel,
  pas de cours). Motivé par les petites victoires visibles.
- **B — le zappeur créatif** : a déjà abandonné plusieurs hobbies, aime
  varier, a besoin d'un cadre externe pour compenser une motivation courte.

**Ton produit — non culpabilisant.** Principe structurant : streak sans
pénalité (simple remise à zéro, aucun message de reproche), aucune
vérification des défis, aucune pression à « ne pas casser la chaîne ».

---

## 2. Parcours & MVP

1. **Onboarding sans compte** — médium(s), fréquence, heure de rappel. < 1 min,
   tout en local.
2. **Découverte** — toujours 2-3 défis proposés, filtrés par médium.
3. **Complétion** — statut auto-déclaré (Brouillon → En cours → Terminé), puis
   souvenir (photo + commentaire ≤ 200 car.). **Aucune vérification.**
4. **Progression** — XP et niveau par médium, streak global suivi en base.
5. **Relance** — notification locale périodique, sans culpabilisation.

MVP couvert : sélection, complétion, gamification, portfolio (chronologique,
filtrable), notifications, export image/PDF.

---

## 3. Contenu

**4 médiums** : photo, dessin, écriture courte, petit artisanat. Tous choisis
pour une barrière matérielle nulle et un souvenir facile à capturer.
**Exclus** : musique et danse (accès trop coûteux, souvenir difficile).
Cuisine envisagée en v2 (dilue le positionnement art/artisanat).

**Génération IA autorisée, relecture humaine obligatoire.** L'exception des
`tips` est étendue à l'ensemble du catalogue : l'agent rédige, l'utilisateur
relit et valide avant commit. Rien n'entre dans `/content` sans cette relecture.

**Cible de volume** : 30-50 défis/médium. **Actuel : ~10/médium (placeholder).**

**Distribution** : JSON statique dans `/content/{fr,en}/`, servi par CDN **et**
embarqué dans l'APK (voir §7). Pas de soumission communautaire.

Champs d'un défi : `id`, `title`, `description`, `estimatedMinutes`, `level`,
`isPremiumOnly`, `tags?`, `tips?`, `packId?`.

> **Piège** : après toute modification de `/content`, **incrémenter la version
> du médium dans `manifest.json`**, sinon la synchro conclut « déjà à jour » et
> ne retélécharge rien. `packs.json` fait exception : il porte son **propre**
> champ `version`, lu directement (le manifeste ne le référence pas). Le champ
> `version` racine de `manifest.json` n'est lu par personne — seul
> `mediums[].version` compte. Un hook alerte désormais en cas d'oubli (§8).

---

## 4. Économie & communauté

**Freemium**, frontière alignée sur l'onboarding :
- **Gratuit** : un seul médium, complet (défis, gamification, portfolio, export).
- **Premium** (abonnement) : tous les médiums + packs thématiques/saisonniers +
  export enrichi (photos).

Abonnement plutôt qu'achat unique : le contenu se renouvelle en continu.

**Communauté** : export/partage autonome uniquement. Pas de feed, ni likes, ni
commentaires — cohérent avec l'absence de compte.

---

## 5. Métriques & hors scope

**Métriques** : activation (1er défi < 48 h) · rétention J1/J7/J30 avec focus
sur la survie du streak à J7 · défis/semaine par actif · conversion premium
(surtout multi-médium) · croissance du portfolio (proxy d'attachement).

**Hors scope MVP** : compte, synchro cloud, génération IA, feed, vérification
des défis, musique/danse, suggestions personnalisées.

---

## 6. Risques ouverts

1. **Rétention sans levier social** — la gamification seule suffit-elle ?
2. **Production de contenu sans IA** — charge de rédaction soutenable ?
3. **Perte de progression** — sans compte ni cloud, un changement d'appareil
   efface tout. **Non résolu** ; à mitiger par un export réimportable.
4. **Conversion freemium** — un médium gratuit suffit-il à déclencher l'achat ?
5. **Analytics** — plus aucun opt-out dans l'UI (voir §8), à réévaluer au
   regard du positionnement vie privée.

---

## 7. Architecture

**Android uniquement**, stack native.

| Couche | Choix |
|---|---|
| Client | Kotlin + Jetpack Compose, MVVM |
| Local | Room (structuré) + stockage interne pour les photos, référencées par chemin |
| Contenu | JSON statique : embarqué dans l'APK **et** servi par CDN |
| Notifications | WorkManager local, **pas de FCM** |
| Paiement | Google Play Billing |
| Export | Canvas/Bitmap natif → PNG/PDF, partagé via `FileProvider` |
| Analytics | PostHog, ID anonyme, **aucune PII** |

**Pas de framework DI** — service locator manuel dans
`LittleBigStepsApplication.kt`, chaque écran a un `XxxViewModelFactory`
construit à la main dans `ui/MainActivity.kt`.

**Synchro de contenu** (`ContentSyncRepository`), dans cet ordre :
1. **catalogue embarqué** (`BundledContentSource`, assets) — appliqué d'abord,
   sans réseau : premier lancement hors-ligne et changement de langue immédiats ;
2. **CDN** — n'apporte ensuite que les mises à jour publiées depuis.

Deux invariants à ne pas casser :
- La synchro est **non destructive** : `upsertAll` puis élagage
  (`deleteByMediumNotIn`). Un `delete` global déclencherait les `CASCADE`
  (activités en cours) et `SET NULL` (portfolio) — perte de données utilisateur.
- Le suivi porte sur **version + langue** (`syncedLocale`) : les catalogues fr
  et en partagent les mêmes numéros de version.

`/content` est déclaré comme dossier d'assets dans `app/build.gradle.kts` — une
seule source de vérité, pas de copie à synchroniser.

---

## 8. Décisions actées — ne pas défaire sans consigne

| Décision | Conséquence pratique |
|---|---|
| **Pas de tests automatisés** | Suite E2E supprimée (jamais exécutée, sélecteurs déjà dérivés). Repartir du dernier commit contenant `app/src/androidTest` si le besoin resurgit. |
| **Thème clair forcé** | Aucune palette sombre. `mediumColors()` n'est pas `@Composable`. |
| **Badges supprimés** | Plus une feature de l'app. |
| **Streak invisible** | Suivi en base, retiré de l'écran Progression (pression contraire au ton produit). |
| **Export "Story" supprimé** | Seuls PNG/PDF subsistent. |
| **Photo obligatoire pour finaliser** | Déroge au « souvenir optionnel » du §2. |
| **`MainActivity: AppCompatActivity` + `Theme.AppCompat`** | Requis par le sélecteur de langue. Un thème framework fait **planter au démarrage**. Le service `AppLocalesMetadataHolderService` au manifeste assure la persistance sous API 33. |
| **Étape « médium gratuit ? » supprimée** | Inatteignable : gratuit = sélection déjà unique, premium = tout débloqué. |
| **Steppers +/-** | Plus aucune roue à défilement (deux bugs de scroll successifs). |
| **Opt-out analytics retiré de l'UI** | Colonne `analyticsEnabled` conservée en base mais non pilotable. Voir risque §6.5. |
| **Restauration d'achats automatique** | Au lancement via `startConnection()`, plus de bouton dédié. |
| **Catalogue rédigé par l'IA, relu avant validation** | Étend l'exception `tips` (§3). L'agent produit fr + en ; l'utilisateur relit avant commit. |
| **Garde-fous outillés** (`.claude/hooks/`, câblés par `.claude/settings.json`) | `content-version-guard.ps1` : après écriture dans `/content`, alerte si la version n'a pas été incrémentée ou si fr et en divergent. `build-gate.ps1` : toute commande `gradlew` / émulateur passe par une demande de confirmation. Chemins **absolus** dans `settings.json` — à corriger si le dépôt est déplacé. |

---

## 9. État d'implémentation

Core loop complet, MVP et modèle économique codés. Compile sur les deux
flavors ; démarrage, persistance du premium et changement de langue vérifiés
sur appareil réel.

| Domaine | État | Fichiers clés |
|---|---|---|
| Modèle de données (Room, **v7**, `fallbackToDestructiveMigration`) | ✅ | `data/local/`, [docs/data-model.md](docs/data-model.md) |
| Onboarding | ✅ | `ui/onboarding/` |
| Sélection / complétion | ✅ | `ui/challenge/` |
| Portfolio | ✅ | `ui/portfolio/` |
| Streak / XP / niveaux | ✅ | `data/repository/ProgressRepository.kt`, `domain/GamificationRules.kt` |
| Renouvellement des propositions | ✅ | `domain/RenewalSchedule.kt` |
| Défi surprise (bonus XP) | ✅ | `ui/challenge/ChallengeSelectionViewModel.kt` |
| Souvenir photo | ✅ | `data/media/SouvenirPhotoStore.kt` |
| Notifications | ✅ | `notification/` |
| Export image/PDF | ✅ | `export/` — enrichi en premium |
| Paramètres | ✅ | `ui/settings/` |
| Localisation fr/en (UI + contenu) | ✅ | `res/values{,-en}/`, `/content/{fr,en}` |
| Popup « Conseils » | ✅ | `ChallengeEntity.tips`, `TipsDialog` |
| Logo / icône | ✅ | `drawable/ic_launcher_foreground.xml` |
| Play Billing | ✅ code | ⚠️ produit à créer en Play Console (§10) |
| Packs thématiques | ✅ code | ⚠️ 1 seul pack placeholder |
| Analytics PostHog | ✅ code | ⚠️ instance à créer (§10) |
| **Catalogue final (30-50/médium)** | ❌ ~10/médium | `/content/{fr,en}` |
| CI | ❌ | — |

---

## 10. Config externe en attente

Non codable depuis un agent seul (comptes/consoles externes) :

1. **Play Console** — `PREMIUM_SUBSCRIPTION_ID` (`"premium_subscription"`) dans
   `billing/PlayBillingRepository.kt` doit correspondre à un abonnement réel.
   Tant qu'il n'existe pas, le flavor `premium` sert de simulation.
2. **PostHog** — `analytics/AnalyticsConfig.kt` contient des placeholders
   `TODO-...`. Sans clé valide le SDK n'envoie rien, **silencieusement**.
3. **CDN** — `NetworkConfig` pointe sur `raw.githubusercontent.com` de ce repo
   (fonctionnel, mais couplé au dépôt de code). GitHub Pages sur `master`,
   dossier `/content`, est la sortie la plus simple.

---

## 11. Naviguer dans le code

**Chemin de lecture** : `LittleBigStepsApplication.kt` (service locator, tout
est construit là) → `ui/MainActivity.kt` (câblage des ViewModels) →
`ui/navigation/NavGraph.kt` (parcours complet).

**Avant d'ajouter une fonctionnalité** : vérifier §9, puis chercher si un
repository/DAO existant peut être **étendu** plutôt que dupliqué.

**Convention DAO systématique** : one-shot `getOnce()` / `getAllX()` + Flow
`observeX()`. Voir n'importe quel fichier de `data/local/dao/`.

**Conventions UI** : popups via `ui/common/MediumTintedPopup.kt` ; contrôles
numériques via `ui/common/NumberSteppers.kt` ; couleurs par médium via
`mediumColors()`.

[README.md](README.md) contient un miroir plus court, organisé par
fonctionnalité.
