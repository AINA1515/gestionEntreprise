<script setup>
import { computed, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useEmployesStore } from '@/stores/employes'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'

const route = useRoute()
const store = useEmployesStore()
const { loading, error, run } = useAsyncAction()
const { formatDate, formatMontant } = useFormatters()
const employe = computed(() => store.employeSelectionne)

watch(() => route.params.id, async (id) => {
  store.employeSelectionne = null
  await run(() => store.obtenir(id))
}, { immediate: true })
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Fiche salarié" :breadcrumb="['Espace Ressources humaines', 'Employés', 'Fiche salarié']" />
    <AlertMessage :message="error" />
    <div v-if="loading && !employe" class="card card-body text-center py-5" role="status">
      <span class="spinner-border spinner-border-sm me-2" aria-hidden="true" />Chargement de la fiche salarié…
    </div>
    <template v-else-if="employe">
      <section class="card shadow-sm mb-4"><div class="card-body">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
          <div><h1 class="h3 mb-1">{{ employe.prenom }} {{ employe.nom }}</h1><p class="text-body-secondary mb-0">Matricule : <strong>{{ employe.matricule }}</strong></p></div>
          <StatusBadge type="employe" :value="employe.statutActivite" />
        </div>
        <hr />
        <div class="row g-3"><div class="col-md-6"><strong>E-mail professionnel</strong><p>{{ employe.emailProfessionnel || 'Non renseigné' }}</p></div><div class="col-md-6"><strong>Téléphone</strong><p>{{ employe.telephone || 'Non renseigné' }}</p></div></div>
      </div></section>

      <div class="row g-4">
        <div class="col-lg-6"><section class="card h-100"><div class="card-body">
          <h2 class="h5">Poste et affectation</h2>
          <template v-if="employe.affectationCourante">
            <h3 class="h6 mb-1">{{ employe.affectationCourante.intitule }}</h3>
            <p class="text-body-secondary">{{ employe.departement }}</p>
            <dl class="row mb-0"><dt class="col-sm-5">Code du poste</dt><dd class="col-sm-7">{{ employe.affectationCourante.codePoste }}</dd><dt class="col-sm-5">Statut du poste</dt><dd class="col-sm-7">{{ employe.affectationCourante.statutPoste }}</dd><dt class="col-sm-5">Début d’affectation</dt><dd class="col-sm-7">{{ formatDate(employe.affectationCourante.dateDebut) }}</dd><dt v-if="employe.affectationCourante.superieurId" class="col-sm-5">Responsable salarié</dt><dd v-if="employe.affectationCourante.superieurId" class="col-sm-7">#{{ employe.affectationCourante.superieurId }}</dd></dl>
          </template>
          <p v-else class="text-body-secondary mb-0">Aucune affectation enregistrée.</p>
        </div></section></div>

        <div class="col-lg-6"><section class="card h-100"><div class="card-body">
          <h2 class="h5">Contrats</h2>
          <div v-if="employe.contrats?.length" class="list-group list-group-flush">
            <article v-for="contrat in employe.contrats" :key="contrat.id" class="list-group-item px-0">
              <div class="d-flex justify-content-between gap-2"><strong>{{ contrat.type }} · contrat #{{ contrat.id }}</strong><span class="badge text-bg-light">{{ contrat.statut.replaceAll('_', ' ') }}</span></div>
              <div class="small text-body-secondary">Du {{ formatDate(contrat.dateDebut) }}<span v-if="contrat.dateFin"> au {{ formatDate(contrat.dateFin) }}</span></div>
              <div v-if="contrat.salaireBrut != null" class="small">Salaire brut : {{ formatMontant(contrat.salaireBrut) }} {{ contrat.devise }}</div>
            </article>
          </div>
          <p v-else class="text-body-secondary mb-0">Aucun contrat enregistré.</p>
        </div></section></div>
      </div>
      <div class="mt-4"><RouterLink class="btn btn-outline-secondary" :to="{ name: 'rh-employes' }">Retour à la liste des salariés</RouterLink></div>
    </template>
  </div>
</template>
