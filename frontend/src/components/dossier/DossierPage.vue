<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { dossierService } from '@/services/dossierService'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { usePermissions } from '@/composables/usePermissions'
import { useToast } from '@/composables/useToast'
import AlertMessage from '@/components/ui/AlertMessage.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import IdentiteTab from './IdentiteTab.vue'
import CoordonneesTab from './CoordonneesTab.vue'
import SituationTab from './SituationTab.vue'
import ContratsTab from './ContratsTab.vue'
import CarriereTab from './CarriereTab.vue'
import RemunerationTab from './RemunerationTab.vue'
import DocumentsTab from './DocumentsTab.vue'
import IntegrationTab from './IntegrationTab.vue'
import HistoriqueTab from './HistoriqueTab.vue'

// Dossier administratif à onglets. Sans employeId : « Mon dossier » du salarié connecté ; sinon : dossier vu par la RH.
const props = defineProps({ employeId: { type: [Number, String], default: null } })
const route = useRoute()
const router = useRouter()
const { can } = usePermissions()
const toast = useToast()
const { loading, error, run } = useAsyncAction()
const { loading: saving, error: saveError, run: runSave } = useAsyncAction()
const dossier = ref(null)
const mode = computed(() => (props.employeId ? 'rh' : 'moi'))

async function charger() {
  dossier.value = null
  dossier.value = (await run(() => dossierService.obtenir(props.employeId))) ?? null
}
watch(() => props.employeId, charger, { immediate: true })

const onglets = computed(() => {
  const d = dossier.value
  if (!d) return []
  return [
    { code: 'identite', label: 'Identité', icon: 'person-vcard' },
    { code: 'coordonnees', label: 'Coordonnées', icon: 'geo-alt' },
    { code: 'situation', label: 'Situation professionnelle', icon: 'briefcase' },
    { code: 'contrats', label: 'Contrats', icon: 'file-earmark-text' },
    { code: 'carriere', label: 'Carrière', icon: 'signpost-split' },
    ...(d.droits.salaireVisible ? [{ code: 'remuneration', label: 'Rémunération', icon: 'cash-coin' }] : []),
    { code: 'documents', label: 'Documents', icon: 'folder2-open' },
    ...(mode.value === 'rh' && can('CONTRAT_GERER') ? [{ code: 'integration', label: 'Intégration', icon: 'person-check' }] : []),
    ...(can('AUDIT_CONSULTER') ? [{ code: 'historique', label: 'Historique', icon: 'clock-history' }] : []),
  ]
})
const actif = computed(() => (onglets.value.some((o) => o.code === route.query.onglet) ? route.query.onglet : 'identite'))
function choisir(code) { router.replace({ query: { ...route.query, onglet: code, ...(code === 'historique' && route.query.onglet ? { historiqueSource: route.query.onglet } : {}) } }) }
function ouvrirHistorique(source) { router.replace({ query: { ...route.query, onglet: 'historique', historiqueSource: source } }) }

const vide = (bloc) => Object.fromEntries(Object.entries(bloc || {}).map(([k, v]) => [k, v === '' ? null : v]))

