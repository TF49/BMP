import request from '@/utils/request'

// 课程列表
export function getCourseList(params) {
  return request({
    url: '/api/course/list',
    method: 'get',
    params
  })
}

// 教练端：我的课程（仅当前教练）
export function getMyCourses(params) {
  return request({
    url: '/api/course/my',
    method: 'get',
    params
  })
}

// 课程详情
export function getCourseInfo(id) {
  return request({
    url: `/api/course/info/${id}`,
    method: 'get'
  })
}

// 新增课程
export function addCourse(data) {
  return request({
    url: '/api/course/add',
    method: 'post',
    data
  })
}

// 更新课程
export function updateCourse(data) {
  return request({
    url: '/api/course/update',
    method: 'put',
    data
  })
}

// 删除课程
export function deleteCourse(id) {
  return request({
    url: `/api/course/${id}`,
    method: 'delete'
  })
}

// 更新课程状态
export function updateCourseStatus(id, status) {
  return request({
    url: '/api/course/status',
    method: 'put',
    params: { id, status }
  })
}

// 统计信息
export function getCourseStatistics() {
  return request({
    url: '/api/course/statistics',
    method: 'get'
  })
}

// 下拉：教练
export function getCourseCoaches() {
  return request({
    url: '/api/course/coaches',
    method: 'get'
  })
}

// 下拉：场地
export function getCourseCourts() {
  return request({
    url: '/api/course/courts',
    method: 'get'
  })
}
