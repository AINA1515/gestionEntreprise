import http from './http'

export const cvService = {
  consulter: () => http.get('/candidat/cv').then((r) => r.data),
  statut: () => http.get('/candidat/cv/statut').then((r) => r.data),
  niveauxFormation: () => http.get('/candidat/cv/niveaux-formation').then((r) => r.data),
  typesEmploi: () => http.get('/candidat/cv/types-emploi').then((r) => r.data),
  enregistrer: (cv) => http.put('/candidat/cv', cv).then((r) => r.data),
  importer: (fichier) => {
    const formData = new FormData()
    formData.append('fichier', fichier)
    return http.post('/candidat/cv/import', formData).then((r) => r.data)
  },
}
