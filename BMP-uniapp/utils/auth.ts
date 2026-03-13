import { useUserStore } from '../store/modules/user'
import { safeReLaunch } from './safeRoute'

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export function checkAuth(): boolean {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
      safeReLaunch('/pages/login/login', '/pages/login/login')
    }, 1500)
    return false
  }
  return true
}

/**
 * 检查用户角色权限
 * @param roles 允许的角色数组
 * @returns {boolean} 是否有权限
 */
export function checkRole(roles: string[]): boolean {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    return false
  }
  return roles.includes(userStore.userRole)
}
