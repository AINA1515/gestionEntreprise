# Prompts Codex — Terminer l'application RH

Ce fichier contient **32 prompts à donner à Codex, dans l'ordre**, pour terminer l'application décrite dans `demarche_gestion_rh.md` (4 blocs : Besoin → Recrutement → Embauche → Contrat/Onboarding).

## Comment l'utiliser

1. Ouvrez Codex à la racine du projet (là où se trouvent `pom.xml` et `frontend/`).
2. **Collez d'abord le Prompt 0** (contexte permanent). Recollez-le au début de chaque nouvelle session.
3. Donnez ensuite **un seul prompt à la fois**, dans l'ordre. Chaque prompt est autonome et se termine par des critères d'acceptation : vérifiez-les avant de passer au suivant.
4. À la fin de chaque prompt, faites `mvn -q compile` (ou `mvn test`) et `npm run build` dans `frontend/`. Ne passez pas au prompt suivant si l'un des deux échoue.
5. Committez après chaque prompt (un commit par prompt).

## Plan

| Phase | Prompts | Contenu |
|---|---|---|
| Cadrage | P0 – P1 | Contexte permanent, alignement API `/api/v1`, conventions |
| Fondations | P2 – P7 | Domaine, workflow, JWT, RBAC, front auth, organisation |
| Services transverses | P8 – P9 | GED (documents), notifications |
| Bloc 1 – Besoin | P10 – P12 | Demandes de recrutement et validation RH → Direction |
| Bloc 2 – Recrutement | P13 – P18 | Offres, candidatures, entretiens, évaluations |
| Bloc 3 – Embauche | P19 – P21 | Propositions, conversion candidat → employé |
| Bloc 4 – Contrat/Onboarding | P22 – P25 | Contrats, checklist d'intégration, portail salarié |
| Transverse final | P26 – P27 | Audit, reporting Direction |
| Qualité | P28 – P31 | Tests, jeu de démo, déploiement, revue finale |

---

# PHASE 0 — CADRAGE

## P0 — Prompt maître (à coller en premier dans chaque session)

```text
Tu travailles sur une application de gestion des ressources humaines (RH).
Référence fonctionnelle : le fichier demarche_gestion_rh.md à la racine. Lis-le en entier avant toute action.

ARCHITECTURE (déjà en place, à respecter)
- Backend : Spring Boot 4.1.1, Java 21, Spring Data JPA, Spring Security, base ORACLE (driver ojdbc17).
  Package racine : com.itu.gestionRH, organisé par domaine : controller/, service/, repository/, entity/
  avec les sous-packages hiring, history, misc, notifications, recruitment, onboarding, organization, security, documents.
- Base de données : scripts SQL dans database/ (tables, index, fonctions, triggers, données de référence).
  Hibernate est en ddl-auto=validate : on ne crée JAMAIS le schéma par Hibernate. Si une colonne manque, on écrit un script SQL
  de migration dans database/08_migrations/ (numéroté) et on l'indique dans le README.
- Les colonnes d'audit (DATE_CREATION, CREE_PAR, DATE_MODIFICATION, MODIFIE_PAR, VERSION_LIGNE) sont gérées par des triggers :
  elles sont insertable=false / updatable=false dans les entités. Ne jamais les renseigner côté Java.
- La fonction Oracle FN_USER lit SYS_CONTEXT('USERENV','CLIENT_IDENTIFIER') : l'utilisateur applicatif doit être posé sur la connexion
  (voir Prompt 2).
- Frontend : dossier frontend/ (Vue 3 + Vite + Pinia + Vue Router + Axios + Bootstrap 5).
  Organisation OBLIGATOIRE : views/ (une page par route, rangée par espace : auth, manager, rh, candidat, ...),
  components/layout, components/ui, router/, stores/ (Pinia), services/ (appels HTTP), composables/.
  Flux : View -> Store -> Service -> API. Une vue n'appelle jamais axios directement.
  Composants existants à réutiliser : DataTable, StatCard, StatusBadge, FormField, PageHeader, AlertMessage,
  SidebarLayout/ManagerLayout/RhLayout, PageLayout ; composables useAsyncAction, useLabels, useFormatters, useOffresFilter.
- Valeurs de statuts : ce sont celles des contraintes CHECK de la base (lire database/01_tables/*.sql), en MAJUSCULES avec underscore :
  DEMANDE : BROUILLON, ATTENTE_RH, ATTENTE_DIRECTION, VALIDEE, REFUSEE
  OFFRE : BROUILLON, PUBLIEE, CLOTUREE
  CANDIDATURE : RECUE, PRESELECTIONNEE, EN_ENTRETIEN, RETENUE, REJETEE
  EVALUATION (decision) : FAVORABLE, DEFAVORABLE, RESERVE
  PROPOSITION : BROUILLON, ENVOYEE, ACCEPTEE, REFUSEE, EXPIREE, ANNULEE
  CONTRAT (type) : CDD, CDI, STAGE, ALTERNANCE
- Rôles (table ROLE_RH) : MANAGER, CHARGE_RECRUTEMENT, DIRECTION, GESTIONNAIRE_RH, CANDIDAT, SALARIE, IT, MOYENS_GENERAUX, ADMIN.
  Permissions (table PERMISSION, ex. DEMANDE_CREER, OFFRE_PUBLIER, EMPLOYE_CONVERTIR, CONTRAT_GERER, ...) : voir database/06_data/.

RÈGLES D'OR MÉTIER (à ne jamais violer)
1. POSTE != EMPLOYE : un poste peut être VACANT sans salarié.
2. CANDIDAT != EMPLOYE : la conversion crée un Employe lié au Candidat, l'historique des candidatures est conservé.
3. Les contrats ne s'écrasent jamais : chaque contrat / avenant / renouvellement = une nouvelle ligne.
4. Matricule unique au format EMP-AAAA-NNNN, généré par la fonction FN_NEXT_MATRICULE (ne pas le recalculer en Java).
5. Chaque décision de validation est historisée (DEMANDE_VALIDATION), rien n'est écrasé.

RÈGLES DE TRAVAIL
- Lis les entités, repositories, scripts SQL et fichiers existants concernés AVANT de coder. Ne réinvente pas ce qui existe.
- N'expose JAMAIS une entité JPA dans une réponse REST : utilise des DTO (records Java).
- Logique métier dans les services (@Service, @Transactional), pas dans les controllers. Les controllers restent fins.
- Validation d'entrée avec Jakarta Validation ; erreurs renvoyées au format JSON uniforme (voir Prompt 1).
- Pas de secrets en dur : tout passe par application.properties / variables d'environnement.
- Messages et libellés visibles par l'utilisateur en français.
- Après chaque tâche : compile (mvn -q compile), lance les tests existants, et pour le front lance npm run build.
- Ne modifie pas ce qui n'est pas demandé. Si une décision t'est nécessaire, choisis l'option la plus simple et note-la dans ton compte rendu final.
- Termine toujours par un compte rendu : fichiers créés/modifiés, endpoints ajoutés, hypothèses prises, points restants.

Réponds simplement « Contexte chargé » puis attends le prochain prompt.
```

---

## P1 — Audit de l'existant et alignement de l'API sur la spécification

```text
OBJECTIF
Aligner l'API existante sur la spécification (préfixe /api/v1) et poser les conventions communes du backend et du frontend.

CONTEXTE
- Des controllers REST existent déjà sous /api/... : UtilisateurController (/api/auth), OffreEmploiController (/api/offres),
  DemandeRecrutementController (/api/manager/...), CandidatureController (/api/rh/candidatures, /api/candidatures),
  EvaluationCandidatController (/api/rh/decisions), PropositionEmbaucheController (/api/rh/confirmations),
  CandidatController (/api/candidat/cv), DepartementController, PosteController. Beaucoup de méthodes d'écriture renvoient 501.
- La spécification (section 5 de demarche_gestion_rh.md) définit :
  /api/v1/demandes-recrutement, /api/v1/recrutement, /api/v1/embauche, /api/v1/onboarding.

TÂCHES BACKEND
1. Ajoute au pom.xml les dépendances manquantes : spring-boot-starter-validation, et springdoc-openapi (UI Swagger) pour documenter l'API.
2. Crée un package com.itu.gestionRH.common avec :
   - ApiError (record : timestamp, status, code, message, path, details[]) ;
   - GlobalExceptionHandler (@RestControllerAdvice) gérant : MethodArgumentNotValidException (400 avec détails par champ),
     NotFoundException (404), BusinessRuleException (409 ou 422, avec un code métier stable ex. "DEMANDE_TRANSITION_INTERDITE"),
     AccessDeniedException (403), AuthenticationException (401), DataIntegrityViolationException (409), erreur générique (500, sans fuite de stack).
   - NotFoundException, BusinessRuleException ;
   - PageResponse<T> (content, page, size, totalElements, totalPages) pour la pagination.
3. Déplace la config : le fichier src/main/java/config/SecurityConfig.java est dans un mauvais dossier ; place-le sous
   src/main/java/com/itu/gestionRH/config/ (même package com.itu.gestionRH.config).
4. Migre les routes vers la spécification, en gardant le découpage en controllers existant :
   - /api/v1/auth/** (login, me, logout)
   - /api/v1/demandes-recrutement (POST /, GET /, GET /{id}, PATCH /{id}/validation) — remplace /api/manager/demandes et /api/manager/dashboard
     (le tableau de bord manager devient GET /api/v1/demandes-recrutement/synthese)
   - /api/v1/recrutement/offres, /api/v1/recrutement/candidatures, PATCH /candidatures/{id}/statut, POST /candidatures/{id}/evaluations
   - /api/v1/embauche/propositions, PATCH /propositions/{id}/reponse, POST /candidats/{id}/convertir-employe
   - /api/v1/onboarding/contrats, GET /employes/{id}/contrats, POST /employes/{id}/initialiser-onboarding, GET /taches/mes-taches, PATCH /taches/{id}
   - Référentiels : /api/v1/departements, /api/v1/postes
   - Espace candidat : /api/v1/candidat/cv, /api/v1/candidat/candidatures
   Les méthodes non implémentées peuvent continuer à répondre 501 à ce stade, mais la signature (DTO d'entrée/sortie) doit être définie.
5. Ajoute un contrôleur de santé minimal GET /api/v1/ping (public) pour tester la liaison front/back.

TÂCHES FRONTEND
6. frontend/src/services/http.js : baseURL = (VITE_API_URL || '') + '/api/v1'. Mets à jour tous les services (authService, offreService,
   demandeService, candidatureService, cvService, referentielService) avec les nouvelles routes.
7. Dans http.js, la gestion d'erreur doit lire le nouveau format ApiError (message + details) et exposer error.code, error.details.
8. Ajoute dans composables/ un useFormErrors() qui transforme details[] en { champ: message } pour afficher les erreurs sous les champs
   (adapte FormField pour afficher une erreur via une prop `error`).
9. Mets à jour vite.config.js si nécessaire (le proxy /api reste valable).

CRITÈRES D'ACCEPTATION
- mvn compile OK ; npm run build OK.
- Swagger UI accessible sur /swagger-ui.html et liste toutes les routes /api/v1.
- Un appel avec un corps invalide renvoie 400 avec un tableau details[] ; une ressource inconnue renvoie 404 au format ApiError.
- Plus aucune route /api/... hors /api/v1 dans le code (grep).
- Le front appelle bien /api/v1/... (vérifier dans l'onglet Réseau).
```

