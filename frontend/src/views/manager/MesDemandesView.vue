<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTable from '@/components/ui/DataTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useDemandesStore()
const router = useRouter()
const { formatDate, formatMontant } = useFormatters()
const filters = reactive({ statut: '', dateDebut: '', dateFin: '', texte: '', sort: 'dateCreation,desc' })
const columns = [
  { key: 'poste', label: 'Poste demandé' },
  { key: 'niveauFormationLibelle', label: 'Niveau de formation' },
  { key: 'dateDemande', label: 'Créée le' },
  { key: 'dateSouhaitee', label: 'Date souhaitée' },
  { key: 'effectif', label: 'Effectif' },
  { key: 'statut', label: 'Statut' },
]

async function charger(page = 0) {
  const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ''))
  await store.charger({ ...params, page, size: 10 })
}
function resetFiltres() {
  Object.assign(filters, { statut: '', dateDebut: '', dateFin: '', texte: '', sort: 'dateCreation,desc' })
  charger(0)
}

onMounted(() => charger())
</script>

<template>
  <PageHeader title="Mes demandes" :breadcrumb="['Espace Manager', 'Mes demandes']"
    subtitle="Consultez le suivi de vos demandes de personnel." />

  <AlertMessage :message="store.error" />
  <section class="card shadow-sm">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 mb-0">Historique des demandes</h2>
        <RouterLink class="btn btn-primary btn-sm" :to="{ name: 'manager-nouvelle-demande' }">Nouvelle demande</RouterLink>
      </div>
      <form class="row g-2 align-items-end mb-4" @submit.prevent="charger(0)">
        <div class="col-sm-6 col-lg-2">
          <label for="filtre-statut" class="form-label">Statut</label>
          <select id="filtre-statut" v-model="filters.statut" class="form-select">
            <option value="">Tous</option><option value="BROUILLON">Brouillon</option>
            <option value="ATTENTE_RH">En attente RH</option><option value="ATTENTE_DIRECTION">En attente Direction</option>
            <option value="VALIDEE">Validée</option><option value="REFUSEE">Refusée</option>
          </select>
        </div>
        <div class="col-sm-6 col-lg-2"><label for="date-debut" class="form-label">Du</label><input id="date-debut" v-model="filters.dateDebut" type="date" class="form-control" /></div>
        <div class="col-sm-6 col-lg-2"><label for="date-fin" class="form-label">Au</label><input id="date-fin" v-model="filters.dateFin" type="date" class="form-control" /></div>
        <div class="col-sm-6 col-lg-3"><label for="texte" class="form-label">Recherche</label><input id="texte" v-model.trim="filters.texte" class="form-control" placeholder="Motif, poste…" /></div>
        <div class="col-sm-6 col-lg-2">
          <label for="tri" class="form-label">Trier par</label>
          <select id="tri" v-model="filters.sort" class="form-select">
            <option value="dateCreation,desc">Plus récentes</option><option value="dateCreation,asc">Plus anciennes</option>
            <option value="dateSouhaitee,asc">Date souhaitée croissante</option><option value="dateSouhaitee,desc">Date souhaitée décroissante</option>
          </select>
        </div>
        <div class="col-lg-1 d-flex gap-2"><button class="btn btn-outline-primary" type="submit">Filtrer</button></div>
        <div class="col-12"><button class="btn btn-link btn-sm p-0" type="button" @click="resetFiltres">Effacer les filtres</button></div>
      </form>

      <DataTable :columns="columns" :rows="store.demandes" :loading="store.loading" row-clickable
        empty-message="Aucune demande ne correspond aux critères."
        @row-click="(row) => router.push({ name: 'manager-demande-detail', params: { id: row.id } })">
        <template #cell-dateDemande="{ value }">{{ formatDate(value) }}</template>
        <template #cell-dateSouhaitee="{ value }">{{ formatDate(value) }}</template>
        <template #cell-statut="{ value }"><StatusBadge type="demande" :value="value" /></template>
      </DataTable>
      <div class="d-flex justify-content-between align-items-center mt-3">
        <span class="small text-body-secondary">{{ store.page.totalElements }} demande(s) · page {{ store.page.number + 1 }} sur {{ Math.max(store.page.totalPages, 1) }}</span>
        <div class="btn-group" aria-label="Pagination">
          <button class="btn btn-outline-secondary btn-sm" :disabled="store.page.number <= 0 || store.loading" @click="charger(store.page.number - 1)">Précédente</button>
          <button class="btn btn-outline-secondary btn-sm" :disabled="store.page.number + 1 >= store.page.totalPages || store.loading" @click="charger(store.page.number + 1)">Suivante</button>
        </div>
      </div>
    </div>
  </section>
</template>
