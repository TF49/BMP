import { describe, expect, it } from 'vitest'
import {
  applyProfileToStoredUser,
  createEditableProfileState,
  toProfileUpdatePayload
} from '@/utils/profileSync'

describe('profileSync helpers', () => {
  it('creates editable state from stored user', () => {
    const state = createEditableProfileState({
      id: 1,
      username: 'demo',
      role: 'USER',
      status: 1,
      phone: '13800000000',
      gender: 1,
      signature: '热爱羽毛球',
      avatar: '/uploads/avatar.png'
    })

    expect(state).toEqual({
      username: 'demo',
      phone: '13800000000',
      gender: 1,
      signature: '热爱羽毛球',
      avatar: '/uploads/avatar.png'
    })
  })

  it('builds update payload and syncs stored user', () => {
    const profile = {
      username: 'demo',
      phone: ' 13800000001 ',
      gender: 2,
      signature: '  新签名 ',
      avatar: '/uploads/new-avatar.png'
    }

    expect(toProfileUpdatePayload(profile)).toEqual({
      phone: '13800000001',
      gender: '2',
      signature: '新签名',
      avatar: '/uploads/new-avatar.png'
    })

    const stored = applyProfileToStoredUser(
      {
        id: 1,
        username: 'demo',
        role: 'USER',
        status: 1
      },
      profile
    )

    expect(stored?.phone).toBe('13800000001')
    expect(stored?.gender).toBe(2)
    expect(stored?.signature).toBe('新签名')
    expect(stored?.avatar).toContain('/uploads/new-avatar.png')
  })
})
