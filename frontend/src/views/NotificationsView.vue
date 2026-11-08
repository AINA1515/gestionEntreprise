<script setup>
import { onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'
import { useAuthStore } from '@/stores/auth'
import { lienNotification, texteNotification } from '@/composables/notificationLink'

const store = useNotificationsStore()
const auth = useAuthStore()
const dates = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium', timeStyle: 'short' })
onMounted(() => store.chargerPage())
async function page(delta) { await store.chargerPage(store.page.page + delta, store.page.size) }
async function lire(item) { if (!item.lue) { try { await store.marquerLue(item.id) } catch { /* sans incidence sur la navigation */ } } }
</script>

<template>
  <section class="container-fluid py-3">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
      <div><h1 class="h2 mb-1">Notifications</h1><p class="text-body-secondary mb-0">Vos dernières informations liées à votre activité.</p></div>
      <button class="btn btn-outline-primary" type="button" :disabled="store.unreadCount === 0" @click="store.lireTout()">Tout marquer comme lu</button>
    </div>
    <div class="list-group shadow-sm">
      <div v-if="store.loading" class="list-group-item text-center text-body-secondary">Chargement…</div>
      <div v-else-if="!store.page.content.length" class="list-group-item text-center text-body-secondary py-5">Aucune notification à afficher.</div>
      <article v-for="item in store.page.content" v-else :key="item.id" class="list-group-item d-flex justify-content-between align-items-start gap-3" :class="{ 'bg-body-tertiary': !item.lue }">
        <div>
          <h2 class="h6 mb-1">{{ item.sujet }}</h2>
          <p v-if="texteNotification(item)" class="small mb-1">{{ texteNotification(item) }}</p>
          <div class="small text-body-secondary">{{ dates.format(new Date(item.dateCreation)) }}</div>
        </div>
        <div class="d-flex flex-column gap-1 align-items-end">
          <RouterLink v-if="lienNotification(item, auth)" class="btn btn-sm btn-primary text-nowrap" :to="lienNotification(item, auth)" @click="lire(item)">Ouvrir</RouterLink>
          <button v-if="!item.lue" class="btn btn-sm btn-outline-secondary text-nowrap" type="button" @click="store.marquerLue(item.id)">Marquer comme lue</button>
          <span v-else class="badge text-bg-light">Lue</span>
        </div>
      </article>
    </div>
    <div class="d-flex justify-content-between align-items-center mt-3">
      <span class="small text-body-secondary">{{ store.page.totalElements }} notification(s)</span>
      <div class="btn-group"><button class="btn btn-outline-secondary" :disabled="store.page.page <= 0" @click="page(-1)">Précédent</button><button class="btn btn-outline-secondary" :disabled="store.page.page + 1 >= store.page.totalPages" @click="page(1)">Suivant</button></div>
    </div>
  </section>
</template>
