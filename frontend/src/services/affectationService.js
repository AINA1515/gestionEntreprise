import http from './http'

export const affectationService = {
  muter: (payload) => http.post('/affectations/mutations', payload).then((r) => r.data),
  historique: (employeId) => http.get(`/affectations/employes/${employeId}`).then((r) => r.data),
}
