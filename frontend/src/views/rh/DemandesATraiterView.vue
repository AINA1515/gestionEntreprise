<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useReferentielStore } from '@/stores/referentiel'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTable from '@/components/ui/DataTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useDemandesStore()
const referentiel = useReferentielStore()
const router = useRouter()
const { formatDate } = useFormatters()
const filters = reactive({ departementId: '', dateDebut: '', dateFin: '', managerId: '' })
const columns = [
  { key: 'poste', label: 'Poste' }, { key: 'departement', label: 'Département' },
  { key: 'manager', label: 'Manager' }, { key: 'dateSouhaitee', label: 'Date souhaitée' },
  { key: 'effectif', label: 'Effectif' }, { key: 'statut', label: 'Statut' },
]

async function charger(page = 0) {
  const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ''))
  await store.charger({ ...params, statut: 'ATTENTE_RH', page, size: 20, sort: 'dateCreation,asc' })
}
onMounted(async () => { await referentiel.chargerDepartements(); await charger() })
</script>

<template>
  <PageHeader title="Demandes à vérifier" :breadcrumb="['Espace RH', 'Demandes à traiter']"
    subtitle="Vérifiez la conformité des demandes avant leur transmission à la Direction." />
  <AlertMessage :message="store.error" />
  <section class="card shadow-sm">
    <div class="card-body">
      <form class="row g-3 align-items-end mb-4" @submit.prevent="charger(0)">
        <div class="col-md-4"><label for="departement" class="form-label">Département</label>
          <select id="departement" v-model="filters.departementId" class="form-select"><option value="">Tous</option><option v-for="d in referentiel.departements" :key="d.id" :value="d.id">{{ d.libelle }}</option></select>
        </div>
        <div class="col-md-2"><label for="dateDebut" class="form-label">Souhaitée du</label><input id="dateDebut" v-model="filters.dateDebut" type="date" class="form-control" /></div>
        <div class="col-md-2"><label for="dateFin" class="form-label">Au</label><input id="dateFin" v-model="filters.dateFin" type="date" class="form-control" /></div>
        <div class="col-md-2"><label for="managerId" class="form-label">ID manager</label><input id="managerId" v-model="filters.managerId" type="number" min="1" class="form-control" /></div>
        <div class="col-md-2"><button class="btn btn-primary w-100" type="submit">Filtrer</button></div>
      </form>
      <DataTable :columns="columns" :rows="store.demandes" :loading="store.loading" row-clickable
        empty-message="Aucune demande en attente de vérification RH."
        @row-click="(row) => router.push({ name: 'rh-demande-revue', params: { id: row.id } })">
        <template #cell-dateSouhaitee="{ value }">{{ formatDate(value) }}</template>
        <template #cell-statut="{ value }"><StatusBadge type="demande" :value="value" /></template>
      </DataTable>
      <div class="d-flex justify-content-between align-items-center mt-3">
        <span class="small text-body-secondary">{{ store.page.totalElements }} demande(s)</span>
        <div class="btn-group"><button class="btn btn-outline-secondary btn-sm" :disabled="store.page.number <= 0 || store.loading" @click="charger(store.page.number - 1)">Précédente</button>
          <button class="btn btn-outline-secondary btn-sm" :disabled="store.page.number + 1 >= store.page.totalPages || store.loading" @click="charger(store.page.number + 1)">Suivante</button></div>
      </div>
    </div>
  </section>
</template>
