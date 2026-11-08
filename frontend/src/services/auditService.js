import http from './http'

const propres = (params) => Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))

export const auditService = {
  rechercher: (params = {}) => http.get('/audit', { params: propres(params) }).then((r) => r.data),
}
