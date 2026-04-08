/**
 * 会长端 - 会员管理 API
 * 后端 Member 实体：id, userId, memberName, phone, memberType, memberLevel, balance, status, registerTime, expireTime 等
 */
import { get, post } from '@/utils/request'

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

/** 新增会员（会长/场馆管理员），字段与后端 `Member` 校验一致 */
export function addMember(data: {
  memberName: string
  gender?: 0 | 1
  phone: string
  idCard?: string
  memberType: 'NORMAL' | 'MEMBER'
  memberLevel?: number
  status?: number
}) {
  return post<MemberListItem>('/member/add', data)
}
