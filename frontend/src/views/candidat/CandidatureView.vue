<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { candidatureService } from '@/services/candidatureService'
import { cvService } from '@/services/cvService'
import { offreService } from '@/services/offreService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

const route = useRoute()
const router = useRouter()
const { loading, error, run } = useAsyncAction()
const { formatDate, formatMontant } = useFormatters()
const candidatures = ref([])
const offreChoisie = ref(null)
const saisirSalaire = ref(false)
const salairePropose = ref('')
const offreId = computed(() => route.query.offreId ? Number(route.query.offreId) : null)

async function charger() {
  candidatures.value = await run(() => candidatureService.candidaturesDuCandidat()) || []
}

async function annuler(candidature) {
  if (!window.confirm(`Annuler votre candidature à « ${candidature.poste} » ?`)) return
  const result = await run(() => candidatureService.annulerCandidature(candidature.id))
  if (result !== undefined) await charger()
}

function peutAnnuler(candidature) {
  return !['ANNULEE', 'REJETEE', 'RETENUE'].includes(candidature.statut)
}

async function postuler(id) {
  if (!id) return
  const status = await run(() => cvService.statut())
  if (!status?.existe) {
    await router.push({ name: 'candidat-cv', query: { offreId: String(id) } })
    return
  }
  offreChoisie.value = await run(() => offreService.detail(id))
  if (offreChoisie.value) saisirSalaire.value = true
}

async function confirmerPostulation() {
  const result = await run(() => candidatureService.deposer(offreId.value,
    salairePropose.value === '' ? null : Number(salairePropose.value)))
  if (result) {
    saisirSalaire.value = false
    await router.replace({ name: 'candidat-candidature' })
    await charger()
  }
}

watch(offreId, (id) => { if (id) postuler(id) }, { immediate: true })
onMounted(() => { if (!offreId.value) charger() })
</script>

<template>
  <PageHeader title="Mes candidatures" :breadcrumb="['Espace candidat', 'Mes candidatures']"
    subtitle="Retrouvez les offres auxquelles vous avez postulé et le suivi de vos candidatures." />
  <AlertMessage :message="error" />
  <div v-if="offreId && !saisirSalaire" class="text-center py-5" role="status">
    <span class="spinner-border spinner-border-sm me-2" />{{ loading ? 'Vérification de votre CV…' : 'Préparation de votre candidature…' }}
  </div>
  <section v-else-if="offreId && saisirSalaire" class="card shadow-sm">
    <div class="card-body p-4">
      <h2 class="h4">Postuler à {{ offreChoisie?.titre }}</h2>
      <p v-if="offreChoisie?.salaireReference != null" class="text-body-secondary">
        Budget estimé, utilisé comme salaire de référence par le manager : <strong>{{ formatMontant(offreChoisie.salaireReference) }}</strong>.
        Laissez le champ vide pour accepter ce montant.
      </p>
      <p v-else class="text-body-secondary">Vous pouvez indiquer le salaire qui vous convient. Laissez le champ vide si vous n’avez pas de préférence salariale.</p>
      <AlertMessage :message="error" />
      <form @submit.prevent="confirmerPostulation">
        <label for="salaire-propose" class="form-label">Votre salaire souhaité (facultatif)</label>
        <div class="input-group mb-3"><input id="salaire-propose" v-model="salairePropose" type="number" min="0" step="0.01" class="form-control" placeholder="Laisser vide pour accepter le salaire de référence" /><span class="input-group-text">MGA</span></div>
        <div class="d-flex gap-2"><button class="btn btn-primary" type="submit" :disabled="loading">{{ loading ? 'Envoi…' : 'Confirmer ma candidature' }}</button>
          <RouterLink class="btn btn-outline-secondary" :to="{ name: 'offres-publiques' }">Annuler</RouterLink></div>
      </form>
    </div>
  </section>
  <section v-else class="card shadow-sm">
    <div v-if="loading" class="text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" />Chargement…</div>
    <div v-else-if="!candidatures.length" class="card-body text-center py-5">
      <i class="bi bi-briefcase display-5 text-body-secondary" aria-hidden="true" />
      <h2 class="h5 mt-3">Vous n’avez pas encore postulé</h2>
      <p class="text-body-secondary">Consultez les offres disponibles et postulez en un clic.</p>
      <RouterLink class="btn btn-primary" :to="{ name: 'offres-publiques' }">Voir les offres d’emploi</RouterLink>
    </div>
    <div v-else class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light"><tr><th>Offre d’emploi</th><th>Date de candidature</th><th>Salaire souhaité</th><th>Statut</th><th>Actions</th></tr></thead>
        <tbody>
          <tr v-for="candidature in candidatures" :key="candidature.id">
            <td>{{ candidature.poste }}</td><td>{{ formatDate(candidature.datePostulation) }}</td>
            <td>{{ formatMontant(candidature.salairePropose ?? candidature.salaireReference) }}<span v-if="candidature.salairePropose == null && candidature.salaireReference != null" class="d-block small text-body-secondary">Salaire de référence accepté</span></td>
            <td><StatusBadge type="candidature" :value="candidature.statut" /></td>
            <td class="d-flex gap-2"><RouterLink v-if="candidature.offreId" class="btn btn-sm btn-outline-primary" :to="{ name: 'offre-detail', params: { id: candidature.offreId } }">Voir l’offre</RouterLink>
              <button v-if="peutAnnuler(candidature)" class="btn btn-sm btn-outline-danger" :disabled="loading" @click="annuler(candidature)">Annuler</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
