# TODO P5 – Bloc 4 (Onboarding) + Transverse (Workflow, GED, Notifications, BI)

**Projet :** Application Gestion RH · Spring Boot (`com.itu.gestionRH`) · JPA · API REST `/api/v1` · JWT · Front SPA
**État de départ :** entités et repositories existants ; controllers et services = classes vides à implémenter.
**Équipe :** P1 Fondations/Sécurité · P2 Organisation + Bloc 1 · P3 Bloc 2 · P4 Bloc 3 + Contrats · P5 Bloc 4 + Transverse

**Attention :** tes services transverses (workflow, notifications, GED) sont utilisés par P2, P3 et P4. Livre-en une **première version simple dès la fin de la Phase 0**.

## Ce que je fournis aux autres
- `NotificationService.notify(destinataire, type, message)`
- Composant machine à états (workflow) réutilisable
- `DocumentService` (upload / téléchargement / droits)
- Cloche de notifications pour le layout de P1

## Ce que j'attends des autres
- P1 : authentification, rôles (dont `IT`, `MOYENS_GENERAUX`), `LogAccesDocument`
- P4 : employés et contrats créés

## Services transverses (en premier)
### Notifications
- [ ] `NotificationService` : notification en base (lue / non lue) + envoi e-mail (templates)
- [ ] API : liste, marquer comme lue, compteur de non-lues
- [ ] Interface d'appel simple pour P2, P3, P4

### Moteur de workflow
- [ ] Machine à états générique : statut courant, transitions autorisées, rôle requis, hooks
- [ ] Documenter son usage et l'intégrer avec P2, P3, P4
- [ ] Journalisation de chaque transition (audit)

### GED
- [ ] `DocumentService` / `DocumentController` : upload, téléchargement, suppression logique (`CV, Contrat_Signe, RIB, Piece_Identite`)
- [ ] Stockage sécurisé + URL non devinable ; contrôle du type et de la taille
- [ ] Contrôle d'accès par document (propriétaire, RH) + journalisation via `LogAccesDocument`
- [ ] Portail salarié : « Mes documents », contrats et fiches de poste

## Bloc 4 – Onboarding (`/api/v1/onboarding`)
- [ ] `ModeleOnbTacheService` : modèles de tâches par service (RH, IT, Moyens Généraux, Manager, Salarié) + caractère obligatoire
- [ ] `POST /employes/{id}/initialiser-onboarding` : générer `Onboarding` + `OnboardingTache` depuis les modèles (une seule fois par employé)
- [ ] `GET /taches/mes-taches` : tâches de l'utilisateur connecté
- [ ] `PATCH /taches/{tacheId}` : `A_Faire → En_Cours → Validee`
- [ ] `TacheAssignmentService` : historique des (ré)assignations
- [ ] Calcul du `statut_global` : `Complete` quand toutes les tâches obligatoires sont validées → employé `Actif`
- [ ] Relances automatiques sur les tâches en retard (`date_limite`)

## Reporting / BI (Direction)
- [ ] Indicateurs : recrutements par département, délai moyen de recrutement, masse salariale prévisionnelle, avancement des onboardings
- [ ] Endpoints de synthèse en lecture seule

## Frontend
- [ ] **Salarié :** checklist d'onboarding, upload RIB / pièce d'identité, signature du règlement, « Mes documents »
- [ ] **Gestionnaire RH :** suivi global d'onboarding, assignation des tâches
- [ ] **Manager / IT / Moyens Généraux :** écran « Mes tâches »
- [ ] Cloche de notifications (à intégrer dans le layout de P1)
- [ ] **Direction :** tableau de bord BI

## Recette (Étape 5)
- [ ] Scénario : onboarding complet jusqu'à `Complete`
- [ ] Test de charge GED et notifications

## Écarts à trancher
1. Rôles `IT` et `MOYENS_GENERAUX` : à créer côté P1.
2. Choix du stockage GED (système de fichiers ou stockage objet).
