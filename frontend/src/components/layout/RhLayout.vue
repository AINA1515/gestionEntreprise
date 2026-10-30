<script setup>
import AppLayout from './AppLayout.vue'

const items = [
  { to: '/rh/accueil', label: 'Accueil RH', icon: 'grid', roles: ['GESTIONNAIRE_RH'] },
  { to: '/rh/demandes-a-traiter', label: 'Demandes à vérifier', icon: 'clipboard-check', roles: ['GESTIONNAIRE_RH'], permission: 'DEMANDE_VERIFIER_RH' },
  { to: '/rh/demandes-direction', label: 'Décisions de la Direction', icon: 'building-check', roles: ['GESTIONNAIRE_RH'], permission: 'DEMANDE_VERIFIER_RH' },
  { to: '/rh/candidatures', label: 'Candidatures', icon: 'people', roles: ['GESTIONNAIRE_RH'], permission: 'CANDIDATURE_CONSULTER' },
  { to: '/rh/confirmation', label: 'Propositions', icon: 'envelope-check', roles: ['GESTIONNAIRE_RH'], permission: 'PROPOSITION_EMETTRE' },
  { to: '/rh/offres', label: 'Offres d’emploi', icon: 'briefcase', roles: ['GESTIONNAIRE_RH'], permission: 'OFFRE_PUBLIER' },
  { to: '/offres', label: 'Catalogue public', icon: 'search' },
  { to: '/rh/departements', label: 'Départements', icon: 'diagram-3', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
  { to: '/rh/postes', label: 'Postes', icon: 'briefcase', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
  { to: '/rh/affectations', label: 'Affectations', icon: 'arrow-left-right', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
  { to: '/rh/employes', label: 'Employés', icon: 'people-fill', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
]
</script>

<template>
  <AppLayout espace="Espace Ressources humaines" :items="items">
    <RouterView />
  </AppLayout>
</template>
