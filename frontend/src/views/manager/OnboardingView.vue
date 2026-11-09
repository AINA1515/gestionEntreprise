<script setup>
import { onMounted, ref } from 'vue'
import { onboardingService } from '@/services/onboardingService'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const collaborateurs = ref([])
const employeId = ref('')
const parcours = ref(null)
const affectations = ref({})
const loading = ref(false)
const error = ref('')
const success = ref('')
const { formatDate } = useFormatters()

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
    affectations.value = Object.fromEntries(parcours.value.taches.map((t) => [t.id, t.assigneeId ? String(t.assigneeId) : '']))
  } catch (e) { parcours.value = null; error.value = e.message || 'Impossible de charger les tâches de ce salarié.' }
  finally { loading.value = false }
}

async function assigner(tache) {
  if (!employeId.value) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    let utilisateurId = null
    if (!tache.assigneeId) {
      const responsable = parcours.value.assignables.find((u) => Number(u.employeId) === Number(employeId.value))
      if (!responsable) {
        error.value = 'Ce salarié ne possède pas encore de compte utilisateur actif.'
        return
      }
      utilisateurId = Number(responsable.utilisateurId)
    }
    await onboardingService.assignerTache(tache.id, utilisateurId)
    success.value = tache.assigneeId ? 'La tâche a été annulée.' : 'La tâche a été attribuée.'
    await chargerParcours()
  } catch (e) { error.value = e.message || 'Impossible d’attribuer cette tâche.' }
  finally { loading.value = false }
}

async function genererTaches() {
  if (!employeId.value) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    parcours.value = await onboardingService.initialiserManager(employeId.value)
    affectations.value = Object.fromEntries(parcours.value.taches.map((t) => [t.id, t.assigneeId ? String(t.assigneeId) : '']))
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
      <div v-else class="list-group list-group-flush">
        <article v-for="tache in parcours.taches" :key="tache.id" class="list-group-item p-3">
          <div class="d-flex flex-wrap justify-content-between gap-2 mb-3">
            <div><h2 class="h6 mb-1">{{ tache.intitule }}</h2><span class="small text-body-secondary">{{ tache.serviceResponsable }} · Échéance : {{ formatDate(tache.dateLimite) }}</span></div>
            <span class="badge text-bg-light align-self-start">{{ tache.statut.replaceAll('_', ' ') }}</span>
          </div>
          <div class="row g-2 align-items-end">
            <div class="col-md-8 small text-body-secondary">
              Responsable sélectionné : <strong>le salarié choisi ci-dessus</strong>
            </div>
            <div class="col-md-4">
              <button v-if="tache.statut !== 'VALIDEE'" :class="tache.assigneeId ? 'btn btn-outline-danger w-100' : 'btn btn-primary w-100'" type="button" :disabled="loading || !employeId" @click="assigner(tache)">
                <i :class="['bi me-2', tache.assigneeId ? 'bi-x-circle' : 'bi-person-plus']" aria-hidden="true"></i>{{ tache.assigneeId ? 'Annuler la tâche' : 'Attribuer la tâche' }}
              </button>
              <span v-else class="badge text-bg-success w-100 p-2"><i class="bi bi-check-circle me-2" aria-hidden="true"></i>Déjà faite</span>
            </div>
          </div>
          <p v-if="!parcours.assignables.length" class="small text-danger mt-2 mb-0">Aucun salarié avec un compte actif n’est disponible dans ce département.</p>
        </article>
      </div>
    </section>
  </div>
</template>
