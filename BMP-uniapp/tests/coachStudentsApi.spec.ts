import { beforeEach, describe, expect, it, vi } from 'vitest'

const { getMock, putMock } = vi.hoisted(() => ({
  getMock: vi.fn(),
  putMock: vi.fn()
}))

vi.mock('@/utils/request', () => ({
  get: getMock,
  put: putMock
}))

import { API_PATHS } from '@/config/api'
import {
  getCoachStudentAttendance,
  getCoachStudentConsumeRecords,
  getCoachStudentDetail,
  getCoachStudentProgress,
  getCoachStudentSchedule,
  getCoachStudents,
  updateCoachBookingAttendance
} from '@/api/coachSelf'

describe('coach student APIs', () => {
  beforeEach(() => {
    getMock.mockReset()
    putMock.mockReset()
  })

  it('declares all six frozen query paths', () => {
    expect(API_PATHS.COACH.STUDENTS).toBe('/coach/students')
    expect(API_PATHS.COACH.STUDENT_DETAIL(8)).toBe('/coach/students/8')
    expect(API_PATHS.COACH.STUDENT_PROGRESS(8)).toBe('/coach/students/8/progress')
    expect(API_PATHS.COACH.STUDENT_SCHEDULE(8)).toBe('/coach/students/8/schedule')
    expect(API_PATHS.COACH.STUDENT_ATTENDANCE(8)).toBe('/coach/students/8/attendance')
    expect(API_PATHS.COACH.STUDENT_CONSUME_RECORDS(8)).toBe('/coach/students/8/consume-records')
  })

  it('calls six query endpoints with the backend field contract', () => {
    getCoachStudents({ page: 1, size: 20, orderBy: 'lastLessonTime' })
    getCoachStudentDetail(8)
    getCoachStudentProgress(8)
    getCoachStudentSchedule(8, { page: 2, size: 10 })
    getCoachStudentAttendance(8, { attendanceStatus: 3 })
    getCoachStudentConsumeRecords(8, { page: 1, size: 20 })

    expect(getMock).toHaveBeenNthCalledWith(1, '/coach/students',
      { page: 1, size: 20, orderBy: 'lastLessonTime' }, expect.anything())
    expect(getMock).toHaveBeenNthCalledWith(2, '/coach/students/8', {}, expect.anything())
    expect(getMock).toHaveBeenNthCalledWith(3, '/coach/students/8/progress', {}, expect.anything())
    expect(getMock).toHaveBeenNthCalledWith(4, '/coach/students/8/schedule',
      { page: 2, size: 10 }, expect.anything())
    expect(getMock).toHaveBeenNthCalledWith(5, '/coach/students/8/attendance',
      { attendanceStatus: 3 }, expect.anything())
    expect(getMock).toHaveBeenNthCalledWith(6, '/coach/students/8/consume-records',
      { page: 1, size: 20 }, expect.anything())
  })

  it('sends attendance action instead of arbitrary attendanceStatus', () => {
    updateCoachBookingAttendance({ id: 99, action: 'ABSENT', remark: '请假' })

    expect(putMock).toHaveBeenCalledWith('/course/booking/for-coach/attendance',
      { id: 99, action: 'ABSENT', remark: '请假' }, expect.anything())
  })
})
