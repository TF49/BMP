import { computed } from 'vue'

/**
 * 权限相关的composable函数
 * 用于在Vue组件中获取用户角色和权限信息
 */
export function useAuth() {
  /**
   * 获取当前用户角色
   */
  const userRole = computed(() => {
    try {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr && userInfoStr !== 'null') {
        const userInfo = JSON.parse(userInfoStr)
        return userInfo.role || null
      }
    } catch (e) {
      console.error('获取用户角色失败:', e)
    }
    return null
  })

  /**
   * 检查用户是否有指定角色
   * @param {string|string[]} roles - 角色或角色数组
   * @returns {boolean}
   */
  const hasRole = (roles) => {
    const role = userRole.value
    if (!role) return false
    if (Array.isArray(roles)) {
      return roles.includes(role)
    }
    return role === roles
  }

  /**
   * 检查是否为协会会长
   */
  const isPresident = computed(() => userRole.value === 'PRESIDENT')

  /**
   * 检查是否为场馆管理者
   */
  const isVenueManager = computed(() => userRole.value === 'VENUE_MANAGER')

  /**
   * 检查是否为普通用户
   */
  const isUser = computed(() => userRole.value === 'USER')

  /**
   * 检查是否为管理员（协会会长或场馆管理者）
   */
  const isAdmin = computed(() => isPresident.value || isVenueManager.value)

  return {
    userRole,
    hasRole,
    isPresident,
    isVenueManager,
    isUser,
    isAdmin
  }
}
