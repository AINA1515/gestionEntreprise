<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useReferentielStore } from '@/stores/referentiel'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useDemandesStore()
const referentiel = useReferentielStore()
const router = useRouter()
const { formatDate, formatMontant } = useFormatters()
const filters = reactive({ departementId: '', dateDebut: '', dateFin: '' })
const selectedIds = ref([])
const selectedBudget = computed(() => Math.round(store.demandes.filter((item) => selectedIds.value.includes(item.id))
  .reduce((sum, item) => sum + Number(item.budget || 0) * 100, 0)) / 100)
const allSelected = computed(() => store.demandes.length > 0 && store.demandes.every((item) => selectedIds.value.includes(item.id)))

async function charger(page = 0) {
  const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ''))
  selectedIds.value = []
  await store.charger({ ...params, statut: 'ATTENTE_DIRECTION', page, size: 20, sort: 'budgetAlloue,desc' })
}
function basculer(id) {
  selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter((value) => value !== id) : [...selectedIds.value, id]
}
function basculerTout() { selectedIds.value = allSelected.value ? [] : store.demandes.map((item) => item.id) }

onMounted(async () => { await referentiel.chargerDepartements(); await charger() })
</script>

<template>
  <PageHeader title="Arbitrage des demandes" :breadcrumb="['Direction', 'Arbitrage']"
    subtitle="Examinez le besoin et l’impact budgétaire avant de statuer." />
  <AlertMessage :message="store.error" />
  <section class="card shadow-sm mb-4"><div class="card-body">
    <form class="row g-3 align-items-end" @submit.prevent="charger(0)">
      <div class="col-md-4"><label for="departement" class="form-label">Département</label><select id="departement" v-model="filters.departementId" class="form-select"><option value="">Tous</option><option v-for="d in referentiel.departements" :key="d.id" :value="d.id">{{ d.libelle }}</option></select></div>
      <div class="col-md-3"><label for="dateDebut" class="form-label">Date souhaitée du</label><input id="dateDebut" v-model="filters.dateDebut" type="date" class="form-control" /></div>
      <div class="col-md-3"><label for="dateFin" class="form-label">Au</label><input id="dateFin" v-model="filters.dateFin" type="date" class="form-control" /></div>
      <div class="col-md-2"><button class="btn btn-primary w-100">Filtrer</button></div>
    </form>
  </div></section>

  <section class="card shadow-sm"><div class="card-body">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
      <h2 class="h5 mb-0">Demandes en attente</h2>
      <div class="text-end"><div class="small text-body-secondary">Budget de la sélection ({{ selectedIds.length }})</div><strong class="fs-5">{{ formatMontant(selectedBudget) }}</strong></div>
    </div>
    <div class="table-responsive"><table class="table table-hover align-middle">
      <thead class="table-light"><tr><th><input type="checkbox" class="form-check-input" aria-label="Sélectionner la page" :checked="allSelected" @change="basculerTout" /></th><th>Poste / département</th><th>Manager</th><th>Effectif</th><th>Budget</th><th>Date souhaitée</th><th>Statut</th></tr></thead>
      <tbody>
        <tr v-if="store.loading"><td colspan="7" class="text-center py-4">Chargement…</td></tr>
        <tr v-else-if="!store.demandes.length"><td colspan="7" class="text-center text-body-secondary py-4">Aucune demande à arbitrer.</td></tr>
        <template v-else>
          <tr v-for="demande in store.demandes" :key="demande.id" class="cursor-pointer" tabindex="0" @click="router.push({ name: 'direction-demande-arbitrage', params: { id: demande.id } })" @keydown.enter="router.push({ name: 'direction-demande-arbitrage', params: { id: demande.id } })">
            <td @click.stop><input class="form-check-input" type="checkbox" :aria-label="`Sélectionner la demande ${demande.id}`" :checked="selectedIds.includes(demande.id)" @change="basculer(demande.id)" /></td>
            <td><strong>{{ demande.poste }}</strong><span class="d-block small text-body-secondary">{{ demande.departement }}</span></td>
            <td>{{ demande.manager }}</td><td class="fw-semibold">{{ demande.effectif }}</td><td class="fw-semibold">{{ formatMontant(demande.budget) }}</td><td>{{ formatDate(demande.dateSouhaitee) }}</td><td><StatusBadge type="demande" :value="demande.statut" /></td>
          </tr>
        </template>
      </tbody>
    </table></div>
    <div class="d-flex justify-content-between align-items-center">
      <span class="small text-body-secondary">{{ store.page.totalElements }} demande(s)</span>
      <div class="btn-group"><button class="btn btn-outline-secondary btn-sm" :disabled="store.page.number <= 0 || store.loading" @click="charger(store.page.number - 1)">Précédente</button><button class="btn btn-outline-secondary btn-sm" :disabled="store.page.number + 1 >= store.page.totalPages || store.loading" @click="charger(store.page.number + 1)">Suivante</button></div>
    </div>
  </div></section>
</template>
