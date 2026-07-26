<template>
  <div class="state-panel" role="status">
    <span class="loading-dot"></span>
    <p>{{ label }}</p>
  </div>
</template>

<script setup>
defineProps({ label: { type: String, default: '正在整理文字…' } })
</script>
