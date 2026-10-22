<script setup>
import SidebarLayout from './SidebarLayout.vue'

const items = [
  { to: '/rh/candidatures', label: 'Candidatures', icon: 'people' },
]
</script>

<template>
  <SidebarLayout espace="Espace Ressources humaines" :items="items">
    <RouterView />
  </SidebarLayout>
</template>
