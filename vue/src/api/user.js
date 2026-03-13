import request from '@/utils/request'

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {string} params.username - 用户名（可选）
 * @param {string} params.idCard - 身份证号（可选）
 * @param {string} params.role - 角色（PRESIDENT/VENUE_MANAGER/USER，可选）
 * @param {number} params.status - 状态（0-禁用，1-启用，可选）
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.size - 每页数量（默认10）
 */
export function getUserList(params) {
  return request({
    url: '/api/user/list',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 * @param {number} id - 用户ID
 */
export function getUserInfo(id) {
  return request({
    url: `/api/user/info/${id}`,
    method: 'get'
  })
}

/**
 * 添加用户
 * @param {Object} data - 用户数据
 */
export function addUser(data) {
  return request({
    url: '/api/user/add',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 * @param {Object} data - 用户数据
 */
export function updateUser(data) {
  return request({
    url: '/api/user/update',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 */
export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

/**
 * 按角色查找用户
 * @param {string} role - 角色名
 */
export function findUserByRole(role) {
  return request({
    url: '/api/user/findByRole',
    method: 'get',
    params: { role }
  })
}

/**
 * 注销账号
 * @param {Object} data - 注销数据
 * @param {string} data.password - 当前密码
 * @param {string} data.reason - 注销原因
 */
export function deleteAccount(data) {
  return request({
    url: '/api/auth/delete-account',
    method: 'post',
    data
  })
}