---

# PHASE 1 — FONDATIONS ET SÉCURITÉ

## P2 — Couche domaine : entités utilisables et contexte d'audit Oracle

```text
OBJECTIF
Rendre les entités JPA modifiables proprement et brancher l'utilisateur applicatif sur la connexion Oracle (pour les triggers d'audit).

CONTEXTE
- Plusieurs entités n'ont que des getters (ex. DemandeRecrutement) : impossible de créer/modifier des lignes.
- Les triggers d'historisation utilisent FN_USER = CLIENT_IDENTIFIER (database/03_fonctions/01_fn_user.sql, database/04_triggers/).

TÂCHES
1. Parcours TOUTES les entités (entity/**). Pour chacune, ajoute ce qui manque : constructeur protégé sans argument (JPA), constructeur ou
   méthode de fabrication statique pour la création, setters ou — mieux — méthodes métier explicites (ex. DemandeRecrutement.soumettre(),
   OffreEmploi.publier()). Ne jamais mettre de setter sur : id, colonnes d'audit, versionLigne.
2. Vérifie chaque mapping contre database/01_tables/*.sql : noms de colonnes, longueurs, nullabilité, séquences SEQ_*. Corrige les écarts
   (mvn spring-boot:run avec ddl-auto=validate doit démarrer sans erreur si la base est disponible).
3. Remplace les statuts en String par des enums Java (DemandeStatut, OffreStatut, CandidatureStatut, EvaluationDecision, PropositionStatut,
   ContratType, ContratStatut, PosteStatut, EmployeStatut, OnboardingStatut, TacheStatut, ServiceResponsable, TypeDocument...)
   mappés avec @Enumerated(EnumType.STRING) et valeurs EXACTEMENT égales à celles des contraintes CHECK.
   Si les enums ne sont pas listés dans les CHECK (ex. STATUT_POSTE, STATUT_ACTIVITE, STATUT_CONTRAT), lis le SQL et reprends ses valeurs.
4. Verrouillage optimiste : si VERSION_LIGNE existe, annote-la @Version (lecture seule côté Java si un trigger l'incrémente — vérifie
   dans database/04_triggers/04_triggers_historisation.sql et choisis la bonne approche sans double incrément).
5. Contexte d'audit : crée un composant (ex. AuditContext + DataSource wrapper / Hikari connection customizer / aspect AOP sur @Transactional)
   qui exécute DBMS_SESSION.SET_IDENTIFIER(<login de l'utilisateur authentifié>) au début de chaque transaction et
   DBMS_SESSION.CLEAR_IDENTIFIER à la fin, pour que CREE_PAR / MODIFIE_PAR contiennent le bon utilisateur.
   En l'absence d'utilisateur authentifié (batch, inscription publique), utiliser "SYSTEME" ou "ANONYME".
6. Ajoute des repositories dérivés dont on aura besoin (méthodes de recherche paginées par statut, département, manager, offre, candidat...),
   sans écrire de SQL natif sauf nécessité.

CRITÈRES D'ACCEPTATION
- L'application démarre avec ddl-auto=validate sans erreur de mapping.
- Un test d'intégration (ou, à défaut, une procédure manuelle décrite dans le README) montre qu'une insertion fait apparaître le login
  applicatif dans CREE_PAR.
- Aucun champ d'audit n'est écrit depuis Java ; tous les statuts sont des enums.
```

## P3 — Moteur de workflow (machines à états)

```text
OBJECTIF
Centraliser et verrouiller toutes les transitions d'état (étape 4 de la démarche : « Moteur de workflow »).

TÂCHES
1. Crée le package com.itu.gestionRH.workflow avec un moteur générique léger (pas de dépendance externe) :
   - interface/classe StateMachine<S extends Enum<S>> définie par une table de transitions : (état source, action) -> état cible,
     avec pour chaque transition : permission ou rôle requis, condition éventuelle (prédicat), effet de bord (callback).
   - méthode transition(entité, action, contexte) qui : vérifie la transition autorisée, vérifie la permission, exécute la condition,
     applique le nouvel état, renvoie un événement de domaine ; lève BusinessRuleException (code stable) sinon.
2. Publie un événement de domaine Spring (ApplicationEventPublisher) à chaque transition : WorkflowTransitionEvent
   (type d'entité, id, état avant/après, acteur, date, commentaire). Il servira aux notifications (P9) et à l'audit (P26).
3. Définis les machines d'état suivantes (une classe de configuration par machine) :
   a) DemandeRecrutement : BROUILLON -soumettre-> ATTENTE_RH ; ATTENTE_RH -transmettre_direction-> ATTENTE_DIRECTION ;
      ATTENTE_RH -refuser-> REFUSEE (justification obligatoire) ; ATTENTE_RH -renvoyer_manager-> BROUILLON ;
      ATTENTE_DIRECTION -valider-> VALIDEE ; ATTENTE_DIRECTION -refuser-> REFUSEE (justification obligatoire).
   b) OffreEmploi : BROUILLON -publier-> PUBLIEE (seulement si la demande liée est VALIDEE) ; PUBLIEE -cloturer-> CLOTUREE.
   c) Candidature : RECUE -preselectionner-> PRESELECTIONNEE ; PRESELECTIONNEE -planifier_entretien-> EN_ENTRETIEN ;
      EN_ENTRETIEN -retenir-> RETENUE (si évaluation FAVORABLE) ; * -rejeter-> REJETEE (motif obligatoire, sauf depuis RETENUE déjà embauchée).
   d) PropositionEmbauche : BROUILLON -envoyer-> ENVOYEE ; ENVOYEE -accepter-> ACCEPTEE ; ENVOYEE -refuser-> REFUSEE (motif) ;
      ENVOYEE -expirer-> EXPIREE ; BROUILLON|ENVOYEE -annuler-> ANNULEE.
   e) Poste : statut selon la règle d'or 1 (Créé, Vacant, Pourvu, Fermé — lire les valeurs réelles dans le SQL).
   f) Onboarding / OnboardingTache : NON_DEBUTE/EN_COURS/COMPLETE et A_FAIRE/EN_COURS/VALIDEE (valeurs réelles à lire dans le SQL) ;
      le statut global de l'onboarding se déduit des tâches obligatoires.
4. Expose pour chaque entité une méthode « actionsDisponibles(entité, utilisateur) » qui renvoie la liste des actions possibles
   (le front l'utilisera pour afficher/masquer les boutons). Les DTO de détail devront inclure ce champ `actionsDisponibles`.
5. Tests unitaires : une transition valide, une transition interdite, une permission manquante, une justification manquante par machine.

CRITÈRES D'ACCEPTATION
- Aucun service métier ne modifie un statut directement : tout passe par le moteur.
- mvn test vert avec au moins 15 tests couvrant les machines ci-dessus.
```

## P4 — Authentification JWT

```text
OBJECTIF
Implémenter la connexion sécurisée (JWT) pour tous les profils (internes et candidats).

CONTEXTE
- Tables : UTILISATEUR (LOGIN, EMAIL, MOT_DE_PASSE_HASH, TYPE_COMPTE, STATUT_COMPTE, NB_ECHECS_CONNEXION, DATE_DERNIERE_CONNEXION,
  DATE_EXPIRATION_MDP, EMPLOYE_ID, CANDIDAT_ID), ROLE_RH, UTILISATEUR_ROLE, PERMISSION, ROLE_PERMISSION, LOG_CONNEXION.
  Lis database/01_tables/01_securite.sql pour les valeurs exactes (TYPE_COMPTE, STATUT_COMPTE).
- SecurityConfig actuel : tout est en permitAll + formLogin (temporaire). Il faut le remplacer.

TÂCHES BACKEND
1. SecurityConfig : API stateless, CSRF désactivé pour l'API, CORS configurable (propriété app.cors.allowed-origins),
   suppression du formLogin, filtre JWT (OncePerRequestFilter) avant UsernamePasswordAuthenticationFilter.
   Routes publiques : POST /api/v1/auth/login, POST /api/v1/auth/inscription-candidat, GET /api/v1/recrutement/offres (offres PUBLIEE),
   GET /api/v1/ping, /swagger-ui/**, /v3/api-docs/**. Tout le reste : authentifié.
2. PasswordEncoder BCrypt. Si des mots de passe existants ne sont pas en BCrypt, fournis un script/commande pour créer un compte admin
   de démarrage (ex. propriétés app.bootstrap-admin.* lues au démarrage SI aucun ADMIN n'existe).
3. JwtService (bibliothèque jjwt ou spring-security-oauth2-jose) : access token court (ex. 30 min) avec claims : sub=login, uid, roles[], perms[].
   Secret et durée dans application.properties (variables d'environnement), jamais en dur.
4. AuthService.authentifier(identifiant, motDePasse) : identifiant = login OU email ; refuse les comptes non actifs ; incrémente
   NB_ECHECS_CONNEXION à chaque échec et verrouille le compte après 5 échecs (STATUT_COMPTE correspondant) ; remet à zéro au succès ;
   met à jour DATE_DERNIERE_CONNEXION ; vérifie DATE_EXPIRATION_MDP ; écrit une ligne LOG_CONNEXION (succès/échec, IP, user-agent).
   Message d'erreur générique côté client (« Identifiants incorrects ») pour ne pas révéler l'existence du compte.
5. Endpoints : POST /auth/login -> { token, expiresAt, utilisateur{ id, login, email, nomAffichage, typeCompte, roles[], permissions[] } } ;
   GET /auth/me -> même objet utilisateur ; POST /auth/logout (idempotent, 204).
6. Inscription candidat : POST /auth/inscription-candidat (nom, prénom, email, téléphone, mot de passe, consentement RGPD) crée CANDIDAT +
   UTILISATEUR (rôle CANDIDAT) dans une seule transaction ; refuse un email déjà utilisé. Renseigne CONSENTEMENT_RGPD et DATE_CONSENTEMENT.
7. Changement de mot de passe : POST /auth/changer-mot-de-passe (ancien, nouveau ; règles de complexité).

TÂCHES FRONTEND
8. LoginView : brancher sur le store auth (déjà prêt), afficher l'erreur « Identifiants incorrects » et le message de compte verrouillé.
9. Ajouter RegisterView (auth/) pour l'inscription candidat + lien depuis LoginView ; service + route /inscription.
10. Activer la garde du routeur : mettre VITE_AUTH_ENABLED=true dans .env.example (voir router/index.js) et vérifier que /login?redirect=... fonctionne.

CRITÈRES D'ACCEPTATION
- Sans token : 401 sur toute route protégée ; avec token valide : 200 ; token expiré/falsifié : 401.
- 5 mauvais mots de passe verrouillent le compte ; chaque tentative apparaît dans LOG_CONNEXION.
- Test MockMvc pour login OK, login KO, accès protégé sans token.
```

