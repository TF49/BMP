import type { CoachStudentWebSocketEvent } from '@/types/coachWebSocket'

type RefreshListener = (event: CoachStudentWebSocketEvent) => void

export function createCoachStudentRefreshBus(delay = 200) {
  const listeners = new Set<RefreshListener>()
  let timer: ReturnType<typeof setTimeout> | null = null
  let latestEvent: CoachStudentWebSocketEvent | null = null

  function emit(event: CoachStudentWebSocketEvent) {
    latestEvent = event
    if (timer !== null) clearTimeout(timer)
    timer = setTimeout(() => {
      timer = null
      const eventToSend = latestEvent
      latestEvent = null
      if (eventToSend) listeners.forEach(listener => listener(eventToSend))
    }, delay)
  }

  function subscribe(listener: RefreshListener) {
    listeners.add(listener)
    return () => listeners.delete(listener)
  }

  function dispose() {
    if (timer !== null) clearTimeout(timer)
    timer = null
    latestEvent = null
    listeners.clear()
  }

  return { emit, subscribe, dispose }
}

const coachStudentRefreshBus = createCoachStudentRefreshBus(200)

export const emitCoachStudentRefresh = coachStudentRefreshBus.emit
export const subscribeCoachStudentRefresh = coachStudentRefreshBus.subscribe
