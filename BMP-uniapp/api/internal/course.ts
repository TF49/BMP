import { request } from '@/utils/request'
import { API_PATHS } from '@/config/api'

export interface CourseItem {
  id: number
  courseName: string
  coachId: number
  coachName: string
  courtId: number
  courtName: string
  courseContent?: string
  coursePrice: number
  courseDuration: number
  courseDate: string
  startTime: string
  endTime: string
  maxStudents: number
  currentStudents: number
  status: number
  createTime: string
  level?: string
  location?: string
  venueName?: string
  description?: string
  coachInfo?: string
  coachRating?: number
  coachExperience?: string
  reviews?: any[]
}

export interface CoursePayload {
  id?: number
  courseName: string
  coachId: number
  courtId?: number
  coursePrice: number
  courseDuration: number
  courseDate: string
  startTime: string
  endTime: string
  maxStudents: number
  status?: number
  courseContent?: string
}

export interface CourseBookingParams {
  memberId: number
  courseId: number
  orderAmount: number
  paymentMethod: string
}

export interface CourseBookingItem {
  id: number
  bookingNo: string
  memberId: number
  memberName: string
  courseId: number
  courseName: string
  coachName?: string
  courseDate?: string
  courseStartTime?: string
  courseEndTime?: string
  orderAmount: number
  paymentMethod: string
  paymentStatus?: number
  status: number
  remark?: string
  createTime: string
  updateTime?: string
}

/**
 * 获取课程列表
 */
export function getCourseList(params?: {
  courseName?: string
  keyword?: string
  coachId?: number
  courtId?: number
  status?: number
  page?: number
  size?: number
}) {
  return request<{
    data: CourseItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.COURSE.LIST,
    method: 'GET',
    data: {
      ...params,
      keyword: params?.keyword ?? params?.courseName
    }
  })
}

/**
 * 获取课程详情
 */
export function getCourseDetail(id: number) {
  return request<CourseItem>({
    url: `${API_PATHS.COURSE.DETAIL}/${id}`,
    method: 'GET'
  })
}

/**
 * 新增课程（会长/场馆管理员）
 */
export function addCourse(data: CoursePayload) {
  return request<{ id: number }>({
    url: '/course/add',
    method: 'POST',
    data
  })
}

/**
 * 更新课程（会长/场馆管理员）
 */
export function updateCourse(data: CoursePayload) {
  return request<null>({
    url: '/course/update',
    method: 'PUT',
    data
  })
}

export function updateCourseStatus(id: number, status: number) {
  return request<null>({
    url: `${API_PATHS.COURSE.STATUS}?id=${id}&status=${status}`,
    method: 'PUT'
  })
}

/**
 * 课程预约列表
 */
export function getCourseBookingList(params?: {
  memberId?: number
  courseId?: number
  status?: number
  keyword?: string
  page?: number
  size?: number
}) {
  return request<{
    data: CourseBookingItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.COURSE.BOOKING.LIST,
    method: 'GET',
    data: params
  })
}

/**
 * 创建课程预约
 */
export function createCourseBooking(params: CourseBookingParams) {
  return request<CourseBookingItem>({
    url: API_PATHS.COURSE.BOOKING.ADD,
    method: 'POST',
    data: params
  })
}

/**
 * 获取课程预约详情
 */
export function getCourseBookingDetail(id: number) {
  return request<CourseBookingItem>({
    url: `${API_PATHS.COURSE.BOOKING.DETAIL}/${id}`,
    method: 'GET'
  })
}

export function updateCourseBookingStatus(id: number, status: number) {
  return request<null>({
    url: `/course/booking/status?id=${id}&status=${status}`,
    method: 'PUT'
  })
}

export function processCourseBookingPayment(
  bookingId: number,
  paymentMethod: 'CASH' | 'ALIPAY' | 'WECHAT' | 'BALANCE'
) {
  return request<null>({
    url: `/course/booking/payment?bookingId=${bookingId}&paymentMethod=${paymentMethod}`,
    method: 'POST'
  })
}

export function processCourseBookingRefund(bookingId: number) {
  return request<null>({
    url: `/course/booking/refund?bookingId=${bookingId}`,
    method: 'POST'
  })
}

export function deleteCourseBooking(id: number) {
  return request<null>({
    url: `/course/booking/${id}`,
    method: 'DELETE'
  })
}

/**
 * 获取教练列表（用于课程筛选）
 */
export function getCoachList(params?: {
  status?: number
  keyword?: string
  page?: number
  size?: number
}) {
  return request<any[]>({
    url: API_PATHS.COURSE.COACHES,
    method: 'GET',
    data: params
  })
}
