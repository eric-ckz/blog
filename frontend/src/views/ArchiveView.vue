<template>
  <div class="page-shell archive-page">
    <PageIntro
      eyebrow="Timeline"
      title="时光档案"
      description="从 2020 年 5 月的第一篇《创刊号》开始，每一年都留下了认真的笔迹。"
    />

    <LoadingState v-if="loading" />
    <div v-else-if="error" class="state-panel error-state"><p>时光档案暂时没有打开。</p><button class="secondary-button" type="button" @click="loadArchive">重新加载</button></div>

    <section v-else class="archive-years">
      <article v-for="year in archive" :key="year.year" class="archive-year" v-reveal>
        <header class="archive-year-heading">
          <h2>{{ year.year }}</h2>
          <span>{{ year.count }} 篇</span>
        </header>
        <div class="archive-month-grid">
          <div v-for="month in year.months" :key="month.month" class="archive-month glass-card">
            <div class="archive-month-heading">
              <strong>{{ month.month }} <span>月</span></strong>
              <i></i>
              <small>{{ month.count }} 篇</small>
            </div>
            <ul>
              <li v-for="item in month.items" :key="item.id">
                <router-link :to="`/article/${item.id}`">
                  <span>{{ item.title }}</span><small>{{ item.categoryLabel }}</small>
                </router-link>
              </li>
            </ul>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import articleRepository from '@/repositories/articleRepository'
import LoadingState from '@/components/LoadingState.vue'
import PageIntro from '@/components/PageIntro.vue'

const archive = ref([])
const loading = ref(true)
const error = ref(false)

async function loadArchive() {
  loading.value = true
  error.value = false
  try {
    archive.value = await articleRepository.getArchive()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadArchive)
</script>
