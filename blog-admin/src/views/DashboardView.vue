<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { dashboardApi } from '@/api/admin'

const loading = ref(true)
const data = ref({ articles: 0, published: 0, drafts: 0, categories: 0, media: 0, totalViews: 0, categoryDistribution: {} })

onMounted(async () => {
  try { data.value = await dashboardApi.get() } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
})
</script>

<template>
  <PageHeader title="内容概览" description="博客内容与阅读数据的实时快照">
    <el-button type="primary" @click="$router.push('/articles/new')">新建文章</el-button>
  </PageHeader>
  <el-skeleton :loading="loading" animated :rows="6">
    <div class="metric-grid">
      <article class="metric-card"><span>全部文章</span><strong>{{ data.articles }}</strong><small>{{ data.published }} 篇已发布</small></article>
      <article class="metric-card"><span>草稿箱</span><strong>{{ data.drafts }}</strong><small>等待继续书写</small></article>
      <article class="metric-card"><span>累计阅读</span><strong>{{ data.totalViews }}</strong><small>真实详情访问量</small></article>
      <article class="metric-card"><span>媒体资源</span><strong>{{ data.media }}</strong><small>{{ data.categories }} 个栏目</small></article>
    </div>
    <div class="dashboard-grid">
      <section class="panel">
        <h3>栏目分布</h3>
        <div v-for="(count, label) in data.categoryDistribution" :key="label" class="distribution-row">
          <span>{{ label }}</span><div><i :style="{ width: `${Math.max(8, count / Math.max(data.articles, 1) * 100)}%` }" /></div><strong>{{ count }}</strong>
        </div>
      </section>
      <section class="panel quiet-panel"><p class="eyebrow">WRITING NOTE</p><blockquote>“文字不是为了追赶时间，而是为了在未来的某一天，仍能认出当时的自己。”</blockquote><span>— Charles Blog</span></section>
    </div>
  </el-skeleton>
</template>

<style scoped>
.dashboard-grid{display:grid;grid-template-columns:1.35fr .65fr;gap:16px;margin-top:18px}.panel h3{margin:0 0 20px;font:600 18px Georgia,serif}.distribution-row{display:grid;grid-template-columns:90px 1fr 32px;align-items:center;gap:12px;margin:15px 0;font-size:13px}.distribution-row>div{height:7px;overflow:hidden;border-radius:10px;background:#eef0f5}.distribution-row i{display:block;height:100%;border-radius:10px;background:linear-gradient(90deg,#6971df,#a675d2)}.distribution-row strong{text-align:right}.quiet-panel{display:flex;flex-direction:column;justify-content:center;color:#ecedfa;background:linear-gradient(145deg,#292e49,#424773)}.quiet-panel blockquote{margin:18px 0;font:400 20px/1.8 Georgia,"Songti SC",serif}.quiet-panel span{color:#aeb3ca;font-size:12px}@media(max-width:800px){.dashboard-grid{grid-template-columns:1fr}}
</style>
