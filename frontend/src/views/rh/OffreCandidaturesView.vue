<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { candidatureService } from '@/services/candidatureService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

const route = useRoute()
const { loading, error, run } = useAsyncAction()
const { formatDate, formatMontant } = useFormatters()
const candidatures = ref([])
const preselectionnees = ref([])

function partagerReponse(liste) {
  candidatures.value = liste || []
  preselectionnees.value = candidatures.value.filter((candidature) => candidature.preselectionnee)
}

onMounted(async () => {
  partagerReponse(await run(() => candidatureService.candidaturesParOffre(route.params.id)))
})
</script>

<template>
  <PageHeader title="Candidats de l’offre" :breadcrumb="['Espace RH', 'Offres d’emploi', 'Candidats']"
    subtitle="Consultez les candidats qui ont postulé à cette offre et le statut de leur candidature." />
  <RouterLink class="btn btn-link px-0 mb-3" :to="{ name: 'rh-offres' }"><i class="bi bi-arrow-left me-1" aria-hidden="true" />Retour aux offres</RouterLink>
  <AlertMessage :message="error" />
  <section v-if="!loading && preselectionnees.length" class="card shadow-sm mb-4">
    <div class="card-header bg-success-subtle">
      <h2 class="h5 mb-1">Candidats présélectionnés ({{ preselectionnees.length }})</h2>
      <p class="small mb-0">Niveau de formation au moins égal au niveau demandé et salaire souhaité inférieur ou égal au budget majoré de 10 %.</p>
    </div>
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light"><tr><th>Candidat</th><th>Niveau obtenu</th><th>Niveau requis</th><th>Salaire souhaité</th><th>Plafond (+10 %)</th><th></th></tr></thead>
        <tbody>
          <tr v-for="candidature in preselectionnees" :key="candidature.id">
            <td>{{ candidature.candidat }}<span class="d-block small text-body-secondary">{{ candidature.email }}</span></td>
            <td>{{ candidature.niveauFormationCandidat || 'Non renseigné' }}</td>
            <td>{{ candidature.niveauFormationRequis || 'Aucun niveau imposé' }}</td>
            <td>{{ formatMontant(candidature.salairePropose ?? candidature.salaireReference) }}</td>
            <td>{{ formatMontant(candidature.plafondSalairePreselection) }}</td>
            <td><RouterLink class="btn btn-sm btn-outline-primary" :to="{ name: 'rh-decision', query: { candidatureId: candidature.id } }">Voir le dossier</RouterLink></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
  <section class="card shadow-sm">
    <div class="card-header"><h2 class="h5 mb-0">Toutes les candidatures ({{ candidatures.length }})</h2></div>
    <div v-if="loading" class="text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" />Chargement…</div>
    <div v-else-if="!candidatures.length" class="card-body text-center text-body-secondary py-5">Aucun candidat n’a postulé à cette offre.</div>
    <div v-else class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light"><tr><th>Candidat</th><th>E-mail</th><th>Date de candidature</th><th>Niveau de formation</th><th>Salaire souhaité</th><th>Présélection</th><th>Statut</th><th></th></tr></thead>
        <tbody>
          <tr v-for="candidature in candidatures" :key="candidature.id">
            <td>{{ candidature.candidat }}</td><td>{{ candidature.email }}</td><td>{{ formatDate(candidature.datePostulation) }}</td>
            <td>{{ candidature.niveauFormationCandidat || 'Non renseigné' }}</td>
            <td>{{ formatMontant(candidature.salairePropose ?? candidature.salaireReference) }}<span v-if="candidature.salairePropose == null && candidature.salaireReference != null" class="d-block small text-body-secondary">Salaire de référence accepté</span></td>
            <td><span class="badge" :class="candidature.preselectionnee ? 'text-bg-success' : 'text-bg-secondary'">{{ candidature.preselectionnee ? 'Retenue' : 'Non retenue' }}</span><span v-if="!candidature.preselectionnee" class="d-block small text-body-secondary">{{ candidature.motifPreselection }}</span></td>
            <td><StatusBadge type="candidature" :value="candidature.statut" /></td>
            <td><RouterLink class="btn btn-sm btn-outline-primary" :to="{ name: 'rh-decision', query: { candidatureId: candidature.id } }">Voir le dossier</RouterLink></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
