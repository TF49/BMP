// 必须最先导入错误抑制器，在任何其他代码之前执行
import './utils/mp-weixin-error-suppressor'
// 必须最先导入 WebSocket 补丁，在任何其他代码之前执行
import './utils/mp-weixin-websocket-patch'
import { installShowToastSanitizer } from './utils/mp-weixin-showToast-patch'
import { createSSRApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import uviewPlus from 'uview-plus'
import 'uview-plus/index.scss'
import './utils/request'

// Disable uni-app dev log sockets in WeChat DevTools to reduce noisy internal connection errors.
if (typeof process !== 'undefined' && process.env) {
  process.env.UNI_CONSOLE_SOCKET_DISABLE = 'true'
  process.env.UNI_APP_CONSOLE_SOCKET_DISABLE = 'true'
  process.env.UNI_CLOUD_LOGSERVE_DISABLE = 'true'
}

if (typeof globalThis !== 'undefined') {
  ;(globalThis as any).__UNI_LOG_DISABLE__ = true
  ;(globalThis as any).__UNI_CONSOLE_LOG_DISABLE__ = true
}

export function createApp() {
  const app = createSSRApp(App)

  const pinia = createPinia()
  app.use(pinia)
  app.use(uviewPlus)

  // #ifdef MP-WEIXIN
  installShowToastSanitizer()
  // #endif

  app.config.globalProperties.$disableLogging = true

  return {
    app,
    pinia
  }
}
