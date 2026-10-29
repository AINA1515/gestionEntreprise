<script setup>
import { onMounted } from 'vue'
import { useDemandesStore } from '@/stores/demandes'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import DataTable from '@/components/ui/DataTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import { useRouter } from 'vue-router'

const store = useDemandesStore()
const router = useRouter()
const { formatDate, formatMontant } = useFormatters()
const statuts = [
  ['BROUILLON', 'Brouillons', 'file-earmark-text'],
  ['ATTENTE_RH', 'En attente RH', 'people'],
  ['ATTENTE_DIRECTION', 'En attente Direction', 'building'],
  ['VALIDEE', 'Validées', 'check-circle'],
  ['REFUSEE', 'Refusées', 'x-circle'],
]

const columns = [
  { key: 'poste', label: 'Poste demandé' },
  { key: 'niveauFormationLibelle', label: 'Niveau de formation' },
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

  <div class="row g-3 mb-4">
    <div v-for="[code, label, icon] in statuts" :key="code" class="col-sm-6 col-xl">
      <StatCard :label="label" :value="store.parStatut?.[code] ?? 0" :icon="icon" />
    </div>
  </div>

  <section class="card border-primary-subtle shadow-sm mb-4">
    <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
      <div><h2 class="h5 mb-1">Intégration des nouveaux salariés</h2><p class="text-body-secondary mb-0">Choisissez un salarié et attribuez chaque tâche d’intégration à un membre de votre équipe.</p></div>
      <RouterLink class="btn btn-primary" :to="{ name: 'manager-onboarding' }"><i class="bi bi-person-check me-2" aria-hidden="true"></i>Attribuer les tâches</RouterLink>
    </div>
  </section>

  <section class="card shadow-sm">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 mb-0">Mes demandes récentes</h2>
        <RouterLink :to="{ name: 'manager-demandes' }" class="small fw-semibold text-decoration-none">
          Voir mes demandes
        </RouterLink>
      </div>

      <DataTable :columns="columns" :rows="store.recentes" :loading="store.loading" row-clickable
                 empty-message="Aucune demande à afficher pour le moment."
                 @row-click="(row) => router.push({ name: 'manager-demande-detail', params: { id: row.id } })">
        <template #cell-dateDemande="{ value }">{{ formatDate(value) }}</template>
        <template #cell-statut="{ value }"><StatusBadge type="demande" :value="value" /></template>
      </DataTable>

      <RouterLink class="btn btn-primary mt-3" :to="{ name: 'manager-nouvelle-demande' }">
        <i class="bi bi-plus-lg me-2" aria-hidden="true"></i>Nouvelle demande
      </RouterLink>
    </div>
  </section>
</template>
