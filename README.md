# gestionEntreprise

## API

Les routes REST sont versionnées sous `/api/v1`. La documentation OpenAPI est publiée par springdoc sur `/v3/api-docs` et l'interface Swagger UI est accessible sur `/swagger-ui.html`.

Les erreurs REST suivent le format `ApiError` (`timestamp`, `status`, `code`, `message`, `path`, `details`). Les détails de validation contiennent le nom du champ et son message. Les endpoints métier encore en cours répondent `501` avec ce même format.

Le client frontend utilise `VITE_API_URL` comme origine facultative et ajoute `/api/v1`; en développement, Vite relaie toujours `/api` vers Spring Boot.