/** Enregistre un ou plusieurs blocs modifiés ; renvoie true si le serveur a accepté. */
async function enregistrer(partiel) {
  const d = dossier.value
  const maj = await runSave(() => {
    if (mode.value === 'rh') {
      return dossierService.modifierRh(props.employeId, {
        version: d.version,
        identite: vide({ ...d.identite, ...partiel.identite }),
        coordonnees: vide({ ...d.coordonnees, ...partiel.coordonnees }),
        contactUrgence: vide({ ...d.contactUrgence, ...partiel.contactUrgence }),
        coordonneesBancaires: vide({ ...d.coordonneesBancaires, ...partiel.coordonneesBancaires }),
      })
    }
    const c = { ...d.coordonnees, ...partiel.coordonnees }
    const contactUrgence = { ...d.contactUrgence, ...partiel.contactUrgence }
    if (partiel.identite) {
      return dossierService.modifierIdentite({ version: d.version, identite: partiel.identite })
    }
    return dossierService.modifierCoordonnees({
        version: d.version,
        emailPersonnel: c.emailPersonnel,
        telephonePersonnel: c.telephonePersonnel,
        adresseLigne1: c.adresseLigne1,
        adresseLigne2: c.adresseLigne2,
        codePostal: c.codePostal,
        ville: c.ville,
        pays: c.pays,
        contactUrgence,
      })
  })
  if (!maj) {
    if (saveError.value?.includes('modifié entre-temps')) await charger()
    return false
  }
  dossier.value = maj
  toast.success(mode.value === 'moi'
    ? (partiel.identite ? 'Identité mise à jour.' : 'Coordonnées mises à jour.')
    : 'Dossier mis à jour.')
  return true
}
</script>

<template>
  <div>
    <AlertMessage :message="error" />
    <div v-if="loading && !dossier" class="card card-body text-center py-5" role="status">
      <span class="spinner-border spinner-border-sm me-2" aria-hidden="true" />Chargement du dossier…
    </div>
    <template v-else-if="dossier">
      <section class="card shadow-sm mb-3"><div class="card-body d-flex flex-wrap justify-content-between align-items-start gap-3">
        <div>
          <h1 class="h3 mb-1">{{ dossier.identite.prenom }} {{ dossier.identite.nom }}</h1>
          <p class="text-body-secondary mb-0">Matricule <strong>{{ dossier.matricule }}</strong>
            <span v-if="dossier.affectationCourante"> · {{ dossier.affectationCourante.intitule }} · {{ dossier.departement }}</span></p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <StatusBadge type="employe" :value="dossier.statutActivite" />
          <slot name="actions" />
        </div>
      </div></section>

      <ul class="nav nav-tabs mb-3 flex-nowrap overflow-auto" role="tablist">
        <li v-for="onglet in onglets" :key="onglet.code" class="nav-item" role="presentation">
          <div class="d-flex align-items-center"><button class="nav-link text-nowrap" :class="{ active: actif === onglet.code }" type="button" role="tab" :aria-selected="actif === onglet.code" @click="choisir(onglet.code)">
            <i :class="`bi bi-${onglet.icon} me-1`" aria-hidden="true"></i>{{ onglet.label }}
          </button><button v-if="!['historique', 'carriere', 'remuneration'].includes(onglet.code) && can('AUDIT_CONSULTER')" class="btn btn-sm btn-link text-secondary px-1" type="button" :title="`Historique : ${onglet.label}`" @click="ouvrirHistorique(onglet.code)"><i class="bi bi-clock-history" aria-hidden="true"></i></button></div>
        </li>
      </ul>

      <IdentiteTab v-if="actif === 'identite'" :dossier="dossier" :mode="mode" :enregistrer="enregistrer" :saving="saving" :erreur="saveError" />
      <CoordonneesTab v-else-if="actif === 'coordonnees'" :dossier="dossier" :mode="mode" :enregistrer="enregistrer" :saving="saving" :erreur="saveError" />
      <SituationTab v-else-if="actif === 'situation'" :dossier="dossier" />
      <ContratsTab v-else-if="actif === 'contrats'" :dossier="dossier" :mode="mode" />
      <CarriereTab v-else-if="actif === 'carriere'" :employe-id="mode === 'rh' ? dossier.id : null" />
      <RemunerationTab v-else-if="actif === 'remuneration'" :employe-id="mode === 'rh' ? dossier.id : null" :mode="mode" />
      <DocumentsTab v-else-if="actif === 'documents'" :dossier="dossier" :mode="mode" />
      <IntegrationTab v-else-if="actif === 'integration'" :employe-id="dossier.id" />
      <HistoriqueTab v-else-if="actif === 'historique'" :employe-id="dossier.id" :source="route.query.historiqueSource || ''" />
    </template>
  </div>
</template>
