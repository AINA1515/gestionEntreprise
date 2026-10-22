import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authService } from '@/services/authService'
import { tokenStorage } from '@/services/tokenStorage'
import { useToast } from '@/composables/useToast'

const HOME_BY_ROLE = {
  MANAGER: '/manager/dashboard',
  CHARGE_RECRUTEMENT: '/rh/candidatures',
  GESTIONNAIRE_RH: '/rh/accueil',
  DIRECTION: '/direction/dashboard',
  CANDIDAT: '/candidat/cv',
  SALARIE: '/salarie/accueil',
  IT: '/taches',
  MOYENS_GENERAUX: '/taches',
  ADMIN: '/admin/utilisateurs',
}
const HOME_PRIORITY = ['ADMIN', 'MANAGER', 'CHARGE_RECRUTEMENT', 'GESTIONNAIRE_RH', 'DIRECTION', 'CANDIDAT', 'SALARIE', 'IT', 'MOYENS_GENERAUX']

export const useAuthStore = defineStore('auth', () => {
  const token = ref(tokenStorage.get())
  const utilisateur = ref(null)
  const toast = useToast()

  const isAuthenticated = computed(() => !!token.value)
  const homeRoute = computed(() => {
    const roles = utilisateur.value?.roles ?? []
    const principal = HOME_PRIORITY.find((role) => roles.includes(role))
    return HOME_BY_ROLE[principal] ?? (utilisateur.value?.typeCompte === 'CANDIDAT' ? '/candidat/cv' : '/login')
  })

  function hasRole(code) { return utilisateur.value?.roles?.includes(code) ?? false }
  function hasPermission(code) { return utilisateur.value?.permissions?.includes(code) ?? false }
  function hasAnyPermission(codes = []) { return codes.some(hasPermission) }

  async function login(identifiant, motDePasse) {
    const data = await authService.login(identifiant, motDePasse)
    token.value = data.token
    utilisateur.value = data.utilisateur
    tokenStorage.set(data.token)
    toast.success('Connexion réussie.')
  }

  async function chargerUtilisateur() {
    if (!token.value) return null
    if (utilisateur.value) return utilisateur.value
    try {
      utilisateur.value = await authService.me()
      return utilisateur.value
    } catch {
      nettoyerSession()
      return null
    }
  }

  function nettoyerSession() {
    token.value = null
    utilisateur.value = null
    tokenStorage.clear()
  }

  function logout() {
    if (tokenStorage.get()) authService.logout().catch(() => {})
    nettoyerSession()
  }

  async function changerMotDePasse(ancien, nouveau) {
    await authService.changerMotDePasse(ancien, nouveau)
    toast.success('Votre mot de passe a été modifié.')
  }

  if (typeof window !== 'undefined') {
    window.addEventListener('gestionrh:session-expired', nettoyerSession)
  }

  return { token, utilisateur, isAuthenticated, homeRoute, hasRole, hasPermission, hasAnyPermission,
    login, chargerUtilisateur, logout, changerMotDePasse }
})
