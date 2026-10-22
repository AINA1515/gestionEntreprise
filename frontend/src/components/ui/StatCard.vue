<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: null },
  icon: { type: String, default: '' },
})
</script>

<template>
  <div class="card shadow-sm h-100">
    <div class="card-body d-flex align-items-center gap-3">
      <span
        v-if="icon"
        class="rounded-3 bg-primary-subtle text-primary d-inline-flex align-items-center justify-content-center p-3 fs-3"
        aria-hidden="true"
      >
        <i :class="`bi bi-${icon}`"></i>
      </span>
      <div>
        <div class="small text-body-secondary">{{ label }}</div>
        <div class="fs-2 fw-bold">{{ value ?? '—' }}</div>
      </div>
    </div>
  </div>
</template>
