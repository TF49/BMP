import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  buildCoachWebSocketUrl,
  createCoachStudentEventController,
  createCoachWebSocketConnectionManager,
  createUniStompSocket,
  shouldConnectCoachWebSocket
} from '@/utils/coachWebSocket'
import { createCoachStudentRefreshBus } from '@/utils/coachStudentRealtime'
import type {
  CoachStudentEventPriority,
  CoachStudentEventType,
  CoachStudentWebSocketEvent
} from '@/types/coachWebSocket'

describe('coach websocket transport', () => {
  it('builds native stomp websocket urls from development and production api bases', () => {
    expect(buildCoachWebSocketUrl('http://127.0.0.1:9090/api')).toBe('ws://127.0.0.1:9090/ws-native')
    expect(buildCoachWebSocketUrl('https://api.badminton.com')).toBe('wss://api.badminton.com/ws-native')
    expect(buildCoachWebSocketUrl('https://api.example.com/api/')).toBe('wss://api.example.com/ws-native')
  })

  it('connects only authenticated coach accounts', () => {
    expect(shouldConnectCoachWebSocket('jwt', 'COACH')).toBe(true)
    expect(shouldConnectCoachWebSocket('jwt', 'coach')).toBe(true)
    expect(shouldConnectCoachWebSocket('', 'COACH')).toBe(false)
    expect(shouldConnectCoachWebSocket('jwt', 'MEMBER')).toBe(false)
  })

  it('adapts uni SocketTask events and frames to the stomp websocket contract', () => {
    const callbacks: Record<string, (value: any) => void> = {}
    const task = {
      send: vi.fn(),
      close: vi.fn(),
      onOpen: vi.fn((callback) => { callbacks.open = callback }),
      onMessage: vi.fn((callback) => { callbacks.message = callback }),
      onError: vi.fn((callback) => { callbacks.error = callback }),
      onClose: vi.fn((callback) => { callbacks.close = callback })
    }
    const connectSocket = vi.fn(() => task)
    const socket = createUniStompSocket('wss://api.example.com/ws-native', connectSocket as any)
    const onOpen = vi.fn()
    const onMessage = vi.fn()
    socket.onopen = onOpen
    socket.onmessage = onMessage

    callbacks.open({})
    callbacks.message({ data: 'CONNECTED\n\n\u0000' })
    socket.send('SUBSCRIBE\n\n\u0000')
    socket.close(1000, 'done')

    expect(connectSocket).toHaveBeenCalledWith(expect.objectContaining({
      url: 'wss://api.example.com/ws-native',
      protocols: expect.arrayContaining(['v12.stomp'])
    }))
    expect(onOpen).toHaveBeenCalledTimes(1)
    expect(onMessage).toHaveBeenCalledWith(expect.objectContaining({ data: 'CONNECTED\n\n\u0000' }))
    expect(task.send).toHaveBeenCalledWith({ data: 'SUBSCRIBE\n\n\u0000' })
    expect(task.close).toHaveBeenCalledWith({ code: 1000, reason: 'done' })
  })

  it('lets only the latest async connection request activate a client', async () => {
    let releaseFirstDisconnect!: () => void
    const firstDisconnect = new Promise<void>(resolve => { releaseFirstDisconnect = resolve })
    const existingClient = { active: true, activate: vi.fn(), deactivate: vi.fn(() => firstDisconnect) }
    const staleClient = { active: false, activate: vi.fn(), deactivate: vi.fn(async () => {}) }
    const freshClient = { active: false, activate: vi.fn(), deactivate: vi.fn(async () => {}) }
    const createClient = vi.fn((token: string) => {
      if (token === 'existing-token') return existingClient
      return token === 'old-token' ? staleClient : freshClient
    })
    const manager = createCoachWebSocketConnectionManager(createClient)

    await manager.syncConnection('existing-token', 'COACH')
    const oldRequest = manager.syncConnection('old-token', 'COACH')
    const freshRequest = manager.syncConnection('fresh-token', 'COACH')
    releaseFirstDisconnect()
    await Promise.all([oldRequest, freshRequest])

    expect(staleClient.activate).not.toHaveBeenCalled()
    expect(freshClient.activate).toHaveBeenCalledTimes(1)
    expect(manager.getActiveToken()).toBe('fresh-token')
  })
})

