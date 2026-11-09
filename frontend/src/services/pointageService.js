import http from './http'
export const pointageService={lister:(id,debut,fin)=>http.get(`/pointages/employes/${id}`,{params:{debut,fin}}).then(r=>r.data),dashboard:(debut,fin)=>http.get('/pointages/dashboard',{params:{debut,fin}}).then(r=>r.data),saisir:(payload)=>http.post('/pointages',payload).then(r=>r.data)}
