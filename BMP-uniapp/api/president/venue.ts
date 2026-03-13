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
  status: number
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
  return get<VenueListResult>('/venue/list', params || {})
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
