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

onMounted(async () => {
  candidatures.value = await run(() => candidatureService.candidaturesParOffre(route.params.id)) || []
})
</script>

<template>
  <PageHeader title="Candidats de l’offre" :breadcrumb="['Espace RH', 'Offres d’emploi', 'Candidats']"
    subtitle="Consultez les candidats qui ont postulé à cette offre et le statut de leur candidature." />
  <RouterLink class="btn btn-link px-0 mb-3" :to="{ name: 'rh-offres' }"><i class="bi bi-arrow-left me-1" aria-hidden="true" />Retour aux offres</RouterLink>
  <AlertMessage :message="error" />
  <section class="card shadow-sm">
    <div v-if="loading" class="text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" />Chargement…</div>
    <div v-else-if="!candidatures.length" class="card-body text-center text-body-secondary py-5">Aucun candidat n’a postulé à cette offre.</div>
    <div v-else class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light"><tr><th>Candidat</th><th>E-mail</th><th>Date de candidature</th><th>Salaire souhaité</th><th>Statut</th><th></th></tr></thead>
        <tbody>
          <tr v-for="candidature in candidatures" :key="candidature.id">
            <td>{{ candidature.candidat }}</td><td>{{ candidature.email }}</td><td>{{ formatDate(candidature.datePostulation) }}</td>
            <td>{{ formatMontant(candidature.salairePropose ?? candidature.salaireReference) }}<span v-if="candidature.salairePropose == null && candidature.salaireReference != null" class="d-block small text-body-secondary">Salaire de référence accepté</span></td>
            <td><StatusBadge type="candidature" :value="candidature.statut" /></td>
            <td><RouterLink class="btn btn-sm btn-outline-primary" :to="{ name: 'rh-decision', query: { candidatureId: candidature.id } }">Voir le dossier</RouterLink></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
