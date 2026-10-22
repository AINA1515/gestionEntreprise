import http from './http'

export const documentService = {
  lister: (entite, id) => http.get('/documents', { params: { entite, id } }).then((r) => r.data),
  televerser: (fichier, { typeDocument, entiteRattachement, entiteId, onUploadProgress } = {}) => {
    const data = new FormData()
    data.append('fichier', fichier)
    return http.post('/documents', data, { params: { typeDocument, entiteRattachement, entiteId }, onUploadProgress }).then((r) => r.data)
  },
  importerCv: (fichier, onUploadProgress) => {
    const data = new FormData()
    data.append('fichier', fichier)
    return http.post('/candidat/cv/import', data, { onUploadProgress }).then((r) => r.data)
  },
  remplacer: (id, fichier, onUploadProgress) => {
    const data = new FormData()
    data.append('fichier', fichier)
    return http.post(`/documents/${id}/versions`, data, { onUploadProgress }).then((r) => r.data)
  },
  telecharger: (id) => http.get(`/documents/${id}/contenu`, { responseType: 'blob' }),
  archiver: (id) => http.delete(`/documents/${id}`),
}
