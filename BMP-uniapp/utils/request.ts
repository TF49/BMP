import { useUserStore } from '../store/modules/user'
import { API_BASE_URL, REQUEST_TIMEOUT } from '../config/api'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  needAuth?: boolean
}

function reportDebug(_payload: Record<string, unknown>) {}

function sanitizeRequestData<T>(payload: T): T {
  if (payload === undefined) return undefined as T
  if (payload === null) return payload
  if (Array.isArray(payload)) {
    return payload
      .map((item) => sanitizeRequestData(item))
      .filter((item) => item !== undefined) as T
  }
  if (typeof payload === 'object') {
    return Object.fromEntries(
      Object.entries(payload as Record<string, unknown>)
        .map(([key, value]) => [key, sanitizeRequestData(value)])
        .filter(([, value]) => value !== undefined)
    ) as T
  }
  return payload
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
      data: sanitizeRequestData(options.data),
      header,
      timeout: REQUEST_TIMEOUT,
      success: (res) => {
        const { statusCode, data } = res
        // #region agent log
        if (statusCode !== 200) {
          reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H5',location:'utils/request.ts:request:non200',message:'non-200 response received',data:{url:options.url,statusCode},timestamp:Date.now()})
        }
        // #endregion

        // 对于认证接口：不进行自动跳转处理，但仍需做统一的业务 code 处理，保证调用方可通过 catch 处理错误
        if (isAuth) {
          if (statusCode === 200) {
            const result = data as any
            if (result.code === 200) {
              resolve(result.data as any)
            } else {
              const errorMessage = result.message || result.msg || '请求失败'
              reject(new Error(errorMessage))
            }
          } else {
            reject(new Error('网络请求失败'))
          }
          return
        }

        // 处理 HTTP 状态码错误（非认证接口）
        if (statusCode === 403) {
          console.error('API 请求被拒绝 (403)，当前账号无权访问该资源')
          const errorMessage = (data as any)?.message || (data as any)?.msg || '权限不足'
          uni.showToast({
            title: errorMessage,
            icon: 'none'
          })
          reject(new Error(errorMessage))
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
        // 过滤掉微信小程序框架内部的 timeout 错误（不是真正的请求超时）
        const errMsg = err?.errMsg || ''
        const isFrameworkTimeout = errMsg.includes('timeout') && !errMsg.includes('request:fail')
        
        if (isFrameworkTimeout) {
          // 这是微信框架内部的超时，不是真正的请求失败，静默处理
          console.warn('[Request] 微信框架内部超时（非请求失败）:', options.url)
          reject(new Error('framework_timeout'))
          return
        }

        console.error('网络请求错误:', err)
        // #region agent log
        reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H5',location:'utils/request.ts:request:fail',message:'uni.request failed',data:{url:options.url,errMsg:err?.errMsg||''},timestamp:Date.now()})
        // #endregion
        let errorMessage = '网络请求失败，请检查网络连接'
        
        if (errMsg) {
          if (errMsg.includes('502') || errMsg.includes('Bad Gateway')) {
            errorMessage = '服务器连接失败，请检查后端服务是否正常运行'
          } else if (errMsg.includes('request:fail timeout')) {
            errorMessage = '请求超时，请检查网络连接或服务器状态'
          } else if (errMsg.includes('fail')) {
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
    url,
    method: 'GET',
    data,
    ...options
  })
}

// POST请求
export function post<T = any>(url: string, data: any = {}, options: Partial<RequestOptions> = {}): Promise<T> {
  return request<T>({
    url,
    method: 'POST',
    data,
    ...options
  })
}

// PUT请求
export function put<T = any>(url: string, data: any = {}, options: Partial<RequestOptions> = {}): Promise<T> {
  return request<T>({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

// DELETE请求
export function del<T = any>(url: string, data: any = {}, options: Partial<RequestOptions> = {}): Promise<T> {
  return request<T>({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}
