/**
 * 角色权限检查工具
 * 小程序端允许普通用户（USER）和协会会长（PRESIDENT）使用；
 * 场馆管理员（VENUE_MANAGER）请使用网页端。
 */

export type UserRole = 'USER' | 'PRESIDENT' | 'VENUE_MANAGER'

/**
 * 检查用户角色是否允许使用小程序端（USER 或 PRESIDENT）
 * @param role 用户角色
 * @returns 是否允许
 */
export function isAllowedRole(role: string | undefined | null): boolean {
  return role === 'USER' || role === 'PRESIDENT'
}

/**
 * 是否为协会会长角色（会长端入口）
 * @param role 用户角色
 * @returns 是否为会长
 */
export function isPresidentRole(role: string | undefined | null): boolean {
  return role === 'PRESIDENT'
}

/**
 * 是否为普通用户角色（用户端入口）
 * @param role 用户角色
 * @returns 是否为普通用户
 */
export function isUserRole(role: string | undefined | null): boolean {
  return role === 'USER' || role === 'MEMBER'
}

/**
 * 获取角色中文名称
 * @param role 用户角色
 * @returns 角色中文名称
 */
export function getRoleName(role: string | undefined | null): string {
  const roleMap: Record<string, string> = {
    USER: '普通用户',
    PRESIDENT: '协会会长',
    VENUE_MANAGER: '场馆管理员'
  }
  return roleMap[role || ''] || '未知角色'
}

/**
 * 检查并处理不允许的角色
 * @param role 用户角色
 * @returns 是否允许，如果不允许会显示提示并返回false
 */
export function checkAndHandleRole(role: string | undefined | null): boolean {
  if (isAllowedRole(role)) {
    return true
  }
  
  const roleName = getRoleName(role)
  uni.showModal({
    title: '提示',
    content: `抱歉，${roleName}账号请使用网页端管理系统登录。\n\n小程序端仅支持普通用户使用，感谢您的理解！`,
    showCancel: false,
    confirmText: '我知道了',
    confirmColor: '#3cc51f'
  })
  
  return false
}
