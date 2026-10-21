# TODO P3 – Bloc 2 (Recrutement)

**Projet :** Gestion RH · Spring Boot 4.1.1 · JPA · Spring Security · Thymeleaf · Oracle
**Lire d'abord :** `00_VUE_ENSEMBLE_equipe.md`
**Ton rôle :** transformer une demande validée en **offre d'emploi**, gérer les **candidatures**, les **entretiens** et les **évaluations**. Ta sortie (candidature `RETENUE`) déclenche, côté P4, l'**envoi automatique de la proposition d'embauche** (fonctionnalité F2).

---

## 0. Livrables / dépendances

| Tu fournis | À qui | Pour quand |
|---|---|---|
| Données de test : offre `PUBLIEE` + candidatures à tous les statuts | P4 | **J4** |
| `CandidatureService.changerStatut(...)` + `CandidatureRetenueEvent` | P4 (F2) | **J8** |
| Évaluations (`EvaluationCandidat`) lisibles par service | P4 (F1) | J8 |
| Historique de traitement des candidatures | RH, Direction | J10 |

| Tu attends | De qui | Solution de repli |
|---|---|---|
| Demandes `VALIDEE` | P2 | insérer une demande `VALIDEE` dans le script de démo |
| Compte `CANDIDAT`, login | P1 | profil `dev` |
| `DocumentFacade.stocker` (CV, lettre) | P5 | stockage provisoire dans `./uploads` puis brancher |
| `NotificationFacade`, `WorkflowEngine` | P5 | `log.info` temporaire |

---

## Partie A – Offres d'emploi (`OFFRE_EMPLOI`) (J3-J6)

Statuts : `BROUILLON, PUBLIEE, CLOTUREE` · diffusion : `INTERNE, EXTERNE, MIXTE`.

- [ ] `OffreEmploiService.creerDepuisDemande(demandeId, form)` :
  - La demande doit être **`VALIDEE`** (sinon `BusinessException`) et ne doit pas déjà avoir une offre active
  - Pré-remplir `titre` et `description_poste` depuis le poste ; `profil_recherche`, `diffusion`, `date_limite_candidature` saisis par le Chargé RH
  - Statut initial `BROUILLON`
- [ ] `publier(id)` : `BROUILLON → PUBLIEE`, renseigne `DATE_PUBLICATION`, vérifie que titre/description/profil sont remplis et que `date_limite_candidature` est dans le futur
- [ ] `cloturer(id)` : `PUBLIEE → CLOTUREE`, renseigne `DATE_CLOTURE`
  - **Clôture automatique** (tâche planifiée `@Scheduled(cron = "0 0 1 * * *")`) quand `date_limite_candidature` est dépassée
  - À la clôture : les candidatures encore `RECUE` restent consultables ; les candidats concernés peuvent être prévenus (bonus)
- [ ] `lister` : version **publique/candidat** (offres `PUBLIEE` dont la diffusion est `EXTERNE`/`MIXTE`, non expirées) et version **interne** (tous statuts, filtres : statut, département, texte) avec pagination
- [ ] Endpoints REST `POST /api/v1/recrutement/offres`, `GET /api/v1/recrutement/offres` (spec §5.2)
- [ ] Tests : création depuis demande non validée → refus ; publication d'une offre incomplète → refus ; offre clôturée invisible côté candidat

---

## Partie B – Candidatures (`CANDIDATURE`) (J4-J9)

Statuts : `RECUE, PRESELECTIONNEE, EN_ENTRETIEN, RETENUE, REJETEE`. Contrainte base : `UNIQUE (OFFRE_ID, CANDIDAT_ID)`.

### B1. Postuler (rôle CANDIDAT)
- [ ] `CandidatureService.postuler(offreId, form, fichierCv, lettre)` :
  - L'offre est `PUBLIEE` et non expirée
  - **Pas de double candidature** : contrôle applicatif (`existsByOffreIdAndCandidatId`) **et** gestion de l'exception de la contrainte `UK_CDT_OFFRE_CAND` (deux clics rapides)
  - CV : appeler `DocumentFacade.stocker(..., TypeDocument.CV, "CANDIDATURE", id, ...)` (décision D7 : le CV est dans la GED, `CANDIDAT.CV_URL` pointe vers le courant) ; lettre de motivation facultative (`LETTRE_MOTIVATION`)
  - Statut initial `RECUE`, `DATE_POSTULATION` = aujourd'hui
  - **Accusé de réception** par e-mail (notification `CANDIDATURE_RECUE`)
  - Notifier le Chargé de recrutement
