<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCandidaturesStore } from '@/stores/candidatures'
import { candidatureService } from '@/services/candidatureService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { DECISIONS } from '@/composables/useLabels'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const router = useRouter()
const candidatures = useCandidaturesStore()
const { loading, error, run } = useAsyncAction()

const form = reactive({
  candidatureId: route.query.candidatureId ?? '',
  decision: '',
  observations: '',
})

const candidature = computed(() => candidatures.trouver(form.candidatureId))

onMounted(() => { if (!candidatures.candidatures.length) candidatures.charger() })

async function enregistrer() {
  const result = await run(() => candidatureService.enregistrerDecision({ ...form }))
  if (!error.value && result !== undefined) {
    // Une décision favorable mène directement à la confirmation de recrutement.
    if (form.decision === 'FAVORABLE') {
      router.push({ name: 'rh-confirmation', query: { candidatureId: form.candidatureId } })
    } else {
      router.push({ name: 'rh-candidatures' })
    }
  }
}
</script>

<template>
  <div class="col-lg-8 mx-auto">
    <section class="card shadow-sm">
      <div class="card-body p-4">
        <h1 class="h3">Décision de recrutement</h1>
        <p class="text-body-secondary mb-4">
          Le responsable RH examine le dossier du candidat et consigne la décision prise à l'issue du
          processus de recrutement.
        </p>

        <AlertMessage :message="error" />
        <AlertMessage :message="candidatures.error" />

        <form @submit.prevent="enregistrer">
          <FormField id="candidature" label="Candidature">
            <select id="candidature" v-model="form.candidatureId" class="form-select" required>
              <option value="" disabled>-- Sélectionner une candidature --</option>
              <option v-for="c in candidatures.candidatures" :key="c.id" :value="String(c.id)">
                {{ c.candidat }} — {{ c.poste }}
              </option>
            </select>
          </FormField>

          <p v-if="candidature" class="small text-body-secondary">
            Candidat : <strong>{{ candidature.candidat }}</strong> · Poste : <strong>{{ candidature.poste }}</strong>
          </p>

          <FormField id="decision" label="Décision">
            <select id="decision" v-model="form.decision" class="form-select" required>
              <option value="" disabled>-- Sélectionner une décision --</option>
              <option v-for="d in DECISIONS" :key="d.value" :value="d.value">{{ d.label }}</option>
            </select>
          </FormField>

          <FormField id="observations" label="Observations du RH">
            <textarea id="observations" v-model="form.observations" rows="4" class="form-control"
                      placeholder="Commentaires, résultats de l'entretien, évaluation..."></textarea>
          </FormField>

          <div class="d-flex flex-wrap gap-2">
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? 'Enregistrement…' : 'Enregistrer la décision' }}
            </button>
            <RouterLink class="btn btn-outline-secondary" :to="{ name: 'rh-candidatures' }">Retour</RouterLink>
          </div>
        </form>
      </div>
    </section>
  </div>
</template>
