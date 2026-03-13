import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface BookingParams {
  memberId: number
  courtId: number
  bookingDate: string
  startTime: string
  endTime: string
  orderAmount: number
  paymentMethod: string
}

export interface BookingItem {
  id: number
  bookingNo: string
  memberId: number
  memberName: string
  courtId: number
  courtName: string
  venueId: number
  venueName: string
  bookingDate: string
  startTime: string
  endTime: string
  orderAmount: number
  paymentMethod: string
  status: number
  createTime: string
  updateTime: string
}

export interface CourtItem {
  id: number
  courtCode: string
  courtName: string
  venueId: number
  venueName: string
  billingType: string
  pricePerHour?: number
  pricePerTime?: number
  status: number
}

/**
 * 获取预约列表
 */
export function getBookingList(params?: {
  memberId?: number
  courtId?: number
  status?: number
  keyword?: string
  page?: number
  size?: number
}) {
  return request<{
    data: BookingItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.BOOKING.LIST,
    method: 'GET',
    data: params
  })
}

/**
 * 创建预约
 */
export function createBooking(params: BookingParams) {
  return request<BookingItem>({
    url: API_PATHS.BOOKING.ADD,
    method: 'POST',
    data: params
  })
}

/**
 * 获取预约详情
 */
export function getBookingDetail(id: number) {
  return request<BookingItem>({
    url: `${API_PATHS.BOOKING.DETAIL}/${id}`,
    method: 'GET'
  })
}

/**
 * 取消预约
 */
export function cancelBooking(id: number) {
  return request<string>({
    url: `${API_PATHS.BOOKING.DELETE}/${id}`,
    method: 'DELETE'
  })
}

/**
 * 更新预约状态
 */
export function updateBookingStatus(id: number, status: number) {
  return request<string>({
    url: API_PATHS.BOOKING.STATUS,
    method: 'PUT',
    data: { id, status }
  })
}

/**
 * 处理支付
 */
export function processPayment(bookingId: number, paymentMethod: string) {
  return request<string>({
    url: API_PATHS.BOOKING.PAYMENT,
    method: 'POST',
    data: { bookingId, paymentMethod }
  })
}

/**
 * 处理退款
 */
export function processRefund(bookingId: number) {
  return request<string>({
    url: API_PATHS.BOOKING.REFUND,
    method: 'POST',
    data: { bookingId }
  })
}

/**
 * 更新预约
 */
export function updateBooking(params: Partial<BookingParams> & { id: number }) {
  return request<BookingItem>({
    url: API_PATHS.BOOKING.UPDATE,
    method: 'PUT',
    data: params
  })
}

/**
 * 获取场地下拉列表
 */
export function getCourtList() {
  return request<CourtItem[]>({
    url: API_PATHS.BOOKING.COURTS,
    method: 'GET'
  })
}

/**
 * 获取场馆下拉列表
 */
export function getVenueList() {
  return request<Array<{
    id: number
    venueName: string
    venueCode: string
  }>>({
    url: API_PATHS.BOOKING.VENUES || '/booking/venues',
    method: 'GET'
  })
}
