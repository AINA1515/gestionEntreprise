# TODO P2 – Organisation + Bloc 1 (Expression du besoin)

**Projet :** Gestion RH · Spring Boot 4.1.1 · JPA · Spring Security · Thymeleaf · Oracle
**Lire d'abord :** `00_VUE_ENSEMBLE_equipe.md`
**Ton rôle :** tu modélises la structure de l'entreprise (départements, postes) puis tu implémentes le **circuit de validation d'une demande de recrutement** : Manager → RH → Direction. Ta sortie (demande `VALIDEE`) est le point de départ de P3.

---

## 0. Livrables / dépendances

| Tu fournis | À qui | Pour quand |
|---|---|---|
| `DepartementService`, `PosteService` (lecture : listes, par id) | P3, P4 | **J4** |
| `DemandeRecrutementService.trouverValidees()` + `DemandeValideeEvent` | P3 | **J7** |
| Widget « CDD bientôt expirés » dans le dashboard manager | P4 → toi | J10 |

| Tu attends | De qui | Solution de repli |
|---|---|---|
| Login, rôles, layout, `UtilisateurCourant` | P1 | profil `dev` (utilisateur fictif) |
| `NotificationFacade`, `WorkflowEngine` | P5 | version « simple » livrée en Phase 0 ; sinon `log.info` temporaire |
| Enums (`StatutPoste`, `StatutValidation`, `TypeDemande`) | P1 | les créer toi-même dans `enums/` si absentes, puis prévenir P1 |

---

## Partie A – Backend Organisation (J1-J5)

Entités concernées : `Departement`, `Poste`, `PosteDepartement`, `DepartementResponsable`, `DepartementRattachment` (table `DEPARTEMENT_RATTACHEMENT`) ; repositories existants.

### A1. Départements
- [ ] `DepartementService` : `creer`, `modifier`, `desactiver` (colonne `EST_ACTIF`, **pas de suppression physique**), `lister` (pagination + recherche par code/libellé), `trouverParId`
- [ ] Règles : `CODE` unique et non vide (majuscules, ex. `INFO`) ; un département désactivé ne peut plus recevoir de nouveau poste
- [ ] `DepartementController` (`/admin/departements`) : liste, formulaire création/édition, désactivation avec confirmation
- [ ] **Responsable de département avec historique** (`DepartementResponsable`) :
  - Le trigger de base alimente déjà `DEPARTEMENT_RESPONSABLE` quand on change `DEPARTEMENT.RESPONSABLE_ID` (voir `07_verification/02_exemple_test.sql` : « 2 lignes : la 1re clôturée, la 2e courante »). **Vérifier ce comportement avant de coder** pour ne pas écrire deux fois l'historique
  - Service `changerResponsable(departementId, employeId)` : modifie `RESPONSABLE_ID` et lit l'historique pour l'afficher
  - Écran : « Historique des responsables » (date début, date fin)
- [ ] **Rattachement hiérarchique** (`DepartementRattachment`) : `rattacher(departementId, parentId, dateDebut)` ; interdire l'auto-rattachement (`CK_DEPRAT_AUTO`) et les **cycles** (A parent de B, B parent de A ou plus profond) → parcours de l'arbre avant d'enregistrer ; clôturer l'ancien rattachement (`DATE_FIN`) avant d'en créer un nouveau
- [ ] Affichage de l'organigramme sous forme d'arbre (liste imbriquée Bootstrap suffit)

### A2. Postes
- [ ] `PosteService` : `creer` (statut initial `CREE`), `modifier`, `changerStatut`, `lister` (filtres : département, statut), `trouverParId`
- [ ] Machine à états du poste : `CREE → VACANT → POURVU → VACANT`, `* → FERME` (voir tableau §4). `POURVU` est posé par P4 à la conversion en employé : expose `PosteService.marquerPourvu(posteId)` et `marquerVacant(posteId)` **pour P4**
- [ ] `CODE_POSTE` unique ; intitulé obligatoire
- [ ] `PosteDepartementService` : lier un poste à un département **avec historique** (`DATE_DEBUT`/`DATE_FIN`) ; un poste n'a qu'un département actif à la fois
- [ ] `PosteController` (`/admin/postes`) : CRUD + filtre par statut + badge de statut
- [ ] **Règle d'or du document : un `Poste` existe sans `Employé`.** Écrire un test qui crée un poste `VACANT` sans aucun employé et vérifie qu'aucune contrainte ne l'empêche
- [ ] Un poste `FERME` ne peut pas faire l'objet d'une nouvelle demande de recrutement

