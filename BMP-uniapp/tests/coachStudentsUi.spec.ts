import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'
import {
  DEFAULT_COACH_STUDENT_AVATAR,
  buildCoachStudentDetailUrl,
  buildCoachStudentListUrl,
  getStudentAvatarWithFallback,
  maskPhone
} from '@/utils/coachStudents'
import * as coachStudentUtils from '@/utils/coachStudents'
import * as coachViewUtils from '@/utils/coachView'
import { canCompleteBooking } from '@/utils/coachView'

describe('coach student routes and privacy helpers', () => {
  it('builds dedicated list and detail routes', () => {
    expect(buildCoachStudentListUrl()).toBe('/pages/coach/student-list')
    expect(buildCoachStudentListUrl({ keyword: '张 三', riskOnly: true })).toBe('/pages/coach/student-list?keyword=%E5%BC%A0%20%E4%B8%89&riskOnly=true')
    expect(buildCoachStudentDetailUrl(18)).toBe('/pages/coach/student-detail?id=18')
  })

  it('masks phone and reuses the single avatar placeholder', () => {
    expect(maskPhone('13812341001')).toBe('138****1001')
    expect(maskPhone('13812341001', true)).toBe('13812341001')
    expect(maskPhone('95123456')).toBe('95123456')
    expect(maskPhone()).toBe('未填写')
    expect(DEFAULT_COACH_STUDENT_AVATAR).toBe('/static/placeholders/avatar.svg')
    expect(getStudentAvatarWithFallback()).toBe(DEFAULT_COACH_STUDENT_AVATAR)
  })

  it('registers pages, keeps four-tab navigation, and adds dashboard/deep links', () => {
    const pages = readFileSync('pages.json', 'utf8')
    const dashboard = readFileSync('pages/coach/index.vue', 'utf8')
    const sessionStudents = readFileSync('pages/coach/students.vue', 'utf8')
    const tabBar = readFileSync('components/coach/CoachTabBar.vue', 'utf8')

    expect(pages).toContain('pages/coach/student-list')
    expect(pages).toContain('pages/coach/student-detail')
    expect(dashboard).toContain('我的学员')
    expect(dashboard).toContain("handleQuickAction('students')")
    expect(sessionStudents).toContain('buildCoachStudentDetailUrl')
    expect(sessionStudents).toContain('登记缺席')
    expect(sessionStudents).not.toContain('缺席/取消')
    expect((tabBar.match(/\{ key: '[^']+', label:/g) || [])).toHaveLength(4)
  })

  it('wires coach websocket lifecycle, badge and realtime page refresh without removing onShow fallbacks', () => {
    const app = readFileSync('App.vue', 'utf8')
    const dashboard = readFileSync('pages/coach/index.vue', 'utf8')
    const list = readFileSync('pages/coach/student-list.vue', 'utf8')
    const detail = readFileSync('pages/coach/student-detail.vue', 'utf8')
    const sessionStudents = readFileSync('pages/coach/students.vue', 'utf8')

    expect(app).toContain('useCoachWebSocketStore')
    expect(app).toContain('watch(')
    expect(app).toContain('appVisible')
    expect(app).toContain('syncConnection')
    expect(app).toContain('coachWebSocketStore.disconnect()')

    for (const source of [dashboard, list, detail, sessionStudents]) {
      expect(source).toContain('useCoachStudentRealtimeRefresh')
      expect(source).toContain('onShow')
    }
    expect(dashboard).toContain('newBookingCount')
    expect(dashboard).toContain('student-realtime-badge')
    expect(dashboard).toContain('clearNewBookingCount()')
  })

  it('uses attendance actions on the booking list and keeps cancellation separate', () => {
    const bookings = readFileSync('pages/coach/bookings.vue', 'utf8')

    expect(bookings).toContain('updateCoachBookingAttendance')
    expect(bookings).toContain("'CHECK_IN'")
    expect(bookings).toContain("'COMPLETE'")
    expect(bookings).toContain("'ABSENT'")
    expect(bookings).toContain('取消预约')
    expect(bookings).not.toContain('缺席/取消')
    expect(bookings).not.toContain('updateStatus(item, 3)')
    expect(bookings).not.toContain('updateStatus(item, 4)')
  })

  it('implements list/detail states, refresh, privacy, avatar fallback and four detail tabs', () => {
    const list = readFileSync('pages/coach/student-list.vue', 'utf8')
    const detail = readFileSync('pages/coach/student-detail.vue', 'utf8')

    for (const source of [list, detail]) {
      expect(source).toContain('refresher-enabled')
      expect(source).toContain('onShow')
      expect(source).toContain('loadFailed')
      expect(source).toContain('DEFAULT_COACH_STUDENT_AVATAR')
      expect(source).toContain('@error')
    }
    expect(list).toContain('maskedPhone')
    expect(list).not.toContain('student.phone')
    expect(detail).toContain("'overview'")
    expect(detail).toContain("'progress'")
    expect(detail).toContain("'schedule'")
    expect(detail).toContain("'attendance'")
    expect(detail).toContain('uni.showModal')
    expect(detail).toContain('uni.makePhoneCall')
    expect(detail).not.toContain('totalRecharge')
    expect(detail).not.toContain('balance')
  })

  it('allows an absent finished booking to be corrected to completed within 24 hours', () => {
    expect(canCompleteBooking({
      status: 4,
      attendanceStatus: 3,
      courseDate: '2026-07-13',
      courseEndTime: '11:00:00'
    }, new Date('2026-07-14T10:59:59'))).toBe(true)
    expect(canCompleteBooking({
      status: 4,
      attendanceStatus: 3,
      courseDate: '2026-07-13',
      courseEndTime: '11:00:00'
    }, new Date('2026-07-14T11:00:01'))).toBe(false)

    const sessionStudents = readFileSync('pages/coach/students.vue', 'utf8')
    expect(sessionStudents).toContain('更正为完成')
  })

  it('partitions schedules using booking and attendance statuses, then sorts each group', () => {
    const partitionCoachStudentSchedules = (coachStudentUtils as Record<string, unknown>)
      .partitionCoachStudentSchedules as undefined | ((items: Array<Record<string, unknown>>) => {
        upcoming: Array<{ bookingId: number }>
        history: Array<{ bookingId: number }>
      })

    expect(partitionCoachStudentSchedules).toBeTypeOf('function')
    if (!partitionCoachStudentSchedules) return

    const base = {
      courseId: 1,
      courseName: '羽毛球私教课',
      startTime: '10:00:00',
      endTime: '11:00:00'
    }
    const result = partitionCoachStudentSchedules([
      { ...base, bookingId: 1, courseDate: '2026-07-20', bookingStatus: 2, attendanceStatus: 0 },
      { ...base, bookingId: 2, courseDate: '2026-07-15', bookingStatus: 3, attendanceStatus: 1 },
      { ...base, bookingId: 3, courseDate: '2026-07-25', bookingStatus: 2, attendanceStatus: 2 },
      { ...base, bookingId: 4, courseDate: '2026-07-01', bookingStatus: 4, attendanceStatus: 0 },
      { ...base, bookingId: 5, courseDate: '2026-07-30', bookingStatus: 0, attendanceStatus: 0 },
      { ...base, bookingId: 6, courseDate: '2026-07-02', bookingStatus: 1, attendanceStatus: 3 }
    ])

    expect(result.upcoming.map(item => item.bookingId)).toEqual([2, 1])
    expect(result.history.map(item => item.bookingId)).toEqual([5, 3, 6, 4])
  })

  it('calculates monthly attendance rate from completed and absent records only', () => {
    const groupCoachStudentAttendanceByMonth = (coachStudentUtils as Record<string, unknown>)
      .groupCoachStudentAttendanceByMonth as undefined | ((items: Array<Record<string, unknown>>) => Array<{
        monthKey: string
        completed: number
        absent: number
        attendanceRate: number
      }>)

    expect(groupCoachStudentAttendanceByMonth).toBeTypeOf('function')
    if (!groupCoachStudentAttendanceByMonth) return

    const base = {
      courseName: '羽毛球私教课',
      startTime: '10:00:00',
      endTime: '11:00:00',
      bookingStatus: 4
    }
    const groups = groupCoachStudentAttendanceByMonth([
      { ...base, bookingId: 1, courseDate: '2026-07-20', attendanceStatus: 2 },
      { ...base, bookingId: 2, courseDate: '2026-07-18', attendanceStatus: 2 },
      { ...base, bookingId: 3, courseDate: '2026-07-15', attendanceStatus: 3 },
      { ...base, bookingId: 4, courseDate: '2026-07-10', attendanceStatus: 1 },
      { ...base, bookingId: 5, courseDate: '2026-06-10', attendanceStatus: 0 }
    ])

    expect(groups[0]).toMatchObject({
      monthKey: '2026-07',
      completed: 2,
      absent: 1,
      attendanceRate: 67
    })
    expect(groups[1]).toMatchObject({
      monthKey: '2026-06',
      completed: 0,
      absent: 0,
      attendanceRate: 0
    })
  })

  it('opens a booking detail from the id route and makes the mini timeline actionable', () => {
    const normalizeCoachBookingId = (coachViewUtils as Record<string, unknown>)
      .normalizeCoachBookingId as undefined | ((value?: string | number) => number | null)
    const bookings = readFileSync('pages/coach/bookings.vue', 'utf8')
    const detail = readFileSync('pages/coach/student-detail.vue', 'utf8')

    expect(normalizeCoachBookingId).toBeTypeOf('function')
    if (!normalizeCoachBookingId) return
    expect(normalizeCoachBookingId('42')).toBe(42)
    expect(normalizeCoachBookingId('0')).toBeNull()
    expect(normalizeCoachBookingId('abc')).toBeNull()
    expect(bookings).toContain('normalizeCoachBookingId(options?.id)')
    expect(bookings).toContain('openDetail(bookingId)')
    expect(detail).toContain('@tap="selectTab(\'attendance\')"')
    expect(detail).toContain('出勤率 {{ group.attendanceRate }}%')
  })

  it('applies student list keyword and risk filters from the generated route', () => {
    const parseCoachStudentListRoute = (coachStudentUtils as Record<string, unknown>)
      .parseCoachStudentListRoute as undefined | ((options?: Record<string, string>) => {
        keyword: string
        riskOnly: boolean
      })
    const list = readFileSync('pages/coach/student-list.vue', 'utf8')

    expect(parseCoachStudentListRoute).toBeTypeOf('function')
    if (!parseCoachStudentListRoute) return
    expect(parseCoachStudentListRoute({
      keyword: '%E5%BC%A0%20%E4%B8%89',
      riskOnly: 'true'
    })).toEqual({ keyword: '张 三', riskOnly: true })
    expect(parseCoachStudentListRoute({ riskOnly: 'false' })).toEqual({ keyword: '', riskOnly: false })
    expect(list).toContain('onLoad(options =>')
    expect(list).toContain('parseCoachStudentListRoute(options)')
  })
})
