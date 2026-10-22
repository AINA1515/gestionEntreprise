<script setup>
import { useRoute } from 'vue-router'

// Titre d'espace et lien de retour pilotés par route.meta (voir router/index.js).
const route = useRoute()
</script>

<template>
  <header class="bg-primary text-white py-3 mb-4">
    <div class="container d-flex flex-wrap justify-content-between align-items-center gap-2">
      <div>
        <h2 class="h4 mb-0">ConceptionLogicielRH</h2>
        <p class="mb-0 small text-white-50">{{ route.meta.espace }}</p>
      </div>
      <RouterLink v-if="route.meta.retour" :to="route.meta.retour.to" class="link-light">
        <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>{{ route.meta.retour.label }}
      </RouterLink>
    </div>
  </header>

  <main class="container pb-5">
    <RouterView />
  </main>
</template>
