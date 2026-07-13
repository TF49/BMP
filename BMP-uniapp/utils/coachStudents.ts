export const DEFAULT_COACH_STUDENT_AVATAR = '/static/placeholders/avatar.svg'

export function buildCoachStudentListUrl(): string {
  return '/pages/coach/student-list'
}

export function buildCoachStudentDetailUrl(memberId: number): string {
  return `/pages/coach/student-detail?id=${memberId}`
}

export function maskPhone(phone?: string): string {
  if (!phone?.trim()) return '未填写'
  const normalized = phone.trim()
  return /^1\d{10}$/.test(normalized)
    ? `${normalized.slice(0, 3)}****${normalized.slice(7)}`
    : normalized
}

export function getStudentAvatarWithFallback(avatar?: string): string {
  return avatar?.trim() || DEFAULT_COACH_STUDENT_AVATAR
}
