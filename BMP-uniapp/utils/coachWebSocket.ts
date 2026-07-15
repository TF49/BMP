import type { IStompSocket } from '@stomp/stompjs'
import {
  COACH_STUDENT_EVENT_TYPES,
  type CoachStudentWebSocketEvent
} from '@/types/coachWebSocket'

type UniConnectSocket = (options: Record<string, any>) => any

interface MutableCount {
  value: number
}

interface EventControllerDependencies {
  now: () => number
  refresh: (event: CoachStudentWebSocketEvent) => void
  alert: (event: CoachStudentWebSocketEvent) => void
  vibrate: () => void
  newBookingCount?: MutableCount
}

export interface CoachWebSocketManagedClient {
  readonly active: boolean
  activate(): void
  deactivate(options?: { force?: boolean }): Promise<void>
}

export function buildCoachWebSocketUrl(apiBaseUrl: string) {
  const normalized = apiBaseUrl.trim().replace(/\/+$/, '').replace(/\/api$/i, '')
  return normalized.replace(/^http:/i, 'ws:').replace(/^https:/i, 'wss:') + '/ws-native'
}

export function shouldConnectCoachWebSocket(token: string, role: string) {
  return Boolean(token?.trim()) && role?.toUpperCase() === 'COACH'
}

export function createCoachWebSocketConnectionManager<T extends CoachWebSocketManagedClient>(
  createClient: (token: string) => T
) {
  let client: T | null = null
  let activeToken = ''
  let requestVersion = 0

  async function deactivateCurrent() {
    const current = client
    client = null
    activeToken = ''
    if (current) await current.deactivate({ force: true })
  }

  async function syncConnection(token: string, role: string) {
    const version = ++requestVersion
    if (!shouldConnectCoachWebSocket(token, role)) {
      await deactivateCurrent()
      return
    }
    if (client?.active && activeToken === token) return
    await deactivateCurrent()
    if (version !== requestVersion) return
    const nextClient = createClient(token)
    client = nextClient
    activeToken = token
    nextClient.activate()
  }

  async function disconnect() {
    requestVersion += 1
    await deactivateCurrent()
  }

  function isCurrent(candidate: T) {
    return client === candidate
  }

  return {
    syncConnection,
    disconnect,
    isCurrent,
    getActiveToken: () => activeToken
  }
}

export function createUniStompSocket(
  url: string,
  connectSocket: UniConnectSocket = options => uni.connectSocket(options as UniApp.ConnectSocketOption)
): IStompSocket & { close(code?: number, reason?: string): void } {
  let readyState = 0
  const socket: IStompSocket & { close(code?: number, reason?: string): void } = {
    url,
    binaryType: 'arraybuffer',
    onopen: null,
    onmessage: null,
    onerror: null,
    onclose: null,
    get readyState() {
      return readyState
    },
    send(data) {
      task.send({ data })
    },
    close(code = 1000, reason = '') {
      if (readyState === 2 || readyState === 3) return
      readyState = 2
      task.close({ code, reason })
    }
  }

  const task = connectSocket({
    url,
    protocols: ['v12.stomp', 'v11.stomp', 'v10.stomp']
  })
  task.onOpen((event: unknown) => {
    readyState = 1
    socket.onopen?.(event)
  })
  task.onMessage((event: { data: string | ArrayBuffer }) => {
    socket.onmessage?.({ data: event.data })
  })
  task.onError((event: unknown) => {
    socket.onerror?.(event)
  })
  task.onClose((event: unknown) => {
    readyState = 3
    socket.onclose?.(event)
  })
  return socket
}

export function createCoachStudentEventController(dependencies: EventControllerDependencies) {
  const newBookingCount = dependencies.newBookingCount || { value: 0 }
  const lastCancellationAlertAtByCoach = new Map<number, number>()

  function handleRawMessage(body: string) {
    const event = parseCoachStudentEvent(body)
    if (!event) return false

    if (event.type === 'COACH_STUDENT_NEW_BOOKING') {
      newBookingCount.value += 1
    } else if (event.type === 'COACH_STUDENT_CANCELLED') {
      const now = dependencies.now()
      const lastAlertAt = lastCancellationAlertAtByCoach.get(event.coachId) ?? Number.NEGATIVE_INFINITY
      if (now - lastAlertAt >= 60_000) {
        lastCancellationAlertAtByCoach.set(event.coachId, now)
        dependencies.vibrate()
        dependencies.alert(event)
      }
    }
    dependencies.refresh(event)
    return true
  }

  function clearNewBookingCount() {
    newBookingCount.value = 0
  }

  return { handleRawMessage, newBookingCount, clearNewBookingCount }
}

export function parseCoachStudentEvent(body: string): CoachStudentWebSocketEvent | null {
  try {
    const value = JSON.parse(body) as Record<string, unknown>
    if (!COACH_STUDENT_EVENT_TYPES.includes(value.type as any)) return null
    if (value.priority !== 'NORMAL' && value.priority !== 'HIGH') return null
    if (!isPositiveNumber(value.coachId) || !isPositiveNumber(value.memberId)
      || !isPositiveNumber(value.bookingId) || !isPositiveNumber(value.courseId)) return null
    if (typeof value.courseName !== 'string' || typeof value.memberName !== 'string'
      || typeof value.occurredAt !== 'string') return null
    return value as unknown as CoachStudentWebSocketEvent
  } catch {
    return null
  }
}

function isPositiveNumber(value: unknown): value is number {
  return typeof value === 'number' && Number.isFinite(value) && value > 0
}
