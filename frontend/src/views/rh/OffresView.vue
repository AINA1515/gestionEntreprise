<script setup>
import { onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useOffresStore } from '@/stores/offres'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import DataTable from '@/components/ui/DataTable.vue'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const router = useRouter()
const store = useOffresStore()
const { offres, page, loading, error } = storeToRefs(store)
const filters = reactive({ statut: '' })
const columns = [{ key: 'titre', label: 'Offre' }, { key: 'departement', label: 'Département' }, { key: 'statut', label: 'Statut' },
  { key: 'nombreCandidatures', label: 'Candidatures' }, { key: 'dateLimiteCandidature', label: 'Date limite' }, { key: 'actions', label: 'Actions' }]
const modal = reactive({ show: false, action: '', offre: null })
function actions(offre) { return offre.actionsDisponibles || (offre.statut === 'BROUILLON' ? ['modifier', 'publier'] : offre.statut === 'PUBLIEE' ? ['cloturer'] : []) }
function ouvrir(action, offre) { modal.action = action; modal.offre = offre; modal.show = true }
async function confirmer() {
  if (!modal.offre) return
  const id = modal.offre.id
  const result = modal.action === 'publier' ? await store.publier(id) : await store.cloturer(id)
  modal.show = false
  if (result) await store.charger({ page: page.value.number, size: page.value.size, statut: filters.statut || undefined })
}
async function charger(pageNum = 0) { await store.charger({ page: pageNum, size: 20, statut: filters.statut || undefined }) }
watch(() => filters.statut, () => charger(0))
onMounted(() => charger())
</script>

<template>
  <PageHeader title="Offres d’emploi" :breadcrumb="['Espace RH', 'Recrutement', 'Offres']" subtitle="Créez, publiez et suivez les offres liées aux demandes validées." />
  <AlertMessage :message="error" />
  <div class="card shadow-sm mb-3"><div class="card-body d-flex flex-wrap align-items-end gap-3">
    <div><label for="offres-statut" class="form-label">Statut</label><select id="offres-statut" v-model="filters.statut" class="form-select"><option value="">Tous</option><option value="BROUILLON">Brouillon</option><option value="PUBLIEE">Publiée</option><option value="CLOTUREE">Clôturée</option></select></div>
    <span class="text-body-secondary small">{{ page.totalElements }} offre(s)</span>
  </div></div>
  <section class="card shadow-sm"><DataTable :columns="columns" :rows="offres" :loading="loading" empty-message="Aucune offre pour ces critères.">
    <template #cell-titre="{ row }"><strong>{{ row.titre }}</strong><div class="small text-body-secondary">Demande #{{ row.demandeId }}</div></template>
    <template #cell-statut="{ row }"><StatusBadge type="offre" :value="row.statut" /></template>
    <template #cell-nombreCandidatures="{ value }"><span class="badge text-bg-light border">{{ value ?? 0 }}</span></template>
    <template #cell-actions="{ row }"><div class="d-flex flex-wrap gap-1">
      <button v-if="actions(row).includes('modifier')" class="btn btn-sm btn-outline-primary" @click="router.push({ name: 'rh-offre-modifier', params: { id: row.id } })">Modifier</button>
      <button v-if="actions(row).includes('publier')" class="btn btn-sm btn-success" @click="ouvrir('publier', row)">Publier</button>
      <button v-if="actions(row).includes('cloturer')" class="btn btn-sm btn-outline-danger" @click="ouvrir('cloturer', row)">Clôturer</button>
    </div></template>
  </DataTable></section>
  <nav v-if="page.totalPages > 1" class="d-flex justify-content-between align-items-center mt-3"><button class="btn btn-outline-secondary" :disabled="page.number <= 0 || loading" @click="charger(page.number - 1)">Précédent</button><span>Page {{ page.number + 1 }} / {{ page.totalPages }}</span><button class="btn btn-outline-secondary" :disabled="page.number + 1 >= page.totalPages || loading" @click="charger(page.number + 1)">Suivant</button></nav>
  <ConfirmModal v-model="modal.show" :title="modal.action === 'publier' ? 'Publier cette offre ?' : 'Clôturer cette offre ?'"
    :message="modal.action === 'publier' ? 'L’offre sera immédiatement visible dans le catalogue public.' : 'L’offre ne sera plus visible dans le catalogue public.'"
    :confirm-text="modal.action === 'publier' ? 'Publier' : 'Clôturer'" :variant="modal.action === 'publier' ? 'success' : 'danger'" @confirm="confirmer" />
</template>
