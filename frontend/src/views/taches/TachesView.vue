<script setup>
import { onMounted, ref } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import { onboardingService } from '@/services/onboardingService'
import { useFormatters } from '@/composables/useFormatters'

const items = [{ to: '/taches', label: 'Mes tâches', icon: 'check2-square', permission: 'TACHE_TRAITER' }, { to: '/offres', label: 'Offres d’emploi', icon: 'search' }]
const taches = ref([])
const chargement = ref(true)
const actionEnCours = ref(false)
const erreur = ref('')
const { formatDate } = useFormatters()

async function charger() {
  chargement.value = true
  erreur.value = ''
  try { taches.value = await onboardingService.mesTaches() || [] }
  catch (e) { erreur.value = e.message || 'Impossible de charger vos tâches.' }
  finally { chargement.value = false }
}

async function mettreAJour(tache, statut) {
  actionEnCours.value = true
  erreur.value = ''
  try {
    const miseAJour = await onboardingService.mettreAJourTache(tache.id, statut)
    const index = taches.value.findIndex((item) => item.id === tache.id)
    if (index !== -1) taches.value[index] = miseAJour
  } catch (e) { erreur.value = e.message || 'Impossible de mettre à jour cette tâche.' }
  finally { actionEnCours.value = false }
}

function statutLabel(statut) {
  return ({ A_FAIRE: 'À faire', EN_COURS: 'En cours', VALIDEE: 'Terminée' })[statut] || statut
}

onMounted(charger)
</script>

<template>
  <AppLayout espace="Mes tâches" :items="items">
    <section class="container-fluid py-3">
      <h1 class="h2">Mes tâches d’intégration</h1>
      <p class="text-body-secondary">Consultez les tâches qui vous ont été attribuées et mettez leur avancement à jour.</p>
      <AlertMessage :message="erreur" />
      <div v-if="chargement" class="card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" />Chargement des tâches…</div>
      <div v-else-if="!taches.length" class="card shadow-sm">
        <div class="card-body text-center py-5">
          <i class="bi bi-check2-square display-5 text-body-secondary" aria-hidden="true" />
          <h2 class="h5 mt-3">Aucune tâche ne vous est attribuée</h2>
          <p class="text-body-secondary mb-0">Les tâches apparaîtront ici lorsque votre manager vous aura désigné comme responsable.</p>
        </div>
      </div>
      <div v-else class="list-group shadow-sm">
        <article v-for="tache in taches" :key="tache.id" class="list-group-item p-3">
          <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
            <div>
              <h2 class="h5 mb-1">{{ tache.intitule }}</h2>
              <p class="small text-body-secondary mb-2">Service responsable : {{ tache.serviceResponsable.replaceAll('_', ' ') }}</p>
              <span v-if="tache.dateLimite" class="small text-body-secondary">Échéance : {{ formatDate(tache.dateLimite) }}</span>
            </div>
            <span class="badge" :class="{ 'text-bg-secondary': tache.statut === 'A_FAIRE', 'text-bg-primary': tache.statut === 'EN_COURS', 'text-bg-success': tache.statut === 'VALIDEE' }">{{ statutLabel(tache.statut) }}</span>
          </div>
          <div v-if="tache.actionsDisponibles?.length" class="d-flex gap-2 mt-3">
            <button v-if="tache.actionsDisponibles.includes('commencer')" class="btn btn-sm btn-primary" :disabled="actionEnCours" @click="mettreAJour(tache, 'EN_COURS')">Commencer</button>
            <button v-if="tache.actionsDisponibles.includes('valider')" class="btn btn-sm btn-success" :disabled="actionEnCours" @click="mettreAJour(tache, 'VALIDEE')">Marquer comme terminée</button>
          </div>
        </article>
      </div>
    </section>
  </AppLayout>
</template>
