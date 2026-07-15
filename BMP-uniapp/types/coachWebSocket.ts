export const COACH_STUDENT_EVENT_TYPES = [
  'COACH_STUDENT_NEW_BOOKING',
  'COACH_STUDENT_CANCELLED',
  'COACH_STUDENT_ATTENDANCE_CHANGED'
] as const

export type CoachStudentEventType = typeof COACH_STUDENT_EVENT_TYPES[number]
export type CoachStudentEventPriority = 'NORMAL' | 'HIGH'

export interface CoachStudentWebSocketEvent {
  type: CoachStudentEventType
  priority: CoachStudentEventPriority
  coachId: number
  memberId: number
  bookingId: number
  courseId: number
  courseName: string
  memberName: string
  occurredAt: string
  bookingStatus?: number | null
  attendanceStatus?: number | null
}
