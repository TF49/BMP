import request from '@/utils/request'

// 搜索场馆
export function searchVenues(params) {
  return request({
    url: '/api/search/venues',
    method: 'get',
    params
  })
}

// 搜索课程
export function searchCourses(params) {
  return request({
    url: '/api/search/courses',
    method: 'get',
    params
  })
}

// 搜索赛事
export function searchTournaments(params) {
  return request({
    url: '/api/search/tournaments',
    method: 'get',
    params
  })
}

// 搜索器材
export function searchEquipment(params) {
  return request({
    url: '/api/search/equipment',
    method: 'get',
    params
  })
}

// 综合搜索
export function searchAll(params) {
  return request({
    url: '/api/search/all',
    method: 'get',
    params
  })
}
