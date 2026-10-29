import http from './http'

export const contratService = {
  creer: (payload) => http.post('/onboarding/contrats', payload).then((r) => r.data),
}
