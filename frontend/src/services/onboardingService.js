import http from './http'

export const onboardingService = {
  obtenir: (employeId) => http.get(`/onboarding/employes/${employeId}`).then((r) => r.data),
  initialiser: (employeId) => http.post(`/onboarding/employes/${employeId}/initialiser-onboarding`).then((r) => r.data),
  employesManager: () => http.get('/manager/onboarding/employes').then((r) => r.data),
  tachesManager: (employeId) => http.get(`/manager/onboarding/employes/${employeId}/taches`).then((r) => r.data),
  initialiserManager: (employeId) => http.post(`/manager/onboarding/employes/${employeId}/initialiser`).then((r) => r.data),
  assignerTache: (tacheId, utilisateurId) => http.patch(`/manager/onboarding/taches/${tacheId}/assigner`, { utilisateurId }).then((r) => r.data),
  mesTaches: () => http.get('/onboarding/taches/mes-taches').then((r) => r.data),
  mettreAJourTache: (tacheId, statut) => http.patch(`/onboarding/taches/${tacheId}`, { statut }).then((r) => r.data),
}
