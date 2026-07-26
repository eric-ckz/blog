<template>
  <div class="page-intro" v-reveal>
    <p class="eyebrow">{{ eyebrow }}</p>
    <div class="page-intro-row">
      <div>
        <h1>{{ title }}</h1>
        <p v-if="description" class="page-description">{{ description }}</p>
      </div>
      <slot name="aside"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  eyebrow: { type: String, required: true },
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>
