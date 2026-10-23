<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import { useFormatters } from '@/composables/useFormatters'
import { offreService } from '@/services/offreService'

const router = useRouter()
const store = useDemandesStore()
const { formatDate, formatMontant } = useFormatters()
const filtre = ref('VALIDEE')
const demandesSansOffre = ref(new Set())
const peutCreerOffre = computed(() => (id) => demandesSansOffre.value.has(id))

async function charger(page = 0) {
  await store.charger({ statut: filtre.value, page, size: 20, sort: 'dateCreation,desc' })
}

watch(filtre, () => charger(0))
onMounted(async () => {
  await Promise.all([charger(), chargerDemandesSansOffre()])
})

async function chargerDemandesSansOffre() {
  const demandes = await offreService.demandesEligibles()
  demandesSansOffre.value = new Set(demandes.map((demande) => demande.id))
}
</script>

<template>
  <PageHeader title="Décisions de la Direction" :breadcrumb="['Espace RH', 'Demandes arbitrées']"
    subtitle="Consultez les demandes acceptées par la Direction à publier et les demandes refusées avec leur motif." />
  <AlertMessage :message="store.error" />

  <div class="d-flex flex-wrap align-items-center gap-3 mb-3">
    <label for="filtre-decision" class="form-label mb-0">Décision</label>
    <select id="filtre-decision" v-model="filtre" class="form-select w-auto">
      <option value="VALIDEE">Acceptées par la Direction</option>
      <option value="REFUSEE">Refusées</option>
    </select>
    <span class="small text-body-secondary">{{ store.page.totalElements }} demande(s)</span>
  </div>

  <section class="card shadow-sm"><div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>Poste / département</th><th>Manager</th><th>Effectif</th><th>Niveau de formation</th><th>Budget · salaire de référence</th><th>Date souhaitée</th><th>Statut</th><th></th></tr></thead>
      <tbody>
        <tr v-if="store.loading"><td colspan="8" class="text-center py-4">Chargement…</td></tr>
        <tr v-else-if="!store.demandes.length"><td colspan="8" class="text-center text-body-secondary py-4">Aucune demande dans cette liste.</td></tr>
        <tr v-for="demande in store.demandes" :key="demande.id" role="button" tabindex="0"
          @click="router.push({ name: 'rh-demande-revue', params: { id: demande.id } })"
          @keydown.enter="router.push({ name: 'rh-demande-revue', params: { id: demande.id } })">
          <td><strong>{{ demande.poste }}</strong><span class="d-block small text-body-secondary">{{ demande.departement }} · #{{ demande.id }}</span></td>
          <td>{{ demande.manager }}</td><td>{{ demande.effectif }}</td><td>{{ demande.niveauFormationLibelle || 'Non précisé' }}</td><td>{{ formatMontant(demande.budget) }}</td>
          <td>{{ formatDate(demande.dateSouhaitee) }}</td><td><StatusBadge type="demande" :value="demande.statut" /></td>
          <td><RouterLink v-if="demande.statut !== 'VALIDEE' || peutCreerOffre(demande.id)" class="btn btn-sm btn-outline-primary" :to="{ name: 'rh-demande-revue', params: { id: demande.id } }" @click.stop>
            {{ demande.statut === 'VALIDEE' ? 'Créer l’offre' : 'Voir le motif' }}
          </RouterLink><RouterLink v-else class="btn btn-sm btn-outline-secondary" :to="{ name: 'rh-demande-revue', params: { id: demande.id } }" @click.stop>
            Voir la demande
          </RouterLink></td>
        </tr>
      </tbody>
    </table>
  </div>
    <div class="card-footer d-flex justify-content-between align-items-center">
      <span class="small text-body-secondary">Page {{ store.page.number + 1 }} / {{ Math.max(store.page.totalPages, 1) }}</span>
      <div class="btn-group"><button class="btn btn-sm btn-outline-secondary" :disabled="store.page.number <= 0 || store.loading" @click="charger(store.page.number - 1)">Précédente</button>
        <button class="btn btn-sm btn-outline-secondary" :disabled="store.page.number + 1 >= store.page.totalPages || store.loading" @click="charger(store.page.number + 1)">Suivante</button></div>
    </div>
  </section>
</template>
