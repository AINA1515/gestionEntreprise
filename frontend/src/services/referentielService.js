import http from './http'

export const referentielService = {
  departements: () => http.get('/departements').then((r) => r.data),
  postes: (departementId) =>
    http.get('/postes', { params: departementId ? { departementId } : {} }).then((r) => r.data),
}
