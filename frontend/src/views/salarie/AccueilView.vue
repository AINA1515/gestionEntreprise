<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import { employeService } from '@/services/employeService'
import { onboardingService } from '@/services/onboardingService'
import { useFormatters } from '@/composables/useFormatters'

const items = [
  { to: '/salarie/accueil', label: 'Accueil', icon: 'house', roles: ['SALARIE'] },
  { to: '/salarie/contrats', label: 'Mes contrats', icon: 'file-earmark-text', roles: ['SALARIE'], permission: 'DOCUMENT_CONSULTER_PERSO' },
  { to: '/taches', label: 'Mes tâches', icon: 'check2-square', roles: ['SALARIE'], permission: 'TACHE_TRAITER' },
  { to: '/offres', label: 'Offres d’emploi', icon: 'search' },
]

const { formatDate } = useFormatters()
const employe = ref(null)
const onboarding = ref(null)
const loading = ref(true)
const error = ref('')

const taches = computed(() => onboarding.value?.taches ?? [])
const obligatoires = computed(() => taches.value.filter((t) => t.obligatoire))
const validees = computed(() => obligatoires.value.filter((t) => t.statut === 'VALIDEE').length)
const pourcentage = computed(() => (obligatoires.value.length ? Math.round((validees.value / obligatoires.value.length) * 100) : 0))
const mesTachesOuvertes = computed(() => taches.value
  .filter((t) => t.serviceResponsable === 'SALARIE' && t.statut !== 'VALIDEE')
  .sort((a, b) => (a.dateLimite || '9999').localeCompare(b.dateLimite || '9999'))
  .slice(0, 5))

function enRetard(tache) { return tache.dateLimite && new Date(tache.dateLimite) < new Date(new Date().toDateString()) }

onMounted(async () => {
  try {
    employe.value = await employeService.moi()
    try { onboarding.value = await onboardingService.obtenir(employe.value.id) }
    catch (e) { if (e.response?.status !== 404) throw e }
  } catch (e) { error.value = e.message || 'Impossible de charger votre espace.' }
  finally { loading.value = false }
})
</script>

<template>
  <AppLayout espace="Espace salarié" :items="items">
    <div class="container-fluid py-3">
      <PageHeader :title="employe ? `Bonjour ${employe.prenom}` : 'Mon espace salarié'" :breadcrumb="['Espace salarié', 'Accueil']"
        :subtitle="employe ? `Matricule ${employe.matricule}` : ''" />
      <AlertMessage :message="error" />
      <div v-if="loading" class="card card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true" />Chargement…</div>
      <div v-else class="row g-3">
        <div class="col-lg-6">
          <section class="card shadow-sm h-100"><div class="card-body">
            <h2 class="h5">Mon intégration</h2>
            <template v-if="onboarding">
              <p class="text-body-secondary">Statut : {{ onboarding.statut.replaceAll('_', ' ') }} · depuis le {{ formatDate(onboarding.dateDebut) }}</p>
              <div class="progress" role="progressbar" :aria-valuenow="pourcentage" aria-valuemin="0" aria-valuemax="100" aria-label="Avancement de l’intégration">
                <div class="progress-bar" :style="{ width: pourcentage + '%' }">{{ pourcentage }} %</div>
              </div>
              <p class="small text-body-secondary mt-2 mb-0">{{ validees }} tâche(s) obligatoire(s) validée(s) sur {{ obligatoires.length }}.</p>
            </template>
            <p v-else class="text-body-secondary mb-0">Votre parcours d’intégration n’a pas encore été initialisé.</p>
          </div></section>
        </div>
        <div class="col-lg-6">
          <section class="card shadow-sm h-100"><div class="card-body">
            <div class="d-flex justify-content-between align-items-center"><h2 class="h5 mb-0">À faire de mon côté</h2><RouterLink to="/taches" class="small">Tout voir</RouterLink></div>
            <ul v-if="mesTachesOuvertes.length" class="list-group list-group-flush mt-2">
              <li v-for="t in mesTachesOuvertes" :key="t.id" class="list-group-item px-0 d-flex justify-content-between gap-2">
                <span>{{ t.intitule }}</span>
                <span class="small" :class="enRetard(t) ? 'text-danger fw-semibold' : 'text-body-secondary'">{{ t.dateLimite ? formatDate(t.dateLimite) : '' }}</span>
              </li>
            </ul>
            <p v-else class="text-body-secondary mt-2 mb-0">Aucune tâche en attente de votre part.</p>
          </div></section>
        </div>
      </div>
    </div>
  </AppLayout>
</template>
