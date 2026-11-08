import http from './http'

export const documentService = {
  lister: (entite, id) => http.get('/documents', { params: { entite, id } }).then((r) => r.data),
  televerser: (fichier, { typeDocument, entiteRattachement, entiteId, dateEmission, dateExpiration, onUploadProgress } = {}) => {
    const data = new FormData()
    data.append('fichier', fichier)
    return http.post('/documents', data, {
      params: { typeDocument, entiteRattachement, entiteId, dateEmission: dateEmission || undefined, dateExpiration: dateExpiration || undefined },
      onUploadProgress,
    }).then((r) => r.data)
  },
  importerCv: (fichier, onUploadProgress) => {
    const data = new FormData()
    data.append('fichier', fichier)
    return http.post('/candidat/cv/import', data, { onUploadProgress }).then((r) => r.data)
  },
  remplacer: (id, fichier, onUploadProgress, { dateEmission, dateExpiration } = {}) => {
    const data = new FormData()
    data.append('fichier', fichier)
    return http.post(`/documents/${id}/versions`, data, {
      params: { dateEmission: dateEmission || undefined, dateExpiration: dateExpiration || undefined }, onUploadProgress,
    }).then((r) => r.data)
  },
  telecharger: (id) => http.get(`/documents/${id}/contenu`, { responseType: 'blob' }),
  archiver: (id) => http.delete(`/documents/${id}`),
  valider: (id) => http.post(`/documents/${id}/validation`).then((r) => r.data),
  refuser: (id, motif) => http.post(`/documents/${id}/refus`, { motif }).then((r) => r.data),
  changerConfidentialite: (id, niveau) => http.patch(`/documents/${id}/confidentialite`, { niveau }).then((r) => r.data),
  modifierValidite: (id, dateEmission, dateExpiration) => http.patch(`/documents/${id}/validite`, { dateEmission: dateEmission || null, dateExpiration: dateExpiration || null }).then((r) => r.data),
  obligatoires: () => http.get('/documents/obligatoires').then((r) => r.data),
}
