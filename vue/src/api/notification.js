import request from '@/utils/request'

/**
 * 分页查询通知列表
 * @param {object} params { page, size }
 */
export function getNotificationList(params) {
  return request({
    url: '/api/notifications',
    method: 'get',
    params
  })
}

/**
 * 发布通知
 * @param {object} data { title, content }
 */
export function createNotification(data) {
  return request({
    url: '/api/notifications',
    method: 'post',
    data
  })
}
