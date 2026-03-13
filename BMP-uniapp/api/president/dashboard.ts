/**
 * 会长端 - 数据看板 API
 */
import { get } from '@/utils/request'

const today = () => new Date().toISOString().split('T')[0]

/** 会员统计 */
export function getMemberStatistics() {
  return get<{ total?: number; newToday?: number; growthRate?: number }>('/member/statistics')
}

/** 预约统计 */
export function getBookingStatistics() {
  return get<{
    todayBookings?: number
    ongoing?: number
    finished?: number
    bookingTrend?: number
    peakHours?: string
  }>('/booking/statistics')
}

/** 财务统计（当日收入） */
export function getFinanceStatistics(params?: { startDate?: string; endDate?: string }) {
  const startDate = params?.startDate ?? today()
  const endDate = params?.endDate ?? today()
  return get<{ totalIncome?: number; incomeChange?: number }>('/finance/statistics', { startDate, endDate })
}

/** 场地统计 */
export function getCourtStatistics() {
  return get<{ total?: number; newToday?: number }>('/court/statistics')
}

/** 运营待办统计 */
export function getOperationTodo() {
  return get<{ pendingBookings?: number; unpaidOrders?: number; pendingIssues?: number }>('/booking/operation-todo')
}
