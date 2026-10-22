<script setup>
import AppLayout from '@/components/layout/AppLayout.vue'
const items = [{ to: '/taches', label: 'Mes tâches', icon: 'check2-square', permission: 'TACHE_TRAITER' }]
</script>
<template><AppLayout espace="Mes tâches" :items="items"><section class="container-fluid py-3"><h1 class="h2">Mes tâches</h1><p class="text-body-secondary">Les tâches qui vous sont attribuées seront disponibles ici.</p></section></AppLayout></template>
