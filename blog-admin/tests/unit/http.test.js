import { describe, expect, it } from 'vitest'
import { unwrap } from '../../src/api/http'

describe('统一响应解包', () => {
  it('returns wrapped data', () => {
    expect(unwrap({ data: { code: '20000', message: 'ok', data: { id: '1' } } })).toEqual({ id: '1' })
  })

  it('keeps compatibility with naked payloads', () => {
    expect(unwrap({ data: { items: [] } })).toEqual({ items: [] })
  })
})
