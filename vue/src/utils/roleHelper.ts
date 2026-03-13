/**
 * 角色名称映射
 */
export const getRoleName = (role: string): string => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'COACH': '教练',
    'MEMBER': '会员',
    'USER': '普通用户',
    'PRESIDENT': '协会会长',
    'VENUE_MANAGER': '场馆管理者'
  }
  return roleMap[role] || role
}

/**
 * 角色标签类型映射
 */
export const getRoleType = (role: string): string => {
  const typeMap: Record<string, string> = {
    'ADMIN': 'danger',
    'COACH': 'warning',
    'MEMBER': 'success',
    'USER': 'info',
    'PRESIDENT': 'danger',
    'VENUE_MANAGER': 'warning'
  }
  return typeMap[role] || 'info'
}
