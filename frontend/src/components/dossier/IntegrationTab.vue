<script setup>
import { onMounted, ref } from 'vue'
import { onboardingService } from '@/services/onboardingService'
import { useFormatters } from '@/composables/useFormatters'

const props = defineProps({ employeId: { type: [Number, String], required: true } })
const { formatDate } = useFormatters()
const onboarding = ref(null)
const erreur = ref('')
const loading = ref(true)

onMounted(async () => {
  try { onboarding.value = await onboardingService.obtenir(props.employeId) }
  catch (e) { if (e.status !== 404) erreur.value = e.message || 'Impossible de charger le parcours d’intégration.' }
  finally { loading.value = false }
})
async function initialiser() {
  erreur.value = ''
  try { onboarding.value = await onboardingService.initialiser(props.employeId) }
  catch (e) { erreur.value = e.message || 'Impossible d’initialiser le parcours.' }
}
</script>

<template>
  <section class="card"><div class="card-body">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
      <div><h2 class="h5 mb-1">Intégration</h2><p v-if="onboarding" class="text-body-secondary mb-0">Statut : {{ onboarding.statut.replaceAll('_', ' ') }} · début le {{ formatDate(onboarding.dateDebut) }}</p></div>
      <button v-if="!onboarding && !loading" class="btn btn-primary" type="button" @click="initialiser">Initialiser le parcours</button>
    </div>
    <p v-if="erreur" class="alert alert-danger py-2">{{ erreur }}</p>
    <p v-else-if="loading" class="text-body-secondary mb-0">Chargement…</p>
    <div v-else-if="onboarding?.taches?.length" class="list-group list-group-flush">
      <div v-for="t in onboarding.taches" :key="t.id" class="list-group-item px-0 d-flex flex-wrap justify-content-between gap-2">
        <div><strong>{{ t.intitule }}</strong><div class="small text-body-secondary">{{ t.serviceResponsable }}<span v-if="t.dateLimite"> · échéance {{ formatDate(t.dateLimite) }}</span></div></div>
        <span class="badge" :class="t.statut === 'VALIDEE' ? 'text-bg-success' : t.statut === 'EN_COURS' ? 'text-bg-primary' : 'text-bg-light'">{{ t.statut.replaceAll('_', ' ') }}</span>
      </div>
    </div>
    <p v-else class="text-body-secondary mb-0">{{ onboarding ? 'Aucune tâche d’intégration.' : 'Aucun parcours d’intégration enregistré.' }}</p>
  </div></section>
</template>
