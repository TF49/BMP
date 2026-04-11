import { get } from '@/utils/request'

export interface PresidentDashboardSummary {
  member?: Record<string, unknown> | null
  booking?: Record<string, unknown> | null
  court?: Record<string, unknown> | null
  finance?: Record<string, unknown> | null
}

export function getPresidentDashboardSummary(params?: {
  startDate?: string
  endDate?: string
}) {
  return get<PresidentDashboardSummary>('/dashboard/summary', params || {})
}
