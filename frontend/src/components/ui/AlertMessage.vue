<script setup>
defineProps({
  variant: { type: String, default: 'danger' },
  message: { type: String, default: '' },
})
</script>

<template>
  <div v-if="message" :class="`alert alert-${variant}`" role="alert">
    <slot>{{ message }}</slot>
  </div>
</template>