- [ ] `mesCandidatures()` : uniquement celles du candidat connecté (via `Utilisateur.candidatId`)
- [ ] Le candidat peut **retirer** sa candidature tant qu'elle n'est pas `EN_ENTRETIEN` (statut `REJETEE` avec motif « retirée par le candidat » ou champ dédié – à décider, ne pas supprimer)

### B2. Traitement RH
- [ ] `lister(filtres, pageable)` : filtres offre, statut, texte (nom/e-mail) ; accessible CHARGE_RECRUTEMENT (tout) et MANAGER (**seulement** les candidatures `PRESELECTIONNEE+` de ses offres)
- [ ] `changerStatut(id, nouveauStatut, motif)` — contrôle via `WorkflowEngine` :
  - `RECUE → PRESELECTIONNEE | REJETEE`
  - `PRESELECTIONNEE → EN_ENTRETIEN | REJETEE`
  - `EN_ENTRETIEN → RETENUE | REJETEE`
  - États `RETENUE` et `REJETEE` **terminaux**
  - `REJETEE` exige un **motif** ; le candidat est notifié du changement de statut
- [ ] **Passage à `EN_ENTRETIEN` automatique** à la planification du premier entretien (Partie C) ; **passage à `RETENUE`** uniquement si une évaluation `FAVORABLE` existe (règle à confirmer avec le groupe)
- [ ] **Publier `CandidatureRetenueEvent(candidatureId)` après commit** (`ApplicationEventPublisher`) quand le statut devient `RETENUE` → **P4 en fait la proposition d'embauche automatique** (F2). Ne **pas** appeler P4 directement : ça évite un couplage et un blocage si P4 est en retard
- [ ] Quand une candidature est `RETENUE`, **rejeter automatiquement les autres candidatures** de la même offre si l'effectif demandé est atteint (option, avec notification) — à décider avec P2 (effectif de la demande)
- [ ] **Transmission au manager** : vue « Candidats présélectionnés » pour le manager de la demande + notification quand de nouveaux profils sont transmis (`CANDIDATURES_TRANSMISES`)
- [ ] Endpoints REST `GET /api/v1/recrutement/candidatures`, `POST /candidatures`, `PATCH /candidatures/{id}/statut` (spec §5.2)

### B3. Historique recruteur (`CandidatureRecruteur`)
- [ ] `CandidatureRecruteurService.assigner(candidatureId, recruteurId)` : ferme la ligne courante (`DATE_FIN`) puis insère la nouvelle ; une candidature a **un seul recruteur actif** à la fois
- [ ] Assigner automatiquement le Chargé RH qui présélectionne s'il n'y en a pas
- [ ] Écran « Historique de traitement » sur la fiche candidature

---

## Partie C – Entretiens et évaluations (J7-J10)

### C1. Entretiens (`ENTRETIEN`) – types `RH, TECHNIQUE, FINAL` · statuts `PLANIFIE, REALISE, ANNULE, REPORTE`
- [ ] `EntretienService.planifier(candidatureId, form)` : date/heure **future**, type, intervenant (utilisateur Manager/RH), lieu ou lien visio
  - Refuser si le candidat n'est pas `PRESELECTIONNEE` ou `EN_ENTRETIEN`
  - **Détection de conflit** : l'intervenant ne peut pas avoir deux entretiens qui se chevauchent
  - Notification de **convocation** (candidat + intervenant) avec date/lieu
- [ ] `reporter`, `annuler` (motif) → nouvelle notification ; un entretien `REALISE` ne se modifie plus
- [ ] Un entretien devient `REALISE` quand l'évaluation est saisie
- [ ] (Bonus) génération d'un fichier `.ics` pour l'agenda

### C2. Évaluation (`EVALUATION_CANDIDAT` + `EVALUATION_CRITERE`)
- [ ] `EvaluationCandidatService.saisir(entretienId, form)` : `note_technique` entre **0 et 20**, `commentaires`, `decision` (`FAVORABLE/DEFAVORABLE/RESERVE`)
  - **Une seule évaluation par entretien** (`UK_EVAL_ENTRETIEN`)
  - Seul l'intervenant désigné (ou un RH) peut saisir
