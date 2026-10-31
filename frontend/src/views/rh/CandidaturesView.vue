<script setup>
import { onMounted } from 'vue'
import { useCandidaturesStore } from '@/stores/candidatures'
import { useFormatters } from '@/composables/useFormatters'
import StatCard from '@/components/ui/StatCard.vue'
import DataTable from '@/components/ui/DataTable.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useCandidaturesStore()
const { formatDate, formatMontant } = useFormatters()

const columns = [
  { key: 'candidat', label: 'Candidat' },
  { key: 'poste', label: 'Poste demandé' },
  { key: 'datePostulation', label: 'Date de réception' },
  { key: 'salairePropose', label: 'Salaire souhaité' },
  { key: 'statut', label: 'Statut' },
  { key: 'actions', label: 'Action' },
]

onMounted(store.charger)
</script>

<template>
  <div class="d-flex justify-content-between align-items-start gap-3 mb-4">
    <div>
      <h1 class="h3">Suivi des candidatures</h1>
      <p class="text-body-secondary mb-0">Consultez les profils et suivez les recrutements.</p>
    </div>
    <span class="badge text-bg-light border fs-6 fw-normal">
      <i class="bi bi-person-circle me-1" aria-hidden="true"></i>Responsable RH
    </span>
  </div>

  <AlertMessage :message="store.error" />

  <div class="row row-cols-1 row-cols-md-3 g-3 mb-4">
    <div class="col"><StatCard label="Candidatures reçues" :value="store.stats.recues" /></div>
    <div class="col"><StatCard label="Entretiens à organiser" :value="store.stats.entretiensAOrganiser" /></div>
    <div class="col"><StatCard label="Candidats retenus" :value="store.stats.retenues" /></div>
  </div>

  <section class="card shadow-sm mb-4">
    <div class="card-body">
      <h2 class="h5">Liste des candidatures</h2>
      <p class="text-body-secondary">Retrouvez ici les dossiers transmis par les candidats.</p>

      <DataTable :columns="columns" :rows="store.candidatures" :loading="store.loading"
                 empty-message="Aucune candidature enregistrée pour le moment.">
        <template #cell-datePostulation="{ value }">{{ formatDate(value) }}</template>
        <template #cell-salairePropose="{ row }">{{ formatMontant(row.salairePropose ?? row.salaireReference) }}<span v-if="row.salairePropose == null && row.salaireReference != null" class="d-block small text-body-secondary">Salaire de référence accepté</span></template>
        <template #cell-statut="{ value }"><StatusBadge type="candidature" :value="value" /></template>
        <template #cell-actions="{ row }">
          <div class="d-flex gap-2">
            <RouterLink class="btn btn-sm btn-outline-secondary" :to="{ name: 'candidature-detail', params: { id: row.id } }">Détail</RouterLink>
            <RouterLink class="btn btn-sm btn-outline-primary"
                        :to="{ name: 'rh-decision', query: { candidatureId: row.id } }">Décision</RouterLink>
            <RouterLink v-if="row.statut === 'RETENUE'" class="btn btn-sm btn-outline-success"
                        :to="{ name: 'rh-confirmation', query: { candidatureId: row.id } }">Confirmer</RouterLink>
          </div>
        </template>
      </DataTable>
    </div>
  </section>

  <section class="card shadow-sm mb-4" id="entretiens">
    <div class="card-body">
      <h2 class="h5">Entretiens</h2>
      <p class="text-body-secondary mb-0">
        Les entretiens pourront être planifiés et suivis depuis cet espace.
      </p>
    </div>
  </section>

  <section class="card shadow-sm" id="evaluations">
    <div class="card-body">
      <h2 class="h5">Évaluations</h2>
      <p class="text-body-secondary mb-0">
        Les évaluations permettront de consigner les observations et les résultats des entretiens.
      </p>
    </div>
  </section>
</template>
