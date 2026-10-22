<script setup>
import SidebarLayout from './SidebarLayout.vue'

defineProps({
  espace: { type: String, required: true },
  items: { type: Array, required: true },
})
</script>

<template>
  <SidebarLayout :espace="espace" :items="items"><slot /></SidebarLayout>
</template>
