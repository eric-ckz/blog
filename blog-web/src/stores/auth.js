import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

const TOKEN_KEY = 'blog_user_access_token'

export const getAccessToken = () => localStorage.getItem(TOKEN_KEY) || ''
export const setAccessToken = (value = '') => {
  if (value) localStorage.setItem(TOKEN_KEY, value)
  else localStorage.removeItem(TOKEN_KEY)
}

/** 访客会话状态。 */
export const useAuthStore = defineStore('userAuth', () => {
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
      const data = await authApi.refresh()
      setAccessToken(data.accessToken)
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

  async function updateProfile(payload) {
    user.value = await authApi.updateProfile(payload)
  }

  async function uploadAvatar(file) {
    user.value = await authApi.uploadAvatar(file)
  }

  return { user, initialized, authenticated, login, ensureSession, logout, updateProfile, uploadAvatar }
})
