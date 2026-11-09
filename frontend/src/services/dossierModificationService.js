import http from './http'
export const dossierModificationService = {
  creer: (payload) => http.post('/dossier-modifications', payload).then((r) => r.data),
  mesDemandes: () => http.get('/dossier-modifications/moi').then((r) => r.data),
  aValider: () => http.get('/dossier-modifications/a-valider').then((r) => r.data),
  decider: (id, decision) => http.post(`/dossier-modifications/${id}/decision`, { decision }).then((r) => r.data),
}
