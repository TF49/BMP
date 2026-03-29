import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 最近活动（Dashboard）
export function getRecentActivities(params) {
  const p = params || {}
  const lim = p.limit != null ? String(p.limit) : ''
  return withDedupe(`GET:/api/activity/recent:${lim}`, () =>
    request({
      url: '/api/activity/recent',
      method: 'get',
      params
    })
  )
}

