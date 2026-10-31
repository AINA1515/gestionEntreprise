<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { candidatureService } from '@/services/candidatureService'
import { useAuthStore } from '@/stores/auth'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { formatDate, formatMontant } = useFormatters()

const c = ref(null)
const loading = ref(true)
const error = ref('')

const cv = computed(() => c.value?.cv ?? null)
const estRh = computed(() => auth.hasPermission('CANDIDATURE_PRESELECTIONNER'))
const libelle = (valeur) => valeur?.replaceAll('_', ' ') ?? ''

async function charger() {
  loading.value = true
  error.value = ''
  try { c.value = await candidatureService.detail(route.params.id) }
  catch (e) { c.value = null; error.value = e.response?.status === 404 ? 'Cette candidature est introuvable ou ne vous est pas accessible.' : (e.message || 'Impossible de charger la candidature.') }
  finally { loading.value = false }
}

onMounted(charger)
watch(() => route.params.id, charger)
</script>

<template>
  <div class="container-fluid py-3">
    <button class="btn btn-link px-0 mb-2" type="button" @click="router.back()"><i class="bi bi-arrow-left me-1" aria-hidden="true"></i>Retour</button>
    <PageHeader :title="c ? c.candidatNom : 'Candidature'" :breadcrumb="['Recrutement', 'Candidature']" :subtitle="c ? `Pour le poste « ${c.offreTitre} »` : ''" />
    <AlertMessage :message="error" />
    <div v-if="loading" class="card card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true" />Chargement…</div>

    <div v-else-if="c" class="row g-4">
      <div class="col-lg-5">
        <section class="card shadow-sm mb-4"><div class="card-body">
          <div class="d-flex justify-content-between align-items-start gap-2 mb-3"><h2 class="h5 mb-0">Candidature</h2><span class="badge text-bg-light">{{ libelle(c.statut) }}</span></div>
          <dl class="row mb-0 small">
            <dt class="col-5">Candidat</dt><dd class="col-7">{{ c.candidatNom }}</dd>
            <dt class="col-5">E-mail</dt><dd class="col-7">{{ c.candidatEmail }}</dd>
            <dt class="col-5">Téléphone</dt><dd class="col-7">{{ c.candidatTelephone || 'Non renseigné' }}</dd>
            <dt class="col-5">Postulé le</dt><dd class="col-7">{{ formatDate(c.datePostulation) }}</dd>
            <dt class="col-5">Salaire souhaité</dt><dd class="col-7">{{ c.salairePropose != null ? formatMontant(c.salairePropose) : 'Non précisé' }}</dd>
            <dt class="col-5">Budget de l’offre</dt><dd class="col-7">{{ c.budget != null ? formatMontant(c.budget) : 'Non renseigné' }}</dd>
            <dt class="col-5">Type d’emploi</dt><dd class="col-7">{{ c.typeEmploi || 'Non imposé' }}</dd>
            <dt class="col-5">Niveau requis</dt><dd class="col-7">{{ c.niveauRequis || 'Non imposé' }}</dd>
          </dl>
          <p v-if="c.motifRejet" class="alert alert-secondary mt-3 mb-0 small">Motif du rejet : {{ c.motifRejet }}</p>
          <div v-if="c.lettreMotivation" class="mt-3"><h3 class="h6">Lettre de motivation</h3><p class="small mb-0" style="white-space: pre-line">{{ c.lettreMotivation }}</p></div>
        </div></section>

        <section class="card shadow-sm"><div class="card-body">
          <h2 class="h5">Entretiens</h2>
          <p v-if="!c.entretiens.length" class="text-body-secondary mb-0">Aucun entretien planifié.</p>
          <ul v-else class="list-group list-group-flush">
            <li v-for="e in c.entretiens" :key="e.id" class="list-group-item px-0">
              <div class="d-flex justify-content-between gap-2"><strong>{{ formatDate(e.date) }}<span v-if="e.type"> · {{ libelle(e.type) }}</span></strong><span class="badge text-bg-light">{{ libelle(e.statut) }}</span></div>
              <div v-if="e.lieu" class="small text-body-secondary">{{ e.lieu }}</div>
              <div v-if="e.decision" class="small mt-1">Décision : <strong>{{ libelle(e.decision) }}</strong></div>
              <div v-if="e.observations" class="small">{{ e.observations }}</div>
            </li>
          </ul>
        </div></section>

        <section v-if="c.proposition" class="card shadow-sm mt-4"><div class="card-body">
          <div class="d-flex justify-content-between gap-2"><h2 class="h5">Proposition d’embauche</h2><span class="badge text-bg-light">{{ libelle(c.proposition.statut) }}</span></div>
          <dl class="row mb-0 small">
            <dt class="col-5">Contrat</dt><dd class="col-7">{{ c.proposition.typeContrat }}</dd>
            <dt class="col-5">Salaire brut</dt><dd class="col-7">{{ c.proposition.salaireBrut != null ? formatMontant(c.proposition.salaireBrut) : 'Non renseigné' }}</dd>
            <dt class="col-5">Début souhaité</dt><dd class="col-7">{{ formatDate(c.proposition.dateDebut) }}</dd>
            <dt v-if="c.proposition.dateFin" class="col-5">Fin</dt><dd v-if="c.proposition.dateFin" class="col-7">{{ formatDate(c.proposition.dateFin) }}</dd>
            <dt class="col-5">Réponse avant le</dt><dd class="col-7">{{ formatDate(c.proposition.dateLimiteReponse) }}</dd>
            <template v-if="c.proposition.motifRefus"><dt class="col-5">Motif de refus</dt><dd class="col-7">{{ c.proposition.motifRefus }}</dd></template>
          </dl>
          <RouterLink v-if="estRh && c.statut === 'RETENUE'" class="btn btn-sm btn-outline-success mt-3" :to="{ name: 'rh-confirmation', query: { candidatureId: c.id } }">Confirmer le recrutement</RouterLink>
        </div></section>
      </div>

      <div class="col-lg-7">
        <section class="card shadow-sm"><div class="card-body">
          <h2 class="h5">Curriculum vitæ</h2>
          <p v-if="!cv" class="text-body-secondary mb-0">Aucun CV structuré n’est disponible (CV importé ou non renseigné).</p>
          <template v-else>
            <p v-if="cv.profil" style="white-space: pre-line">{{ cv.profil }}</p>
            <h3 class="h6 mt-3">Formations</h3>
            <p v-if="!cv.formations?.length && !cv.diplome" class="text-body-secondary small">Aucune formation renseignée.</p>
            <ul class="list-unstyled small">
              <li v-for="(f, i) in cv.formations" :key="i" class="mb-1"><strong>{{ f.diplome }}</strong><span v-if="f.etablissement"> · {{ f.etablissement }}</span><span v-if="f.annee"> · {{ f.annee }}</span></li>
              <li v-if="!cv.formations?.length && cv.diplome"><strong>{{ cv.diplome }}</strong><span v-if="cv.etablissement"> · {{ cv.etablissement }}</span><span v-if="cv.annee"> · {{ cv.annee }}</span></li>
            </ul>
            <h3 class="h6 mt-3">Expériences professionnelles</h3>
            <p v-if="!cv.experiences?.length && !cv.poste" class="text-body-secondary small">Aucune expérience renseignée.</p>
            <ul class="list-unstyled small mb-0">
              <li v-for="(x, i) in cv.experiences" :key="i" class="mb-3">
                <strong>{{ x.poste }}</strong><span v-if="x.entreprise"> · {{ x.entreprise }}</span><span v-if="x.periode" class="text-body-secondary"> · {{ x.periode }}</span>
                <div class="mt-1"><span v-if="x.typeEmploi" class="badge text-bg-light me-1">{{ libelle(x.typeEmploi) }}</span><span v-if="x.niveau" class="badge text-bg-light">{{ libelle(x.niveau) }}</span></div>
                <div v-if="x.missions" style="white-space: pre-line">{{ x.missions }}</div>
              </li>
              <li v-if="!cv.experiences?.length && cv.poste"><strong>{{ cv.poste }}</strong><span v-if="cv.entreprise"> · {{ cv.entreprise }}</span><span v-if="cv.periode"> · {{ cv.periode }}</span><div v-if="cv.missions">{{ cv.missions }}</div></li>
            </ul>
          </template>
        </div></section>
      </div>
    </div>
  </div>
</template>
