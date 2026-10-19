# Démarche Complète et Spécifications Fonctionnelles - Application RH

Ce document constitue le guide de référence méthodologique, fonctionnel et technique pour la conception et le développement de l'application de gestion des Ressources Humaines (RH).

## 1. Démarche et Méthodologie Proposée

Pour mener à bien le développement de cette application basée sur les 4 blocs fonctionnels, nous préconisons une **démarche itérative en 5 étapes clés** :

```
[Étape 1: Conception & Modélisation] 
       │
       ▼
[Étape 2: Mise en place de la Base & Sécurité (RBAC)]
       │
       ▼
[Étape 3: Développement par Bloc (1 ➔ 2 ➔ 3 ➔ 4)]
       │
       ▼
[Étape 4: Moteur de Workflow & Intégration GED]
       │
       ▼
[Étape 5: Recette & Déploiement]

```

1. **Étape 1 - Conception et Validation du Modèle de Données :** Définir les schémas de base de données en veillant aux règles d'intégrité (séparation `Poste`/`Employé`, `Candidat`/`Employé`, historique des contrats).

2. **Étape 2 - Sécurisation et Gestion des Accès (RBAC) :** Implémenter le système d'authentification et de rôles/permissions strictes avant le développement des écrans métier.

3. **Étape 3 - Développement Métier par Séquence :**

   * *Sprint 1 :* Bloc 1 (Expression du besoin & validations).

   * *Sprint 2 :* Bloc 2 (Recrutement, Offres & Candidatures).

   * *Sprint 3 :* Bloc 3 (Sélection, Offre d'embauche & Conversion Salarié).

   * *Sprint 4 :* Bloc 4 (Contrats & Checklist d'Onboarding).

4. **Étape 4 - Moteur de Workflow & Notification Service :** Orchestrer les transitions d'états, le stockage GED sécurisé des documents (CV, pièces d'identité, contrats) et les notifications e-mails automatiques.

5. **Étape 5 - Recette, Tests & Déploiement :** Valider les parcours utilisateurs de bout en bout et déployer l'architecture applicative.

## 2. Découpage par Rôle Utilisateur

L'application repose sur une gestion des accès par rôles (RBAC - Role-Based Access Control) pour garantir la confidentialité et la cohérence des processus.

### 2.1. Manager (Chef de Département / Responsable d'Équipe)

* **Périmètre :** Son département et ses collaborateurs directes.

* **Missions dans l'application :**

  * **Bloc 1 :** Créer et soumettre une demande de besoin en personnel (création ou remplacement).

  * **Bloc 1 :** Suivre le statut de ses demandes (En attente RH, En attente Direction, Validé).

  * **Bloc 2 :** Examiner la liste des candidats présélectionnés transmis par le recruteur.

  * **Bloc 2 :** Réaliser les entretiens techniques, saisir la grille d'évaluation et consigner son avis.

  * **Bloc 4 :** Réaliser les tâches d'accueil de la checklist d'onboarding (ex: entretien d'intégration J1, désignation d'un parrain).

### 2.2. Chargé de Recrutement RH

* **Périmètre :** Gestion opérationnelle du recrutement.

* **Missions dans l'application :**

  * **Bloc 1 :** Examiner, qualifier et vérifier la conformité des demandes émises par les managers.

  * **Bloc 1 :** Soumettre la demande à la Direction pour arbitrage budgétaire.

  * **Bloc 2 :** Convertir une demande validée en `OffreEmploi` et la diffuser.

  * **Bloc 2 :** Réceptionner les candidatures, procéder à la présélection (tri de premier niveau).

  * **Bloc 2 :** Transmettre les profils retenus au manager concerné et planifier les entretiens.

  * **Bloc 3 :** Rédiger et envoyer la proposition d'embauche au candidat finaliste.

### 2.3. Direction / Validateur Budgétaire

* **Périmètre :** Arbitrage stratégique et financier.

* **Missions dans l'application :**

  * **Bloc 1 :** Valider ou rejeter les demandes de recrutement soumises (avec justification en cas de refus).

  * **Transverse :** Consulter les tableaux de bord et reporting BI (masses salariales prévisionnelles, volumes de recrutements par département).

### 2.4. Gestionnaire RH / Administrateur Personnel

* **Périmètre :** Administration des salariés, contrats et processus d'intégration.

* **Missions dans l'application :**

  * **Bloc 3 :** Valider l'intégration du candidat accepté et générer automatiquement son dossier `Employé` ainsi que son **Matricule** unique.

  * **Bloc 4 :** Rédiger les contrats de travail (CDD, CDI, Avenants) et gérer l'historique sans écraser les données antérieures.

  * **Bloc 4 :** Initialiser et administrer les checklists d'onboarding.

  * **Bloc 4 :** Assigner les tâches aux différents acteurs (IT, Moyens Généraux, Salarié, Manager) et suivre l'avancement global.

### 2.6. Candidat (Espace externe)

* **Périmètre :** Candidat aux offres de l'entreprise.

* **Missions dans l'application :**

  * **Bloc 2 :** Parcourir les offres publiées et soumettre un dossier (CV, lettre de motivation).

  * **Bloc 2 :** Suivre le statut de sa ou ses candidature(s).

  * **Bloc 3 :** Consulter et accepter/refuser la proposition d'embauche reçue.

### 2.7. Salarié / Employé (Portail interne)

* **Périmètre :** Espace collaborateur personnel.

* **Missions dans l'application :**

  * **Bloc 4 :** Compléter sa partie dans la checklist d'onboarding (téléversement du RIB, carte vitale, pièce d'identité, signature électronique du règlement).

  * **Transverse :** Consulter la liste de ses contrats, affectations et fiches de poste dans sa GED personnelle.

