<script setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useOffresStore } from '@/stores/offres'
import { useAuthStore } from '@/stores/auth'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useOffresStore()
const { offres, page, loading, error } = storeToRefs(store)
const router = useRouter()
const auth = useAuthStore()
const filters = reactive({ texte: '', departementId: '', typeContrat: '' })
const departements = computed(() => {
  const map = new Map()
  for (const offer of offres.value) if (offer.departementId && offer.departement) map.set(offer.departementId, offer.departement)
  return [...map.entries()].sort((a, b) => a[1].localeCompare(b[1], 'fr'))
})
let searchTimer

async function charger(pageNum = 0) {
  await store.charger({ page: pageNum, size: 12, texte: filters.texte || undefined,
    departementId: filters.departementId || undefined, typeContrat: filters.typeContrat || undefined })
}
function postuler(offre) {
  const destination = router.resolve({ name: 'candidat-candidature', query: { offreId: offre.id } }).fullPath
  if (!auth.isAuthenticated) router.push({ name: 'login', query: { redirect: destination } })
  else router.push(destination)
}
watch(() => [filters.texte, filters.departementId, filters.typeContrat], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => charger(0), 250)
})
onMounted(() => charger(0))
</script>

<template>
  <header class="mb-4">
    <h1 class="h2">Offres d’emploi</h1>
    <p class="text-body-secondary mb-0">Découvrez les opportunités et trouvez le poste qui correspond à votre profil.</p>
  </header>
  <AlertMessage :message="error" />
  <section class="card shadow-sm mb-4" aria-label="Filtres des offres">
    <div class="card-body"><div class="row g-3">
      <div class="col-md-6">
        <label for="offre-recherche" class="form-label">Recherche</label>
        <div class="input-group"><span class="input-group-text"><i class="bi bi-search" aria-hidden="true"></i></span>
          <input id="offre-recherche" v-model="filters.texte" type="search" class="form-control" placeholder="Titre, description…" />
        </div>
      </div>
      <div class="col-md-3"><label for="offre-departement" class="form-label">Département</label>
        <select id="offre-departement" v-model="filters.departementId" class="form-select">
          <option value="">Tous les départements</option><option v-for="[id, name] in departements" :key="id" :value="id">{{ name }}</option>
        </select>
      </div>
      <div class="col-md-3"><label for="offre-type" class="form-label">Type de contrat</label>
        <select id="offre-type" v-model="filters.typeContrat" class="form-select">
          <option value="">Tous les contrats</option><option value="CDI">CDI</option><option value="CDD">CDD</option>
          <option value="STAGE">Stage</option><option value="ALTERNANCE">Alternance</option>
        </select>
      </div>
    </div></div>
  </section>
  <div v-if="loading" class="text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" />Chargement des offres…</div>
  <div v-else-if="!offres.length && !error" class="alert alert-light border text-center py-5">
    <i class="bi bi-search display-6 d-block mb-2 text-body-secondary" aria-hidden="true"></i>Aucune offre ne correspond à votre recherche.
  </div>
  <div v-else class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4">
    <div v-for="offre in offres" :key="offre.id" class="col">
      <article class="card h-100 shadow-sm"><div class="card-body d-flex flex-column">
        <span v-if="offre.departement" class="badge text-bg-primary align-self-start mb-2">{{ offre.departement }}</span>
        <h2 class="h5">{{ offre.titre }}</h2><p class="text-body-secondary">{{ offre.description }}</p>
        <ul class="list-unstyled small text-body-secondary mt-auto mb-3">
          <li v-if="offre.lieu"><i class="bi bi-geo-alt me-1" aria-hidden="true" />{{ offre.lieu }}</li>
          <li v-if="offre.typeContrat"><i class="bi bi-briefcase me-1" aria-hidden="true" />{{ offre.typeContrat }}</li>
          <li v-if="offre.dateLimiteCandidature"><i class="bi bi-calendar-event me-1" aria-hidden="true" />Candidatures jusqu’au {{ offre.dateLimiteCandidature }}</li>
        </ul>
        <div class="d-flex gap-2"><RouterLink class="btn btn-outline-primary" :to="{ name: 'offre-detail', params: { id: offre.id } }">Voir l’offre</RouterLink>
          <button class="btn btn-primary" type="button" @click="postuler(offre)">Postuler</button></div>
      </div></article>
    </div>
  </div>
  <nav v-if="page.totalPages > 1" class="d-flex justify-content-between align-items-center mt-4" aria-label="Pagination des offres">
    <button class="btn btn-outline-secondary" :disabled="page.number <= 0 || loading" @click="charger(page.number - 1)">Précédent</button>
    <span class="small text-body-secondary">Page {{ page.number + 1 }} sur {{ page.totalPages }} · {{ page.totalElements }} offres</span>
    <button class="btn btn-outline-secondary" :disabled="page.number + 1 >= page.totalPages || loading" @click="charger(page.number + 1)">Suivant</button>
  </nav>
</template>
