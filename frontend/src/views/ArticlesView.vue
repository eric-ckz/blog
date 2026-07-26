<template>
  <div class="page-shell articles-page">
    <PageIntro eyebrow="Archive" title="全部文章">
      <template #aside><p class="page-count">共 {{ displayTotal }} 篇 · 2020 – 2026</p></template>
    </PageIntro>

    <section class="filter-bar glass-panel" aria-label="文章筛选" v-reveal>
      <div class="category-filter" role="group" aria-label="栏目">
        <button
          v-for="category in categoryOptions"
          :key="category.key"
          type="button"
          :class="['filter-button', { active: state.category === category.key }]"
          @click="updateQuery({ cat: category.key, page: 1 })"
        >
          {{ category.label }}
        </button>
      </div>
      <label class="search-field">
        <Search :size="18" />
        <input v-model="keywordInput" type="search" placeholder="搜标题或正文…" aria-label="搜索文章">
      </label>
      <label class="year-field">
        <select :value="state.year" aria-label="年份" @change="updateQuery({ year: $event.target.value, page: 1 })">
          <option value="all">全部年份</option>
          <option v-for="year in years" :key="year" :value="year">{{ year }} 年</option>
        </select>
        <ChevronDown :size="16" />
      </label>
    </section>

    <LoadingState v-if="loading && !items.length" />

    <div v-else-if="error" class="state-panel error-state">
      <p>文章暂时没有加载成功。</p>
      <button class="secondary-button" type="button" @click="fetchArticles">重新加载</button>
    </div>

    <div v-else-if="!items.length" class="state-panel empty-state">
      <FileSearch :size="30" />
      <h2>没有找到对应的文章</h2>
      <p>换一个栏目、年份或关键词再试试。</p>
      <button class="secondary-button" type="button" @click="clearFilters">清除筛选</button>
    </div>

    <template v-else>
      <section class="article-list-grid" aria-live="polite">
        <router-link v-for="article in items" :key="article.id" class="article-list-card glass-card" :to="`/article/${article.id}`">
          <SmartImage :src="article.coverUrl" :alt="article.title" wrapper-class="article-list-image" />
          <div class="article-list-copy">
            <h2>{{ article.title }}</h2>
            <p>{{ article.summary }}</p>
            <div class="article-list-meta">
              <span>{{ article.categoryLabel }}</span>
              <time :datetime="article.publishedAt">{{ formatDate(article.publishedAt) }}</time>
              <Eye :size="13" /> {{ article.views }}
            </div>
          </div>
          <ArrowUpRight class="article-list-arrow" :size="17" />
        </router-link>
      </section>

      <div v-if="hasMore" class="load-more-wrap">
        <button class="secondary-button" type="button" :disabled="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中…' : `加载更多（${remaining} 篇）` }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowUpRight, ChevronDown, Eye, FileSearch, Search } from '@lucide/vue'
import articleRepository from '@/repositories/articleRepository'
import LoadingState from '@/components/LoadingState.vue'
import PageIntro from '@/components/PageIntro.vue'
import SmartImage from '@/components/SmartImage.vue'

const PAGE_SIZE = 15
const route = useRoute()
const router = useRouter()
const items = ref([])
const total = ref(0)
const hasMore = ref(false)
const loading = ref(true)
const loadingMore = ref(false)
const error = ref(false)
const keywordInput = ref('')
const categories = ref([])
const state = reactive({ category: 'all', year: 'all', keyword: '', page: 1 })
const years = ['2026', '2025', '2024', '2023', '2022', '2021', '2020']
let activeController
let searchTimer

const categoryOptions = computed(() => [{ key: 'all', label: '全部' }, ...categories.value])
const hasFilters = computed(() => state.category !== 'all' || state.year !== 'all' || Boolean(state.keyword))
const displayTotal = computed(() => hasFilters.value ? total.value : 528)
const remaining = computed(() => Math.max(total.value - items.value.length, 0))

function formatDate(value) {
  return value?.replaceAll('-', '.') || ''
}

function readRoute() {
  state.category = String(route.query.cat || 'all')
  state.year = String(route.query.year || 'all')
  state.keyword = String(route.query.keyword || '')
  state.page = Math.max(1, Number(route.query.page || 1))
  keywordInput.value = state.keyword
}

function updateQuery(changes) {
  const next = {
    cat: changes.cat ?? state.category,
    year: changes.year ?? state.year,
    keyword: changes.keyword ?? state.keyword,
    page: changes.page ?? state.page,
  }
  if (next.cat === 'all') delete next.cat
  if (next.year === 'all') delete next.year
  if (!next.keyword) delete next.keyword
  if (Number(next.page) === 1) delete next.page
  router.replace({ query: next })
}

async function fetchArticles() {
  activeController?.abort()
  activeController = new AbortController()
  const isLoadingMore = state.page > 1 && items.value.length > 0
  loading.value = !isLoadingMore
  loadingMore.value = isLoadingMore
  error.value = false
  try {
    const result = await articleRepository.getArticles({
      category: state.category,
      year: state.year,
      keyword: state.keyword,
      page: 1,
      pageSize: PAGE_SIZE * state.page,
      signal: activeController.signal,
    })
    items.value = result.items
    total.value = result.total
    hasMore.value = result.hasMore
  } catch (requestError) {
    if (requestError.name !== 'AbortError' && requestError.code !== 'ERR_CANCELED') error.value = true
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  updateQuery({ page: state.page + 1 })
}

function clearFilters() {
  router.replace({ query: {} })
}

watch(keywordInput, (value) => {
  window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    if (value !== state.keyword) updateQuery({ keyword: value.trim(), page: 1 })
  }, 260)
})

watch(() => route.query, () => {
  readRoute()
  fetchArticles()
}, { deep: true })

onMounted(async () => {
  readRoute()
  categories.value = await articleRepository.getCategories()
  await fetchArticles()
})

onBeforeUnmount(() => {
  activeController?.abort()
  window.clearTimeout(searchTimer)
})
</script>