## 3. Spécification des Workflows Métier (4 Blocs)

```
┌─────────────────────────┐      ┌─────────────────────────┐
│ BLOC 1 : BESOIN         │      │ BLOC 2 : RECRUTEMENT    │
│ Manager -> RH -> Dir.   │ ───> │ Offre -> Tri -> Entretien│
└─────────────────────────┘      └─────────────────────────┘
             │                                │
             ▼                                ▼
┌─────────────────────────┐      ┌─────────────────────────┐
│ BLOC 4 : ONBOARDING     │      │ BLOC 3 : EMBAUCHE       │
│ Checklist & Suivi Multi │ <─── │ Offre Val. -> Matricule │
└─────────────────────────┘      └─────────────────────────┘

```

### 3.1. Bloc 1 - Expression du Besoin en Personnel

1. **Émission :** Le Manager crée la `DemandeRecrutement` avec les attributs (`Poste`, `Département`, `Effectif`, `Motif`, `Budget`, `Date souhaitée`).

2. **Circuit de validation :**
   

   $$
   \text{Manager (Saisie)} \longrightarrow \text{RH (Vérification)} \longrightarrow \text{Direction (Approbation)} \longrightarrow \text{Ouverture Recrutement}
   $$

3. **Règle d'or :** L'objet `Poste` est dissocié de l'objet `Employé`. Un poste peut exister à l'état "Vacant" sans qu'aucun salarié ne lui soit rattaché.

### 3.2. Bloc 2 - Processus de Recrutement

1. **Offre :** La RH transforme la demande validée en `OffreEmploi` publique.

2. **Postulation :** Réception de plusieurs `Candidature` associées à des `Candidat`.

3. **Présélection RH :** Le chargé de recrutement filtre et sélectionne les profils viables (`CANDIDATURE_PRESELECTIONNEE`).

4. **Évaluation :** Le manager réalise l'entretien et renseigne une fiche `EvaluationCandidat` (`Note`, `Avis`, `Décision`).

### 3.3. Bloc 3 - Sélection et Embauche

1. **Proposition :** L'offre d'embauche est envoyée au candidat finaliste.

2. **Acceptation :** Le candidat accepte la proposition.

3. **Transformation :**

   * **Règle d'or :** `CANDIDAT` $\neq$ `EMPLOYÉ`. Une personne physique conserve son historique de candidatures.

   * La validation déclenche la création d'un profil `Employé` et la génération d'un **Matricule unique** (ex: `EMP-2026-0042`).

### 3.4. Bloc 4 - Contrat et Onboarding

