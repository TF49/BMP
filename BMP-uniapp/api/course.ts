/**
 * 用户端课程 API（仅导出会员页使用的符号；实现见 @/api/internal/course）
 */
export type { CourseItem, CourseBookingParams, CourseBookingItem } from '@/api/internal/course'
export {
  getCourseList,
  getCourseDetail,
  createCourseBooking,
  getCoachList,
  getCourseBookingList,
  getCourseBookingDetail,
  processCourseBookingPayment
} from '@/api/internal/course'
