import axios from 'axios'
import { getAccessToken } from '@/stores/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

export const commentClient = axios.create({ baseURL, timeout: 15000, withCredentials: true })

commentClient.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

function unwrap(response) {
  const payload = response?.data
  if (payload?.code && payload.code !== '20000') {
    throw Object.assign(new Error(payload.message || '请求失败'), { code: payload.code })
  }
  return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload
}

export const commentApi = {
  list: (articleId, { page = 1, pageSize = 15, signal } = {}) =>
    commentClient.get(`/common/articles/${encodeURIComponent(articleId)}/comments`, {
      params: { page, pageSize },
      signal,
    }).then(unwrap),
  create: (articleId, content) =>
    commentClient.post(`/common/articles/${encodeURIComponent(articleId)}/comments`, { content }).then(unwrap),
}
