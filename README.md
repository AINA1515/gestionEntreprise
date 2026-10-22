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
