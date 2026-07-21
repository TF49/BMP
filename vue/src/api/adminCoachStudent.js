import request from '@/utils/request'

/**
 * 管理端 - 教练学员关系管理 API
 * 对应后端: /api/admin/coach-students
 */

// 分页查询教练学员关系列表
export function getCoachStudentList(params) {
  return request({
    url: '/api/admin/coach-students/list',
    method: 'get',
    params
  })
}

// 手动绑定学员到教练
export function bindCoachStudent(coachId, memberId) {
  return request({
    url: '/api/admin/coach-students/bind',
    method: 'post',
    data: { coachId, memberId }
  })
}

// 解绑（逻辑删除关系）
export function unbindCoachStudent(id) {
  return request({
    url: `/api/admin/coach-students/${id}`,
    method: 'delete'
  })
}

// 学员详情 - 课程排课列表
export function getStudentSchedule(params) {
  return request({
    url: '/api/admin/coach-students/detail/schedule',
    method: 'get',
    params
  })
}

// 学员详情 - 考勤历史列表
export function getStudentAttendance(params) {
  return request({
    url: '/api/admin/coach-students/detail/attendance',
    method: 'get',
    params
  })
}

// 学员详情 - 消费明细列表
export function getStudentConsumeRecords(params) {
  return request({
    url: '/api/admin/coach-students/detail/consume-records',
    method: 'get',
    params
  })
}
