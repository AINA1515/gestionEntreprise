import http from './http'

export const authService = {
  login: (identifiant, motDePasse) =>
    http.post('/auth/login', { identifiant, motDePasse }).then((r) => r.data),
  me: () => http.get('/auth/me').then((r) => r.data),
  renouveler: () => http.post('/auth/refresh').then((r) => r.data),
  logout: () => http.post('/auth/logout'),
  inscriptionCandidat: (donnees) => http.post('/auth/inscription-candidat', donnees).then((r) => r.data),
  changerMotDePasse: (ancienMotDePasse, nouveauMotDePasse) =>
    http.post('/auth/changer-mot-de-passe', { ancienMotDePasse, nouveauMotDePasse }),
}
