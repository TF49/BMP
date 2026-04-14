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
  keyword?: string
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

/** 财务统计概览 */
export function getFinanceStatistics(params?: {
  venueId?: number
  startDate?: string
  endDate?: string
}) {
  return get<{
    totalIncome: number
    totalExpense: number
    netIncome: number
    transactionCount: number
  }>('/finance/statistics', params || {})
}

/** 财务趋势数据 */
export function getFinanceTrend(params?: {
  venueId?: number
  startDate?: string
  endDate?: string
}) {
  return get<{
    dates: string[]
    incomes: number[]
    expenses: number[]
  }>('/finance/trend', params || {})
}

/** 业务占比数据 */
export function getBusinessRatio(params?: {
  venueId?: number
  startDate?: string
  endDate?: string
}) {
  return get<{
    labels: string[]
    values: number[]
  }>('/finance/business-ratio', params || {})
}
