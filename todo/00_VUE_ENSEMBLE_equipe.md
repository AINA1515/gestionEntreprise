# 00 – Vue d'ensemble de l'équipe (à lire AVANT son TODO personnel)

**Projet :** Gestion RH · Spring Boot 4.1.1 (`com.itu.gestionRH`) · Spring Data JPA/Hibernate · Spring Security · Thymeleaf + Bootstrap 5 · **Oracle** (`ojdbc17`)
**Équipe :** 5 étudiants → P1 Fondations/Sécurité · P2 Organisation + Bloc 1 · P3 Bloc 2 · P4 Bloc 3 + Contrats · P5 Bloc 4 + Transverse
**Références :** `demarche_gestion_rh.md` (spécifications), `database/` (schéma réel), `test.md` (fonctionnalités complémentaires)

> Ce fichier fixe les règles communes. Chaque `TODO_Px_*.md` détaille ensuite le travail de chacun, étape par étape.

---

## 1. État réel du projet (constaté dans le code)

| Élément | État actuel |
|---|---|
| Entités JPA (`entity/*`) | Existent (32 classes) |
| Repositories (`repository/*`) | Existent (interfaces vides ou presque) |
| Services (`service/*`) | **Classes vides** (`@Service` sans méthode) |
| Controllers (`controller/*`) | **Classes vides** (`@Controller` sans méthode) |
| Base de données | Script complet dans `database/` : tables, séquences `SEQ_*`, triggers d'audit, matricule, données de référence (9 rôles, 21 permissions, checklist d'onboarding) |
| Pages Thymeleaf | 11 maquettes HTML dans `src/main/resources/templates` (statiques, pas encore branchées) |
| Configuration | `application.properties` Oracle, `ddl-auto=validate` (Hibernate **ne crée rien**, il vérifie seulement) |

### Conséquences pratiques
1. **Le schéma se modifie dans `database/`, jamais via Hibernate.** Toute modification de table = un script SQL daté + mise à jour de l'entité + prévenir P1 (propriétaire du schéma).
2. **Les valeurs de statut en base sont en MAJUSCULES** (contraintes `CHECK`) : `ATTENTE_RH`, `EN_ENTRETIEN`, `EN_ONBOARDING`… Les anciens TODO écrivaient `Attente_RH` : c'était faux, utiliser la forme en base.
3. **Pas de `DELETE` physique** sur les tables métier (triggers `TBD_*`, erreur `ORA-20002`) : on fait des suppressions logiques (statut ou `DATE_FIN`).
4. **Les colonnes d'audit** (`DATE_CREATION`, `CREE_PAR`, `DATE_MODIFICATION`, `MODIFIE_PAR`, `VERSION_LIGNE`) sont remplies par les triggers : ne jamais les renseigner côté Java.
5. **Architecture UI :** les controllers sont des `@Controller` (Thymeleaf, rendu serveur). L'API REST `/api/v1/...` du document de spécification est à fournir en complément avec des `@RestController` (voir §6, décision D1).

---

## 2. Décisions d'équipe à valider à la 1re réunion (30 min max, P1 anime)

| # | Question | Proposition par défaut | Décision |
|---|---|---|---|
| D1 | Rendu : Thymeleaf ou SPA + JWT ? | **Thymeleaf + session Spring Security** (`login.html` existe déjà). Les endpoints REST `/api/v1` du document sont codés en plus (`@RestController`), mêmes services. JWT = bonus uniquement | ☐ |
| D2 | Version exacte d'Oracle ? | À vérifier par P1 : `application.properties` parle d'Oracle 11g, mais Hibernate 7 (Spring Boot 4) cible des versions récentes → **risque** sur la pagination et certaines requêtes | ☐ |
| D3 | Budget d'une demande (`BUDGET_ALLOUE`) | Total pour la demande, annuel, même devise que `CONTRAT.DEVISE` | ☐ |
| D4 | Valeurs `TYPE_DEMANDE` | `CREATION` / `REMPLACEMENT` (déjà dans le `CHECK`) → résolu | ✔ |
| D5 | Rôles `IT`, `MOYENS_GENERAUX` | **Déjà créés** dans `06_data/01_roles.sql` (+ `ADMIN`) → résolu | ✔ |
| D6 | Stockage des fichiers (GED) | Système de fichiers local, dossier hors `static/`, nom = UUID | ☐ |
| D7 | CV : `CANDIDAT.CV_URL` ou `DOCUMENT` ? | Fichier dans `DOCUMENT` (type `CV`) ; `CV_URL` pointe vers le document courant | ☐ |
| D8 | Rôle de `AFFECTATION` | Historique employé ↔ poste (+ supérieur hiérarchique), lié au contrat | ☐ |

