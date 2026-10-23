<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'

const route = useRoute()
const router = useRouter()
const store = useDemandesStore()
const { formatDate, formatMontant } = useFormatters()
const detail = computed(() => store.demandeSelectionnee)
const demande = computed(() => detail.value?.demande)
const actions = computed(() => detail.value?.actionsDisponibles || [])
const modalOpen = ref(false)
const modalAction = ref('')
const justification = ref('')
const refusing = computed(() => modalAction.value === 'REFUSER')

function ouvrir(action) {
  const actionDisponible = actions.value.includes(action.toLowerCase())
    || (action === 'REFUSER' && demande.value?.statut === 'ATTENTE_DIRECTION')
  if (!actionDisponible) return
  modalAction.value = action
  justification.value = ''
  modalOpen.value = true
}
async function confirmer() {
  const result = await store.valider(route.params.id, {
    action: modalAction.value,
    justification: justification.value.trim() || null,
  })
  if (result) {
    modalOpen.value = false
    router.push({ name: 'direction-arbitrage' })
  }
}
onMounted(() => store.obtenir(route.params.id))
</script>

<template>
  <PageHeader title="Arbitrage de la demande" :breadcrumb="['Direction', 'Arbitrage', 'Détail']"
    subtitle="Consultez l’avis RH et rendez la décision de Direction." />
  <AlertMessage :message="store.error" />
  <div v-if="store.loading && !demande" class="text-center py-5">Chargement…</div>
  <template v-else-if="demande">
    <section class="card shadow-sm mb-4"><div class="card-body p-4">
      <div class="d-flex flex-wrap justify-content-between gap-3 mb-4"><div><h2 class="h4 mb-1">{{ demande.poste }}</h2><p class="text-body-secondary mb-0">{{ demande.departement }} · Demande #{{ demande.id }}</p></div><StatusBadge type="demande" :value="demande.statut" /></div>
      <div class="row g-4">
        <div class="col-md-4"><div class="small text-body-secondary">Effectif demandé</div><div class="display-6 fw-semibold">{{ demande.effectif }}</div></div>
        <div class="col-md-4"><div class="small text-body-secondary">Budget · salaire de référence</div><div class="display-6 fw-semibold">{{ formatMontant(demande.budget) }}</div></div>
        <div class="col-md-4"><div class="small text-body-secondary">Date souhaitée</div><div class="fs-4 fw-semibold">{{ formatDate(demande.dateSouhaitee) }}</div></div>
        <div class="col-md-6"><div class="small text-body-secondary">Manager demandeur</div><div>{{ demande.manager }}</div></div>
        <div class="col-md-6"><div class="small text-body-secondary">Type de demande</div><div>{{ demande.typeDemande }}<span v-if="demande.employeRemplaceId"> · salarié #{{ demande.employeRemplaceId }}</span></div></div>
        <div class="col-md-6"><div class="small text-body-secondary">Niveau de formation requis</div><div>{{ demande.niveauFormationLibelle || 'Non précisé' }}</div></div>
        <div class="col-12"><div class="small text-body-secondary">Motif</div><p class="mb-0">{{ demande.motif }}</p></div>
      </div>
      <div v-if="actions.includes('valider') || demande.statut === 'ATTENTE_DIRECTION'" class="d-flex flex-wrap gap-2 mt-4">
        <button v-if="actions.includes('valider')" class="btn btn-success" :disabled="store.loading" @click="ouvrir('VALIDER')">Valider la demande</button>
        <button v-if="demande.statut === 'ATTENTE_DIRECTION'" class="btn btn-outline-danger" :disabled="store.loading" @click="ouvrir('REFUSER')">Refuser</button>
      </div>
      <p v-else class="small text-body-secondary mt-4 mb-0">Aucune action de Direction n’est disponible pour cet état.</p>
    </div></section>

    <section class="card shadow-sm"><div class="card-body p-4">
      <h2 class="h5 mb-3">Historique et avis RH</h2>
      <div v-if="!detail.historiqueValidations?.length" class="text-body-secondary">Aucune validation antérieure.</div>
      <ol v-else class="list-group list-group-numbered">
        <li v-for="validation in detail.historiqueValidations" :key="validation.id" class="list-group-item">
          <div class="d-flex flex-wrap justify-content-between gap-2"><strong>{{ validation.etape === 'RH' ? 'Étape RH' : 'Étape Direction' }} · {{ validation.decision }}</strong><span>{{ formatDate(validation.dateDecision) }}</span></div>
          <div class="small text-body-secondary">{{ validation.valideur }}</div>
          <p v-if="validation.justification" class="mb-0 mt-2">{{ validation.justification }}</p>
        </li>
      </ol>
    </div></section>
    <RouterLink class="btn btn-link px-0 mt-3" :to="{ name: 'direction-arbitrage' }">Retour à la file d’arbitrage</RouterLink>
  </template>

  <ConfirmModal v-model="modalOpen" v-model:justification="justification"
    :title="refusing ? 'Refuser cette demande ?' : 'Valider cette demande ?'"
    :message="refusing ? 'La justification sera conservée dans l’historique de validation.' : 'La demande sera renvoyée aux RH pour créer et publier l’offre d’emploi.'"
    :confirm-text="refusing ? 'Confirmer le refus' : 'Confirmer la validation'"
    :variant="refusing ? 'danger' : 'success'" :requires-justification="refusing"
    @confirm="confirmer" />
</template>
