import request from '@/utils/request'
import { withDedupe } from '@/utils/requestDedupe'

/**
 * 获取场馆列表
 * @param {Object} params - 查询参数
 * @param {string} params.venueName - 场馆名称（可选）
 * @param {string} params.address - 地址（可选）
 * @param {number} params.status - 状态（0-关闭，1-营业中，2-暂停，可选）
 * @param {number} params.page - 页码（默认1）
 * @param {number} params.size - 每页数量（默认10）
 */
export function getVenueList(params) {
  return request({
    url: '/api/venue/list',
    method: 'get',
    params
  })
}

/**
 * 获取场馆详情
 * @param {number} id - 场馆ID
 */
export function getVenueInfo(id) {
  return request({
    url: `/api/venue/info/${id}`,
    method: 'get'
  })
}

/**
 * 添加场馆
 * @param {Object} data - 场馆数据
 */
export function addVenue(data) {
  return request({
    url: '/api/venue/add',
    method: 'post',
    data
  })
}

/**
 * 更新场馆
 * @param {Object} data - 场馆数据
 */
export function updateVenue(data) {
  return request({
    url: '/api/venue/update',
    method: 'put',
    data
  })
}

/**
 * 删除场馆
 * @param {number} id - 场馆ID
 */
export function deleteVenue(id) {
  return request({
    url: `/api/venue/${id}`,
    method: 'delete'
  })
}

/**
 * 上传场馆图片（管理员）- 单图片（向后兼容）
 * @param {File} file - 图片文件
 */
export function uploadVenueImage(file, venueId) {
  const formData = new FormData()
  formData.append('file', file)
  if (venueId) {
    formData.append('venueId', venueId)
  }

  return request({
    url: '/api/venue/upload-image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 多图片上传（管理员）
 * @param {File[]} files - 图片文件数组
 * @param {number} venueId - 场馆ID（可选）
 * @param {string} imageType - 图片类型（MAIN/DETAIL，默认DETAIL）
 */
export function uploadVenueImages(files, venueId, imageType = 'DETAIL') {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  if (venueId) {
    formData.append('venueId', venueId)
  }
  formData.append('imageType', imageType)

  return request({
    url: '/api/venue/upload-images',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取场馆的所有图片
 * @param {number} venueId - 场馆ID
 */
export function getVenueImages(venueId) {
  return request({
    url: `/api/venue/${venueId}/images`,
    method: 'get'
  })
}

/**
 * 将已经上传到服务器但尚未入库的图片 URL 绑定到指定场馆
 * 使用场景：新增场馆时先上传详情图，保存成功拿到场馆ID后再调用本接口完成绑定
 * @param {number} venueId - 场馆ID
 * @param {Array} images - 图片列表（元素支持字段：imageUrl/url, imageType, sortOrder）
 */
export function bindVenueImages(venueId, images) {
  return request({
    url: `/api/venue/${venueId}/images/bind`,
    method: 'post',
    data: images
  })
}

/**
 * 删除图片（管理员）
 * @param {number} id - 图片ID
 */
export function deleteVenueImage(id) {
  return request({
    url: `/api/venue/image/${id}`,
    method: 'delete'
  })
}

/**
 * 更新图片排序（管理员）
 * @param {number} id - 图片ID
 * @param {number} sortOrder - 排序顺序
 */
export function updateImageSortOrder(id, sortOrder) {
  return request({
    url: `/api/venue/image/${id}/sort`,
    method: 'put',
    params: { sortOrder }
  })
}

/**
 * 获取场馆的营业时间配置
 * @param {number} venueId - 场馆ID
 */
export function getVenueSchedules(venueId) {
  return request({
    url: `/api/venue/${venueId}/schedules`,
    method: 'get'
  })
}

/**
 * 保存场馆营业时间配置（管理员）
 * @param {number} venueId - 场馆ID
 * @param {Array} schedules - 营业时间列表
 */
export function saveVenueSchedules(venueId, schedules) {
  return request({
    url: `/api/venue/${venueId}/schedules`,
    method: 'post',
    data: schedules
  })
}

/**
 * 删除营业时间配置（管理员）
 * @param {number} id - 时间表ID
 */
export function deleteVenueSchedule(id) {
  return request({
    url: `/api/venue/schedule/${id}`,
    method: 'delete'
  })
}

/**
 * 获取场馆状态变更历史（管理员）
 * @param {number} venueId - 场馆ID
 */
export function getVenueStatusLogs(venueId) {
  return request({
    url: `/api/venue/${venueId}/status-logs`,
    method: 'get'
  })
}

/**
 * 获取场馆统计信息（Dashboard 多图并发时去重）
 */
export function getVenueStatistics() {
  return withDedupe('GET:/api/venue/statistics', () =>
    request({
      url: '/api/venue/statistics',
      method: 'get'
    })
  )
}
