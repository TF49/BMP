import { get, put } from '@/utils/request'
import { API_PATHS } from '@/config/api'
import type {
  CoachAttendanceCommand,
  CoachStudentAttendanceItem,
  CoachStudentConsumeRecord,
  CoachStudentDetail,
  CoachStudentListQuery,
  CoachStudentListResponse,
  CoachStudentProgress,
  CoachStudentScheduleItem,
  CoachStudentScheduleQuery,
  PageResult as CoachStudentPageResult
} from '@/types/coachStudents'

export interface CoachProfile {
  id: number
  coachName: string
  phone?: string
  specialty?: string
  experience?: string
  avatar?: string
  venueId?: number
  venueName?: string
  rating?: number
  totalStudents?: number
  hourlyPrice?: number
  status?: number
}

export interface CoachProfileUpdatePayload {
  coachName: string
  phone: string
  specialty?: string
  experience?: string
  avatar?: string
}

export interface CoachCourseItem {
  id: number
  courseName: string
  coachId: number
  coachName?: string
  courtId?: number
  courtName?: string
  venueName?: string
  courseContent?: string
  coursePrice?: number
  courseDuration?: number
  courseDate?: string
  startTime?: string
  endTime?: string
  maxStudents?: number
  currentStudents?: number
  status?: number
}

export interface CoachBookingItem {
  id: number
  bookingNo: string
  memberId?: number
  memberName?: string
  courseId?: number
  courseName?: string
  coachName?: string
  courtName?: string
  courseDate?: string
  courseStartTime?: string
  courseEndTime?: string
  startTime?: string
  endTime?: string
  orderAmount?: number
  paymentMethod?: string
  paymentStatus?: number
  status?: number
  attendanceStatus?: number
  actualCheckinTime?: string
  actualFinishTime?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  data: T[]
  total: number
  page: number
  size: number
  pages: number
}

export interface CoachCourseQuery {
  page?: number
  size?: number
  status?: number
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface CoachBookingQuery {
  page?: number
  size?: number
  courseId?: number
  status?: number
  keyword?: string
}

export interface CoachBookingStatusPayload {
  id: number
  status: number
  remark?: string
}

export function getCurrentCoach() {
  return get<CoachProfile>(API_PATHS.COACH.ME, {}, { suppressErrorToast: true })
}

export function updateCurrentCoach(data: CoachProfileUpdatePayload) {
  return put<null>(API_PATHS.COACH.ME, data, { suppressErrorToast: true })
}

export function getMyCourses(params?: CoachCourseQuery) {
  return get<PageResult<CoachCourseItem>>(API_PATHS.COURSE.MY, params, { suppressErrorToast: true })
}

export function getBookingsForCoach(params?: CoachBookingQuery) {
  return get<PageResult<CoachBookingItem>>(API_PATHS.COURSE.BOOKING.COACH_LIST, params, { suppressErrorToast: true })
}

export function getBookingDetailForCoach(id: number) {
  return get<CoachBookingItem>(`${API_PATHS.COURSE.BOOKING.COACH_DETAIL}/${id}`, {}, { suppressErrorToast: true })
}

export function updateBookingStatusForCoach(data: CoachBookingStatusPayload) {
  return put<null>(API_PATHS.COURSE.BOOKING.COACH_STATUS, data, { suppressErrorToast: true })
}

export function updateCoachBookingAttendance(data: CoachAttendanceCommand) {
  return put(API_PATHS.COURSE.BOOKING.COACH_ATTENDANCE, data, { suppressErrorToast: true })
}

export function getCoachStudents(params?: CoachStudentListQuery) {
  return get<CoachStudentListResponse>(API_PATHS.COACH.STUDENTS, params, { suppressErrorToast: true })
}

export function getCoachStudentDetail(id: number) {
  return get<CoachStudentDetail>(API_PATHS.COACH.STUDENT_DETAIL(id), {}, { suppressErrorToast: true })
}

export function getCoachStudentProgress(id: number) {
  return get<CoachStudentProgress[]>(API_PATHS.COACH.STUDENT_PROGRESS(id), {}, { suppressErrorToast: true })
}

export function getCoachStudentSchedule(id: number, params?: CoachStudentScheduleQuery) {
  return get<CoachStudentPageResult<CoachStudentScheduleItem>>(
    API_PATHS.COACH.STUDENT_SCHEDULE(id), params, { suppressErrorToast: true })
}

export function getCoachStudentAttendance(id: number, params?: CoachStudentScheduleQuery) {
  return get<CoachStudentPageResult<CoachStudentAttendanceItem>>(
    API_PATHS.COACH.STUDENT_ATTENDANCE(id), params, { suppressErrorToast: true })
}

export function getCoachStudentConsumeRecords(id: number, params?: Pick<CoachStudentScheduleQuery, 'page' | 'size'>) {
  return get<CoachStudentPageResult<CoachStudentConsumeRecord>>(
    API_PATHS.COACH.STUDENT_CONSUME_RECORDS(id), params, { suppressErrorToast: true })
}

export type {
  CoachAttendanceCommand,
  CoachStudentAttendanceItem,
  CoachStudentConsumeRecord,
  CoachStudentDetail,
  CoachStudentListItem,
  CoachStudentListQuery,
  CoachStudentListResponse,
  CoachStudentProgress,
  CoachStudentScheduleItem,
  CoachStudentScheduleQuery
} from '@/types/coachStudents'
