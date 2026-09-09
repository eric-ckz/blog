<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Eye, Calendar, ExternalLink, Edit, FileText } from '@lucide/vue'
import PageHeader from '@/components/PageHeader.vue'
import { commentApi, articleApi } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 15 })
const filters = reactive({ keyword: '', dateRange: null })
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewArticle = ref(null)

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startTime = filters.dateRange[0] + 'T00:00:00'
      params.endTime = filters.dateRange[1] + 'T23:59:59'
    }
    const data = await commentApi.list(params)
    rows.value = data.items
    total.value = data.total
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}

async function openPreview(row) {
  if (!row.articleId) return
  previewVisible.value = true
  previewLoading.value = true
  previewArticle.value = null
  try {
    previewArticle.value = await articleApi.get(row.articleId)
  } catch (error) { ElMessage.error(error.message) } finally { previewLoading.value = false }
}

function goEdit(id) {
  router.push('/articles/' + id + '/edit')
}

function statusLabel(status) {
  return { PUBLISHED: '已发布', DRAFT: '草稿', ARCHIVED: '已归档' }[status] || status || ''
}

function statusTagType(status) {
  return { PUBLISHED: 'success', DRAFT: 'info', ARCHIVED: 'warning' }[status] || 'info'
}

function formatTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : ''
}

function formatLongDate(value) {
  if (!value) return ''
  const day = value.split('T')[0].split('-')
  if (day.length < 3) return value
  return day[0] + ' 年 ' + Number(day[1]) + ' 月 ' + Number(day[2]) + ' 日'
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除这条评论吗？删除后不可恢复。', '删除评论', { type: 'warning' })
  try { await commentApi.remove(row.id); ElMessage.success('评论已删除'); load() } catch (error) { ElMessage.error(error.message) }
}

function search() {
  query.page = 1
  load()
}

function resetFilters() {
  filters.keyword = ''
  filters.dateRange = null
  query.page = 1
  load()
}

onMounted(load)
</script>

