<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { articleApi, categoryApi } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const categories = ref([])
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 15, keyword: '', category: '', status: '', sortField: 'updateTime', sortOrder: 'desc' })

async function load() {
  loading.value = true
  try {
    const data = await articleApi.list(query)
    rows.value = data.items
    total.value = data.total
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}

function search() { query.page = 1; load() }
async function remove(row) {
  await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '删除文章', { type: 'warning' })
  try { await articleApi.remove(row.id); ElMessage.success('文章已删除'); load() } catch (error) { ElMessage.error(error.message) }
}

onMounted(async () => {
  try { categories.value = await categoryApi.list() } catch (error) { ElMessage.error(error.message) }
  load()
})
</script>

<template>
  <PageHeader title="文章管理" description="管理草稿、已发布内容与历史文章">
    <el-button type="primary" @click="router.push('/articles/new')">新建文章</el-button>
  </PageHeader>
  <section class="panel">
    <div class="toolbar">
      <el-input v-model="query.keyword" clearable placeholder="搜索标题或摘要" style="width:260px" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.category" clearable placeholder="全部栏目" style="width:150px" @change="search">
        <el-option v-for="item in categories" :key="item.key" :label="item.label" :value="item.key" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="全部状态" style="width:140px" @change="search">
        <el-option label="草稿" value="DRAFT" /><el-option label="已发布" value="PUBLISHED" /><el-option label="已归档" value="ARCHIVED" />
      </el-select>
      <el-button @click="search">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" row-key="id">
      <el-table-column label="文章" min-width="310">
        <template #default="{ row }"><div class="article-cell"><img v-if="row.coverUrl" :src="row.coverUrl" /><div><strong>{{ row.title }}</strong><span>{{ row.summary }}</span></div></div></template>
      </el-table-column>
      <el-table-column prop="categoryLabel" label="栏目" width="110" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'DRAFT' ? 'warning' : 'info'" effect="light">{{ {PUBLISHED:'已发布',DRAFT:'草稿',ARCHIVED:'已归档'}[row.status] }}</el-tag></template></el-table-column>
      <el-table-column prop="views" label="阅读" width="80" />
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="router.push(`/articles/${row.id}/edit`)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
    </el-table>
    <div class="pagination"><el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load" /></div>
  </section>
</template>

<style scoped>
.article-cell{display:flex;align-items:center;gap:12px;min-width:0}.article-cell img{width:62px;height:45px;object-fit:cover;border-radius:8px;background:#eee}.article-cell div{min-width:0}.article-cell strong,.article-cell span{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.article-cell strong{font-size:13px}.article-cell span{max-width:420px;margin-top:5px;color:#8a8f9e;font-size:11px}.pagination{display:flex;justify-content:flex-end;margin-top:20px}
</style>
