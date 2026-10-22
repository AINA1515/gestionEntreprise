import http from './http'

export const offreService = {
  lister: () => http.get('/recrutement/offres').then((r) => r.data),
  detail: (id) => http.get(`/recrutement/offres/${id}`).then((r) => r.data),
}
