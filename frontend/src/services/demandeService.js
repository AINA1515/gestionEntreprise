import http from './http'

export const demandeService = {
  dashboard: () => http.get('/demandes-recrutement/synthese').then((r) => r.data),
  lister: (params = {}) => http.get('/demandes-recrutement', { params: { size: 100, ...params } }).then((r) => r.data),
  creer: (demande) => http.post('/demandes-recrutement', demande).then((r) => r.data),
  modifier: (id, demande) => http.put(`/demandes-recrutement/${id}`, demande).then((r) => r.data),
  detail: (id) => http.get(`/demandes-recrutement/${id}`).then((r) => r.data),
  soumettre: (id) => http.post(`/demandes-recrutement/${id}/soumettre`).then((r) => r.data),
  valider: (id, validation) => http.patch(`/demandes-recrutement/${id}/validation`, validation).then((r) => r.data),
  supprimer: (id) => http.delete(`/demandes-recrutement/${id}`),
}
