# TODO P5 – Bloc 4 (Onboarding) + Transverse (Workflow, GED, Notifications, BI)

**Projet :** Gestion RH · Spring Boot 4.1.1 · JPA · Spring Security · Thymeleaf · Oracle
**Lire d'abord :** `00_VUE_ENSEMBLE_equipe.md`
**Ton rôle :** tu fournis les **services transverses** utilisés par P2, P3 et P4 (notifications, workflow, GED), puis le **parcours d'onboarding** et le **reporting Direction**.

> ⚠ **Priorité absolue :** les 3 services transverses sont attendus par tous. Livre une **version simple dès la Phase 0 (J2)** (interfaces + implémentation minimale), puis enrichis-la. Ne bloque jamais les autres.

---

## 0. Livrables / dépendances

| Tu fournis | À qui | Pour quand |
|---|---|---|
| `NotificationFacade` (version simple : écrit en base + log) | P2, P3, P4 | **J2** |
| `WorkflowEngine` (version simple : tables de transitions en code) | P2, P3, P4 | **J2** |
| `DocumentFacade` (stockage local + téléchargement) | P3 (CV), P4 (contrat signé) | **J4** |
| E-mails réels + cloche de notifications | P1 (layout), tous | **J6** |
| `OnboardingFacade.initialiser(employeId)` | P4 | **J8** |
| Anti-doublon de notifications (pour F3 de P4) | P4 | J8 |
| Tableau de bord BI | Direction | J11 |

| Tu attends | De qui | Solution de repli |
|---|---|---|
| Login, rôles (dont `IT`, `MOYENS_GENERAUX`), layout, `UtilisateurCourant` | P1 | profil `dev` |
| `LogAccesDocumentService.journaliser(...)` | P1 (avant J5) | `log.info` |
| Employé + 1er contrat | P4 | script SQL de démo |
| `EvaluationCandidat`, demandes, offres… (pour le BI) | P2, P3 | données de démo |

---

## Partie A – Notifications (`NOTIFICATION`) (J1-J6)

Colonnes existantes : `DESTINATAIRE_ID`, `DESTINATAIRE_EMAIL` (**NOT NULL**), `SUJET`, `CORPS` (CLOB), `CODE_MODELE`, `ENTITE_RATTACHEMENT`, `ENTITE_ID`, `STATUT_ENVOI` (`EN_ATTENTE/ENVOYEE/ECHEC`), `NB_TENTATIVES`, `DATE_ENVOI_PREVUE`, `DATE_ENVOI_REEL`, `MESSAGE_ERREUR`.

### A1. Modèle de données — **il manque la notion « lue / non lue »**
- [ ] Ajouter une colonne `DATE_LECTURE TIMESTAMP(6)` (NULL = non lue) par un script `database/01_tables/08_notification_lecture.sql` (`ALTER TABLE NOTIFICATION ADD (DATE_LECTURE TIMESTAMP(6))`) + index `(DESTINATAIRE_ID, DATE_LECTURE)` ; **prévenir P1** (propriétaire du schéma) et mettre à jour `Notification.java` ; vérifier que `ddl-auto=validate` passe toujours
- [ ] Une notification est **à la fois** une ligne in-app (cloche) **et** un e-mail : `DESTINATAIRE_EMAIL` est rempli depuis `Utilisateur.email`

### A2. Service
- [ ] `NotificationService implements NotificationFacade` :
  - `notifier(destinataireId, codeModele, sujet, corps, entiteRattachement, entiteId)` : résout l'e-mail, crée la ligne `EN_ATTENTE`
  - `notifierRole(codeRole, ...)` : une notification par utilisateur **actif** ayant ce rôle (ex. tous les `GESTIONNAIRE_RH`)
  - `existeDeja(codeModele, entiteRattachement, entiteId, palier)` : **anti-doublon** utilisé par F3 (alerte CDD) et les relances
