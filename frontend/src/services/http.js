import axios from 'axios'
import { tokenStorage } from './tokenStorage'

const http = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL || ''}/api`,
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Normalise les erreurs : les vues/stores reçoivent toujours une Error avec un message lisible.
http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    let message = error.response?.data?.message
    if (!message) {
      if (status === 501) message = 'Cette fonctionnalité n\'est pas encore disponible côté serveur.'
      else if (status === 404) message = 'Ressource introuvable.'
      else if (status) message = `Erreur serveur (${status}).`
      else message = 'Serveur injoignable. Vérifiez que l\'API Spring Boot est démarrée.'
    }
    return Promise.reject(Object.assign(new Error(message), { status }))
  },
)

export default http
