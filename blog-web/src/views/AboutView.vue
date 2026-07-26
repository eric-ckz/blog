<template>
  <div class="page-shell about-page">
    <LoadingState v-if="loading" />
    <div v-else-if="error" class="state-panel error-state"><p>关于页面暂时没有加载成功。</p><button class="secondary-button" type="button" @click="loadAbout">重新加载</button></div>

    <template v-else-if="data">
      <section class="about-hero" v-reveal>
        <p class="eyebrow">About</p>
        <h1>你好，我是<br>爱喝咖啡的文艺大叔</h1>
        <div class="about-intro">
          <p v-for="paragraph in data.intro" :key="paragraph">{{ paragraph }}</p>
        </div>
      </section>

      <section class="about-section" v-reveal>
        <h2>四件热爱的事</h2>
        <div class="love-grid">
          <article v-for="item in data.loves" :key="item.title" class="love-card glass-card">
            <span class="column-icon"><component :is="loveIcons[item.icon]" :size="21" /></span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section class="about-section timeline-section" v-reveal>
        <h2>这些年</h2>
        <div class="about-timeline">
          <article v-for="item in data.timeline" :key="item.year">
            <strong>{{ item.year }}</strong>
            <span></span>
            <p>{{ item.description }}</p>
          </article>
        </div>
      </section>

      <section class="contact-panel glass-panel" v-reveal>
        <div>
          <p class="eyebrow">Find Me</p>
          <h2>找到我</h2>
          <p>这个网站的全部文章，都同步自我的微信公众号。想第一时间读到更新，欢迎来公众号找我。</p>
        </div>
        <div class="contact-card">
          <span class="brand-mark">文</span>
          <p><small>{{ data.contact.label }}</small><strong>{{ data.contact.value }}</strong></p>
        </div>
        <router-link class="primary-button" to="/articles">先去读几篇 <ArrowRight :size="17" /></router-link>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ArrowRight, BookOpen, Camera, Coffee, MapPin } from '@lucide/vue'
import articleRepository from '@/repositories/articleRepository'
import LoadingState from '@/components/LoadingState.vue'

const data = ref(null)
const loading = ref(true)
const error = ref(false)
const loveIcons = { book: BookOpen, camera: Camera, 'map-pin': MapPin, coffee: Coffee }

async function loadAbout() {
  loading.value = true
  error.value = false
  try {
    data.value = await articleRepository.getAbout()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadAbout)
</script>
