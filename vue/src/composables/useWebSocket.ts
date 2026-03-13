/**
 * WebSocket (STOMP over SockJS) 封装
 * - 管理端：订阅 /topic/admin/todo、/topic/admin/dashboard
 * - 用户端：订阅 /user/queue/notifications
 * - 生产环境：若前后端分离部署，需在 Nginx 等反向代理中配置 /ws 转发到后端
 */
import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '@/utils/auth'

const WS_BASE = (typeof window !== 'undefined' && window.location?.origin) ? window.location.origin : ''
const WS_PATH = '/ws'

/** 订单状态推送 payload */
export interface OrderStatusPayload {
  type: string
  orderType: string
  orderId: number
  status: number
  statusText: string
  title?: string
}

/** 待办推送 payload */
export interface TodoUpdatePayload {
  type: string
  count: Record<string, number>
  list?: Array<{ type: string; id: number; title?: string; bookingNo?: string }>
}

/** 看板刷新 payload */
export interface DashboardRefreshPayload {
  type: string
}

let stompClient: Client | null = null
const connected = ref(false)
const dashboardRefreshTrigger = ref(0)
const todoCount = ref<Record<string, number>>({})
const todoList = ref<any[]>([])
const onOrderStatusCallback = ref<((payload: OrderStatusPayload) => void) | null>(null)

function getWsUrl(): string {
  return (typeof window !== 'undefined' && window.location?.origin) ? window.location.origin + WS_PATH : ''
}

function createClient(token: string, isAdmin: boolean): Client {
  const client = new Client({
    webSocketFactory: () => new SockJS(getWsUrl()) as any,
    connectHeaders: {
      token: token,
      Authorization: 'Bearer ' + token
    },
    reconnectDelay: 3000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      connected.value = true
      if (isAdmin) {
        client.subscribe('/topic/admin/todo', (msg) => {
          try {
            const body: TodoUpdatePayload = JSON.parse(msg.body)
            if (body.count) todoCount.value = body.count
            if (body.list) todoList.value = body.list
          } catch (e) {
            console.warn('WebSocket todo 解析失败', e)
          }
        })
        client.subscribe('/topic/admin/dashboard', (msg) => {
          try {
            const body: DashboardRefreshPayload = JSON.parse(msg.body)
            if (body.type === 'DASHBOARD_REFRESH') {
              dashboardRefreshTrigger.value += 1
              if (typeof window !== 'undefined') {
                window.dispatchEvent(new CustomEvent('bmp-dashboard-refresh'))
              }
            }
          } catch (e) {
            console.warn('WebSocket dashboard 解析失败', e)
          }
        })
      } else {
        client.subscribe('/user/queue/notifications', (msg) => {
          try {
            const body: OrderStatusPayload = JSON.parse(msg.body)
            if (body.type === 'ORDER_STATUS' && onOrderStatusCallback.value) {
              onOrderStatusCallback.value(body)
            }
          } catch (e) {
            console.warn('WebSocket 订单状态解析失败', e)
          }
        })
      }
    },
    onStompError: (frame) => {
      const msg = (frame.headers?.message || frame.body || '') as string
      console.warn('STOMP error', msg)
      if (/token|无效|未提供|Unauthorized/i.test(msg)) {
        try { client.deactivate() } catch (_) { /* ignore */ }
        stompClient = null
        connected.value = false
      }
    },
    onWebSocketClose: () => {
      connected.value = false
    }
  })
  return client
}

/**
 * 初始化 WebSocket（管理端：isAdmin=true；用户端：isAdmin=false）
 * 返回：connected, dashboardRefreshTrigger, todoCount, todoList, setOnOrderStatus, disconnect
 */
export function useWebSocket(isAdmin: boolean) {
  function connect() {
    const token = getToken()
    if (!token) return
    if (stompClient?.connected) return
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
    stompClient = createClient(token, isAdmin)
    stompClient.activate()
  }

  function disconnect() {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
    connected.value = false
  }

  function setOnOrderStatus(cb: (payload: OrderStatusPayload) => void) {
    onOrderStatusCallback.value = cb
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    dashboardRefreshTrigger,
    todoCount,
    todoList,
    setOnOrderStatus,
    connect,
    disconnect
  }
}
