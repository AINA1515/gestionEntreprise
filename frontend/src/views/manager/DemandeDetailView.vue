<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import StepTimeline from '@/components/ui/StepTimeline.vue'

const route = useRoute()
const router = useRouter()
const store = useDemandesStore()
const { formatDate, formatMontant } = useFormatters()
const { statut } = useLabels()
const detail = computed(() => store.demandeSelectionnee)
const demande = computed(() => detail.value?.demande)
const actions = computed(() => detail.value?.actionsDisponibles || [])
const validationRefus = computed(() => [...(detail.value?.historiqueValidations || [])].reverse().find((item) => item.decision === 'REFUSEE'))
const progression = computed(() => {
  switch (demande.value?.statut) {
    case 'BROUILLON': return { index: 0, stopped: false }
    case 'ATTENTE_RH': return { index: 1, stopped: false }
    case 'ATTENTE_DIRECTION': return { index: 2, stopped: false }
    case 'VALIDEE': return { index: 3, stopped: false }
    case 'REFUSEE': return { index: validationRefus.value?.etape === 'DIRECTION' ? 2 : 1, stopped: true }
    default: return { index: 0, stopped: false }
  }
})
const brouillon = computed(() => demande.value?.statut === 'BROUILLON' && actions.value.includes('soumettre'))

async function soumettre() {
  if (!window.confirm('Soumettre cette demande à l’équipe RH ? Elle ne sera ensuite plus modifiable.')) return
  if (await store.soumettre(demande.value.id)) await recharger()
}
async function supprimer() {
  if (!window.confirm('Supprimer définitivement ce brouillon ?')) return
  if (await store.supprimer(demande.value.id)) router.push({ name: 'manager-demandes' })
}
async function recharger() { await store.obtenir(route.params.id) }

onMounted(recharger)
</script>

<template>
  <PageHeader title="Détail de la demande" :breadcrumb="['Espace Manager', 'Mes demandes', 'Détail']"
    subtitle="Suivez l’avancement et consultez les décisions enregistrées." />
  <AlertMessage :message="store.error" />
  <div v-if="store.loading && !demande" class="text-center py-5">Chargement…</div>
  <template v-else-if="demande">
    <section class="card shadow-sm mb-4">
      <div class="card-body p-4">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
          <div><h2 class="h4 mb-1">{{ demande.poste }}</h2><p class="text-body-secondary mb-0">{{ demande.departement }} · Demande #{{ demande.id }}</p></div>
          <StatusBadge type="demande" :value="demande.statut" />
        </div>
        <StepTimeline :steps="['Manager', 'RH', 'Direction', 'Ouvert']" :current-index="progression.index" :stopped="progression.stopped" />
        <div class="row g-3 mt-4">
          <div class="col-md-6"><span class="small text-body-secondary">Type de demande</span><div>{{ demande.typeDemande === 'REMPLACEMENT' ? 'Remplacement' : 'Création' }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Niveau d’expérience requis</span><div>{{ demande.niveauFormationLibelle || 'Non précisé' }}</div></div><div class="col-md-6"><span class="small text-body-secondary">Type d’emploi</span><div>{{ demande.typeEmploiLibelle || 'Non précisé' }}</div></div>
          <div v-if="demande.employeRemplaceId" class="col-md-6"><span class="small text-body-secondary">Salarié remplacé</span><div>#{{ demande.employeRemplaceId }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Effectif</span><div>{{ demande.effectif }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Budget estimé</span><div>{{ formatMontant(demande.budget) }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Date souhaitée</span><div>{{ formatDate(demande.dateSouhaitee) }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Créée le</span><div>{{ formatDate(demande.dateDemande) }}</div></div>
          <div class="col-12"><span class="small text-body-secondary">Motif</span><p class="mb-0">{{ demande.motif }}</p></div>
        </div>
        <div class="d-flex flex-wrap gap-2 mt-4">
          <RouterLink v-if="brouillon" class="btn btn-outline-primary" :to="{ name: 'manager-demande-modifier', params: { id: demande.id } }">Modifier</RouterLink>
          <button v-if="brouillon" class="btn btn-primary" :disabled="store.loading" @click="soumettre">Soumettre à la RH</button>
          <button v-if="brouillon" class="btn btn-outline-danger" :disabled="store.loading" @click="supprimer">Supprimer</button>
          <RouterLink class="btn btn-link" :to="{ name: 'manager-demandes' }">Retour à mes demandes</RouterLink>
        </div>
      </div>
    </section>

    <section class="card shadow-sm mb-4">
      <div class="card-body p-4">
        <h2 class="h5 mb-3">Historique des validations</h2>
        <div v-if="validationRefus" class="alert alert-danger">
          <strong>Demande refusée{{ validationRefus.etape === 'DIRECTION' ? ' par la Direction' : ' par les RH' }}.</strong>
          <div>{{ validationRefus.justification }}</div>
        </div>
        <div v-if="!detail.historiqueValidations?.length" class="text-body-secondary">Aucune décision enregistrée.</div>
        <ol v-else class="list-group list-group-numbered">
          <li v-for="validation in detail.historiqueValidations" :key="validation.id" class="list-group-item">
            <div class="d-flex flex-wrap justify-content-between gap-2">
              <strong>{{ validation.etape === 'RH' ? 'Ressources humaines' : 'Direction' }}</strong>
              <span>{{ formatDate(validation.dateDecision) }}</span>
            </div>
            <div class="small">{{ validation.valideur }} · {{ statut('demande', validation.decision).label }}</div>
            <p v-if="validation.justification" class="mb-0 mt-2">{{ validation.justification }}</p>
          </li>
        </ol>
      </div>
    </section>

    <section v-if="detail.offresLiees?.length" class="card shadow-sm">
      <div class="card-body p-4"><h2 class="h5">Offres liées</h2><ul class="mb-0"><li v-for="offre in detail.offresLiees" :key="offre.id">{{ offre.titre }} — {{ offre.statut }}</li></ul></div>
    </section>
  </template>
</template>
