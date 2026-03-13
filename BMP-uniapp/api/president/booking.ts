/**
 * 会长端 - 预约管理 API
 * 取消预约：后端为 PUT /booking/status?id=xx&status=0（0=已取消）
 */
import { get, put } from '@/utils/request'

export interface BookingItem {
  id: number
  venueId?: number
  venueName?: string
  courtId?: number
  courtName?: string
  userId?: number
  userName?: string
  bookingDate?: string
  startTime?: string
  endTime?: string
  status?: number
  amount?: number
  createTime?: string
  [key: string]: unknown
}

export interface BookingListResult {
  data: BookingItem[]
  total: number
  page?: number
  size?: number
  pages?: number
}

export function getBookingList(params?: {
  venueId?: number
  status?: number
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}) {
  return get<BookingListResult>('/booking/list', params || {})
}

export function getBookingDetail(id: number) {
  return get<BookingItem>(`/booking/info/${id}`)
}

/** 取消预约：将状态更新为 0（已取消） */
export function cancelBooking(id: number) {
  return put<unknown>(`/booking/status?id=${id}&status=0`, {})
}
