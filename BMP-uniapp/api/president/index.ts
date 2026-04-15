/**
 * 会长端 API 统一导出
 * - 所有请求均通过 @/utils/request，已统一处理：Token、403 登出、业务错误 Toast
 * - 列表接口返回格式兼容：{ data: [], total } 或 { data: { data: [], total } }
 */
export * from './dashboard'
export * from './audit'
export * from './user'
export * from './venue'
export * from './finance'
export * from './booking'
export * from './member'
export * from './coach'
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
  deleteCourseBooking
} from './course'
export * from './tournament'
export * from './notification'
export * from './equipment'
export * from './stringing'
