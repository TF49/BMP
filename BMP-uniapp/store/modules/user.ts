import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getCurrentUser, refreshToken as refreshTokenApi } from '@/api/auth'
import type { LoginResult, UserInfo } from '@/api/auth'
import { checkAndHandleRole } from '@/utils/roleCheck'
import { safeReLaunch } from '@/utils/safeRoute'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshTokenValue = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => userInfo.value?.role || '')
  const userId = computed(() => userInfo.value?.id || 0)

  const normalizeUserInfo = (user: UserInfo | null) => {
    if (!user) return null

    return {
      ...user,
      avatar: user.avatar ? resolveImageUrl(user.avatar) : ''
    }
  }

  // 方法
  async function doLogin(params: { username: string; password: string }) {
    try {
      const result = await login(params)
      token.value = result.token
      refreshTokenValue.value = result.refreshToken
      const normalizedUser = normalizeUserInfo(result.user)
      userInfo.value = normalizedUser
      
      // 保存到本地存储
      uni.setStorageSync('token', result.token)
      uni.setStorageSync('refreshToken', result.refreshToken)
      uni.setStorageSync('userInfo', normalizedUser)
      
      return result
    } catch (error) {
      throw error
    }
  }

  async function checkLogin() {
    const savedToken = uni.getStorageSync('token')
    if (savedToken) {
      token.value = savedToken
      refreshTokenValue.value = uni.getStorageSync('refreshToken')
      userInfo.value = normalizeUserInfo(uni.getStorageSync('userInfo'))
      
      // 验证Token是否有效
      try {
        const currentUser = await getCurrentUser()
        const normalizedCurrentUser = normalizeUserInfo(currentUser)
        userInfo.value = normalizedCurrentUser
        uni.setStorageSync('userInfo', normalizedCurrentUser)
        
        // 检查用户角色，如果是不允许的角色，清除登录状态并提示
        if (!checkAndHandleRole(currentUser?.role)) {
          logout()
          return
        }
      } catch (error) {
        // Token失效，清除登录状态
        logout()
      }
    }
  }

  async function refreshToken() {
    try {
      const result = await refreshTokenApi(refreshTokenValue.value)
      token.value = result.token
      refreshTokenValue.value = result.refreshToken
      const normalizedUser = normalizeUserInfo(result.user)
      userInfo.value = normalizedUser
      
      uni.setStorageSync('token', result.token)
      uni.setStorageSync('refreshToken', result.refreshToken)
      uni.setStorageSync('userInfo', normalizedUser)
      
      if (!checkAndHandleRole(result.user?.role)) {
        logout()
        setTimeout(() => {
          safeReLaunch('/pages/login/login', '/pages/login/login')
        }, 500)
        throw new Error('该角色不允许使用小程序端')
      }
      
      return result
    } catch (error) {
      logout()
      throw error
    }
  }

  function logout() {
    token.value = ''
    refreshTokenValue.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('userInfo')
  }

  return {
    token,
    refreshTokenValue, // 暴露 refreshToken 值（如果需要访问值）
    userInfo,
    isLoggedIn,
    userRole,
    userId,
    doLogin,
    checkLogin,
    refreshToken, // 暴露 refreshToken 作为方法
    logout
  }
})
