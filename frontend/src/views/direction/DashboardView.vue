<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { reportingService } from '@/services/reportingService'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const { formatMontant } = useFormatters()
const data = ref(null)
const loading = ref(true)
const error = ref('')

const somme = (lignes) => (lignes ?? []).reduce((total, l) => total + l.valeur, 0)
const valeur = (lignes, cle) => lignes?.find((l) => l.libelle === cle)?.valeur ?? 0
const maxDe = (lignes) => Math.max(1, ...(lignes ?? []).map((l) => l.valeur))
const libelle = (texte) => texte.replaceAll('_', ' ').toLowerCase().replace(/^./, (c) => c.toUpperCase())

const ordreEntonnoir = ['RECUE', 'PRESELECTIONNEE', 'EN_ENTRETIEN', 'RETENUE', 'REJETEE']
const entonnoir = computed(() => ordreEntonnoir.map((s) => ({ libelle: s, valeur: valeur(data.value?.entonnoirCandidatures, s) })))
const effectifTotal = computed(() => somme(data.value?.effectifsParStatut))
const tachesEnRetard = computed(() => somme(data.value?.tachesEnRetardParService))

onMounted(async () => {
  try { data.value = await reportingService.synthese() }
  catch (e) { error.value = e.message || 'Impossible de charger les indicateurs.' }
  finally { loading.value = false }
})
</script>

<template>
  <section class="container-fluid py-3">
    <PageHeader title="Tableau de bord Direction" :breadcrumb="['Direction', 'Tableau de bord']"
      subtitle="Effectifs, recrutement, intégration et masse salariale." />
    <AlertMessage :message="error" />
    <div v-if="loading" class="card card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true" />Chargement des indicateurs…</div>
    <template v-else-if="data">
      <div class="row g-3 mb-4">
        <div class="col-6 col-xl-3"><StatCard label="Effectif total" :value="effectifTotal" icon="people" /></div>
        <div class="col-6 col-xl-3"><StatCard label="Demandes à arbitrer" :value="valeur(data.demandesParStatut, 'ATTENTE_DIRECTION')" icon="building" /></div>
        <div class="col-6 col-xl-3"><StatCard label="En onboarding" :value="valeur(data.effectifsParStatut, 'EN_ONBOARDING')" icon="person-plus" /></div>
        <div class="col-6 col-xl-3"><StatCard label="Tâches d’accueil en retard" :value="tachesEnRetard" icon="exclamation-triangle" /></div>
      </div>

      <div class="row g-4">
        <div class="col-lg-6"><section class="card shadow-sm h-100"><div class="card-body">
          <h2 class="h5">Entonnoir de recrutement</h2>
          <p v-if="!somme(data.entonnoirCandidatures)" class="text-body-secondary mb-0">Aucune candidature.</p>
          <div v-for="etape in entonnoir" :key="etape.libelle" class="mb-2">
            <div class="d-flex justify-content-between small"><span>{{ libelle(etape.libelle) }}</span><strong>{{ etape.valeur }}</strong></div>
            <div class="progress" role="progressbar" :aria-label="libelle(etape.libelle)" :aria-valuenow="etape.valeur" aria-valuemin="0" :aria-valuemax="maxDe(entonnoir)">
              <div class="progress-bar" :class="etape.libelle === 'REJETEE' ? 'bg-secondary' : ''" :style="{ width: (etape.valeur / maxDe(entonnoir)) * 100 + '%' }"></div>
            </div>
          </div>
        </div></section></div>

        <div class="col-lg-6"><section class="card shadow-sm h-100"><div class="card-body">
          <h2 class="h5">Effectifs par département</h2>
          <p v-if="!data.effectifsParDepartement.length" class="text-body-secondary mb-0">Aucune affectation en cours.</p>
          <div v-for="l in data.effectifsParDepartement" :key="l.libelle" class="mb-2">
            <div class="d-flex justify-content-between small"><span>{{ l.libelle }}</span><strong>{{ l.valeur }}</strong></div>
            <div class="progress" role="progressbar" :aria-label="l.libelle" :aria-valuenow="l.valeur" aria-valuemin="0" :aria-valuemax="maxDe(data.effectifsParDepartement)">
              <div class="progress-bar bg-success" :style="{ width: (l.valeur / maxDe(data.effectifsParDepartement)) * 100 + '%' }"></div>
            </div>
          </div>
        </div></section></div>

        <div class="col-lg-6"><section class="card shadow-sm h-100"><div class="card-body">
          <h2 class="h5">Masse salariale (contrats en cours)</h2>
          <table v-if="data.masseSalarialeParDepartement.length" class="table table-sm mb-0">
            <thead><tr><th scope="col">Département</th><th scope="col" class="text-end">Salaires bruts (MGA)</th></tr></thead>
            <tbody>
              <tr v-for="l in data.masseSalarialeParDepartement" :key="l.libelle"><td>{{ l.libelle }}</td><td class="text-end">{{ formatMontant(l.montant) }}</td></tr>
            </tbody>
            <tfoot><tr><th scope="row">Total</th><th class="text-end">{{ formatMontant(data.masseSalarialeTotale) }}</th></tr></tfoot>
          </table>
          <p v-else class="text-body-secondary mb-0">Aucun contrat en cours.</p>
        </div></section></div>

        <div class="col-lg-6"><section class="card shadow-sm h-100"><div class="card-body">
          <h2 class="h5">Intégration et propositions</h2>
          <h3 class="h6 text-body-secondary">Onboardings</h3>
          <p class="mb-2"><span v-for="l in data.onboardingsParStatut" :key="l.libelle" class="badge text-bg-light me-1">{{ libelle(l.libelle) }} : {{ l.valeur }}</span><span v-if="!data.onboardingsParStatut.length" class="text-body-secondary">Aucun.</span></p>
          <h3 class="h6 text-body-secondary">Tâches en retard par service</h3>
          <p class="mb-2"><span v-for="l in data.tachesEnRetardParService" :key="l.libelle" class="badge text-bg-danger me-1">{{ libelle(l.libelle) }} : {{ l.valeur }}</span><span v-if="!data.tachesEnRetardParService.length" class="text-body-secondary">Aucun retard.</span></p>
          <h3 class="h6 text-body-secondary">Propositions d’embauche</h3>
          <p class="mb-0"><span v-for="l in data.propositionsParStatut" :key="l.libelle" class="badge text-bg-light me-1">{{ libelle(l.libelle) }} : {{ l.valeur }}</span><span v-if="!data.propositionsParStatut.length" class="text-body-secondary">Aucune.</span></p>
        </div></section></div>
      </div>
      <RouterLink class="btn btn-primary mt-4" :to="{ name: 'direction-arbitrage' }">Voir les demandes à arbitrer</RouterLink>
    </template>
  </section>
</template>