### A3. Tests (organisation)
- [ ] Création département : code dupliqué → refus
- [ ] Rattachement : cycle détecté → refus ; auto-rattachement → refus
- [ ] Poste vacant sans employé → OK
- [ ] Changement de statut interdit (`FERME → VACANT`) → refus

---

## Partie B – Bloc 1 : Demandes de recrutement (J3-J9)

Entités : `DemandeRecrutement` (`DEMANDE_RECRUTEMENT`), `DemandeValidation` (`DEMANDE_VALIDATION`). Statuts : `BROUILLON, ATTENTE_RH, ATTENTE_DIRECTION, VALIDEE, REFUSEE`.

### B1. Création et soumission (rôle MANAGER)
- [ ] `DemandeRecrutementForm` (DTO) avec validations Bean Validation : `posteId` obligatoire, `typeDemande` (`CREATION`/`REMPLACEMENT`), `employeRemplaceId` **obligatoire si REMPLACEMENT** (contrainte `CK_DR_REMPL`), `motif` obligatoire (≤ 1000 car.), `effectifDemande` ≥ 1, `budgetAlloue` ≥ 0 si renseigné, `dateSouhaitee` obligatoire et **dans le futur**
- [ ] `DemandeRecrutementService.creerBrouillon(form)` : `MANAGER_ID` = utilisateur courant, statut `BROUILLON`
  - Le poste doit appartenir au **département dont le manager est responsable**
  - Pour un remplacement : l'employé remplacé doit être `ACTIF` et rattaché à ce département
- [ ] `modifier` (uniquement en `BROUILLON`) et `supprimer` logique/annuler un brouillon
- [ ] `soumettre(id)` : `BROUILLON → ATTENTE_RH`, propriétaire uniquement, vérifier que tous les champs obligatoires sont présents
- [ ] Notifier le(s) Chargé(s) de recrutement (`notifierRole("CHARGE_RECRUTEMENT", ...)`)
- [ ] Endpoint REST `POST /api/v1/demandes-recrutement` (spec §5.1)

### B2. Consultation
- [ ] `lister(filtres, pageable)` : filtres `statut`, `departementId`, `managerId` ; **portée par rôle** :
  - MANAGER : uniquement **ses** demandes / celles de son département
  - CHARGE_RECRUTEMENT : tout (surtout `ATTENTE_RH`)
  - DIRECTION : tout (surtout `ATTENTE_DIRECTION`)
- [ ] `detail(id)` : demande + historique complet des `DemandeValidation` (étape, décision, valideur, date, justification) ; accès refusé (403) si hors périmètre
- [ ] Endpoints REST `GET /api/v1/demandes-recrutement` et `GET /{id}`