## P5 — RBAC : rôles et permissions appliqués partout

```text
OBJECTIF
Appliquer les permissions de la table PERMISSION sur chaque endpoint et gérer les comptes/rôles (administration).

TÂCHES BACKEND
1. Charge les permissions de l'utilisateur dans les authorities Spring Security (une GrantedAuthority "PERM_<CODE>" par permission,
   "ROLE_<CODE>" par rôle). Active @EnableMethodSecurity.
2. Annote CHAQUE endpoint existant et futur avec @PreAuthorize("hasAuthority('PERM_...')") en suivant la matrice de la section 2 de la démarche
   (Manager, Chargé de recrutement, Direction, Gestionnaire RH, Candidat, Salarié) et database/06_data/03_role_permission.sql.
   Exemples : POST demandes -> DEMANDE_CREER ; PATCH validation étape RH -> DEMANDE_VERIFIER_RH ; étape Direction -> DEMANDE_APPROUVER_DIR ;
   POST offres -> OFFRE_PUBLIER ; conversion en employé -> EMPLOYE_CONVERTIR ; contrats -> CONTRAT_GERER.
3. Périmètres (sécurité au niveau des données) via un composant AccessScope :
   - Manager : uniquement les demandes dont il est manager_id, les candidats des offres liées à ses demandes, ses collaborateurs directs.
   - Candidat : uniquement ses propres candidatures / propositions / CV.
   - Salarié : uniquement ses propres contrats, tâches, documents.
   - RH / Direction / Gestionnaire : selon permission (« tous »).
   Retourne 404 (pas 403) quand un utilisateur tente d'accéder à une ressource hors périmètre, pour ne pas révéler son existence.
4. Administration (rôle ADMIN, permission à ajouter si absente dans database/08_migrations/) :
   GET/POST/PATCH /api/v1/admin/utilisateurs (créer un compte interne, activer/désactiver, réinitialiser le mot de passe, lier à un employé),
   GET/PUT /api/v1/admin/utilisateurs/{id}/roles, GET /api/v1/admin/roles, GET /api/v1/admin/permissions, PUT /api/v1/admin/roles/{code}/permissions.
5. Tests : pour chaque rôle, au moins un test MockMvc vérifiant une route autorisée et une route interdite (403) ; un test de périmètre
   (un manager ne voit pas la demande d'un autre manager -> 404).

CRITÈRES D'ACCEPTATION
- Aucun endpoint métier n'est accessible sans @PreAuthorize (ajoute un test d'architecture qui échoue si un @RestController expose
  une méthode sans annotation de sécurité, hors routes publiques listées).
- La matrice rôles/permissions de la base est la source de vérité (aucun rôle codé en dur dans la logique).
```

## P6 — Frontend : authentification, rôles et navigation

```text
OBJECTIF
Faire de l'interface un SPA responsive adapté au rôle connecté (section 6 de la démarche).

CONTEXTE
frontend/src/stores/auth.js, router/index.js, components/layout/SidebarLayout.vue, PageLayout.vue existent. La navigation est pour l'instant
statique par espace (manager, rh, candidat).

TÂCHES
1. stores/auth.js : stocker utilisateur{ roles[], permissions[] } renvoyés par /auth/me ; restaurer la session au chargement
   (si un token existe -> appeler me(), sinon rester déconnecté) ; getters hasRole(code), hasPermission(code), hasAnyPermission([..]).
2. composables/usePermissions.js (can(permission), hasRole(role)) et une directive v-can="'PERMISSION_CODE'" (plugins/permissions.js)
   pour masquer les éléments d'interface non autorisés.
3. router : meta.permissions / meta.roles sur chaque route ; garde globale qui (a) redirige vers /login?redirect= si non connecté,
   (b) redirige vers une page 403 (views/ForbiddenView.vue) si connecté mais non autorisé,
   (c) après login redirige vers l'accueil du rôle principal : MANAGER -> /manager/dashboard, CHARGE_RECRUTEMENT et GESTIONNAIRE_RH -> /rh/...,
   DIRECTION -> /direction/dashboard, CANDIDAT -> /candidat/cv, SALARIE -> /salarie/accueil, IT/MOYENS_GENERAUX -> /taches,
   ADMIN -> /admin/utilisateurs. Corrige authStore.homeRoute (il utilisait des codes de rôle obsolètes).
4. Menus dynamiques : SidebarLayout reçoit les items filtrés par permission. Crée un layout générique AppLayout (sidebar + topbar avec nom
   d'utilisateur, rôle, cloche de notifications (placeholder pour P9), menu profil/déconnexion/changement de mot de passe) et fais
   migrer ManagerLayout et RhLayout dessus (ou rends-les configurables). Responsive : sidebar repliable en mobile (offcanvas Bootstrap).
5. Intercepteur axios : 401 -> déconnexion + redirection login avec message « Session expirée » ; 403 -> toast d'erreur ;
   ajoute un système de toasts global (composables/useToast.js + components/ui/ToastHost.vue) utilisé par tous les stores pour les succès/erreurs.
6. Vues : ForbiddenView.vue, ChangerMotDePasseView.vue (profil), et squelettes vides (avec titre) pour les futurs espaces : direction/, salarie/, admin/, taches/.

CRITÈRES D'ACCEPTATION
- Un candidat ne peut ni voir ni atteindre /manager/... ; un manager ne voit pas les menus RH.
- Rafraîchir la page conserve la session. Déconnexion nettoie le token et le store.
- npm run build OK ; lighthouse/DevTools : aucune erreur console au chargement.
```

## P7 — Organisation : départements, postes, employés (référentiels)

```text
OBJECTIF
Fournir les référentiels (Bloc 1 : règle d'or POSTE != EMPLOYE) nécessaires à tous les autres écrans.

CONTEXTE
Tables DEPARTEMENT, POSTE, POSTE_DEPARTEMENT, DEPARTEMENT_RESPONSABLE, DEPARTEMENT_RATTACHEMENT, EMPLOYE (database/01_tables/02_organisation_bloc1.sql
et 05_historique_relations.sql). GET /departements et GET /postes existent déjà en lecture simple.

TÂCHES BACKEND
1. DepartementService/Controller : liste paginée, détail (avec responsable actuel et historique de responsables), création, modification,
   désactivation (EST_ACTIF), changement de responsable (ligne DEPARTEMENT_RESPONSABLE, ancienne ligne clôturée, jamais écrasée).
2. PosteService/Controller : CRUD ; statut du poste géré par le moteur de workflow (VACANT tant qu'aucune affectation active ; POURVU
   dès qu'un contrat/affectation actif existe ; FERME). Filtres : département, statut, recherche texte.
3. EmployeService/Controller (lecture + recherche) : GET /api/v1/employes (filtres département, statut d'activité, texte), GET /{id} avec
   contrats actifs et affectation courante. Permission : consultation RH/Direction ; un salarié ne voit que sa fiche (GET /api/v1/employes/moi).
4. Endpoint « mes collaborateurs directs » pour un manager : GET /api/v1/manager/collaborateurs.
5. Contrôle d'intégrité : un poste ne peut pas être supprimé s'il a des demandes, offres ou contrats ; on le FERME à la place.

TÂCHES FRONTEND
6. views/admin/ (ou rh/) : DepartementsView, PostesView (liste, filtres, création/édition en modale ou page), EmployesView (liste + fiche détail).
   Services referentielService/employeService, stores correspondants. Utilise DataTable, FormField, StatusBadge (ajoute les statuts de poste
   et d'employé dans useLabels).
7. NouvelleDemandeView (manager) continue d'utiliser le référentiel département -> poste.

CRITÈRES D'ACCEPTATION
- Créer un département, un poste (VACANT par défaut), changer le responsable d'un département : l'ancien responsable reste visible dans l'historique.
- Un poste avec contrat actif apparaît POURVU.
```

---

# PHASE 2 — SERVICES TRANSVERSES

## P8 — GED : stockage sécurisé des documents

