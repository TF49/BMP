import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

function summaryKey(params) {
  const p = params || {}
  return `GET:/api/dashboard/summary:${p.startDate ?? ''}:${p.endDate ?? ''}`
}

/**
 * 聚合：member / booking / court / finance（无财务权限时 finance 为 null）
 * 相同日期参数并发只发一次请求。
 */
export function getDashboardSummary(params) {
  return withDedupe(summaryKey(params), () =>
    request({
      url: '/api/dashboard/summary',
      method: 'get',
      params
    })
  )
}
