<script setup>
import { useRoute } from 'vue-router'
import AppLayout from './AppLayout.vue'
import { useRoleMenu } from '@/composables/useRoleMenu'

// Coque unique de l'application connectée : le menu de gauche vient du profil de l'utilisateur,
// pas de la page courante, donc il ne change plus d'un lien à l'autre.
const route = useRoute()
const { items, espace } = useRoleMenu()
</script>

<template>
  <AppLayout :espace="espace" :items="items">
    <div class="container-fluid">
      <RouterLink v-if="route.meta.retour" :to="route.meta.retour.to" class="btn btn-link px-0 mb-3">
        <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>{{ route.meta.retour.label }}
      </RouterLink>
      <RouterView />
    </div>
  </AppLayout>
</template>
