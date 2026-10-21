# TODO P1 – Fondations, Sécurité & Front-shell

**Projet :** Gestion RH · Spring Boot 4.1.1 · JPA/Hibernate · Spring Security · Thymeleaf · Oracle
**Lire d'abord :** `00_VUE_ENSEMBLE_equipe.md` (conventions, statuts, interfaces, planning)
**Ton rôle :** tu construis le **socle** dont les 4 autres dépendent. Tant que ton socle n'est pas livré, ils travaillent avec un profil `dev` sans sécurité : **livre vite une première version, puis améliore**.

---

## 0. Ce que tu livres / ce que tu attends

| Tu fournis | À qui | Pour quand |
|---|---|---|
| Projet qui démarre (`mvn spring-boot:run`) connecté à Oracle, schéma validé | Tous | **J1** |
| Enums (`com.itu.gestionRH.enums`), exceptions, `GlobalExceptionHandler` | Tous | **J2** |
| Profil `dev` (sécurité désactivée, utilisateur fictif) | Tous | **J2** |
| Script de données de démo (1 utilisateur par rôle, etc.) | Tous | **J3** |
| Login + rôles + `UtilisateurCourant` + `@PreAuthorize` | Tous | **J6** |
| Layout Thymeleaf (menu dynamique, fragments communs) | Tous | **J6** |
| `LogAccesDocumentService.journaliser(...)` | P5 | **J5** |
| Écrans admin utilisateurs / rôles | Tous | **J10** |

| Tu attends | De qui |
|---|---|
| Composant « cloche de notifications » à insérer dans le layout | P5 |
| Page d'accueil par rôle (dashboards) | P2/P3/P4/P5 |

---

## Phase 0 – Socle projet (J1-J2)

