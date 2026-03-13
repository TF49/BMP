import request from '@/utils/request'

// 穿线服务列表
export function getStringingList(params) {
  return request({
    url: '/api/stringing/list',
    method: 'get',
    params
  })
}

// 穿线服务详情
export function getStringingInfo(id) {
  return request({
    url: `/api/stringing/info/${id}`,
    method: 'get'
  })
}

// 根据服务单号查询
export function getStringingInfoByNo(serviceNo) {
  return request({
    url: `/api/stringing/info/no/${serviceNo}`,
    method: 'get'
  })
}

// 新增穿线服务申请
export function addStringingService(data) {
  return request({
    url: '/api/stringing/add',
    method: 'post',
    data
  })
}

// 更新穿线服务
export function updateStringingService(data) {
  return request({
    url: '/api/stringing/update',
    method: 'put',
    data
  })
}

// 删除穿线服务
export function deleteStringingService(id) {
  return request({
    url: `/api/stringing/${id}`,
    method: 'delete'
  })
}

// 更新穿线服务状态
export function updateStringingStatus(id, status) {
  return request({
    url: '/api/stringing/status',
    method: 'put',
    params: { id, status }
  })
}

// 统计信息
export function getStringingStatistics() {
  return request({
    url: '/api/stringing/statistics',
    method: 'get'
  })
}

// 获取线材选项（从系统选择）
export function getStringOptions() {
  return request({
    url: '/api/stringing/strings',
    method: 'get'
  })
}

// 计算价格（用于前端预览）
export function calculateStringingPrice(stringId, isOwnString) {
  return request({
    url: '/api/stringing/calculate-price',
    method: 'get',
    params: { stringId, isOwnString }
  })
}

// 穿线服务支付（与器材租借/预约管理逻辑一致）
export function processStringingPayment(serviceId, paymentMethod) {
  return request({
    url: '/api/stringing/payment',
    method: 'post',
    params: { serviceId, paymentMethod }
  })
}

// 穿线服务退款
export function processStringingRefund(serviceId) {
  return request({
    url: '/api/stringing/refund',
    method: 'post',
    params: { serviceId }
  })
}
