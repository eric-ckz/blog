<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { siteApi } from '@/api/admin'

const loading = ref(true)
const saving = ref(false)
const items = ref([])
const typeLabels = { INTRO: '介绍段落', LOVE: '热爱事项', TIMELINE: '时间线', CONTACT: '联系方式' }

function add(type) { items.value.push({ itemType: type, title: '', description: '', icon: '', sortOrder: items.value.filter(i => i.itemType === type).length + 1 }) }
function remove(index) { items.value.splice(index, 1) }
async function save() { saving.value = true; try { await siteApi.updateAbout({ items: items.value }); ElMessage.success('关于页已保存') } catch (error) { ElMessage.error(error.message) } finally { saving.value = false } }
onMounted(async () => { try { const data = await siteApi.about(); items.value = (data.items || []).map(({ itemType, title, description, icon, sortOrder }) => ({ itemType, title, description, icon, sortOrder })) } catch (error) { ElMessage.error(error.message) } finally { loading.value = false } })
</script>

<template>
  <PageHeader title="关于页配置" description="维护介绍、热爱事项、时间线与联系方式"><el-button type="primary" :loading="saving" @click="save">保存关于页</el-button></PageHeader>
  <el-skeleton :loading="loading" animated :rows="10">
    <section v-for="type in ['INTRO','LOVE','TIMELINE','CONTACT']" :key="type" class="panel about-section">
      <div class="section-title"><div><h3>{{ typeLabels[type] }}</h3><p>{{ {INTRO:'个人介绍段落',LOVE:'文学、摄影、旅行等事项',TIMELINE:'按年份记录重要节点',CONTACT:'公众号等联系方式'}[type] }}</p></div><el-button size="small" @click="add(type)">新增</el-button></div>
      <el-empty v-if="!items.some(item => item.itemType === type)" description="暂无内容" :image-size="60" />
      <div v-for="(item, index) in items" v-show="item.itemType === type" :key="index" class="about-row">
        <el-input v-if="type !== 'INTRO'" v-model="item.title" :placeholder="type === 'TIMELINE' ? '年份' : type === 'CONTACT' ? '联系方式名称' : '标题'" />
        <el-input v-model="item.description" :type="type === 'INTRO' ? 'textarea' : 'text'" :rows="3" placeholder="内容描述" />
        <el-input v-if="type === 'LOVE'" v-model="item.icon" placeholder="图标，如 book" />
        <el-input-number v-model="item.sortOrder" :min="0" controls-position="right" />
        <el-button link type="danger" @click="remove(index)">删除</el-button>
      </div>
    </section>
  </el-skeleton>
</template>

<style scoped>.about-section{margin-bottom:16px}.section-title{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:14px}.section-title h3{margin:0;font:600 18px Georgia,"Songti SC",serif}.section-title p{margin:5px 0 0;color:#8c91a1;font-size:12px}.about-row{display:grid;grid-template-columns:160px minmax(240px,1fr) 150px 105px 45px;gap:10px;align-items:start;padding:12px 0;border-top:1px solid #eceef3}.about-row:has(textarea){grid-template-columns:minmax(300px,1fr) 105px 45px}.about-row:has(textarea)>:first-child{grid-column:auto}@media(max-width:850px){.about-row,.about-row:has(textarea){grid-template-columns:1fr}.about-row>*{width:100%!important}}</style>
