<script setup>
import AppLayout from './AppLayout.vue'

const items = [
  { to: '/direction/dashboard', label: 'Tableau de bord', icon: 'grid', roles: ['DIRECTION'], permission: 'REPORTING_CONSULTER' },
  { to: '/direction/arbitrage', label: 'Arbitrage des demandes', icon: 'bank', roles: ['DIRECTION'], permission: 'DEMANDE_APPROUVER_DIR' },
  { to: '/offres', label: 'Offres d’emploi', icon: 'search' },
]
</script>

<template><AppLayout espace="Direction" :items="items"><RouterView /></AppLayout></template>
