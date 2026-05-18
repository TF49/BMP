import { request } from '@/utils/request'

export interface PaymentAutoCancelConfig {
  enabled?: boolean
  timeoutMinutes?: number
  timeoutSeconds?: number
  serverTime?: string
}

export function getPaymentAutoCancelConfig() {
  return request<PaymentAutoCancelConfig>({
    url: '/api/payment/auto-cancel/config',
    method: 'GET'
  })
}