```text
OBJECTIF
Mettre en place la gestion électronique de documents (CV, pièce d'identité, RIB, contrats signés...), utilisée par les blocs 2, 3 et 4.

CONTEXTE
Tables DOCUMENT (NOM_FICHIER, TYPE_DOCUMENT, ENTITE_RATTACHEMENT, ENTITE_ID, URL_STOCKAGE_GED, TYPE_MIME, TAILLE_OCTETS, NUMERO_VERSION,
DOCUMENT_PRECEDENT_ID, CONFIDENTIALITE, STATUT_DOCUMENT) et LOG_ACCES_DOCUMENT (database/01_tables/06_ged_notifications.sql, 07_journaux.sql).
Lis les contraintes CHECK pour les valeurs autorisées (TYPE_DOCUMENT, CONFIDENTIALITE, STATUT_DOCUMENT, ENTITE_RATTACHEMENT).

TÂCHES BACKEND
1. Abstraction StockageGed (interface) avec une implémentation « système de fichiers » (répertoire configurable app.ged.root) :
   stocke sous <root>/<entité>/<id>/<uuid>, jamais avec le nom d'origine ; calcule et conserve la taille et le type MIME réel.
   (une implémentation S3/MinIO pourra être ajoutée plus tard sans toucher aux services).
2. DocumentService : téléverser (multipart), télécharger (stream), lister par entité, remplacer (nouvelle version : NUMERO_VERSION+1,
   DOCUMENT_PRECEDENT_ID renseigné, ancienne version passée à l'état archivé — jamais supprimée), archiver.
3. Sécurité des fichiers : liste blanche d'extensions/MIME selon TYPE_DOCUMENT (pdf, docx, png, jpg), taille max configurable (ex. 5 Mo),
   rejet des fichiers exécutables, nom de fichier assaini, protection contre le path traversal, antivirus optionnel (interface prévue).
4. Contrôle d'accès : un document n'est accessible que si l'utilisateur a DOCUMENT_CONSULTER_TOUS, ou DOCUMENT_CONSULTER_PERSO et en est
   propriétaire (candidat pour son CV, salarié pour ses pièces), ou si son rôle est concerné par l'entité (ex. manager pour le CV d'un
   candidat de ses offres). Respecter le niveau de CONFIDENTIALITE.
5. Chaque consultation/téléchargement écrit une ligne LOG_ACCES_DOCUMENT (utilisateur, document, action, IP, date).
6. Endpoints : POST /api/v1/documents (multipart : fichier, typeDocument, entiteRattachement, entiteId), GET /api/v1/documents?entite=&id=,
   GET /api/v1/documents/{id}/contenu, POST /api/v1/documents/{id}/versions, DELETE /api/v1/documents/{id} (archivage logique).
7. Branche le CV candidat : POST /api/v1/candidat/cv/import utilise la GED (TYPE_DOCUMENT=CV, entité CANDIDAT) et met à jour CANDIDAT.CV_URL.

TÂCHES FRONTEND
8. components/ui/FileUploader.vue (glisser-déposer, barre de progression, validation type/taille côté client, messages d'erreur),
   components/ui/DocumentList.vue (nom, type, version, date, boutons télécharger / remplacer / archiver selon permissions),
   services/documentService.js (upload avec onUploadProgress, téléchargement en blob avec le token), stores/documents.js.
9. Brancher CvImporterView sur FileUploader + documentService.

CRITÈRES D'ACCEPTATION
- Téléverser un PDF de CV, le retrouver, le télécharger ; un autre candidat reçoit 404 s'il essaie d'y accéder.
- Remplacer le fichier crée la version 2 et conserve la version 1 archivée.
- Chaque téléchargement produit une ligne LOG_ACCES_DOCUMENT. Un fichier .exe est refusé.
```

## P9 — Notifications e-mail et in-app

```text
OBJECTIF
Envoyer automatiquement les e-mails du processus et afficher les notifications dans l'application (étape 4 de la démarche).

CONTEXTE
Table NOTIFICATION (DESTINATAIRE_ID, DESTINATAIRE_EMAIL, SUJET, CORPS CLOB, CODE_MODELE, ENTITE_RATTACHEMENT, ENTITE_ID, STATUT_ENVOI,
NB_TENTATIVES, DATE_ENVOI_PREVUE, DATE_ENVOI_REEL, MESSAGE_ERREUR). Les WorkflowTransitionEvent (P3) sont publiés à chaque transition.

TÂCHES BACKEND
1. Modèle « outbox » : un NotificationService.programmer(modèle, destinataires, variables, entité) écrit une ligne NOTIFICATION
   (STATUT_ENVOI = EN_ATTENTE) dans la MÊME transaction que l'action métier.
2. Moteur de modèles : modèles HTML/texte en français dans src/main/resources/notifications/*.html (Thymeleaf ou simple substitution {{variable}}),
   un par CODE_MODELE. Prévois au minimum :
   DEMANDE_SOUMISE (-> chargés RH), DEMANDE_TRANSMISE_DIRECTION (-> Direction), DEMANDE_VALIDEE / DEMANDE_REFUSEE (-> manager),
   OFFRE_PUBLIEE, CANDIDATURE_RECUE (-> candidat), CANDIDATURE_PRESELECTIONNEE (-> manager), ENTRETIEN_PLANIFIE (-> candidat + manager),
   CANDIDATURE_REJETEE (-> candidat), PROPOSITION_ENVOYEE (-> candidat), PROPOSITION_ACCEPTEE / PROPOSITION_REFUSEE (-> RH),
   EMPLOYE_CREE (-> gestionnaire RH), ONBOARDING_TACHE_ASSIGNEE (-> assigné), ONBOARDING_TACHE_EN_RETARD, CONTRAT_FIN_PROCHE.
3. Un @EventListener (phase AFTER_COMMIT / @TransactionalEventListener) écoute WorkflowTransitionEvent et programme les bonnes notifications.
4. Envoi : spring-boot-starter-mail ; tâche planifiée (@Scheduled, activable par propriété) qui prend les NOTIFICATION EN_ATTENTE dont
   DATE_ENVOI_PREVUE est échue, envoie, passe à ENVOYEE ; en cas d'échec incrémente NB_TENTATIVES, stocke MESSAGE_ERREUR,
   re-planifie avec backoff, passe à ECHEC après 5 tentatives (valeurs exactes : lire le CHECK de STATUT_ENVOI). Un verrou évite le double envoi
   si plusieurs instances tournent. En profil dev : serveur SMTP factice (MailHog/GreenMail) ou simple log.
5. Rappels automatiques : tâches planifiées pour (a) tâches d'onboarding en retard, (b) propositions dont la date limite est dépassée
   (-> transition EXPIREE), (c) contrats CDD dont la fin approche (30 jours).
6. API in-app : GET /api/v1/notifications (les miennes, paginées, non lues d'abord), GET /api/v1/notifications/non-lues/count,
   PATCH /api/v1/notifications/{id}/lue, PATCH /api/v1/notifications/lire-tout. Si la colonne « lue » n'existe pas, ajoute-la par migration SQL.

TÂCHES FRONTEND
7. stores/notifications.js + services/notificationService.js ; composant components/layout/NotificationBell.vue dans la topbar
   (badge de compteur, menu déroulant des 10 dernières, lien « tout voir » vers NotificationsView.vue) ; rafraîchissement par polling
   toutes les 60 s (composables/usePolling.js, arrêté quand l'onglet est caché).

CRITÈRES D'ACCEPTATION
- Soumettre une demande (une fois P10 faite) crée une ligne NOTIFICATION pour chaque chargé RH ; le planificateur l'envoie ;
  la cloche du destinataire affiche +1.
- Un échec SMTP ne casse jamais l'action métier (l'envoi est asynchrone via outbox) et est rejoué.
```

---

# PHASE 3 — BLOC 1 : EXPRESSION DU BESOIN

## P10 — Demandes de recrutement : backend et circuit de validation

```text
OBJECTIF
Implémenter complètement le Bloc 1 côté API : Manager -> RH -> Direction -> Ouverture du recrutement.

CONTEXTE
Entités DemandeRecrutement et DemandeValidation ; contraintes SQL : TYPE_DEMANDE (CREATION | REMPLACEMENT, EMPLOYE_REMPLACE_ID obligatoire
si REMPLACEMENT), EFFECTIF_DEMANDE > 0, BUDGET_ALLOUE >= 0 ; DEMANDE_VALIDATION.DECISION IN (VALIDEE, REFUSEE) et JUSTIFICATION obligatoire
si REFUSEE. Le moteur de workflow (P3) porte les transitions ; les routes sont dans la section 5.1 de la démarche.

TÂCHES
1. DTO : DemandeCreateRequest (posteId, typeDemande, employeRemplaceId?, effectif, motif, budget, dateSouhaitee, soumettreDirectement),
   DemandeUpdateRequest (modifiable uniquement en BROUILLON), DemandeResponse, DemandeDetailResponse (poste, département, manager, historique
   des validations, offres liées, actionsDisponibles), ValidationRequest (action, justification).
2. Routes (/api/v1/demandes-recrutement) :
   POST / (crée en BROUILLON, ou ATTENTE_RH si soumettreDirectement) ; PUT /{id} (BROUILLON seulement, propriétaire) ;
   GET / (pagination, filtres statut, département, manager, période, texte ; périmètre selon rôle) ; GET /{id} ;
   POST /{id}/soumettre ; PATCH /{id}/validation (body : action = TRANSMETTRE_DIRECTION | VALIDER | REFUSER | RENVOYER_MANAGER, justification) ;
   DELETE /{id} (BROUILLON seulement) ; GET /synthese (compteurs par statut pour le tableau de bord du manager).
3. Règles : seul le manager propriétaire crée/modifie/soumet ; seuls les CHARGE_RECRUTEMENT agissent à l'étape ATTENTE_RH
   (DEMANDE_VERIFIER_RH) ; seule la DIRECTION agit à l'étape ATTENTE_DIRECTION (DEMANDE_APPROUVER_DIR) ; refus = justification obligatoire ;
   chaque action ajoute une ligne DEMANDE_VALIDATION (valideur, niveau RH/DIRECTION, décision, justification, date) sans jamais modifier les anciennes ;
   vérification de conformité RH : poste actif (non FERME), budget cohérent, effectif > 0.
4. Un manager ne peut pas valider sa propre demande même s'il cumule des rôles.
5. Remplace les 501 de DemandeRecrutementController par ces implémentations (le GET /demandes actuel basé sur findAll doit disparaître).
6. Notifications : WorkflowTransitionEvent -> modèles DEMANDE_* (P9).
7. Tests de service : cycle complet, refus sans justification, transition interdite, périmètre manager.

CRITÈRES D'ACCEPTATION
- Parcours complet par appels API : création -> soumission -> transmission Direction -> validation ; l'historique contient 3 lignes de validation.
- Refus Direction sans justification : 400 ; tentative d'un manager sur la demande d'un autre : 404.
```

## P11 — Bloc 1 côté Manager (Vue)

```text
OBJECTIF
Écrans du Manager : créer, suivre, corriger et soumettre ses demandes (section 2.1 de la démarche).

CONTEXTE
Vues existantes : views/manager/DashboardView.vue, MesDemandesView.vue, NouvelleDemandeView.vue ; stores/demandes.js ; services/demandeService.js.

TÂCHES FRONTEND
1. demandeService / stores/demandes.js : mettre à jour pour les routes P10 (lister paginé avec filtres, détail, créer, modifier, soumettre, supprimer, synthèse).
2. DashboardView : cartes par statut (Brouillon, En attente RH, En attente Direction, Validées, Refusées) via GET /synthese ; liste des 5 dernières demandes ;
   raccourci « Nouvelle demande ».
3. MesDemandesView : tableau paginé avec filtres (statut, période, texte), tri, StatusBadge ; clic sur une ligne -> DemandeDetailView.
4. NouvelleDemandeView : ajouter le choix CREATION / REMPLACEMENT (si remplacement : sélecteur de l'employé remplacé parmi ses collaborateurs
   via /manager/collaborateurs), boutons « Enregistrer en brouillon » et « Soumettre à la RH », affichage des erreurs par champ (useFormErrors),
   confirmation avant soumission. La même vue sert à l'édition (route /manager/demandes/:id/modifier) en BROUILLON.
5. Nouvelle vue DemandeDetailView (manager/) : informations de la demande, frise de progression du circuit (Manager -> RH -> Direction -> Ouvert),
   historique des validations avec justifications de refus, boutons conditionnés par `actionsDisponibles` (Modifier, Soumettre, Supprimer).
   Créer components/ui/StepTimeline.vue réutilisable.
6. Compléter useLabels avec tous les statuts de demande et ajouter les routes dans router/index.js avec les permissions P5.

CRITÈRES D'ACCEPTATION
- Un manager crée un brouillon, le modifie, le soumet ; la demande passe En attente RH et n'est plus modifiable.
- Une demande refusée affiche la justification de la Direction.
```

