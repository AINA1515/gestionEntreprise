<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'
import { usePolling } from '@/composables/usePolling'

const store = useNotificationsStore()
const router = useRouter()
const lastTen = computed(() => store.notifications.slice(0, 10))
const dates = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'short' })
usePolling(() => store.actualiser({ limit: 10 }), 60_000)

async function ouvrir(notification) {
  if (!notification.lue) await store.marquerLue(notification.id)
  await router.push({ name: 'notifications' })
}
</script>

<template>
  <div class="dropdown">
    <button class="btn btn-light position-relative" type="button" data-bs-toggle="dropdown" aria-expanded="false" aria-label="Notifications">
      <i class="bi bi-bell" aria-hidden="true"></i>
      <span v-if="store.unreadCount" class="position-absolute top-0 start-100 translate-middle badge rounded-pill text-bg-danger">{{ store.unreadCount > 99 ? '99+' : store.unreadCount }}<span class="visually-hidden">notifications non lues</span></span>
    </button>
    <div class="dropdown-menu dropdown-menu-end notification-menu p-0 shadow">
      <div class="d-flex justify-content-between align-items-center px-3 py-2 border-bottom">
        <strong>Notifications</strong><span class="small text-body-secondary">{{ store.unreadCount }} non lue(s)</span>
      </div>
      <button v-for="item in lastTen" :key="item.id" class="dropdown-item text-wrap py-2 border-bottom" :class="{ 'bg-body-tertiary': !item.lue }" type="button" @click="ouvrir(item)">
        <span class="d-block fw-semibold small">{{ item.sujet }}</span>
        <span class="d-block text-body-secondary small">{{ dates.format(new Date(item.dateCreation)) }}</span>
      </button>
      <div v-if="!lastTen.length" class="px-3 py-3 small text-body-secondary">Aucune notification.</div>
      <RouterLink class="dropdown-item text-center text-primary fw-semibold py-2" :to="{ name: 'notifications' }">Tout voir</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.notification-menu { width: min(24rem, calc(100vw - 2rem)); max-height: 28rem; overflow-y: auto; }
</style>
