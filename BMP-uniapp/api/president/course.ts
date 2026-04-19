/**
 * 会长端课程 API（仅从 internal 转发）
 */
export type {
  CourseItem,
  CoursePayload,
  CourseBookingParams,
  CourseBookingItem
} from '@/api/internal/course'
export {
  getCourseList,
  getCourseDetail,
  addCourse,
  updateCourse,
  updateCourseStatus,
  getCourseBookingList,
  createCourseBooking,
  getCourseBookingDetail,
  updateCourseBookingStatus,
  processCourseBookingPayment,
  processCourseBookingRefund,
  deleteCourseBooking,
  getCoachList
} from '@/api/internal/course'
