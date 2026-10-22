<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useReferentielStore } from '@/stores/referentiel'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useLabels } from '@/composables/useLabels'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTable from '@/components/ui/DataTable.vue'
import FormField from '@/components/ui/FormField.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useReferentielStore()
const action = useAsyncAction()
const { statut } = useLabels()
const editId = ref(null)
const filters = reactive({ departementId: '', statut: '', recherche: '' })
const form = reactive({ codePoste: '', intitule: '', description: '', departementId: '' })
const columns = [{ key: 'codePoste', label: 'Code' }, { key: 'intitule', label: 'Intitulé' }, { key: 'departement', label: 'Département' }, { key: 'statut', label: 'Statut' }, { key: 'actions', label: 'Actions' }]
const rows = computed(() => store.postesPage?.content ?? [])
const params = () => ({ size: 100, ...(filters.departementId ? { departementId: filters.departementId } : {}), ...(filters.statut ? { statut: filters.statut } : {}), ...(filters.recherche ? { recherche: filters.recherche } : {}) })
async function load() { await action.run(() => store.chargerPostesPage(params())) }
onMounted(() => action.run(async () => { await store.chargerDepartements(); await load() }))
watch(filters, () => load())
function edit(row) { editId.value = row.id; Object.assign(form, { codePoste: row.codePoste, intitule: row.intitule, description: row.description ?? '', departementId: row.departementId }) }
function reset() { editId.value = null; Object.assign(form, { codePoste: '', intitule: '', description: '', departementId: '' }) }
async function save() { await action.run(() => editId.value ? store.modifierPoste(editId.value, form) : store.creerPoste(form)); if (!action.error.value) { reset(); await load() } }
async function fermer(id) { await action.run(() => store.fermerPoste(id)); if (!action.error.value) await load() }
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Postes" subtitle="Un poste existe indépendamment de tout salarié. Un poste fermé est conservé dans l’historique." />
    <AlertMessage :message="action.error.value" />
    <div class="card card-body mb-3"><div class="row g-3 align-items-end">
      <div class="col-md-4"><FormField id="filtreDepartement" label="Département"><select id="filtreDepartement" v-model="filters.departementId" class="form-select"><option value="">Tous</option><option v-for="d in store.departements" :key="d.id" :value="d.id">{{ d.libelle }}</option></select></FormField></div>
      <div class="col-md-3"><FormField id="filtreStatut" label="Statut"><select id="filtreStatut" v-model="filters.statut" class="form-select"><option value="">Tous</option><option v-for="s in ['CREE','VACANT','POURVU','FERME']" :key="s" :value="s">{{ statut('poste', s).label }}</option></select></FormField></div>
      <div class="col-md-5"><FormField id="recherche" label="Recherche"><input id="recherche" v-model="filters.recherche" class="form-control" placeholder="Code ou intitulé" /></FormField></div>
    </div></div>
    <div class="row g-4">
      <div class="col-xl-8"><div class="card"><DataTable :columns="columns" :rows="rows" :loading="action.loading.value">
        <template #cell-statut="{ row }"><StatusBadge type="poste" :value="row.statut" /></template>
        <template #cell-actions="{ row }"><div class="btn-group btn-group-sm"><button class="btn btn-outline-primary" :disabled="row.statut === 'FERME'" @click="edit(row)">Modifier</button><button v-if="row.statut !== 'FERME'" class="btn btn-outline-danger" @click="fermer(row.id)">Fermer</button></div></template>
      </DataTable></div></div>
      <div class="col-xl-4"><form class="card card-body" @submit.prevent="save"><h2 class="h5">{{ editId ? 'Modifier le poste' : 'Créer un poste' }}</h2>
        <FormField id="codePoste" label="Code poste"><input id="codePoste" v-model="form.codePoste" class="form-control" maxlength="30" required /></FormField>
        <FormField id="intitule" label="Intitulé"><input id="intitule" v-model="form.intitule" class="form-control" maxlength="200" required /></FormField>
        <FormField id="description" label="Description"><textarea id="description" v-model="form.description" class="form-control" rows="3" maxlength="4000"></textarea></FormField>
        <FormField id="departementId" label="Département"><select id="departementId" v-model="form.departementId" class="form-select" required><option value="">Choisir</option><option v-for="d in store.departements.filter(x => x.estActif)" :key="d.id" :value="d.id">{{ d.libelle }}</option></select></FormField>
        <p v-if="!editId" class="small text-body-secondary">Le poste sera créé puis ouvert au statut Vacant.</p>
        <div class="d-flex gap-2"><button class="btn btn-primary" type="submit" :disabled="action.loading.value">{{ editId ? 'Enregistrer' : 'Créer' }}</button><button v-if="editId" class="btn btn-outline-secondary" type="button" @click="reset">Annuler</button></div>
      </form></div>
    </div>
  </div>
</template>
