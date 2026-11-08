<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { contratService } from '@/services/contratService'
import { useAuthStore } from '@/stores/auth'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const auth = useAuthStore()
const { formatDate, formatMontant } = useFormatters()

const contrat = ref(null)
const loading = ref(true)
const error = ref('')
const enCours = ref(false)

const peutTerminer = computed(() => auth.hasPermission('CONTRAT_GERER') && ['EN_COURS', 'EN_ATTENTE'].includes(contrat.value?.statut))
const retour = computed(() => auth.hasPermission('CONTRAT_GERER') && contrat.value
  ? { name: 'rh-employe-detail', params: { id: contrat.value.employeId } } : { name: 'salarie-contrats' })
const libelle = (valeur) => valeur?.replaceAll('_', ' ') ?? ''

async function charger() {
  loading.value = true
  error.value = ''
  try { contrat.value = await contratService.obtenir(route.params.id) }
  catch (e) { contrat.value = null; error.value = e.response?.status === 404 ? 'Ce contrat est introuvable.' : (e.message || 'Impossible de charger ce contrat.') }
  finally { loading.value = false }
}

async function terminer() {
  if (!window.confirm('Terminer ce contrat ? Le salarié passera en « sorti » s’il n’a aucun autre contrat actif.')) return
  enCours.value = true
  error.value = ''
  try { contrat.value = await contratService.terminer(contrat.value.id) }
  catch (e) { error.value = e.message || 'Impossible de terminer le contrat.' }
  finally { enCours.value = false }
}

onMounted(charger)
watch(() => route.params.id, charger)
</script>

<template>
  <div class="container-fluid py-3">
    <RouterLink :to="retour" class="btn btn-link px-0 mb-2"><i class="bi bi-arrow-left me-1" aria-hidden="true"></i>Retour</RouterLink>
    <PageHeader :title="contrat ? `Contrat #${contrat.id}` : 'Contrat'" :breadcrumb="['Contrats', 'Détail']"
      :subtitle="contrat ? `${contrat.employeNom} · ${contrat.posteIntitule}` : ''" />
    <AlertMessage :message="error" />
    <div v-if="loading" class="card card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true" />Chargement…</div>
    <section v-else-if="contrat" class="card shadow-sm">
      <div class="card-body">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-3">
          <h2 class="h5 mb-0">{{ contrat.typeContrat }} · {{ libelle(contrat.typeActe) }}</h2>
          <span class="badge text-bg-light">{{ libelle(contrat.statut) }}</span>
        </div>
        <dl class="row mb-0">
          <dt class="col-sm-4">Salarié</dt><dd class="col-sm-8">{{ contrat.employeNom }} <span class="text-body-secondary">({{ contrat.employeMatricule }})</span></dd>
          <dt class="col-sm-4">Poste</dt><dd class="col-sm-8">{{ contrat.posteIntitule }}</dd>
          <dt class="col-sm-4">Début</dt><dd class="col-sm-8">{{ formatDate(contrat.dateDebut) }}</dd>
          <dt class="col-sm-4">Fin prévue</dt><dd class="col-sm-8">{{ contrat.dateFin ? formatDate(contrat.dateFin) : 'Durée indéterminée' }}</dd>
          <template v-if="contrat.dateFinEffective"><dt class="col-sm-4">Fin effective</dt><dd class="col-sm-8">{{ formatDate(contrat.dateFinEffective) }}</dd></template>
          <template v-else-if="contrat.joursRestants != null && contrat.statut === 'EN_COURS'">
            <dt class="col-sm-4">Échéance</dt>
            <dd class="col-sm-8"><span :class="contrat.joursRestants <= 7 ? 'text-danger fw-semibold' : ''">{{ contrat.joursRestants <= 0 ? 'Arrive à échéance aujourd’hui' : `Dans ${contrat.joursRestants} jour(s)` }}</span></dd>
          </template>
          <dt class="col-sm-4">Salaire brut</dt><dd class="col-sm-8">{{ contrat.salaireBrut != null ? `${formatMontant(contrat.salaireBrut)} ${contrat.devise ?? ''}` : 'Non renseigné' }}</dd>
          <template v-if="contrat.contratPrecedentId">
            <dt class="col-sm-4">Contrat précédent</dt>
            <dd class="col-sm-8"><RouterLink :to="{ name: 'contrat-detail', params: { id: contrat.contratPrecedentId } }">Contrat #{{ contrat.contratPrecedentId }}</RouterLink></dd>
          </template>
        </dl>
        <button v-if="peutTerminer" class="btn btn-outline-danger mt-4" type="button" :disabled="enCours" @click="terminer">{{ enCours ? 'En cours…' : 'Terminer le contrat' }}</button>
      </div>
    </section>
  </div>
</template>