### 0.1 Faire démarrer le projet
- [ ] Installer/vérifier : JDK 17+, Maven (`./mvnw -v`), accès à Oracle (docker `EE.oracle.docker` ou local)
- [ ] Exécuter les scripts de `database/` dans l'ordre : `00_init` → `01_tables` → `02_index` → `03_fonctions` → `04_triggers` → `05_commentaires` → `06_data` → `07_verification` (ou directement `gestion_rh.sql` s'il est le script consolidé : **vérifier** qu'il appelle bien tous les sous-scripts)
- [ ] Lancer `07_verification/01_verification.sql` : aucun objet invalide, 29 tables métier + 2 journaux présentes
- [ ] `./mvnw spring-boot:run` : l'application démarre **sans erreur Hibernate `validate`**
  - Si erreur « missing column » / « wrong column type » : l'entité ne correspond pas au script SQL → corriger l'entité (ou le script, après accord de l'équipe) et noter l'écart dans le fichier de décisions
  - Si erreur de dialecte/pagination Oracle : cf. décision D2 (version d'Oracle). Tester une requête paginée (`Pageable`) sur `RoleRhRepository` pour vérifier
- [ ] Ne **jamais** passer `ddl-auto` à `update/create` (risque de casser les triggers et séquences)

### 0.2 `pom.xml`
- [ ] Ajouter les dépendances manquantes : `spring-boot-starter-validation` (Bean Validation), `spring-boot-starter-mail` (P5), `springdoc-openapi` (Swagger pour les endpoints REST), `spring-boot-starter-actuator` (santé), Lombok (optionnel, accord de l'équipe)
- [ ] Vérifier que le build passe en CI/local : `./mvnw clean verify`
- [ ] Supprimer les dépendances inutiles/dupliquées (ex. `jakarta.persistence-api` est déjà apporté par JPA)

### 0.3 Configuration par profils
- [ ] `application.properties` (commun) + `application-dev.properties` + `application-test.properties` + `application-prod.properties`
- [ ] Sortir les secrets du dépôt : `spring.datasource.password=${DB_PASSWORD}`, `${MAIL_PASSWORD}`… et fournir `application-dev.properties.example`
- [ ] Propriétés personnalisées regroupées sous le préfixe `app.` (une classe `@ConfigurationProperties("app")`) :
  ```properties
  app.stockage.dossier=./uploads
  app.stockage.taille-max-mo=5
  app.contrat.alerte-cdd-jours=30          # F3 (P4)
  app.embauche.validation-auto=true        # F1 (P4)
  app.embauche.delai-reponse-jours=7       # F2 (P4)
  app.mail.expediteur=rh@entreprise.local
  app.onboarding.relance-jours=2           # P5
  ```
- [ ] Taille max d'upload : `spring.servlet.multipart.max-file-size=5MB`, `max-request-size=10MB`

### 0.4 Audit : renseigner l'utilisateur dans Oracle (**important, piège classique**)
Les triggers remplissent `CREE_PAR` / `MODIFIE_PAR` avec `FN_USER`, qui lit `SYS_CONTEXT('USERENV','CLIENT_IDENTIFIER')`, sinon retombe sur l'utilisateur Oracle (`entreprise`). Sans action, **toutes les lignes seraient créées par `entreprise`**.
- [ ] Créer un `@Aspect` (`AuditSessionAspect`) exécuté **à l'intérieur** de la transaction (`@Order(Ordered.LOWEST_PRECEDENCE)`) autour des méthodes `@Transactional` des services : il exécute
  `BEGIN DBMS_SESSION.SET_IDENTIFIER(:login); END;` via `EntityManager.createNativeQuery(...)`, avec le login de l'utilisateur connecté (ou `systeme` pour les tâches planifiées)
- [ ] En fin de transaction : `DBMS_SESSION.CLEAR_IDENTIFIER` (la connexion retourne dans le pool Hikari, sinon l'identifiant « fuit » vers la requête suivante)
- [ ] `connectionInitSql` de Hikari **ne convient pas** (aucun utilisateur à ce moment-là)
- [ ] Test : se connecter en `marie.rh`, créer un département, vérifier `SELECT CREE_PAR FROM DEPARTEMENT` = `marie.rh`

### 0.5 Enums, exceptions, conventions
- [ ] Créer le package `com.itu.gestionRH.enums` avec toutes les enums du tableau §4 de `00_VUE_ENSEMBLE` (valeurs = `CHECK` Oracle, en majuscules). Pour chaque enum : un champ `libelle` (affichage français) + méthode `estTerminal()` si utile
- [ ] Prévenir P2→P5 de convertir les champs `String` de leurs entités en `@Enumerated(EnumType.STRING)`
- [ ] Exceptions : `BusinessException`, `NotFoundException`, `ForbiddenException` (+ champ `code` métier)
- [ ] `GlobalExceptionHandler` (`@ControllerAdvice`) : page `templates/error/erreur.html` pour les requêtes web, JSON `{timestamp, status, code, message, details}` pour `/api/**`
  - Traduire aussi `DataIntegrityViolationException` (doublon, FK) et les erreurs Oracle de triggers `ORA-20010/20011/20002` en messages lisibles
  - Ne **jamais** afficher une stack trace à l'utilisateur
- [ ] Convention de pagination : `Page<T>` côté service, paramètres `?page=0&size=20&sort=...`
- [ ] Convention REST : base `/api/v1`, codes `200/201/204/400/401/403/404/409/422`

### 0.6 Interfaces partagées + profil `dev`
- [ ] Publier **tout de suite** les interfaces du §5 de `00_VUE_ENSEMBLE` (`UtilisateurCourant`, et créer les packages pour les autres)
- [ ] Profil `dev` : `SecurityFilterChain` qui autorise tout + `UtilisateurCourant` renvoyant un utilisateur de test paramétrable (`app.dev.login=marie.rh`) → les 4 autres peuvent coder sans attendre ton login
- [ ] `CorsConfig` + Swagger (`/swagger-ui.html`) restreint au profil `dev`

### 0.7 Jeu de données de démo (`database/08_demo/01_demo.sql`)
- [ ] 1 utilisateur par rôle (**mots de passe hachés BCrypt**, pas en clair) : `manager1`, `rh1` (CHARGE_RECRUTEMENT), `direction1`, `gestionnaire1`, `it1`, `mg1`, `salarie1`, `candidat1`, `admin`
- [ ] 2 départements, 3 postes (1 `VACANT`), 1 demande `VALIDEE`, 1 offre `PUBLIEE`
- [ ] 1 employé avec un **CDD qui expire dans ~15 jours** (pour la fonctionnalité F3 de P4)
- [ ] Script **rejouable** (début : suppression logique des données de démo ou `MERGE`) et documenté dans le README

### 0.8 CI minimale
- [ ] GitHub Actions (`.github/workflows/build.yml`) : `./mvnw -B verify` sur chaque PR (le dossier `.github` existe déjà)
- [ ] Les tests qui exigent Oracle sont marqués `@Tag("integration")` et exclus de la CI par défaut

### 0.9 Animer la réunion de cadrage (J1)
- [ ] Faire valider le tableau de décisions D1-D8 de `00_VUE_ENSEMBLE` et noter les réponses
- [ ] Créer le tableau de suivi (GitHub Projects / Trello) : une colonne par phase, une carte par case cochable des 5 TODO

---

## Étape 2 – Sécurité & RBAC (J3-J6)

### 2.1 Authentification (session + formulaire, décision D1)
- [ ] `UserDetailsService` : charger l'`Utilisateur` par `LOGIN` ; ses **rôles actifs** (`UtilisateurRole` avec `DATE_FIN` nulle ou future) ; ses **permissions** via `RolePermission`
- [ ] Mapper en `GrantedAuthority` : `ROLE_MANAGER`… **et** `DEMANDE_CREER`… (permissions) pour pouvoir utiliser `hasRole` et `hasAuthority`
- [ ] `PasswordEncoder` = `BCryptPasswordEncoder` ; jamais de mot de passe en clair en base ou dans les logs
- [ ] `SecurityFilterChain` :
  - publiques : `/login`, `/candidat/inscription`, `/css/**`, `/js/**`, liste publique des offres `/candidat/offres` (si acceptée par l'équipe)
  - `/manager/**` → MANAGER · `/rh/**` → CHARGE_RECRUTEMENT, GESTIONNAIRE_RH, DIRECTION · `/candidat/**` → CANDIDAT · `/salarie/**` → SALARIE · `/admin/**` → ADMIN
  - tout le reste authentifié
- [ ] Brancher `templates/security/login.html` (`th:action="@{/login}"`, champs `username`/`password`, message d'erreur, token CSRF automatique avec Thymeleaf)
- [ ] **Redirection après connexion selon le rôle** (`AuthenticationSuccessHandler`) : MANAGER→`/manager/dashboard`, CHARGE_RECRUTEMENT/GESTIONNAIRE_RH→`/rh/...`, DIRECTION→tableau de bord BI, CANDIDAT→`/candidat/offres`, SALARIE/IT/MG→« Mes tâches »
- [ ] **Verrouillage de compte** : après 5 échecs, `STATUT_COMPTE='VERROUILLE'` ; compteur `NB_ECHECS_CONNEXION` remis à 0 en cas de succès ; `DATE_DERNIERE_CONNEXION` mise à jour
- [ ] Déconnexion (`/logout`) avec journalisation `DECONNEXION`

### 2.2 Journaux
- [ ] `LogConnexionService` : enregistrer `SUCCES`, `ECHEC`, `VERROUILLAGE`, `DECONNEXION` (IP, user-agent, login saisi). Brancher via les événements Spring Security (`AuthenticationSuccessEvent`, `AbstractAuthenticationFailureEvent`, `LogoutSuccessHandler`)
- [ ] `LogAccesDocumentService.journaliser(Long documentId, TypeAcces type, Utilisateur u)` : valeurs du `CHECK CK_LOGD_TYPE` (voir `07_journaux.sql`). **Livrer avant J5** : P5 l'appelle dans la GED
- [ ] Les tables de journaux sont **en insertion seule** (triggers `TBX_*`) : ne jamais faire d'`update`/`delete`
- [ ] Service d'audit transverse : méthode `AuditService.tracer(action, entite, id, details)` réutilisable (utilisée par P5 pour les transitions)

### 2.3 Gestion des comptes, rôles, permissions (ADMIN)
- [ ] `UtilisateurService` : création (hash BCrypt), activation/désactivation (`STATUT_COMPTE`), réinitialisation de mot de passe, recherche paginée
- [ ] `RoleRhService`, `PermissionService`, `RolePermissionService`, `UtilisateurRoleService` : affecter/retirer un rôle **avec historique** (`DATE_DEBUT`, `DATE_FIN`) — retirer = fermer la ligne, pas la supprimer
- [ ] Règle : un utilisateur `TYPE_COMPTE='CANDIDAT'` ne peut avoir que le rôle `CANDIDAT` ; un compte `INTERNE` n'a pas le rôle `CANDIDAT`
- [ ] Règle : on ne peut pas retirer son propre rôle `ADMIN`
- [ ] Controllers `@Controller` `/admin/utilisateurs`, `/admin/roles` + `UtilisateurController` REST optionnel `/api/v1/utilisateurs`

### 2.4 Espace Candidat (inscription / connexion)
- [ ] Page `/candidat/inscription` : nom, prénom, e-mail, mot de passe, **case de consentement RGPD obligatoire** (`CONSENTEMENT_RGPD=1`, `DATE_CONSENTEMENT=today`)
- [ ] `CandidatService.inscrire(...)` en **une seule transaction** : crée `Candidat` + `Utilisateur(TYPE_COMPTE='CANDIDAT', CANDIDAT_ID)` + rôle `CANDIDAT`
- [ ] Unicité de l'e-mail (index `UX_CAND_EMAIL` insensible à la casse) → message clair « adresse déjà utilisée »
- [ ] Règle d'or : **`CANDIDAT ≠ EMPLOYÉ`** — l'inscription ne crée jamais d'`Employe`
- [ ] Prévoir l'anonymisation RGPD (`DATE_ANONYMISATION`) : bonus, méthode `anonymiser(candidatId)` appelable par ADMIN

### 2.5 Profil utilisateur
- [ ] `GET /me` (page « Mon profil ») et `GET /api/v1/me` (JSON) : login, nom, rôles, permissions, dernière connexion
- [ ] `UtilisateurCourantImpl` (implémentation réelle de l'interface publiée en 0.6) ; basculer le profil `dev` en option

### 2.6 Tests
- [ ] Login OK / mauvais mot de passe / compte verrouillé / compte désactivé
- [ ] Accès refusé (403) : un `MANAGER` essaie `/rh/**` ; un `CANDIDAT` essaie `/manager/**`
- [ ] Inscription candidat : doublon d'e-mail, consentement manquant
- [ ] L'identifiant d'audit Oracle est bien celui de l'utilisateur connecté (test d'intégration)

---

## Front-shell (J4-J8) – utilisé par tous

Constat : chaque maquette contient **sa propre copie** du menu latéral (`<aside>`) et des `<link>` Bootstrap. Il faut factoriser.

- [ ] `templates/fragments/layout.html` : `head` (Bootstrap 5.3 + Bootstrap Icons + css maison), `sidebar(role)`, `topbar` (nom de l'utilisateur, **emplacement cloche P5**, bouton déconnexion), `footer`, `toasts`
- [ ] Menu **dynamique selon le rôle** avec `sec:authorize="hasRole('MANAGER')"` (dépendance `thymeleaf-extras-springsecurity6` déjà présente) ; entrées déjà imaginées dans les maquettes : `/manager/dashboard`, `/rh/candidatures`, `/candidat/offres`, `/candidat/cv`
- [ ] Fragments réutilisables (documentés dans un `templates/fragments/README.md`) :
  - `badgeStatut(statut)` : couleur par statut (ex. `ATTENTE_RH` = jaune, `VALIDEE` = vert, `REFUSEE` = rouge)
  - `pagination(page)` : liens précédent/suivant, taille de page
  - `champFichier(name, types, tailleMax)` : upload avec aperçu du nom
  - `alerte(type, message)` : messages flash (`RedirectAttributes`)
  - `confirmation(texte)` : modale de confirmation (refus, annulation…)
- [ ] Pages d'erreur `templates/error/403.html`, `404.html`, `500.html`
- [ ] Migrer les 11 maquettes pour utiliser le layout commun (en accord avec leurs propriétaires – voir tableau §7 de `00_VUE_ENSEMBLE`) ; **ne pas casser** leur contenu
- [ ] Page d'accueil `/` qui redirige selon le rôle
- [ ] Responsive (mobile) vérifié sur le layout
- [ ] Accessibilité de base : labels de formulaires, contrastes, `aria-label` sur les boutons-icônes

---

## Étape 5 – Recette & déploiement (J13-J14)

### Revue de sécurité (checklist)
- [ ] Aucune route métier accessible sans authentification (tester en navigation privée)
- [ ] Chaque `@PostMapping`/`@PatchMapping` vérifie le **rôle ET la propriété** de la ressource (un manager ne voit que ses demandes ; un candidat que ses candidatures ; un salarié que ses documents)
- [ ] CSRF actif sur tous les formulaires ; cookies de session `HttpOnly` + `Secure` (prod)
- [ ] Aucun mot de passe/secret dans le dépôt Git ni dans les logs (`git log -p | grep -i password`)
- [ ] Les entités JPA ne sont jamais exposées directement dans le JSON (utiliser des DTO) – pas de fuite du hash de mot de passe
- [ ] Les requêtes sont paramétrées (pas de concaténation SQL/JPQL)
- [ ] Upload : type et taille contrôlés (relire le code de P5)
- [ ] Relire la sécurité du code des 4 autres (binôme de relecture) et ouvrir des tickets

### Déploiement
- [ ] Profil `prod` : `ddl-auto=validate`, `show-sql=false`, niveau de log `INFO`, secrets via variables d'environnement
- [ ] `./mvnw clean package` → un seul `.jar` ; documenter la commande de lancement
- [ ] (Bonus) `Dockerfile` + `docker-compose.yml` (application + Oracle)
- [ ] `README.md` : prérequis, installation de la base, lancement, comptes de démo, URL utiles, structure du projet
- [ ] Sauvegarde/restauration de la base documentée

### Recette
- [ ] Participer aux **6 scénarios S1–S6** (voir `00_VUE_ENSEMBLE`) en jouant chaque rôle
- [ ] Tenir la liste des anomalies (gravité, responsable, statut) jusqu'à zéro bloquant

---

## Pièges à connaître
1. **Audit à `entreprise`** si tu oublies le `SET_IDENTIFIER` (§0.4).
2. **`ddl-auto=validate`** échoue à la moindre différence de type entre entité et table.
3. `@Column(insertable=false, updatable=false)` sur les colonnes d'audit : si une entité les oublie, le trigger fonctionne quand même ; si elle les écrit, c'est écrasé.
4. Spring Security 7 : l'ancienne syntaxe `WebSecurityConfigurerAdapter` n'existe plus → déclarer un bean `SecurityFilterChain`.
5. Oracle : un `VARCHAR2` vide est traité comme `NULL` (attention aux champs obligatoires).

## Écarts / points à trancher
1. Statuts en `String` dans les entités → enums (livré en 0.5, **résolu** par la convention).
2. Colonnes d'audit gérées par triggers → **résolu** (§0.4 pour l'identifiant utilisateur).
3. Rôles `IT`, `MOYENS_GENERAUX`, `ADMIN` : **déjà présents** en base (`06_data/01_roles.sql`).
4. Version d'Oracle réelle et compatibilité Hibernate 7 : à confirmer en J1 (décision D2).
5. Faut-il autoriser la consultation des offres **sans** compte (page publique) ? À décider avec P3.
