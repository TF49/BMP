import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 赛事列表
export function getTournamentList(params) {
  return request({
    url: '/api/tournament/list',
    method: 'get',
    params
  })
}

// 赛事详情
export function getTournamentInfo(id) {
  return request({
    url: `/api/tournament/info/${id}`,
    method: 'get'
  })
}

// 新增赛事
export function addTournament(data) {
  return request({
    url: '/api/tournament/add',
    method: 'post',
    data
  })
}

// 更新赛事
export function updateTournament(data) {
  return request({
    url: '/api/tournament/update',
    method: 'put',
    data
  })
}

// 删除赛事
export function deleteTournament(id) {
  return request({
    url: `/api/tournament/${id}`,
    method: 'delete'
  })
}

// 更新状态
export function updateTournamentStatus(id, status) {
  return request({
    url: '/api/tournament/status',
    method: 'put',
    params: { id, status }
  })
}

// 统计信息
export function getTournamentStatistics() {
  return withDedupe('GET:/api/tournament/statistics', () =>
    request({
      url: '/api/tournament/statistics',
      method: 'get'
    })
  )
}

// 场馆下拉
export function getTournamentVenues() {
  return request({
    url: '/api/tournament/venues',
    method: 'get'
  })
}
