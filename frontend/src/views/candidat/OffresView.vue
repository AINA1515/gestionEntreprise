<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useOffresStore } from '@/stores/offres'
import { useOffresFilter } from '@/composables/useOffresFilter'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useOffresStore()
const { offres } = storeToRefs(store)
const { recherche, departement, departements, offresFiltrees } = useOffresFilter(offres)

onMounted(store.charger)
</script>

<template>
  <RouterLink class="btn btn-link px-0 mb-3" :to="{ name: 'candidat-cv' }">
    <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>Retour à mon espace candidat
  </RouterLink>

  <div class="mb-4">
    <h1 class="h2">Nos offres d'emploi</h1>
    <p class="text-body-secondary">
      Découvrez les opportunités et trouvez le poste qui correspond à votre profil.
    </p>
  </div>

  <AlertMessage :message="store.error" />

  <div class="row g-3 mb-4">
    <div class="col-md-8">
      <div class="input-group">
        <span class="input-group-text"><i class="bi bi-search" aria-hidden="true"></i></span>
        <input v-model="recherche" type="search" class="form-control" placeholder="Rechercher un poste..." />
      </div>
    </div>
    <div class="col-md-4">
      <select v-model="departement" class="form-select">
        <option value="">Tous les départements</option>
        <option v-for="d in departements" :key="d" :value="d">{{ d }}</option>
      </select>
    </div>
  </div>

  <p v-if="store.loading" class="text-body-secondary">Chargement des offres…</p>
  <p v-else-if="!offresFiltrees.length && !store.error" class="text-body-secondary">
    Aucune offre ne correspond pour le moment.
  </p>

  <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4 mb-4">
    <div v-for="offre in offresFiltrees" :key="offre.id" class="col">
      <article class="card h-100 shadow-sm">
        <div class="card-body d-flex flex-column">
          <span v-if="offre.departement" class="badge text-bg-primary align-self-start mb-2">
            {{ offre.departement }}
          </span>
          <h2 class="h5 card-title">{{ offre.titre }}</h2>
          <p class="card-text">{{ offre.description }}</p>
          <RouterLink class="btn btn-primary mt-auto align-self-start"
                      :to="{ name: 'candidat-candidature', query: { offreId: offre.id } }">
            Postuler
          </RouterLink>
        </div>
      </article>
    </div>
  </div>
</template>
