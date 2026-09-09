<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { userApi } from '@/api/admin'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 15 })
const filters = reactive({ keyword: '' })

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    if (filters.keyword) params.keyword = filters.keyword
    const data = await userApi.list(params)
    rows.value = data.items
    total.value = data.total
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
}

async function toggleEnabled(row) {
  const action = row.enabled ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户“${row.displayName || row.username}”吗？${row.enabled ? '禁用后该用户将无法登录。' : ''}`, `${action}用户`, { type: 'warning' })
  try {
    await userApi.setEnabled(row.id, !row.enabled)
    ElMessage.success(`用户已${action}`)
    load()
  } catch (error) { ElMessage.error(error.message) }
}

function search() {
  query.page = 1
  load()
}

function resetFilters() {
  filters.keyword = ''
  query.page = 1
  load()
}

function formatTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : ''
}

onMounted(load)
</script>

<template>
  <PageHeader title="用户管理" description="管理已注册的访客账号" />
  <section class="panel">
    <div class="filters">
      <el-input v-model="filters.keyword" placeholder="搜索邮箱、昵称或用户名" clearable style="width:280px" @keyup.enter="search" />
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" row-key="id">
      <el-table-column label="用户" min-width="200">
        <template #default="{ row }">
          <div class="user-cell">
            <img v-if="row.avatarUrl" :src="row.avatarUrl" alt="头像" />
            <span v-else class="avatar-fallback">{{ (row.displayName || row.username || '匿').slice(0, 1) }}</span>
            <div><strong>{{ row.displayName || row.username }}</strong><span>{{ row.email }}</span></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="用户名" prop="username" min-width="120" />
      <el-table-column label="状态" width="160">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">{{ row.enabled ? '正常' : '已禁用' }}</el-tag>
          <el-tag v-if="row.emailVerified" type="info" size="small" effect="plain">邮箱已验证</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="简介" min-width="180">
        <template #default="{ row }"><span class="muted">{{ row.bio || '—' }}</span></template>
      </el-table-column>
      <el-table-column label="最后登录" width="170"><template #default="{ row }">{{ formatTime(row.lastLoginAt) }}</template></el-table-column>
      <el-table-column label="注册时间" width="170"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link :type="row.enabled ? 'danger' : 'success'" @click="toggleEnabled(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination"><el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load" /></div>
  </section>
</template>

<style scoped>
.filters{display:flex;align-items:center;gap:12px;margin-bottom:16px;flex-wrap:wrap}
.user-cell{display:flex;align-items:center;gap:10px;min-width:0}.user-cell img,.avatar-fallback{width:34px;height:34px;border-radius:50%;object-fit:cover;flex:0 0 34px}.avatar-fallback{display:inline-flex;align-items:center;justify-content:center;color:#fff;background:#6b7a99;font-weight:700}.user-cell div{min-width:0}.user-cell strong,.user-cell span{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.user-cell strong{font-size:13px}.user-cell span{max-width:180px;margin-top:3px;color:#8a8f9e;font-size:11px}.muted{color:#8a8f9e;font-size:12px}.pagination{display:flex;justify-content:flex-end;margin-top:20px}
</style>