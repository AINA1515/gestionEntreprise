<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  breadcrumb: { type: Array, default: () => [] },
})
</script>

<template>
  <header class="mb-4">
    <nav v-if="breadcrumb.length" aria-label="breadcrumb">
      <ol class="breadcrumb small mb-1">
        <li
          v-for="(item, i) in breadcrumb"
          :key="item"
          :class="['breadcrumb-item', { active: i === breadcrumb.length - 1 }]"
        >
          {{ item }}
        </li>
      </ol>
    </nav>
    <h1 class="h3 mb-1">{{ title }}</h1>
    <p v-if="subtitle" class="text-body-secondary mb-0">{{ subtitle }}</p>
  </header>
</template>
