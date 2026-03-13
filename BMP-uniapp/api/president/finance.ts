/**
 * 会长端 - 财务管理 API
 */
import { get, del } from '@/utils/request'

export interface FinanceItem {
  id: number
  venueId?: number
  venueName?: string
  businessType?: string
  incomeExpenseType?: string
  amount?: number
  paymentMethod?: string
  operatorName?: string
  remark?: string
  createTime?: string
  [key: string]: unknown
}

export interface FinanceListResult {
  data: FinanceItem[]
  total: number
  page?: number
  size?: number
  pages?: number
}

export function getFinanceList(params?: {
  venueId?: number
  businessType?: string
  incomeExpenseType?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}) {
  return get<FinanceListResult>('/finance/list', params || {})
}

export function getFinanceInfo(id: number) {
  return get<FinanceItem>(`/finance/info/${id}`)
}

export function deleteFinance(id: number) {
  return del<unknown>(`/finance/${id}`)
}

/** 全面对账（仅会长） */
export function reconciliation() {
  return get<Record<string, unknown>>('/finance/reconciliation/full')
}
