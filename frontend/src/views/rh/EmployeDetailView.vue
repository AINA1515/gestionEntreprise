<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useEmployesStore } from '@/stores/employes'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import { onboardingService } from '@/services/onboardingService'
import { contratService } from '@/services/contratService'

const route = useRoute()
const store = useEmployesStore()
const { loading, error, run } = useAsyncAction()
const { formatDate, formatMontant } = useFormatters()
const employe = computed(() => store.employeSelectionne)
const onboarding = ref(null)
const onboardingErreur = ref('')
const onboardingChargement = ref(false)
const formulaireContrat = ref(false)
const contratErreur = ref('')
const contratEnregistrement = ref(false)
const nouveauContrat = ref({ typeContrat: 'CDD', typeActe: 'RENOUVELLEMENT', dateDebut: '', dateFin: '', salaireBrut: '' })

async function chargerOnboarding(id) {
  onboarding.value = null
  onboardingErreur.value = ''
  onboardingChargement.value = true
  try { onboarding.value = await onboardingService.obtenir(id) }
  catch (error) {
    if (error.response?.status !== 404) onboardingErreur.value = error.message || 'Impossible de charger le parcours d’intégration.'
  } finally { onboardingChargement.value = false }
}

async function initialiserOnboarding() {
  if (!employe.value) return
  onboardingChargement.value = true
  onboardingErreur.value = ''
  try { onboarding.value = await onboardingService.initialiser(employe.value.id) }
  catch (error) { onboardingErreur.value = error.message || 'Impossible d’initialiser le parcours d’intégration.' }
  finally { onboardingChargement.value = false }
}

async function creerContrat() {
  if (!employe.value) return
  contratErreur.value = ''
  contratEnregistrement.value = true
  const contrats = employe.value.contrats || []
  const precedent = contrats[0]
  try {
    await contratService.creer({
      employeId: employe.value.id,
      posteId: employe.value.affectationCourante?.posteId ?? null,
      typeContrat: nouveauContrat.value.typeContrat,
      typeActe: precedent ? nouveauContrat.value.typeActe : 'INITIAL',
      contratPrecedentId: precedent?.id ?? null,
      dateDebut: nouveauContrat.value.dateDebut,
      dateFin: nouveauContrat.value.typeContrat === 'CDI' ? null : nouveauContrat.value.dateFin,
      salaireBrut: nouveauContrat.value.salaireBrut,
    })
    formulaireContrat.value = false
    await store.obtenir(employe.value.id)
  } catch (error) { contratErreur.value = error.message || 'Impossible d’enregistrer le nouveau contrat.' }
  finally { contratEnregistrement.value = false }
}

