import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 报名列表
export function getTournamentRegistrationList(params) {
  return request({
    url: '/api/tournament/registration/list',
    method: 'get',
    params
  })
}

// 报名详情
export function getTournamentRegistrationInfo(id) {
  return request({
    url: `/api/tournament/registration/info/${id}`,
    method: 'get'
  })
}

// 新增报名
export function addTournamentRegistration(data) {
  return request({
    url: '/api/tournament/registration/add',
    method: 'post',
    data
  })
}

// 更新报名
export function updateTournamentRegistration(data) {
  return request({
    url: '/api/tournament/registration/update',
    method: 'put',
    data
  })
}

// 删除报名
export function deleteTournamentRegistration(id) {
  return request({
    url: `/api/tournament/registration/${id}`,
    method: 'delete'
  })
}

// 更新状态
export function updateTournamentRegistrationStatus(id, status) {
  return request({
    url: '/api/tournament/registration/status',
    method: 'put',
    params: { id, status }
  })
}

// 统计信息
export function getTournamentRegistrationStatistics() {
  return withDedupe('GET:/api/tournament/registration/statistics', () =>
    request({
      url: '/api/tournament/registration/statistics',
      method: 'get'
    })
  )
}

// 下拉：赛事
export function getTournamentRegistrationTournaments() {
  return request({
    url: '/api/tournament/registration/tournaments',
    method: 'get'
  })
}

// 下拉：会员
export function getTournamentRegistrationMembers(keyword) {
  return request({
    url: '/api/tournament/registration/members',
    method: 'get',
    params: { keyword }
  })
}

// 处理支付
export function processTournamentRegistrationPayment(registrationId, paymentMethod) {
  return request({
    url: '/api/tournament/registration/payment',
    method: 'post',
    params: { registrationId, paymentMethod }
  })
}

// 处理退款
export function processTournamentRegistrationRefund(registrationId) {
  return request({
    url: '/api/tournament/registration/refund',
    method: 'post',
    params: { registrationId }
  })
}
