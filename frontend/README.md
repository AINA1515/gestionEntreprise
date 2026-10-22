# Frontend Vue 3 – Gestion RH

Vue 3 + Vite + Pinia + Vue Router + Axios + Bootstrap 5.

## Lancer
```bash
npm install
cp .env.example .env     # optionnel
npm run dev              # http://localhost:5173  (proxy /api -> http://localhost:8080)
npm run build            # production -> dist/
```
Démarrer aussi l'API Spring Boot (port 8080).

## Organisation (`src/`)
| Dossier | Rôle |
|---|---|
| `views/` | Une page par route, rangée par espace : `auth/`, `manager/`, `rh/`, `candidat/` |
| `components/layout/` | Gabarits : `SidebarLayout` (+ `ManagerLayout`, `RhLayout`), `PageLayout` (en-tête + retour) |
| `components/ui/` | Composants réutilisables : `DataTable`, `StatCard`, `StatusBadge`, `FormField`, `PageHeader`, `AlertMessage` |
| `router/` | Routes (layouts en routes parentes) + garde d'authentification |
| `stores/` | Pinia : `auth`, `demandes`, `offres`, `candidatures`, `referentiel` |
| `services/` | Appels HTTP à l'API (`http.js` = instance axios + gestion d'erreurs) |
| `composables/` | Logique réutilisable : `useAsyncAction`, `useOffresFilter`, `useLabels`, `useFormatters` |

Flux : **View → Store → Service → API**.

## Authentification
La garde du routeur est activée par défaut. `VITE_AUTH_ENABLED=true` dans `.env.example` rappelle
le réglage à conserver pour les environnements locaux et de déploiement.
