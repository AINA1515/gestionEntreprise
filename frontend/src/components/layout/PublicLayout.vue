<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppLayout from './AppLayout.vue'

const auth = useAuthStore()
const utilisateurConnecte = computed(() => auth.isAuthenticated)
const menuConnecte = [
  { to: '/', label: 'Accueil', icon: 'house' },
  { to: '/candidat/cv', label: 'Mon CV', icon: 'file-person', roles: ['CANDIDAT'], permission: 'DOCUMENT_CONSULTER_PERSO' },
  { to: '/candidat/candidature', label: 'Mes candidatures', icon: 'briefcase', roles: ['CANDIDAT'], permission: 'CANDIDATURE_POSTULER' },
  { to: '/offres', label: 'Offres d’emploi', icon: 'search' },
]
</script>

<template>
  <AppLayout v-if="utilisateurConnecte" espace="Espace personnel" :items="menuConnecte">
    <div class="container-fluid"><RouterView /></div>
  </AppLayout>
  <div v-else class="min-vh-100 bg-body-tertiary">
    <header class="navbar navbar-expand bg-white border-bottom shadow-sm">
      <div class="container">
        <RouterLink class="navbar-brand fw-semibold text-primary" :to="{ name: 'offres-publiques' }">
          <i class="bi bi-people-fill me-2" aria-hidden="true"></i>ConceptionLogicielRH
        </RouterLink>
        <nav class="d-flex gap-2">
          <RouterLink class="btn btn-outline-primary btn-sm" :to="{ name: 'login' }">Se connecter</RouterLink>
          <RouterLink class="btn btn-primary btn-sm" :to="{ name: 'inscription' }">S’inscrire</RouterLink>
        </nav>
      </div>
    </header>
    <main class="container py-4 py-lg-5"><RouterView /></main>
    <footer class="container py-4 border-top text-body-secondary small">ConceptionLogicielRH · Recrutement</footer>
  </div>
</template>