- [ ] **Envoi d'e-mails** : dépendance `spring-boot-starter-mail` ; config SMTP en variables d'environnement ; en dev, utiliser un faux serveur (MailHog/Mailpit) ou le simple log
- [ ] **File d'envoi** : tâche `@Scheduled(fixedDelay = 60_000)` qui traite les `EN_ATTENTE` dont `DATE_ENVOI_PREVUE ≤ now` : succès → `ENVOYEE` + `DATE_ENVOI_REEL` ; échec → `NB_TENTATIVES+1`, `MESSAGE_ERREUR` ; **après 3 échecs → `ECHEC`** (ne jamais bloquer le métier si le SMTP est en panne)
- [ ] **Gabarits d'e-mail** Thymeleaf : `templates/mail/<code_modele>.html` (corps HTML, variables nom/lien/date). Catalogue minimal à créer :

  | `CODE_MODELE` | Déclencheur | Destinataire | Bloc |
  |---|---|---|---|
  | `DEMANDE_SOUMISE`, `DEMANDE_DECISION` | soumission / validation / refus | RH, Direction, Manager | P2 |
  | `CANDIDATURE_RECUE`, `CANDIDATURE_STATUT`, `ENTRETIEN_CONVOCATION`, `CANDIDATURES_TRANSMISES` | cycle de candidature | candidat, manager | P3 |
  | `PROPOSITION_ENVOYEE` (**incl. envoi auto F2**), `PROPOSITION_REPONDUE`, `PROPOSITION_A_COMPLETER`, `EMBAUCHE_VALIDATION_MANUELLE` (F1) | embauche | candidat, RH | P4 |
  | `EMPLOYE_CREE`, `CONTRAT_CREE`, `CONTRAT_RENOUVELE` | contrats | RH, salarié | P4 |
  | `ALERTE_CDD` (**F3**, paliers 30/15/7 jours) | CDD bientôt expiré | RH + manager | P4 |
  | `ONBOARDING_TACHE_ASSIGNEE`, `ONBOARDING_RELANCE`, `ONBOARDING_COMPLET` | onboarding | assigné, RH | P5 |

- [ ] **Sécurité** : ne jamais mettre de mot de passe dans un e-mail ; liens vers l'application (pas de pièces jointes sensibles)

### A3. API / interface utilisateur
- [ ] `GET /notifications` (page) et `GET /api/v1/notifications` : liste paginée de l'utilisateur connecté (filtre non lues)
- [ ] `GET /api/v1/notifications/non-lues/compteur`
- [ ] `PATCH /api/v1/notifications/{id}/lue` et `POST /notifications/tout-lire` : **uniquement ses propres** notifications (403 sinon)
- [ ] **Cloche** : fragment `templates/fragments/cloche.html` (badge de compteur, menu déroulant des 5 dernières, lien « Tout voir ») à intégrer dans la **topbar de P1** ; mise à jour du compteur toutes les 60 s (`fetch` + `setInterval`)
- [ ] Tests : `notifierRole` ne notifie que les comptes actifs du rôle ; échec SMTP → 3 tentatives puis `ECHEC` ; anti-doublon ; un utilisateur ne peut pas marquer comme lue la notification d'un autre

---

## Partie B – Moteur de workflow (J1-J5)