describe('coach student websocket message handling', () => {
  let now = 1_000_000
  const refresh = vi.fn()
  const alert = vi.fn()
  const vibrate = vi.fn()

  beforeEach(() => {
    now = 1_000_000
    refresh.mockReset()
    alert.mockReset()
    vibrate.mockReset()
  })

  it('ignores malformed and unrelated messages without throwing', () => {
    const controller = createCoachStudentEventController({ now: () => now, refresh, alert, vibrate })

    expect(controller.handleRawMessage('{bad json')).toBe(false)
    expect(controller.handleRawMessage(JSON.stringify({ type: 'ORDER_STATUS' }))).toBe(false)
    expect(refresh).not.toHaveBeenCalled()
  })

  it('increments new booking badge and refreshes relevant pages', () => {
    const controller = createCoachStudentEventController({ now: () => now, refresh, alert, vibrate })
    const body = JSON.stringify(event('COACH_STUDENT_NEW_BOOKING'))

    expect(controller.handleRawMessage(body)).toBe(true)
    expect(controller.newBookingCount.value).toBe(1)
    expect(refresh).toHaveBeenCalledTimes(1)

    controller.clearNewBookingCount()
    expect(controller.newBookingCount.value).toBe(0)
  })

  it('throttles cancellation alerts for one minute but never suppresses refreshes', () => {
    const controller = createCoachStudentEventController({ now: () => now, refresh, alert, vibrate })
    const body = JSON.stringify(event('COACH_STUDENT_CANCELLED', 'HIGH'))

    controller.handleRawMessage(body)
    now += 30_000
    controller.handleRawMessage(body)
    now += 30_001
    controller.handleRawMessage(body)

    expect(refresh).toHaveBeenCalledTimes(3)
    expect(alert).toHaveBeenCalledTimes(2)
    expect(vibrate).toHaveBeenCalledTimes(2)
  })

  it('throttles cancellation alerts independently for each coach account', () => {
    const controller = createCoachStudentEventController({ now: () => now, refresh, alert, vibrate })
    const firstCoach = event('COACH_STUDENT_CANCELLED', 'HIGH')
    const secondCoach = { ...firstCoach, coachId: 31 }

    controller.handleRawMessage(JSON.stringify(firstCoach))
    controller.handleRawMessage(JSON.stringify(secondCoach))

    expect(alert).toHaveBeenCalledTimes(2)
    expect(vibrate).toHaveBeenCalledTimes(2)
  })

  it('refreshes attendance changes without interrupting the coach', () => {
    const controller = createCoachStudentEventController({ now: () => now, refresh, alert, vibrate })

    controller.handleRawMessage(JSON.stringify(event('COACH_STUDENT_ATTENDANCE_CHANGED')))

    expect(refresh).toHaveBeenCalledTimes(1)
    expect(alert).not.toHaveBeenCalled()
    expect(vibrate).not.toHaveBeenCalled()
  })
})

describe('coach student refresh bus', () => {
  it('merges rapid events into one refresh after 200ms and unsubscribes cleanly', () => {
    vi.useFakeTimers()
    const bus = createCoachStudentRefreshBus(200)
    const listener = vi.fn()
    const unsubscribe = bus.subscribe(listener)

    bus.emit(event('COACH_STUDENT_NEW_BOOKING'))
    bus.emit(event('COACH_STUDENT_ATTENDANCE_CHANGED'))
    vi.advanceTimersByTime(199)
    expect(listener).not.toHaveBeenCalled()
    vi.advanceTimersByTime(1)
    expect(listener).toHaveBeenCalledTimes(1)
    expect(listener).toHaveBeenCalledWith(expect.objectContaining({
      type: 'COACH_STUDENT_ATTENDANCE_CHANGED'
    }))

    unsubscribe()
    bus.emit(event('COACH_STUDENT_NEW_BOOKING'))
    vi.advanceTimersByTime(200)
    expect(listener).toHaveBeenCalledTimes(1)
    bus.dispose()
    vi.useRealTimers()
  })
})

function event(
  type: CoachStudentEventType,
  priority: CoachStudentEventPriority = 'NORMAL'
): CoachStudentWebSocketEvent {
  return {
    type,
    priority,
    coachId: 30,
    memberId: 50,
    bookingId: 10,
    courseId: 20,
    courseName: '进阶训练',
    memberName: '张学员',
    occurredAt: '2026-07-15T12:00:00'
  }
}
