import { useUserStore } from '../store/modules/user'
import { API_BASE_URL } from '../config/api'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  needAuth?: boolean
}

/**
 * 判断是否是认证相关接口（登录/注册等），这些接口不需要token，也不应该自动跳转
 */
function isAuthRequest(url: string): boolean {
  return !!(
    url.includes('/auth/login') ||
    url.includes('/auth/register') ||
    url.includes('/auth/refresh') ||
    url.includes('/auth/reset-password') ||
    url.includes('/auth/forgot-password')
  )
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    const userStore = useUserStore()
    const token = uni.getStorageSync('token') || ''
    const isAuth = isAuthRequest(options.url)

    // 构建请求头
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
      ...options.header
    }

    // 添加认证Token（非认证接口且需要认证时）
    if (options.needAuth !== false && !isAuth && token) {
      header['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: API_BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: (res) => {
        const { statusCode, data } = res

        // 对于认证接口，直接返回响应，不进行自动跳转处理（参考CMS实现）
        if (isAuth) {
          resolve(data as any)
          return
        }

        // 处理 HTTP 状态码错误（非认证接口）
        if (statusCode === 403) {
          console.error('API 请求被拒绝 (403)，可能是权限不足或 token 无效')
          userStore.logout()
          uni.showToast({
            title: '权限不足，请重新登录',
            icon: 'none'
          })
          setTimeout(() => {
            // 检查当前页面，避免重复跳转
            const pages = getCurrentPages()
            const currentPage = pages[pages.length - 1]
            if (currentPage && currentPage.route !== 'pages/login/login') {
              uni.redirectTo({
                url: '/pages/login/login'
              })
            }
          }, 1500)
          reject(new Error('权限不足'))
          return
        }

        if (statusCode === 200) {
          const result = data as any
          if (result.code === 200) {
            resolve(result.data)
          } else if (result.code === 401 || result.code === '401') {
            // Token过期
            userStore.logout()
            uni.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none'
            })
            setTimeout(() => {
              const pages = getCurrentPages()
              const currentPage = pages[pages.length - 1]
              if (currentPage && currentPage.route !== 'pages/login/login') {
                uni.redirectTo({
                  url: '/pages/login/login'
                })
              }
            }, 1500)
            reject(new Error('登录已过期'))
          } else {
            // 业务错误
            const errorMessage = result.message || result.msg || '请求失败'
            uni.showToast({
              title: errorMessage,
              icon: 'none'
            })
            reject(new Error(errorMessage))
          }
        } else if (statusCode === 401) {
          // HTTP 401
          userStore.logout()
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          setTimeout(() => {
            const pages = getCurrentPages()
            const currentPage = pages[pages.length - 1]
            if (currentPage && currentPage.route !== 'pages/login/login') {
              uni.redirectTo({
                url: '/pages/login/login'
              })
            }
          }, 1500)
          reject(new Error('未授权'))
        } else {
          // 其他HTTP错误
          let errorMessage = '网络请求失败'
          if (statusCode === 502) {
            errorMessage = '服务器连接失败，请检查后端服务是否正常运行'
          } else if (statusCode === 503) {
            errorMessage = '服务暂时不可用，请稍后重试'
          } else if (statusCode === 504) {
            errorMessage = '请求超时，请检查网络连接'
          } else if (statusCode === 500) {
            errorMessage = (data as any)?.message || (data as any)?.msg || '服务器内部错误，请联系管理员'
          } else if (statusCode === 404) {
            errorMessage = '请求的资源不存在'
          }
          uni.showToast({
            title: errorMessage,
            icon: 'none'
          })
          reject(new Error(errorMessage))
        }
      },
      fail: (err) => {
        console.error('网络请求错误:', err)
        let errorMessage = '网络请求失败，请检查网络连接'
        
        if (err.errMsg) {
          if (err.errMsg.includes('502') || err.errMsg.includes('Bad Gateway')) {
            errorMessage = '服务器连接失败，请检查后端服务是否正常运行'
          } else if (err.errMsg.includes('timeout')) {
            errorMessage = '请求超时，请检查网络连接或服务器状态'
          } else if (err.errMsg.includes('fail')) {
            errorMessage = '网络请求失败，请检查网络连接和后端服务'
          }
        }

        // 对于认证接口，不显示全局 toast，让页面自己处理
        if (!isAuth) {
          uni.showToast({
            title: errorMessage,
            icon: 'none',
            duration: 2000
          })
        }

        reject(new Error(errorMessage))
      }
    })
  })
}

// GET请求
export function get<T = any>(url: string, data: any = {}, options: Partial<RequestOptions> = {}): Promise<T> {
  return request<T>({
    ...options,
    url,
    method: 'GET',
    data
  })
}

// POST请求
export function post<T = any>(url: string, data: any = {}, options: Partial<RequestOptions> = {}): Promise<T> {
  return request<T>({
    ...options,
    url,
    method: 'POST',
    data
  })
}

// PUT请求
export function put<T = any>(url: string, data: any = {}, options: Partial<RequestOptions> = {}): Promise<T> {
  return request<T>({
    ...options,
    url,
    method: 'PUT',
    data
  })
}

// DELETE请求
export function del<T = any>(url: string, data: any = {}, options: Partial<RequestOptions> = {}): Promise<T> {
  return request<T>({
    ...options,
    url,
    method: 'DELETE',
    data
  })
}
