/**
 * 统一错误处理工具
 * 处理网络错误、业务错误和验证错误
 */

export interface ErrorHandlerOptions {
  /** 是否显示错误提示 */
  showToast?: boolean
  /** 错误提示持续时间（毫秒） */
  duration?: number
  /** 是否在权限不足时返回上一页 */
  navigateBackOnForbidden?: boolean
  /** 是否在未授权时跳转到登录页 */
  navigateToLoginOnUnauthorized?: boolean
}

import { safeReLaunch } from './safeRoute'
import { safeNavigateBack } from './navigation'

/**
 * 统一错误处理函数
 * @param error 错误对象
 * @param context 错误上下文（用于日志记录）
 * @param options 处理选项
 */
export function handleError(
  error: any,
  context: string,
  options: ErrorHandlerOptions = {}
): void {
  const {
    showToast = true,
    duration = 2000,
    navigateBackOnForbidden = true,
    navigateToLoginOnUnauthorized = true
  } = options

  console.error(`${context}失败:`, error)

  // 网络错误处理
  if (error.message?.includes('timeout') || error.message?.includes('超时')) {
    if (showToast) {
      uni.showToast({
        title: '网络连接超时，请重试',
        icon: 'none',
        duration
      })
    }
    return
  }

  if (error.message?.includes('网络') || error.message?.includes('network')) {
    if (showToast) {
      uni.showToast({
        title: '网络不可用，请检查网络连接',
        icon: 'none',
        duration
      })
    }
    return
  }

  // 业务错误处理
  const statusCode = error.statusCode || error.code

  switch (statusCode) {
    case 401:
      // 未授权 - 清除登录状态，跳转到登录页
      if (showToast) {
        uni.showToast({
          title: '登录已过期，请重新登录',
          icon: 'none',
          duration
        })
      }
      if (navigateToLoginOnUnauthorized) {
        setTimeout(() => {
          safeReLaunch('/pages/login/login', '/pages/login/login')
        }, duration)
      }
      break

    case 403:
      // 权限不足
      if (showToast) {
        uni.showToast({
          title: '权限不足',
          icon: 'none',
          duration
        })
      }
      if (navigateBackOnForbidden) {
        setTimeout(() => {
          safeNavigateBack('/pages/index/index')
        }, duration)
      }
      break

    case 404:
      // 资源不存在
      if (showToast) {
        uni.showToast({
          title: error.message || '未找到该服务记录',
          icon: 'none',
          duration
        })
      }
      break

    case 500:
    case 502:
    case 503:
      // 服务器错误
      if (showToast) {
        uni.showToast({
          title: '服务器错误，请稍后重试',
          icon: 'none',
          duration
        })
      }
      break

    default:
      // 默认错误提示
      if (showToast) {
        uni.showToast({
          title: error.message || `${context}失败`,
          icon: 'none',
          duration
        })
      }
  }
}

/**
 * 带重试机制的加载函数
 * @param loadFn 加载函数
 * @param maxRetries 最大重试次数
 * @param retryDelay 重试延迟（毫秒）
 */
export async function loadWithRetry<T>(
  loadFn: () => Promise<T>,
  maxRetries: number = 3,
  retryDelay: number = 1000
): Promise<T> {
  let retries = 0
  let lastError: any

  while (retries < maxRetries) {
    try {
      return await loadFn()
    } catch (error) {
      lastError = error
      retries++
      
      if (retries >= maxRetries) {
        throw lastError
      }
      
      // 等待后重试
      await new Promise(resolve => setTimeout(resolve, retryDelay * retries))
    }
  }

  throw lastError
}

/**
 * 显示重试对话框
 * @param message 错误消息
 * @param onRetry 重试回调
 */
export function showRetryDialog(message: string, onRetry: () => void): void {
  uni.showModal({
    title: '提示',
    content: message,
    confirmText: '重试',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        onRetry()
      }
    }
  })
}