### B3. Circuit de validation
- [ ] `DemandeValidationService.decider(demandeId, etape, decision, justification)` — **une transaction** qui :
  1. vérifie le rôle (RH pour l'étape `RH`, DIRECTION pour l'étape `DIRECTION`) via `@PreAuthorize` **et** `WorkflowEngine.verifierTransition(...)`
  2. vérifie que le statut courant correspond à l'étape (`ATTENTE_RH` ↔ `RH`, `ATTENTE_DIRECTION` ↔ `DIRECTION`)
  3. insère une ligne `DemandeValidation` (valideur = utilisateur courant) — **jamais d'`update` d'une décision existante**
  4. fait passer la demande au statut suivant :
     - RH + `VALIDEE` → `ATTENTE_DIRECTION`
     - RH + `REFUSEE` → `REFUSEE`
     - DIRECTION + `VALIDEE` → `VALIDEE` (+ publie `DemandeValideeEvent`)
     - DIRECTION + `REFUSEE` → `REFUSEE`
  5. notifie le manager (et la Direction à l'étape suivante)
- [ ] **Justification obligatoire en cas de refus** (≥ 10 caractères) — contrainte aussi côté base (`CK_DV_JUSTIF`) : gérer proprement l'erreur
- [ ] Une demande déjà `VALIDEE`/`REFUSEE` est **figée** (aucune nouvelle décision)
- [ ] Un manager peut **re-soumettre** une demande refusée en créant une **nouvelle** demande (copie) plutôt qu'en écrasant l'ancienne ; prévoir un bouton « Dupliquer »
- [ ] Endpoint REST `PATCH /api/v1/demandes-recrutement/{id}/validation` (spec §5.1)
- [ ] Méthode pour P3 : `trouverValidees(Pageable)` (demandes `VALIDEE` sans offre existante)

### B4. Règles de gestion récapitulées
| Règle | Où la coder |
|---|---|
| Seule la Direction approuve définitivement | `@PreAuthorize("hasAuthority('DEMANDE_APPROUVER_DIR')")` |
| Le RH vérifie la conformité | `hasAuthority('DEMANDE_VERIFIER_RH')` |
| Budget ≥ 0, effectif > 0 | Bean Validation + contrôle service |
| Un manager ne voit que son périmètre | filtre dans le service, pas dans le template |
| Chaque étape est tracée | table `DEMANDE_VALIDATION` |
| Notification à chaque étape | `NotificationFacade` |

### B5. Tests unitaires (matrice des transitions)
- [ ] **Autorisées** : `BROUILLON→ATTENTE_RH`, `ATTENTE_RH→ATTENTE_DIRECTION`, `ATTENTE_RH→REFUSEE`, `ATTENTE_DIRECTION→VALIDEE`, `ATTENTE_DIRECTION→REFUSEE`
- [ ] **Interdites** : `BROUILLON→VALIDEE`, `ATTENTE_RH→VALIDEE`, `VALIDEE→REFUSEE`, `REFUSEE→ATTENTE_RH`, Manager qui valide, Direction à l'étape RH
- [ ] Refus sans justification → exception
- [ ] Manager A ne peut pas lire la demande du manager B
- [ ] Remplacement sans employé remplacé → refus
- [ ] La publication de `DemandeValideeEvent` n'a lieu qu'au passage à `VALIDEE`

---

## Partie C – Frontend Thymeleaf (J5-J10)

Maquettes à brancher (dans `src/main/resources/templates/recruitment/`) : `manager-dashboard.html`, `manager-demande.html`, `manager-mes-demandes.html`, `rh-decision.html`. Utiliser les fragments du layout de P1.

- [ ] **Manager – `manager-dashboard.html`** : compteurs réels (brouillons, en attente RH, en attente Direction, validées) ; dernières demandes ; **zone d'alertes « contrats CDD bientôt expirés »** (alimentée par le service de P4, voir fonctionnalité F3)
- [ ] **Manager – `manager-demande.html`** : formulaire lié à `DemandeRecrutementForm` (`th:object`, `th:field`, affichage des erreurs par champ) ; liste déroulante des postes **du département du manager** ; champ « employé remplacé » affiché seulement si `REMPLACEMENT` (petit JS)
- [ ] **Manager – `manager-mes-demandes.html`** : tableau paginé + filtre par statut + `badgeStatut` + lien vers le détail avec la **frise de validation** (qui a validé/refusé, quand, pourquoi)
- [ ] **Chargé RH – file de qualification** : demandes `ATTENTE_RH`, détail, boutons « Soumettre à la Direction » / « Refuser » (modale avec motif obligatoire)
- [ ] **Direction – arbitrage** (`rh-decision.html` ou page dédiée) : demandes `ATTENTE_DIRECTION`, résumé budget (montant, département), boutons « Valider » / « Refuser » (motif obligatoire)
- [ ] **Admin – départements et postes** : écrans de la Partie A
- [ ] Messages de succès/erreur (flash), états vides (« Aucune demande »), protection double-clic sur les boutons d'action
- [ ] Vérifier chaque écran avec un compte **non autorisé** (doit renvoyer la page 403)

---

## Intégration avec les autres

- **P3** : après `VALIDEE`, le Chargé RH peut créer l'offre. Fournir un lien « Créer une offre » sur la demande validée.
- **P4** : appelle `PosteService.marquerPourvu` à la conversion ; fournit le service d'alertes CDD que tu affiches (F3).
- **P5** : fournit `WorkflowEngine` et `NotificationFacade` — tes transitions doivent passer par eux.
- **P1** : tes pages utilisent son layout ; tes routes sont déclarées dans sa `SecurityFilterChain` (`/manager/**`, `/rh/**`).

## Recette (J13-J14)
- [ ] **S1 :** Manager crée → soumet → RH qualifie → Direction valide → demande `VALIDEE`, offre créable
- [ ] **S2 :** refus RH avec justification, puis refus Direction avec justification ; vérifier l'historique et les notifications
- [ ] Vérifier que la liste d'un manager ne contient pas les demandes des autres départements

## Écarts / points à trancher
1. `typeDemande` : **résolu** → `CREATION` / `REMPLACEMENT` (contrainte `CK_DR_TYPE`).
2. `DepartementResponsable` et `DepartementRattachment` : ce sont des **tables d'historique** (période `DATE_DEBUT`/`DATE_FIN`) ; vérifier qui, du trigger ou du service, écrit l'historique.
3. `BUDGET_ALLOUE` : montant total annuel ou par poste ? (décision D3 — à confirmer car P4 l'utilise pour la validation automatique de l'embauche).
4. Une demande avec `EFFECTIF_DEMANDE > 1` génère **une seule offre** : combien d'embauches maximum ? (P4 compare le nombre d'employés créés à cet effectif).
