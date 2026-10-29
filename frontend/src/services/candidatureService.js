import http from './http'

export const candidatureService = {
  // Côté RH
  suivi: () => http.get('/recrutement/candidatures/synthese').then((r) => r.data),
  enregistrerDecision: (decision) => http.post('/recrutement/candidatures/' + decision.candidatureId + '/evaluations', decision).then((r) => r.data),
  confirmerRecrutement: (confirmation) => http.post('/embauche/propositions', {
    ...confirmation,
    salaireBrut: confirmation.salaireBrut === '' ? null : Number(confirmation.salaireBrut),
    dateDebut: confirmation.dateDebut || null,
    dateFin: confirmation.typeContrat === 'CDI' ? null : confirmation.dateFin || null,
  }).then((r) => r.data),
  // Côté candidat
  deposer: (offreId, salairePropose = null) => http.post('/recrutement/candidatures', { offreId, salairePropose }).then((r) => r.data),
  changerStatut: (id, statut, commentaire) =>
    http.patch(`/recrutement/candidatures/${id}/statut`, { statut, commentaire }).then((r) => r.data),
  repondreProposition: (id, statut, commentaire) =>
    http.patch(`/embauche/propositions/${id}/reponse`, { statut, commentaire }).then((r) => r.data),
  candidaturesDuCandidat: () => http.get('/candidat/candidatures').then((r) => r.data),
  annulerCandidature: (id) => http.delete(`/candidat/candidatures/${id}`),
  candidaturesParOffre: (offreId) => http.get('/recrutement/candidatures', { params: { offreId } }).then((r) => r.data),
  transmettreAuManager: (offreId, candidatureIds) => http.post(`/recrutement/offres/${offreId}/transmettre-manager`, { candidatureIds }).then((r) => r.data),
  entretiens: (params = {}) => http.get('/recrutement/entretiens', { params }).then((r) => r.data),
  candidaturesAPlanifier: () => http.get('/recrutement/candidatures', { params: { statut: 'PRESELECTIONNEE' } }).then((r) => r.data),
  profilCandidat: (candidatureId) => http.get(`/recrutement/candidatures/${candidatureId}/profil`).then((r) => r.data),
  planifierEntretien: (candidatureId, entretien) => http.post(`/recrutement/candidatures/${candidatureId}/entretiens`, entretien).then((r) => r.data),
  evaluer: (candidatureId, evaluation) => http.post(`/recrutement/candidatures/${candidatureId}/evaluations`, {
    ...evaluation,
    salaireBrut: evaluation.salaireBrut === '' ? null : Number(evaluation.salaireBrut),
    dateDebut: evaluation.dateDebut || null,
    dateFin: evaluation.typeContrat === 'CDI' ? null : evaluation.dateFin || null,
  }).then((r) => r.data),
  evaluations: (candidatureId) => http.get(`/recrutement/candidatures/${candidatureId}/evaluations`).then((r) => r.data),
  mesPropositions: () => http.get('/candidat/candidatures/propositions').then((r) => r.data),
  propositions: () => http.get('/embauche/propositions').then((r) => r.data),
}
