import axios from 'axios'
import { normalizeArticle, normalizePage } from './normalizers'

export function createHttpArticleRepository(client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})) {
  return {
    async getHomeData({ signal } = {}) {
      const { data } = await client.get('/site/home', { signal })
      return {
        ...data,
        heroSlides: (data.heroSlides ?? []).map(normalizeArticle),
        featured: (data.featured ?? []).map(normalizeArticle),
        recent: (data.recent ?? []).map(normalizeArticle),
      }
    },
    async getCategories({ signal } = {}) {
      const { data } = await client.get('/categories', { signal })
      return data
    },
    async getArticles({ category, year, keyword, page = 1, pageSize = 15, signal } = {}) {
      const params = { category, year, keyword, page, pageSize }
      Object.keys(params).forEach((key) => params[key] == null && delete params[key])
      const { data } = await client.get('/articles', { params, signal })
      return normalizePage(data)
    },
    async getArticleById(id, { signal } = {}) {
      const { data } = await client.get(`/articles/${encodeURIComponent(id)}`, { signal })
      return normalizeArticle(data)
    },
    async getArchive({ signal } = {}) {
      const { data } = await client.get('/archive', { signal })
      return data
    },
    async getAbout({ signal } = {}) {
      const { data } = await client.get('/site/about', { signal })
      return data
    },
  }
}

export default createHttpArticleRepository()