1. **Contrat :** Création du `Contrat` initial.

   * **Règle d'or :** Ne pas écraser l'historique en stockant uniquement un champ `type_contrat` dans la table employé. Chaque contrat fait l'objet d'un enregistrement distinct.

2. **Parcours d'Onboarding (Checklist) :** Génération automatique d'un ensemble d'objets `OnboardingTache` assignés aux différents rôles :

   * *RH :* Inscription à la paie, constitution du dossier administratif.

   * *IT :* Préparation de la machine, création des accès email/outils.

   * *Moyens Généraux :* Badge physique, poste de travail.

   * *Manager :* Entretien J1, désignation d'un parrain.

   * *Salarié :* Téléversement RIB, pièce d'identité.

## 4. Modèle de Données et Structure des Entités Métier

### 4.1. Relations entre les Entités (Vue conceptuelle)

* `Departement` 1 --- \*\*\* `Poste`

* `Poste` 1 --- \*\*\* `DemandeRecrutement`

* `Poste` 1 --- \*\*\* `OffreEmploi`

* `OffreEmploi` **1 --- \*\*\* `Candidature`  \* --- 1** `Candidat`

* `Candidature` **1 --- \*\*\* `Entretien`  \* --- 1** `EvaluationCandidat`

* `Candidat` **1 --- 0..1** `Employe`

* `Employe` 1 --- \*\*\* `Contrat`

* `Employe` **1 --- 1** `Onboarding` 1 --- \*\*\* `OnboardingTache`

### 4.2. Liste Détaillée des Objets Métier

* **Departement :** `id`, `code`, `libelle`, `responsable_id`

* **Poste :** `id`, `code_poste`, `intitule`, `description`, `departement_id`, `statut_poste` (*Créé, Vacant, Pourvu, Fermé*)

* **DemandeRecrutement :** `id`, `poste_id`, `manager_id`, `motif`, `effectif_demande`, `budget_alloue`, `date_souhaitee`, `statut_validation` (*Brouillon, Attente_RH, Attente_Direction, Validee, Refusee*)

* **OffreEmploi :** `id`, `demande_recrutement_id`, `titre`, `description_poste`, `profil_recherche`, `statut_offre` (*Brouillon, Publiee, Cloturee*)

* **Candidat :** `id`, `nom`, `prenom`, `email`, `telephone`, `adresse`, `cv_url`

* **Candidature :** `id`, `offre_id`, `candidat_id`, `date_postulation`, `statut_candidature` (*Recue, Preselectionnee, En_Entretien, Retenue, Rejetee*)

* **Entretien / EvaluationCandidat :** `id`, `candidature_id`, `intervenant_id`, `date_entretien`, `note_technique`, `commentaires`, `decision` (*Favorable, Defavorable, Reserve*)

* **Employe :** `id`, `matricule` (Unique), `candidat_id`, `nom`, `prenom`, `email_professionnel`, `date_embauche_initiale`, `statut_activite` (*En_Onboarding, Actif, Suspendu, Sorti*)

* **Contrat :** `id`, `employe_id`, `poste_id`, `type_contrat` (*CDD, CDI, Stage, Alternance*), `date_debut`, `date_fin`, `salaire_brut`, `statut_contrat` (*En_Attente, En_Cours, Renouvele, Termine*)

* **Onboarding :** `id`, `employe_id`, `date_debut`, `date_fin_prevue`, `statut_global` (*Non_Debute, En_Cours, Complete*)

* **OnboardingTache :** `id`, `onboarding_id`, `intitule_tache`, `service_responsable` (*RH, IT, MANAGER, MOYENS_GENERAUX, SALARIE*), `assignee_a_id`, `est_obligatoire`, `statut_tache` (*A_Faire, En_Cours, Validee*), `date_limite`

* **Document :** `id`, `nom_fichier`, `type_document` (*CV, Contrat_Signe, RIB, Piece_Identite*), `entite_rattachement`, `entite_id`, `url_stockage_ged`

## 5. Spécifications des API REST Cibles

### 5.1. Bloc 1 - Expression du Besoin (`/api/v1/demandes-recrutement`)

* `POST /` : Soumettre une nouvelle demande de besoin (Manager).

* `GET /` : Consulter les demandes (Filtres par statut, département, manager).

