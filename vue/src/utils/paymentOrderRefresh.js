import { onBeforeUnmount, onMounted } from 'vue'

/**
 * 订单状态 WebSocket / 管理端看板刷新 → 业务列表联动
 */

export const PAYMENT_ORDER_TYPES = {
  booking: 'booking',
  courseBooking: 'courseBooking',
  equipmentRental: 'equipmentRental',
  tournamentRegistration: 'tournamentRegistration',
  stringingService: 'stringingService'
}

const ORDER_STATUS_EVENT = 'bmp-order-status'
const ADMIN_ORDERS_REFRESH_EVENT = 'bmp-dashboard-refresh'

export function dispatchOrderStatusRefresh(payload) {
  if (typeof window === 'undefined' || !payload) return
  window.dispatchEvent(new CustomEvent(ORDER_STATUS_EVENT, { detail: payload }))
}

export function useOrderStatusRefreshListener(orderTypes, callback) {
  const types = (Array.isArray(orderTypes) ? orderTypes : [orderTypes]).filter(Boolean)

  const handler = (event) => {
    const detail = event?.detail
    if (!detail || !types.includes(detail.orderType)) return
    callback(detail)
  }

  onMounted(() => {
    window.addEventListener(ORDER_STATUS_EVENT, handler)
  })

  onBeforeUnmount(() => {
    window.removeEventListener(ORDER_STATUS_EVENT, handler)
  })
}

export function useAdminOrdersRefreshListener(callback) {
  const handler = () => {
    callback()
  }

  onMounted(() => {
    window.addEventListener(ADMIN_ORDERS_REFRESH_EVENT, handler)
  })

  onBeforeUnmount(() => {
    window.removeEventListener(ADMIN_ORDERS_REFRESH_EVENT, handler)
  })
}
