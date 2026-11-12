<script setup>
import { computed, onMounted, ref } from 'vue'
import { onboardingService } from '@/services/onboardingService'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const collaborateurs = ref([])
const employeId = ref('')
const parcours = ref(null)
const responsableGlobal = ref('')
const responsablesChoisis = ref({})
const tri = ref('echeance')
const loading = ref(false)
const error = ref('')
const success = ref('')
const { formatDate } = useFormatters()
const nonAttribuees = computed(() => parcours.value?.taches.filter((t) => t.statut !== 'VALIDEE' && !t.assigneeId).length ?? 0)
const terminees = computed(() => parcours.value?.taches.filter((t) => t.statut === 'VALIDEE').length ?? 0)
const tachesTriees = computed(() => {
  const taches = [...(parcours.value?.taches ?? [])]
  const nom = (valeur) => (valeur || '').toLocaleLowerCase('fr')
  const comparateurs = {
    echeance: (a, b) => (a.dateLimite || '9999-12-31').localeCompare(b.dateLimite || '9999-12-31'),
    intitule: (a, b) => nom(a.intitule).localeCompare(nom(b.intitule), 'fr'),
    statut: (a, b) => ({ A_FAIRE: 0, EN_COURS: 1, VALIDEE: 2 }[a.statut] ?? 3) - ({ A_FAIRE: 0, EN_COURS: 1, VALIDEE: 2 }[b.statut] ?? 3),
    service: (a, b) => nom(a.serviceResponsable).localeCompare(nom(b.serviceResponsable), 'fr'),
  }
  return taches.sort((a, b) => comparateurs[tri.value](a, b) || a.id - b.id)
})

function preparerAffectations(donnees) {
  responsablesChoisis.value = Object.fromEntries(donnees.taches.map((t) => [t.id, t.assigneeId ? String(t.assigneeId) : '']))
  responsableGlobal.value = ''
}

function nomResponsable(utilisateurId) {
  return parcours.value?.assignables.find((u) => String(u.utilisateurId) === String(utilisateurId))?.nom
    || (utilisateurId ? 'Compte utilisateur' : 'Non attribuée')
}

function responsableDisponible(utilisateurId) {
  return parcours.value?.assignables.some((u) => String(u.utilisateurId) === String(utilisateurId)) ?? false
}

async function chargerCollaborateurs() {
  loading.value = true
  error.value = ''
  try {
    collaborateurs.value = await onboardingService.employesManager() || []
    if (collaborateurs.value.length && !employeId.value) {
      employeId.value = String(collaborateurs.value[0].id)
      await chargerParcours()
    }
  } catch (e) { error.value = e.message || 'Impossible de charger vos salariés.' }
  finally { loading.value = false }
}

async function chargerParcours() {
  if (!employeId.value) { parcours.value = null; return }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    parcours.value = await onboardingService.tachesManager(employeId.value)
    preparerAffectations(parcours.value)
  } catch (e) { parcours.value = null; error.value = e.message || 'Impossible de charger les tâches de ce salarié.' }
  finally { loading.value = false }
}

async function enregistrerAffectation(tache) {
  if (!employeId.value) return
  const utilisateurId = responsablesChoisis.value[tache.id] ? Number(responsablesChoisis.value[tache.id]) : null
  if ((tache.assigneeId ?? null) === utilisateurId) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await onboardingService.assignerTache(tache.id, utilisateurId)
    success.value = utilisateurId ? 'Le responsable de la tâche a été enregistré.' : 'La tâche n’est plus attribuée.'
    await chargerParcours()
  } catch (e) { error.value = e.message || 'Impossible d’attribuer cette tâche.' }
  finally { loading.value = false }
}

async function attribuerToutes() {
  if (!responsableGlobal.value || !parcours.value) return
  const enAttente = parcours.value.taches.filter((t) => t.statut !== 'VALIDEE' && !t.assigneeId)
  if (!enAttente.length) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    for (const tache of enAttente) {
      await onboardingService.assignerTache(tache.id, Number(responsableGlobal.value))
    }
    success.value = `${enAttente.length} tâche(s) attribuée(s).`
    await chargerParcours()
  } catch (e) { error.value = e.message || 'Impossible d’attribuer toutes les tâches.' }
  finally { loading.value = false }
}

async function genererTaches() {
  if (!employeId.value) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    parcours.value = await onboardingService.initialiserManager(employeId.value)
    preparerAffectations(parcours.value)
    success.value = parcours.value.taches.length
      ? 'La liste des tâches d’intégration a été créée.'
      : 'Aucun modèle de tâche actif n’est configuré dans la base.'
  } catch (e) { error.value = e.message || 'Impossible de générer les tâches d’intégration.' }
  finally { loading.value = false }
}

