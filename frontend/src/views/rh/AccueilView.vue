<script setup>
import { onMounted, ref } from 'vue'
import { demandeService } from '@/services/demandeService'
import PageHeader from '@/components/ui/PageHeader.vue'
import ContratsExpirantAlert from '@/components/ui/ContratsExpirantAlert.vue'

const demandesEnAttente = ref(0)
const chargement = ref(true)

onMounted(async () => {
  try {
    const page = await demandeService.lister({ statut: 'ATTENTE_RH', page: 0, size: 1 })
    demandesEnAttente.value = page.totalElements
  } finally {
    chargement.value = false
  }
})
</script>

<template>
  <PageHeader title="Espace Ressources humaines" :breadcrumb="['Espace RH', 'Accueil']"
    subtitle="Gérez les demandes de recrutement, les candidatures et l’administration du personnel." />
  <ContratsExpirantAlert lien-employe />
  <div class="row g-3">
    <div class="col-md-6 col-xl-4">
      <RouterLink to="/rh/demandes-a-traiter" class="card card-body h-100 text-decoration-none">
        <div class="d-flex justify-content-between align-items-start">
          <div><h2 class="h5">Demandes à traiter</h2><p class="text-body-secondary mb-0">Vérifiez les demandes envoyées par les managers.</p></div>
          <span class="badge text-bg-primary">{{ chargement ? '…' : demandesEnAttente }}</span>
        </div>
      </RouterLink>
    </div>
    <div class="col-md-6 col-xl-4">
      <RouterLink to="/rh/offres" class="card card-body h-100 text-decoration-none">
        <h2 class="h5">Offres d’emploi</h2><p class="text-body-secondary mb-0">Créez et publiez les offres liées aux demandes validées.</p>
      </RouterLink>
    </div>
    <div class="col-md-6 col-xl-4">
      <RouterLink to="/rh/candidatures" class="card card-body h-100 text-decoration-none">
        <h2 class="h5">Candidatures</h2><p class="text-body-secondary mb-0">Suivez les candidatures et les entretiens.</p>
      </RouterLink>
    </div>
  </div>
</template>
