/**
 * 会长端 - 场馆管理 API
 */
import { get, post, put, del } from '@/utils/request'

export interface VenueItem {
  id: number
  venueName: string
  address?: string
  contactPhone?: string
  contactPerson?: string
  businessHours?: string
  description?: string
  // 后端 venue 主图（会以相对路径形式返回，如 /api/uploads/venues/xxx.png）
  // 小程序端需要拼接 IMAGE_BASE_URL 后才能展示。
  venueImage?: string
  status: number
  hourlyPrice?: number
  createTime?: string
  updateTime?: string
}

export interface VenueListResult {
  data: VenueItem[]
  total: number
  page?: number
  size?: number
  pages?: number
}

export function getVenueList(params?: {
  venueName?: string
  status?: number
  page?: number
  size?: number
}) {
  // 过滤掉 undefined / null 的参数，避免后端收到 "undefined" 字符串
  const cleanParams = params
    ? Object.fromEntries(
        Object.entries(params).filter(([, value]) => value !== undefined && value !== null)
      )
    : {}

  return get<VenueListResult>('/venue/list', cleanParams)
}

export function getVenueInfo(id: number) {
  return get<VenueItem>(`/venue/info/${id}`)
}

export function addVenue(data: Record<string, unknown>) {
  return post<unknown>('/venue/add', data)
}

export function updateVenue(data: Record<string, unknown>) {
  return put<unknown>('/venue/update', data)
}

export function deleteVenue(id: number) {
  return del<unknown>(`/venue/${id}`)
}
