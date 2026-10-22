<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useReferentielStore } from '@/stores/referentiel'
import { useEmployesStore } from '@/stores/employes'
import { useAsyncAction } from '@/composables/useAsyncAction'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTable from '@/components/ui/DataTable.vue'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useReferentielStore()
const employees = useEmployesStore()
const action = useAsyncAction()
const editId = ref(null)
const selected = ref(null)
const form = reactive({ code: '', libelle: '', responsableId: '', departementParentId: '', estActif: true })
const errorMessage = computed(() => action.error.value)
const columns = [
  { key: 'code', label: 'Code' }, { key: 'libelle', label: 'Département' }, { key: 'responsable', label: 'Responsable actuel' }, { key: 'estActif', label: 'État' }, { key: 'actions', label: 'Actions' },
]
const rows = computed(() => (store.departementsPage?.content ?? []).map((d) => ({ ...d, responsable: d.responsableActuel ? `${d.responsableActuel.prenom} ${d.responsableActuel.nom}` : 'Non défini' })))

onMounted(() => { action.run(() => Promise.all([store.chargerDepartementsPage({ size: 100 }), employees.charger({ size: 500, statutActivite: 'ACTIF' })])) })
function reset() { editId.value = null; Object.assign(form, { code: '', libelle: '', responsableId: '', departementParentId: '', estActif: true }) }
function edit(row) { editId.value = row.id; Object.assign(form, { code: row.code, libelle: row.libelle, responsableId: row.responsableActuel?.employeId ?? '', departementParentId: row.departementParentId ?? '', estActif: row.estActif }) }
async function save() {
  const payload = { ...form, responsableId: form.responsableId || null, departementParentId: form.departementParentId || null }
  await action.run(() => editId.value ? store.modifierDepartement(editId.value, payload) : store.creerDepartement(payload))
  if (!action.error.value) { reset(); await action.run(() => store.chargerDepartementsPage({ size: 100 })) }
}
async function reassign() {
  if (!selected.value || !form.responsableId) return
  await action.run(() => store.changerResponsable(selected.value.id, { employeId: Number(form.responsableId), motif: 'Changement de responsable' }))
  if (!action.error.value) { selected.value = null; await action.run(() => store.chargerDepartementsPage({ size: 100 })) }
}
async function deactivate(id) { await action.run(() => store.desactiverDepartement(id)); await action.run(() => store.chargerDepartementsPage({ size: 100 })) }
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Départements" subtitle="Gérez les départements et conservez l’historique de leurs responsables." />
    <AlertMessage :message="errorMessage" />
    <div class="row g-4">
      <div class="col-xl-8">
        <div class="card"><DataTable :columns="columns" :rows="rows" :loading="action.loading.value">
          <template #cell-estActif="{ row }"><span class="badge" :class="row.estActif ? 'text-bg-success' : 'text-bg-secondary'">{{ row.estActif ? 'Actif' : 'Désactivé' }}</span></template>
          <template #cell-actions="{ row }"><div class="btn-group btn-group-sm"><button class="btn btn-outline-primary" @click="edit(row)">Modifier</button><button class="btn btn-outline-secondary" @click="selected = row">Responsable</button><button v-if="row.estActif" class="btn btn-outline-danger" @click="deactivate(row.id)">Désactiver</button></div></template>
        </DataTable></div>
        <div v-if="selected" class="card mt-3"><div class="card-body"><h2 class="h5">Historique des responsables · {{ selected.libelle }}</h2><ul v-if="selected.historiqueResponsables?.length" class="list-group list-group-flush"><li v-for="r in selected.historiqueResponsables" :key="`${r.employeId}-${r.dateDebut}`" class="list-group-item px-0">{{ r.prenom }} {{ r.nom }} · {{ r.dateDebut }} → {{ r.dateFin || 'Actuel' }} <span v-if="r.motif" class="text-body-secondary">({{ r.motif }})</span></li></ul><p v-else class="text-body-secondary">Aucun responsable enregistré.</p><div class="d-flex gap-2 align-items-end"><div class="flex-grow-1"><FormField id="nouveau-responsable" label="Nouveau responsable"><select id="nouveau-responsable" v-model="form.responsableId" class="form-select"><option value="">Choisir un salarié</option><option v-for="e in employees.page?.content ?? []" :key="e.id" :value="e.id">{{ e.prenom }} {{ e.nom }} — {{ e.matricule }}</option></select></FormField></div><button class="btn btn-primary mb-3" @click="reassign">Enregistrer</button><button class="btn btn-link mb-3" @click="selected = null">Fermer</button></div></div></div>
      </div>
      <div class="col-xl-4"><form class="card card-body" @submit.prevent="save"><h2 class="h5">{{ editId ? 'Modifier le département' : 'Nouveau département' }}</h2>
        <FormField id="code" label="Code"><input id="code" v-model="form.code" class="form-control" maxlength="20" required /></FormField>
        <FormField id="libelle" label="Libellé"><input id="libelle" v-model="form.libelle" class="form-control" maxlength="150" required /></FormField>
        <FormField id="departementParent" label="Département parent"><select id="departementParent" v-model="form.departementParentId" class="form-select"><option value="">Aucun</option><option v-for="d in store.departements" :key="d.id" :value="d.id" :disabled="d.id === editId">{{ d.libelle }}</option></select></FormField>
        <FormField id="responsable" label="Responsable"><select id="responsable" v-model="form.responsableId" class="form-select"><option value="">Aucun</option><option v-for="e in employees.page?.content ?? []" :key="e.id" :value="e.id">{{ e.prenom }} {{ e.nom }} — {{ e.matricule }}</option></select></FormField>
        <div v-if="editId" class="form-check mb-3"><input id="actif" v-model="form.estActif" class="form-check-input" type="checkbox" /><label class="form-check-label" for="actif">Département actif</label></div>
        <div class="d-flex gap-2"><button class="btn btn-primary" type="submit" :disabled="action.loading.value">{{ editId ? 'Enregistrer' : 'Créer' }}</button><button v-if="editId" class="btn btn-outline-secondary" type="button" @click="reset">Annuler</button></div>
      </form></div>
    </div>
  </div>
</template>
