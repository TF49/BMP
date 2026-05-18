import request from '@/utils/request'

export function getPaymentAutoCancelConfig() {
  return request({
    url: '/api/payment/auto-cancel/config',
    method: 'get'
  })
}
