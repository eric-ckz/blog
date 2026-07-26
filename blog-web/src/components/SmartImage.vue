<template>
  <div :class="['smart-image', wrapperClass]">
    <img
      v-if="src && !failed"
      :src="src"
      :alt="alt"
      :loading="eager ? 'eager' : 'lazy'"
      @error="failed = true"
    >
    <div v-else class="image-fallback" aria-hidden="true">
      <span>{{ fallbackText }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  wrapperClass: { type: String, default: '' },
  eager: { type: Boolean, default: false },
})

const failed = ref(false)
const fallbackText = computed(() => props.alt?.trim()?.charAt(0)?.toUpperCase() || '文')
watch(() => props.src, () => { failed.value = false })
</script>
