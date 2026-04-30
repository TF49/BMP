import type { CoachBookingItem, CoachCourseItem } from '@/api/coachSelf'

export const COURSE_STATUS_TEXT_MAP: Record<number, string> = {
  0: '已取消',
  1: '报名中',
  2: '进行中',
  3: '已结束'
}

export const COURSE_STATUS_CLASS_MAP: Record<number, string> = {
  0: 'neutral',
  1: 'warning',
  2: 'success',
  3: 'neutral'
}

export const BOOKING_STATUS_TEXT_MAP: Record<number, string> = {
  0: '已取消',
  1: '待支付',
  2: '已支付',
  3: '进行中',
  4: '已完成'
}

export const BOOKING_STATUS_CLASS_MAP: Record<number, string> = {
  0: 'neutral',
  1: 'warning',
  2: 'warning',
  3: 'success',
  4: 'neutral'
}

export const PAYMENT_STATUS_TEXT_MAP: Record<number, string> = {
  0: '未支付',
  1: '已支付',
  2: '已退款'
}

export function normalizeTime(value?: string) {
  return String(value || '--:--').slice(0, 5)
}

export function formatDateKey(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function formatCoachCourseTime(row: Pick<CoachCourseItem, 'startTime' | 'endTime'>) {
  return `${normalizeTime(row.startTime)} - ${normalizeTime(row.endTime)}`
}

export function formatCoachBookingTime(
  row: Pick<CoachBookingItem, 'startTime' | 'endTime' | 'courseStartTime' | 'courseEndTime'>
) {
  return `${normalizeTime(row.startTime || row.courseStartTime)} - ${normalizeTime(row.endTime || row.courseEndTime)}`
}

export function formatCoachCourseStatus(status?: number) {
  return COURSE_STATUS_TEXT_MAP[Number(status ?? 1)] || '未知状态'
}

export function formatCoachCourseStatusClass(status?: number) {
  return COURSE_STATUS_CLASS_MAP[Number(status ?? 1)] || 'neutral'
}

export function formatCoachBookingStatus(status?: number) {
  return BOOKING_STATUS_TEXT_MAP[Number(status ?? 1)] || '未知状态'
}

export function formatCoachBookingStatusClass(status?: number) {
  return BOOKING_STATUS_CLASS_MAP[Number(status ?? 1)] || 'neutral'
}

export function formatCoachPaymentStatus(status?: number) {
  return PAYMENT_STATUS_TEXT_MAP[Number(status ?? 0)] || '未知'
}

export function compareCoachCourseTime(
  left: Pick<CoachCourseItem, 'courseDate' | 'startTime'>,
  right: Pick<CoachCourseItem, 'courseDate' | 'startTime'>
) {
  const leftKey = `${left.courseDate || ''} ${normalizeTime(left.startTime)}`
  const rightKey = `${right.courseDate || ''} ${normalizeTime(right.startTime)}`
  return leftKey.localeCompare(rightKey)
}

export function formatStudentCount(item: Pick<CoachCourseItem, 'currentStudents' | 'maxStudents'>) {
  return `${Number(item.currentStudents || 0)}/${Number(item.maxStudents || 0)}`
}

export function formatStudentText(item: Pick<CoachCourseItem, 'currentStudents' | 'maxStudents'>) {
  const text = formatStudentCount(item)
  return `${text} 名学员`
}

export function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

export function isCoachCourseFinished(
  item: Pick<CoachCourseItem, 'courseDate' | 'endTime' | 'status'>,
  now: Date = new Date()
) {
  if (Number(item.status ?? -1) === 3) return true
  if (!item.courseDate) return false
  const endTime = normalizeTime(item.endTime)
  const endAt = new Date(`${item.courseDate} ${endTime}:00`.replace(/-/g, '/'))
  return Number.isFinite(endAt.getTime()) && endAt.getTime() < now.getTime()
}

export function canStartBooking(item: Pick<CoachBookingItem, 'status'>) {
  return Number(item.status ?? -1) === 2
}

export function canCompleteBooking(item: Pick<CoachBookingItem, 'status'>) {
  return Number(item.status ?? -1) === 3
}

export function canCancelBooking(item: Pick<CoachBookingItem, 'status'>) {
  const status = Number(item.status ?? -1)
  return status === 2 || status === 3
}

export function buildCoachBookingsUrl(courseId?: number, courseName?: string, status?: number) {
  const query: string[] = []
  if (courseId) query.push(`courseId=${courseId}`)
  if (courseName) query.push(`courseName=${encodeURIComponent(courseName)}`)
  if (status !== undefined) query.push(`status=${status}`)
  return `/pages/coach/bookings${query.length ? `?${query.join('&')}` : ''}`
}

export function buildCoachStudentsUrl(courseId?: number, courseName?: string) {
  const query: string[] = []
  if (courseId) query.push(`courseId=${courseId}`)
  if (courseName) query.push(`courseName=${encodeURIComponent(courseName)}`)
  return `/pages/coach/students${query.length ? `?${query.join('&')}` : ''}`
}
