import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/admin'
import { refreshSession } from '@/api/http'
import { setAccessToken } from '@/api/authToken'

/** 管理员会话状态。initialized 用于避免每次路由跳转都重复刷新 Cookie。 */
export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const initialized = ref(false)
  const authenticated = computed(() => Boolean(user.value))

  async function login(credentials) {
    const data = await authApi.login(credentials)
    setAccessToken(data.accessToken)
    user.value = data.user
    initialized.value = true
  }

  async function ensureSession() {
    if (initialized.value) return authenticated.value
    try {
      const data = await refreshSession()
      user.value = data.user
      return true
    } catch {
      setAccessToken('')
      user.value = null
      return false
    } finally {
      initialized.value = true
    }
  }

  async function logout() {
    try { await authApi.logout() } finally {
      setAccessToken('')
      user.value = null
      initialized.value = true
    }
  }

  return { user, initialized, authenticated, login, ensureSession, logout }
})
