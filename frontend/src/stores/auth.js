import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authService } from '@/services/authService'
import { tokenStorage } from '@/services/tokenStorage'

const HOME_BY_TYPE = {
  MANAGER: '/manager/dashboard',
  RH: '/rh/candidatures',
  CANDIDAT: '/candidat/cv',
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(tokenStorage.get())
  const utilisateur = ref(null)

  const isAuthenticated = computed(() => !!token.value)
  const homeRoute = computed(
    () => HOME_BY_TYPE[utilisateur.value?.role] ?? HOME_BY_TYPE[utilisateur.value?.typeCompte] ?? '/candidat/cv',
  )

  async function login(identifiant, motDePasse) {
    const data = await authService.login(identifiant, motDePasse)
    token.value = data.token
    utilisateur.value = data.utilisateur
    tokenStorage.set(data.token)
  }

  async function chargerUtilisateur() {
    if (!token.value || utilisateur.value) return
    try {
      utilisateur.value = await authService.me()
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    utilisateur.value = null
    tokenStorage.clear()
  }

  return { token, utilisateur, isAuthenticated, homeRoute, login, chargerUtilisateur, logout }
})