## P12 — Bloc 1 côté RH et Direction (Vue)

```text
OBJECTIF
Écrans de vérification RH et d'arbitrage Direction (section 2.2 et 2.3 de la démarche).

TÂCHES FRONTEND
1. Espace RH (views/rh/) : DemandesATraiterView (file des demandes ATTENTE_RH, filtres département/date/manager), DemandeRevueView
   (détail + checklist de conformité : poste actif, budget, effectif ; boutons « Transmettre à la Direction », « Renvoyer au manager »
   avec commentaire, « Refuser » avec justification obligatoire).
2. Espace Direction (nouveau views/direction/ + layout dédié DirectionLayout) : ArbitrageView (file ATTENTE_DIRECTION avec budget et effectif
   mis en avant, total budgétaire de la sélection), DemandeArbitrageView (détail, historique RH, boutons Valider / Refuser avec justification
   obligatoire via modale de confirmation components/ui/ConfirmModal.vue).
3. Stores/services : réutiliser demandeService (PATCH validation). Ajouter les entrées de menu et routes avec les permissions DEMANDE_VERIFIER_RH et DEMANDE_APPROUVER_DIR.
4. Après validation Direction, proposer à la RH un bouton « Créer l'offre d'emploi » qui mène au formulaire de P14 (route préparée : /rh/offres/nouvelle?demandeId=).

CRITÈRES D'ACCEPTATION
- Parcours de bout en bout dans l'interface avec trois comptes (manager, chargé RH, direction) jusqu'à l'état VALIDEE.
- Les boutons absents de `actionsDisponibles` ne s'affichent pas ; aucune action impossible n'est proposée.
```

---

# PHASE 4 — BLOC 2 : RECRUTEMENT

## P13 — Offres d'emploi : backend

```text
OBJECTIF
Transformer une demande VALIDEE en offre, la publier et la clôturer (section 5.2 de la démarche).

CONTEXTE
Entité OffreEmploi (DEMANDE_ID, TITRE, DESCRIPTION_POSTE, PROFIL_RECHERCHE, DIFFUSION, DATE_PUBLICATION, DATE_LIMITE_CANDIDATURE,
DATE_CLOTURE, STATUT_OFFRE). L'endpoint public GET /offres existe (lecture simple).

TÂCHES
1. Routes (/api/v1/recrutement/offres) : POST / (créer en BROUILLON à partir d'une demande VALIDEE, pré-remplie avec le poste ;
   refuser si la demande n'est pas VALIDEE ou si l'effectif demandé est déjà couvert par des offres ouvertes), PUT /{id} (BROUILLON),
   POST /{id}/publier, POST /{id}/cloturer, GET / (publique : seulement PUBLIEE non expirées, avec recherche texte, département ; interne : tous
   statuts avec permission OFFRE_PUBLIER), GET /{id} (détail public pour PUBLIEE ; compteurs de candidatures pour le personnel RH).
2. Clôture automatique : tâche planifiée qui clôture les offres dont DATE_LIMITE_CANDIDATURE est dépassée.
3. DTO publics distincts des DTO internes (le public ne voit ni budget, ni demande, ni manager).
4. Champs manquants pour l'écran candidat (lieu, type de contrat, temps de travail) : si absents de la table, ajouter par migration SQL
   (database/08_migrations/) et dans l'entité/DTO ; sinon les déduire du poste. Les filtres « type de contrat » de la maquette d'origine
   pourront alors être restaurés.
5. Permissions : OFFRE_PUBLIER pour CHARGE_RECRUTEMENT ; lecture publique sans authentification.
6. Tests : création refusée si demande non validée ; publication ; clôture auto.

CRITÈRES D'ACCEPTATION
- Une demande VALIDEE donne une offre BROUILLON puis PUBLIEE visible sans authentification ; une demande REFUSEE ne peut pas donner d'offre.
```

## P14 — Offres d'emploi : interfaces RH et candidat

```text
OBJECTIF
Écrans de gestion des offres (RH) et de consultation (candidat).

TÂCHES FRONTEND
1. Espace RH : OffresView (liste, filtres statut, compteur de candidatures par offre), OffreFormView (création depuis /rh/offres/nouvelle?demandeId=,
   édition en BROUILLON : titre, description, profil, diffusion, dates), boutons Publier / Clôturer avec ConfirmModal.
2. Espace candidat : OffresView (existante) à compléter : filtres département + type de contrat + recherche, pagination, état de chargement/vide ;
   nouvelle OffreDetailView (candidat/) avec description complète, date limite, bouton « Postuler » (redirige vers la connexion si non connecté,
   puis vers le formulaire de candidature). Les offres doivent aussi être consultables SANS compte : route publique /offres
   (layout PublicLayout, bouton Se connecter / S'inscrire).
3. stores/offres.js et offreService : lister avec filtres/pagination côté serveur, détail, créer, modifier, publier, clôturer.
4. Ajouter les routes (avec permissions) et entrées de menu.

CRITÈRES D'ACCEPTATION
- La RH crée une offre à partir d'une demande validée, la publie ; elle apparaît immédiatement sur la page publique ; la clôturer la fait disparaître.
```

## P15 — Candidatures : backend

```text
OBJECTIF
Réceptionner les candidatures et gérer leur cycle de vie (présélection, rejet, transmission au manager).

CONTEXTE
Entités Candidature (OFFRE_ID, CANDIDAT_ID, DATE_POSTULATION, LETTRE_MOTIVATION, RECRUTEUR_ID, STATUT_CANDIDATURE, MOTIF_REJET), Candidat,
CandidatureRecruteur (historique d'attribution des recruteurs). CandidatureRepository.findByOffreIdAndCandidatId existe.

TÂCHES
1. Dépôt : POST /api/v1/recrutement/candidatures (rôle CANDIDAT authentifié ; le candidat est celui du token, jamais fourni par le client) :
   offreId, lettre de motivation, et CV (document GED existant ou nouvelle version). Refuser : offre non PUBLIEE / expirée, doublon
   (même candidat + même offre), CV absent, consentement RGPD absent. Statut initial RECUE. Notification CANDIDATURE_RECUE.
2. Liste RH : GET /api/v1/recrutement/candidatures (pagination ; filtres offre, statut, période, texte nom/email ; tri) avec stats
   (reçues, présélectionnées, en entretien, retenues, rejetées) ; périmètre manager : uniquement candidatures présélectionnées de ses offres.
3. Détail : GET /candidatures/{id} (candidat, offre, CV via GED, lettre, entretiens, évaluations, historique de statuts, actionsDisponibles).
4. Transitions : PATCH /candidatures/{id}/statut (PRESELECTIONNER | REJETER{motif obligatoire} | PLANIFIER_ENTRETIEN | RETENIR) via le moteur de workflow.
   Attribution automatique/explicite du recruteur (ligne CANDIDATURE_RECRUTEUR à chaque changement).
5. Espace candidat : GET /api/v1/candidat/candidatures (mes candidatures, statut simplifié et compréhensible, entretien à venir, proposition reçue),
   DELETE /candidat/candidatures/{id} (retrait tant que RECUE).
6. CV candidat (formulaire) : PUT/GET /api/v1/candidat/cv enregistre les rubriques structurées (profil, formation, expérience, compétences, langues).
   Si les colonnes n'existent pas, créer la structure (migration SQL + entité CandidatProfil) ou stocker un JSON dans une colonne CLOB — choisis la
   solution la plus simple et documente-la. Ne pas exposer les données d'un candidat à un autre.
7. RGPD : endpoint d'anonymisation d'un candidat (DATE_ANONYMISATION) réservé aux RH, qui efface les données personnelles mais conserve les statistiques.
8. Tests : doublon refusé, offre clôturée refusée, périmètre candidat/manager, transitions.

CRITÈRES D'ACCEPTATION
- Un candidat ne peut pas postuler deux fois ; ne voit que ses candidatures ; la RH voit toutes les candidatures avec stats correctes
  (le endpoint /rh/candidatures renvoie désormais des chiffres exacts, plus de findAll en mémoire).
```

## P16 — Candidatures : interfaces candidat et RH

```text
OBJECTIF
Parcours candidat complet et suivi RH des candidatures.

TÂCHES FRONTEND
1. Candidat : CandidatureView (existante) -> dépôt réel : offre préchoisie via ?offreId, CV sélectionné parmi le CV créé/importé
   (liste via documentService), lettre de motivation, case RGPD ; erreurs par champ ; message de succès + redirection vers MesCandidaturesView.
2. Nouvelle MesCandidaturesView (candidat/) : liste avec StatusBadge « parlant » (Reçue, En cours d'étude, Entretien, Retenue, Non retenue), date,
   offre ; DetailView avec frise d'avancement ; bouton Retirer ; badge/alerte si une proposition d'embauche est en attente.
3. CvCreerView : brancher sur P15 (chargement du CV existant + enregistrement) ; CvAccueilView affiche l'état du CV (complet/à compléter,
   fichier importé).
4. RH : CandidaturesView (existante) à brancher sur les routes paginées : filtres (offre, statut, période, texte), tri, stats réelles,
   actions rapides (Présélectionner, Rejeter avec motif) via ConfirmModal ; clic -> CandidatureDetailView (rh/) avec CV téléchargeable
   (DocumentList), lettre, historique, boutons selon actionsDisponibles.
5. Stores : stores/candidatures.js (RH) et stores/mesCandidatures.js (candidat) avec pagination ; supprimer l'usage de store.trouver sur la liste complète.

CRITÈRES D'ACCEPTATION
- Un candidat s'inscrit, crée/importe son CV, postule, voit sa candidature « Reçue » ; la RH la présélectionne, le statut change côté candidat.
```

## P17 — Entretiens et évaluations : backend