* `GET /{id}` : Détails d'une demande de recrutement.

* `PATCH /{id}/validation` : Mettre à jour le statut de validation (RH ou Direction).

### 5.2. Bloc 2 - Recrutement et Candidatures (`/api/v1/recrutement`)

* `POST /offres` : Publier une nouvelle offre d'emploi à partir d'une demande validée.

* `GET /offres` : Lister les offres d'emploi publiques ou internes.

* `POST /candidatures` : Soumission d'une candidature (Public / Candidat).

* `GET /candidatures` : Liste filtrée des candidatures par offre/statut (RH).

* `PATCH /candidatures/{id}/statut` : Faire évoluer le statut (ex: Présélectionné).

* `POST /candidatures/{id}/evaluations` : Enregistrer un compte-rendu d'entretien et une note.

### 5.3. Bloc 3 - Sélection et Embauche (`/api/v1/embauche`)

* `POST /propositions` : Émettre une proposition d'embauche à un candidat.

* `PATCH /propositions/{id}/reponse` : Enregistrer la réponse du candidat (Acceptée/Refusée).

* `POST /candidats/{id}/convertir-employe` : Créer l'entité `Employe`, générer le matricule et basculer le profil.

### 5.4. Bloc 4 - Contrats & Onboarding (`/api/v1/onboarding`)

* `POST /contrats` : Générer un nouveau contrat (CDD, CDI, renouvellement).

* `GET /employes/{id}/contrats` : Obtenir l'historique complet des contrats d'un employé.

* `POST /employes/{id}/initialiser-onboarding` : Générer l'instance d'onboarding et ses tâches associées.

* `GET /taches/mes-taches` : Récupérer les tâches d'onboarding attribuées à l'utilisateur connecté.

* `PATCH /taches/{tacheId}` : Valider ou mettre à jour l'état d'avancement d'une tâche d'intégration.

## 6. Architecture Technique et Infrastructure

D'après le schéma d'architecture cible, l'application s'appuie sur une séparation claire entre la couche applicative, la couche de données et les services d'intégration :

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           COUCHE APPLICATIVE                            │
│  ┌──────────────────┐       ┌────────────────┐       ┌───────────────┐  │
│  │ Front-end Web/Mob│ <───> │    API REST    │ <───> │ Services RH   │  │
│  └──────────────────┘       └────────────────┘       └───────────────┘  │
│                                                              │          │
│                                                   ┌──────────────────┐  │
│                                                   │ Moteur Workflow  │  │
│                                                   └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                     │
┌─────────────────────────────────────────────────────────────────────────┐
│                          DONNÉES & INTÉGRATION                          │
│  ┌──────────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ PostgreSQL / MySQL   │  │ GED / Documents  │  │ Notifications    │  │
│  └──────────────────────┘  └──────────────────┘  └──────────────────┘  │
│  ┌──────────────────────┐  ┌──────────────────┐                        │
│  │ Audit & Logs         │  │ Reporting / BI   │                        │
│  └──────────────────────┘  └──────────────────┘                        │
└─────────────────────────────────────────────────────────────────────────┘

```

1. **Couche Présentation (Front-end) :** Application Web monopage (SPA) responsive, proposant un tableau de bord sur-mesure selon le rôle connecté (Manager, RH, Direction, Salarié).

2. **Couche Services (Back-end) :**

   * **API REST :** Point d'entrée sécurisé (JWT / OAuth2).

   * **Services Métier RH :** Logique d'entreprise (gestion des règles de gestion, matricules).

   * **Moteur de Workflow :** Gestionnaire d'états contrôlant les transitions du besoin jusqu'à la fin de l'onboarding.

3. **Données et Stockage :**

   * **SGBD Relationnel (PostgreSQL / MySQL) :** Stockage structuré de l'ensemble des entités métiers et liens de traçabilité.

   * **GED (Gestion Électronique de Documents) :** Espace de stockage sécurisé pour les pièces jointes (CVs, contrats d'embauche, pièces d'identité).

   * **Services Transverses :** Service d'envoi de mails/notifications, journalisation d'audit (Audit Logs) pour la conformité et module Reporting/BI pour les synthèses de Direction.