Objectif : **un seul endroit** qui dit « le statut X peut-il passer au statut Y, et par qui ? ».
- [ ] `enum TypeWorkflow { DEMANDE, OFFRE, CANDIDATURE, ENTRETIEN, PROPOSITION, CONTRAT, POSTE, EMPLOYE, ONBOARDING_TACHE }`
- [ ] `WorkflowEngine.verifierTransition(type, statutActuel, statutCible, roles)` : consulte une table de transitions (`Transition(de, vers, rolesAutorises, justificationObligatoire)`) construite **à partir du tableau §4 de `00_VUE_ENSEMBLE`** ; transition inconnue → `BusinessException("Transition X → Y interdite")` ; rôle insuffisant → `ForbiddenException`
- [ ] Définir les transitions dans **une classe de configuration lisible** (un `Map` par type) ; aucun `if/else` éparpillé dans les services
- [ ] **Hooks** : possibilité d'enregistrer des actions `avant` / `après` transition (ex. `après(DEMANDE → VALIDEE)` : publier l'événement) via `WorkflowHook` ; documenter comment s'y abonner
- [ ] **Journalisation** de chaque transition acceptée **et** refusée (utilisateur, type, id, ancien → nouveau statut, date) via `AuditService` de P1
- [ ] Méthode utilitaire `transitionsPossibles(type, statutActuel, roles)` pour **n'afficher que les boutons autorisés** dans les templates
- [ ] **Documentation** d'usage (`docs/workflow.md`, 1 page avec exemple) et séance de 15 min avec P2, P3, P4 pour l'intégrer
- [ ] Tests : table complète (chaque transition autorisée/interdite), rôles, justification obligatoire

---

## Partie C – GED / Documents (`DOCUMENT`) (J3-J9)

Colonnes existantes : `NOM_FICHIER`, `TYPE_DOCUMENT`, `ENTITE_RATTACHEMENT` (`CANDIDAT, CANDIDATURE, EMPLOYE, CONTRAT, ONBOARDING_TACHE, POSTE, PROPOSITION_EMBAUCHE`), `ENTITE_ID`, `URL_STOCKAGE_GED`, `TYPE_MIME`, `TAILLE_OCTETS`, `EMPREINTE_SHA256`, `NUMERO_VERSION`, `DOCUMENT_PRECEDENT_ID`, `CONFIDENTIALITE` (`INTERNE/CONFIDENTIEL/STRICT`), `STATUT_DOCUMENT` (`ACTIF/ARCHIVE/SUPPRIME`).

- [ ] `DocumentService implements DocumentFacade` :
  - `stocker(...)` : valide, écrit le fichier, enregistre la ligne, calcule `EMPREINTE_SHA256`, journalise `TELEVERSEMENT`
  - `telecharger(id)` : contrôle d'accès → journalise `TELECHARGEMENT` → renvoie la ressource
  - `supprimer(id)` : **suppression logique** (`STATUT_DOCUMENT = SUPPRIME`) car le `DELETE` est interdit en base ; journalise `SUPPRESSION`
- [ ] **Stockage sécurisé** (décision D6) :
  - dossier `app.stockage.dossier` **hors** de `static/` et hors du dépôt Git (ajouter à `.gitignore`)
  - nom de fichier sur disque = **UUID** (jamais le nom d'origine) ; arborescence par année/mois ; `URL_STOCKAGE_GED` = chemin **relatif** (pas de chemin absolu en base)
  - le téléchargement passe **toujours** par un controller (aucun accès direct au dossier)
  - protection contre le *path traversal* (`..`, `/`), normalisation du nom d'origine pour l'affichage
- [ ] **Contrôles à l'upload** : taille max (`app.stockage.taille-max-mo`), liste blanche des types (`pdf, png, jpg, doc, docx`), vérification du **type réel** (signature du fichier, pas seulement l'extension), fichier non vide, quota par entité (ex. 10 documents max par candidature)
- [ ] **Versions** : un nouvel upload du même type pour la même entité crée `NUMERO_VERSION+1`, `DOCUMENT_PRECEDENT_ID` = ancien, et passe l'ancien en `ARCHIVE` (jamais d'écrasement)
- [ ] **Contrôle d'accès par document** (matrice à coder dans une classe `DocumentAccessPolicy`) :

  | Type | Confidentialité par défaut | Qui peut lire |
  |---|---|---|
  | `CV`, `LETTRE_MOTIVATION` | `INTERNE` | le candidat propriétaire, CHARGE_RECRUTEMENT, MANAGER (si présélectionné) |
  | `PROPOSITION` | `CONFIDENTIEL` | candidat propriétaire, CHARGE_RECRUTEMENT, GESTIONNAIRE_RH |
  | `CONTRAT_SIGNE`, `FICHE_POSTE` | `CONFIDENTIEL` | salarié propriétaire, GESTIONNAIRE_RH |
  | `RIB`, `PIECE_IDENTITE`, `CARTE_VITALE` | `STRICT` | salarié propriétaire, GESTIONNAIRE_RH (la tâche RH) |
  | `REGLEMENT_SIGNE` | `INTERNE` | salarié propriétaire, GESTIONNAIRE_RH |

  Refus → 403 **et** journalisation de la tentative
