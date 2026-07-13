import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'
import {
  DEFAULT_COACH_STUDENT_AVATAR,
  buildCoachStudentDetailUrl,
  buildCoachStudentListUrl,
  getStudentAvatarWithFallback,
  maskPhone
} from '@/utils/coachStudents'
import { canCompleteBooking } from '@/utils/coachView'

describe('coach student routes and privacy helpers', () => {
  it('builds dedicated list and detail routes', () => {
    expect(buildCoachStudentListUrl()).toBe('/pages/coach/student-list')
    expect(buildCoachStudentDetailUrl(18)).toBe('/pages/coach/student-detail?id=18')
  })

  it('masks phone and reuses the single avatar placeholder', () => {
    expect(maskPhone('13812341001')).toBe('138****1001')
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
})
