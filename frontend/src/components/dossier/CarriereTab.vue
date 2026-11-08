<script setup>
import { onMounted, ref } from 'vue'
import { dossierService } from '@/services/dossierService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import { EVENEMENTS_CARRIERE } from '@/composables/useDossierLabels'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const props = defineProps({ employeId: { type: [Number, String], default: null } })
const { formatDate, formatMontant } = useFormatters()
const { loading, error, run } = useAsyncAction()
const evenements = ref([])
onMounted(async () => { evenements.value = (await run(() => dossierService.carriere(props.employeId))) || [] })
</script>

<template>
  <section class="card"><div class="card-body">
    <h2 class="h5">Chronologie de carrière</h2>
    <AlertMessage :message="error" />
    <p v-if="loading" class="text-body-secondary mb-0">Chargement…</p>
    <p v-else-if="!evenements.length" class="text-body-secondary mb-0">Aucun événement.</p>
    <ol v-else class="list-unstyled mb-0">
      <li v-for="(e, i) in evenements" :key="`${e.source}-${e.sourceId}-${e.type}-${i}`" class="d-flex gap-3 pb-3">
        <span class="rounded-circle d-inline-flex align-items-center justify-content-center flex-shrink-0" :class="`text-bg-${EVENEMENTS_CARRIERE[e.type]?.variant || 'secondary'}`" style="width: 2.25rem; height: 2.25rem">
          <i :class="`bi bi-${EVENEMENTS_CARRIERE[e.type]?.icon || 'circle'}`" aria-hidden="true"></i>
        </span>
        <div>
          <div class="fw-semibold">{{ e.libelle }} <span class="small fw-normal text-body-secondary">· {{ formatDate(e.date) }}</span></div>
          <div v-if="e.detail" class="small text-body-secondary">{{ e.detail }}</div>
          <div v-if="e.salaireNouveau != null" class="small">
            <template v-if="e.salaireAncien != null">{{ formatMontant(e.salaireAncien) }} → </template><strong>{{ formatMontant(e.salaireNouveau) }} {{ e.devise }}</strong>
          </div>
        </div>
      </li>
    </ol>
  </div></section>
</template>
