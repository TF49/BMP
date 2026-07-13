export type CourseBookingStatus = 0 | 1 | 2 | 3 | 4
export type AttendanceStatus = 0 | 1 | 2 | 3
export type AttendanceAction = 'CHECK_IN' | 'COMPLETE' | 'ABSENT'

export interface CoachStudentListItem {
  id: number
  memberName: string
  gender?: number
  avatar?: string
  maskedPhone: string
  memberType: 'NORMAL' | 'MEMBER'
  memberLevel?: number
  memberStatus?: number
  expireTime?: string
  firstLessonTime?: string
  lastLessonTime?: string
  nextLessonTime?: string
  totalBookings: number
  attendedCount: number
  absentCount: number
  cancelledCount: number
  scheduledCount: number
  attendanceRate: number
  totalHours: number
  totalPaidAmount: number
  risk: boolean
}

export interface CoachStudentDetail {
  id: number
  memberName: string
  gender?: number
  avatar?: string
  phone?: string
  idCardTail?: string
  memberType: 'NORMAL' | 'MEMBER'
  memberLevel?: number
  memberStatus?: number
  registerTime?: string
  expireTime?: string
  firstLessonTime?: string
  lastLessonTime?: string
  nextLessonTime?: string
  totalBookings: number
  attendedCount: number
  absentCount: number
  attendanceRate: number
  totalHours: number
  totalConsumptionForCoach: number
  risk: boolean
}

export interface CoachStudentProgress {
  courseName: string
  totalBookings: number
  completedLessons: number
  totalHours: number
  lastLessonTime?: string
}

export interface CoachStudentScheduleItem {
  bookingId: number
  courseId: number
  courseName: string
  courseDate: string
  startTime: string
  endTime: string
  bookingStatus: CourseBookingStatus
  attendanceStatus: AttendanceStatus
  actualCheckinTime?: string
  actualFinishTime?: string
  remark?: string
}

export interface CoachStudentAttendanceItem {
  bookingId: number
  courseName: string
  courseDate: string
  startTime: string
  endTime: string
  bookingStatus: CourseBookingStatus
  attendanceStatus: AttendanceStatus
  durationMinutes?: number
  remark?: string
}

export interface CoachStudentConsumeRecord {
  id: number
  bookingId: number
  courseName: string
  amount: number
  paymentMethod?: string
  paymentStatus?: number
  description?: string
  remark?: string
  createTime: string
}

export interface PageResult<T> {
  data: T[]
  total: number
  page: number
  size: number
  pages: number
}

export interface CoachStudentListResponse {
  page: PageResult<CoachStudentListItem>
  totalStudents: number
  riskStudents: number
  todayStudents: number
  averageAttendanceRate: number
}

export interface CoachStudentListQuery {
  page?: number
  size?: number
  keyword?: string
  memberType?: 'NORMAL' | 'MEMBER'
  riskOnly?: boolean
  todayOnly?: boolean
  orderBy?: 'lastLessonTime' | 'attendanceRate' | 'totalPaidAmount' | 'totalHours'
  orderDirection?: 'ASC' | 'DESC'
}

export interface CoachStudentScheduleQuery {
  startDate?: string
  endDate?: string
  status?: CourseBookingStatus
  attendanceStatus?: AttendanceStatus
  page?: number
  size?: number
}

export interface CoachAttendanceCommand {
  id: number
  action: AttendanceAction
  remark?: string
}
