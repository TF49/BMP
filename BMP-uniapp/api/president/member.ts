/**
 * 会长端 - 会员管理 API
 * 后端 Member 实体：id, userId, memberName, phone, memberType, memberLevel, balance, status, registerTime, expireTime 等
 */
import { get } from '@/utils/request'

export interface MemberListItem {
  id: number
  userId?: number
  memberName?: string
  phone?: string
  memberType?: string
  memberLevel?: number
  balance?: number
  status?: number
  registerTime?: string
  expireTime?: string
  totalConsumption?: number
  totalRecharge?: number
  [key: string]: unknown
}

export interface MemberListResult {
  data: MemberListItem[]
  total: number
  page?: number
  size?: number
  pages?: number
}

export function getMemberList(params?: {
  memberName?: string
  phone?: string
  memberId?: number
  memberType?: string
  status?: number
  page?: number
  size?: number
}) {
  const sanitizedParams = params
    ? Object.fromEntries(
        Object.entries(params).filter(([, value]) => value !== undefined && value !== null)
      )
    : {}
  return get<MemberListResult>('/member/list', sanitizedParams)
}
