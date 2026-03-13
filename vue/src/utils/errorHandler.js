/**
 * 统一错误处理工具
 */

import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 处理API错误
 * @param {Error} error 错误对象
 * @param {Object} options 配置选项
 */
export function handleApiError(error, options = {}) {
  const {
    showMessage = true,
    defaultMessage = '操作失败，请稍后重试',
    onError = null
  } = options

  let errorMessage = defaultMessage

  // 处理不同类型的错误
  if (error.response) {
    // HTTP错误响应
    const { status, data } = error.response
    
    switch (status) {
      case 400:
        errorMessage = data?.message || data?.msg || '请求参数错误'
        break
      case 401:
        errorMessage = '登录已过期，请重新登录'
        // 可以在这里触发登出逻辑
        break
      case 403:
        errorMessage = '没有权限执行此操作'
        break
      case 404:
        errorMessage = '请求的资源不存在'
        break
      case 500:
        errorMessage = data?.message || data?.msg || '服务器内部错误'
        break
      case 502:
        errorMessage = '网关错误，请稍后重试'
        break
      case 503:
        errorMessage = '服务暂时不可用'
        break
      default:
        errorMessage = data?.message || data?.msg || `请求失败 (${status})`
    }
  } else if (error.request) {
    // 请求已发出但没有收到响应
    errorMessage = '网络连接失败，请检查网络'
  } else {
    // 其他错误
    errorMessage = error.message || defaultMessage
  }

  // 显示错误消息
  if (showMessage) {
    ElMessage.error(errorMessage)
  }

  // 执行自定义错误处理
  if (onError && typeof onError === 'function') {
    onError(error, errorMessage)
  }

  // 返回错误信息供调用者使用
  return errorMessage
}

/**
 * 处理业务错误
 * @param {Object} response 响应对象
 * @param {Object} options 配置选项
 */
export function handleBusinessError(response, options = {}) {
  const {
    showMessage = true,
    defaultMessage = '操作失败'
  } = options

  if (response.code !== 200) {
    const errorMessage = response.message || response.msg || defaultMessage
    
    if (showMessage) {
      ElMessage.error(errorMessage)
    }
    
    return errorMessage
  }

  return null
}

/**
 * 确认对话框
 * @param {string} message 消息内容
 * @param {string} title 标题
 * @param {Object} options 配置选项
 */
export async function confirmAction(message, title = '提示', options = {}) {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    })
    return true
  } catch {
    return false
  }
}

/**
 * 成功提示
 * @param {string} message 消息内容
 */
export function showSuccess(message) {
  ElMessage.success(message)
}

/**
 * 警告提示
 * @param {string} message 消息内容
 */
export function showWarning(message) {
  ElMessage.warning(message)
}

/**
 * 信息提示
 * @param {string} message 消息内容
 */
export function showInfo(message) {
  ElMessage.info(message)
}
