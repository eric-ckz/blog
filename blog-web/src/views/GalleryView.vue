<template>
  <div class="page-shell gallery-page">
    <PageIntro
      eyebrow="Gallery"
      title="光影集"
      description="快门里的世界——旅途的黄昏、街角的光、偶然撞见的瞬间。"
    />
    <section class="gallery-placeholder glass-panel" v-reveal>
      <span class="gallery-icon"><Camera :size="27" /></span>
      <h2>光影尚未抵达</h2>
      <p>照片与影像正在整理中，稍后与你见面。</p>
    </section>
  </div>
</template>

<script setup>
import { Camera } from '@lucide/vue'
import PageIntro from '@/components/PageIntro.vue'
</script>
