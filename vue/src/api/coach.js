import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 教练列表
export function getCoachList(params) {
  return request({
    url: '/api/coach/list',
    method: 'get',
    params
  })
}

// 教练详情
export function getCoachInfo(id) {
  return request({
    url: `/api/coach/info/${id}`,
    method: 'get'
  })
}

// 新增教练
export function addCoach(data) {
  return request({
    url: '/api/coach/add',
    method: 'post',
    data
  })
}

// 更新教练
export function updateCoach(data) {
  return request({
    url: '/api/coach/update',
    method: 'put',
    data
  })
}

// 删除教练
export function deleteCoach(id) {
  return request({
    url: `/api/coach/${id}`,
    method: 'delete'
  })
}

// 更新状态
export function updateCoachStatus(id, status) {
  return request({
    url: '/api/coach/status',
    method: 'put',
    params: { id, status }
  })
}

// 统计信息
export function getCoachStatistics() {
  return withDedupe('GET:/api/coach/statistics', () =>
    request({
      url: '/api/coach/statistics',
      method: 'get'
    })
  )
}

// 场馆下拉（新增教练时会长需选择所属场馆）
export function getVenueList() {
  return request({
    url: '/api/coach/venues',
    method: 'get'
  })
}

// 教练端：当前教练信息（COACH 角色且已绑定）
export function getCurrentCoach() {
  return request({
    url: '/api/coach/me',
    method: 'get'
  })
}

// 教练端：更新本人档案（仅可编辑姓名、电话、专长、经验、头像）
export function updateCurrentCoach(data) {
  return request({
    url: '/api/coach/me',
    method: 'put',
    data
  })
}

// 管理端：未绑定教练档案的 COACH 用户列表（关联账号下拉）
export function getUnboundCoachUsers() {
  return request({
    url: '/api/coach/unbound-users',
    method: 'get'
  })
}
