/**
 * 会长端 - 用户管理 API
 */
import { get, post, put, del } from '@/utils/request'

export interface UserListItem {
  id: number
  username: string
  role: string
  status: number
  venueId?: number
  idCard?: string
  createTime?: string
}

export interface UserDetail extends UserListItem {
  [key: string]: unknown
}

export interface UserListResult {
  data: UserListItem[]
  total: number
  page: number
  size: number
  pages?: number
}

export function getUserList(params: {
  username?: string
  idCard?: string
  role?: string
  status?: number
  page?: number
  size?: number
}) {
  return get<UserListResult>('/user/list', params)
}

export function getUserInfo(id: number) {
  return get<UserDetail>(`/user/info/${id}`)
}

export function addUser(data: Record<string, unknown>) {
  return post<unknown>('/user/add', data)
}

export function updateUser(data: Record<string, unknown>) {
  return put<unknown>('/user/update', data)
}

export function deleteUser(id: number) {
  return del<unknown>(`/user/${id}`)
}

/** 检查是否已存在协会会长（用于唯一性校验）：拉取角色为 PRESIDENT 的列表，若有则已存在 */
export async function checkPresidentExists(excludeUserId?: number): Promise<boolean> {
  const res = await get<{ data?: UserListItem[]; total?: number }>('/user/list', { role: 'PRESIDENT', page: 1, size: 1 })
  const data = (res as any)?.data ?? res
  const list = Array.isArray(data) ? data : (data?.data ?? data?.list ?? [])
  const total = (res as any)?.total ?? data?.total ?? list.length
  if (total === 0 || list.length === 0) return false
  if (excludeUserId != null && list[0]?.id === excludeUserId) return false
  return true
}
