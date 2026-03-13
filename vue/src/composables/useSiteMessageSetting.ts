import { ref } from 'vue'
import { getSettings } from '@/api/login'

/**
 * 站内消息开关 - 控制小铃铛通知的显示
 * 与系统设置中的「站内消息」开关联动
 */
const siteMessageEnabled = ref(true)

export function useSiteMessageSetting() {
  const loadFromApi = async () => {
    try {
      const res = (await getSettings()) as { code?: number; data?: { siteMessage?: boolean } }
      if (res?.code === 200 && res?.data) {
        const v = res.data.siteMessage
        siteMessageEnabled.value = v !== false
      }
    } catch (e) {
      console.warn('加载站内消息设置失败，默认开启:', e)
      siteMessageEnabled.value = true
    }
  }

  const setValue = (value: boolean) => {
    siteMessageEnabled.value = value
  }

  return {
    siteMessageEnabled,
    loadFromApi,
    setValue
  }
}
