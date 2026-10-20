# TODO P2 – Organisation + Bloc 1 (Expression du besoin)

**Projet :** Application Gestion RH · Spring Boot (`com.itu.gestionRH`) · JPA · API REST `/api/v1` · JWT · Front SPA
**État de départ :** entités et repositories existants ; controllers et services = classes vides à implémenter.
**Équipe :** P1 Fondations/Sécurité · P2 Organisation + Bloc 1 · P3 Bloc 2 · P4 Bloc 3 + Contrats · P5 Bloc 4 + Transverse

## Ce que je fournis aux autres
- Départements et postes (utilisés par P3, P4)
- Demandes de recrutement **validées** (point de départ de P3)

## Ce que j'attends des autres
- P1 : authentification, rôles, layout front
- P5 : `NotificationService` et moteur de workflow (machine à états)

## Backend – Organisation
- [ ] `DepartementService` / `DepartementController` : CRUD
- [ ] Rattachement hiérarchique (`DepartementRattachment`) et responsable (`DepartementResponsable`) avec historique
- [ ] `PosteService` / `PosteController` : CRUD, statuts `Créé / Vacant / Pourvu / Fermé`
- [ ] `PosteDepartementService` / controller : liaison poste ↔ département
- [ ] **Règle d'or :** un `Poste` existe sans `Employé` (poste vacant) – à couvrir par un test

## Backend – Bloc 1 (`/api/v1/demandes-recrutement`)
- [ ] `POST /` : créer une demande (création ou remplacement via `typeDemande` + `employeRemplaceId`), statut initial `Brouillon`
- [ ] Soumission : `Brouillon → Attente_RH`
- [ ] `GET /` avec filtres (statut, département, manager) + pagination
- [ ] `GET /{id}` : détail + historique des validations
- [ ] `PATCH /{id}/validation` : `Attente_RH → Attente_Direction → Validee` ou `Refusee`
- [ ] `DemandeValidationService` : tracer chaque décision (valideur, date, **justification obligatoire en cas de refus**)
- [ ] Règles : un manager ne voit que ses demandes / son département ; seule la Direction approuve ; budget ≥ 0 et effectif > 0
- [ ] Appeler le service de notifications à chaque étape
- [ ] Tests unitaires sur chaque transition (autorisées et interdites)

## Frontend
- [ ] **Manager :** formulaire de demande, liste et suivi de statut (En attente RH / Direction / Validé)
- [ ] **Chargé RH :** file des demandes à qualifier, bouton « Soumettre à la Direction »
- [ ] **Direction :** demandes à arbitrer, validation / refus avec motif
- [ ] Écrans admin : départements et postes

## Recette (Étape 5)
- [ ] Scénario : Manager → RH → Direction → demande validée, puis scénario de refus avec justification

## Écarts à trancher
1. Valeurs possibles de `typeDemande` (ex. `CREATION`, `REMPLACEMENT`).
2. Rôle exact de `DepartementRattachment` et `DepartementResponsable` (historisation ?).
