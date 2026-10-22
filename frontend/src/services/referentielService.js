import http from './http'

export const referentielService = {
  departements: () => http.get('/departements', { params: { size: 500 } }).then((r) => r.data.content ?? r.data),
  listerDepartements: (params = {}) => http.get('/departements', { params }).then((r) => r.data),
  departement: (id) => http.get(`/departements/${id}`).then((r) => r.data),
  creerDepartement: (data) => http.post('/departements', data).then((r) => r.data),
  modifierDepartement: (id, data) => http.put(`/departements/${id}`, data).then((r) => r.data),
  changerResponsable: (id, data) => http.patch(`/departements/${id}/responsable`, data).then((r) => r.data),
  desactiverDepartement: (id) => http.patch(`/departements/${id}/desactivation`),
  postes: (departementId) =>
    http.get('/postes', { params: { ...(departementId ? { departementId } : {}), size: 500 } }).then((r) => r.data.content ?? r.data),
  listerPostes: (params = {}) => http.get('/postes', { params }).then((r) => r.data),
  creerPoste: (data) => http.post('/postes', data).then((r) => r.data),
  modifierPoste: (id, data) => http.put(`/postes/${id}`, data).then((r) => r.data),
  fermerPoste: (id) => http.delete(`/postes/${id}`).then((r) => r.data),
}
