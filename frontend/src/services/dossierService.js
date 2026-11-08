import http from './http'

// id absent = dossier du salarié connecté (« Mon dossier »).
const base = (id) => (id ? `/dossiers/${id}` : '/dossiers/moi')

export const dossierService = {
  obtenir: (id) => http.get(base(id)).then((r) => r.data),
  modifierRh: (id, payload) => http.put(`/dossiers/${id}`, payload).then((r) => r.data),
  modifierCoordonnees: (payload) => http.put('/dossiers/moi/coordonnees', payload).then((r) => r.data),
  carriere: (id) => http.get(`${base(id)}/carriere`).then((r) => r.data),
  remuneration: (id) => http.get(`${base(id)}/remuneration`).then((r) => r.data),
  pieces: (id) => http.get(`${base(id)}/pieces`).then((r) => r.data),
  ajouterEvolutionSalaire: (id, payload) => http.post(`/dossiers/${id}/evolutions-salaire`, payload).then((r) => r.data),
}
