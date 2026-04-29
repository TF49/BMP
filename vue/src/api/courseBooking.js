import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 课程预约列表
export function getCourseBookingList(params) {
  return request({
    url: '/api/course/booking/list',
    method: 'get',
    params
  })
}

// 教练端：我的课程预约明细（当前教练所教课程的预约）
export function getBookingsForCoach(params) {
  return request({
    url: '/api/course/booking/for-coach',
    method: 'get',
    params
  })
}

// 教练端：预约详情
export function getBookingDetailForCoach(id) {
  return request({
    url: `/api/course/booking/for-coach/${id}`,
    method: 'get'
  })
}

// 教练端：更新预约状态
export function updateBookingStatusForCoach(data) {
  return request({
    url: '/api/course/booking/for-coach/status',
    method: 'put',
    data
  })
}

// 预约详情
export function getCourseBookingInfo(id) {
  return request({
    url: `/api/course/booking/info/${id}`,
    method: 'get'
  })
}

// 新增预约
export function addCourseBooking(data) {
  return request({
    url: '/api/course/booking/add',
    method: 'post',
    data
  })
}

// 更新预约
export function updateCourseBooking(data) {
  return request({
    url: '/api/course/booking/update',
    method: 'put',
    data
  })
}

// 删除预约
export function deleteCourseBooking(id) {
  return request({
    url: `/api/course/booking/${id}`,
    method: 'delete'
  })
}

// 更新状态
export function updateCourseBookingStatus(id, status) {
  return request({
    url: '/api/course/booking/status',
    method: 'put',
    params: { id, status }
  })
}

// 统计信息
export function getCourseBookingStatistics() {
  return withDedupe('GET:/api/course/booking/statistics', () =>
    request({
      url: '/api/course/booking/statistics',
      method: 'get'
    })
  )
}

// 下拉：课程
export function getCourseBookingCourses() {
  return request({
    url: '/api/course/booking/courses',
    method: 'get'
  })
}

// 下拉：会员
export function getCourseBookingMembers(keyword) {
  return request({
    url: '/api/course/booking/members',
    method: 'get',
    params: { keyword }
  })
}

// 支付（与器材租借、场地预约一致）
export function payCourseBooking(params) {
  return request({
    url: '/api/course/booking/payment',
    method: 'post',
    params
  })
}

// 普通用户支付本人课程预约
export function payMemberCourseBooking(params) {
  return request({
    url: '/api/course/booking/member/payment',
    method: 'post',
    params
  })
}

// 退款（与器材租借、场地预约一致）
export function refundCourseBooking(params) {
  return request({
    url: '/api/course/booking/refund',
    method: 'post',
    params
  })
}
