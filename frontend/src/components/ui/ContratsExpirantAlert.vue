<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { contratService } from '@/services/contratService'
import { useFormatters } from '@/composables/useFormatters'

const props = defineProps({
  jours: { type: Number, default: 30 },
  lienEmploye: { type: Boolean, default: false },
})

const { formatDate } = useFormatters()
const contrats = ref([])

onMounted(async () => {
  // L'alerte est un complément : une erreur ne doit jamais bloquer la page qui l'affiche.
  try { contrats.value = await contratService.expirant(props.jours) }
  catch { contrats.value = [] }
})

function urgence(contrat) {
  return contrat.joursRestants != null && contrat.joursRestants <= 7 ? 'text-bg-danger' : 'text-bg-warning'
}
</script>

<template>
  <section v-if="contrats.length" class="alert alert-warning" role="alert" aria-live="polite">
    <h2 class="h6 mb-2"><i class="bi bi-exclamation-triangle me-1" aria-hidden="true"></i>
      {{ contrats.length }} CDD arrivent à échéance dans les {{ jours }} prochains jours</h2>
    <ul class="list-unstyled mb-0">
      <li v-for="c in contrats" :key="c.id" class="d-flex flex-wrap align-items-center gap-2 py-1">
        <span class="badge" :class="urgence(c)">{{ c.joursRestants === 0 ? 'Aujourd’hui' : `J-${c.joursRestants}` }}</span>
        <component :is="lienEmploye ? RouterLink : 'strong'" v-bind="lienEmploye ? { to: { name: 'rh-employe-detail', params: { id: c.employeId } } } : {}">
          {{ c.employeNom }}
        </component>
        <span class="text-body-secondary">{{ c.employeMatricule }} · {{ c.posteIntitule }} · fin le {{ formatDate(c.dateFin) }}</span>
      </li>
    </ul>
  </section>
</template>
