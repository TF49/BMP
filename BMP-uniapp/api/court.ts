import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface CourtItem {
  id: number
  courtCode: string
  courtName: string
  venueId: number
  billingType?: string
  pricePerHour?: number
  pricePerTime?: number
  status: number
}

/**
 * 获取场地列表（按场馆、分页）
 */
export function getCourtList(params?: {
  venueId?: number
  date?: string
  status?: number
  keyword?: string
  page?: number
  size?: number
}) {
  const query: Record<string, string | number> = {
    page: params?.page ?? 1,
    size: params?.size ?? 100
  }
  if (params?.venueId != null) query.venueId = params.venueId
  if (params?.status != null) query.status = params.status
  if (params?.keyword != null && params.keyword !== '') query.keyword = params.keyword

  return request<{
    data: CourtItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.COURT.LIST,
    method: 'GET',
    data: query
  })
}