Noter les décisions ici (date + initiales) pour ne pas les rediscuter.

---

## 3. Conventions de code (tout le monde)

- **Packages :** respecter l'arborescence existante (`entity/organization`, `service/hiring`, …). Un nouveau fichier va dans le package de son domaine.
- **Nommage :** DTO = `XxxRequest` / `XxxResponse` / `XxxForm` (pour les formulaires Thymeleaf), dans un sous-package `dto/<domaine>`. Exceptions métier dans `exception/`.
- **Enums :** P1 crée **toutes** les enums dans `com.itu.gestionRH.enums` en Phase 0 (liste §4). Les valeurs = exactement celles des `CHECK` Oracle. Utiliser `@Enumerated(EnumType.STRING)`. Chacun convertit les champs `String` de **ses** entités ; ne pas toucher aux entités des autres sans prévenir.
- **Couches :** `Controller` (HTTP, validation de forme) → `Service` (règles métier, `@Transactional`) → `Repository` (accès données). Aucune règle métier dans un controller ni dans un template.
- **Erreurs :** les services lancent `BusinessException` (règle violée, → 400/422), `NotFoundException` (→ 404), `ForbiddenException` (→ 403). `GlobalExceptionHandler` (P1) les traduit en page d'erreur (Thymeleaf) ou JSON (REST).
- **Sécurité :** chaque route protégée par `@PreAuthorize("hasAuthority('PERMISSION_CODE')")` (permissions de `06_data/02_permissions.sql`) ou `hasRole(...)`.
- **Dates :** `LocalDate` pour `DATE`, `LocalDateTime` pour `TIMESTAMP`. Jamais `java.util.Date`.
- **Logs :** SLF4J (`log.info`) sur chaque transition métier importante. Pas de `System.out`.
- **Tests :** JUnit 5 + Mockito pour les services, `@DataJpaTest`/`@SpringBootTest` si la base est nécessaire. Un test par règle de gestion et par transition interdite.

---

## 4. Référentiel commun des statuts (valeurs EXACTES en base)

| Objet | Valeurs | Transitions autorisées (rôle) |
|---|---|---|
| `Poste.statut_poste` | `CREE, VACANT, POURVU, FERME` | CREE→VACANT (RH) · VACANT→POURVU (conversion employé, P4) · POURVU→VACANT (sortie) · *→FERME |
| `DemandeRecrutement.statut_validation` | `BROUILLON, ATTENTE_RH, ATTENTE_DIRECTION, VALIDEE, REFUSEE` | BROUILLON→ATTENTE_RH (MANAGER) · ATTENTE_RH→ATTENTE_DIRECTION ou REFUSEE (CHARGE_RECRUTEMENT) · ATTENTE_DIRECTION→VALIDEE ou REFUSEE (DIRECTION). Refus = justification obligatoire |
| `DemandeValidation.etape / decision` | `RH, DIRECTION` / `VALIDEE, REFUSEE` | une ligne par décision, jamais écrasée |
| `OffreEmploi.statut_offre` | `BROUILLON, PUBLIEE, CLOTUREE` | BROUILLON→PUBLIEE→CLOTUREE (CHARGE_RECRUTEMENT) |
| `OffreEmploi.diffusion` | `INTERNE, EXTERNE, MIXTE` | |
| `Candidature.statut_candidature` | `RECUE, PRESELECTIONNEE, EN_ENTRETIEN, RETENUE, REJETEE` | RECUE→PRESELECTIONNEE/REJETEE · PRESELECTIONNEE→EN_ENTRETIEN/REJETEE · EN_ENTRETIEN→RETENUE/REJETEE (CHARGE_RECRUTEMENT) |
| `Entretien.type / statut` | `RH, TECHNIQUE, FINAL` / `PLANIFIE, REALISE, ANNULE, REPORTE` | |
| `EvaluationCandidat.decision` | `FAVORABLE, DEFAVORABLE, RESERVE` (note 0–20) | |
| `PropositionEmbauche.statut_proposition` | `BROUILLON, ENVOYEE, ACCEPTEE, REFUSEE, EXPIREE, ANNULEE` | BROUILLON→ENVOYEE/ANNULEE · ENVOYEE→ACCEPTEE/REFUSEE (CANDIDAT) ou EXPIREE (système) ou ANNULEE |
| `Employe.statut_activite` | `EN_ONBOARDING, ACTIF, SUSPENDU, SORTI` | EN_ONBOARDING→ACTIF (auto quand onboarding `COMPLETE`) |
| `Contrat.type_contrat / type_acte` | `CDD, CDI, STAGE, ALTERNANCE` / `INITIAL, AVENANT, RENOUVELLEMENT` | |
| `Contrat.statut_contrat` | `EN_ATTENTE, EN_COURS, RENOUVELE, TERMINE` | EN_ATTENTE→EN_COURS · EN_COURS→RENOUVELE ou TERMINE |
| `Onboarding.statut_global` | `NON_DEBUTE, EN_COURS, COMPLETE` | |
| `OnboardingTache.statut_tache` | `A_FAIRE, EN_COURS, VALIDEE` | |
| Services responsables | `RH, IT, MANAGER, MOYENS_GENERAUX, SALARIE` | |
| `Document.type_document` | `CV, LETTRE_MOTIVATION, CONTRAT_SIGNE, RIB, PIECE_IDENTITE, CARTE_VITALE, REGLEMENT_SIGNE, FICHE_POSTE, PROPOSITION, AUTRE` | |
| `Document.confidentialite / statut` | `INTERNE, CONFIDENTIEL, STRICT` / `ACTIF, ARCHIVE, SUPPRIME` | |
| `Notification.statut_envoi` | `EN_ATTENTE, ENVOYEE, ECHEC` | |
| `Utilisateur.type_compte / statut_compte` | `INTERNE, CANDIDAT` / `ACTIF, VERROUILLE, DESACTIVE` | |

