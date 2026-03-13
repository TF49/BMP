import axios from 'axios'
import { ElMessage } from 'element-plus'

// 是否正在刷新Token
let isRefreshing = false
// 等待刷新Token的请求队列
let refreshSubscribers = []

// 添加请求到等待队列
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

// 刷新完成后，执行队列中的请求
function onRefreshed(token) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

// 创建axios实例
const service = axios.create({
  baseURL: process.env.NODE_ENV === 'production' ? '/api' : '', // 生产环境使用/api前缀，开发环境使用代理
  timeout: 30000 // request timeout (30秒，减少超时时间以更快失败)
})

// request拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      // 让每个请求携带token
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// response拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是200，则判断为错误
    if (res.code !== 200) {
      // 50008: 非法token; 50012: 其他客户端登录了; 50014: Token 过期了
      if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
        // 重新登录
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        // 跳转到官网（用户可从官网点「去登录」再登录）
        window.location.href = (window.location.origin + window.location.pathname).replace(/\/$/, '') + '#/site'
      } else {
        // 统一错误提示：避免页面静默失败（例如统计卡片一直为0）
        const msg = res.message || '请求失败'
        try {
          ElMessage.error(msg)
        } catch (e) {
          // 忽略 UI 组件未就绪等异常
          console.error('ElMessage.error failed:', e)
        }
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  async error => {
    const originalRequest = error.config

    // 处理401错误（Token过期）
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      // 如果是刷新Token的请求失败，直接跳转登录页
      if (originalRequest.url && originalRequest.url.includes('/api/auth/refresh')) {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        window.location.href = (window.location.origin + window.location.pathname).replace(/\/$/, '') + '#/site'
        return Promise.reject(error)
      }

      // 标记为重试请求
      originalRequest._retry = true

      const refreshToken = localStorage.getItem('refreshToken')

      // 如果没有refreshToken，跳转官网
      if (!refreshToken) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = (window.location.origin + window.location.pathname).replace(/\/$/, '') + '#/site'
        return Promise.reject(error)
      }

      // 如果正在刷新Token，将请求加入队列
      if (isRefreshing) {
        return new Promise(resolve => {
          subscribeTokenRefresh(token => {
            originalRequest.headers['Authorization'] = 'Bearer ' + token
            resolve(service(originalRequest))
          })
        })
      }

      isRefreshing = true

      try {
        // 调用刷新Token接口
        const response = await axios.post('/api/auth/refresh', {
          refreshToken: refreshToken
        })

        if (response.data && response.data.code === 200) {
          const { token: newToken, refreshToken: newRefreshToken, expiresIn } = response.data.data

          // 保存新Token
          localStorage.setItem('token', newToken)
          if (newRefreshToken) {
            localStorage.setItem('refreshToken', newRefreshToken)
          }
          if (expiresIn) {
            localStorage.setItem('tokenExpiresIn', expiresIn)
          }

          // 通知队列中的请求
          onRefreshed(newToken)

          // 重试原始请求
          originalRequest.headers['Authorization'] = 'Bearer ' + newToken
          return service(originalRequest)
        } else {
          // 刷新失败，跳转官网
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          window.location.href = (window.location.origin + window.location.pathname).replace(/\/$/, '') + '#/site'
          return Promise.reject(error)
        }
      } catch (refreshError) {
        // 刷新Token失败，跳转官网
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        window.location.href = (window.location.origin + window.location.pathname).replace(/\/$/, '') + '#/site'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 处理其他网络错误
    if (error.response) {
      switch (error.response.status) {
        case 403:
          // 拒绝访问，可能是token无效或过期，跳转到官网
          console.error('403 Forbidden: 拒绝访问，请重新登录')
          try { ElMessage.error('拒绝访问，请重新登录') } catch (e) {}
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          window.location.href = (window.location.origin + window.location.pathname).replace(/\/$/, '') + '#/site'
          break
        case 404:
          // 请求资源不存在
          console.error('404 Not Found: 请求的资源不存在', error.config.url)
          try { ElMessage.error('请求的资源不存在') } catch (e) {}
          break
        case 500:
          // 服务器内部错误
          console.error('500 Server Error: 服务器内部错误')
          try { ElMessage.error('服务器内部错误') } catch (e) {}
          break
        default:
          console.error(`HTTP 错误: ${error.response.status}`)
          try { ElMessage.error(`网络错误：${error.response.status}`) } catch (e) {}
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      // 对于某些非关键API（如天气API），静默处理，不显示错误提示
      const isNonCriticalAPI = error.config?.url?.includes('/api/weather')
      
      if (!isNonCriticalAPI) {
        console.error('无响应错误：请检查后端服务是否启动', error.message)
        try { ElMessage.error('无响应：请检查后端服务是否启动') } catch (e) {}
      }
    } else {
      // 断网等情况
      // 对于某些非关键API（如天气API），静默处理
      const isNonCriticalAPI = error.config?.url?.includes('/api/weather')
      
      if (!isNonCriticalAPI) {
        console.error('网络连接异常:', error.message)
        try { ElMessage.error('网络连接异常') } catch (e) {}
      }
    }
    return Promise.reject(error)
  }
)

export default service

// 封装常用的请求方法
export const get = (url, params) => {
  return service({
    url,
    method: 'get',
    params
  })
}

export const post = (url, data) => {
  return service({
    url,
    method: 'post',
    data
  })
}

export const put = (url, data) => {
  return service({
    url,
    method: 'put',
    data
  })
}

export const del = (url) => {
  return service({
    url,
    method: 'delete'
  })
}
