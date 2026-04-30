import { getCurrentCoach } from '@/api/coachSelf'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { safeReLaunch } from '@/utils/safeRoute'

export const COACH_ENTRY_PATH = '/pages/coach/entry'
export const COACH_HOME_PATH = '/pages/coach/index'
export const COACH_UNBOUND_PATH = '/pages/coach/unbound'
export const COACH_UNBOUND_MESSAGE = '未绑定教练档案，请联系管理员在教练管理中关联账号'

function getErrorMessage(error: unknown) {
  if (error instanceof Error) return error.message
  if (typeof error === 'string') return error
  return String((error as { message?: string })?.message || '')
}

export function isCoachUnboundError(error: unknown) {
  const message = getErrorMessage(error)
  return message.includes(COACH_UNBOUND_MESSAGE) || message.includes('未绑定教练档案')
}

export function resolveCoachAvatar(avatar?: string) {
  return resolveImageUrl(avatar || '/static/placeholders/avatar.svg')
}

export function formatDateKey(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export async function resolveCoachHomePath() {
  try {
    await getCurrentCoach()
    return COACH_HOME_PATH
  } catch (error) {
    if (isCoachUnboundError(error)) {
      return COACH_UNBOUND_PATH
    }
    throw error
  }
}

export async function navigateToCoachHome(showErrorToast = true) {
  try {
    const target = await resolveCoachHomePath()
    await safeReLaunch(target, COACH_HOME_PATH)
    return target
  } catch (error) {
    if (showErrorToast) {
      uni.showToast({
        title: '进入教练端失败，请重试',
        icon: 'none'
      })
    }
    throw error
  }
}
