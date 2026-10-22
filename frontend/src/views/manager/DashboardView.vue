<script setup>
import { onMounted } from 'vue'
import { useDemandesStore } from '@/stores/demandes'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
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

onMounted(store.chargerDashboard)
</script>

<template>
  <PageHeader title="Tableau de bord" :breadcrumb="['Espace Manager', 'Accueil']"
              subtitle="Retrouvez ici le suivi de vos demandes de personnel." />

  <AlertMessage :message="store.error" />

  <div class="row mb-4">
    <div class="col-md-6 col-xl-4">
      <StatCard label="Nombre de demandes" :value="store.nombreDemandes" icon="card-list" />
    </div>
  </div>

  <section class="card shadow-sm">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 mb-0">Mes demandes récentes</h2>
        <RouterLink :to="{ name: 'manager-demandes' }" class="small fw-semibold text-decoration-none">
          Voir mes demandes
        </RouterLink>
      </div>

      <DataTable :columns="columns" :rows="store.recentes" :loading="store.loading"
                 empty-message="Aucune demande à afficher pour le moment.">
        <template #cell-dateDemande="{ value }">{{ formatDate(value) }}</template>
        <template #cell-statut="{ value }"><StatusBadge type="demande" :value="value" /></template>
      </DataTable>

      <RouterLink class="btn btn-primary mt-3" :to="{ name: 'manager-nouvelle-demande' }">
        <i class="bi bi-plus-lg me-2" aria-hidden="true"></i>Nouvelle demande
      </RouterLink>
    </div>
  </section>
</template>
