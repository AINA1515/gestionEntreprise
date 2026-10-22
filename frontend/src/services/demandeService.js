import http from './http'

export const demandeService = {
  dashboard: () => http.get('/demandes-recrutement/synthese').then((r) => r.data),
  lister: () => http.get('/demandes-recrutement').then((r) => r.data),
  creer: (demande) => http.post('/demandes-recrutement', demande).then((r) => r.data),
  detail: (id) => http.get(`/demandes-recrutement/${id}`).then((r) => r.data),
  valider: (id, validation) => http.patch(`/demandes-recrutement/${id}/validation`, validation).then((r) => r.data),
}
