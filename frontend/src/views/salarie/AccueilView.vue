<script setup>
import AppLayout from '@/components/layout/AppLayout.vue'
const items = [{ to: '/salarie/accueil', label: 'Accueil', icon: 'house', roles: ['SALARIE'] }, { to: '/taches', label: 'Mes tâches', icon: 'check2-square', roles: ['SALARIE'], permission: 'TACHE_TRAITER' }, { to: '/offres', label: 'Offres d’emploi', icon: 'search' }]
</script>
<template><AppLayout espace="Espace salarié" :items="items"><section class="container-fluid py-3"><h1 class="h2">Mon espace salarié</h1><p class="text-body-secondary">Vos informations et services RH seront disponibles ici.</p></section></AppLayout></template>
