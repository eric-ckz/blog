import { describe, expect, it } from 'vitest'
import repository, { groupArchive } from '@/repositories/mockArticleRepository'

describe('mockArticleRepository', () => {
  it('filters articles by category, year, and keyword', async () => {
    const result = await repository.getArticles({
      category: 'travel',
      year: '2026',
      keyword: '香港',
      page: 1,
      pageSize: 10,
    })

    expect(result.items.length).toBeGreaterThan(0)
    expect(result.items.every((item) => item.category === 'travel')).toBe(true)
    expect(result.items.every((item) => item.publishedAt.startsWith('2026'))).toBe(true)
  })

  it('returns stable pagination metadata', async () => {
    const result = await repository.getArticles({ page: 1, pageSize: 5 })
    expect(result.items).toHaveLength(5)
    expect(result.total).toBeGreaterThan(5)
    expect(result.hasMore).toBe(true)
  })

  it('normalizes ids as strings and provides adjacent articles', async () => {
    const article = await repository.getArticleById(0)
    expect(article.id).toBe('0')
    expect(article.previous || article.next).toBeTruthy()
  })

  it('groups archive entries by year and month', () => {
    const archive = groupArchive([
      { id: '1', title: 'A', categoryLabel: '生活感悟', publishedAt: '2026-07-01' },
      { id: '2', title: 'B', categoryLabel: '旅行游记', publishedAt: '2026-06-01' },
    ])
    expect(archive[0].year).toBe('2026')
    expect(archive[0].months).toHaveLength(2)
  })
})
