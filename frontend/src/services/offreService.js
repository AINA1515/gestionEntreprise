import http from './http'

export const offreService = {
  lister: (params = {}) => http.get('/recrutement/offres', { params }).then((r) => r.data),
  detail: (id) => http.get(`/recrutement/offres/${id}`).then((r) => r.data),
  creer: (offre) => http.post('/recrutement/offres', offre).then((r) => r.data),
  modifier: (id, offre) => http.put(`/recrutement/offres/${id}`, offre).then((r) => r.data),
  publier: (id) => http.post(`/recrutement/offres/${id}/publier`).then((r) => r.data),
  cloturer: (id) => http.post(`/recrutement/offres/${id}/cloturer`).then((r) => r.data),
}