- [ ] Permissions : `DOCUMENT_CONSULTER_PERSO` (ses propres documents) / `DOCUMENT_CONSULTER_TOUS` (RH)
- [ ] Journal d'accès : appeler `LogAccesDocumentService.journaliser(...)` de P1 à chaque consultation/téléchargement/suppression (valeurs autorisées : `CONSULTATION, TELECHARGEMENT, TELEVERSEMENT, SUPPRESSION, PARTAGE`)
- [ ] **Portail salarié « Mes documents »** (`/salarie/documents`) : contrats, fiches de poste, RIB, pièces déposées, avec statut et version ; **Candidat** : « Mon CV »
- [ ] Endpoints : `POST /api/v1/documents` (multipart), `GET /api/v1/documents/{id}/telecharger`, `DELETE /api/v1/documents/{id}`
- [ ] Tests : fichier trop gros ; type interdit (`.exe` renommé `.pdf`) ; téléchargement du RIB d'un autre salarié → 403 + log ; double upload → version 2 et version 1 `ARCHIVE` ; nom malveillant `../../etc/passwd`

---

## Partie D – Bloc 4 : Onboarding (J6-J12)

Entités : `ModeleOnbTache` (`MODELE_ONB_TACHE`), `Onboarding`, `OnboardingTache`, `TacheAssignment` (`TACHE_ASSIGNATION`). Les **12 tâches par défaut** sont déjà dans `database/06_data/04_modele_onboarding.sql` (RH_PAIE, RH_DOSSIER, IT_MACHINE, IT_ACCES, MG_BADGE, MG_POSTE, MGR_J1, MGR_PARRAIN, SAL_RIB, SAL_IDENTITE, SAL_VITALE, SAL_REGLEMENT). `DELAI_JOURS` est **relatif à la date de début** (peut être négatif : « préparer la machine 2 jours avant »).

### D1. Modèles de tâches
- [ ] `ModeleOnbTacheService` : CRUD (Gestionnaire RH) ; désactivation par `EST_ACTIF` ; `EST_OBLIGATOIRE` ; `ORDRE` ; `SERVICE_RESPONSABLE` ∈ `RH, IT, MANAGER, MOYENS_GENERAUX, SALARIE`
- [ ] Écran d'administration des modèles (liste ordonnée, activer/désactiver)

