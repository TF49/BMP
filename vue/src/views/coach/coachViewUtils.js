export const COURSE_STATUS_TEXT_MAP = {
  0: '已取消',
  1: '报名中',
  2: '进行中',
  3: '已结束'
}

export const COURSE_STATUS_TYPE_MAP = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: ''
}

export const BOOKING_STATUS_TEXT_MAP = {
  0: '已取消',
  1: '待支付',
  2: '已支付',
  3: '进行中',
  4: '已完成'
}

export const BOOKING_STATUS_TYPE_MAP = {
  0: 'info',
  1: 'warning',
  2: 'success',
  3: 'primary',
  4: ''
}

export const MEMBER_STATUS_TEXT_MAP = {
  0: '停用',
  1: '正常'
}

export const MEMBER_TYPE_TEXT_MAP = {
  REGULAR: '普通会员',
  VIP: 'VIP会员'
}

export const WEEK_LABELS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

export const formatStatusText = (status, map) => map[status] ?? '未知'

export const formatStatusType = (status, map) => map[status] ?? 'info'

export const formatTimeValue = (value) => {
  if (!value) return ''
  return String(value).slice(0, 5)
}

export const formatCourseTime = (row) => {
  if (!row) return '-'
  const dateText = row.courseDate || '-'
  const start = formatTimeValue(row.startTime || row.courseStartTime)
  const end = formatTimeValue(row.endTime || row.courseEndTime)
  if (!start && !end) return dateText
  return `${dateText} ${start || '--:--'} - ${end || '--:--'}`
}

export const formatTimeRange = (row) => {
  const start = formatTimeValue(row?.startTime || row?.courseStartTime)
  const end = formatTimeValue(row?.endTime || row?.courseEndTime)
  return `${start || '--:--'} - ${end || '--:--'}`
}

export const formatStudentCount = (row) => `${row?.currentStudents ?? 0} / ${row?.maxStudents ?? 0}`

export const formatAmount = (value) => {
  const amount = Number(value ?? 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

export const formatDateKey = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export const parseDate = (value) => {
  if (!value) return null
  const [year, month, day] = String(value).split('-').map(Number)
  if (!year || !month || !day) return null
  return new Date(year, month - 1, day)
}

export const addDays = (dateValue, offset) => {
  const date = parseDate(dateValue)
  if (!date) return dateValue
  date.setDate(date.getDate() + offset)
  return formatDateKey(date)
}

export const getWeekStart = (dateValue) => {
  const date = parseDate(dateValue)
  if (!date) return dateValue
  const day = date.getDay()
  const diff = day === 0 ? -6 : 1 - day
  date.setDate(date.getDate() + diff)
  return formatDateKey(date)
}

export const getWeekdayLabel = (dateValue) => {
  const date = parseDate(dateValue)
  if (!date) return '未知'
  return WEEK_LABELS[date.getDay()]
}

export const formatGroupDate = (dateValue) => {
  if (!dateValue) return '-'
  const date = parseDate(dateValue)
  if (!date) return dateValue
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day} ${WEEK_LABELS[date.getDay()]}`
}

export const compareCourseTime = (leftRow, rightRow) => {
  const left = `${leftRow?.courseDate || ''} ${formatTimeValue(leftRow?.startTime || leftRow?.courseStartTime)}`
  const right = `${rightRow?.courseDate || ''} ${formatTimeValue(rightRow?.startTime || rightRow?.courseStartTime)}`
  return left.localeCompare(right)
}

export const toAbsoluteAssetUrl = (url) => {
  if (!url) return ''
  if (String(url).startsWith('http')) return url
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return base ? `${base.replace(/\/$/, '')}${url}` : url
}

export const isTodayCourseOngoing = (course, now = new Date()) => {
  return Number(course?.status) === 2 && course?.courseDate === formatDateKey(now)
}

export const isUpcomingCourse = (course, now = new Date()) => {
  if (!course || course.courseDate !== formatDateKey(now)) return false
  if (Number(course.status) === 0 || Number(course.status) === 3) return false
  const startText = formatTimeValue(course.startTime || course.courseStartTime)
  if (!startText) return false
  const [hours, minutes] = startText.split(':').map(Number)
  const startDate = new Date(now)
  startDate.setHours(hours || 0, minutes || 0, 0, 0)
  return startDate.getTime() > now.getTime()
}

export const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${mi}:${s}`
}

export const COACH_UNBOUND_MESSAGE = '未绑定教练档案，请联系管理员在教练管理中关联账号'
export const COACH_PROFILE_UPDATED_EVENT = 'coach-profile-updated'

export const isCoachUnboundError = (error) => {
  const message = typeof error === 'string'
    ? error
    : (error?.message || error?.response?.data?.message || '')
  return String(message).includes(COACH_UNBOUND_MESSAGE)
}

export const emitCoachProfileUpdated = (payload) => {
  if (typeof window === 'undefined') return
  window.dispatchEvent(new CustomEvent(COACH_PROFILE_UPDATED_EVENT, { detail: payload || null }))
}
