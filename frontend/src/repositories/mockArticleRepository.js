import { aboutData, articles, categories, homeData } from '@/data/mockData'

const sortedArticles = [...articles].sort((left, right) => right.publishedAt.localeCompare(left.publishedAt))

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

function wait(signal, duration = 90) {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) {
      reject(new DOMException('Request aborted', 'AbortError'))
      return
    }
    const timer = window.setTimeout(resolve, duration)
    signal?.addEventListener('abort', () => {
      window.clearTimeout(timer)
      reject(new DOMException('Request aborted', 'AbortError'))
    }, { once: true })
  })
}

function toLink(article) {
  return article ? { id: article.id, title: article.title } : null
}

function groupArchive(items) {
  const years = new Map()
  for (const article of items) {
    const [year, month] = article.publishedAt.split('-')
    if (!years.has(year)) years.set(year, new Map())
    const months = years.get(year)
    if (!months.has(month)) months.set(month, [])
    months.get(month).push(toLink({ ...article, categoryLabel: article.categoryLabel }))
    const monthItems = months.get(month)
    monthItems[monthItems.length - 1].categoryLabel = article.categoryLabel
  }

  return [...years.entries()].map(([year, months]) => {
    const monthGroups = [...months.entries()].map(([month, monthItems]) => ({
      month,
      count: monthItems.length,
      items: monthItems,
    }))
    return {
      year,
      count: monthGroups.reduce((sum, month) => sum + month.count, 0),
      months: monthGroups,
    }
  })
}

const mockArticleRepository = {
  async getHomeData({ signal } = {}) {
    await wait(signal)
    return clone(homeData)
  },

  async getCategories({ signal } = {}) {
    await wait(signal)
    return clone(categories)
  },

  async getArticles({ category, year, keyword, page = 1, pageSize = 15, signal } = {}) {
    await wait(signal)
    const normalizedKeyword = keyword?.trim().toLocaleLowerCase('zh-CN')
    const filtered = sortedArticles.filter((article) => {
      if (category && category !== 'all' && article.category !== category) return false
      if (year && year !== 'all' && !article.publishedAt.startsWith(String(year))) return false
      if (!normalizedKeyword) return true
      const searchable = `${article.title} ${article.summary} ${article.contentHtml.replace(/<[^>]+>/g, ' ')}`.toLocaleLowerCase('zh-CN')
      return searchable.includes(normalizedKeyword)
    })
    const start = (Number(page) - 1) * Number(pageSize)
    const items = filtered.slice(start, start + Number(pageSize))
    return {
      items: clone(items),
      total: filtered.length,
      page: Number(page),
      pageSize: Number(pageSize),
      hasMore: start + items.length < filtered.length,
    }
  },

  async getArticleById(id, { signal } = {}) {
    await wait(signal, 70)
    const index = sortedArticles.findIndex((article) => article.id === String(id))
    if (index === -1) {
      const error = new Error('Article not found')
      error.status = 404
      throw error
    }
    return clone({
      ...sortedArticles[index],
      previous: toLink(sortedArticles[index + 1]),
      next: toLink(sortedArticles[index - 1]),
    })
  },

  async getArchive({ signal } = {}) {
    await wait(signal)
    return clone(groupArchive(sortedArticles))
  },

  async getAbout({ signal } = {}) {
    await wait(signal)
    return clone(aboutData)
  },
}

export { groupArchive }
export default mockArticleRepository
