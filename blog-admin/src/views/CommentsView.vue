<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { commentApi } from '@/api/admin'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 15 })
const filters = reactive({ keyword: '', dateRange: null })

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

function formatTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : ''
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
        <template #default="{ row }"><span class="article-title" :title="row.articleTitle">{{ row.articleTitle || ('#' + row.articleId) }}</span></template>
      </el-table-column>
      <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatTime(row.createdAt) }}</template></el-table-column>
      <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table>
    <div class="pagination"><el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load" /></div>
  </section>
</template>

<style scoped>
.filters{display:flex;align-items:center;gap:12px;margin-bottom:16px;flex-wrap:wrap}
.user-cell{display:flex;align-items:center;gap:10px;min-width:0}.user-cell img,.avatar-fallback{width:34px;height:34px;border-radius:50%;object-fit:cover;flex:0 0 34px}.avatar-fallback{display:inline-flex;align-items:center;justify-content:center;color:#fff;background:#6b7a99;font-weight:700}.user-cell div{min-width:0}.user-cell strong,.user-cell span{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.user-cell strong{font-size:13px}.user-cell span{max-width:180px;margin-top:3px;color:#8a8f9e;font-size:11px}.comment-content{white-space:pre-wrap;word-break:break-word;font-size:13px;line-height:1.6}.article-title{color:#4a6cf7;font-size:13px}.pagination{display:flex;justify-content:flex-end;margin-top:20px}
</style>
