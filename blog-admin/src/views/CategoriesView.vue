<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { categoryApi } from '@/api/admin'

const rows = ref([])
const loading = ref(false)
const dialog = ref(false)
const editingId = ref('')
const form = reactive({ categoryKey: '', label: '', description: '', icon: 'book', displayCount: 0, sortOrder: 0 })

async function load() { loading.value = true; try { rows.value = await categoryApi.list() } catch (error) { ElMessage.error(error.message) } finally { loading.value = false } }
function open(row) { editingId.value = row?.id || ''; Object.assign(form, row ? { categoryKey: row.key, label: row.label, description: row.description, icon: row.icon, displayCount: row.count, sortOrder: row.sortOrder } : { categoryKey: '', label: '', description: '', icon: 'book', displayCount: 0, sortOrder: rows.value.length + 1 }); dialog.value = true }
async function save() { try { editingId.value ? await categoryApi.update(editingId.value, form) : await categoryApi.create(form); ElMessage.success('栏目已保存'); dialog.value = false; load() } catch (error) { ElMessage.error(error.message) } }
async function remove(row) { await ElMessageBox.confirm(`删除栏目“${row.label}”？栏目中有文章时后端会拒绝操作。`, '删除栏目', { type: 'warning' }); try { await categoryApi.remove(row.id); ElMessage.success('栏目已删除'); load() } catch (error) { ElMessage.error(error.message) } }
onMounted(load)
</script>

<template>
  <PageHeader title="栏目管理" description="维护用户端五个内容栏目及其展示顺序"><el-button type="primary" @click="open()">新增栏目</el-button></PageHeader>
  <section class="panel"><el-table v-loading="loading" :data="rows"><el-table-column prop="sortOrder" label="顺序" width="80" /><el-table-column label="栏目" min-width="180"><template #default="{row}"><strong>{{ row.label }}</strong><div class="muted">{{ row.key }}</div></template></el-table-column><el-table-column prop="description" label="描述" min-width="220" /><el-table-column prop="icon" label="图标" width="110" /><el-table-column prop="count" label="展示数量" width="110" /><el-table-column label="操作" width="140"><template #default="{row}"><el-button link type="primary" @click="open(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table></section>
  <el-dialog v-model="dialog" :title="editingId ? '编辑栏目' : '新增栏目'" width="520px"><el-form label-position="top"><el-form-item label="英文字段"><el-input v-model="form.categoryKey" placeholder="travel" /></el-form-item><el-form-item label="栏目名称"><el-input v-model="form.label" /></el-form-item><el-form-item label="描述"><el-input v-model="form.description" /></el-form-item><el-form-item label="Lucide 图标标识"><el-input v-model="form.icon" /></el-form-item><div class="two-columns"><el-form-item label="展示文章数"><el-input-number v-model="form.displayCount" :min="0" /></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item></div></el-form><template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
</template>

<style scoped>.muted{margin-top:4px;color:#9297a7;font-size:11px}.two-columns{display:grid;grid-template-columns:1fr 1fr;gap:16px}</style>
