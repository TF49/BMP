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
  status: number
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
  return request<{
    data: VenueItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.VENUE.LIST,
    method: 'GET',
    data: params
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
