<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCandidaturesStore } from '@/stores/candidatures'
import { candidatureService } from '@/services/candidatureService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { TYPES_CONTRAT } from '@/composables/useLabels'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const router = useRouter()
const candidatures = useCandidaturesStore()
const { loading, error, run } = useAsyncAction()

const form = reactive({
  candidatureId: route.query.candidatureId ?? '',
  typeContrat: '',
  salaireBrut: '',
  dateDebut: '',
  dateFin: '',
  dateLimiteReponse: '',
})

const retenues = computed(() => candidatures.candidatures.filter((c) => c.statut === 'RETENUE'))
const candidature = computed(() => candidatures.trouver(form.candidatureId))

onMounted(() => { if (!candidatures.candidatures.length) candidatures.charger() })

async function confirmer() {
  const result = await run(() => candidatureService.confirmerRecrutement({ ...form }))
  if (!error.value && result !== undefined) router.push({ name: 'rh-candidatures' })
}
</script>

<template>
  <div class="col-lg-8 mx-auto">
    <section class="card shadow-sm">
      <div class="card-body p-4">
        <h1 class="h3">Confirmation de recrutement</h1>
        <p class="text-body-secondary">
          Préparez la confirmation du candidat retenu et indiquez la date prévue de sa prise de fonction.
        </p>

        <div class="alert alert-info mb-4" role="alert">
          <i class="bi bi-info-circle me-2" aria-hidden="true"></i>
          Cette page concerne uniquement les candidats dont la décision de recrutement est favorable.
        </div>

        <AlertMessage :message="error" />
        <AlertMessage :message="candidatures.error" />

        <form @submit.prevent="confirmer">
          <FormField id="candidature" label="Candidat retenu">
            <select id="candidature" v-model="form.candidatureId" class="form-select" required>
              <option value="" disabled>-- Sélectionner un candidat --</option>
              <option v-for="c in retenues" :key="c.id" :value="String(c.id)">{{ c.candidat }} — {{ c.poste }}</option>
            </select>
          </FormField>

          <p v-if="candidature" class="small text-body-secondary">
            E-mail : <strong>{{ candidature.email ?? '—' }}</strong> · Poste obtenu: <strong>{{ candidature.poste }}</strong>
          </p>

          <FormField id="contrat" label="Type de contrat">
            <select id="contrat" v-model="form.typeContrat" class="form-select" required>
              <option value="" disabled>-- Sélectionner le contrat --</option>
              <option v-for="t in TYPES_CONTRAT" :key="t.value" :value="t.value">{{ t.label }}</option>
            </select>
          </FormField>

          <FormField id="salaireBrut" label="Salaire brut proposé (facultatif)">
            <input id="salaireBrut" v-model="form.salaireBrut" type="number" min="0" step="0.01" class="form-control" />
          </FormField>

          <FormField id="debut" label="Date de début du travail">
            <input id="debut" v-model="form.dateDebut" type="date" class="form-control" />
          </FormField>

          <FormField v-if="form.typeContrat !== 'CDI'" id="dateFin" label="Date de fin du contrat">
            <input id="dateFin" v-model="form.dateFin" type="date" class="form-control" :min="form.dateDebut || new Date().toISOString().slice(0, 10)" required />
          </FormField>

          <FormField id="dateLimiteReponse" label="Date limite de réponse du candidat">
            <input id="dateLimiteReponse" v-model="form.dateLimiteReponse" type="date" class="form-control" :min="new Date().toISOString().slice(0, 10)" required />
          </FormField>

          <div class="d-flex flex-wrap gap-2">
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? 'Envoi…' : 'Confirmer le recrutement' }}
            </button>
            <RouterLink class="btn btn-outline-secondary" :to="{ name: 'rh-candidatures' }">Retour</RouterLink>
          </div>
        </form>
      </div>
    </section>
  </div>
</template>
