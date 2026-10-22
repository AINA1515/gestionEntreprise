<script setup>
import AppLayout from '@/components/layout/AppLayout.vue'
const items = [{ to: '/direction/dashboard', label: 'Tableau de bord', icon: 'grid', roles: ['DIRECTION'], permission: 'REPORTING_CONSULTER' }]
</script>
<template><AppLayout espace="Direction" :items="items"><section class="container-fluid py-3"><h1 class="h2">Tableau de bord Direction</h1><p class="text-body-secondary">Les indicateurs de pilotage seront disponibles ici.</p></section></AppLayout></template>
