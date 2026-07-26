export function normalizeArticle(article = {}) {
  return {
    id: String(article.id ?? ''),
    title: article.title ?? '',
    summary: article.summary ?? '',
    category: article.category ?? '',
    categoryLabel: article.categoryLabel ?? '',
    coverUrl: article.coverUrl ?? article.cover ?? '',
    publishedAt: article.publishedAt ?? article.createdAt ?? '',
    views: Number(article.views ?? 0),
    readMinutes: Number(article.readMinutes ?? article.readTime ?? 0),
    contentHtml: article.contentHtml ?? article.content ?? '',
    originalUrl: article.originalUrl ?? '',
    previous: article.previous ? normalizeArticleLink(article.previous) : null,
    next: article.next ? normalizeArticleLink(article.next) : null,
  }
}

export function normalizeArticleLink(article = {}) {
  return {
    id: String(article.id ?? ''),
    title: article.title ?? '',
  }
}

export function normalizePage(payload = {}) {
  const items = (payload.items ?? payload.list ?? []).map(normalizeArticle)
  const total = Number(payload.total ?? items.length)
  const page = Number(payload.page ?? 1)
  const pageSize = Number(payload.pageSize ?? items.length)
  return {
    items,
    total,
    page,
    pageSize,
    hasMore: payload.hasMore ?? page * pageSize < total,
  }
}
