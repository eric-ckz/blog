import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { describe, expect, it } from 'vitest'
import { createHttpArticleRepository } from '@/repositories/httpArticleRepository'

describe('httpArticleRepository', () => {
  it('passes filters and normalizes a paginated response', async () => {
    const client = axios.create()
    const mock = new MockAdapter(client)
    mock.onGet('/articles').reply((config) => {
      expect(config.params.category).toBe('travel')
      return [200, {
        items: [{ id: 1, title: 'A', cover: '/a.jpg', createdAt: '2026-01-01' }],
        total: 1,
        page: 1,
        pageSize: 15,
      }]
    })

    const repository = createHttpArticleRepository(client)
    const result = await repository.getArticles({ category: 'travel' })
    expect(result.items[0]).toMatchObject({ id: '1', coverUrl: '/a.jpg', publishedAt: '2026-01-01' })
    expect(result.hasMore).toBe(false)
  })
})
