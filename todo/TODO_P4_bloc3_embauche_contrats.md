# TODO P4 – Bloc 3 (Embauche) + Contrats & Affectations + fonctionnalités F1, F2, F3

**Projet :** Gestion RH · Spring Boot 4.1.1 · JPA · Spring Security · Thymeleaf · Oracle
**Lire d'abord :** `00_VUE_ENSEMBLE_equipe.md`
**Ton rôle :** de la candidature `RETENUE` jusqu'à l'**employé avec son contrat**. Tu portes aussi les 3 fonctionnalités automatiques de `test.md` :
- **F1** – validation **automatique** de l'embauche selon des conditions
- **F2** – envoi **automatique** d'une proposition d'embauche quand un candidat est retenu
- **F3** – **alerte** « CDD bientôt expiré » sur la page du RH et du manager

---

## 0. Livrables / dépendances

| Tu fournis | À qui | Pour quand |
|---|---|---|
| Employé créé avec matricule unique | P5 (onboarding) | **J8** |
| `ContratAlerteService.cddExpirantBientot(...)` | P2 (widget manager), P5 (notif) | **J8** |
| `PropositionEmbaucheService` (consultation par candidat) | P3 (« Mes candidatures ») | J9 |
| Historique des contrats et affectations | Direction, RH | J10 |

| Tu attends | De qui | Solution de repli |
|---|---|---|
| Candidature `RETENUE` + `CandidatureRetenueEvent` | P3 | créer une candidature `RETENUE` en SQL de démo |
| `OnboardingFacade.initialiser(employeId)` | P5 | interface vide qui log |
| `NotificationFacade`, `DocumentFacade` (contrat signé) | P5 | `log.info` puis brancher |
| `PosteService.marquerPourvu(posteId)` | P2 | appeler directement `PosteRepository` en attendant |
| Création de compte salarié (`UtilisateurService`) | P1 | script SQL de démo |

---

## Partie A – Proposition d'embauche (`PROPOSITION_EMBAUCHE`) (J4-J8)

Statuts : `BROUILLON, ENVOYEE, ACCEPTEE, REFUSEE, EXPIREE, ANNULEE`.
Colonnes : `TYPE_CONTRAT_PROPOSE` (CDD/CDI/STAGE/ALTERNANCE), `SALAIRE_BRUT_PROPOSE` (≥ 0), `DATE_DEBUT_SOUHAITEE`, `DATE_ENVOI`, `DATE_LIMITE_REPONSE`, `DATE_REPONSE`, `MOTIF_REFUS`, `REDACTEUR_ID` (peut être nul = « système »), `EMPLOYE_ID`.

### A1. Rédaction manuelle (Chargé de recrutement)
- [ ] `PropositionEmbaucheService.creer(candidatureId, form)` :
  - La candidature doit être **`RETENUE`** (sinon `BusinessException`)
  - **Une seule proposition active** (`BROUILLON`/`ENVOYEE`/`ACCEPTEE`) par candidature
  - Validation du formulaire : type de contrat, salaire ≥ 0, `dateDebutSouhaitee` ≥ aujourd'hui, `dateLimiteReponse` > aujourd'hui et ≤ date de début
  - Rédacteur = utilisateur courant, statut `BROUILLON`
- [ ] `envoyer(id)` : `BROUILLON → ENVOYEE`, `DATE_ENVOI = today` ; notifier le candidat (e-mail + cloche) ; joindre/enregistrer un PDF de la proposition dans la GED (`TypeDocument.PROPOSITION`, entité `PROPOSITION_EMBAUCHE`) — bonus
- [ ] `annuler(id, motif)` : `BROUILLON|ENVOYEE → ANNULEE`
- [ ] Endpoint REST `POST /api/v1/embauche/propositions` (spec §5.3)

### A2. Réponse du candidat
- [ ] `repondre(id, accepter, motifRefus)` : seulement par le **candidat propriétaire** ; seulement si `ENVOYEE` et `today ≤ DATE_LIMITE_REPONSE`
  - Acceptation → `ACCEPTEE`, `DATE_REPONSE = today`, publie `PropositionAccepteeEvent` (déclenche le contrôle **F1**)
  - Refus → `REFUSEE` ; **motif conseillé**, notifier le Chargé de recrutement ; l'offre peut être rouverte (à signaler à P3)
