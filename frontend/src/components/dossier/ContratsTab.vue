<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { employeService } from '@/services/employeService'
import { contratService } from '@/services/contratService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const props = defineProps({ dossier: { type: Object, required: true }, mode: { type: String, default: 'moi' } })
const { formatDate, formatMontant } = useFormatters()
const { loading, error, run } = useAsyncAction()
const contrats = ref([])

// Les deux sources n'ont pas exactement les mêmes noms de champs : on les ramène à une forme unique.
const normaliser = (c) => ({ id: c.id, type: c.type ?? c.typeContrat, acte: c.acte ?? c.typeActe, statut: c.statut, dateDebut: c.dateDebut,
  dateFin: c.dateFin, dateFinEffective: c.dateFinEffective, salaireBrut: c.salaireBrut, devise: c.devise })

onMounted(async () => {
  const liste = await run(async () => (props.mode === 'rh' ? (await employeService.obtenir(props.dossier.id)).contrats : await contratService.mesContrats()))
  contrats.value = (liste || []).map(normaliser)
})
</script>

<template>
  <section class="card"><div class="card-body">
    <h2 class="h5">Contrats</h2>
    <AlertMessage :message="error" />
    <p v-if="loading" class="text-body-secondary mb-0">Chargement…</p>
    <p v-else-if="!contrats.length" class="text-body-secondary mb-0">Aucun contrat enregistré.</p>
    <div v-else class="list-group list-group-flush">
      <article v-for="c in contrats" :key="c.id" class="list-group-item px-0">
        <div class="d-flex justify-content-between gap-2"><strong>{{ c.type }} · {{ c.acte?.replaceAll('_', ' ') }}</strong><span class="badge text-bg-light">{{ c.statut?.replaceAll('_', ' ') }}</span></div>
        <div class="small text-body-secondary">Du {{ formatDate(c.dateDebut) }}<span v-if="c.dateFin"> au {{ formatDate(c.dateFin) }}</span><span v-if="c.dateFinEffective"> · fin effective {{ formatDate(c.dateFinEffective) }}</span></div>
        <div v-if="c.salaireBrut != null" class="small">Salaire brut : {{ formatMontant(c.salaireBrut) }} {{ c.devise }}</div>
        <RouterLink class="btn btn-sm btn-outline-primary mt-2" :to="{ name: 'contrat-detail', params: { id: c.id } }">Voir le détail</RouterLink>
      </article>
    </div>
  </div></section>
</template>
