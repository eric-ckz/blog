<template>
  <div class="home-page">
    <LoadingState v-if="loading" label="正在打开数字书房…" />

    <template v-else-if="data">
      <section class="home-hero section-shell">
        <div class="hero-copy">
          <p class="journal-pill"><span></span>A Personal Journal Since 2020</p>
          <h1 class="hero-title" aria-label="把日子过成 值得重读的文章">
            <span class="hero-line">
              <span v-for="(character, index) in '把日子过成'" :key="`a-${index}`" class="title-character" :style="{ '--delay': `${index * 45}ms` }">{{ character }}</span>
            </span>
            <span class="hero-line">
              <span v-for="(character, index) in '值得重读的文章'" :key="`b-${index}`" class="title-character" :style="{ '--delay': `${(index + 5) * 45}ms` }">{{ character }}</span>
            </span>
          </h1>
          <p class="hero-description">
            这里是一个热爱文学、摄影与旅行的人的自留地——528 篇文字，30 万字，7 年坚持，记录生活的感悟、快门里的光影，和路上的风景。
          </p>
          <div class="hero-actions">
            <router-link class="primary-button" to="/articles">
              开始阅读 <ArrowRight :size="17" />
            </router-link>
            <router-link class="secondary-button" to="/about">认识我</router-link>
          </div>
          <div class="hero-stats" aria-label="博客统计">
            <div><strong>{{ data.stats.articles }}</strong><span>篇文字</span></div>
            <div><strong>{{ data.stats.words }}</strong><span>字</span></div>
            <div><strong>{{ data.stats.years }}</strong><span>年坚持</span></div>
            <div><strong>{{ data.stats.columns }}</strong><span>个栏目</span></div>
          </div>
        </div>

        <div
          v-if="activeSlide"
          class="hero-carousel"
          @mouseenter="carouselPaused = true"
          @mouseleave="carouselPaused = false"
          @focusin="carouselPaused = true"
          @focusout="carouselPaused = false"
        >
          <transition name="carousel" mode="out-in">
            <router-link :key="activeSlide.id" class="hero-card glass-card" :to="`/article/${activeSlide.id}`">
              <SmartImage :src="activeSlide.coverUrl" :alt="activeSlide.title" wrapper-class="hero-card-image" eager />
              <div class="hero-card-body">
                <p class="article-meta"><span>{{ activeSlide.categoryLabel }} · 精选</span>{{ formatDate(activeSlide.publishedAt) }}</p>
                <h2>{{ activeSlide.title }}</h2>
                <p>{{ activeSlide.summary }}</p>
              </div>
            </router-link>
          </transition>
          <div class="carousel-controls">
            <button class="icon-button" type="button" aria-label="上一篇" @click="moveSlide(-1)"><ChevronLeft :size="18" /></button>
            <div class="carousel-dots">
              <button
                v-for="(_, index) in data.heroSlides"
                :key="index"
                type="button"
                :class="{ active: index === slideIndex }"
                :aria-label="`第 ${index + 1} 篇`"
                @click="slideIndex = index"
              ></button>
            </div>
            <button class="icon-button" type="button" aria-label="下一篇" @click="moveSlide(1)"><ChevronRight :size="18" /></button>
          </div>
        </div>
      </section>

      <section class="content-section section-shell" v-reveal>
        <div class="section-heading-row">
          <div><p class="eyebrow">Featured</p><h2>值得一读再读</h2></div>
          <router-link class="text-link" to="/articles">全部文章 <ArrowUpRight :size="15" /></router-link>
        </div>
        <div class="featured-grid">
          <router-link v-for="(article, index) in data.featured" :key="article.id" class="featured-card glass-card" :to="`/article/${article.id}`">
            <div class="featured-number">{{ String(index + 2).padStart(2, '0') }}</div>
            <ArrowUpRight class="featured-arrow" :size="18" />
            <h3>{{ article.title }}</h3>
            <p>{{ article.summary }}</p>
            <div class="featured-meta"><span><i></i>{{ article.categoryLabel }} · {{ formatDate(article.publishedAt) }}</span><span>{{ article.views }} 阅读</span></div>
          </router-link>
        </div>
      </section>

      <section class="content-section section-shell" v-reveal>
        <div class="section-heading-row"><div><p class="eyebrow">Columns</p><h2>五个栏目，五种目光</h2></div></div>
        <div class="columns-grid">
          <router-link v-for="column in data.categories" :key="column.key" class="column-card glass-card" :to="`/articles?cat=${column.key}`">
            <span class="column-icon"><component :is="columnIcons[column.icon]" :size="21" /></span>
            <h3>{{ column.label }}</h3>
            <p>{{ column.description }}</p>
            <span>{{ column.count }} 篇</span>
          </router-link>
        </div>
      </section>

      <section class="content-section section-shell" v-reveal>
        <div class="section-heading-row">
          <div><p class="eyebrow">Recent</p><h2>最近写下</h2></div>
          <router-link class="text-link" to="/articles">查看全部 <ArrowUpRight :size="15" /></router-link>
        </div>
        <div class="recent-grid">
          <router-link v-for="(article, index) in data.recent" :key="article.id" class="recent-card glass-card" :to="`/article/${article.id}`">
            <span v-if="index === 0" class="new-badge">NEW</span>
            <SmartImage :src="article.coverUrl" :alt="article.title" wrapper-class="recent-image" />
            <div class="recent-body">
              <p class="article-meta"><span>{{ article.categoryLabel }}</span>{{ formatDate(article.publishedAt) }}<b v-if="index === 0">· 今天</b></p>
              <h3>{{ article.title }}</h3>
            </div>
          </router-link>
        </div>
      </section>

      <section class="manifesto section-shell" v-reveal>
        <div>
          <h2>{{ data.manifesto?.title || 'Stay Hungry, Stay Foolish.' }}</h2>
          <p>{{ data.manifesto?.text || '求知若渴，虚怀若愚。—— 这是 2020 年创刊号上的第一句话，也是之后的每一年。' }}</p>
          <router-link to="/article/526">读一读创刊号 <ArrowRight :size="17" /></router-link>
        </div>
      </section>
    </template>

    <div v-else class="state-panel error-state">
      <p>数字书房暂时没有打开。</p>
      <button class="secondary-button" type="button" @click="loadHome">重新加载</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  ArrowRight, ArrowUpRight, BookOpen, Building2, Camera, ChevronLeft, ChevronRight, Map, PenLine,
} from '@lucide/vue'
import articleRepository from '@/repositories/articleRepository'
import LoadingState from '@/components/LoadingState.vue'
import SmartImage from '@/components/SmartImage.vue'

const data = ref(null)
const loading = ref(true)
const slideIndex = ref(0)
const carouselPaused = ref(false)
let carouselTimer

const columnIcons = { map: Map, camera: Camera, building: Building2, book: BookOpen, pen: PenLine }
const activeSlide = computed(() => data.value?.heroSlides?.[slideIndex.value])

function formatDate(value) {
  return value?.replaceAll('-', '.') || ''
}

function moveSlide(direction) {
  const count = data.value?.heroSlides?.length || 0
  if (!count) return
  slideIndex.value = (slideIndex.value + direction + count) % count
}

function startCarousel() {
  window.clearInterval(carouselTimer)
  carouselTimer = window.setInterval(() => {
    if (!carouselPaused.value) moveSlide(1)
  }, 5200)
}

async function loadHome() {
  loading.value = true
  try {
    data.value = await articleRepository.getHomeData()
    slideIndex.value = 0
  } catch (error) {
    if (error.name !== 'AbortError') data.value = null
  } finally {
    loading.value = false
  }
}

watch(() => data.value?.heroSlides?.length, (count) => { if (count) startCarousel() })
onMounted(loadHome)
onBeforeUnmount(() => window.clearInterval(carouselTimer))
</script>
