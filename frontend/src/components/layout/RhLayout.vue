<script setup>
import AppLayout from './AppLayout.vue'

const items = [
  { to: '/rh/accueil', label: 'Accueil RH', icon: 'grid', roles: ['CHARGE_RECRUTEMENT', 'GESTIONNAIRE_RH'] },
  { to: '/rh/demandes-a-traiter', label: 'Demandes à vérifier', icon: 'clipboard-check', roles: ['CHARGE_RECRUTEMENT'], permission: 'DEMANDE_VERIFIER_RH' },
  { to: '/rh/candidatures', label: 'Candidatures', icon: 'people', roles: ['CHARGE_RECRUTEMENT'], permission: 'CANDIDATURE_CONSULTER' },
  { to: '/rh/confirmation', label: 'Propositions', icon: 'envelope-check', roles: ['CHARGE_RECRUTEMENT'], permission: 'PROPOSITION_EMETTRE' },
  { to: '/rh/offres/nouvelle', label: 'Créer une offre', icon: 'file-earmark-plus', roles: ['CHARGE_RECRUTEMENT'], permission: 'OFFRE_PUBLIER' },
  { to: '/rh/departements', label: 'Départements', icon: 'diagram-3', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
  { to: '/rh/postes', label: 'Postes', icon: 'briefcase', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
  { to: '/rh/employes', label: 'Employés', icon: 'people-fill', roles: ['GESTIONNAIRE_RH'], permission: 'CONTRAT_GERER' },
]
</script>

<template>
  <AppLayout espace="Espace Ressources humaines" :items="items">
    <RouterView />
  </AppLayout>
</template>
