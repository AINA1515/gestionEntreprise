<script setup>
import { computed } from 'vue'
import { useLabels } from '@/composables/useLabels'

const props = defineProps({
  type: { type: String, required: true }, // 'demande' | 'candidature'
  value: { type: String, default: null },
})

const { statut } = useLabels()
const info = computed(() => statut(props.type, props.value))
</script>

<template>
  <span :class="`badge text-bg-${info.variant}`">{{ info.label }}</span>
</template>
