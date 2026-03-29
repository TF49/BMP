import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 租借记录列表
export function getEquipmentRentalList(params) {
  return request({
    url: '/api/equipment/rental/list',
    method: 'get',
    params
  })
}

// 租借详情
export function getEquipmentRentalInfo(id) {
  return request({
    url: `/api/equipment/rental/info/${id}`,
    method: 'get'
  })
}

// 新增租借
export function addEquipmentRental(data) {
  return request({
    url: '/api/equipment/rental/add',
    method: 'post',
    data
  })
}

// 更新租借
export function updateEquipmentRental(data) {
  return request({
    url: '/api/equipment/rental/update',
    method: 'put',
    data
  })
}

// 删除租借
export function deleteEquipmentRental(id) {
  return request({
    url: `/api/equipment/rental/${id}`,
    method: 'delete'
  })
}

// 更新租借状态（归还/取消/逾期）
export function updateEquipmentRentalStatus(id, status) {
  return request({
    url: '/api/equipment/rental/status',
    method: 'put',
    params: { id, status }
  })
}

// 支付（管理员或用户为自己的订单支付，支付成功后扣款/记消费）
export function processEquipmentRentalPayment(rentalId, paymentMethod) {
  return request({
    url: '/api/equipment/rental/payment',
    method: 'post',
    params: { rentalId, paymentMethod }
  })
}

// 统计信息
export function getEquipmentRentalStatistics() {
  return withDedupe('GET:/api/equipment/rental/statistics', () =>
    request({
      url: '/api/equipment/rental/statistics',
      method: 'get'
    })
  )
}

// 下拉：器材
export function getEquipmentRentalOptions() {
  return request({
    url: '/api/equipment/rental/equipments',
    method: 'get'
  })
}

// 下拉：会员（按姓名/手机号关键字模糊查询）
export function getEquipmentRentalMembers(keyword) {
  return request({
    url: '/api/equipment/rental/members',
    method: 'get',
    params: { keyword }
  })
}
