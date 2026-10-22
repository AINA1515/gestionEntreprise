<script setup>
import AppLayout from '@/components/layout/AppLayout.vue'
const items = [{ to: '/admin/utilisateurs', label: 'Utilisateurs', icon: 'people', roles: ['ADMIN'], permission: 'UTILISATEUR_ADMIN' }]
</script>
<template><AppLayout espace="Administration" :items="items"><section class="container-fluid py-3"><h1 class="h2">Administration des utilisateurs</h1><p class="text-body-secondary">La gestion des comptes et des rôles sera disponible ici.</p></section></AppLayout></template>
