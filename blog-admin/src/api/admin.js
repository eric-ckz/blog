import { http } from './http'

export const authApi = {
  login: (payload) => http.post('/admin/auth/login', payload),
  logout: () => http.post('/admin/auth/logout'),
  me: () => http.get('/admin/auth/me'),
  changePassword: (payload) => http.put('/admin/user/password', payload),
}

export const dashboardApi = { get: () => http.get('/admin/dashboard') }

export const articleApi = {
  list: (params) => http.get('/admin/articles', { params }),
  get: (id) => http.get(`/admin/articles/${id}`),
  create: (payload) => http.post('/admin/articles', payload),
  update: (id, payload) => http.put(`/admin/articles/${id}`, payload),
  remove: (id) => http.delete(`/admin/articles/${id}`),
}

export const categoryApi = {
  list: () => http.get('/admin/categories'),
  create: (payload) => http.post('/admin/categories', payload),
  update: (id, payload) => http.put(`/admin/categories/${id}`, payload),
  remove: (id) => http.delete(`/admin/categories/${id}`),
}

export const mediaApi = {
  list: (params) => http.get('/admin/media', { params }),
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/admin/media', form)
  },
  remove: (id) => http.delete(`/admin/media/${id}`),
}

export const siteApi = {
  settings: () => http.get('/admin/site/settings'),
  updateSettings: (payload) => http.put('/admin/site/settings', payload),
  about: () => http.get('/admin/site/about'),
  updateAbout: (payload) => http.put('/admin/site/about', payload),
}

export const commentApi = {
  list: (params) => http.get('/admin/comments', { params }),
  remove: (id) => http.delete(`/admin/comments/${id}`),
}

export const userApi = {
  list: (params) => http.get('/admin/users', { params }),
  setEnabled: (id, enabled) => http.put(`/admin/users/${id}/enabled`, { enabled }),
}