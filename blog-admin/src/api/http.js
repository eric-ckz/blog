import axios from 'axios'
import { getAccessToken, setAccessToken } from './authToken'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

export const http = axios.create({ baseURL, timeout: 15000, withCredentials: true })
const refreshClient = axios.create({ baseURL, timeout: 15000, withCredentials: true })
let refreshPromise = null

http.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => unwrap(response),
  async (error) => {
    const original = error.config
    const isAuthEndpoint = original?.url?.includes('/admin/auth/')
    if (error.response?.status === 401 && original && !original.__retried && !isAuthEndpoint) {
      original.__retried = true
      refreshPromise ||= refreshClient.post('/admin/auth/refresh')
        .then((response) => {
          const data = unwrap(response)
          setAccessToken(data.accessToken)
          return data
        })
        .finally(() => { refreshPromise = null })
      await refreshPromise
      original.headers = original.headers || {}
      original.headers.Authorization = `Bearer ${getAccessToken()}`
      return http(original)
    }
    return Promise.reject(normalizeError(error))
  },
)

/** 将后端统一包装转换为页面直接使用的数据。 */
export function unwrap(response) {
  const payload = response?.data
  if (payload?.code && payload.code !== '20000') {
    throw Object.assign(new Error(payload.message || '请求失败'), { code: payload.code })
  }
  return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload
}

function normalizeError(error) {
  const message = error.response?.data?.message || error.message || '网络请求失败'
  return Object.assign(new Error(message), {
    status: error.response?.status,
    code: error.response?.data?.code,
  })
}

export async function refreshSession() {
  const response = await refreshClient.post('/admin/auth/refresh')
  const data = unwrap(response)
  setAccessToken(data.accessToken)
  return data
}
