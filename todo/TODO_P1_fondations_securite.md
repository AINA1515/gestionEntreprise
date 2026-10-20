# TODO P1 – Fondations, Sécurité & Front-shell

**Projet :** Application Gestion RH · Spring Boot (`com.itu.gestionRH`) · JPA · API REST `/api/v1` · JWT · Front SPA
**État de départ :** entités et repositories existants ; controllers et services = classes vides à implémenter.
**Équipe :** P1 Fondations/Sécurité · P2 Organisation + Bloc 1 · P3 Bloc 2 · P4 Bloc 3 + Contrats · P5 Bloc 4 + Transverse

**Priorité : tu es bloquant pour les autres.** Livre le socle et l'authentification en premier. En attendant, les autres travaillent avec un profil `dev` sans sécurité.

## Ce que je fournis aux autres
- Projet qui compile (`pom.xml`, config, schéma SQL, données de test)
- Authentification JWT + rôles/permissions (`@PreAuthorize`)
- `GlobalExceptionHandler`, conventions DTO/erreurs/pagination
- Layout front, routing, guards, composants partagés
- Point d'écriture `LogAccesDocument` pour P5

## Ce que j'attends des autres
- P5 : composant cloche de notifications à intégrer dans le layout

## Phase 0 – Socle projet (J1-J2)
- [ ] Créer / compléter `pom.xml` (Web, JPA, Validation, Security, JWT, driver BDD, Mail, Swagger/OpenAPI, Lombok/MapStruct si souhaité)
- [ ] `application.yml` avec profils `dev` / `test` / `prod`
- [ ] Script SQL de création du schéma (tables, séquences `SEQ_*`, colonnes d'audit `DATE_CREATION`, `CREE_PAR`, `VERSION_LIGNE`…) + triggers d'audit (colonnes `insertable=false`)
- [ ] Jeu de données de départ : rôles, permissions, un utilisateur de test par rôle
- [ ] Définir les conventions : nommage des DTO, format d'erreur JSON, pagination, codes HTTP
- [ ] `GlobalExceptionHandler` (`@ControllerAdvice`)
- [ ] CORS + Swagger
- [ ] Pipeline CI minimal (build + tests)
- [ ] Animer la réunion de cadrage pour trancher les écarts (voir fin de fichier)

## Étape 2 – Sécurité & RBAC
- [ ] Rôles : `MANAGER`, `CHARGE_RECRUTEMENT`, `DIRECTION`, `GESTIONNAIRE_RH`, `CANDIDAT`, `SALARIE` (+ `IT`, `MOYENS_GENERAUX` pour l'onboarding)
- [ ] `UtilisateurService` / `UtilisateurController` : CRUD, activation, mot de passe BCrypt
- [ ] `RoleRhService`, `PermissionService`, `RolePermissionService`, `UtilisateurRoleService` + controllers
- [ ] `POST /api/v1/auth/login`, refresh token, logout
- [ ] Filtre JWT + `SecurityFilterChain` + `@PreAuthorize`
- [ ] Inscription / connexion **Candidat** (`CandidatService` / `CandidatController`)
- [ ] `LogConnexionService` : connexions réussies et échouées
- [ ] `LogAccesDocumentService` : exposer une méthode réutilisable par P5
- [ ] Service d'**audit** transverse (qui, quoi, quand)
- [ ] `GET /api/v1/me` : profil + rôles + permissions
- [ ] Tests : login OK/KO, accès refusé selon le rôle

## Front-shell (utilisé par tous)
- [ ] Initialiser la SPA (routing, store, client HTTP avec intercepteur JWT)
- [ ] Page de connexion + inscription candidat
- [ ] Layout principal + menu **dynamique selon le rôle**
- [ ] Guards de routes par rôle
- [ ] Composants partagés : tableau paginé, formulaire, badge de statut, upload de fichier, toasts d'erreur
- [ ] Écran d'administration utilisateurs / rôles

## Étape 5 – Recette & déploiement
- [ ] Revue de sécurité globale (accès par rôle, JWT, CORS, mots de passe)
- [ ] Configuration prod et déploiement
- [ ] Participer à la recette de bout en bout (6 rôles)
- [ ] Relire la sécurité du code des 4 autres

## Écarts à trancher en Phase 0
1. Les statuts sont des `String` dans les entités : définir des `enum`/constantes partagées.
2. Colonnes d'audit en `insertable=false, updatable=false` : fournir triggers ou valeurs par défaut en base.
3. Rôles `IT` et `MOYENS_GENERAUX` absents de la liste des rôles du document.
