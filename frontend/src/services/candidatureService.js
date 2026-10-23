import http from './http'

export const candidatureService = {
  // Côté RH
  suivi: () => http.get('/recrutement/candidatures/synthese').then((r) => r.data),
  enregistrerDecision: (decision) => http.post('/recrutement/candidatures/' + decision.candidatureId + '/evaluations', decision).then((r) => r.data),
  confirmerRecrutement: (confirmation) =>
    http.post('/embauche/propositions', confirmation).then((r) => r.data),
  // Côté candidat
  deposer: (offreId, salairePropose = null) => http.post('/recrutement/candidatures', { offreId, salairePropose }).then((r) => r.data),
  changerStatut: (id, statut, commentaire) =>
    http.patch(`/recrutement/candidatures/${id}/statut`, { statut, commentaire }).then((r) => r.data),
  repondreProposition: (id, statut, commentaire) =>
    http.patch(`/embauche/propositions/${id}/reponse`, { statut, commentaire }).then((r) => r.data),
  candidaturesDuCandidat: () => http.get('/candidat/candidatures').then((r) => r.data),
  annulerCandidature: (id) => http.delete(`/candidat/candidatures/${id}`),
  candidaturesParOffre: (offreId) => http.get('/recrutement/candidatures', { params: { offreId } }).then((r) => r.data),
}
