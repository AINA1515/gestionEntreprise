<script setup>
import { useToast } from '@/composables/useToast'

const { toasts, dismiss } = useToast()
</script>

<template>
  <div class="toast-container position-fixed top-0 end-0 p-3" aria-live="polite" aria-atomic="true">
    <div v-for="toast in toasts" :key="toast.id" class="toast show border-0 shadow-sm mb-2"
         :class="`text-bg-${toast.variant}`" role="status">
      <div class="d-flex align-items-center">
        <div class="toast-body">{{ toast.message }}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" aria-label="Fermer"
                @click="dismiss(toast.id)"></button>
      </div>
    </div>
  </div>
</template>