---

## 5. Contrats d'interface entre membres (à coder en premier, même vides)

Chaque fournisseur publie **dès la Phase 0** une interface + une implémentation minimale (qui log seulement). Les autres codent contre ces signatures sans attendre.

```java
// P1 – utilisateur connecté
public interface UtilisateurCourant {
    Utilisateur get();                       // lève ForbiddenException si anonyme
    boolean aRole(String codeRole);
}

// P5 – notifications (crée une ligne NOTIFICATION + envoie l'e-mail)
public interface NotificationFacade {
    void notifier(Long destinataireUtilisateurId, String codeModele, String sujet,
                  String corps, String entiteRattachement, Long entiteId);
    void notifierRole(String codeRole, String codeModele, String sujet,
                      String corps, String entiteRattachement, Long entiteId);
}

// P5 – contrôle des transitions de statut
public interface WorkflowEngine {
    void verifierTransition(TypeWorkflow type, String statutActuel, String statutCible,
                            Set<String> rolesUtilisateur);   // lève BusinessException/ForbiddenException
}

// P5 – GED
public interface DocumentFacade {
    Document stocker(MultipartFile fichier, TypeDocument type,
                     String entiteRattachement, Long entiteId, Confidentialite conf);
    Resource telecharger(Long documentId);   // vérifie les droits + écrit LOG_ACCES_DOCUMENT
}

// P5 – appelée par P4 après le premier contrat
public interface OnboardingFacade {
    Onboarding initialiser(Long employeId);  // idempotent : 1 seul onboarding par employé
}
```

