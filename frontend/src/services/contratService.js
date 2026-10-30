import http from './http'

export const contratService = {
  creer: (payload) => http.post('/onboarding/contrats', payload).then((r) => r.data),
  obtenir: (id) => http.get(`/onboarding/contrats/${id}`).then((r) => r.data),
  expirant: (jours = 30) => http.get('/onboarding/contrats/expirant', { params: { jours } }).then((r) => r.data),
  terminer: (id, dateFin = null) => http.post(`/onboarding/contrats/${id}/terminer`, { dateFin }).then((r) => r.data),
  mesContrats: () => http.get('/employes/moi/contrats').then((r) => r.data),
}
