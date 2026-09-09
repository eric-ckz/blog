<template>
  <div class="page-shell auth-page">
    <section class="auth-card glass-panel" v-reveal>
      <p class="eyebrow">Join Us</p>
      <h1>注册</h1>
      <p class="auth-subtitle">注册后即可参与文章评论。</p>

      <form v-if="!sent" class="auth-form" @submit.prevent="register">
        <label class="field">
          <span>邮箱</span>
          <input v-model.trim="form.email" type="email" autocomplete="email" placeholder="you@example.com" required />
        </label>
        <label class="field">
          <span>昵称</span>
          <input v-model.trim="form.username" type="text" autocomplete="username" placeholder="2-32 位中英文、数字、下划线或连字符" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="form.password" type="password" autocomplete="new-password" placeholder="至少 8 位" minlength="8" required />
        </label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '发送验证码…' : '注册并发送验证码' }}
        </button>
      </form>

      <form v-else class="auth-form" @submit.prevent="verify">
        <p class="verify-hint">验证码已发送至 <strong>{{ form.email }}</strong>，10 分钟内有效。</p>
        <label class="field">
          <span>6 位验证码</span>
          <input v-model.trim="code" type="text" inputmode="numeric" maxlength="6" placeholder="请输入验证码" required />
        </label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '验证中…' : '完成注册' }}
        </button>
        <button class="secondary-button" type="button" @click="sent = false">返回修改信息</button>
      </form>

      <p class="auth-switch">已有账号？<router-link to="/login">直接登录</router-link></p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const sent = ref(false)
const code = ref('')
const form = reactive({ email: '', username: '', password: '' })

async function register() {
  loading.value = true
  error.value = ''
  try {
    await authApi.register({ email: form.email, username: form.username, password: form.password })
    sent.value = true
  } catch (requestError) {
    error.value = requestError.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function verify() {
  loading.value = true
  error.value = ''
  try {
    await authApi.verifyEmail({ email: form.email, code: code.value })
    router.push('/login')
  } catch (requestError) {
    error.value = requestError.message || '验证失败，请稍后重试'
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
.verify-hint { font-size: 13px; color: var(--muted, #8a8f9e); }
.auth-switch { margin-top: 22px; text-align: center; font-size: 13px; color: var(--muted, #8a8f9e); }
.auth-switch a { color: var(--accent, #6b7a99); }
</style>
