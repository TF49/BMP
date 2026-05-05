import { get, post, put } from '@/utils/request'

export interface BookingItem {
  id: number
  bookingNo?: string
  memberId?: number
  memberName?: string
  courtId?: number
  courtName?: string
  courtIds?: number[]
  courtNames?: string[]
  courtCount?: number
  primaryCourtName?: string
  venueId?: number
  venueName?: string
  bookingMode?: 'SHARED' | 'PACKAGE'
  pricingMode?: 'PACKAGE_HOUR' | 'SHARED_HOUR' | 'SHARED_TIME'
  pricingModeSummary?: string
  bookingDate?: string
  startTime?: string
  endTime?: string
  orderAmount?: number
  amount?: number
  paymentMethod?: string
  paymentStatus?: number
  status?: number
  createTime?: string
  updateTime?: string
  [key: string]: unknown
}

export interface BookingListResult {
  data: BookingItem[]
  total: number
  page?: number
  size?: number
  pages?: number
}

export interface BookingStatistics {
  total?: number
  cancelled?: number
  pending?: number
  paid?: number
  ongoing?: number
  finished?: number
  todayBookings?: number
  bookingTrend?: number
  peakHours?: string
  [key: string]: unknown
}

export interface BookingPayload {
  memberId: number
  courtId?: number
  courtIds?: number[]
  bookingMode?: 'SHARED' | 'PACKAGE'
  pricingMode?: 'PACKAGE_HOUR' | 'SHARED_HOUR' | 'SHARED_TIME'
  bookingDate: string
  startTime: string
  endTime: string
  orderAmount?: number
  paymentMethod?: string
  remark?: string
}

export interface BookingOccupancyUser {
  bookingId?: number
  memberName?: string
  memberType?: string
  memberLevel?: number
  startTime?: string
  endTime?: string
  status?: number
  statusText?: string
}

export interface BookingOccupancyResult {
  count: number
  users: BookingOccupancyUser[]
}

export interface SharedBookingCountResult {
  count: number
}

export function getBookingList(params?: {
  venueId?: number
  memberId?: number
  courtId?: number
  status?: number
  memberKeyword?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) {
  const cleanParams = params
    ? Object.fromEntries(
        Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
      )
    : {}

  return get<BookingListResult>('/booking/list', cleanParams)
}

export function getBookingDetail(id: number) {
  return get<BookingItem>(`/booking/info/${id}`)
}

export function getBookingStatistics() {
  return get<BookingStatistics>('/booking/statistics')
}

export function cancelBooking(id: number) {
  return put<unknown>(`/booking/status?id=${id}&status=0`, {})
}

export function createBooking(data: BookingPayload) {
  return post<{ id: number; bookingNo?: string }>('/booking/add', data)
}

export function getBookingRangeOccupancy(params: {
  courtId: number
  bookingDate: string
  startTime: string
  endTime: string
}) {
  return get<BookingOccupancyResult>('/booking/occupancy/range', params)
}

export function getBookingCount(params: {
  courtId: number
  bookingDate: string
  startTime: string
  endTime: string
}) {
  return get<SharedBookingCountResult>('/booking/count', params)
}
