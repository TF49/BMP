import { installShowToastSanitizer } from './utils/mp-weixin-showToast-patch'
import { createSSRApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import uviewPlus from 'uview-plus'
import 'uview-plus/index.scss'
import './utils/request'

// ========== 微信小程序 closeSocket code 兜底 ==========
// 微信要求 closeSocket code 只能是 1000 或 3000-4999，部分运行时/框架会错误传入 1006。
// 需要尽早执行，避免在 App.vue 之前就触发报错。
// #ifdef MP-WEIXIN
try {
  const sanitizeCloseCode = (code: any) => (code === 1006 ? 1000 : code)

  const wxAny = (globalThis as any).wx
  if (wxAny && typeof wxAny.closeSocket === 'function') {
    const originalCloseSocket = wxAny.closeSocket
    wxAny.closeSocket = function(options: any = {}) {
      if (options) options.code = sanitizeCloseCode(options.code)
      return originalCloseSocket.call(this, options)
    }
  }

  // 兜底 SocketTask.close（connectSocket 返回对象的 close）
  if (wxAny && typeof wxAny.connectSocket === 'function') {
    const originalConnectSocket = wxAny.connectSocket
    wxAny.connectSocket = function(...args: any[]) {
      const task = originalConnectSocket.apply(this, args)
      try {
        if (task && typeof task.close === 'function') {
          const originalTaskClose = task.close
          task.close = function(arg0: any = {}, ...rest: any[]) {
            // 微信 SocketTask.close 规范是 close({ code, reason })
            if (arg0 && typeof arg0 === 'object') {
              arg0.code = sanitizeCloseCode(arg0.code)
              return originalTaskClose.call(this, arg0, ...rest)
            }
            // 兼容少数实现：close(code, reason)
            if (typeof arg0 === 'number') {
              return originalTaskClose.call(this, sanitizeCloseCode(arg0), ...rest)
            }
            return originalTaskClose.call(this, arg0, ...rest)
          }
        }
      } catch (e) {
        // ignore
      }
      return task
    }
  }

  const uniAny = (globalThis as any).uni
  if (uniAny && typeof uniAny.closeSocket === 'function') {
    const originalUniCloseSocket = uniAny.closeSocket
    uniAny.closeSocket = function(options: any = {}) {
      if (options) options.code = sanitizeCloseCode(options.code)
      return originalUniCloseSocket.call(this, options)
    }
  }

  if (uniAny && typeof uniAny.connectSocket === 'function') {
    const originalUniConnectSocket = uniAny.connectSocket
    uniAny.connectSocket = function(...args: any[]) {
      const task = originalUniConnectSocket.apply(this, args)
      try {
        if (task && typeof task.close === 'function') {
          const originalTaskClose = task.close
          task.close = function(arg0: any = {}, ...rest: any[]) {
            if (arg0 && typeof arg0 === 'object') {
              arg0.code = sanitizeCloseCode(arg0.code)
              return originalTaskClose.call(this, arg0, ...rest)
            }
            if (typeof arg0 === 'number') {
              return originalTaskClose.call(this, sanitizeCloseCode(arg0), ...rest)
            }
            return originalTaskClose.call(this, arg0, ...rest)
          }
        }
      } catch (e) {
        // ignore
      }
      return task
    }
  }

  // 最底层兜底：如果环境暴露了 WebSocket，拦截其 close(code, reason)
  const WebSocketAny = (globalThis as any).WebSocket
  if (WebSocketAny?.prototype && typeof WebSocketAny.prototype.close === 'function') {
    const originalWsClose = WebSocketAny.prototype.close
    WebSocketAny.prototype.close = function(code?: any, reason?: any) {
      return originalWsClose.call(this, sanitizeCloseCode(code), reason)
    }
  }
} catch (e) {
  // ignore
}
// #endif

// ========== 禁用 uni-app 开发模式的 WebSocket 日志功能 ==========
// 原因：微信小程序最多只支持2个同时WebSocket连接
// uni-app的日志功能会占用1个连接，导致应用只能使用1个连接
// 必须在应用初始化时立即禁用，否则框架会自动建立日志连接

// 方法1：设置环境变量（在框架加载前）
if (typeof process !== 'undefined' && process.env) {
  process.env.UNI_CONSOLE_SOCKET_DISABLE = 'true'
  process.env.UNI_APP_CONSOLE_SOCKET_DISABLE = 'true'
  process.env.UNI_CLOUD_LOGSERVE_DISABLE = 'true'
}

// 方法2：直接设置全局变量禁用日志
if (typeof globalThis !== 'undefined') {
  globalThis.__UNI_LOG_DISABLE__ = true
  globalThis.__UNI_CONSOLE_LOG_DISABLE__ = true
}

export function createApp() {
  const app = createSSRApp(App)

  const pinia = createPinia()
  app.use(pinia)
  app.use(uviewPlus)

  // #ifdef MP-WEIXIN
  // 模块首行已 patch 一次；插件安装后若替换了 uni API，再包一层
  installShowToastSanitizer()
  // #endif

  // 在应用初始化后再次尝试禁用日志（双保险）
  app.config.globalProperties.$disableLogging = true

  return {
    app,
    pinia
  }
}
