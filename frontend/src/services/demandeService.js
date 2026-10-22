import http from './http'

export const demandeService = {
  dashboard: () => http.get('/manager/dashboard').then((r) => r.data),
  lister: () => http.get('/manager/demandes').then((r) => r.data),
  creer: (demande) => http.post('/manager/demandes', demande).then((r) => r.data),
}
