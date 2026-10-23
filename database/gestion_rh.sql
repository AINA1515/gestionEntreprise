/* =============================================================================
   APPLICATION DE GESTION RH - SCRIPT MAITRE ORACLE 11g Enterprise Edition
   -----------------------------------------------------------------------------
   Ce fichier execute, dans l'ordre, tous les scripts des sous-dossiers.
   Lancer depuis ce dossier (database/) avec SQL*Plus :
         sqlplus utilisateur/motdepasse@base @gestion_rh.sql
   (@@ = chemin relatif au script appelant)

   Blocs couverts : 1 Besoin | 2 Recrutement | 3 Embauche | 4 Contrat/Onboarding
                    + Securite (RBAC), GED, Notifications, Journaux de securite.

   OBJETS UTILISES : tables, sequences, triggers, fonctions (+ index et contraintes
   rattaches aux tables). Aucun package, aucune procedure stockee.

   TRACABILITE (3 niveaux)
   N1  Colonnes d'audit sur CHAQUE table metier :
         DATE_CREATION, CREE_PAR, DATE_MODIFICATION, MODIFIE_PAR, VERSION_LIGNE
         (VERSION_LIGNE sert aussi au verrouillage optimiste cote application).
   N2  Historisation des RELATIONS : une table datee (DATE_DEBUT / DATE_FIN) pour chaque
         relation qui change dans le temps. L'enregistrement courant a DATE_FIN nulle.
         Nouvelles tables (alimentees automatiquement par triggers) :
           DEPARTEMENT_RESPONSABLE   departement  <-> responsable
           DEPARTEMENT_RATTACHEMENT  departement  <-> departement parent
           POSTE_DEPARTEMENT         poste        <-> departement
           CANDIDATURE_RECRUTEUR     candidature  <-> recruteur
           TACHE_ASSIGNATION         tache d'onboarding <-> assignee
         Deja existantes : DEMANDE_VALIDATION, CONTRAT (1 ligne par acte),
           AFFECTATION (employe <-> poste <-> superieur), UTILISATEUR_ROLE.
   N3  Journaux de securite en ajout seul : LOG_CONNEXION, LOG_ACCES_DOCUMENT.
   PROTECTIONS : pas de suppression physique sur les tables metier, tables de journal
         en ajout seul, matricule immuable, termes d'un contrat verrouilles.

   IMPORTANT - IDENTIFICATION DE L'UTILISATEUR APPLICATIF
   Avec un pool de connexions, USER = compte technique. L'application doit donc,
   a chaque emprunt de connexion, executer :
         DBMS_SESSION.SET_IDENTIFIER('login_de_l_utilisateur');
   (et DBMS_SESSION.CLEAR_IDENTIFIER a la restitution). La fonction FN_USER lit cette valeur.

   PREREQUIS : executer dans le schema applicatif (ex. RH_APP), encodage UTF-8
               (NLS_LANG=FRENCH_FRANCE.AL32UTF8). Droits : CREATE TABLE, SEQUENCE,
               TRIGGER, PROCEDURE (necessaire pour creer des fonctions), INDEX.

   ORDRE D'EXECUTION (a respecter : dependances entre objets)
     00_init           parametres de session, identifiant d'installation
     01_tables         tables (securite, organisation, blocs 1 a 4, historiques, GED, journaux)
     02_index          index sur cles etrangeres et recherches frequentes
     03_fonctions      FN_USER, FN_NEXT_MATRICULE
     04_triggers       audit / sequences generes, puis regles metier et historisation
     05_commentaires   dictionnaire de donnees
     06_data           donnees de reference (roles, permissions, modeles de taches)
     07_verification   controles post-installation
   ============================================================================= */

-- 00. Initialisation
@@00_init/01_parametres_session.sql
@@00_init/02_creation_utilisateur.sql

-- 01. Tables
@@01_tables/01_securite.sql
@@01_tables/02_organisation_bloc1.sql
@@01_tables/03_bloc2_recrutement.sql
@@01_tables/04_blocs3_4_embauche_contrat.sql
@@01_tables/05_historique_relations.sql
@@01_tables/06_ged_notifications.sql
@@01_tables/07_journaux.sql

-- 02. Index
@@02_index/01_index_cles_etrangeres.sql

-- 03. Fonctions
@@03_fonctions/01_fn_user.sql
@@03_fonctions/02_fn_next_matricule.sql

-- 04. Triggers
@@04_triggers/01_generation_audit_sequences.sql
@@04_triggers/02_trigger_matricule.sql
@@04_triggers/03_trigger_contrat.sql
@@04_triggers/04_triggers_historisation.sql

-- 05. Commentaires
@@05_commentaires/01_commentaires_tables.sql

-- 06. Donnees de reference
@@06_data/01_roles.sql
@@06_data/02_permissions.sql
@@06_data/03_role_permission.sql
@@06_data/04_modele_onboarding.sql
@@06_data/05_utilisateurs_demo.sql
@@06_data/06_offres_demo.sql

-- 07. Verification
@@07_verification/01_verification.sql
