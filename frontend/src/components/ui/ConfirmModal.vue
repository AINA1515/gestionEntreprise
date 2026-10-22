<script setup>
import FormField from './FormField.vue'
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, required: true },
  message: { type: String, default: '' },
  confirmText: { type: String, default: 'Confirmer' },
  cancelText: { type: String, default: 'Annuler' },
  variant: { type: String, default: 'primary' },
  requiresJustification: { type: Boolean, default: false },
  justification: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue', 'update:justification', 'confirm'])
function fermer() { emit('update:modelValue', false) }
</script>

<template>
  <div v-if="modelValue" class="confirm-backdrop" @click.self="fermer">
    <section class="confirm-dialog card shadow-lg" role="dialog" aria-modal="true" :aria-label="title">
      <div class="card-header d-flex justify-content-between align-items-center"><h2 class="h5 mb-0">{{ title }}</h2><button class="btn-close" type="button" aria-label="Fermer" @click="fermer"></button></div>
      <div class="card-body">
        <p v-if="message">{{ message }}</p>
        <FormField v-if="requiresJustification" id="confirm-justification" label="Justification" hint="Obligatoire pour refuser.">
          <textarea id="confirm-justification" class="form-control" rows="4" maxlength="1000" :value="justification"
            @input="emit('update:justification', $event.target.value)"></textarea>
        </FormField>
        <slot />
      </div>
      <div class="card-footer d-flex justify-content-end gap-2">
        <button class="btn btn-outline-secondary" type="button" @click="fermer">{{ cancelText }}</button>
        <button class="btn" :class="`btn-${variant}`" type="button"
          :disabled="requiresJustification && !justification.trim()" @click="emit('confirm')">{{ confirmText }}</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.confirm-backdrop { position: fixed; inset: 0; z-index: 1080; display: grid; place-items: center; padding: 1rem; background: rgb(0 0 0 / 50%); }
.confirm-dialog { width: min(100%, 34rem); }
</style>
