import { Client, type IMessage } from '@stomp/stompjs'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { config } from '@/config/env'
import {
  buildCoachWebSocketUrl,
  createCoachStudentEventController,
  createCoachWebSocketConnectionManager,
  createUniStompSocket,
  shouldConnectCoachWebSocket
} from '@/utils/coachWebSocket'
import { emitCoachStudentRefresh } from '@/utils/coachStudentRealtime'

export const useCoachWebSocketStore = defineStore('coachWebSocket', () => {
  const connected = ref(false)
  const newBookingCount = ref(0)

  const eventController = createCoachStudentEventController({
    now: () => Date.now(),
    newBookingCount,
    refresh: emitCoachStudentRefresh,
    vibrate: () => {
      try {
        uni.vibrateShort({ type: 'light' })
      } catch {
        // 非真机环境可能不支持震动，忽略即可。
      }
    },
    alert: event => {
      void uni.showModal({
        title: '学员取消预约',
        content: `${event.memberName || '学员'}取消了「${event.courseName || '课程'}」预约`,
        showCancel: false,
        confirmText: '知道了'
      })
    }
  })

  const connectionManager = createCoachWebSocketConnectionManager((token: string) => {
    const websocketUrl = buildCoachWebSocketUrl(config.BASE_URL)
    let nextClient: Client
    nextClient = new Client({
      connectHeaders: {
        token,
        Authorization: `Bearer ${token}`
      },
      webSocketFactory: () => createUniStompSocket(websocketUrl),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        if (!connectionManager.isCurrent(nextClient)) return
        connected.value = true
        nextClient.subscribe('/user/queue/notifications', handleMessage)
      },
      onDisconnect: () => {
        if (!connectionManager.isCurrent(nextClient)) return
        connected.value = false
      },
      onWebSocketClose: () => {
        if (!connectionManager.isCurrent(nextClient)) return
        connected.value = false
      },
      onStompError: frame => {
        console.warn('教练 WebSocket STOMP 错误:', frame.headers.message || frame.body)
      },
      onWebSocketError: error => {
        console.warn('教练 WebSocket 连接错误:', error)
      }
    })
    return nextClient
  })

  async function syncConnection(token: string, role: string) {
    if (!shouldConnectCoachWebSocket(token, role)) connected.value = false
    await connectionManager.syncConnection(token, role)
  }

  function handleMessage(message: IMessage) {
    eventController.handleRawMessage(message.body)
  }

  async function disconnect() {
    connected.value = false
    await connectionManager.disconnect()
  }

  return {
    connected,
    newBookingCount,
    syncConnection,
    disconnect,
    clearNewBookingCount: eventController.clearNewBookingCount,
    handleRawMessage: eventController.handleRawMessage
  }
})
