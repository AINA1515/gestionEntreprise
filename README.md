# gestionEntreprise

## API

Les routes REST sont versionnées sous `/api/v1`. La documentation OpenAPI est publiée par springdoc sur `/v3/api-docs` et l'interface Swagger UI est accessible sur `/swagger-ui.html`.

Les erreurs REST suivent le format `ApiError` (`timestamp`, `status`, `code`, `message`, `path`, `details`). Les détails de validation contiennent le nom du champ et son message. Les endpoints métier encore en cours répondent `501` avec ce même format.

Le client frontend utilise `VITE_API_URL` comme origine facultative et ajoute `/api/v1`; en développement, Vite relaie toujours `/api` vers Spring Boot.

## Vérification du contexte d'audit Oracle

Avec la base initialisée par les scripts `database/`, démarrer Oracle et l'application (avec `ddl-auto=validate`). Pour la vérification manuelle, ajouter temporairement un service de test annoté `@Transactional` qui crée une `Notification` avec `Notification.nouvelle()`, renseigne `destinataireEmail` et `sujet`, puis appelle `NotificationRepository.save()`. Avant l'appel, installer dans le `SecurityContextHolder` une authentification dont le nom est `audit-test-login`. Récupérer l'identifiant créé et exécuter :

```sql
SELECT CREE_PAR, DATE_CREATION, VERSION_LIGNE
FROM DEPARTEMENT
WHERE ID = :id_insere;
```

`CREE_PAR` doit être `audit-test-login`. Répéter l'insertion sans principal applicatif : `CREE_PAR` doit être `SYSTEME` ou `ANONYME`, selon le contexte d'exécution. Cela vérifie aussi que l'identifiant précédent a été effacé avant la réutilisation de la connexion. Le trigger Oracle incrémente `VERSION_LIGNE`; Hibernate relit cette valeur comme version générée et ne l'écrit pas.

## Moteur de workflow

Les changements d'état métier passent par les machines du package `com.itu.gestionRH.workflow`. Chaque transition vérifie l'état source, les rôles/permissions et ses conditions métier, puis publie un `WorkflowTransitionEvent`. Les services exposent les transitions dans leur transaction. Les DTO de détail incluent `actionsDisponibles`, filtrées selon l'état, l'autorisation et les conditions applicables.

Les tests unitaires des machines s'exécutent avec `mvn test`. Le test de démarrage Spring est activé avec `-Doracle.tests.enabled=true` lorsqu'une base Oracle de test est disponible.

## Authentification

L'API utilise des jetons JWT HMAC valables 30 minutes par défaut. Avant le démarrage, définir `APP_JWT_SECRET` (au moins 32 octets aléatoires), `DB_USERNAME` et `DB_PASSWORD`; `DB_URL`, `APP_JWT_ACCESS_TOKEN_MINUTES` et `APP_CORS_ALLOWED_ORIGINS` peuvent aussi être configurés. Exemple pour générer un secret : `export APP_JWT_SECRET="$(openssl rand -hex 32)"`.

Les mots de passe sont stockés avec BCrypt. Les nouveaux mots de passe font 12 à 72 caractères UTF-8, avec une minuscule, une majuscule, un chiffre et un caractère spécial. Les anciens hashes qui ne commencent pas par `$2` ne sont pas acceptés : les remplacer par des hashes BCrypt avant migration. Pour créer un compte administrateur initial, activer `BOOTSTRAP_ADMIN_ENABLED=true` et définir `BOOTSTRAP_ADMIN_LOGIN`, `BOOTSTRAP_ADMIN_EMAIL` et `BOOTSTRAP_ADMIN_PASSWORD` (12 caractères minimum). Au démarrage, le compte n'est créé que si aucun utilisateur n'a déjà le rôle `ADMIN`.