### D2. Initialisation (`POST /api/v1/onboarding/employes/{id}/initialiser-onboarding`)
- [ ] `OnboardingService.initialiser(employeId)` (**idempotent** : un seul onboarding par employé ; deuxième appel = renvoie l'existant) :
  1. Créer `Onboarding` : `date_debut` = date de début du premier contrat (ou `DATE_EMBAUCHE_INITIALE`), `date_fin_prevue` = date de début + 30 jours (param) ou la plus lointaine `date_limite`, statut `NON_DEBUTE`
  2. Pour chaque modèle **actif** : créer une `OnboardingTache` (copie de l'intitulé, service, obligatoire) avec `date_limite = date_debut + delai_jours`, statut `A_FAIRE`
  3. **Assigner** (`assignee_a_id`) :
     - `SALARIE` → le compte utilisateur du nouvel employé
     - `MANAGER` → responsable courant de son département (ou supérieur de l'affectation)
     - `RH` → le Gestionnaire RH qui a validé l'embauche
     - `IT` / `MOYENS_GENERAUX` → **non assignée à une personne** (file du service : n'importe quel membre du rôle peut « Prendre en charge »)
  4. Enregistrer l'historique d'assignation (`TacheAssignment`)
  5. Notifier chaque assigné (`ONBOARDING_TACHE_ASSIGNEE`) et le Gestionnaire RH
- [ ] Appelable **automatiquement** par P4 (`OnboardingFacade`) et **manuellement** par le Gestionnaire RH (`ONBOARDING_ADMIN`)
- [ ] Refuser si l'employé n'est pas `EN_ONBOARDING`

### D3. Traitement des tâches
- [ ] `GET /api/v1/onboarding/taches/mes-taches` + page « Mes tâches » : tâches de l'utilisateur connecté **et** celles de la file de son service (IT/MG), filtrables par statut/retard
- [ ] `OnboardingTacheService.changerStatut(tacheId, nouveauStatut)` : `A_FAIRE → EN_COURS → VALIDEE` ; retour arrière interdit sauf RH ; **seul l'assigné (ou la file de son service, ou le Gestionnaire RH) peut agir**
- [ ] **Règles de validation liées aux documents** : `SAL_RIB`, `SAL_IDENTITE`, `SAL_VITALE`, `SAL_REGLEMENT` ne peuvent être validées que si le **document correspondant** (`RIB`, `PIECE_IDENTITE`, `CARTE_VITALE`, `REGLEMENT_SIGNE`) existe dans la GED pour cet employé ; le téléversement valide automatiquement la tâche (ou la passe `EN_COURS` pour relecture RH, à décider)
- [ ] `TacheAssignmentService` : `reassigner(tacheId, nouvelAssigneId, motif)` → ferme l'assignation courante (`DATE_FIN`) puis crée la nouvelle ; notification à l'ancien et au nouvel assigné
- [ ] Endpoint REST `PATCH /api/v1/onboarding/taches/{tacheId}` (spec §5.4)

### D4. Statut global et fin d'onboarding
- [ ] Recalcul après chaque changement de tâche :
  - `NON_DEBUTE` tant qu'aucune tâche n'est `EN_COURS`/`VALIDEE`
  - `EN_COURS` dès qu'une tâche est commencée
  - **`COMPLETE` quand toutes les tâches `EST_OBLIGATOIRE = 1` sont `VALIDEE`**
- [ ] À `COMPLETE` : publier `OnboardingCompletEvent` → **P4 passe l'employé en `ACTIF`** ; notifier le Gestionnaire RH et le manager ; (bonus) archiver un récapitulatif PDF
- [ ] Pourcentage d'avancement (tâches validées / total) exposé au front

### D5. Relances automatiques
- [ ] Tâche `@Scheduled(cron = "0 0 8 * * *")` : tâches non `VALIDEE` dont `date_limite < today` → notification `ONBOARDING_RELANCE` à l'assigné (ou au service) et au Gestionnaire RH
- [ ] **Anti-spam** : au plus une relance tous les `app.onboarding.relance-jours` (défaut 2) via `NotificationService.existeDeja(...)`
- [ ] Rappel **préventif** à J-1 de l'échéance (bonus) ; tâches en retard affichées en rouge

### D6. Tests
- [ ] `initialiser` deux fois → **un seul** onboarding ; nombre de tâches = nombre de modèles actifs ; `date_limite` correcte avec délai négatif
- [ ] Une tâche IT ne peut pas être validée par un salarié ; validation de `SAL_RIB` sans document → refus
- [ ] Statut global `COMPLETE` seulement quand **toutes les obligatoires** sont validées (une tâche facultative non faite ne bloque pas)
- [ ] Réassignation → ancienne ligne fermée ; relance non dupliquée dans la fenêtre

---

## Partie E – Reporting / BI pour la Direction (J10-J12)

Lecture seule, rôle `DIRECTION` (permission `REPORTING_CONSULTER`). Écrire des requêtes agrégées **compatibles Oracle** (pas de `LIMIT` : `FETCH FIRST`/`ROWNUM` selon la version, cf. décision D2).
- [ ] **Recrutements par département** (nombre de demandes validées / candidats embauchés, par période)
- [ ] **Délai moyen de recrutement** : de la `DATE_CREATION` de la demande validée jusqu'à la `DATE_EMBAUCHE_INITIALE` de l'employé (en jours) ; détailler par département
- [ ] **Masse salariale prévisionnelle** : Σ `SALAIRE_BRUT` des contrats `EN_COURS` **+** propositions `ENVOYEE/ACCEPTEE` non encore converties, sur 12 mois (mensualisation à préciser, décision D3)
- [ ] **Avancement des onboardings** : nombre d'employés par statut global, % moyen, tâches en retard
- [ ] Indicateurs complémentaires (bonus) : taux de conversion candidature → embauche, **nombre de CDD expirant sous 30 jours** (réutilise le service de P4, F3), taux d'acceptation des propositions
- [ ] `ReportingService` + `ReportingController` : `GET /api/v1/reporting/{indicateur}?du=&au=&departementId=` (JSON) et page `direction/dashboard.html`
- [ ] **Tableau de bord** : cartes de chiffres clés + graphiques (Chart.js via CDN) + filtre de période ; export CSV (bonus)
- [ ] Performances : requêtes agrégées côté base (pas de chargement d'entités en boucle) ; vérifier le plan d'exécution (`EXPLAIN PLAN`) sur le jeu de données de démo
- [ ] Tests : indicateurs calculés sur un jeu de données connu ; accès refusé à un MANAGER

---

## Partie F – Frontend Thymeleaf (J6-J13)
- [ ] **Salarié** : checklist d'onboarding (cases, barre de progression, dates limites, retard en rouge), dépôt du **RIB / pièce d'identité / carte vitale**, **signature du règlement intérieur** (case de consentement + horodatage → crée `REGLEMENT_SIGNE`), « Mes documents »
- [ ] **Gestionnaire RH** : suivi global des onboardings (liste des employés, % d'avancement, retards), détail d'un onboarding, assignation/réassignation, initialisation manuelle
- [ ] **Manager / IT / Moyens Généraux** : écran « Mes tâches » (en cours, en retard, file du service, « Prendre en charge », « Valider »)
- [ ] **Cloche de notifications** (Partie A) intégrée au layout de P1 + page « Mes notifications »
- [ ] **Direction** : tableau de bord BI (Partie E)
- [ ] Chaque écran testé avec un rôle non autorisé (403)

## Recette (J13-J14)
- [ ] **S5 :** onboarding complet jusqu'à `COMPLETE` → employé `ACTIF`
- [ ] Vérifier la réception des e-mails du catalogue (surtout `PROPOSITION_ENVOYEE` auto, `ALERTE_CDD`)
- [ ] **Test de charge simple** (ex. 100 uploads + 500 notifications) : pas d'erreur, temps de réponse acceptable, file d'envoi vidée
- [ ] Vérifier le journal d'accès documents après le scénario complet

## Écarts / points à trancher
1. Rôles `IT`, `MOYENS_GENERAUX` : **déjà créés** (`06_data/01_roles.sql`) → résolu.
2. Choix du stockage GED : décision D6 (système de fichiers recommandé ; stockage objet = bonus).
3. Notification « lue » : colonne **absente** du schéma → ajout de `DATE_LECTURE` (Partie A1), validé avec P1.
4. Les tâches `IT`/`MOYENS_GENERAUX` sont-elles assignées à une personne ou à une **file de service** ? (proposition : file de service avec « Prendre en charge »).
5. Le téléversement valide-t-il automatiquement la tâche salarié, ou le RH doit-il la valider après relecture ?
