import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

// 财务列表
export function getFinanceList(params) {
  return request({
    url: '/api/finance/list',
    method: 'get',
    params
  })
}

// 财务详情
export function getFinanceInfo(id) {
  return request({
    url: `/api/finance/info/${id}`,
    method: 'get'
  })
}

// 新增财务记录
export function addFinance(data) {
  return request({
    url: '/api/finance/add',
    method: 'post',
    data
  })
}

// 更新财务记录
export function updateFinance(data) {
  return request({
    url: '/api/finance/update',
    method: 'put',
    data
  })
}

// 删除财务记录
export function deleteFinance(id) {
  return request({
    url: `/api/finance/${id}`,
    method: 'delete'
  })
}

function financeStatisticsKey(params) {
  const p = params || {}
  return `GET:/api/finance/statistics:${p.startDate ?? ''}:${p.endDate ?? ''}`
}

// 统计概览（Dashboard 与 RevenueGauge 等并发去重）
export function getFinanceStatistics(params) {
  return withDedupe(financeStatisticsKey(params), () =>
    request({
      url: '/api/finance/statistics',
      method: 'get',
      params
    })
  )
}

function financeTrendKey(params) {
  const p = params || {}
  return `GET:/api/finance/trend:${p.startDate ?? ''}:${p.endDate ?? ''}`
}

// 趋势数据（多图同区间去重）
export function getFinanceTrend(params) {
  return withDedupe(financeTrendKey(params), () =>
    request({
      url: '/api/finance/trend',
      method: 'get',
      params
    })
  )
}

// 各场馆收入趋势（Dashboard）
export function getFinanceVenueTrend(params) {
  return withDedupe('GET:/api/finance/venue-trend', () =>
    request({
      url: '/api/finance/venue-trend',
      method: 'get',
      params
    })
  )
}

// 业务占比
export function getFinanceRatio(params) {
  const p = params || {}
  return withDedupe(`GET:/api/finance/ratio:${p.startDate ?? ''}:${p.endDate ?? ''}`, () =>
    request({
      url: '/api/finance/ratio',
      method: 'get',
      params
    })
  )
}

// 场馆下拉（仅会长可用）
export function getFinanceVenues() {
  return request({
    url: '/api/finance/venues',
    method: 'get'
  })
}

// 业务类型选项
export function getBusinessTypes() {
  return request({
    url: '/api/finance/businessTypes',
    method: 'get'
  })
}

// ========== 财务审计日志相关接口 ==========

// 分页查询审计日志
export function getAuditLogList(params) {
  return request({
    url: '/api/finance/audit/page',
    method: 'get',
    params
  })
}

// 查询审计日志详情
export function getAuditLogDetail(id) {
  return request({
    url: `/api/finance/audit/${id}`,
    method: 'get'
  })
}

// 创建导出任务
export function createAuditLogExport(params) {
  return request({
    url: '/api/finance/audit/export/create',
    method: 'post',
    params
  })
}

// 查询导出任务状态
export function getExportTaskStatus(taskId) {
  return request({
    url: `/api/finance/audit/export/status/${taskId}`,
    method: 'get'
  })
}

// 下载导出文件
export function downloadAuditLogExport(taskId) {
  return request({
    url: `/api/finance/audit/export/download/${taskId}`,
    method: 'get',
    responseType: 'blob'
  }).then(response => {
    // 如果响应是blob类型，直接返回
    if (response instanceof Blob) {
      return response
    }
    // 否则尝试从response.data获取blob
    return response.data || response
  })
}
