import http from './http'

export const offreService = {
  lister: () => http.get('/offres').then((r) => r.data),
  detail: (id) => http.get(`/offres/${id}`).then((r) => r.data),
}
