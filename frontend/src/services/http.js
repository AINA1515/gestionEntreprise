import axios from 'axios'
import { tokenStorage } from './tokenStorage'
import { useToast } from '@/composables/useToast'

const http = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL || ''}/api/v1`,
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
  async (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''
    const hasSession = !!tokenStorage.get()
    const sessionEndpoint = ['/auth/login', '/auth/inscription-candidat', '/auth/logout'].some((path) => url.includes(path))
    const sessionExpired = status === 401 && hasSession && !sessionEndpoint
    if (sessionExpired) {
      tokenStorage.clear()
      window.dispatchEvent(new Event('gestionrh:session-expired'))
      const toast = useToast()
      toast.info('Session expirée. Veuillez vous reconnecter.')
      try {
        const { default: router } = await import('@/router')
        await router.replace({ name: 'login', query: { message: 'session-expiree' } })
      } catch { /* La session locale est déjà effacée si la navigation n'est pas encore montée. */ }
    }
    let message = error.response?.data?.message
    const code = error.response?.data?.code
    const details = error.response?.data?.details || []
    if (!message) {
      if (sessionExpired) message = 'Session expirée. Veuillez vous reconnecter.'
      else if (status === 401) message = 'Identifiants incorrects.'
      else if (status === 403) message = 'Vous n’êtes pas autorisé à effectuer cette action.'
      else if (status === 501) message = 'Cette fonctionnalité n\'est pas encore disponible côté serveur.'
      else if (status === 404) message = 'Ressource introuvable.'
      else if (status) message = `Erreur serveur (${status}).`
      else message = 'Serveur injoignable. Vérifiez que l\'API Spring Boot est démarrée.'
    }
    if (status === 403) useToast().error(message)
    return Promise.reject(Object.assign(new Error(message), { status, code, details, toastShown: status === 403 || sessionExpired }))
  },
)

export default http
