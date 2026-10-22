<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useOffresStore } from '@/stores/offres'
import { useAuthStore } from '@/stores/auth'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const router = useRouter()
const store = useOffresStore()
const auth = useAuthStore()
const { offreSelectionnee: offre, loading, error } = storeToRefs(store)
onMounted(() => store.obtenir(route.params.id))
function postuler() {
  const target = router.resolve({ name: 'candidat-candidature', query: { offreId: offre.value.id } }).fullPath
  router.push(!auth.isAuthenticated ? { name: 'login', query: { redirect: target } } : target)
}
</script>

<template>
  <RouterLink class="btn btn-link px-0 mb-3" :to="{ name: 'offres-publiques' }"><i class="bi bi-arrow-left me-1" aria-hidden="true" />Retour aux offres</RouterLink>
  <div v-if="loading" class="text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" />Chargement de l’offre…</div>
  <AlertMessage v-else-if="error" :message="error" />
  <article v-else-if="offre" class="card shadow-sm"><div class="card-body p-4 p-lg-5">
    <span v-if="offre.departement" class="badge text-bg-primary mb-3">{{ offre.departement }}</span>
    <h1 class="h2">{{ offre.titre }}</h1>
    <div class="d-flex flex-wrap gap-3 text-body-secondary mb-4 small">
      <span v-if="offre.lieu"><i class="bi bi-geo-alt me-1" aria-hidden="true" />{{ offre.lieu }}</span>
      <span v-if="offre.typeContrat"><i class="bi bi-briefcase me-1" aria-hidden="true" />{{ offre.typeContrat }}</span>
      <span v-if="offre.tempsTravail"><i class="bi bi-clock me-1" aria-hidden="true" />{{ offre.tempsTravail === 'TEMPS_PLEIN' ? 'Temps plein' : 'Temps partiel' }}</span>
      <span v-if="offre.dateLimiteCandidature"><i class="bi bi-calendar-event me-1" aria-hidden="true" />Candidature avant le {{ offre.dateLimiteCandidature }}</span>
    </div>
    <section class="mb-4"><h2 class="h5">Description du poste</h2><p class="white-space-pre-line">{{ offre.description }}</p></section>
    <section v-if="offre.profilRecherche" class="mb-4"><h2 class="h5">Profil recherché</h2><p class="white-space-pre-line">{{ offre.profilRecherche }}</p></section>
    <button class="btn btn-primary btn-lg" type="button" @click="postuler">Postuler à cette offre</button>
  </div></article>
</template>
