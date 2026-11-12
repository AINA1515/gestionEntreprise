<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'danger' },
  message: { type: String, default: '' },
})

const visibleMessage = computed(() => {
  const value = props.message
  const message = typeof value === 'string' || typeof value === 'number'
    ? String(value)
    : value?.message || value?.response?.data?.message || ''
  if (typeof message !== 'string' && typeof message !== 'number') return ''
  const text = String(message)
  return text.trim() && text !== '[object Object]' ? text : ''
})
</script>

<template>
  <div v-if="visibleMessage" :class="`alert alert-${variant}`" role="alert">
    <slot>{{ visibleMessage }}</slot>
  </div>
</template>