watch(() => route.params.id, async (id) => {
  store.employeSelectionne = null
  await run(() => store.obtenir(id))
  await chargerOnboarding(id)
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
          <div class="d-flex justify-content-between align-items-center gap-2"><h2 class="h5">Contrats</h2><button class="btn btn-sm btn-outline-primary" type="button" @click="formulaireContrat = !formulaireContrat">{{ formulaireContrat ? 'Fermer' : 'Nouveau contrat' }}</button></div>
          <form v-if="formulaireContrat" class="border rounded p-3 my-3" @submit.prevent="creerContrat">
            <p v-if="contratErreur" class="alert alert-danger py-2">{{ contratErreur }}</p>
            <div class="row g-2">
              <div class="col-md-4"><label class="form-label" for="type-contrat">Type</label><select id="type-contrat" v-model="nouveauContrat.typeContrat" class="form-select"><option value="CDD">CDD</option><option value="CDI">CDI</option><option value="STAGE">Stage</option><option value="ALTERNANCE">Alternance</option></select></div>
              <div v-if="employe.contrats?.length" class="col-md-4"><label class="form-label" for="acte-contrat">Acte</label><select id="acte-contrat" v-model="nouveauContrat.typeActe" class="form-select"><option value="RENOUVELLEMENT">Renouvellement</option><option value="AVENANT">Avenant / changement de contrat</option></select></div>
              <div class="col-md-4"><label class="form-label" for="date-debut-contrat">Date de début</label><input id="date-debut-contrat" v-model="nouveauContrat.dateDebut" type="date" class="form-control" required /></div>
              <div v-if="nouveauContrat.typeContrat !== 'CDI'" class="col-md-4"><label class="form-label" for="date-fin-contrat">Date de fin</label><input id="date-fin-contrat" v-model="nouveauContrat.dateFin" type="date" class="form-control" required /></div>
              <div class="col-md-4"><label class="form-label" for="salaire-contrat">Salaire brut (MGA)</label><input id="salaire-contrat" v-model="nouveauContrat.salaireBrut" type="number" min="0" step="0.01" class="form-control" required /></div>
            </div>
            <button class="btn btn-primary btn-sm mt-3" type="submit" :disabled="contratEnregistrement">{{ contratEnregistrement ? 'Enregistrement…' : 'Enregistrer le contrat' }}</button>
          </form>
          <div v-if="employe.contrats?.length" class="list-group list-group-flush">
            <article v-for="contrat in employe.contrats" :key="contrat.id" class="list-group-item px-0">
              <div class="d-flex justify-content-between gap-2"><strong>{{ contrat.type }} · {{ contrat.acte?.replaceAll('_', ' ') }} · contrat #{{ contrat.id }}</strong><span class="badge text-bg-light">{{ contrat.statut.replaceAll('_', ' ') }}</span></div>
              <div class="small text-body-secondary">Du {{ formatDate(contrat.dateDebut) }}<span v-if="contrat.dateFin"> au {{ formatDate(contrat.dateFin) }}</span></div>
              <div v-if="contrat.dateFinEffective" class="small text-body-secondary">Fin effective : {{ formatDate(contrat.dateFinEffective) }}</div>
              <div v-if="contrat.contratPrecedentId" class="small text-body-secondary">Succède au contrat #{{ contrat.contratPrecedentId }}</div>
              <div v-if="contrat.salaireBrut != null" class="small">Salaire brut : {{ formatMontant(contrat.salaireBrut) }} {{ contrat.devise }}</div>
            </article>
          </div>
          <p v-else class="text-body-secondary mb-0">Aucun contrat enregistré.</p>
        </div></section></div>
      </div>
      <section class="card shadow-sm mt-4"><div class="card-body">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
          <div><h2 class="h5 mb-1">Onboarding</h2><p v-if="onboarding" class="text-body-secondary mb-0">Statut : {{ onboarding.statut.replaceAll('_', ' ') }} · Début : {{ formatDate(onboarding.dateDebut) }}</p></div>
          <button v-if="!onboarding && !onboardingChargement" class="btn btn-primary" type="button" @click="initialiserOnboarding">Initialiser le parcours</button>
        </div>
        <p v-if="onboardingErreur" class="alert alert-danger py-2">{{ onboardingErreur }}</p>
        <p v-else-if="onboardingChargement" class="text-body-secondary">Chargement du parcours d’intégration…</p>
        <div v-else-if="onboarding?.taches?.length" class="list-group list-group-flush">
          <div v-for="tache in onboarding.taches" :key="tache.id" class="list-group-item px-0 d-flex flex-wrap justify-content-between gap-2">
            <div><strong>{{ tache.intitule }}</strong><div class="small text-body-secondary">{{ tache.serviceResponsable }}<span v-if="tache.dateLimite"> · Échéance : {{ formatDate(tache.dateLimite) }}</span></div></div>
            <span class="badge" :class="tache.statut === 'VALIDEE' ? 'text-bg-success' : tache.statut === 'EN_COURS' ? 'text-bg-primary' : 'text-bg-light'">{{ tache.statut.replaceAll('_', ' ') }}</span>
          </div>
        </div>
        <p v-else-if="onboarding" class="text-body-secondary mb-0">Aucune tâche d’intégration n’est configurée.</p>
        <p v-else-if="!onboardingChargement" class="text-body-secondary mb-0">Aucun parcours d’intégration enregistré.</p>
      </div></section>
      <div class="mt-4"><RouterLink class="btn btn-outline-secondary" :to="{ name: 'rh-employes' }">Retour à la liste des salariés</RouterLink></div>
    </template>
  </div>
</template>
