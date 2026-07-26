<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { mediaApi } from '@/api/admin'

const rows = ref([])
const loading = ref(false)
const uploading = ref(false)
const total = ref(0)
const page = ref(1)

async function load() { loading.value = true; try { const data = await mediaApi.list({ page: page.value, pageSize: 24 }); rows.value = data.items; total.value = data.total } catch (error) { ElMessage.error(error.message) } finally { loading.value = false } }
async function upload(options) { uploading.value = true; try { await mediaApi.upload(options.file); ElMessage.success('图片已上传'); options.onSuccess(); load() } catch (error) { options.onError(error); ElMessage.error(error.message) } finally { uploading.value = false } }
async function remove(item) { await ElMessageBox.confirm(`删除 ${item.originalName}？`, '删除媒体', { type: 'warning' }); try { await mediaApi.remove(item.id); ElMessage.success('媒体已删除'); load() } catch (error) { ElMessage.error(error.message) } }
async function copy(url) { await navigator.clipboard.writeText(url); ElMessage.success('链接已复制') }
onMounted(load)
</script>

<template>
  <PageHeader title="媒体库" description="JPEG、PNG、WebP、GIF，单张不超过 10MB"><el-upload :show-file-list="false" accept="image/jpeg,image/png,image/webp,image/gif" :http-request="upload"><el-button type="primary" :loading="uploading">上传图片</el-button></el-upload></PageHeader>
  <section class="panel"><el-skeleton :loading="loading" animated :rows="8"><el-empty v-if="!rows.length" description="还没有媒体文件" /><div v-else class="media-grid"><article v-for="item in rows" :key="item.id" class="media-card"><img :src="item.url" loading="lazy" /><div class="media-card-body"><strong :title="item.originalName">{{ item.originalName }}</strong><div class="media-card-actions"><span>{{ (item.size/1024).toFixed(1) }} KB</span><span><el-button link size="small" @click="copy(item.url)">复制</el-button><el-button link type="danger" size="small" @click="remove(item)">删除</el-button></span></div></div></article></div><div class="pagination"><el-pagination v-model:current-page="page" layout="total, prev, pager, next" :page-size="24" :total="total" @current-change="load" /></div></el-skeleton></section>
</template>

<style scoped>.pagination{display:flex;justify-content:flex-end;margin-top:22px}</style>