<template>
  <PageHeader title="评论管理" description="查看并管理用户端文章评论" />
  <section class="panel">
    <div class="filters">
      <el-input v-model="filters.keyword" placeholder="搜索评论内容或文章标题" clearable style="width:260px" @keyup.enter="search" />
      <el-date-picker v-model="filters.dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:280px" />
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" row-key="id">
      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <img v-if="row.author?.avatarUrl" :src="row.author.avatarUrl" alt="头像" />
            <span v-else class="avatar-fallback">{{ (row.author?.displayName || row.author?.username || '匿').slice(0, 1) }}</span>
            <div><strong>{{ row.author?.displayName || row.author?.username || '已注销用户' }}</strong><span>{{ row.author?.email }}</span></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="评论内容" min-width="320">
        <template #default="{ row }"><div class="comment-content">{{ row.content }}</div></template>
      </el-table-column>
      <el-table-column label="文章" min-width="220">
        <template #default="{ row }">
          <el-button link type="primary" class="article-link" :title="row.articleTitle || ('#' + row.articleId)" @click="openPreview(row)">{{ row.articleTitle || ('#' + row.articleId) }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatTime(row.createdAt) }}</template></el-table-column>
      <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table>
    <div class="pagination"><el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load" /></div>
  </section>

  <el-dialog v-model="previewVisible" title="文章预览" width="860px" top="4vh" destroy-on-close class="article-preview-dialog">
    <div v-loading="previewLoading" class="preview-body">
      <template v-if="previewArticle">
        <div class="preview-cover-wrap">
          <img v-if="previewArticle.coverUrl" :src="previewArticle.coverUrl" alt="封面" class="preview-cover" />
          <div v-else class="preview-cover preview-cover-fallback">
            <FileText :size="44" />
          </div>
        </div>

        <header class="preview-header">
          <div class="preview-meta-row">
            <p class="preview-meta">
              <span v-if="previewArticle.categoryLabel" class="preview-category">{{ previewArticle.categoryLabel }}</span>
              <template v-if="previewArticle.publishedAt"><b>·</b><time class="preview-date"><Calendar :size="13" /> {{ formatLongDate(previewArticle.publishedAt) }}</time></template>
              <template v-if="previewArticle.readMinutes"><b>·</b><span class="preview-read"><Clock :size="13" /> 约 {{ previewArticle.readMinutes }} 分钟读完</span></template>
              <template v-if="previewArticle.views != null"><b>·</b><span class="preview-views"><Eye :size="13" /> {{ previewArticle.views }} 阅读</span></template>
            </p>
            <el-tag :type="statusTagType(previewArticle.status)" size="small" effect="light" class="preview-status">{{ statusLabel(previewArticle.status) }}</el-tag>
          </div>
          <h2 class="preview-title">{{ previewArticle.title }}</h2>
        </header>

        <p v-if="previewArticle.summary" class="preview-summary">{{ previewArticle.summary }}</p>

        <div v-if="previewArticle.contentHtml" class="preview-content" v-html="previewArticle.contentHtml"></div>

        <footer v-if="previewArticle.originalUrl" class="preview-footer">
          <span class="preview-footer-note">原文地址</span>
          <a :href="previewArticle.originalUrl" target="_blank" rel="noopener noreferrer" class="preview-original">
            <ExternalLink :size="15" /> 查看原文
          </a>
        </footer>
      </template>

      <div v-else class="preview-empty">
        <el-empty :image-size="72" description="文章不存在或已被删除" />
      </div>
    </div>

    <template #footer>
      <el-button @click="previewVisible = false">关闭</el-button>
      <el-button v-if="previewArticle" type="primary" plain @click="goEdit(previewArticle.id)">
        <Edit :size="15" /> 编辑文章
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.filters{display:flex;align-items:center;gap:12px;margin-bottom:16px;flex-wrap:wrap}
.user-cell{display:flex;align-items:center;gap:10px;min-width:0}.user-cell img,.avatar-fallback{width:34px;height:34px;border-radius:50%;object-fit:cover;flex:0 0 34px}.avatar-fallback{display:inline-flex;align-items:center;justify-content:center;color:#fff;background:#6b7a99;font-weight:700}.user-cell div{min-width:0}.user-cell strong,.user-cell span{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.user-cell strong{font-size:13px}.user-cell span{max-width:180px;margin-top:3px;color:#8a8f9e;font-size:11px}.comment-content{white-space:pre-wrap;word-break:break-word;font-size:13px;line-height:1.6}.article-link{padding:0}.pagination{display:flex;justify-content:flex-end;margin-top:20px}

.preview-body{min-height:220px;max-height:74vh;overflow-y:auto;padding:2px 4px 8px;scrollbar-width:thin}
.preview-cover-wrap{border-radius:14px;overflow:hidden;background:#eef1f7;margin-bottom:24px}
.preview-cover{width:100%;max-height:340px;object-fit:cover;display:block}
.preview-cover-fallback{height:180px;display:flex;align-items:center;justify-content:center;color:#b8c0d4}
.preview-header{margin-bottom:18px}
.preview-meta-row{display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;margin-bottom:12px}
.preview-meta{display:flex;align-items:center;gap:8px;color:#8a8f9e;font-size:13px;margin:0;flex-wrap:wrap}
.preview-meta b{color:#c3c8d6;font-weight:400}
.preview-meta .preview-date,.preview-meta .preview-read,.preview-meta .preview-views{display:inline-flex;align-items:center;gap:4px}
.preview-status{flex:0 0 auto}
.preview-title{font-size:26px;line-height:1.35;margin:0;color:#1f2430;font-weight:700;letter-spacing:-0.2px}
.preview-summary{color:#5a5f6e;font-size:14px;line-height:1.8;margin:0 0 22px;padding:14px 18px;background:#f7f8fc;border-left:3px solid #4a6cf7;border-radius:6px}
.preview-content{font-size:16px;color:#3c3f46;line-height:2.0}
.preview-content :deep(p){margin:0 0 1.2em}
.preview-content :deep(h2){font-size:22px;margin:1.6em 0 .8em;line-height:1.4}
.preview-content :deep(h3){font-size:18px;margin:1.5em 0 .7em;line-height:1.4}
.preview-content :deep(h4){font-size:16px;margin:1.4em 0 .6em;line-height:1.4}
.preview-content :deep(img){max-width:100%;border-radius:8px;margin:1.2em 0}
.preview-content :deep(blockquote){margin:1.2em 0;padding:12px 20px;border-left:4px solid #4a6cf7;background:#f7f8fc;color:#5a5f6e;border-radius:0 8px 8px 0}
.preview-content :deep(ul),.preview-content :deep(ol){padding-left:1.5em;margin:0 0 1.2em}
.preview-content :deep(li){margin:.35em 0}
.preview-content :deep(a){color:#4a6cf7}
.preview-content :deep(code){background:#f0f2f8;padding:2px 6px;border-radius:4px;font-size:.9em;color:#c0392b}
.preview-content :deep(pre){background:#1e2430;color:#e6e9f0;padding:16px 20px;border-radius:8px;overflow-x:auto;margin:1.2em 0;font-size:14px;line-height:1.7}
.preview-content :deep(pre code){background:transparent;color:inherit;padding:0;font-size:inherit}
.preview-footer{margin-top:28px;padding-top:18px;border-top:1px solid #eef0f5;display:flex;justify-content:space-between;align-items:center;gap:12px}
.preview-footer-note{color:#8a8f9e;font-size:12px}
.preview-original{display:inline-flex;align-items:center;gap:6px;color:#4a6cf7;font-size:14px;text-decoration:none}
.preview-original:hover{text-decoration:underline}
.preview-empty{min-height:220px;display:flex;align-items:center;justify-content:center}
</style>