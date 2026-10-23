<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDemandesStore } from '@/stores/demandes'
import { useReferentielStore } from '@/stores/referentiel'
import { useFormatters } from '@/composables/useFormatters'
import { useAuthStore } from '@/stores/auth'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import FormField from '@/components/ui/FormField.vue'
import AlertMessage from '@/components/ui/AlertMessage.vue'

const route = useRoute()
const router = useRouter()
const demandes = useDemandesStore()
const auth = useAuthStore()
const referentiel = useReferentielStore()
const { formatDate, formatMontant } = useFormatters()
const commentaire = ref('')
const detail = computed(() => demandes.demandeSelectionnee)
const demande = computed(() => detail.value?.demande)
const actions = computed(() => detail.value?.actionsDisponibles || [])
const offreExistante = computed(() => detail.value?.offresLiees?.[0] || null)
const derniereRefus = computed(() => [...(detail.value?.historiqueValidations || [])].reverse()
  .find((validation) => validation.decision === 'REFUSEE'))
const poste = computed(() => referentiel.postes.find((item) => item.id === demande.value?.posteId))
const departement = computed(() => referentiel.departementSelectionne)
const conformite = computed(() => [
  { label: 'Poste et département actifs', ok: Boolean(poste.value && poste.value.statut !== 'FERME' && departement.value?.estActif) },
  { label: 'Budget cohérent (nul ou positif)', ok: demande.value?.budget == null || Number(demande.value.budget) >= 0 },
  { label: 'Effectif supérieur à zéro', ok: Number(demande.value?.effectif) > 0 },
])
const conforme = computed(() => conformite.value.every((item) => item.ok))
const peutTraiter = computed(() => demande.value?.statut === 'ATTENTE_RH')

async function charger() {
  const data = await demandes.obtenir(route.params.id)
  if (!data?.demande) return
  await Promise.all([
    referentiel.chargerPostes(data.demande.departementId),
    referentiel.chargerDepartement(data.demande.departementId),
  ])
}
async function traiter(action) {
  if (!actions.value.includes(action.toLowerCase())) return
  if (['REFUSER', 'RENVOYER_MANAGER'].includes(action) && !commentaire.value.trim()) return
  const result = await demandes.valider(route.params.id, { action, justification: commentaire.value.trim() || null })
  if (result) { commentaire.value = ''; await charger() }
}
onMounted(charger)
</script>

