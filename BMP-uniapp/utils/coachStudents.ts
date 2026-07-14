import type {
  CoachStudentAttendanceItem,
  CoachStudentScheduleItem
} from '@/types/coachStudents'

export const DEFAULT_COACH_STUDENT_AVATAR = '/static/placeholders/avatar.svg'

export function buildCoachStudentListUrl(query?: { keyword?: string; riskOnly?: boolean }): string {
  const params: string[] = []
  if (query?.keyword?.trim()) params.push(`keyword=${encodeURIComponent(query.keyword.trim())}`)
  if (query?.riskOnly) params.push('riskOnly=true')
  return `/pages/coach/student-list${params.length ? `?${params.join('&')}` : ''}`
}

export function parseCoachStudentListRoute(options?: Record<string, string | undefined>) {
  return {
    keyword: safeDecodeURIComponent(options?.keyword || '').trim(),
    riskOnly: options?.riskOnly === 'true'
  }
}

export function buildCoachStudentDetailUrl(memberId: number): string {
  return `/pages/coach/student-detail?id=${memberId}`
}

export function maskPhone(phone?: string, showFull = false): string {
  if (!phone?.trim()) return '未填写'
  const normalized = phone.trim()
  if (showFull) return normalized
  return /^1\d{10}$/.test(normalized)
    ? `${normalized.slice(0, 3)}****${normalized.slice(7)}`
    : normalized
}

export function getStudentAvatarWithFallback(avatar?: string): string {
  return avatar?.trim() || DEFAULT_COACH_STUDENT_AVATAR
}

export function partitionCoachStudentSchedules(items: CoachStudentScheduleItem[]) {
  const upcoming = items
    .filter(item => [1, 2, 3].includes(item.bookingStatus) && ![2, 3].includes(item.attendanceStatus))
    .sort((left, right) => scheduleTimeKey(left, 'startTime').localeCompare(scheduleTimeKey(right, 'startTime')))
  const history = items
    .filter(item => [0, 4].includes(item.bookingStatus) || [2, 3].includes(item.attendanceStatus))
    .sort((left, right) => scheduleTimeKey(right, 'endTime').localeCompare(scheduleTimeKey(left, 'endTime')))

  return { upcoming, history }
}

export interface CoachStudentMonthlyAttendance {
  monthKey: string
  monthStr: string
  completed: number
  absent: number
  attendanceRate: number
  items: CoachStudentAttendanceItem[]
}

export function groupCoachStudentAttendanceByMonth(
  items: CoachStudentAttendanceItem[]
): CoachStudentMonthlyAttendance[] {
  const groups = new Map<string, CoachStudentAttendanceItem[]>()

  items.forEach(item => {
    const monthKey = /^\d{4}-\d{2}/.test(item.courseDate || '') ? item.courseDate.slice(0, 7) : '其他'
    const monthItems = groups.get(monthKey) || []
    monthItems.push(item)
    groups.set(monthKey, monthItems)
  })

  return [...groups.entries()]
    .sort(([left], [right]) => {
      if (left === '其他') return 1
      if (right === '其他') return -1
      return right.localeCompare(left)
    })
    .map(([monthKey, monthItems]) => {
      const sortedItems = [...monthItems].sort((left, right) => attendanceTimeKey(right).localeCompare(attendanceTimeKey(left)))
      const completed = sortedItems.filter(item => item.attendanceStatus === 2).length
      const absent = sortedItems.filter(item => item.attendanceStatus === 3).length
      const denominator = completed + absent
      const monthStr = monthKey === '其他'
        ? monthKey
        : `${monthKey.slice(0, 4)}年${monthKey.slice(5, 7)}月`

      return {
        monthKey,
        monthStr,
        completed,
        absent,
        attendanceRate: denominator ? Math.round((completed * 100) / denominator) : 0,
        items: sortedItems
      }
    })
}

function scheduleTimeKey(
  item: CoachStudentScheduleItem,
  timeField: 'startTime' | 'endTime'
) {
  return `${item.courseDate || ''} ${item[timeField] || ''}`
}

function attendanceTimeKey(item: CoachStudentAttendanceItem) {
  return `${item.courseDate || ''} ${item.startTime || ''}`
}

function safeDecodeURIComponent(value: string) {
  try {
    return decodeURIComponent(value)
  } catch {
    return value
  }
}
