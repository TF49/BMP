import { ref } from 'vue'
import { getNotificationList } from '@/api/notification'

const STORAGE_KEY = 'bmp_notification_popup_date'

function getTodayString(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/**
 * 每天首次进入系统时弹出通知
 * 需在「站内消息」开关开启时才会执行
 * @returns { popupVisible, notifications, loading, closePopup, checkAndShow }
 */
export function useNotificationPopup() {
  const popupVisible = ref(false)
  const notifications = ref<any[]>([])
  const loading = ref(false)

  const closePopup = () => {
    popupVisible.value = false
    try {
      localStorage.setItem(STORAGE_KEY, getTodayString())
    } catch (e) {
      console.warn('保存通知弹窗日期失败:', e)
    }
  }

  const checkAndShow = async () => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY)
      const today = getTodayString()
      if (stored === today) {
        return
      }

      loading.value = true
      const res = await getNotificationList({ page: 1, size: 50 })
      const list = res?.data?.data || []
      loading.value = false

      if (list.length > 0) {
        notifications.value = list
        popupVisible.value = true
      } else {
        try {
          localStorage.setItem(STORAGE_KEY, getTodayString())
        } catch (e) {
          console.warn('保存通知弹窗日期失败:', e)
        }
      }
    } catch (e) {
      console.error('获取通知失败:', e)
      loading.value = false
    }
  }

  /** 点击顶部铃铛时打开通知弹窗（与每日首次弹窗同款样式） */
  const openFromBell = async () => {
    loading.value = true
    popupVisible.value = true
    try {
      const res = await getNotificationList({ page: 1, size: 50 })
      const list = res?.data?.data || []
      notifications.value = list
    } catch (e) {
      console.error('获取通知失败:', e)
      notifications.value = []
    } finally {
      loading.value = false
    }
  }

  return {
    popupVisible,
    notifications,
    loading,
    closePopup,
    checkAndShow,
    openFromBell
  }
}
