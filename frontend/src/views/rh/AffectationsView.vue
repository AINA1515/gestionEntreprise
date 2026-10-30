<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { employeService } from '@/services/employeService'
import { referentielService } from '@/services/referentielService'
import { affectationService } from '@/services/affectationService'
import { useFormatters } from '@/composables/useFormatters'
import PageHeader from '@/components/ui/PageHeader.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import FormField from '@/components/ui/FormField.vue'

const { formatDate } = useFormatters()
const aujourdhui = new Date().toISOString().slice(0, 10)

const employes = ref([])
const postesDisponibles = ref([])
const historique = ref([])
const chargement = ref(true)
const enregistrement = ref(false)
const erreur = ref('')
const succes = ref('')
const form = reactive({ employeId: '', nouveauPosteId: '', dateEffet: aujourdhui, motif: '', nouveauSalaire: '' })

const employe = computed(() => employes.value.find((e) => e.id === form.employeId) ?? null)
const posteActuel = computed(() => historique.value.find((a) => a.courante) ?? null)
// Le poste actuel n'est jamais proposé : on ne peut pas muter vers le poste que l'on occupe déjà.
const choixPostes = computed(() => postesDisponibles.value.filter((p) => p.id !== posteActuel.value?.posteId))
const peutValider = computed(() => form.employeId && form.nouveauPosteId && form.motif.trim() && !enregistrement.value)

async function chargerPostes() {
  const [vacants, crees] = await Promise.all([
    referentielService.listerPostes({ statut: 'VACANT', size: 500 }),
    referentielService.listerPostes({ statut: 'CREE', size: 500 }),
  ])
  postesDisponibles.value = [...(vacants.content ?? []), ...(crees.content ?? [])]
}

async function chargerHistorique() {
  historique.value = form.employeId ? await affectationService.historique(form.employeId) : []
}

async function initialiser() {
  try {
    const page = await employeService.lister({ statutActivite: 'ACTIF', size: 500 })
    employes.value = (page.content ?? []).map((e) => ({ ...e, nomComplet: `${e.prenom} ${e.nom} (${e.matricule})` }))
    await chargerPostes()
  } catch (e) { erreur.value = e.message || 'Impossible de charger les données.' }
  finally { chargement.value = false }
}

watch(() => form.employeId, async () => {
  form.nouveauPosteId = ''
  succes.value = ''
  try { await chargerHistorique() } catch (e) { erreur.value = e.message || 'Impossible de charger l’historique.' }
})

async function valider() {
  erreur.value = ''
  succes.value = ''
  enregistrement.value = true
  try {
    const nom = employe.value?.nomComplet
    const poste = choixPostes.value.find((p) => p.id === form.nouveauPosteId)
    await affectationService.muter({
      employeId: form.employeId,
      nouveauPosteId: form.nouveauPosteId,
      dateEffet: form.dateEffet || null,
      motif: form.motif.trim(),
      nouveauSalaire: form.nouveauSalaire === '' ? null : Number(form.nouveauSalaire),
    })
    succes.value = `${nom} est maintenant affecté(e) au poste « ${poste?.intitule ?? ''} ». Son ancien poste est redevenu vacant.`
    form.nouveauPosteId = ''
    form.motif = ''
    form.nouveauSalaire = ''
    await Promise.all([chargerHistorique(), chargerPostes()])
  } catch (e) { erreur.value = e.message || 'Impossible d’enregistrer l’affectation.' }
  finally { enregistrement.value = false }
}

onMounted(initialiser)
</script>