```text
OBJECTIF
Planifier les entretiens et enregistrer l'évaluation du manager (Bloc 2, étape « Évaluation »).

CONTEXTE
Entités Entretien (CANDIDATURE_ID, INTERVENANT_ID, DATE_ENTRETIEN...), EvaluationCandidat (ENTRETIEN_ID, NOTE_TECHNIQUE, COMMENTAIRES, DECISION),
EvaluationCritere (grille de critères). Lis database/01_tables/03_bloc2_recrutement.sql pour les colonnes et CHECK exacts (type/mode d'entretien,
bornes des notes...).

TÂCHES
1. Planification (CHARGE_RECRUTEMENT, ENTRETIEN_PLANIFIER) : POST /api/v1/recrutement/candidatures/{id}/entretiens (date/heure, lieu ou lien visio,
   intervenant = manager de la demande par défaut, type). Refuse un créneau passé et les conflits d'agenda de l'intervenant.
   La candidature passe EN_ENTRETIEN (moteur de workflow). Notifications ENTRETIEN_PLANIFIE (candidat + intervenant).
   PATCH /entretiens/{id} (reporter/annuler), GET /api/v1/recrutement/entretiens (mes entretiens à réaliser pour le manager ; tous pour la RH).
2. Évaluation (EVALUATION_SAISIR, intervenant uniquement) : POST /api/v1/recrutement/candidatures/{id}/evaluations (note technique, notes par
   critère de la grille, commentaires/avis, décision FAVORABLE | DEFAVORABLE | RESERVE). Une seule évaluation par entretien ; modifiable
   tant que la candidature n'est pas RETENUE/REJETEE. Calcule la note moyenne pondérée si les critères ont des coefficients.
3. Règles : décision FAVORABLE permet à la RH de RETENIR la candidature ; DEFAVORABLE propose le rejet ; RESERVE laisse EN_ENTRETIEN.
   Un candidat ne voit jamais les évaluations internes.
4. Grille de critères par défaut configurable (table ou propriété) : compétences techniques, communication, motivation, adéquation culturelle.
5. Remplace le stub POST /api/v1/rh/decisions de EvaluationCandidatController par ces routes (la « décision RH » finale = transition RETENIR / REJETER).
6. Tests : conflit d'agenda, évaluation par un non-intervenant refusée, double évaluation refusée, enchaînement de statuts.

CRITÈRES D'ACCEPTATION
- Planifier -> évaluer FAVORABLE -> retenir : la candidature est RETENUE ; l'historique conserve toutes les étapes.
```

## P18 — Entretiens et évaluations : interfaces RH et Manager

```text
OBJECTIF
Écrans de présélection, planification et évaluation.

TÂCHES FRONTEND
1. RH : dans CandidatureDetailView, bloc « Entretiens » : formulaire de planification (date/heure, lieu/visio, intervenant) + liste des entretiens ;
   vue agenda simple EntretiensView (rh/) listant les entretiens à venir groupés par jour.
2. Manager (views/manager/) : CandidatsPreselectionnesView (candidats présélectionnés de ses offres, avec CV téléchargeable),
   MesEntretiensView (entretiens à réaliser, statut évalué / à évaluer), EvaluationView (fiche d'évaluation : notes par critère avec sliders ou
   étoiles, note technique, commentaires, décision FAVORABLE/DEFAVORABLE/RESERVE, enregistrement). Ajouter ces entrées au menu Manager.
3. Remplacer l'ancien DecisionView (views/rh/DecisionView.vue) par DecisionRecrutementView : la RH consulte l'évaluation reçue et décide
   RETENIR ou REJETER (motif obligatoire) ; si RETENIR, bouton « Préparer la proposition d'embauche » (-> P21).
4. Supprimer les sélecteurs libres « candidature » de l'ancienne maquette : on arrive toujours depuis une candidature précise (route avec :id).
5. Composants : components/ui/RatingInput.vue, components/ui/ConfirmModal.vue (si pas déjà créé), mise à jour de useLabels.

CRITÈRES D'ACCEPTATION
- Parcours complet : présélection RH -> entretien planifié -> évaluation du manager -> décision RH, avec notifications reçues par chaque acteur.
```

---

# PHASE 5 — BLOC 3 : SÉLECTION ET EMBAUCHE

## P19 — Propositions d'embauche : backend

```text
OBJECTIF
Émettre la proposition d'embauche et enregistrer la réponse du candidat (section 5.3 de la démarche).

CONTEXTE
Entité PropositionEmbauche (CANDIDATURE_ID, REDACTEUR_ID, TYPE_CONTRAT_PROPOSE, SALAIRE_BRUT_PROPOSE, DATE_DEBUT_SOUHAITEE, DATE_ENVOI,
DATE_LIMITE_REPONSE, DATE_REPONSE, MOTIF_REFUS, EMPLOYE_ID, STATUT_PROPOSITION).

TÂCHES
1. POST /api/v1/embauche/propositions (CHARGE_RECRUTEMENT, PROPOSITION_EMETTRE) : candidature RETENUE obligatoire ; une seule proposition active par
   candidature ; type de contrat (CDD/CDI/STAGE/ALTERNANCE), salaire brut >= 0, date de début, date limite de réponse (> aujourd'hui), message.
   Création en BROUILLON puis POST /propositions/{id}/envoyer (ENVOYEE, notification + lien vers l'espace candidat).
   PUT /propositions/{id} (BROUILLON), POST /{id}/annuler.
2. Réponse du candidat : PATCH /api/v1/embauche/propositions/{id}/reponse (PROPOSITION_REPONDRE, candidat concerné uniquement) :
   ACCEPTER ou REFUSER (motif obligatoire) ; impossible après la date limite ou si non ENVOYEE. Notification à la RH.
   Le candidat ne voit pas le salaire proposé des autres ; il voit uniquement sa proposition.
3. Lecture : GET /propositions (RH, filtres), GET /propositions/{id}, GET /api/v1/candidat/propositions (mes propositions).
4. Expiration automatique (tâche planifiée du Prompt 9) -> EXPIREE.
5. Génération du document de proposition (PDF) stocké en GED : service de génération avec un modèle HTML -> PDF (OpenPDF ou équivalent),
   téléchargeable par le candidat et la RH.
6. Remplace le stub /rh/confirmations de PropositionEmbaucheController par ces routes.
7. Tests : proposition sans candidature retenue refusée, double proposition refusée, réponse hors délai refusée, refus sans motif refusé.

CRITÈRES D'ACCEPTATION
- Candidature RETENUE -> proposition envoyée -> acceptée par le candidat : statut ACCEPTEE, RH notifiée, PDF disponible.
```

## P20 — Conversion candidat -> employé et matricule

```text
OBJECTIF
Implémenter la règle d'or n°2 (CANDIDAT != EMPLOYE) et la génération du matricule.

CONTEXTE
POST /api/v1/embauche/candidats/{id}/convertir-employe (permission EMPLOYE_CONVERTIR, GESTIONNAIRE_RH).
Fonction Oracle FN_NEXT_MATRICULE + trigger matricule (lis database/04_triggers/02_trigger_matricule.sql : ne duplique pas la logique, vérifie
qui génère le matricule — trigger à l'insertion ou appel explicite — et utilise une seule voie).
Employe : MATRICULE unique, CANDIDAT_ID, NOM, PRENOM, EMAIL_PROFESSIONNEL, DATE_EMBAUCHE_INITIALE, STATUT_ACTIVITE (En_Onboarding, Actif, Suspendu, Sorti).

TÂCHES
1. Préconditions : proposition ACCEPTEE, candidat sans Employe existant (relation 1 --- 0..1), poste cible non FERME.
2. Dans UNE SEULE transaction : créer Employe (statut EN_ONBOARDING, matricule généré, date d'embauche = date de début de la proposition,
   email pro généré selon une règle configurable prenom.nom@domaine ou saisi), lier PropositionEmbauche.EMPLOYE_ID, passer le poste à POURVU
   si l'effectif demandé est atteint, créer le compte UTILISATEUR SALARIE lié (mot de passe temporaire à changer au premier login) en conservant
   le compte CANDIDAT et tout l'historique des candidatures, publier l'événement EMPLOYE_CREE.
3. Idempotence et concurrence : un double clic ou deux requêtes simultanées ne créent pas deux employés (contrainte d'unicité + gestion de l'erreur).
4. Le résultat renvoie l'employé avec son matricule (ex. EMP-2026-0042) et propose les étapes suivantes (contrat, onboarding).
5. Enchaînement optionnel paramétrable (app.embauche.auto-onboarding=true) : initialiser directement l'onboarding (P23) après conversion.
6. Tests : conversion nominale, candidat déjà converti, proposition non acceptée, matricules consécutifs sur deux conversions.

CRITÈRES D'ACCEPTATION
- Deux conversions successives donnent des matricules consécutifs EMP-AAAA-NNNN ; le candidat conserve ses candidatures passées ; un second appel renvoie 409.
```

## P21 — Embauche : interfaces

```text
OBJECTIF
Écrans de proposition d'embauche (RH), de réponse (candidat) et de validation d'intégration (Gestionnaire RH).

TÂCHES FRONTEND
1. RH : remplacer ConfirmationView par PropositionFormView (rh/) accessible depuis une candidature RETENUE : type de contrat, salaire brut,
   date de début, date limite de réponse, message ; aperçu ; boutons Enregistrer brouillon / Envoyer ; PropositionsView (liste + statuts + relance).
2. Candidat : PropositionView (candidat/) lisant sa proposition (poste, contrat, salaire, date de début, date limite, document PDF),
   boutons Accepter / Refuser (motif obligatoire) avec ConfirmModal ; état final affiché ; lien dans le menu avec badge si en attente.
3. Gestionnaire RH : IntegrationsAValiderView (propositions ACCEPTEES sans employé), bouton « Valider l'intégration » -> appel de
   convertir-employe, écran de résultat montrant le matricule généré et les liens « Créer le contrat » / « Lancer l'onboarding ».
4. Services/stores : propositionService, stores/propositions.js, employeService.convertir ; mettre à jour useLabels.
5. Supprimer ConfirmationView et sa route (la maquette d'origine est remplacée) ; nettoyer le code mort lié aux anciennes routes /rh/confirmation.

CRITÈRES D'ACCEPTATION
- Parcours complet : proposition envoyée -> candidat accepte -> gestionnaire valide l'intégration -> matricule affiché.
```

---

# PHASE 6 — BLOC 4 : CONTRAT ET ONBOARDING

## P22 — Contrats : backend

