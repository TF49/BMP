import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface VenueItem {
  id: number
  venueName: string
  address: string
  contactPhone: string
  contactPerson: string
  businessHours: string
  description: string
  // 后端 venue 主图（会以相对路径形式返回，如 /api/uploads/venues/xxx.png）
  // 小程序端需要拼接 IMAGE_BASE_URL 后才能展示。
  venueImage?: string
  status: number
  hourlyPrice?: number
  createTime: string
  updateTime: string
}

export interface VenueImage {
  id: number
  venueId: number
  imageUrl: string
  sortOrder: number
}

export interface VenueSchedule {
  id: number
  venueId: number
  scheduleType: string
  startTime: string
  endTime: string
  isActive: number
}

/**
 * 获取场馆列表
 */
export function getVenueList(params?: {
  venueName?: string
  status?: number
  page?: number
  size?: number
}) {
  // 过滤掉 undefined / null 的参数，避免后端收到 "undefined" 字符串
  const cleanParams = params
    ? Object.fromEntries(
        Object.entries(params).filter(([, value]) => value !== undefined && value !== null)
      )
    : undefined

  return request<{
    data: VenueItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.VENUE.LIST,
    method: 'GET',
    data: cleanParams
  })
}

/**
 * 获取场馆详情
 */
export function getVenueDetail(id: number) {
  return request<VenueItem>({
    url: `${API_PATHS.VENUE.DETAIL}/${id}`,
    method: 'GET'
  })
}

/**
 * 获取场馆图片列表
 */
export function getVenueImages(venueId: number) {
  return request<VenueImage[]>({
    url: `${API_PATHS.VENUE.IMAGES}/${venueId}/images`,
    method: 'GET'
  })
}

/**
 * 获取场馆营业时间
 */
export function getVenueSchedules(venueId: number) {
  return request<VenueSchedule[]>({
    url: `${API_PATHS.VENUE.SCHEDULES}/${venueId}/schedules`,
    method: 'GET'
  })
}
