import http from './http'
export const tempsTravailService={
 regimes:()=>http.get('/temps-travail/regimes').then(r=>r.data),
 plannings:(id)=>http.get(`/temps-travail/employes/${id}/plannings`).then(r=>r.data),
 creneaux:(id,debut,fin)=>http.get(`/temps-travail/employes/${id}/creneaux`,{params:{debut,fin}}).then(r=>r.data), creneauxPlanning:(id)=>http.get(`/temps-travail/plannings/${id}/creneaux`).then(r=>r.data),
 creerCreneau:(payload)=>http.post('/temps-travail/creneaux',payload).then(r=>r.data),
 supprimerCreneau:(id)=>http.delete(`/temps-travail/creneaux/${id}`),
 modifierCreneau:(id,payload)=>http.put(`/temps-travail/creneaux/${id}`,payload).then(r=>r.data),
 changerStatutCreneau:(id,statut)=>http.patch(`/temps-travail/creneaux/${id}/statut`,{statut}).then(r=>r.data),
 declarerCreneau:(id,statutEmploye,commentaireEmploye)=>http.patch(`/temps-travail/creneaux/${id}/declaration`,{statutEmploye,commentaireEmploye}).then(r=>r.data),
 validerCreneau:(id,statut)=>http.patch(`/temps-travail/creneaux/${id}/validation`,{statut}).then(r=>r.data),
 aValider:()=>http.get('/temps-travail/creneaux/a-valider').then(r=>r.data),
 supprimerPlanning:(id)=>http.delete(`/temps-travail/plannings/${id}`),
 modifierPlanning:(id,payload)=>http.put(`/temps-travail/plannings/${id}`,payload).then(r=>r.data),
}