onMounted(chargerCollaborateurs)
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Onboarding des salariés" :breadcrumb="['Espace Manager', 'Onboarding']" subtitle="Attribuez les tâches d’intégration aux membres de votre équipe." />
    <AlertMessage :message="error" />
    <div v-if="success" class="alert alert-success" role="status">{{ success }}</div>
    <section class="card shadow-sm mb-4"><div class="card-body">
      <label class="form-label" for="employe-onboarding">Salarié</label>
      <select id="employe-onboarding" v-model="employeId" class="form-select" @change="chargerParcours">
        <option value="">Choisir un salarié</option>
        <option v-for="e in collaborateurs" :key="e.id" :value="String(e.id)">{{ e.prenom }} {{ e.nom }} — {{ e.matricule }}</option>
      </select>
    </div></section>
    <section class="card shadow-sm">
      <div v-if="loading" class="card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2"/>Chargement…</div>
      <div v-else-if="!employeId" class="card-body text-center text-body-secondary py-5">Aucun salarié ne vous est rattaché.</div>
      <div v-else-if="!parcours" class="card-body text-center text-body-secondary py-5">Aucun parcours d’onboarding disponible pour ce salarié.</div>
      <div v-else-if="!parcours.taches.length" class="card-body text-center py-5">
        <p class="text-body-secondary">Aucune tâche n’a encore été créée pour ce salarié.</p>
        <button class="btn btn-primary" type="button" :disabled="loading" @click="genererTaches">
          <i class="bi bi-list-check me-2" aria-hidden="true"></i>Générer la liste des tâches d’intégration
        </button>
      </div>
      <template v-else>
      <div class="card-body border-bottom">
        <div class="d-flex flex-wrap gap-2 mb-3">
          <span class="badge text-bg-light border">{{ parcours.taches.length }} tâche(s)</span>
          <span class="badge text-bg-warning">{{ nonAttribuees }} sans responsable</span>
          <span class="badge text-bg-success">{{ terminees }} terminée(s)</span>
        </div>
        <div class="row g-2 align-items-end">
          <div class="col-md-8">
            <label class="form-label" for="responsable-global">Attribuer toutes les tâches sans responsable</label>
            <select id="responsable-global" v-model="responsableGlobal" class="form-select" :disabled="loading || !parcours.assignables.length">
              <option value="">Choisir un responsable</option>
              <option v-for="u in parcours.assignables" :key="u.utilisateurId" :value="String(u.utilisateurId)">{{ u.nom }}</option>
            </select>
          </div>
          <div class="col-md-4"><button class="btn btn-primary w-100" type="button" :disabled="loading || !responsableGlobal || !nonAttribuees" @click="attribuerToutes">
            <i class="bi bi-people me-2" aria-hidden="true"></i>Attribuer {{ nonAttribuees }} tâche(s)
          </button></div>
        </div>
        <p v-if="!parcours.assignables.length" class="small text-danger mt-2 mb-0">Aucun utilisateur actif n’est disponible dans le département pour recevoir des tâches.</p>
      </div>
      <div class="card-body border-bottom py-2">
        <div class="row align-items-center g-2">
          <div class="col-auto"><label class="form-label mb-0" for="tri-taches">Trier par</label></div>
          <div class="col-sm-6 col-md-4"><select id="tri-taches" v-model="tri" class="form-select form-select-sm">
            <option value="echeance">Échéance, de la plus proche</option>
            <option value="intitule">Intitulé, A à Z</option>
            <option value="statut">Statut, tâches à faire d’abord</option>
            <option value="service">Service responsable, A à Z</option>
          </select></div>
        </div>
      </div>
      <div class="list-group list-group-flush">
        <article v-for="tache in tachesTriees" :key="tache.id" class="list-group-item p-3">
          <div class="d-flex flex-wrap justify-content-between gap-2 mb-3">
            <div><h2 class="h6 mb-1">{{ tache.intitule }}</h2><span class="small text-body-secondary">{{ tache.serviceResponsable }} · Échéance : {{ formatDate(tache.dateLimite) }}</span><div class="small mt-1">Responsable actuel : <strong>{{ nomResponsable(tache.assigneeId) }}</strong></div></div>
            <span class="badge text-bg-light align-self-start">{{ tache.statut.replaceAll('_', ' ') }}</span>
          </div>
          <div class="row g-2 align-items-end">
            <div class="col-md-8">
              <label class="form-label" :for="`responsable-${tache.id}`">Assigner à</label>
              <select :id="`responsable-${tache.id}`" v-model="responsablesChoisis[tache.id]" class="form-select" :disabled="loading || tache.statut === 'VALIDEE' || (!parcours.assignables.length && !tache.assigneeId)">
                <option value="">Aucun responsable</option>
                <option v-if="tache.assigneeId && !responsableDisponible(tache.assigneeId)" :value="String(tache.assigneeId)">Responsable actuel — indisponible</option>
                <option v-for="u in parcours.assignables" :key="u.utilisateurId" :value="String(u.utilisateurId)">{{ u.nom }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <button v-if="tache.statut !== 'VALIDEE'" class="btn btn-outline-primary w-100" type="button" :disabled="loading || String(responsablesChoisis[tache.id] || '') === String(tache.assigneeId || '')" @click="enregistrerAffectation(tache)">
                <i class="bi bi-person-check me-2" aria-hidden="true"></i>Enregistrer le responsable
              </button>
              <span v-else class="badge text-bg-success w-100 p-2"><i class="bi bi-check-circle me-2" aria-hidden="true"></i>Déjà faite</span>
            </div>
          </div>
        </article>
      </div>
      </template>
    </section>
  </div>
</template>