- [ ] `EvaluationCritereService` : grille détaillée (`CRITERE`, `NOTE` 0-20, `POIDS` > 0) ; **la note technique globale peut être calculée** = moyenne pondérée (Σ note×poids / Σ poids) – recalcul automatique à chaque modification de critère
- [ ] Endpoint REST `POST /api/v1/recrutement/candidatures/{id}/evaluations` (spec §5.2)
- [ ] Fournir à P4 `EvaluationCandidatService.derniereDecision(candidatureId)` (utilisé par la validation auto F1)

### C3. Notifications du Bloc 2 (via `NotificationFacade` de P5)
| Code modèle | Quand | Destinataire |
|---|---|---|
| `CANDIDATURE_RECUE` | candidature déposée | candidat |
| `CANDIDATURE_STATUT` | changement de statut | candidat |
| `ENTRETIEN_CONVOCATION` | entretien planifié/reporté/annulé | candidat + intervenant |
| `CANDIDATURES_TRANSMISES` | profils présélectionnés envoyés | manager |
| `EVALUATION_SAISIE` | évaluation terminée | chargé RH |

---

## Partie D – Frontend Thymeleaf (J5-J11)

Maquettes à brancher : `recruitment/candidat-offres.html`, `candidat-candidature.html`, `rh-candidatures.html`, `security/candidat-cv.html`, `candidat-cv-creer.html`.

- [ ] **Candidat – `candidat-offres.html`** : liste paginée des offres publiées, recherche par mot-clé, détail d'une offre, bouton « Postuler » (désactivé si déjà candidat ou offre expirée)
- [ ] **Candidat – `candidat-candidature.html`** : formulaire (CV + lettre), contrôle côté client du type (`pdf, doc, docx`) et de la taille (voir `app.stockage.taille-max-mo`), page de confirmation
- [ ] **Candidat – espace « Mes candidatures »** : statut avec `badgeStatut` et frise (Reçue → Présélectionnée → Entretien → Retenue/Rejetée), proposition d'embauche si disponible (écran de P4)
- [ ] **Candidat – `candidat-cv.html` / `candidat-cv-creer.html`** : consultation et création du CV en ligne ; **à clarifier en J1** (le CV créé en ligne est-il un `DOCUMENT` généré ?), importation d'un fichier via P5
- [ ] **Chargé RH – création/publication d'offre** : formulaire depuis une demande `VALIDEE`, boutons Publier / Clôturer
- [ ] **Chargé RH – `rh-candidatures.html`** : tableau de présélection (filtres offre/statut), actions groupées « Présélectionner », « Rejeter » (motif), planification d'entretien, voir CV (lien GED sécurisé)
- [ ] **Manager** : liste des candidats présélectionnés de son département, fiche candidat (CV, historique), **grille d'évaluation** (critères dynamiques + note globale calculée + décision)
- [ ] Vérifier chaque écran avec un rôle non autorisé (403)

---

## Tests (à fournir avec la PR)
- [ ] Postuler deux fois à la même offre → refus (applicatif **et** contrainte de base)
- [ ] Postuler à une offre `CLOTUREE` ou expirée → refus
- [ ] Transitions de statut : toutes les autorisées + `RETENUE→REJETEE`, `REJETEE→PRESELECTIONNEE`, `RECUE→RETENUE` refusées
- [ ] Rejet sans motif → refus
- [ ] Conflit d'horaire d'un intervenant → refus
- [ ] Note hors 0–20 → refus ; calcul de la moyenne pondérée
- [ ] `CandidatureRetenueEvent` publié **une seule fois**, seulement au passage à `RETENUE`
- [ ] Un manager ne voit pas les candidatures d'un autre département ; un candidat ne voit que les siennes

## Recette (J13-J14)
- [ ] **S3 :** offre → candidature → présélection → entretien → évaluation → `RETENUE`
- [ ] Vérifier la réception des e-mails (accusé de réception, convocation, changement de statut)
- [ ] Vérifier que le passage à `RETENUE` déclenche bien la proposition automatique chez P4 (**S4**)

## Écarts / points à trancher
1. Statuts de candidature : enum `StatutCandidature` partagée avec P4 → **résolu** (créée par P1, valeurs en majuscules).
2. Stockage du CV : décision D7 (GED recommandée) → à confirmer avec P5.
3. Faut-il rejeter automatiquement les autres candidatures quand l'effectif est atteint ? (à décider avec P2).
4. Condition exacte pour passer à `RETENUE` : une évaluation `FAVORABLE` obligatoire, ou simple décision du Chargé RH ?
5. Retrait de candidature par le candidat : quel statut (`REJETEE` + motif ou nouvel état) ? Le `CHECK` actuel n'a pas de statut `RETIREE`.
