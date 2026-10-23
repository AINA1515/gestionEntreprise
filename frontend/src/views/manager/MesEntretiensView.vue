<script setup>
import { onMounted, ref } from 'vue'
import { candidatureService } from '@/services/candidatureService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import { TYPES_CONTRAT } from '@/composables/useLabels'

const entretiens = ref([])
const aPlanifier = ref([])
const dates = ref({})
const entretienChoisi = ref(null)
const form = ref({ decision: 'FAVORABLE', observations: '' })
const { loading, error, run } = useAsyncAction()
const { formatDate } = useFormatters()

async function charger() {
  aPlanifier.value = await run(() => candidatureService.candidaturesAPlanifier()) || []
  entretiens.value = await run(() => candidatureService.entretiens()) || []
}
async function planifier(candidature) {
  const result = await run(() => candidatureService.planifierEntretien(candidature.id, { dateEntretien: dates.value[candidature.id], typeEntretien: 'RH' }))
  if (result) await charger()
}
async function evaluer(entretien) {
  entretienChoisi.value = entretien
  form.value = { decision: 'FAVORABLE', observations: '', typeContrat: '', salaireBrut: '', dateDebut: '', dateLimiteReponse: '' }
}
async function enregistrer() {
  const result = await run(() => candidatureService.evaluer(entretienChoisi.value.candidatureId, form.value))
  if (result) { entretienChoisi.value = null; await charger() }
}
onMounted(charger)
</script>

<template>
  <PageHeader title="Mes entretiens" :breadcrumb="['Espace Manager', 'Entretiens']" subtitle="Consultez les entretiens planifiés et enregistrez votre évaluation du candidat." />
  <AlertMessage :message="error" />
  <section v-if="aPlanifier.length" class="card shadow-sm mb-4">
    <div class="card-header"><h2 class="h5 mb-1">Candidats transmis par les RH</h2><p class="small text-body-secondary mb-0">Choisissez une date pour organiser chaque entretien.</p></div>
    <div class="table-responsive"><table class="table align-middle mb-0"><thead class="table-light"><tr><th>Candidat</th><th>Offre</th><th>Transmission</th><th>Date de l’entretien</th><th></th></tr></thead>
      <tbody><tr v-for="c in aPlanifier" :key="c.id"><td>{{ c.candidat }}</td><td>{{ c.poste }}</td><td>{{ formatDate(c.datePostulation) }}</td><td><input v-model="dates[c.id]" type="date" class="form-control form-control-sm" :min="new Date().toISOString().slice(0, 10)" :aria-label="`Date de l'entretien pour ${c.candidat}`" /></td><td><button class="btn btn-sm btn-success" :disabled="loading || !dates[c.id]" @click="planifier(c)">Planifier</button></td></tr></tbody>
    </table></div>
  </section>
  <section class="card shadow-sm">
    <div v-if="loading && !entretiens.length" class="text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2"/>Chargement…</div>
    <div v-else-if="!entretiens.length" class="card-body text-center text-body-secondary py-5">Aucun entretien ne vous est assigné.</div>
    <div v-else class="table-responsive"><table class="table align-middle mb-0">
      <thead class="table-light"><tr><th>Candidat</th><th>Offre</th><th>Date</th><th>Évaluation</th><th></th></tr></thead>
      <tbody><tr v-for="entretien in entretiens" :key="entretien.id"><td>{{ entretien.candidat }}</td><td>{{ entretien.poste }}</td><td>{{ formatDate(entretien.dateEntretien) }}</td>
        <td>{{ entretien.decision || 'À évaluer' }}<small v-if="entretien.commentaires" class="d-block text-body-secondary">{{ entretien.commentaires }}</small></td>
        <td><button v-if="!entretien.decision" class="btn btn-sm btn-primary" @click="evaluer(entretien)">Évaluer</button></td>
      </tr></tbody>
    </table></div>
  </section>
  <div v-if="entretienChoisi" class="modal d-block" tabindex="-1" role="dialog" aria-modal="true"><div class="modal-dialog"><div class="modal-content">
    <form @submit.prevent="enregistrer"><div class="modal-header"><h2 class="modal-title h5">Évaluer {{ entretienChoisi.candidat }}</h2><button type="button" class="btn-close" aria-label="Fermer" @click="entretienChoisi = null" /></div>
      <div class="modal-body"><AlertMessage :message="error"/><label class="form-label" for="decision">Décision après entretien</label><select id="decision" v-model="form.decision" class="form-select mb-3"><option value="FAVORABLE">Favorable — envoyer une proposition</option><option value="DEFAVORABLE">Défavorable — refuser</option><option value="RESERVE">À revoir</option></select>
        <template v-if="form.decision === 'FAVORABLE'"><label class="form-label" for="type-contrat">Type de contrat proposé</label><select id="type-contrat" v-model="form.typeContrat" class="form-select mb-3" required><option value="" disabled>Choisir un contrat</option><option v-for="type in TYPES_CONTRAT" :key="type.value" :value="type.value">{{ type.label }}</option></select>
          <label class="form-label" for="salaire-brut">Salaire brut proposé (facultatif)</label><input id="salaire-brut" v-model="form.salaireBrut" class="form-control mb-3" type="number" min="0" step="0.01" />
          <label class="form-label" for="date-debut">Date de début souhaitée (facultatif)</label><input id="date-debut" v-model="form.dateDebut" class="form-control mb-3" type="date" />
          <label class="form-label" for="date-limite">Date limite de réponse</label><input id="date-limite" v-model="form.dateLimiteReponse" class="form-control mb-3" type="date" :min="new Date().toISOString().slice(0, 10)" required /></template>
        <label class="form-label" for="observations">Remarque d’entretien</label><textarea id="observations" v-model="form.observations" class="form-control" rows="4" required /></div>
      <div class="modal-footer"><button type="button" class="btn btn-outline-secondary" @click="entretienChoisi = null">Annuler</button><button class="btn btn-primary" :disabled="loading">Enregistrer l’évaluation</button></div>
    </form>
  </div></div></div>
</template>