<template>
  <div class="container-fluid py-3">
    <PageHeader title="Affectations" :breadcrumb="['Espace Ressources humaines', 'Affectations']"
      subtitle="Faites changer un salarié en poste de poste : il quitte l’ancien (qui redevient vacant) pour le nouveau." />
    <AlertMessage :message="erreur" />
    <div v-if="succes" class="alert alert-success" role="status">{{ succes }}</div>
    <div v-if="chargement" class="card card-body text-center py-5" role="status"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true" />Chargement…</div>

    <div v-else class="row g-4">
      <div class="col-lg-6">
        <form class="card shadow-sm" @submit.prevent="valider">
          <div class="card-body">
            <h2 class="h5">Nouvelle affectation</h2>
            <p class="small text-body-secondary">Réservé aux salariés actifs. Pour un candidat retenu, l’affectation est créée automatiquement à l’embauche.</p>
            <FormField id="aff-employe" label="Salarié">
              <select id="aff-employe" v-model="form.employeId" class="form-select" required>
                <option value="" disabled>Choisir un salarié</option>
                <option v-for="e in employes" :key="e.id" :value="e.id">{{ e.nomComplet }}</option>
              </select>
            </FormField>
            <p v-if="posteActuel" class="small mb-3">Poste actuel : <strong>{{ posteActuel.posteIntitule }}</strong> · {{ posteActuel.departement }} · depuis le {{ formatDate(posteActuel.dateDebut) }}</p>
            <FormField id="aff-poste" label="Nouveau poste (vacant)">
              <select id="aff-poste" v-model="form.nouveauPosteId" class="form-select" :disabled="!form.employeId" required>
                <option value="" disabled>{{ choixPostes.length ? 'Choisir un poste' : 'Aucun poste vacant' }}</option>
                <option v-for="p in choixPostes" :key="p.id" :value="p.id">{{ p.intitule }} — {{ p.departement }} ({{ p.codePoste }})</option>
              </select>
            </FormField>
            <div class="row g-3">
              <div class="col-md-6"><FormField id="aff-date" label="Date d’effet"><input id="aff-date" v-model="form.dateEffet" type="date" class="form-control" :max="aujourdhui" required /></FormField></div>
              <div class="col-md-6"><FormField id="aff-salaire" label="Nouveau salaire brut (facultatif)"><input id="aff-salaire" v-model="form.nouveauSalaire" type="number" min="0" step="0.01" class="form-control" placeholder="Inchangé" /></FormField></div>
            </div>
            <FormField id="aff-motif" label="Motif">
              <textarea id="aff-motif" v-model="form.motif" class="form-control" rows="2" maxlength="300" placeholder="Mutation, promotion, réorganisation…" required></textarea>
            </FormField>
            <p class="small text-body-secondary">Un avenant est créé sur le nouveau poste ; le contrat précédent est conservé et clôturé.</p>
            <button class="btn btn-primary" type="submit" :disabled="!peutValider">{{ enregistrement ? 'Enregistrement…' : 'Valider l’affectation' }}</button>
          </div>
        </form>
      </div>

      <div class="col-lg-6">
        <section class="card shadow-sm h-100"><div class="card-body">
          <h2 class="h5">Historique des affectations</h2>
          <p v-if="!form.employeId" class="text-body-secondary mb-0">Choisissez un salarié pour voir son parcours.</p>
          <p v-else-if="!historique.length" class="text-body-secondary mb-0">Aucune affectation enregistrée.</p>
          <ol v-else class="list-group list-group-flush list-group-numbered">
            <li v-for="a in historique" :key="a.id" class="list-group-item px-0">
              <div class="ms-2">
                <div class="d-flex flex-wrap justify-content-between gap-2"><strong>{{ a.posteIntitule }}</strong><span class="badge" :class="a.courante ? 'text-bg-success' : 'text-bg-light'">{{ a.courante ? 'En cours' : 'Terminée' }}</span></div>
                <div class="small text-body-secondary">{{ a.departement }} · du {{ formatDate(a.dateDebut) }}<span v-if="a.dateFin"> au {{ formatDate(a.dateFin) }}</span></div>
                <div v-if="a.motif" class="small">{{ a.motif }}</div>
              </div>
            </li>
          </ol>
          <RouterLink v-if="form.employeId" class="btn btn-sm btn-outline-secondary mt-3" :to="{ name: 'rh-employe-detail', params: { id: form.employeId } }">Voir la fiche salarié</RouterLink>
        </div></section>
      </div>
    </div>
  </div>
</template>
