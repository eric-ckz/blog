import axios from 'axios'
import { normalizeArticle, normalizePage } from './normalizers'

/**
 * 后端统一使用 code/message/data 包装响应。保留对裸数据的兼容，便于 Repository
 * 单元测试以及旧接口在迁移期间继续工作。
 */
function unwrapResponse(response) {
  const payload = response?.data
  return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload
}

export function createHttpArticleRepository(client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})) {
  return {
    async getHomeData({ signal } = {}) {
      const data = unwrapResponse(await client.get('/site/home', { signal }))
      return {
        ...data,
        heroSlides: (data.heroSlides ?? []).map(normalizeArticle),
        featured: (data.featured ?? []).map(normalizeArticle),
        recent: (data.recent ?? []).map(normalizeArticle),
      }
    },
    async getCategories({ signal } = {}) {
      return unwrapResponse(await client.get('/categories', { signal }))
    },
    async getArticles({ category, year, keyword, page = 1, pageSize = 15, signal } = {}) {
      // “all” 只属于筛选控件的前端状态，不能传给要求整数年份的 Java DTO。
      // 在 Repository 边界统一清理，也能避免其他页面未来重复实现相同转换。
      const params = {
        category: category && category !== 'all' ? category : undefined,
        year: year && year !== 'all' ? year : undefined,
        keyword: keyword?.trim() || undefined,
        page,
        pageSize,
      }
      Object.keys(params).forEach((key) => params[key] == null && delete params[key])
      return normalizePage(unwrapResponse(await client.get('/articles', { params, signal })))
    },
    async getArticleById(id, { signal } = {}) {
      return normalizeArticle(unwrapResponse(await client.get(`/articles/${encodeURIComponent(id)}`, { signal })))
    },
    async getArchive({ signal } = {}) {
      return unwrapResponse(await client.get('/archive', { signal }))
    },
    async getAbout({ signal } = {}) {
      return unwrapResponse(await client.get('/site/about', { signal }))
    },
  }
}

export default createHttpArticleRepository()
