import { request } from '@/utils/request'

export interface PaymentAutoCancelConfig {
  enabled?: boolean
  timeoutMinutes?: number
  timeoutSeconds?: number
  serverTime?: string
}

export function getPaymentAutoCancelConfig(options: { suppressErrorToast?: boolean } = {}) {
  return request<PaymentAutoCancelConfig>({
    url: '/payment/auto-cancel/config',
    method: 'GET',
    suppressErrorToast: options.suppressErrorToast
  })
}
