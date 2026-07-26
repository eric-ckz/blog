<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { articleApi, siteApi } from '@/api/admin'

const loading = ref(true)
const saving = ref(false)
const articles = ref([])
const form = reactive({ settings: {}, heroArticleIds: [], featuredArticleIds: [] })

async function save() { saving.value = true; try { await siteApi.updateSettings(form); ElMessage.success('首页配置已保存') } catch (error) { ElMessage.error(error.message) } finally { saving.value = false } }
onMounted(async () => { try { Object.assign(form, await siteApi.settings()); articles.value = (await articleApi.list({ page: 1, pageSize: 100, status: 'PUBLISHED' })).items } catch (error) { ElMessage.error(error.message) } finally { loading.value = false } })
</script>

<template>
  <PageHeader title="首页配置" description="配置展示统计、宣言以及首页人工精选文章"><el-button type="primary" :loading="saving" @click="save">保存配置</el-button></PageHeader>
  <el-skeleton :loading="loading" animated :rows="10">
    <div class="settings-grid">
      <section class="panel"><h3>展示统计</h3><div class="four-columns"><el-form-item label="文章数"><el-input v-model="form.settings.stat_articles" /></el-form-item><el-form-item label="累计字数"><el-input v-model="form.settings.stat_words" /></el-form-item><el-form-item label="写作年数"><el-input v-model="form.settings.stat_years" /></el-form-item><el-form-item label="栏目数"><el-input v-model="form.settings.stat_columns" /></el-form-item></div><h3>深色宣言区</h3><el-form-item label="标题"><el-input v-model="form.settings.manifesto_title" /></el-form-item><el-form-item label="正文"><el-input v-model="form.settings.manifesto_text" type="textarea" :rows="4" /></el-form-item></section>
      <section class="panel"><h3>文章编排</h3><el-form-item label="Hero 轮播（最多 3 篇）"><el-select v-model="form.heroArticleIds" multiple :multiple-limit="3" filterable style="width:100%"><el-option v-for="item in articles" :key="item.id" :label="item.title" :value="item.id" /></el-select></el-form-item><el-form-item label="精选文章（最多 4 篇）"><el-select v-model="form.featuredArticleIds" multiple :multiple-limit="4" filterable style="width:100%"><el-option v-for="item in articles" :key="item.id" :label="item.title" :value="item.id" /></el-select></el-form-item><p class="hint">最近文章由后端按发布时间自动取最新 6 篇，无需手工配置。</p></section>
    </div>
  </el-skeleton>
</template>

<style scoped>.settings-grid{display:grid;grid-template-columns:1.2fr .8fr;gap:18px}.panel h3{margin:0 0 20px;font:600 18px Georgia,"Songti SC",serif}.panel h3:not(:first-child){margin-top:24px}.four-columns{display:grid;grid-template-columns:repeat(2,1fr);gap:0 16px}.hint{padding:12px;border-radius:9px;color:#757b8e;background:#f4f5fa;font-size:12px;line-height:1.7}@media(max-width:850px){.settings-grid{grid-template-columns:1fr}}@media(max-width:520px){.four-columns{grid-template-columns:1fr}}</style>
