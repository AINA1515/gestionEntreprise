# TODO P4 – Bloc 3 (Embauche) + Contrats & Affectations

**Projet :** Application Gestion RH · Spring Boot (`com.itu.gestionRH`) · JPA · API REST `/api/v1` · JWT · Front SPA
**État de départ :** entités et repositories existants ; controllers et services = classes vides à implémenter.
**Équipe :** P1 Fondations/Sécurité · P2 Organisation + Bloc 1 · P3 Bloc 2 · P4 Bloc 3 + Contrats · P5 Bloc 4 + Transverse

## Ce que je fournis aux autres
- Employé créé avec matricule unique (point de départ de P5)
- Historique des contrats et affectations

## Ce que j'attends des autres
- P1 : authentification et rôles
- P3 : candidatures au statut `Retenue`
- P5 : `initialiser-onboarding`, `NotificationService`, `DocumentService` (contrat signé)

## Backend – Proposition & conversion (`/api/v1/embauche`)
- [ ] `POST /propositions` : proposition d'embauche à un candidat `Retenue` (rémunération, date de début, poste, validité)
- [ ] `PATCH /propositions/{id}/reponse` : réponse du candidat `Acceptée / Refusée`
- [ ] `CompteurMatriculeService` : matricule **unique et thread-safe** (ex. `EMP-2026-0042`), transaction + verrou
- [ ] `POST /candidats/{id}/convertir-employe` : créer l'`Employe`, générer le matricule, statut `En_Onboarding`, conserver `candidat_id`
  - **Règle d'or :** `CANDIDAT ≠ EMPLOYÉ`, l'historique de candidatures est conservé
- [ ] Passer le `Poste` de `Vacant` à `Pourvu`
- [ ] `EmployeService` / `EmployeController` : consultation, recherche, changement de `statut_activite`

## Backend – Contrats
- [ ] `POST /contrats` : CDD / CDI / Stage / Alternance / Avenant / renouvellement
- [ ] `GET /employes/{id}/contrats` : historique complet
- [ ] **Règle d'or :** un contrat = un enregistrement distinct, **aucun écrasement** ; un renouvellement clôture l'ancien (`Termine` / `Renouvele`) et crée un nouveau
- [ ] Validations : `date_fin` > `date_debut`, `date_fin` obligatoire pour un CDD, pas de chevauchement de contrats actifs
- [ ] `AffectationService` / controller : affecter un employé à un poste / département avec historique
- [ ] Déclencher l'initialisation de l'onboarding (appel à P5) après le premier contrat
- [ ] Notifications : proposition envoyée, réponse reçue, employé créé
- [ ] Tests : unicité du matricule sous concurrence, historique des contrats, conversion refusée si proposition non acceptée

## Frontend
- [ ] **Chargé de recrutement :** rédaction et envoi de la proposition
- [ ] **Candidat :** consulter et accepter / refuser la proposition
- [ ] **Gestionnaire RH :** validation de l'intégration, fiche employé, création et historique des contrats, affectations

## Recette (Étape 5)
- [ ] Scénario : proposition → acceptation → employé + matricule → contrat → renouvellement

## Écarts à trancher
1. Rôle exact de `Affectation` (absent du modèle du document).
2. Qui rédige le contrat : Gestionnaire RH (document) – confirmer qu'il valide aussi la conversion.