<template>
  <PageHeader title="Revue RH de la demande" :breadcrumb="['Espace RH', 'Demandes à traiter', 'Revue']"
    subtitle="Contrôlez les informations et choisissez l’action adaptée." />
  <AlertMessage :message="demandes.error" />
  <div v-if="demandes.loading && !demande" class="text-center py-5">Chargement…</div>
  <template v-else-if="demande">
    <section class="card shadow-sm mb-4">
      <div class="card-body p-4">
        <div class="d-flex flex-wrap justify-content-between gap-3 mb-4"><div><h2 class="h4 mb-1">{{ demande.poste }}</h2><p class="text-body-secondary mb-0">{{ demande.departement }} · Demande #{{ demande.id }}</p></div><StatusBadge type="demande" :value="demande.statut" /></div>
        <div class="row g-3">
          <div class="col-md-6"><span class="small text-body-secondary">Manager demandeur</span><div>{{ demande.manager }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Type</span><div>{{ demande.typeDemande }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Effectif demandé</span><div>{{ demande.effectif }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Niveau de formation requis</span><div>{{ demande.niveauFormationLibelle || 'Non précisé' }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Budget alloué</span><div>{{ formatMontant(demande.budget) }}</div></div>
          <div class="col-md-6"><span class="small text-body-secondary">Date souhaitée</span><div>{{ formatDate(demande.dateSouhaitee) }}</div></div>
          <div class="col-12"><span class="small text-body-secondary">Motif</span><p class="mb-0">{{ demande.motif }}</p></div>
        </div>
      </div>
    </section>

    <section class="card shadow-sm mb-4"><div class="card-body p-4">
      <h2 class="h5 mb-3">Checklist de conformité</h2>
      <ul class="list-unstyled mb-0"><li v-for="item in conformite" :key="item.label" class="mb-2">
        <i :class="['bi me-2', item.ok ? 'bi-check-circle-fill text-success' : 'bi-exclamation-circle-fill text-danger']" aria-hidden="true"></i>{{ item.label }}
        <span v-if="item.label.startsWith('Poste')" class="small text-body-secondary">({{ poste?.statut || 'statut indisponible' }}{{ departement ? ` · ${departement.estActif ? 'département actif' : 'département inactif'}` : '' }})</span>
      </li></ul>
    </div></section>

    <section v-if="peutTraiter" class="card shadow-sm"><div class="card-body p-4">
      <h2 class="h5">Décision RH</h2>
      <p v-if="!conforme" class="text-danger small">La demande ne satisfait pas tous les contrôles. Vous pouvez la renvoyer ou la refuser avec une justification.</p>
      <FormField id="commentaire" label="Commentaire ou justification" :hint="'Obligatoire pour un renvoi au manager ou un refus.'">
        <textarea id="commentaire" v-model="commentaire" class="form-control" rows="3" maxlength="1000" placeholder="Expliquez les corrections attendues ou le motif de refus."></textarea>
      </FormField>
      <div class="d-flex flex-wrap gap-2">
        <button v-if="actions.includes('transmettre_direction')" class="btn btn-primary" :disabled="!conforme || demandes.loading" @click="traiter('TRANSMETTRE_DIRECTION')">Transmettre à la Direction</button>
        <button v-if="actions.includes('renvoyer_manager')" class="btn btn-outline-secondary" :disabled="!commentaire.trim() || demandes.loading" @click="traiter('RENVOYER_MANAGER')">Renvoyer au manager</button>
        <button v-if="actions.includes('refuser')" class="btn btn-outline-danger" :disabled="!commentaire.trim() || demandes.loading" @click="traiter('REFUSER')">Refuser</button>
      </div>
    </div></section>
    <section v-else-if="demande.statut === 'VALIDEE'" class="card border-success shadow-sm"><div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
      <div><h2 class="h5 mb-1">Demande validée par la Direction</h2><p class="mb-0 text-body-secondary">
        {{ offreExistante ? `Une offre « ${offreExistante.titre} » est déjà associée à cette demande (${offreExistante.statut.toLowerCase()}).` : 'La demande est revenue à l’équipe RH. Vous pouvez créer puis publier l’offre.' }}
      </p></div>
      <RouterLink v-if="auth.hasPermission('OFFRE_PUBLIER') && !offreExistante" class="btn btn-success" :to="{ name: 'rh-offre-nouvelle', query: { demandeId: demande.id } }">Créer l’offre d’emploi</RouterLink>
      <RouterLink v-else-if="offreExistante" class="btn btn-outline-primary" to="/rh/offres">Voir les offres</RouterLink>
    </div></section>
    <section v-else-if="demande.statut === 'REFUSEE' && derniereRefus" class="card border-danger shadow-sm"><div class="card-body">
      <h2 class="h5 text-danger">Demande refusée à l’étape {{ derniereRefus.etape === 'DIRECTION' ? 'Direction' : 'RH' }}</h2>
      <p class="mb-1">La demande a été refusée. Elle ne peut pas donner lieu à la publication d’une offre.</p>
      <p v-if="derniereRefus.justification" class="mb-0"><strong>Motif :</strong> {{ derniereRefus.justification }}</p>
    </div></section>
    <RouterLink class="btn btn-link px-0 mt-3" :to="{ name: 'rh-demandes-a-traiter' }">Retour à la file RH</RouterLink>
  </template>
</template>
