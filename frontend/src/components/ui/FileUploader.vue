<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: { type: File, default: null },
  accept: { type: Array, default: () => ['.pdf', '.docx', '.png', '.jpg'] },
  maxSize: { type: Number, default: 5 * 1024 * 1024 },
  progress: { type: Number, default: 0 },
  disabled: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue'])
const drag = ref(false)
const error = ref('')
const input = ref(null)

function choose(file) {
  error.value = ''
  if (!file) { emit('update:modelValue', null); return }
  const ext = `.${file.name.split('.').pop().toLowerCase()}`
  if (!props.accept.includes(ext)) { error.value = 'Ce format de fichier n’est pas autorisé.'; return }
  if (file.size > props.maxSize) { error.value = 'Le fichier dépasse la taille maximale de 5 Mo.'; return }
  emit('update:modelValue', file)
}
function onDrop(event) { drag.value = false; choose(event.dataTransfer.files?.[0]) }
</script>

<template>
  <div>
    <button type="button" class="upload-zone w-100 text-start p-4 rounded border border-2" :class="{ 'border-primary bg-primary-subtle': drag, 'border-secondary-subtle': !drag }" :disabled="disabled" @click="input?.click()" @dragover.prevent="drag = true" @dragleave.prevent="drag = false" @drop.prevent="onDrop">
      <input ref="input" class="visually-hidden" type="file" :accept="accept.join(',')" :disabled="disabled" @change="choose($event.target.files?.[0]); $event.target.value = ''" />
      <span class="d-block fw-semibold"><i class="bi bi-cloud-arrow-up me-2" aria-hidden="true"></i>Déposez un fichier ou cliquez pour parcourir</span>
      <span class="d-block small text-body-secondary mt-1">Formats autorisés : {{ accept.join(', ') }} · 5 Mo maximum</span>
      <span v-if="modelValue" class="d-block small mt-2">{{ modelValue.name }} ({{ (modelValue.size / 1024 / 1024).toFixed(2) }} Mo)</span>
    </button>
    <div v-if="error" class="text-danger small mt-2" role="alert">{{ error }}</div>
    <div v-if="progress > 0" class="progress mt-3" role="progressbar" :aria-valuenow="progress" aria-valuemin="0" aria-valuemax="100">
      <div class="progress-bar" :style="{ width: `${progress}%` }">{{ progress }}%</div>
    </div>
  </div>
</template>

<style scoped>
.upload-zone { background: var(--bs-body-bg); }
.upload-zone:disabled { opacity: .65; cursor: not-allowed; }
</style>
