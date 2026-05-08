import { describe, expect, it } from 'vitest'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

describe('resolveImageUrl', () => {
  it('preserves absolute http image urls', () => {
    expect(resolveImageUrl('http://127.0.0.1:9090/uploads/avatar.png')).toBe(
      'http://127.0.0.1:9090/uploads/avatar.png'
    )
  })

  it('preserves absolute https image urls', () => {
    expect(resolveImageUrl('https://example.com/uploads/avatar.png')).toBe(
      'https://example.com/uploads/avatar.png'
    )
  })

  it('resolves relative image urls against image base url', () => {
    expect(resolveImageUrl('/uploads/avatar.png')).toContain('/uploads/avatar.png')
  })
})
