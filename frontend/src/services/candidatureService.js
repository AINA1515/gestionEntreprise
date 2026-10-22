import http from './http'

export const candidatureService = {
  // Côté RH
  suivi: () => http.get('/recrutement/candidatures/synthese').then((r) => r.data),
  enregistrerDecision: (decision) => http.post('/recrutement/candidatures/' + decision.candidatureId + '/evaluations', decision).then((r) => r.data),
  confirmerRecrutement: (confirmation) =>
    http.post('/embauche/propositions', confirmation).then((r) => r.data),
  // Côté candidat
  deposer: (candidature) => http.post('/recrutement/candidatures', candidature).then((r) => r.data),
  changerStatut: (id, statut, commentaire) =>
    http.patch(`/recrutement/candidatures/${id}/statut`, { statut, commentaire }).then((r) => r.data),
  repondreProposition: (id, statut, commentaire) =>
    http.patch(`/embauche/propositions/${id}/reponse`, { statut, commentaire }).then((r) => r.data),
  candidaturesDuCandidat: () => http.get('/candidat/candidatures').then((r) => r.data),
}