### Événements Spring (découplage entre blocs)
Publiés avec `ApplicationEventPublisher`, écoutés avec `@TransactionalEventListener(phase = AFTER_COMMIT)` (on n'agit que si la transaction source est bien validée).

| Événement | Publié par | Écouté par | Effet |
|---|---|---|---|
| `DemandeValideeEvent(demandeId)` | P2 | P3 (+ notif Chargé RH) | l'offre peut être créée |
| `CandidatureRetenueEvent(candidatureId)` | P3 | **P4** | **envoi automatique de la proposition d'embauche** (§7-F2) |
| `PropositionAccepteeEvent(propositionId)` | P4 | P4 | **contrôle de validation automatique de l'embauche** (§7-F1) |
| `EmployeCreeEvent(employeId)` | P4 | P5 | préparer l'onboarding (une fois le 1er contrat posé) |
| `OnboardingCompletEvent(employeId)` | P5 | P4 | employé → `ACTIF` |

---

## 6. Fonctionnalités complémentaires (issues de `test.md`) – qui fait quoi

| Réf. | Fonctionnalité | Responsable principal | Contributions |
|---|---|---|---|
| **F1** | Validation **automatique** d'une embauche selon des conditions | **P4** | P3 (fournit évaluations), P5 (notification + audit), P1 (paramètre de config) |
| **F2** | **Envoi automatique** d'une proposition d'embauche quand un candidat est retenu | **P4** (listener + création/envoi) | P3 (publie `CandidatureRetenueEvent`), P5 (e-mail au candidat) |
| **F3** | **Alerte** « CDD bientôt expiré » sur la page du RH **et** du manager | **P4** (calcul, tâche planifiée, page RH) | P2 (widget dans `manager-dashboard.html`), P5 (notification e-mail/cloche) |

Le détail pas à pas est dans `TODO_P4`. P2, P3 et P5 y trouvent leurs points de contribution dans leur propre fichier.

---

## 7. Organisation du travail (équipe de 5)

### Git
- Branche `main` protégée : **jamais de commit direct**. Branches `feature/p2-demandes`, `feature/p4-contrats`…
- Une Pull Request = une fonctionnalité courte (≤ 1 jour de travail). **Relue par une autre personne** (binôme de relecture ci-dessous) avant fusion.
- Mettre à jour sa branche depuis `main` (`git pull --rebase origin main`) **chaque matin**.
- Ne jamais commiter : `application.properties` avec mots de passe réels, dossier `target/`, fichiers uploadés.
- Binômes de relecture : P1↔P5 · P2↔P3 · P4↔P1 (sécurité) · P5↔P4 · P3↔P2.

### Rituels (15 min/jour suffisent)
- **Point quotidien** : hier / aujourd'hui / bloqué par qui ?
- **Réunion de cadrage (J1)** : décisions §2 + répartition des maquettes.
- **Réunion d'intégration (milieu de projet)** : enchaîner à la main le flux complet Manager → … → Onboarding.
- **Répétition de la démonstration** (veille de la recette).

### Propriété des maquettes HTML existantes

| Template | Propriétaire | Remarque |
|---|---|---|
| `security/login.html` | P1 | formulaire de connexion |
| `security/candidat-cv.html`, `candidat-cv-creer.html` | P3 | espace candidat ; upload via P5 |
| `recruitment/candidat-offres.html`, `candidat-candidature.html` | P3 | |
| `recruitment/manager-dashboard.html`, `manager-demande.html`, `manager-mes-demandes.html` | P2 | + widget alerte CDD (F3) |
| `recruitment/rh-decision.html` | P2 | décision RH/Direction sur une demande |
| `recruitment/rh-candidatures.html` | P3 | |
| `hiring/rh-confirmation.html` | P4 | confirmation de recrutement / conversion en employé |

### Ordre de livraison (dépendances)

```
Phase 0 (J1-J2)   P1 : projet qui démarre + schéma OK + interfaces vides (§5) + enums
                  P5 : NotificationFacade / WorkflowEngine / DocumentFacade en version "simple"
Phase 1 (J3-J6)   P1 : login + rôles          P2 : organisation + demandes
                  P5 : notifications réelles   P3 : modèle candidatures (avec données de test)
                  P4 : contrats + matricule (sur données de test)
Phase 2 (J7-J10)  P3 : offres / candidatures / entretiens     P4 : propositions + conversion + F1/F2
                  P5 : GED + onboarding                        P2 : écrans admin + alerte manager (F3)
Phase 3 (J11-J12) Intégration bout en bout, correction des bugs, tests
Phase 4 (J13-J14) Recette (6 scénarios), revue de sécurité, déploiement, répétition de démo
```
Si un fournisseur a du retard, les autres utilisent la **version simple** de l'interface (ne bloque jamais).

### Définition de « terminé » (Definition of Done) pour chaque tâche
1. Le code compile et `mvn test` passe.
2. Les règles de gestion sont couvertes par au moins un test (cas nominal + cas interdit).
3. L'accès est protégé par rôle/permission ; testé avec un compte non autorisé.
4. L'écran correspondant fonctionne avec des données réelles de la base (pas seulement la maquette).
5. Les erreurs sont affichées clairement à l'utilisateur (message métier, pas une stack trace).
6. PR relue par le binôme, fusionnée dans `main`.

---

## 8. Scénarios de recette de bout en bout (6 rôles)

| N° | Scénario | Acteurs |
|---|---|---|
| S1 | Demande créée → qualifiée RH → validée Direction → offre créée | Manager, Chargé RH, Direction |
| S2 | Demande refusée avec justification (RH, puis Direction) | idem |
| S3 | Offre publiée → candidature → présélection → entretien → évaluation → `RETENUE` | Candidat, Chargé RH, Manager |
| S4 | Candidature retenue → **proposition envoyée automatiquement** → acceptation → **validation auto** → employé + matricule | Candidat, système, Gestionnaire RH |
| S5 | Premier contrat CDD → onboarding généré → toutes les tâches obligatoires validées → employé `ACTIF` | Gestionnaire RH, Salarié, IT, MG, Manager |
| S6 | CDD proche de l'échéance → **alerte** visible RH et manager → renouvellement (ancien contrat `RENOUVELE`, nouveau `EN_COURS`) | Gestionnaire RH, Manager |

Prévoir un **script SQL de jeu de données de démo** (P1) : 1 utilisateur par rôle, 2 départements, 3 postes, 1 demande validée, 1 CDD qui expire dans 15 jours.
