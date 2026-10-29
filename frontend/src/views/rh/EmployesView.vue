<script setup>
import { computed, onMounted, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import { useEmployesStore } from '@/stores/employes'
import { useReferentielStore } from '@/stores/referentiel'
import { useAsyncAction } from '@/composables/useAsyncAction'
import PageHeader from '@/components/ui/PageHeader.vue'
import DataTable from '@/components/ui/DataTable.vue'
import FormField from '@/components/ui/FormField.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const store = useEmployesStore()
const referentiel = useReferentielStore()
const action = useAsyncAction()
const filters = reactive({ departementId: '', statutActivite: '', recherche: '' })
const columns = [{ key: 'matricule', label: 'Matricule' }, { key: 'nomComplet', label: 'Salarié' }, { key: 'departement', label: 'Département' }, { key: 'statutActivite', label: 'Statut' }, { key: 'actions', label: '' }]
const rows = computed(() => (store.page?.content ?? []).map((e) => ({ ...e, nomComplet: `${e.prenom} ${e.nom}` })))
const params = () => ({ size: 100, ...(filters.departementId ? { departementId: filters.departementId } : {}), ...(filters.statutActivite ? { statutActivite: filters.statutActivite } : {}), ...(filters.recherche ? { recherche: filters.recherche } : {}) })
async function load() { await action.run(() => store.charger(params())) }
onMounted(() => action.run(async () => { await referentiel.chargerDepartements(); await load() }))
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Employés" subtitle="Consultez les fiches salariés, leurs contrats et leurs affectations." />
    <AlertMessage :message="action.error.value" />
    <div class="card card-body mb-3"><div class="row g-3 align-items-end">
      <div class="col-md-4"><FormField id="departement-filtre" label="Département"><select id="departement-filtre" v-model="filters.departementId" class="form-select"><option value="">Tous</option><option v-for="d in referentiel.departements" :key="d.id" :value="d.id">{{ d.libelle }}</option></select></FormField></div>
      <div class="col-md-3"><FormField id="statut-filtre" label="Statut d’activité"><select id="statut-filtre" v-model="filters.statutActivite" class="form-select"><option value="">Tous</option><option v-for="s in ['EN_ONBOARDING','ACTIF','SUSPENDU','SORTI']" :key="s" :value="s">{{ s.replace('_', ' ') }}</option></select></FormField></div>
      <div class="col-md-5"><FormField id="recherche-employe" label="Recherche"><input id="recherche-employe" v-model="filters.recherche" class="form-control" placeholder="Nom, prénom, matricule ou email" /></FormField></div>
    </div><button class="btn btn-outline-primary align-self-start" @click="load">Filtrer</button></div>
    <div class="card"><DataTable :columns="columns" :rows="rows" :loading="store.loading">
      <template #cell-statutActivite="{ row }"><StatusBadge type="employe" :value="row.statutActivite" /></template>
      <template #cell-actions="{ row }"><RouterLink class="btn btn-sm btn-outline-primary" :to="{ name: 'rh-employe-detail', params: { id: row.id } }">Voir la fiche</RouterLink></template>
    </DataTable></div>
  </div>
</template>
