<script setup>
defineProps({
  steps: { type: Array, required: true },
  currentIndex: { type: Number, required: true },
  stopped: { type: Boolean, default: false },
})
</script>

<template>
  <ol class="step-timeline list-unstyled d-flex flex-column flex-md-row gap-3 mb-0">
    <li v-for="(step, index) in steps" :key="step" class="step-timeline__item flex-fill"
      :class="{ 'is-complete': index < currentIndex, 'is-current': index === currentIndex, 'is-stopped': stopped && index === currentIndex }">
      <span class="step-timeline__marker">{{ index < currentIndex ? '✓' : index + 1 }}</span>
      <span class="small fw-semibold">{{ step }}</span>
    </li>
  </ol>
</template>

<style scoped>
.step-timeline__item { display: flex; align-items: center; gap: .6rem; color: var(--bs-secondary-color); }
.step-timeline__marker { width: 2rem; height: 2rem; display: inline-flex; align-items: center; justify-content: center; border: 2px solid var(--bs-border-color); border-radius: 50%; background: var(--bs-body-bg); flex: 0 0 auto; }
.is-complete { color: var(--bs-success); }
.is-complete .step-timeline__marker { background: var(--bs-success); color: white; border-color: var(--bs-success); }
.is-current { color: var(--bs-primary); }
.is-current .step-timeline__marker { color: var(--bs-primary); border-color: var(--bs-primary); }
.is-stopped, .is-stopped .step-timeline__marker { color: var(--bs-danger); border-color: var(--bs-danger); }
</style>
