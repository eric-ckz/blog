<template>
  <div class="page-shell profile-page">
    <section class="profile-card glass-panel" v-reveal>
      <p class="eyebrow">Profile</p>
      <h1>个人中心</h1>

      <div class="profile-head">
        <div class="avatar-wrap">
          <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" alt="头像" />
          <span v-else class="avatar-fallback">{{ initial }}</span>
          <label class="avatar-upload">
            更换头像
            <input type="file" accept="image/*" hidden @change="onAvatar" />
          </label>
        </div>
        <div class="profile-meta">
          <strong>{{ auth.user?.displayName || auth.user?.username }}</strong>
          <span>{{ auth.user?.email }}</span>
        </div>
      </div>

      <form class="profile-form" @submit.prevent="save">
        <label class="field">
          <span>昵称</span>
          <input v-model.trim="form.username" type="text" placeholder="2-32 位中英文、数字、下划线或连字符" />
        </label>
        <label class="field">
          <span>展示名称</span>
          <input v-model.trim="form.displayName" type="text" placeholder="留空则使用昵称" />
        </label>
        <label class="field">
          <span>个人简介</span>
          <textarea v-model.trim="form.bio" rows="3" placeholder="介绍一下自己（最多 500 字）"></textarea>
        </label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <p v-if="saved" class="form-success">已保存</p>
        <button class="primary-button" type="submit" :disabled="loading">保存资料</button>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const saved = ref(false)
const form = reactive({ username: '', displayName: '', bio: '' })

const initial = computed(() => (auth.user?.displayName || auth.user?.username || '文').slice(0, 1))

watch(() => auth.user, (user) => {
  if (user) Object.assign(form, { username: user.username, displayName: user.displayName || '', bio: user.bio || '' })
}, { immediate: true })

async function save() {
  loading.value = true
  error.value = ''
  saved.value = false
  try {
    await auth.updateProfile({ username: form.username, displayName: form.displayName, bio: form.bio })
    saved.value = true
  } catch (requestError) {
    error.value = requestError.message || '保存失败'
  } finally {
    loading.value = false
  }
}

async function onAvatar(event) {
  const file = event.target.files?.[0]
  if (!file) return
  loading.value = true
  error.value = ''
  try {
    await auth.uploadAvatar(file)
  } catch (requestError) {
    error.value = requestError.message || '头像上传失败'
  } finally {
    loading.value = false
    event.target.value = ''
  }
}
</script>

<style scoped>
.profile-page { display: flex; justify-content: center; padding-top: 48px; }
.profile-card { width: min(560px, 100%); padding: 40px 36px; }
.profile-card h1 { margin: 6px 0 24px; font-size: 30px; }
.profile-head { display: flex; align-items: center; gap: 20px; margin-bottom: 28px; }
.avatar-wrap { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.avatar-wrap img, .avatar-fallback { width: 84px; height: 84px; border-radius: 50%; object-fit: cover; display: flex; align-items: center; justify-content: center; font-size: 30px; background: rgba(255,255,255,.08); }
.avatar-upload { font-size: 12px; color: var(--accent, #6b7a99); cursor: pointer; }
.profile-meta { display: flex; flex-direction: column; gap: 4px; }
.profile-meta strong { font-size: 20px; }
.profile-meta span { color: var(--muted, #8a8f9e); font-size: 13px; }
.profile-form { display: flex; flex-direction: column; gap: 18px; }
.field { display: flex; flex-direction: column; gap: 8px; font-size: 13px; color: var(--muted, #8a8f9e); }
.field input, .field textarea { padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(255,255,255,.12); background: rgba(255,255,255,.05); color: inherit; font-size: 15px; resize: vertical; }
.field input:focus, .field textarea:focus { outline: none; border-color: var(--accent, #6b7a99); }
.form-error { color: #e06c75; font-size: 13px; }
.form-success { color: #7ec699; font-size: 13px; }
</style>
