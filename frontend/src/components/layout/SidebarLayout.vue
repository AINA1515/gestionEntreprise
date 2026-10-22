<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

defineProps({
  espace: { type: String, required: true },
  items: { type: Array, required: true }, // [{ to, label, icon }]
})

const auth = useAuthStore()
const router = useRouter()

function deconnecter() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="row g-0 min-vh-100">
    <aside class="col-md-3 col-xl-2 bg-dark text-white p-3 d-flex flex-column app-sidebar">
      <div class="fs-5 fw-bold mb-4">
        ConceptionLogicielRH
        <small class="d-block fs-6 fw-normal text-white-50">{{ espace }}</small>
      </div>

      <div class="small text-uppercase text-white-50 px-2 mb-2">Menu principal</div>
      <nav class="nav nav-pills flex-column gap-1">
        <RouterLink
          v-for="item in items"
          :key="item.to"
          :to="item.to"
          class="nav-link text-white"
          active-class="active"
        >
          <i :class="`bi bi-${item.icon} me-2`" aria-hidden="true"></i>{{ item.label }}
        </RouterLink>
      </nav>

      <div class="mt-auto pt-4 border-top border-secondary small text-white-50">
        <button v-if="auth.isAuthenticated" class="btn btn-sm btn-outline-light mb-3" @click="deconnecter">
          <i class="bi bi-box-arrow-right me-1" aria-hidden="true"></i>Déconnexion
        </button>
        <div>ConceptionLogicielRH<br />Gestion des ressources humaines</div>
      </div>
    </aside>

    <main class="col-md-9 col-xl-10 p-3 p-md-4">
      <slot />
    </main>
  </div>
</template>