```text
OBJECTIF
Gérer les contrats sans jamais écraser l'historique (règle d'or n°3).

CONTEXTE
Entité Contrat (EMPLOYE_ID, POSTE_ID, TYPE_CONTRAT CDD|CDI|STAGE|ALTERNANCE, DATE_DEBUT, DATE_FIN, SALAIRE_BRUT, STATUT_CONTRAT, ...) ;
contrainte : CDI => DATE_FIN nulle (lire CK_CTR_FIN et le trigger database/04_triggers/03_trigger_contrat.sql pour ne pas dupliquer ses règles) ;
entité Affectation (employé <-> poste).

TÂCHES
1. POST /api/v1/onboarding/contrats (CONTRAT_GERER) : contrat initial depuis la proposition acceptée (pré-rempli) ; type ACTE : INITIAL, AVENANT,
   RENOUVELLEMENT (ajouter la colonne par migration si absente). Règles : aucune période active chevauchante pour un même employé sauf avenant
   qui clôture (DATE_FIN) la ligne précédente en créant une nouvelle ligne ; DATE_FIN > DATE_DEBUT ; CDI sans date de fin ; salaire >= 0.
2. Renouvellement CDD : nouvelle ligne, ancienne -> RENOUVELE, plafond configurable de renouvellements/durée totale (propriété).
3. Cycle de statut : EN_ATTENTE -> EN_COURS (à la date de début, tâche planifiée ou à la signature) -> TERMINE / RENOUVELE.
4. GET /api/v1/onboarding/employes/{id}/contrats : historique complet trié ; GET /api/v1/onboarding/contrats/{id} ; contrat en cours ;
   salarié : uniquement ses propres contrats (GET /api/v1/employes/moi/contrats).
5. Génération du contrat PDF (modèle HTML -> PDF) stocké en GED ; téléversement du contrat SIGNÉ (TYPE_DOCUMENT = CONTRAT_SIGNE) qui passe le contrat EN_COURS.
6. Affectation : création/clôture de l'Affectation employé-poste en cohérence avec le contrat ; mise à jour du statut du poste (P7).
7. Fin de contrat / sortie : clôture du contrat, employé -> SORTI si plus aucun contrat actif, poste -> VACANT.
8. Tests : chevauchement refusé, CDI avec date de fin refusé, avenant conserve l'ancienne ligne, renouvellement CDD.

CRITÈRES D'ACCEPTATION
- Après un avenant, l'historique contient les deux lignes (ancienne datée de fin, nouvelle en cours) ; aucune ligne n'a été modifiée en écrasant les valeurs.
```

## P23 — Onboarding : backend

```text
OBJECTIF
Générer et piloter la checklist d'intégration (section 3.4 de la démarche).

CONTEXTE
Tables MODELE_ONB_TACHE (CODE, INTITULE, SERVICE_RESPONSABLE, DELAI_JOURS, EST_OBLIGATOIRE, ORDRE — 12 tâches par défaut dans
database/06_data/04_modele_onboarding.sql), ONBOARDING (EMPLOYE_ID, DATE_DEBUT, DATE_FIN_PREVUE, STATUT_GLOBAL), ONBOARDING_TACHE,
TACHE_ASSIGNATION (historique des assignations).

TÂCHES
1. POST /api/v1/onboarding/employes/{id}/initialiser-onboarding (ONBOARDING_ADMIN) : un seul onboarding par employé (1 --- 1) ;
   crée l'ONBOARDING (date de début = date d'embauche) et une ONBOARDING_TACHE par modèle actif, avec date limite = date de début + DELAI_JOURS,
   service responsable repris du modèle. Assignation automatique : MANAGER -> manager du poste/département ; SALARIE -> l'employé ;
   RH -> gestionnaire courant ou file RH ; IT / MOYENS_GENERAUX -> file du service (assignation à un utilisateur du rôle correspondant,
   modifiable). Notifications ONBOARDING_TACHE_ASSIGNEE.
2. GET /api/v1/onboarding/taches/mes-taches : tâches assignées à l'utilisateur connecté (filtres statut, en retard) ;
   GET /api/v1/onboarding/employes/{id} (vue gestionnaire : toutes les tâches, avancement global en %, retards) ;
   GET /api/v1/onboarding (liste des onboardings en cours).
3. PATCH /api/v1/onboarding/taches/{tacheId} : changer l'état (A_FAIRE -> EN_COURS -> VALIDEE) ; seul l'assigné ou la RH (ONBOARDING_ADMIN) ;
   certaines tâches exigent un justificatif GED (RIB, pièce d'identité, carte vitale -> document de type correspondant obligatoire pour passer à VALIDEE) ;
   la signature du règlement est enregistrée avec horodatage et identité du signataire.
4. Réassignation (POST /taches/{id}/assigner) avec historique TACHE_ASSIGNATION ; ajout de tâches ad hoc ; gestion des modèles
   (GET/POST/PUT /api/v1/onboarding/modeles-taches) réservée ONBOARDING_ADMIN.
5. Statut global automatique : NON_DEBUTE -> EN_COURS dès la première tâche entamée -> COMPLETE quand toutes les tâches obligatoires sont VALIDEES ;
   à COMPLETE, l'employé passe de EN_ONBOARDING à ACTIF et l'événement ONBOARDING_TERMINE est publié.
6. Tests : génération des 12 tâches avec dates limites exactes, validation sans justificatif refusée, passage à COMPLETE et à ACTIF, assignation.

CRITÈRES D'ACCEPTATION
- Après initialisation, 12 tâches existent avec les bons services ; valider les obligatoires fait basculer l'employé en ACTIF.
```

## P24 — Contrats et onboarding : interfaces du Gestionnaire RH

```text
OBJECTIF
Écrans du Gestionnaire RH pour les contrats et le suivi de l'onboarding.

TÂCHES FRONTEND
1. views/rh/ContratsEmployeView (historique en frise chronologique : initial, avenants, renouvellements ; contrat en cours mis en évidence),
   ContratFormView (création/avenant/renouvellement avec validations : dates, CDI sans date de fin, salaire), téléchargement du PDF,
   téléversement du contrat signé (FileUploader).
2. views/rh/OnboardingListView (onboardings en cours, avancement en barre de progression, retards en rouge), OnboardingDetailView
   (tâches groupées par service responsable : RH, IT, Moyens généraux, Manager, Salarié ; état, assigné, date limite ; réassigner ; ajouter une tâche ;
   valider une tâche à la place d'un service si permission), bouton « Initialiser l'onboarding » depuis la fiche employé.
3. EmployeDetailView (rh/) : onglets Informations / Contrats / Onboarding / Documents (DocumentList) / Affectations.
4. Gestion des modèles de tâches d'onboarding : ModelesTachesView (admin/ ou rh/) : liste, ordre, délai, obligatoire, activer/désactiver.
5. services : contratService, onboardingService ; stores/contrats.js, stores/onboarding.js ; composants réutilisables components/ui/ProgressBar.vue
   et components/ui/Timeline.vue.

CRITÈRES D'ACCEPTATION
- Le gestionnaire crée un contrat CDD, un avenant, voit l'historique à deux lignes, initialise l'onboarding et suit l'avancement.
```

## P25 — Mes tâches (Manager, IT, Moyens généraux) et portail Salarié

```text
OBJECTIF
Permettre à chaque acteur d'exécuter ses tâches d'accueil et au salarié de gérer son espace personnel (sections 2.1, 2.6, 2.7).

TÂCHES FRONTEND
1. views/taches/MesTachesView (partagée par MANAGER, IT, MOYENS_GENERAUX, RH) : tâches assignées à l'utilisateur connecté via
   GET /taches/mes-taches, groupées/filtrées par statut et échéance, indicateur de retard, action Démarrer/Valider avec commentaire ;
   ajouter l'entrée de menu « Mes tâches » avec badge du nombre de tâches ouvertes (menu Manager, IT, Moyens généraux).
2. Portail salarié (views/salarie/, layout SalarieLayout) :
   - AccueilSalarieView : bienvenue, avancement de l'onboarding (ProgressBar), prochaines tâches, dernières notifications.
   - MesTachesOnboardingView : uniquement les tâches « SALARIE » : téléversement du RIB, de la pièce d'identité, de la carte vitale
     (FileUploader lié à la tâche, type de document imposé), signature électronique du règlement intérieur (case de consentement + saisie du nom +
     validation horodatée affichée).
   - MesContratsView : liste de ses contrats (historique), téléchargement des PDF.
   - MesDocumentsView : sa GED personnelle (contrats, fiches de poste, pièces) via DocumentList.
   - MonProfilView : informations personnelles, matricule affiché, changement de mot de passe.
3. Au premier login d'un salarié créé par conversion : obligation de changer le mot de passe (flag côté backend à ajouter si nécessaire).
4. Manager : tâches spécifiques « Entretien d'intégration J1 » et « Désignation d'un parrain » : le formulaire de validation propose la sélection
   du parrain parmi les collaborateurs du département.
5. Routes et permissions (TACHE_TRAITER, DOCUMENT_CONSULTER_PERSO) ; services/stores : tachesService, stores/taches.js, salarieService.

CRITÈRES D'ACCEPTATION
- Un salarié nouvellement converti se connecte, change son mot de passe, téléverse RIB et pièce d'identité, signe le règlement : ses tâches passent VALIDEES
  et la RH voit l'avancement évoluer. Un technicien IT ne voit que ses tâches IT.
```

---

# PHASE 7 — TRANSVERSE FINAL

## P26 — Audit et journaux

```text
OBJECTIF
Fournir la traçabilité (conformité) : consultation de l'audit par les administrateurs.

CONTEXTE
Colonnes d'audit sur chaque table (triggers), tables LOG_CONNEXION et LOG_ACCES_DOCUMENT, événements de workflow (P3).

TÂCHES
1. Table d'audit métier (si absente, migration SQL) AUDIT_EVENT : date, acteur, action, type d'entité, id d'entité, état avant/après, adresse IP,
   commentaire. Un listener sur WorkflowTransitionEvent et sur les actions sensibles (conversion, contrat, accès GED, changement de rôle,
   réinitialisation de mot de passe) l'alimente. Aucune donnée sensible (mot de passe, token) dans les journaux.
2. API (permission AUDIT_CONSULTER, rôle ADMIN) : GET /api/v1/admin/audit (filtres acteur, entité, action, période, pagination),
   GET /api/v1/admin/journaux/connexions, GET /api/v1/admin/journaux/acces-documents, export CSV.
3. Journalisation applicative : logs structurés (corrélation par identifiant de requête MDC), niveau configurable, pas de données personnelles en clair.
4. Frontend : views/admin/AuditView.vue (filtres, tableau paginé, détail en modale), JournalConnexionsView.vue, JournalAccesDocumentsView.vue,
   service/store adminAuditService ; menu Administration visible pour AUDIT_CONSULTER.

CRITÈRES D'ACCEPTATION
- Une validation de demande, une conversion en employé et un téléchargement de CV apparaissent dans les journaux avec l'acteur et l'horodatage.
```

