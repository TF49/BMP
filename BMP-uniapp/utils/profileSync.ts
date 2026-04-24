import type { UpdateUserInfoParams, UserInfo } from '@/api/auth'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

export interface EditableProfileState {
  username: string
  phone: string
  gender: number
  signature: string
  avatar: string
}

export function createEditableProfileState(user?: UserInfo | null): EditableProfileState {
  return {
    username: user?.nickname || user?.username || '',
    phone: user?.phone || '',
    gender: Number(user?.gender ?? 0),
    signature: user?.signature || '',
    avatar: user?.avatar || ''
  }
}

export function toProfileUpdatePayload(profile: EditableProfileState): UpdateUserInfoParams {
  return {
    phone: profile.phone.trim(),
    gender: String(Number(profile.gender ?? 0)),
    signature: profile.signature.trim(),
    avatar: profile.avatar
  }
}

export function applyProfileToStoredUser(user: UserInfo | null, profile: EditableProfileState): UserInfo | null {
  if (!user) return null
  return {
    ...user,
    phone: profile.phone.trim(),
    gender: Number(profile.gender ?? 0),
    signature: profile.signature.trim(),
    avatar: resolveImageUrl(profile.avatar)
  }
}
