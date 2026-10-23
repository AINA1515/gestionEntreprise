<script setup>
import { useRoute } from 'vue-router'
import AppLayout from './AppLayout.vue'

// Titre d'espace et lien de retour pilotés par route.meta (voir router/index.js).
const route = useRoute()
const candidatItems = [
  { to: '/candidat/cv', label: 'Mon CV', icon: 'file-person', roles: ['CANDIDAT'], permission: 'DOCUMENT_CONSULTER_PERSO' },
  { to: '/offres', label: 'Offres', icon: 'search', roles: ['CANDIDAT'] },
  { to: '/candidat/candidature', label: 'Mes candidatures', icon: 'briefcase', roles: ['CANDIDAT'], permission: 'CANDIDATURE_POSTULER' },
]
const rhItems = [
  { to: '/rh/accueil', label: 'Accueil RH', icon: 'grid', roles: ['GESTIONNAIRE_RH'] },
  { to: '/rh/demandes-a-traiter', label: 'Demandes à vérifier', icon: 'clipboard-check', roles: ['GESTIONNAIRE_RH'], permission: 'DEMANDE_VERIFIER_RH' },
  { to: '/rh/demandes-direction', label: 'Décisions de la Direction', icon: 'building-check', roles: ['GESTIONNAIRE_RH'], permission: 'DEMANDE_VERIFIER_RH' },
  { to: '/rh/candidatures', label: 'Candidatures', icon: 'people', roles: ['GESTIONNAIRE_RH'], permission: 'CANDIDATURE_CONSULTER' },
  { to: '/rh/confirmation', label: 'Propositions', icon: 'envelope-check', roles: ['GESTIONNAIRE_RH'], permission: 'PROPOSITION_EMETTRE' },
  { to: '/rh/offres', label: 'Offres d’emploi', icon: 'briefcase', roles: ['GESTIONNAIRE_RH'], permission: 'OFFRE_PUBLIER' },
  { to: '/rh/departements', label: 'Départements', icon: 'diagram-3', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
  { to: '/rh/postes', label: 'Postes', icon: 'briefcase', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
  { to: '/rh/employes', label: 'Employés', icon: 'people-fill', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
]
const menuItems = route.path.startsWith('/candidat') ? candidatItems : rhItems
</script>

<template>
  <AppLayout :espace="route.meta.espace || 'Espace personnel'" :items="menuItems">
    <div class="container-fluid">
      <RouterLink v-if="route.meta.retour" :to="route.meta.retour.to" class="btn btn-link px-0 mb-3">
        <i class="bi bi-arrow-left me-1" aria-hidden="true"></i>{{ route.meta.retour.label }}
      </RouterLink>
      <RouterView />
    </div>
  </AppLayout>
</template>