- [ ] **Expiration automatique** : tâche planifiée quotidienne (`@Scheduled(cron = "0 30 1 * * *")`) : `ENVOYEE` dont `DATE_LIMITE_REPONSE < today` → `EXPIREE` + notification
- [ ] Endpoint REST `PATCH /api/v1/embauche/propositions/{id}/reponse` (spec §5.3)
- [ ] **Relance** (bonus) : notification au candidat 2 jours avant l'échéance

---

## Partie F2 – Envoi automatique de la proposition après « candidat retenu » (J8-J9)

**Principe :** quand P3 passe une candidature à `RETENUE`, il publie `CandidatureRetenueEvent`. Ton listener crée et, si possible, envoie la proposition **sans action humaine**.

- [ ] `PropositionAutoListener` :
  ```java
  @Component
  class PropositionAutoListener {
      @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
      @Transactional(propagation = Propagation.REQUIRES_NEW)   // nouvelle transaction : indispensable après AFTER_COMMIT
      public void surCandidatureRetenue(CandidatureRetenueEvent e) {
          propositionService.creerEtEnvoyerAutomatiquement(e.candidatureId());
      }
  }
  ```
- [ ] `creerEtEnvoyerAutomatiquement(candidatureId)` :
  1. **Idempotence** : si une proposition active existe déjà pour cette candidature → ne rien faire (l'événement peut être rejoué)
  2. Construire les valeurs par défaut :
     - `typeContratPropose` : paramètre `app.embauche.type-contrat-defaut` (ex. `CDI`) — ou celui indiqué sur l'offre/la demande s'il existe
     - `dateDebutSouhaitee` : `DATE_SOUHAITEE` de la **demande de recrutement** (si déjà passée → aujourd'hui + 15 jours)
     - `salaireBrutPropose` : **à trancher** (voir Écarts) — par défaut le salaire de référence du poste ou une valeur saisie par le Chargé RH sur l'offre
     - `dateLimiteReponse` : aujourd'hui + `app.embauche.delai-reponse-jours` (défaut 7)
     - `redacteurId` : `NULL` (« système »)
  3. **Si toutes les données obligatoires sont disponibles** → créer et passer **directement à `ENVOYEE`** ; notifier le candidat (`PROPOSITION_ENVOYEE`) et le Chargé RH (« envoyée automatiquement »)
  4. **Sinon** → créer en `BROUILLON`, notifier le Chargé de recrutement « proposition à compléter » (aucun envoi incomplet)
- [ ] **Interrupteur** : `app.embauche.envoi-auto=true|false` (pouvoir couper la fonctionnalité sans redéployer)
- [ ] **Traçabilité** : chaque envoi automatique est journalisé (audit) avec la mention « automatique »
- [ ] **Annulation** : tant que le candidat n'a pas répondu, le Chargé RH peut annuler (`ANNULEE`) une proposition auto
- [ ] Tests : événement rejoué deux fois → une seule proposition ; données manquantes → `BROUILLON` + notification ; interrupteur coupé → rien ; échec d'e-mail → la proposition reste créée (la notification est retentée par P5)

---

## Partie B – Conversion Candidat → Employé (J8-J10)

**Règle d'or :** `CANDIDAT ≠ EMPLOYÉ`. Le candidat garde son historique de candidatures ; l'employé est une **autre entité** liée par `EMPLOYE.CANDIDAT_ID` (**unique** : `UK_EMP_CANDIDAT`, un candidat → au plus un employé).

- [ ] `EmployeService.convertirCandidat(candidatId / propositionId)` — **une seule transaction** :
  1. Pré-conditions : proposition `ACCEPTEE` ; candidat pas déjà employé ; poste concerné `VACANT` ; (si F1 désactivée) validation manuelle du Gestionnaire RH (`EMPLOYE_CONVERTIR`)
  2. Créer `Employe` : nom/prénom depuis `Candidat`, `candidat`, `dateEmbaucheInitiale` = date de début de la proposition, `statutActivite = EN_ONBOARDING`, `emailProfessionnel` généré (`prenom.nom@entreprise.local`, **unique** : ajouter un suffixe numérique en cas de doublon)
  3. **Matricule** : **ne pas le calculer en Java.** Le trigger `TBR_EMPLOYE_MATRICULE` appelle `FN_NEXT_MATRICULE` (compteur annuel verrouillé en base → sans doublon sous concurrence) et produit `EMP-AAAA-NNNN`. Voir « Piège matricule » ci-dessous
  4. Relier la proposition (`EMPLOYE_ID`)
  5. `PosteService.marquerPourvu(posteId)` (P2) : `VACANT → POURVU`
  6. Créer le **compte utilisateur interne** du salarié (rôle `SALARIE`, mot de passe temporaire, `DATE_EXPIRATION_MDP`) via le service de P1 ; le compte `CANDIDAT` est conservé
  7. Publier `EmployeCreeEvent`, notifier le Gestionnaire RH et le futur salarié
- [ ] `CompteurMatriculeService` : **lecture seule/diagnostic** (dernier numéro de l'année). La génération est dans Oracle. Si l'équipe préfère la logique en Java, utiliser `@Transactional` + verrou `PESSIMISTIC_WRITE` sur la ligne `COMPTEUR_MATRICULE` — mais **ne pas faire les deux** (doublon de numérotation)
- [ ] Endpoint REST `POST /api/v1/embauche/candidats/{id}/convertir-employe` (spec §5.3)
- [ ] `EmployeService` : consultation, recherche paginée (nom, matricule, statut, département), détail, changement de `statut_activite` (`ACTIF ↔ SUSPENDU`, `→ SORTI` avec `DATE_SORTIE` ≥ date d'embauche et `MOTIF_SORTIE`) ; **pas de suppression**

### ⚠ Piège matricule (à traiter dès le début)
`Employe.matricule` est mappé `@Column(nullable = false)` et écrit à l'`INSERT`/`UPDATE`. Or il doit rester **NULL** à l'insertion (le trigger le génère) et ne **jamais** être réécrit ensuite (`ORA-20010` si modifié).
- [ ] Faire changer le mapping (avec accord P1) : `@Column(name = "MATRICULE", insertable = false, updatable = false)`
- [ ] Après `save()` : `entityManager.flush(); entityManager.refresh(employe);` pour lire le matricule généré avant de l'afficher ou de le notifier
- [ ] Test d'intégration : créer 50 employés en parallèle (`ExecutorService`) → 50 matricules **distincts**, format `^EMP-\d{4}-\d{4,}$`

---

## Partie F1 – Validation automatique de l'embauche (J9-J10)

**Principe :** à l'acceptation de la proposition (`PropositionAccepteeEvent`), le système évalue des **conditions**. Si **toutes** sont remplies → la conversion en employé est faite **automatiquement**. Sinon → le dossier va dans la **file de validation manuelle** du Gestionnaire RH (`hiring/rh-confirmation.html`) avec la liste des conditions non remplies.

- [ ] `RegleEmbaucheService.controler(propositionId)` retourne `ResultatControle { boolean valide; List<String> motifsEchec; }`
- [ ] Conditions proposées (à valider avec l'équipe ; chacune = une petite méthode testable) :

  | # | Condition | Source |
  |---|---|---|
  | C1 | Proposition `ACCEPTEE` et réponse dans le délai (`DATE_REPONSE ≤ DATE_LIMITE_REPONSE`) | `PropositionEmbauche` |
  | C2 | Candidature `RETENUE` et dernière évaluation `FAVORABLE` (aucune `DEFAVORABLE`) | P3 (`derniereDecision`) |
  | C3 | Le poste est `VACANT` et le **nombre d'embauches déjà faites < `EFFECTIF_DEMANDE`** de la demande | P2 |
  | C4 | Rémunération proposée ≤ **budget alloué** de la demande (cf. décision D3 : budget total annuel/par poste ?) | `DemandeRecrutement` |
  | C5 | Le candidat n'est **pas déjà employé** (`UK_EMP_CANDIDAT`) et l'e-mail professionnel peut être généré | `Employe` |
  | C6 | Date de début ≥ aujourd'hui (pas de rétro-datation) | proposition |
  | C7 | Dossier candidat complet : CV présent dans la GED, RGPD consenti | P5/P1 |

- [ ] `ValidationAutoListener` : `@TransactionalEventListener(AFTER_COMMIT)` sur `PropositionAccepteeEvent` → si `app.embauche.validation-auto=true` et `controler(...).valide` → `EmployeService.convertirCandidat(...)` avec auteur « système »
- [ ] **Sinon** : créer une notification au Gestionnaire RH (« Validation manuelle requise : motifs … ») et afficher le dossier dans la file `rh-confirmation.html` avec les motifs en clair
- [ ] **Trace d'audit** : la décision (automatique/manuelle), les conditions évaluées et leur résultat sont enregistrées (`AuditService` de P1) pour pouvoir expliquer une embauche
- [ ] Le Gestionnaire RH peut **forcer** la validation manuelle d'un dossier non conforme (avec justification) ou la refuser
- [ ] Tests : tous les C1–C7 OK → employé créé ; chaque condition KO individuellement → pas de création + bon message ; interrupteur coupé → manuel ; double événement → **un seul** employé

---

## Partie D – Contrats (`CONTRAT`) (J5-J10)

Colonnes clés : `TYPE_CONTRAT`, `TYPE_ACTE` (`INITIAL/AVENANT/RENOUVELLEMENT`), `CONTRAT_PRECEDENT_ID`, `DATE_DEBUT`, `DATE_FIN`, `DATE_FIN_EFFECTIVE`, `SALAIRE_BRUT`, `DEVISE`, `STATUT_CONTRAT` (`EN_ATTENTE, EN_COURS, RENOUVELE, TERMINE`).

**Règle d'or : un contrat = un enregistrement. Aucun écrasement.** La base le garantit aussi : le trigger `TBU_CONTRAT_FIGE` interdit de modifier employé, poste, type, date début, salaire, date fin d'un contrat qui n'est plus `EN_ATTENTE` (`ORA-20011`).

### D1. Création et activation
- [ ] `ContratService.creer(form)` (rôle `CONTRAT_GERER`, Gestionnaire RH) : crée un contrat `EN_ATTENTE`
  - **Type `INITIAL`** : l'employé n'a encore aucun contrat actif
  - **Validations alignées sur les `CHECK` Oracle** :
    - `CDI` → `DATE_FIN` **absente** ; **tous les autres types (CDD, STAGE, ALTERNANCE) → `DATE_FIN` obligatoire** (le document ne parlait que du CDD, la base est plus stricte)
    - `DATE_FIN ≥ DATE_DEBUT`, `SALAIRE_BRUT ≥ 0`, poste non `FERME`
    - `AVENANT`/`RENOUVELLEMENT` → `CONTRAT_PRECEDENT_ID` **obligatoire**
- [ ] `activer(id)` : `EN_ATTENTE → EN_COURS`
  - **Un seul contrat `EN_COURS` par employé** (index unique `UX_CTR_ENCOURS`) : contrôler avant d'activer et donner un message métier clair
  - **Pas de chevauchement** de périodes avec un contrat actif ou à venir
  - Option : attacher le **contrat signé** à la GED (`CONTRAT_SIGNE`)
- [ ] `terminer(id, dateFinEffective, motif)` : `EN_COURS → TERMINE`, renseigne `DATE_FIN_EFFECTIVE` (le trigger autorise ce changement de statut)

### D2. Renouvellement / avenant (dans **une seule transaction**, **dans cet ordre**)
1. Charger l'ancien contrat `EN_COURS` (verrouiller la ligne : `@Lock(PESSIMISTIC_WRITE)`)
2. Passer l'ancien en `RENOUVELE` + `DATE_FIN_EFFECTIVE` (**flush** pour libérer l'index unique `EN_COURS`)
3. Insérer le nouveau (`TYPE_ACTE = RENOUVELLEMENT` ou `AVENANT`, `CONTRAT_PRECEDENT_ID = ancien`), `DATE_DEBUT` = lendemain de l'ancienne fin, puis `EN_COURS`
4. Si l'étape 3 échoue, **tout est annulé** (l'ancien contrat reste `EN_COURS`)
- [ ] Règles : un `CDD` renouvelé ne dépasse pas les limites légales choisies par l'équipe (ex. 2 renouvellements — paramétrable `app.contrat.renouvellements-max`) ; message si dépassé
- [ ] `GET /api/v1/onboarding/employes/{id}/contrats` (spec §5.4) et page « Historique des contrats » : **toute la chaîne**, ordonnée, avec le lien précédent→suivant
- [ ] Endpoint REST `POST /api/v1/onboarding/contrats` (spec §5.4)

### D3. Déclenchement de l'onboarding
- [ ] Après l'**activation du premier contrat** (`TYPE_ACTE = INITIAL`) : appeler `OnboardingFacade.initialiser(employeId)` **après commit** (idempotent : ne rien faire si un onboarding existe)
- [ ] Notifications : `CONTRAT_CREE`, `CONTRAT_ACTIVE`, `CONTRAT_RENOUVELE`

---

## Partie E – Affectations (`AFFECTATION`) (J6-J10)

Rôle (décision D8) : historique **employé ↔ poste**, avec le supérieur hiérarchique (`SUPERIEUR_ID`) et le contrat éventuel.
- [ ] `AffectationService.affecter(employeId, posteId, contratId, superieurId, dateDebut, motif)` : ferme l'affectation courante (`DATE_FIN = dateDebut - 1`) puis crée la nouvelle ; **une seule affectation courante** par employé
- [ ] Affectation initiale **automatique** à la création du premier contrat (poste du contrat, supérieur = responsable du département)
- [ ] Un mouvement de poste met à jour le statut des postes concernés (ancien → `VACANT`, nouveau → `POURVU`) via P2
- [ ] Historique consultable sur la fiche employé ; endpoint/controller `AffectationController`

---

## Partie F3 – Alerte « CDD bientôt expiré » (J8-J11)

**Objectif :** prévenir le **RH** et le **manager** quand le contrat d'un salarié en CDD approche de sa fin, **avant** qu'il soit trop tard pour renouveler ou clôturer.

### F3-a. Logique (P4)
- [ ] Repository : requête sur les contrats **actifs à durée déterminée** dont la fin approche
  ```java
  @Query("""
     select c from Contrat c
     where c.statutContrat = 'EN_COURS'
       and c.typeContrat = 'CDD'
       and c.dateFin between :aujourdhui and :limite
     order by c.dateFin
  """)
  List<Contrat> cddExpirantEntre(@Param("aujourdhui") LocalDate aujourdhui,
                                 @Param("limite") LocalDate limite);
  ```
  (étendre à `STAGE`/`ALTERNANCE` si l'équipe le souhaite : ils ont aussi une `DATE_FIN` obligatoire)
- [ ] `ContratAlerteService` :
  - `cddExpirantBientot()` : seuil `app.contrat.alerte-cdd-jours` (défaut **30**) → liste de `AlerteContratDto { employe, matricule, poste, departement, dateFin, joursRestants, niveau }` avec **niveau** : `INFO` (>15 j), `ATTENTION` (8–15 j), `URGENT` (≤ 7 j)
  - `cddExpirantBientotPourManager(utilisateur)` : **uniquement** les salariés du **département dont il est responsable** (via `Affectation` courante / `DepartementResponsable` courant)
  - Exclure les contrats pour lesquels un **renouvellement est déjà en cours** (`EN_ATTENTE` lié)
- [ ] **Tâche planifiée** quotidienne `@Scheduled(cron = "0 0 7 * * *")` : pour chaque alerte, créer une **notification** (cloche + e-mail via P5) au Gestionnaire RH **et** au manager, **sans doublon** (clé : `ENTITE_RATTACHEMENT='CONTRAT'`, `ENTITE_ID`, `CODE_MODELE='ALERTE_CDD'` + palier 30/15/7 j)
- [ ] Penser à `@EnableScheduling` (P1) et à un verrou si l'application tourne sur plusieurs instances (bonus : ShedLock)

### F3-b. Affichage
- [ ] **Page RH** (Gestionnaire RH) : encart « Contrats à échéance » en haut du tableau de bord/fiche employés : tableau trié par `joursRestants`, badge de couleur par niveau, boutons **« Renouveler »** (ouvre le formulaire de D2) et **« Laisser expirer »**
- [ ] **Page Manager** : même encart dans `manager-dashboard.html` — **P2 intègre le fragment** `fragments/alerte-cdd.html` que **tu fournis** (`th:replace`) ; le manager voit seulement son département et peut « Demander un renouvellement » (notification au Gestionnaire RH)
- [ ] Bandeau/compteur dans le menu (« 3 CDD expirent bientôt »)
- [ ] Un CDD **déjà expiré** non traité (`DATE_FIN < today` et toujours `EN_COURS`) est signalé en **rouge** et proposé à la clôture (`TERMINE`)

### F3-c. Tests
- [ ] CDD qui expire dans 10 jours → alerte `ATTENTION` ; dans 40 jours → aucune ; un CDI n'en génère jamais
- [ ] Manager A ne voit pas les CDD du département B
- [ ] Deux exécutions de la tâche le même jour → **une seule** notification
- [ ] Contrat renouvelé (nouveau `EN_ATTENTE`/`EN_COURS`) → l'alerte disparaît

---

## Partie G – Frontend Thymeleaf (J6-J12)

- [ ] **Chargé de recrutement** : liste des candidats `RETENUE` ; formulaire de proposition ; statut d'envoi ; badge « envoyée automatiquement » (F2)
- [ ] **Candidat** : « Mes propositions » → détail, boutons **Accepter / Refuser** (modale avec motif), compte à rebours jusqu'à la date limite (l'écran est lié à l'espace candidat de P3)
- [ ] **Gestionnaire RH – `hiring/rh-confirmation.html`** : file des embauches à valider (avec les **motifs d'échec de F1**), bouton « Confirmer / Refuser », résultat avec **matricule affiché**
- [ ] **Gestionnaire RH – fiche employé** : identité, matricule, statut, historique des contrats (frise), historique des affectations, documents (via P5)
- [ ] **Gestionnaire RH – contrats** : création, activation, renouvellement/avenant, clôture, avec validations expliquées à l'écran
- [ ] **Encart d'alertes CDD** (F3) – voir Partie F3-b
- [ ] Vérifier chaque écran avec un rôle non autorisé

---

## Tests à fournir (récapitulatif)
- [ ] Unicité du matricule sous concurrence (50 threads) — format validé
- [ ] Conversion refusée si proposition non `ACCEPTEE`, si poste non `VACANT`, si le candidat est déjà employé
- [ ] Historique : renouveler 2 fois → 3 lignes, chaînage correct, un seul `EN_COURS`
- [ ] Renouvellement qui échoue → l'ancien contrat reste `EN_COURS`
- [ ] Validations contrat : CDI avec date fin → refus ; CDD/STAGE/ALTERNANCE sans date fin → refus ; chevauchement → refus
- [ ] Modification directe des termes d'un contrat actif → `ORA-20011` traduit en message métier
- [ ] F1 (7 conditions), F2 (idempotence, données manquantes), F3 (seuil, périmètre manager, anti-doublon)

## Recette (J13-J14)
- [ ] **S4 :** candidature `RETENUE` → proposition **envoyée automatiquement** → acceptation → validation auto → employé + matricule
- [ ] **S5 (début) :** premier contrat → onboarding généré chez P5
- [ ] **S6 :** CDD à 15 jours de la fin → alerte visible RH **et** manager → renouvellement → ancien `RENOUVELE`, nouveau `EN_COURS`

## Écarts / points à trancher
1. **Salaire de la proposition automatique (F2)** : d'où vient-il ? (champ sur l'offre saisi par le Chargé RH, grille du poste, ou budget/effectif de la demande). Sans réponse, la proposition auto reste en `BROUILLON`.
2. **Budget (C4)** : décision D3 (total vs. par poste, annuel vs. mensuel).
3. **Rôle de `Affectation`** : décision D8 (proposé : historique employé ↔ poste + supérieur).
4. Qui valide l'intégration ? Le Gestionnaire RH (document) **ou** le système (F1) → proposé : système si toutes les conditions OK, Gestionnaire RH sinon.
5. Limite de renouvellements d'un CDD : valeur à retenir pour le projet (paramètre `app.contrat.renouvellements-max`).
6. `DEVISE` du contrat : `EUR` par défaut dans le script SQL (commenté « adapter à la devise locale ») → à fixer avec l'équipe.
