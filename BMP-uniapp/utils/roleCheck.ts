/**
 * UniApp 角色分流工具
 * 用户端：USER、MEMBER
 * 管理端：PRESIDENT、VENUE_MANAGER
 * 教练端：COACH（本轮暂未开放）
 */

export type UserRole = 'USER' | 'MEMBER' | 'PRESIDENT' | 'VENUE_MANAGER' | 'COACH'

const USER_SIDE_ROLES: UserRole[] = ['USER', 'MEMBER']
const MANAGEMENT_SIDE_ROLES: UserRole[] = ['PRESIDENT', 'VENUE_MANAGER']
const TEMPORARILY_BLOCKED_ROLES: UserRole[] = ['COACH']

export function isKnownRole(role: string | undefined | null): role is UserRole {
  return [...USER_SIDE_ROLES, ...MANAGEMENT_SIDE_ROLES, ...TEMPORARILY_BLOCKED_ROLES].includes((role || '') as UserRole)
}

/**
 * 检查用户角色是否允许进入 UniApp
 */
export function isAllowedRole(role: string | undefined | null): boolean {
  return isUserRole(role) || isManagementRole(role)
}

/**
 * 是否属于移动管理端角色
 */
export function isManagementRole(role: string | undefined | null): boolean {
  return MANAGEMENT_SIDE_ROLES.includes((role || '') as UserRole)
}

/**
 * 是否为协会会长角色
 */
export function isPresidentRole(role: string | undefined | null): boolean {
  return role === 'PRESIDENT'
}

/**
 * 是否属于用户端角色
 */
export function isUserRole(role: string | undefined | null): boolean {
  return USER_SIDE_ROLES.includes((role || '') as UserRole)
}

/**
 * 是否为当前轮暂不开放的角色
 */
export function isTemporarilyBlockedRole(role: string | undefined | null): boolean {
  return TEMPORARILY_BLOCKED_ROLES.includes((role || '') as UserRole)
}

/**
 * 根据角色返回 UniApp 首页
 */
export function getRoleHomePath(role: string | undefined | null): string {
  return isManagementRole(role) ? '/pages/index/index' : '/pages/index/index'
}

/**
 * 获取角色中文名称
 */
export function getRoleName(role: string | undefined | null): string {
  const roleMap: Record<UserRole, string> = {
    USER: '普通用户',
    MEMBER: '会员',
    PRESIDENT: '协会会长',
    VENUE_MANAGER: '场馆管理员',
    COACH: '教练'
  }
  return roleMap[(role || '') as UserRole] || '未知角色'
}

function getRoleBlockMessage(role: string | undefined | null): string {
  if (isTemporarilyBlockedRole(role)) {
    return '教练端移动入口暂未开放，请使用 Web 端或等待后续开放。'
  }

  if (!role || !isKnownRole(role)) {
    return '当前账号角色异常，请联系管理员核对账号权限后再尝试登录。'
  }

  return '当前账号角色暂不支持进入移动端，请联系管理员核对账号权限。'
}

/**
 * 检查并处理不允许的角色
 */
export function checkAndHandleRole(role: string | undefined | null): boolean {
  if (isAllowedRole(role)) {
    return true
  }

  const roleName = getRoleName(role)
  uni.showModal({
    title: '提示',
    content: `抱歉，${roleName}账号当前无法进入移动端。\n\n${getRoleBlockMessage(role)}`,
    showCancel: false,
    confirmText: '我知道了',
    confirmColor: '#3cc51f'
  })

  return false
}
