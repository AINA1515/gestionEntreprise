<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
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
const selected = computed(() => store.employeSelectionne)
const filters = reactive({ departementId: '', statutActivite: '', recherche: '' })
const columns = [{ key: 'matricule', label: 'Matricule' }, { key: 'nomComplet', label: 'Salarié' }, { key: 'departement', label: 'Département' }, { key: 'statutActivite', label: 'Statut' }, { key: 'actions', label: '' }]
const rows = computed(() => (store.page?.content ?? []).map((e) => ({ ...e, nomComplet: `${e.prenom} ${e.nom}` })))
const params = () => ({ size: 100, ...(filters.departementId ? { departementId: filters.departementId } : {}), ...(filters.statutActivite ? { statutActivite: filters.statutActivite } : {}), ...(filters.recherche ? { recherche: filters.recherche } : {}) })
async function load() { await action.run(() => store.charger(params())) }
onMounted(() => action.run(async () => { await referentiel.chargerDepartements(); await load() }))
async function detail(id) { await action.run(() => store.obtenir(id)) }
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
      <template #cell-actions="{ row }"><button class="btn btn-sm btn-outline-primary" @click="detail(row.id)">Voir la fiche</button></template>
    </DataTable></div>
    <section v-if="selected" class="card mt-4"><div class="card-body">
      <div class="d-flex justify-content-between align-items-start"><div><h2 class="h4">{{ selected.prenom }} {{ selected.nom }}</h2><p class="text-body-secondary">{{ selected.matricule }} · {{ selected.emailProfessionnel || 'Email professionnel non renseigné' }}</p></div><button class="btn-close" aria-label="Fermer la fiche" @click="store.employeSelectionne = null"></button></div>
      <div class="row g-3"><div class="col-lg-6"><h3 class="h6">Poste et affectation</h3><div v-if="selected.affectationCourante"><p class="mb-1"><strong>{{ selected.affectationCourante.intitule }}</strong> · {{ selected.departement }}</p><p class="small text-body-secondary">{{ selected.affectationCourante.codePoste }} · poste {{ selected.affectationCourante.statutPoste }} · affectation depuis {{ selected.affectationCourante.dateDebut }}</p><p v-if="selected.affectationCourante.superieurId" class="small">Responsable salarié #{{ selected.affectationCourante.superieurId }}</p></div><p v-else class="text-body-secondary">Aucune affectation enregistrée.</p></div>
        <div class="col-lg-6"><h3 class="h6">Contrats</h3><ul v-if="selected.contrats?.length" class="list-group list-group-flush"><li v-for="c in selected.contrats" :key="c.id" class="list-group-item px-0"><strong>{{ c.type }}</strong> · {{ c.statut.replace('_', ' ') }} · début {{ c.dateDebut }}<span v-if="c.dateFin"> · fin {{ c.dateFin }}</span><span v-if="c.salaireBrut != null"> · {{ Number(c.salaireBrut).toLocaleString('fr-FR') }} {{ c.devise }}</span></li></ul><p v-else class="text-body-secondary">Aucun contrat enregistré.</p></div></div>
    </div></section>
  </div>
</template>
