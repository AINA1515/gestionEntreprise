<script setup>
import { onMounted } from 'vue'
import { useDemandesStore } from '@/stores/demandes'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTable from '@/components/ui/DataTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useDemandesStore()
const { formatDate } = useFormatters()

const columns = [
  { key: 'poste', label: 'Poste demandé' },
  { key: 'dateDemande', label: 'Date de la demande' },
  { key: 'effectif', label: 'Effectif' },
  { key: 'statut', label: 'Statut' },
]

onMounted(store.charger)
</script>

<template>
  <PageHeader title="Mes demandes" :breadcrumb="['Espace Manager', 'Mes demandes']"
              subtitle="Consultez le suivi de vos demandes de personnel." />

  <AlertMessage :message="store.error" />

  <section class="card shadow-sm">
    <div class="card-body">
      <h2 class="h5 mb-3">Historique des demandes</h2>

      <DataTable :columns="columns" :rows="store.demandes" :loading="store.loading"
                 empty-message="Aucune demande enregistrée pour le moment.">
        <template #cell-dateDemande="{ value }">{{ formatDate(value) }}</template>
        <template #cell-statut="{ value }"><StatusBadge type="demande" :value="value" /></template>
      </DataTable>

      <RouterLink class="btn btn-primary mt-4" :to="{ name: 'manager-nouvelle-demande' }">
        <i class="bi bi-plus-lg me-2" aria-hidden="true"></i>Créer une demande
      </RouterLink>
    </div>
  </section>
</template>
