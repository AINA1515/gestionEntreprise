import http from './http'

export const employeService = {
  lister: (params = {}) => http.get('/employes', { params }).then((r) => r.data),
  obtenir: (id) => http.get(`/employes/${id}`).then((r) => r.data),
  moi: () => http.get('/employes/moi').then((r) => r.data),
  collaborateurs: (params = {}) => http.get('/manager/collaborateurs', { params }).then((r) => r.data),
}
