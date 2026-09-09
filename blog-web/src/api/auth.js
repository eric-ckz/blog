import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

export const authClient = axios.create({ baseURL, timeout: 15000, withCredentials: true })

// 访客 Access Token 从 localStorage 读取并附加到 Authorization 头，
// 否则 /web/auth/me、/profile、/avatar 等需要登录的接口会返回 401。
authClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('blog_user_access_token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function unwrap(response) {
  const payload = response?.data
  if (payload?.code && payload.code !== '20000') {
    throw Object.assign(new Error(payload.message || '请求失败'), { code: payload.code })
  }
  return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload
}

export const authApi = {
  register: (payload) => authClient.post('/web/auth/register', payload).then(unwrap),
  verifyEmail: (payload) => authClient.post('/web/auth/verify-email', payload).then(unwrap),
  login: (payload) => authClient.post('/web/auth/login', payload).then(unwrap),
  refresh: () => authClient.post('/web/auth/refresh').then(unwrap),
  logout: () => authClient.post('/web/auth/logout').then(unwrap),
  me: () => authClient.get('/web/auth/me').then(unwrap),
  updateProfile: (payload) => authClient.put('/web/auth/profile', payload).then(unwrap),
  uploadAvatar: (file) => {
    const form = new FormData()
    form.append('file', file)
    return authClient.post('/web/auth/avatar', form).then(unwrap)
  },
}
