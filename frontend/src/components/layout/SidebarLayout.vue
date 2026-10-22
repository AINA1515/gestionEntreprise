<script setup>
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({
  espace: { type: String, required: true },
  items: { type: Array, required: true }, // [{ to, label, icon }]
})

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const visibleItems = computed(() => props.items.filter((item) =>
  (!item.permission || auth.hasPermission(item.permission))
  && (!item.roles || item.roles.some((role) => auth.hasRole(role))),
))
const userRole = computed(() => auth.utilisateur?.roles?.[0] ?? 'Utilisateur')

function deconnecter() {
  auth.logout()
  router.push({ name: 'login' })
}

const profileItems = [
  { to: '/profil/mot-de-passe', label: 'Changer le mot de passe' },
]
</script>

<template>
  <div class="app-shell min-vh-100">
    <aside id="appSidebar" class="offcanvas-md offcanvas-start bg-dark text-white app-sidebar" tabindex="-1" aria-labelledby="sidebarTitle">
      <div class="offcanvas-header border-bottom border-secondary d-md-none">
        <h2 id="sidebarTitle" class="h5 text-white mb-0">{{ espace }}</h2>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas" data-bs-target="#appSidebar" aria-label="Fermer le menu"></button>
      </div>
      <div class="offcanvas-body p-3 d-flex flex-column">
      <div class="fs-5 fw-bold mb-4 d-none d-md-block">
        ConceptionLogicielRH
        <small class="d-block fs-6 fw-normal text-white-50">{{ espace }}</small>
      </div>
      <div class="small text-uppercase text-white-50 px-2 mb-2">Menu principal</div>
      <nav class="nav nav-pills flex-column gap-1">
        <RouterLink
          v-for="item in visibleItems"
          :key="item.to"
          :to="item.to"
          class="nav-link text-white"
          active-class="active"
          data-bs-dismiss="offcanvas" data-bs-target="#appSidebar"
        >
          <i :class="`bi bi-${item.icon} me-2`" aria-hidden="true"></i>{{ item.label }}
        </RouterLink>
      </nav>
      <div class="mt-auto pt-4 border-top border-secondary small text-white-50">
        <div>ConceptionLogicielRH<br />Gestion des ressources humaines</div>
      </div>
      </div>
    </aside>

    <div class="app-main">
      <header class="navbar navbar-expand bg-white border-bottom px-3 px-md-4 sticky-top">
        <button class="btn btn-outline-secondary d-md-none me-2" type="button" data-bs-toggle="offcanvas" data-bs-target="#appSidebar" aria-controls="appSidebar" aria-label="Ouvrir le menu">
          <i class="bi bi-list fs-5" aria-hidden="true"></i>
        </button>
        <span class="navbar-text fw-semibold text-body">{{ route.meta.espace || espace }}</span>
        <div class="ms-auto d-flex align-items-center gap-2">
          <button class="btn btn-light position-relative" type="button" aria-label="Notifications">
            <i class="bi bi-bell" aria-hidden="true"></i><span class="visually-hidden">Aucune nouvelle notification</span>
          </button>
          <div class="dropdown">
            <button class="btn btn-light dropdown-toggle d-flex align-items-center gap-2" data-bs-toggle="dropdown" aria-expanded="false">
              <span class="d-none d-sm-inline text-end"><span class="d-block small fw-semibold">{{ auth.utilisateur?.nomAffichage || auth.utilisateur?.login }}</span><span class="d-block small text-body-secondary">{{ userRole }}</span></span>
              <i class="bi bi-person-circle fs-5" aria-hidden="true"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
              <li><RouterLink v-for="item in profileItems" :key="item.to" class="dropdown-item" :to="item.to">{{ item.label }}</RouterLink></li>
              <li><hr class="dropdown-divider" /></li>
              <li><button class="dropdown-item text-danger" @click="deconnecter"><i class="bi bi-box-arrow-right me-2"></i>Déconnexion</button></li>
            </ul>
          </div>
        </div>
      </header>
      <main class="p-3 p-md-4"><slot /></main>
    </div>
  </div>
</template>

<style scoped>
.app-shell { display: flex; }
.app-sidebar { width: 17rem; flex: 0 0 17rem; min-height: 100vh; }
.app-main { min-width: 0; flex: 1; }
@media (max-width: 767.98px) {
  .app-sidebar { --bs-offcanvas-width: min(85vw, 18rem); min-height: 100%; }
}
</style>
