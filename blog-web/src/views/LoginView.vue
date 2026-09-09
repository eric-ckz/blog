<template>
  <div class="page-shell auth-page">
    <section class="auth-card glass-panel" v-reveal>
      <p class="eyebrow">Welcome Back</p>
      <h1>登录</h1>
      <p class="auth-subtitle">登录后即可参与文章评论。</p>

      <form class="auth-form" @submit.prevent="submit">
        <label class="field">
          <span>邮箱</span>
          <input v-model.trim="form.email" type="email" autocomplete="email" placeholder="you@example.com" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="form.password" type="password" autocomplete="current-password" placeholder="请输入密码" required />
        </label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="auth-switch">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const form = reactive({ email: '', password: '' })

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await auth.login({ email: form.email, password: form.password })
    router.replace(route.query.redirect || '/')
  } catch (requestError) {
    error.value = requestError.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding-top: 48px; }
.auth-card { width: min(440px, 100%); padding: 40px 36px; }
.auth-card h1 { margin: 6px 0 8px; font-size: 30px; }
.auth-subtitle { color: var(--muted, #8a8f9e); margin-bottom: 28px; }
.auth-form { display: flex; flex-direction: column; gap: 18px; }
.field { display: flex; flex-direction: column; gap: 8px; font-size: 13px; color: var(--muted, #8a8f9e); }
.field input { padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(255,255,255,.12); background: rgba(255,255,255,.05); color: inherit; font-size: 15px; }
.field input:focus { outline: none; border-color: var(--accent, #6b7a99); }
.form-error { color: #e06c75; font-size: 13px; }
.auth-switch { margin-top: 22px; text-align: center; font-size: 13px; color: var(--muted, #8a8f9e); }
.auth-switch a { color: var(--accent, #6b7a99); }
</style>
