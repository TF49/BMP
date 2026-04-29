/**
 * 权限检查工具
 * 提供前端按钮级权限控制
 */

import { getUserInfo } from '@/utils/auth'

/**
 * 角色常量
 */
export const ROLES = {
  PRESIDENT: 'PRESIDENT',       // 协会会长
  VENUE_MANAGER: 'VENUE_MANAGER', // 场馆管理者
  USER: 'USER',                 // 普通用户
  MEMBER: 'MEMBER'              // 会员
}

/**
 * 获取当前用户角色
 * @returns {string|null} 用户角色
 */
export function getCurrentRole() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.role : null
}

/**
 * 获取当前用户场馆ID
 * @returns {number|null} 场馆ID
 */
export function getCurrentVenueId() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.venueId : null
}

/**
 * 检查是否为协会会长
 * @returns {boolean}
 */
export function isPresident() {
  return getCurrentRole() === ROLES.PRESIDENT
}

/**
 * 检查是否为场馆管理者
 * @returns {boolean}
 */
export function isVenueManager() {
  return getCurrentRole() === ROLES.VENUE_MANAGER
}

/**
 * 检查是否为用户端角色
 * @returns {boolean}
 */
export function isUser() {
  return [ROLES.USER, ROLES.MEMBER].includes(getCurrentRole())
}

/**
 * 检查是否有指定角色之一
 * @param {string|string[]} roles 角色或角色数组
 * @returns {boolean}
 */
export function hasRole(roles) {
  const currentRole = getCurrentRole()
  if (!currentRole) return false

  if (Array.isArray(roles)) {
    return roles.includes(currentRole)
  }
  return currentRole === roles
}

/**
 * 检查是否有管理权限（会长或场馆管理者）
 * @returns {boolean}
 */
export function hasManagePermission() {
  return hasRole([ROLES.PRESIDENT, ROLES.VENUE_MANAGER])
}

/**
 * 检查是否可以管理指定场馆
 * @param {number} venueId 场馆ID
 * @returns {boolean}
 */
export function canManageVenue(venueId) {
  if (isPresident()) return true
  if (isVenueManager()) {
    return getCurrentVenueId() === venueId
  }
  return false
}

/**
 * 检查是否可以编辑指定记录
 * @param {Object} record 记录对象（需包含venueId或userId）
 * @returns {boolean}
 */
export function canEditRecord(record) {
  if (isPresident()) return true

  if (isVenueManager()) {
    // 场馆管理者只能编辑自己场馆的数据
    if (record.venueId) {
      return canManageVenue(record.venueId)
    }
    return false
  }

  if (isUser()) {
    // 普通用户只能编辑自己的数据
    const userInfo = getUserInfo()
    if (record.userId || record.memberId) {
      return (record.userId === userInfo?.id) || (record.memberId === userInfo?.memberId)
    }
    return false
  }

  return false
}

/**
 * 检查是否可以删除指定记录
 * @param {Object} record 记录对象
 * @returns {boolean}
 */
export function canDeleteRecord(record) {
  // 删除权限通常比编辑权限更严格
  if (isPresident()) return true

  if (isVenueManager()) {
    // 场馆管理者可以删除自己场馆的部分数据
    if (record.venueId) {
      return canManageVenue(record.venueId)
    }
    return false
  }

  // 普通用户通常不能删除
  return false
}

/**
 * 权限配置表
 * 定义各模块各操作的权限要求
 */
export const PERMISSIONS = {
  // 用户管理
  user: {
    view: [ROLES.PRESIDENT],
    create: [ROLES.PRESIDENT],
    edit: [ROLES.PRESIDENT],
    delete: [ROLES.PRESIDENT]
  },
  // 场馆管理
  venue: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT],
    edit: [ROLES.PRESIDENT],
    delete: [ROLES.PRESIDENT]
  },
  // 会员管理
  member: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT],
    recharge: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER]
  },
  // 场地管理
  court: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER]
  },
  // 预约管理
  booking: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    cancel: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER]
  },
  // 器材管理
  equipment: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER]
  },
  // 穿线服务
  stringing: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    updateStatus: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER]
  },
  // 教练管理
  coach: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER]
  },
  // 课程管理
  course: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER]
  },
  // 赛事管理
  tournament: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER, ROLES.USER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT]
  },
  // 财务管理
  finance: {
    view: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    create: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    edit: [ROLES.PRESIDENT, ROLES.VENUE_MANAGER],
    delete: [ROLES.PRESIDENT],
    export: [ROLES.PRESIDENT]
  }
}

/**
 * 检查是否有指定模块的指定操作权限
 * @param {string} module 模块名
 * @param {string} action 操作名
 * @returns {boolean}
 */
export function hasPermission(module, action) {
  const modulePerms = PERMISSIONS[module]
  if (!modulePerms) return false

  const allowedRoles = modulePerms[action]
  if (!allowedRoles) return false

  return hasRole(allowedRoles)
}

export default {
  ROLES,
  getCurrentRole,
  getCurrentVenueId,
  isPresident,
  isVenueManager,
  isUser,
  hasRole,
  hasManagePermission,
  canManageVenue,
  canEditRecord,
  canDeleteRecord,
  hasPermission,
  PERMISSIONS
}
