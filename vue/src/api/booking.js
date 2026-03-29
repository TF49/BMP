import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 场地预约列表
export function getBookingList(params) {
  return request({
    url: '/api/booking/list',
    method: 'get',
    params
  })
}

// 预约详情
export function getBookingInfo(id) {
  return request({
    url: `/api/booking/info/${id}`,
    method: 'get'
  })
}

// 新增预约
export function addBooking(data) {
  return request({
    url: '/api/booking/add',
    method: 'post',
    data
  })
}

// 更新预约
export function updateBooking(data) {
  return request({
    url: '/api/booking/update',
    method: 'put',
    data
  })
}

// 删除预约
export function deleteBooking(id) {
  return request({
    url: `/api/booking/${id}`,
    method: 'delete'
  })
}

// 更新预约状态（取消等）
export function updateBookingStatus(id, status) {
  return request({
    url: '/api/booking/status',
    method: 'put',
    params: { id, status }
  })
}

// 预约统计（Dashboard 与多图并发去重）
export function getBookingStatistics() {
  return withDedupe('GET:/api/booking/statistics', () =>
    request({
      url: '/api/booking/statistics',
      method: 'get'
    })
  )
}

// 预订趋势（week/month）（多图同 period 并发去重）
export function getBookingTrend(params) {
  const p = params || {}
  return withDedupe(`GET:/api/booking/trend:${p.period ?? ''}`, () =>
    request({
      url: '/api/booking/trend',
      method: 'get',
      params
    })
  )
}

// 预订热力图（period=week|month）
export function getBookingHeatmap(params) {
  const p = params || {}
  return withDedupe(`GET:/api/booking/heatmap:${p.period ?? ''}`, () =>
    request({
      url: '/api/booking/heatmap',
      method: 'get',
      params
    })
  )
}

// 各场馆预订趋势（Dashboard）
export function getBookingVenueTrend(params) {
  return withDedupe('GET:/api/booking/venue-trend', () =>
    request({
      url: '/api/booking/venue-trend',
      method: 'get',
      params
    })
  )
}

// 运营待办统计（多卡片并发去重）
export function getOperationTodoStatistics() {
  return withDedupe('GET:/api/booking/operation-todo', () =>
    request({
      url: '/api/booking/operation-todo',
      method: 'get'
    })
  )
}

// 按时间段统计当前场地预约人数
export function getBookingCount(params) {
  return request({
    url: '/api/booking/count',
    method: 'get',
    params
  })
}

// 获取指定场地当前时刻的占用情况（人数 + 脱敏姓名列表）
export function getBookingCurrentOccupancy(params) {
  return request({
    url: '/api/booking/occupancy/current',
    method: 'get',
    params
  })
}

// 获取指定场地在给定日期和时间段内的占用情况（人数 + 脱敏姓名列表）
export function getBookingRangeOccupancy(params) {
  return request({
    url: '/api/booking/occupancy/range',
    method: 'get',
    params
  })
}

// 场馆下拉
export function getBookingVenues() {
  return request({
    url: '/api/booking/venues',
    method: 'get'
  })
}

// 场地下拉
export function getBookingCourts(venueId) {
  return request({
    url: '/api/booking/courts',
    method: 'get',
    params: { venueId }
  })
}

// 会员下拉
export function getBookingMembers(keyword) {
  return request({
    url: '/api/booking/members',
    method: 'get',
    params: { keyword }
  })
}

// 支付
export function payBooking(params) {
  return request({
    url: '/api/booking/payment',
    method: 'post',
    params
  })
}

// 退款
export function refundBooking(params) {
  return request({
    url: '/api/booking/refund',
    method: 'post',
    params
  })
}
