import { get, post } from '@/utils/request'
import { API_PATHS } from '@/config/api'

export interface NotificationItem {
  id: number
  title: string
  content: string
  publisherId: number
  publisherName?: string
  venueId?: number | null
  createTime?: string
}

export interface NotificationListResult {
  data: NotificationItem[]
  total: number
  page: number
  size: number
  pages: number
}

export function getNotificationList(params?: { page?: number; size?: number }) {
  return get<NotificationListResult>(API_PATHS.NOTIFICATION.LIST, params || {})
}

export function getNotificationDetail(id: number) {
  return get<NotificationItem>(`${API_PATHS.NOTIFICATION.DETAIL}/${id}`)
}

export function createNotification(payload: Pick<NotificationItem, 'title' | 'content' | 'publisherId' | 'publisherName' | 'venueId'>) {
  return post<{ id: number }>(API_PATHS.NOTIFICATION.LIST, payload)
}

