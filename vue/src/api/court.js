import request from '@/utils/request'

/**
 * 获取场地列表
 * @param {Object} params - 查询参数
 * @param {number} params.venueId - 场馆ID（可选）
 * @param {number} params.status - 状态（0-维护中，1-空闲，2-预约中，3-使用中，可选）
 * @param {string} params.keyword - 关键词（匹配场地编号或名称，可选）
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.size - 每页数量（默认10）
 */
export function getCourtList(params) {
  return request({
    url: '/api/court/list',
    method: 'get',
    params
  })
}

/**
 * 获取场地详情
 * @param {number} id - 场地ID
 */
export function getCourtInfo(id) {
  return request({
    url: `/api/court/info/${id}`,
    method: 'get'
  })
}

/**
 * 添加场地（管理员）
 * @param {Object} data - 场地数据
 * @param {string} data.courtCode - 场地编号（必填）
 * @param {number} data.venueId - 所属场馆ID（必填）
 * @param {string} data.courtName - 场地名称
 * @param {string} data.billingType - 计费方式（HOUR-按小时，TIME-按次）
 * @param {number} data.pricePerHour - 价格（必填）
 * @param {number} data.status - 状态（0-维护中，1-空闲，2-预约中，3-使用中）
 */
export function addCourt(data) {
  return request({
    url: '/api/court/add',
    method: 'post',
    data
  })
}

/**
 * 更新场地（管理员）
 * @param {Object} data - 场地数据
 * @param {number} data.id - 场地ID（必填）
 */
export function updateCourt(data) {
  return request({
    url: '/api/court/update',
    method: 'put',
    data
  })
}

/**
 * 删除场地（管理员）
 * @param {number} id - 场地ID
 */
export function deleteCourt(id) {
  return request({
    url: `/api/court/${id}`,
    method: 'delete'
  })
}

/**
 * 更新场地状态（管理员）
 * @param {number} id - 场地ID
 * @param {number} status - 新状态（0-维护中，1-空闲，2-预约中，3-使用中）
 */
export function updateCourtStatus(id, status) {
  return request({
    url: '/api/court/status',
    method: 'put',
    params: { id, status }
  })
}

/**
 * 获取场地统计信息
 * @returns {Promise} 返回统计数据（总数、维护中、空闲、预约中、使用中）
 */
export function getCourtStatistics() {
  return request({
    url: '/api/court/statistics',
    method: 'get'
  })
}

/**
 * 获取场馆下拉列表（供选择场馆使用）
 * @returns {Promise} 返回场馆列表（仅包含ID、名称、状态）
 */
export function getVenueOptions() {
  return request({
    url: '/api/court/venues',
    method: 'get'
  })
}

/**
 * 获取指定场地当天的预约用户信息
 * @param {number} courtId - 场地ID
 * @param {string} [date] - 查询日期 yyyy-MM-dd，不传则使用服务器当前日期（建议传入用户本地日期避免时区问题）
 * @returns {Promise} 返回当天预约用户列表（姓名已脱敏）
 * 返回数据结构：
 * [
 *   {
 *     bookingId: 1,
 *     memberName: "张**",       // 脱敏姓名
 *     memberType: "MEMBER",     // NORMAL/MEMBER
 *     memberLevel: 3,           // 会员等级 1-5
 *     startTime: "09:00",
 *     endTime: "11:00",
 *     status: 3,                // 1-待支付，2-已支付，3-进行中
 *     statusText: "进行中",
 *     memberLevelText: "VIP Lv.3",
 *     memberTagType: "warning",
 *     statusTagType: "primary"
 *   }
 * ]
 */
export function getTodayBookingUsers(courtId, date) {
  return request({
    url: `/api/court/${courtId}/bookings/today`,
    method: 'get',
    params: date ? { date } : {}
  })
}

/**
 * 批量获取多个场地当天的预约数量
 * @param {Array<number>} courtIds - 场地ID数组
 * @param {string} [date] - 查询日期 yyyy-MM-dd，不传则使用服务器当前日期（建议传入用户本地日期避免时区问题）
 * @returns {Promise} 返回场地ID与预约数量的映射 { courtId: count }
 */
export function getTodayBookingCounts(courtIds, date) {
  const params = { ids: courtIds.join(',') }
  if (date) params.date = date
  return request({
    url: '/api/court/bookings/today/counts',
    method: 'get',
    params
  })
}

/**
 * 获取指定日期各场地的占用时间轴（Dashboard 今日场地时间轴）
 * @param {Object} params - { date: 'yyyy-MM-dd' }
 * @returns {Promise} 返回 [{ courtId, courtName, timeSlots: [{ status, duration }] }]
 */
export function getCourtSchedule(params) {
  return request({
    url: '/api/court/timeline/today',
    method: 'get',
    params
  })
}
