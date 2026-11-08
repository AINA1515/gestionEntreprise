<script setup>
import { onMounted, ref } from 'vue'
import { auditService } from '@/services/auditService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { ACTIONS_AUDIT, libelleChamp } from '@/composables/useDossierLabels'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const props = defineProps({ employeId: { type: [Number, String], required: true } })
const { loading, error, run } = useAsyncAction()
const page = ref(null)
const numero = ref(0)
const heure = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'medium' })

async function charger() { page.value = (await run(() => auditService.rechercher({ employeId: props.employeId, page: numero.value, size: 20 }))) ?? null }
onMounted(charger)
function aller(n) { numero.value = n; charger() }
</script>

<template>
  <section class="card"><div class="card-body">
    <h2 class="h5">Historique des modifications et consultations</h2>
    <AlertMessage :message="error" />
    <p v-if="loading && !page" class="text-body-secondary mb-0">Chargement…</p>
    <div v-else-if="page" class="table-responsive">
      <table class="table table-sm align-middle">
        <thead><tr><th>Date</th><th>Auteur</th><th>Action</th><th>Élément</th><th>Champ</th><th>Ancienne valeur</th><th>Nouvelle valeur</th></tr></thead>
        <tbody>
          <tr v-if="!page.content.length"><td colspan="7" class="text-center text-body-secondary">Aucun événement.</td></tr>
          <tr v-for="l in page.content" :key="l.id">
            <td class="text-nowrap">{{ heure.format(new Date(l.dateEvenement)) }}</td><td>{{ l.login }}</td><td>{{ ACTIONS_AUDIT[l.action] || l.action }}</td>
            <td>{{ l.entite }}<span v-if="l.entiteId"> #{{ l.entiteId }}</span></td><td>{{ l.champ ? libelleChamp(l.champ) : '' }}<div v-if="l.motif" class="small text-body-secondary">{{ l.motif }}</div></td>
            <td>{{ l.ancienneValeur ?? '' }}</td><td>{{ l.nouvelleValeur ?? '' }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="page.totalPages > 1" class="d-flex justify-content-between align-items-center">
        <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="numero === 0" @click="aller(numero - 1)">Précédent</button>
        <span class="small text-body-secondary">Page {{ numero + 1 }} / {{ page.totalPages }}</span>
        <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="numero + 1 >= page.totalPages" @click="aller(numero + 1)">Suivant</button>
      </div>
    </div>
  </div></section>
</template>
