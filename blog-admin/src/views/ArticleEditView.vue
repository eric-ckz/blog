<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EditorContent, useEditor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Link from '@tiptap/extension-link'
import PageHeader from '@/components/PageHeader.vue'
import { articleApi, categoryApi, mediaApi } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const editing = computed(() => Boolean(route.params.id))
const saving = ref(false)
const loading = ref(editing.value)
const categories = ref([])
const media = ref([])
const mediaDialog = ref(false)
const form = reactive({
  title: '', summary: '', categoryId: '', coverMediaId: '', coverUrl: '', contentHtml: '<p></p>',
  status: 'DRAFT', publishedAt: '', readMinutes: 3, originalUrl: '',
})

const editor = useEditor({
  content: form.contentHtml,
  extensions: [StarterKit, Image, Link.configure({ openOnClick: false })],
  onUpdate: ({ editor }) => { form.contentHtml = editor.getHTML() },
})

async function chooseMedia() {
  try { media.value = (await mediaApi.list({ page: 1, pageSize: 100 })).items; mediaDialog.value = true } catch (error) { ElMessage.error(error.message) }
}
function useCover(item) { form.coverMediaId = item.id; form.coverUrl = item.url; mediaDialog.value = false }
function insertImage(item) { editor.value?.chain().focus().setImage({ src: item.url, alt: item.originalName }).run(); mediaDialog.value = false }

async function submit() {
  if (!form.title || !form.summary || !form.categoryId || !form.contentHtml) return ElMessage.warning('请补全标题、摘要、栏目和正文')
  saving.value = true
  try {
    const payload = { ...form, publishedAt: form.publishedAt || null, coverMediaId: form.coverMediaId || null, coverUrl: form.coverUrl || null }
    if (editing.value) await articleApi.update(route.params.id, payload)
    else await articleApi.create(payload)
    ElMessage.success(editing.value ? '文章已更新' : '文章已创建')
    router.push('/articles')
  } catch (error) { ElMessage.error(error.message) } finally { saving.value = false }
}

onMounted(async () => {
  try {
    categories.value = await categoryApi.list()
    if (editing.value) {
      Object.assign(form, await articleApi.get(route.params.id))
      editor.value?.commands.setContent(form.contentHtml || '<p></p>')
    }
  } catch (error) { ElMessage.error(error.message) } finally { loading.value = false }
})
onBeforeUnmount(() => editor.value?.destroy())
</script>

<template>
  <PageHeader :title="editing ? '编辑文章' : '新建文章'" description="正文保存前会由服务端再次进行安全清理">
    <el-button @click="router.back()">取消</el-button><el-button type="primary" :loading="saving" @click="submit">保存文章</el-button>
  </PageHeader>
  <el-skeleton :loading="loading" animated :rows="10">
    <el-form label-position="top" class="edit-grid">
      <section class="panel editor-panel">
        <el-form-item label="文章标题"><el-input v-model="form.title" maxlength="255" show-word-limit size="large" placeholder="写下一个值得重读的标题" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="3" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item label="正文">
          <div class="editor-shell">
            <div v-if="editor" class="editor-toolbar">
              <button type="button" :class="{ 'is-active': editor.isActive('bold') }" @click="editor.chain().focus().toggleBold().run()">粗体</button>
              <button type="button" :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }" @click="editor.chain().focus().toggleHeading({ level: 2 }).run()">二级标题</button>
              <button type="button" :class="{ 'is-active': editor.isActive('blockquote') }" @click="editor.chain().focus().toggleBlockquote().run()">引用</button>
              <button type="button" :class="{ 'is-active': editor.isActive('bulletList') }" @click="editor.chain().focus().toggleBulletList().run()">列表</button>
              <button type="button" @click="chooseMedia">插入图片</button>
            </div>
            <EditorContent :editor="editor" />
          </div>
        </el-form-item>
      </section>
      <aside class="panel side-form">
        <el-form-item label="文章状态"><el-select v-model="form.status" style="width:100%"><el-option label="草稿" value="DRAFT" /><el-option label="已发布" value="PUBLISHED" /><el-option label="已归档" value="ARCHIVED" /></el-select></el-form-item>
        <el-form-item label="所属栏目"><el-select v-model="form.categoryId" style="width:100%"><el-option v-for="item in categories" :key="item.id" :label="item.label" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="发布时间"><el-date-picker v-model="form.publishedAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" placeholder="发布时为空则使用当前时间" /></el-form-item>
        <el-form-item label="预计阅读"><el-input-number v-model="form.readMinutes" :min="1" :max="240" /><span class="form-suffix">分钟</span></el-form-item>
        <el-form-item label="封面">
          <div v-if="form.coverUrl" class="cover-preview"><img :src="form.coverUrl" /><button type="button" @click="form.coverMediaId='';form.coverUrl=''">移除</button></div>
          <el-button v-else style="width:100%" @click="chooseMedia">从媒体库选择</el-button>
        </el-form-item>
        <el-form-item label="原文地址"><el-input v-model="form.originalUrl" placeholder="https://" /></el-form-item>
      </aside>
    </el-form>
  </el-skeleton>

  <el-dialog v-model="mediaDialog" title="选择媒体" width="min(860px, 92vw)">
    <div class="media-grid"><article v-for="item in media" :key="item.id" class="media-card"><img :src="item.url" /><div class="media-card-body"><strong>{{ item.originalName }}</strong><div class="choose-actions"><el-button size="small" @click="insertImage(item)">插入正文</el-button><el-button size="small" type="primary" @click="useCover(item)">设为封面</el-button></div></div></article></div>
  </el-dialog>
</template>

<style scoped>
.edit-grid{display:grid;grid-template-columns:minmax(0,1fr) 300px;gap:18px;align-items:start}.editor-panel{min-width:0}.editor-shell{width:100%}.side-form{position:sticky;top:20px}.form-suffix{margin-left:8px;color:#8a8f9e;font-size:12px}.cover-preview{position:relative;width:100%;overflow:hidden;border-radius:10px}.cover-preview img{width:100%;aspect-ratio:16/10;display:block;object-fit:cover}.cover-preview button{position:absolute;right:8px;top:8px;padding:5px 9px;border:0;border-radius:7px;color:#fff;background:rgba(20,22,32,.72);cursor:pointer}.choose-actions{display:flex;gap:8px;margin-top:9px}@media(max-width:900px){.edit-grid{grid-template-columns:1fr}.side-form{position:static}}
</style>
