<template>
  <div class="article-detail-page">
    <LoadingState v-if="loading" />

    <div v-else-if="error" class="state-panel error-state detail-error">
      <FileQuestion :size="34" />
      <h1>这篇文章没有找到</h1>
      <p>它可能已经移动，或者暂时离开了书架。</p>
      <router-link class="secondary-button" to="/articles"><ArrowLeft :size="16" /> 返回全部文章</router-link>
    </div>

    <article v-else-if="article" class="article-shell">
      <router-link class="back-link" to="/articles"><ArrowLeft :size="15" /> 全部文章</router-link>

      <header class="article-header">
        <p class="article-detail-meta">
          <span>{{ article.categoryLabel }}</span>
          <time :datetime="article.publishedAt">{{ formatLongDate(article.publishedAt) }}</time>
          <b>·</b>
          <span>约 {{ article.readMinutes }} 分钟读完</span>
          <b>·</b>
          <span class="views"><Eye :size="14" /> {{ article.views }} 阅读</span>
        </p>
        <h1>{{ article.title }}</h1>
      </header>

      <SmartImage v-if="article.coverUrl" :src="article.coverUrl" :alt="article.title" wrapper-class="article-cover" eager />

      <div class="article-content" v-html="safeContent"></div>

      <footer class="article-signature">
        <span class="brand-mark">文</span>
        <div>
          <p>爱喝咖啡的文艺大叔</p>
          <p>写于 {{ formatLongDate(article.publishedAt) }}</p>
        </div>
        <a v-if="article.originalUrl" class="original-link" :href="article.originalUrl" target="_blank" rel="noopener noreferrer">
          微信原文 <ExternalLink :size="15" />
        </a>
      </footer>

      <CommentSection :article-id="article.id" />

      <nav class="article-navigation" aria-label="相邻文章">
        <router-link v-if="article.previous" :to="`/article/${article.previous.id}`">
          <span><ArrowLeft :size="15" /> 更早一篇</span>
          <strong>{{ article.previous.title }}</strong>
        </router-link>
        <span v-else></span>
        <router-link v-if="article.next" class="next-article" :to="`/article/${article.next.id}`">
          <span>更新一篇 <ArrowRight :size="15" /></span>
          <strong>{{ article.next.title }}</strong>
        </router-link>
      </nav>
    </article>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import DOMPurify from 'dompurify'
import { ArrowLeft, ArrowRight, ExternalLink, Eye, FileQuestion } from '@lucide/vue'
import articleRepository from '@/repositories/articleRepository'
import LoadingState from '@/components/LoadingState.vue'
import SmartImage from '@/components/SmartImage.vue'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()
const article = ref(null)
const loading = ref(true)
const error = ref(false)
let activeController

const safeContent = computed(() => DOMPurify.sanitize(article.value?.contentHtml || '', {
  USE_PROFILES: { html: true },
  ADD_ATTR: ['loading'],
}))

function formatLongDate(value) {
  if (!value) return ''
  const [year, month, day] = value.split('-')
  return `${year} 年 ${Number(month)} 月 ${Number(day)} 日`
}

async function fetchArticle(id) {
  activeController?.abort()
  activeController = new AbortController()
  loading.value = true
  error.value = false
  try {
    article.value = await articleRepository.getArticleById(id, { signal: activeController.signal })
    document.title = `${article.value.title} · 大叔的文字与光影`
  } catch (requestError) {
    if (requestError.name !== 'AbortError' && requestError.code !== 'ERR_CANCELED') {
      article.value = null
      error.value = true
      document.title = '文章不存在 · 大叔的文字与光影'
    }
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, (id) => fetchArticle(id), { immediate: true })
onBeforeUnmount(() => activeController?.abort())
</script>
