import { onMounted, onUnmounted } from 'vue'
import { subscribeCoachStudentRefresh } from '@/utils/coachStudentRealtime'

export function useCoachStudentRealtimeRefresh(refresh: () => void | Promise<void>) {
  let unsubscribe: (() => void) | null = null

  onMounted(() => {
    unsubscribe = subscribeCoachStudentRefresh(() => {
      void refresh()
    })
  })

  onUnmounted(() => {
    unsubscribe?.()
    unsubscribe = null
  })
}
