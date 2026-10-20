# TODO P3 – Bloc 2 (Recrutement)

**Projet :** Application Gestion RH · Spring Boot (`com.itu.gestionRH`) · JPA · API REST `/api/v1` · JWT · Front SPA
**État de départ :** entités et repositories existants ; controllers et services = classes vides à implémenter.
**Équipe :** P1 Fondations/Sécurité · P2 Organisation + Bloc 1 · P3 Bloc 2 · P4 Bloc 3 + Contrats · P5 Bloc 4 + Transverse

## Ce que je fournis aux autres
- Candidatures au statut `Retenue` (point de départ de P4)
- Évaluations et historique de traitement

## Ce que j'attends des autres
- P1 : authentification, rôle `CANDIDAT`, layout front
- P2 : demandes au statut `Validee`
- P5 : `NotificationService`, `DocumentService` (upload CV)

## Backend (`/api/v1/recrutement`)
- [ ] `POST /offres` : créer une `OffreEmploi` **uniquement** à partir d'une demande `Validee` ; statuts `Brouillon / Publiee / Cloturee`
- [ ] `GET /offres` : liste publique (candidats) et interne (filtres)
- [ ] Publication / clôture d'une offre
- [ ] `POST /candidatures` : postuler (CV + lettre) ; empêcher la double candidature à la même offre
- [ ] `GET /candidatures` : filtres par offre / statut (RH) ; candidatures du candidat connecté
- [ ] `PATCH /candidatures/{id}/statut` : `Recue → Preselectionnee → En_Entretien → Retenue / Rejetee` (contrôle des transitions)
- [ ] `CandidatureRecruteurService` : historiser quel recruteur traite quelle candidature
- [ ] `EntretienService` : planifier un entretien (date, intervenant, lieu/lien)
- [ ] `POST /candidatures/{id}/evaluations` : `EvaluationCandidat` (note, avis, décision `Favorable / Defavorable / Reserve`)
- [ ] `EvaluationCritereService` : grille détaillée par critère
- [ ] Transmettre les profils présélectionnés au manager concerné
- [ ] Notifications : accusé de réception, changement de statut, convocation entretien
- [ ] Tests : transitions de statut, droits d'accès par rôle

## Frontend
- [ ] **Candidat :** liste des offres, détail, formulaire de candidature + upload CV, suivi de ses candidatures
- [ ] **Chargé de recrutement :** création / publication d'offre, tableau de présélection, planification d'entretiens
- [ ] **Manager :** liste des candidats présélectionnés, fiche et grille d'évaluation

## Recette (Étape 5)
- [ ] Scénario : offre → candidature → présélection → entretien → évaluation → candidature retenue

## Écarts à trancher
1. Statuts de candidature : `enum` partagée avec P4.
2. Où stocker le CV : `cv_url` sur `Candidat` ou `Document` via la GED (à caler avec P5).
