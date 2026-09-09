<template>
  <section class="comment-section">
    <h2 class="comment-title">评论 <span v-if="total > 0">({{ total }})</span></h2>

    <div v-if="loading" class="comment-loading">评论加载中…</div>
    <div v-else-if="error" class="comment-error">评论暂时无法加载。</div>

    <template v-else>
      <ul v-if="comments.length" class="comment-list">
        <li v-for="comment in comments" :key="comment.id" class="comment-item">
          <img v-if="comment.author?.avatarUrl" :src="comment.author.avatarUrl" class="comment-avatar" alt="头像" />
          <span v-else class="comment-avatar comment-avatar-fallback">{{ authorInitial(comment) }}</span>
          <div class="comment-body">
            <div class="comment-meta">
              <strong>{{ comment.author?.displayName || comment.author?.username || '匿名' }}</strong>
              <time>{{ formatTime(comment.createdAt) }}</time>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
          </div>
        </li>
      </ul>
      <p v-else class="comment-empty">还没有评论，来抢沙发吧。</p>

      <div v-if="needLogin" class="comment-login-hint">
        <p>登录后查看完整评论并参与讨论。</p>
        <router-link class="secondary-button" to="/login">登录</router-link>
      </div>

      <template v-else>
        <form v-if="auth.authenticated" class="comment-form" @submit.prevent="submit">
          <textarea v-model.trim="draft" rows="3" maxlength="1000" placeholder="写下你的想法…" required></textarea>
          <div class="comment-form-actions">
            <span class="comment-count">{{ draft.length }}/1000</span>
            <button class="primary-button" type="submit" :disabled="posting">{{ posting ? '发表中…' : '发表评论' }}</button>
          </div>
          <p v-if="postError" class="comment-error">{{ postError }}</p>
        </form>
        <p v-else class="comment-login-hint">
          <router-link class="secondary-button" to="/login">登录后发表评论</router-link>
        </p>

        <button v-if="hasMore" class="secondary-button comment-more" type="button" :disabled="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中…' : '加载更多' }}
        </button>
      </template>
    </template>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { commentApi } from '@/api/comment'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({ articleId: { type: String, required: true } })

const auth = useAuthStore()
const comments = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 15
const hasMore = ref(false)
const needLogin = ref(false)
const loading = ref(true)
const loadingMore = ref(false)
const error = ref(false)
const draft = ref('')
const posting = ref(false)
const postError = ref('')

function authorInitial(comment) {
  return (comment.author?.displayName || comment.author?.username || '匿').slice(0, 1)
}

function formatTime(value) {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

async function load(reset = false) {
  if (reset) {
    loading.value = true
    error.value = false
    page.value = 1
  } else {
    loadingMore.value = true
  }
  try {
    const data = await commentApi.list(props.articleId, { page: page.value, pageSize })
    if (reset) {
      comments.value = data.items || []
    } else {
      comments.value = comments.value.concat(data.items || [])
    }
    total.value = data.total || 0
    hasMore.value = data.hasMore
    needLogin.value = data.needLogin
  } catch {
    if (reset) error.value = true
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  page.value += 1
  await load(false)
}

async function submit() {
  posting.value = true
  postError.value = ''
  try {
    await commentApi.create(props.articleId, draft.value)
    draft.value = ''
    await load(true)
  } catch (requestError) {
    postError.value = requestError.message || '发表失败，请稍后重试'
  } finally {
    posting.value = false
  }
}

onMounted(async () => {
  await auth.ensureSession()
  await load(true)
})
</script>

<style scoped>
.comment-section { margin-top: 48px; padding-top: 32px; border-top: 1px solid rgba(255,255,255,.14); }
.comment-title { font-size: 22px; margin-bottom: 20px; }
.comment-title span { color: var(--muted, #8a8f9e); font-size: 15px; }
.comment-loading, .comment-empty, .comment-error { color: var(--muted, #8a8f9e); font-size: 14px; padding: 12px 0; }
.comment-error { color: #e06c75; }
.comment-list { list-style: none; display: flex; flex-direction: column; gap: 20px; }
.comment-item { display: flex; gap: 14px; }
.comment-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; flex: 0 0 40px; }
.comment-avatar-fallback { display: inline-flex; align-items: center; justify-content: center; color: #fff; background: linear-gradient(145deg, #222329, #474750); font-weight: 700; }
.comment-body { min-width: 0; }
.comment-meta { display: flex; align-items: baseline; gap: 10px; }
.comment-meta strong { font-size: 14px; }
.comment-meta time { color: var(--muted, #8a8f9e); font-size: 12px; }
.comment-content { margin-top: 6px; font-size: 15px; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
.comment-login-hint { margin-top: 20px; padding: 18px; border-radius: 16px; background: rgba(255,255,255,.05); display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.comment-login-hint p { color: var(--muted, #8a8f9e); font-size: 14px; }
.comment-form { margin-top: 24px; display: flex; flex-direction: column; gap: 12px; }
.comment-form textarea { padding: 14px; border-radius: 14px; border: 1px solid rgba(255,255,255,.12); background: rgba(255,255,255,.05); color: inherit; font-size: 15px; resize: vertical; }
.comment-form textarea:focus { outline: none; border-color: var(--accent, #6b7a99); }
.comment-form-actions { display: flex; align-items: center; justify-content: space-between; }
.comment-count { color: var(--muted, #8a8f9e); font-size: 12px; }
.comment-more { margin-top: 20px; }
</style>
