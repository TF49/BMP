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
  packagePricePerHour?: number
  sharedPricePerHour?: number
  sharedPricePerTime?: number
  enablePackageHour?: boolean
  enableSharedHour?: boolean
  enableSharedTime?: boolean
  status: number
  venueName?: string
  createTime?: string
  updateTime?: string
}

export interface CourtPayload {
  id?: number
  courtCode: string
  courtName: string
  venueId: number
  billingType: 'HOUR' | 'TIME'
  pricePerHour: number
  packagePricePerHour?: number
  sharedPricePerHour?: number
  sharedPricePerTime?: number
  enablePackageHour?: boolean
  enableSharedHour?: boolean
  enableSharedTime?: boolean
  status?: number
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

export function getCourtDetail(id: number) {
  return request<CourtItem>({
    url: `${API_PATHS.COURT.DETAIL}/${id}`,
    method: 'GET'
  })
}

export function addCourt(data: CourtPayload) {
  return request<{ id: number }>({
    url: API_PATHS.COURT.ADD,
    method: 'POST',
    data
  })
}

export function updateCourt(data: CourtPayload) {
  return request<null>({
    url: API_PATHS.COURT.UPDATE,
    method: 'PUT',
    data
  })
}

export function deleteCourt(id: number) {
  return request<null>({
    url: `${API_PATHS.COURT.DELETE}/${id}`,
    method: 'DELETE'
  })
}

export function updateCourtStatus(id: number, status: number) {
  return request<null>({
    url: API_PATHS.COURT.STATUS,
    method: 'PUT',
    data: { id, status }
  })
}

export function getCourtVenueOptions() {
  return request<Array<{ id: number; venueName: string; status?: number }>>({
    url: API_PATHS.COURT.VENUES,
    method: 'GET'
  })
}

export function getTodayCourtBookings(id: number, date?: string) {
  return request<Array<{ bookingUserName?: string; bookingTime?: string }>>({
    url: `${API_PATHS.COURT.TODAY_BOOKINGS}/${id}/bookings/today`,
    method: 'GET',
    data: date ? { date } : {}
  })
}
