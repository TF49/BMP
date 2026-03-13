import request from '@/utils/request'

// 器材列表
export function getEquipmentList(params) {
  return request({
    url: '/api/equipment/list',
    method: 'get',
    params
  })
}

// 器材详情
export function getEquipmentInfo(id) {
  return request({
    url: `/api/equipment/info/${id}`,
    method: 'get'
  })
}

// 新增器材
export function addEquipment(data) {
  return request({
    url: '/api/equipment/add',
    method: 'post',
    data
  })
}

// 更新器材
export function updateEquipment(data) {
  return request({
    url: '/api/equipment/update',
    method: 'put',
    data
  })
}

// 删除器材
export function deleteEquipment(id) {
  return request({
    url: `/api/equipment/${id}`,
    method: 'delete'
  })
}

// 更新状态
export function updateEquipmentStatus(id, status) {
  return request({
    url: '/api/equipment/status',
    method: 'put',
    params: { id, status }
  })
}

// 器材统计
export function getEquipmentStatistics() {
  return request({
    url: '/api/equipment/statistics',
    method: 'get'
  })
}

// 器材类型下拉
export function getEquipmentTypes() {
  return request({
    url: '/api/equipment/types',
    method: 'get'
  })
}

// 场馆下拉（新增器材时选择所属场馆）
export function getEquipmentVenues() {
  return request({
    url: '/api/equipment/venues',
    method: 'get'
  })
}

// 库存预警列表
export function getEquipmentLowStock(params) {
  return request({
    url: '/api/equipment/low-stock',
    method: 'get',
    params
  })
}
