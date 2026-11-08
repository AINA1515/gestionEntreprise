<script setup>
import { computed, onMounted, ref } from 'vue'
import { dossierService } from '@/services/dossierService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import { usePermissions } from '@/composables/usePermissions'
import { useToast } from '@/composables/useToast'
import { EVOLUTIONS_SALAIRE } from '@/composables/useDossierLabels'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const props = defineProps({ employeId: { type: [Number, String], default: null }, mode: { type: String, default: 'moi' } })
const { formatDate, formatMontant } = useFormatters()
const { can } = usePermissions()
const toast = useToast()
const { loading, error, run } = useAsyncAction()
const { loading: saving, error: saveError, run: runSave } = useAsyncAction()
const donnees = ref(null)
const ouvert = ref(false)
const form = ref({ dateEffet: '', salaireBrutNouveau: '', typeEvolution: 'AUGMENTATION', motif: '' })
const peutAjouter = computed(() => props.mode === 'rh' && can('CONTRAT_GERER') && can('SALAIRE_CONSULTER'))
const origines = { CONTRAT_INITIAL: 'Contrat initial', AVENANT: 'Avenant', RENOUVELLEMENT: 'Renouvellement', EVOLUTION: 'Évolution' }

async function charger() { donnees.value = (await run(() => dossierService.remuneration(props.employeId))) ?? null }
onMounted(charger)

async function ajouter() {
  const point = await runSave(() => dossierService.ajouterEvolutionSalaire(props.employeId, { ...form.value }))
  if (!point) return
  toast.success('Changement de salaire enregistré.')
  ouvert.value = false
  form.value = { dateEffet: '', salaireBrutNouveau: '', typeEvolution: 'AUGMENTATION', motif: '' }
  await charger()
}
</script>

<template>
  <section class="card"><div class="card-body">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="h5 mb-0">Rémunération</h2>
      <button v-if="peutAjouter && !ouvert" class="btn btn-sm btn-outline-primary" type="button" @click="ouvert = true">Enregistrer un changement de salaire</button>
    </div>
    <AlertMessage :message="error" />
    <p v-if="loading" class="text-body-secondary">Chargement…</p>
    <template v-else-if="donnees">
      <div class="mb-3">
        <div class="text-body-secondary small">Salaire brut actuel</div>
        <div class="display-6">{{ donnees.salaireActuel != null ? `${formatMontant(donnees.salaireActuel)} ${donnees.devise}` : '—' }}</div>
        <div v-if="donnees.dateEffetActuel" class="small text-body-secondary">En vigueur depuis le {{ formatDate(donnees.dateEffetActuel) }}</div>
      </div>

      <form v-if="ouvert" class="border rounded p-3 mb-3" @submit.prevent="ajouter">
        <AlertMessage :message="saveError" />
        <p class="small text-body-secondary">Pour une augmentation sans avenant. Un changement de poste ou de contrat passe par un avenant.</p>
        <div class="row g-3">
          <div class="col-md-3"><label class="form-label" for="ev-date">Date d’effet</label><input id="ev-date" v-model="form.dateEffet" type="date" class="form-control" required /></div>
          <div class="col-md-3"><label class="form-label" for="ev-sal">Nouveau salaire brut</label><input id="ev-sal" v-model="form.salaireBrutNouveau" type="number" min="0" step="0.01" class="form-control" required /></div>
          <div class="col-md-3"><label class="form-label" for="ev-type">Type</label><select id="ev-type" v-model="form.typeEvolution" class="form-select"><option v-for="(libelle, code) in EVOLUTIONS_SALAIRE" :key="code" :value="code">{{ libelle }}</option></select></div>
          <div class="col-md-12"><label class="form-label" for="ev-motif">Motif</label><input id="ev-motif" v-model="form.motif" class="form-control" maxlength="500" required /></div>
        </div>
        <div class="mt-3 d-flex gap-2">
          <button class="btn btn-primary" type="submit" :disabled="saving">{{ saving ? 'Enregistrement…' : 'Enregistrer' }}</button>
          <button class="btn btn-outline-secondary" type="button" :disabled="saving" @click="ouvert = false">Annuler</button>
        </div>
      </form>

      <div class="table-responsive"><table class="table align-middle mb-0">
        <thead><tr><th>Date d’effet</th><th>Origine</th><th class="text-end">Ancien</th><th class="text-end">Nouveau</th><th>Motif</th></tr></thead>
        <tbody>
          <tr v-if="!donnees.historique.length"><td colspan="5" class="text-center text-body-secondary">Aucun historique.</td></tr>
          <tr v-for="(p, i) in [...donnees.historique].reverse()" :key="`${p.contratId}-${p.evolutionId}-${i}`">
            <td>{{ formatDate(p.dateEffet) }}</td>
            <td>{{ origines[p.origine] || p.origine }}<span v-if="p.typeEvolution" class="small text-body-secondary"> · {{ EVOLUTIONS_SALAIRE[p.typeEvolution] }}</span></td>
            <td class="text-end">{{ p.salaireAncien != null ? formatMontant(p.salaireAncien) : '—' }}</td>
            <td class="text-end fw-semibold">{{ formatMontant(p.salaireNouveau) }} {{ p.devise }}</td>
            <td class="small">{{ p.motif || '' }}</td>
          </tr>
        </tbody>
      </table></div>
    </template>
  </div></section>
</template>
