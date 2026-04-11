import { get } from '@/utils/request'

export interface FinanceAuditLogItem {
  id: number
  financeId?: number | null
  financeNo?: string | null
  operationType?: string | null
  operator?: string | null
  operatorId?: number | null
  operationTime?: string | null
  beforeData?: string | null
  afterData?: string | null
  changeSummary?: string | null
  ipAddress?: string | null
  userAgent?: string | null
  remark?: string | null
}

export interface FinanceAuditPageResult {
  records?: FinanceAuditLogItem[]
  total?: number
  size?: number
  current?: number
  pages?: number
}

export function getFinanceAuditPage(params?: {
  pageNum?: number
  pageSize?: number
  financeId?: number
  financeNo?: string
  operationType?: string
  operator?: string
  startTime?: string
  endTime?: string
}) {
  return get<FinanceAuditPageResult>('/finance/audit/page', params || {})
}

export function getFinanceAuditDetail(id: number) {
  return get<FinanceAuditLogItem>(`/finance/audit/${id}`)
}
