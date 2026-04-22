import { get, post } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface SearchParams {
  keyword: string
  page?: number
  size?: number
}

export interface SearchResult {
  id: number
  name: string
  [key: string]: any
}

/**
 * 搜索场馆
 */
export function searchVenues(params: SearchParams) {
  return get<{ data: SearchResult[], total: number }>(API_PATHS.SEARCH.VENUES, params)
}

/**
 * 搜索课程
 */
export function searchCourses(params: SearchParams) {
  return get<{ data: SearchResult[], total: number }>(API_PATHS.SEARCH.COURSES, params)
}

/**
 * 搜索赛事
 */
export function searchTournaments(params: SearchParams) {
  return get<{ data: SearchResult[], total: number }>(API_PATHS.SEARCH.TOURNAMENTS, params)
}

/**
 * 搜索器材
 */
export function searchEquipment(params: SearchParams) {
  return get<{ data: SearchResult[], total: number }>(API_PATHS.SEARCH.EQUIPMENT, params)
}

/**
 * 获取搜索建议
 */
export function getSearchSuggestions(params: { keyword: string }) {
  return get<string[]>(API_PATHS.SEARCH.SUGGESTIONS, params)
}
