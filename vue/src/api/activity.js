import request from '@/utils/request'

// 最近活动（Dashboard）
export function getRecentActivities(params) {
  return request({
    url: '/api/activity/recent',
    method: 'get',
    params
  })
}

