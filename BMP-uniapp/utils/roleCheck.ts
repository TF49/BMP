/**
 * UniApp 角色分流工具
 * 用户端：USER、MEMBER
 * 管理端：PRESIDENT、VENUE_MANAGER
 * 教练端：COACH
 */

export type UserRole = 'USER' | 'MEMBER' | 'PRESIDENT' | 'VENUE_MANAGER' | 'COACH'

const USER_SIDE_ROLES: UserRole[] = ['USER', 'MEMBER']
const MANAGEMENT_SIDE_ROLES: UserRole[] = ['PRESIDENT', 'VENUE_MANAGER']
const COACH_SIDE_ROLES: UserRole[] = ['COACH']

export function isKnownRole(role: string | undefined | null): role is UserRole {
  return [...USER_SIDE_ROLES, ...MANAGEMENT_SIDE_ROLES, ...COACH_SIDE_ROLES].includes((role || '') as UserRole)
}

/**
 * 检查用户角色是否允许进入 UniApp
 */
export function isAllowedRole(role: string | undefined | null): boolean {
  return isUserRole(role) || isManagementRole(role) || isCoachRole(role)
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
 * 是否属于教练端角色
 */
export function isCoachRole(role: string | undefined | null): boolean {
  return COACH_SIDE_ROLES.includes((role || '') as UserRole)
}

/**
 * 是否属于用户端角色
 */
export function isUserRole(role: string | undefined | null): boolean {
  return USER_SIDE_ROLES.includes((role || '') as UserRole)
}

export function getRoleHomePath(role: string | undefined | null): string {
  if (isCoachRole(role)) {
    return '/pages/coach/entry'
  }
  return '/pages/index/index'
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
