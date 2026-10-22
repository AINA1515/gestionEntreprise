import http from './http'

export const candidatureService = {
  // Côté RH
  suivi: () => http.get('/rh/candidatures').then((r) => r.data),
  enregistrerDecision: (decision) => http.post('/rh/decisions', decision).then((r) => r.data),
  confirmerRecrutement: (confirmation) =>
    http.post('/rh/confirmations', confirmation).then((r) => r.data),
  // Côté candidat
  deposer: (candidature) => http.post('/candidatures', candidature).then((r) => r.data),
}