## P27 — Reporting / BI pour la Direction

```text
OBJECTIF
Tableaux de bord de la Direction (volumes de recrutement par département, masse salariale prévisionnelle) — section 2.3 de la démarche.

TÂCHES BACKEND
1. Endpoints agrégés (permission REPORTING_CONSULTER) sous /api/v1/reporting :
   - GET /recrutements-par-departement?de=&a= : nombre de demandes, offres, candidatures, embauches par département et par mois ;
   - GET /masse-salariale-previsionnelle?annee= : somme des salaires bruts annualisés des contrats actifs + des propositions ACCEPTEES non converties
     + budget alloué des demandes VALIDEES sans embauche, ventilée par département et par mois ;
   - GET /entonnoir-recrutement : candidatures reçues -> présélectionnées -> en entretien -> retenues -> propositions -> acceptées -> embauchées ;
   - GET /delais : délai moyen demande -> validation, publication -> première candidature, candidature -> embauche ;
   - GET /onboarding : taux de complétion, tâches en retard par service ;
   - GET /effectifs : effectifs par département et par statut d'activité.
   Requêtes agrégées côté base (JPQL/SQL avec GROUP BY), jamais de calcul en mémoire sur findAll.
2. Export CSV/Excel des principaux tableaux.

TÂCHES FRONTEND
3. Ajouter la bibliothèque de graphiques (chart.js + vue-chartjs) dans package.json.
4. views/direction/DashboardView.vue (accueil Direction) : cartes KPI (StatCard) + graphiques : histogramme recrutements par département,
   courbe de masse salariale prévisionnelle, entonnoir, délais ; filtre période/département ; états de chargement et vide ; responsive.
   Composants : components/charts/BarChart.vue, LineChart.vue, FunnelChart.vue (wrappers génériques), composable useReporting().
5. Les managers voient un dashboard limité à leur département (réutiliser les mêmes endpoints avec périmètre).

CRITÈRES D'ACCEPTATION
- Les chiffres du dashboard correspondent à un comptage manuel sur le jeu de démo (P30) ; aucun endpoint de reporting n'est accessible hors DIRECTION/ADMIN/RH autorisés.
```

---

# PHASE 8 — QUALITÉ, DÉMO, DÉPLOIEMENT

## P28 — Tests backend

```text
OBJECTIF
Filet de sécurité automatisé pour tout le backend.

TÂCHES
1. Tests unitaires de services (JUnit 5 + Mockito) : moteur de workflow, demandes, offres, candidatures, évaluations, propositions, conversion,
   contrats, onboarding, GED (validation de fichiers), authentification (verrouillage).
2. Tests de controllers (MockMvc + spring-security-test) : codes HTTP, format ApiError, validation d'entrée, matrice 401/403/404 par rôle,
   périmètres de données.
3. Tests d'intégration persistance : base de test Oracle via Testcontainers (image gvenzl/oracle-free ou xe) exécutant les scripts database/
   dans l'ordre (init, tables, index, fonctions, triggers, données) ; au minimum : matricule consécutif (FN_NEXT_MATRICULE), triggers de contrat,
   remplissage CREE_PAR via le contexte d'audit, ddl-auto=validate. Si Docker n'est pas disponible, marquer ces tests @Tag("integration")
   et les exclure du build par défaut en le documentant.
4. Test d'architecture (ArchUnit) : controllers n'accèdent pas aux repositories ; aucune entité retournée par un controller ; tout endpoint non public
   a une annotation de sécurité.
5. Couverture : JaCoCo avec seuil minimal de 70 % sur les packages service et workflow.

CRITÈRES D'ACCEPTATION
- mvn verify vert ; rapport JaCoCo généré ; les tests d'intégration Oracle passent (ou sont documentés comme exclus avec la commande pour les lancer).
```

## P29 — Tests frontend et end-to-end

```text
OBJECTIF
Tester le front Vue et les parcours utilisateurs de bout en bout (étape 5 : recette).

TÂCHES
1. Vitest + @vue/test-utils + jsdom : tests des stores (auth, demandes, candidatures, offres), des composables (useAsyncAction, usePermissions,
   useOffresFilter, useFormErrors), des composants ui (DataTable, StatusBadge, FormField, FileUploader, ConfirmModal), de la garde du routeur
   (redirection non connecté, 403 selon rôle). Mock des services axios.
2. ESLint + Prettier (config Vue 3) et scripts npm lint / format / test ; corriger les avertissements.
3. Playwright (e2e) avec un script qui démarre le backend (profil test, jeu de données P30) et le front. Scénarios obligatoires :
   a) Parcours complet « besoin -> embauche -> onboarding » avec les rôles manager, chargé RH, direction, candidat, gestionnaire RH, salarié ;
   b) Refus de la Direction avec justification visible du manager ;
   c) Un candidat ne peut pas postuler deux fois ;
   d) Contrôle d'accès : un candidat n'accède pas aux pages manager/RH ;
   e) Téléversement d'un fichier interdit rejeté ;
   f) Session expirée -> retour au login.
4. Accessibilité de base : labels associés, focus visible, contrastes, navigation clavier dans les modales (vérification axe-core dans Playwright).
5. Intégrer dans un pipeline CI (GitHub Actions ou équivalent) : build backend, tests backend, build frontend, lint, tests unitaires, e2e.

CRITÈRES D'ACCEPTATION
- npm run lint && npm run test && npm run build vert ; e2e a) à f) verts.
```

## P30 — Jeu de données de démonstration et documentation

```text
OBJECTIF
Permettre une démonstration et une recette complètes en quelques minutes.

TÂCHES
1. Script SQL database/09_demo/ idempotent (ou commande de seed Spring activable par profil « demo ») créant : 3 départements, 8 postes,
   un compte par rôle (login/mot de passe de démo documentés, BCrypt), 2 managers avec collaborateurs, des demandes à tous les statuts,
   2 offres publiées, une dizaine de candidatures à tous les statuts, une proposition acceptée, un employé converti avec onboarding en cours
   et des contrats avec historique (un avenant).
2. README.md à la racine : prérequis (Java 21, Maven, Node 20+, Oracle), création de la base (ordre exact des scripts database/),
   variables d'environnement, lancement backend/frontend, comptes de démo, URL de Swagger, commandes de test.
3. Documentation fonctionnelle courte : docs/parcours.md décrivant les parcours par rôle avec captures ou liste d'écrans et routes ;
   docs/api.md généré/complété à partir d'OpenAPI ; docs/decisions.md listant les choix techniques pris pendant les prompts (hypothèses notées dans chaque compte rendu).
4. Mise à jour de demarche_gestion_rh.md pour refléter l'implémentation réelle (Oracle au lieu de PostgreSQL/MySQL, routes finales, écarts éventuels).

CRITÈRES D'ACCEPTATION
- Sur une base vierge, suivre le README permet de lancer l'application et de se connecter avec chaque rôle de démo en moins de 15 minutes.
```

## P31 — Déploiement, sécurité et revue finale

```text
OBJECTIF
Préparer la mise en production et vérifier la conformité de bout en bout avec la démarche.

TÂCHES
1. Conteneurisation : Dockerfile backend (multi-stage, utilisateur non root), Dockerfile frontend (build Vite servi par Nginx avec fallback SPA
   et proxy /api vers le backend), docker-compose.yml (backend, frontend, Oracle, serveur SMTP de dev), healthchecks (/actuator/health).
2. Profils Spring : dev, test, demo, prod ; application-prod.properties sans valeur sensible (tout via variables d'environnement : base, JWT secret,
   SMTP, répertoire GED, origines CORS) ; Actuator limité (health, info) ; logs sans données personnelles.
3. Revue de sécurité (OWASP Top 10) avec correction : injection (requêtes paramétrées), contrôle d'accès cassé (périmètres), exposition de données
   (DTO), uploads (type/taille/nom), en-têtes de sécurité (CSP, X-Content-Type-Options, HSTS), limitation de débit sur /auth/login,
   dépendances à jour (mvn versions / npm audit), secrets absents du dépôt, mots de passe BCrypt, jetons à durée courte.
4. Performance : index SQL sur les colonnes de filtre fréquentes (vérifier database/02_index et compléter), pagination partout, N+1 (activer les logs
   de requêtes en test et corriger avec fetch join / EntityGraph), mise en cache du référentiel stable.
5. Revue de conformité fonctionnelle : produire docs/recette.md, un tableau reprenant CHAQUE exigence de demarche_gestion_rh.md
   (sections 2 à 6 : rôles, workflows des 4 blocs, règles d'or, API, architecture) avec : statut (OK / partiel / manquant), endpoint ou écran
   correspondant, test qui le couvre. Corriger tout « manquant » ou « partiel » bloquant, ou le lister explicitement dans docs/decisions.md.
6. Nettoyage : supprimer les anciens templates Thymeleaf (src/main/resources/templates), la dépendance thymeleaf et devtools si inutiles en prod,
   le code mort, les routes legacy, les TODO résolus ; vérifier qu'aucune route /api hors /api/v1 ne subsiste.

CRITÈRES D'ACCEPTATION
- docker compose up lance l'ensemble ; le parcours e2e du Prompt 29 passe contre les conteneurs ; docs/recette.md ne contient aucun « manquant » non justifié ;
  npm audit et mvn dependency-check sans vulnérabilité critique non traitée.
```

---

## Annexe — Conseils pour travailler avec Codex

- **Un prompt = un commit** (`git commit -m "P10 demandes de recrutement backend"`) : vous pourrez revenir en arrière facilement.
- Si Codex produit trop de code d'un coup, demandez-lui de **découper en deux passes** : d'abord le backend du prompt, puis le frontend.
- Si une base Oracle n'est pas disponible dans l'environnement de Codex, précisez-le : il écrira les tests de service avec des mocks et laissera les tests d'intégration marqués `@Tag("integration")`.
- À chaque prompt, demandez en dernière ligne : *« Donne-moi la liste des routes ajoutées et la commande pour tester manuellement. »*
- Les prompts P10 à P25 dépendent des prompts P2 à P9 : **ne les sautez pas**, en particulier le moteur de workflow (P3), le RBAC (P5) et la GED (P8).