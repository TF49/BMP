// 屏蔽 Element Plus 动态布局组件（el-table / el-dialog）触发的 ResizeObserver 循环警告。
// 该警告在 Webpack Dev Server 下会以全屏 Overlay 形式遮挡页面，但不影响实际功能。
const _origOnError = window.onerror
window.onerror = function (message, source, lineno, colno, error) {
  if (
    typeof message === 'string' &&
    message.includes('ResizeObserver loop completed with undelivered notifications')
  ) {
    return true // 拦截，不向 Overlay 上报
  }
  return _origOnError ? _origOnError.call(this, message, source, lineno, colno, error) : false
}

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/uiverse/index.css'
import '@/styles/site.css'
import '@/styles/responsive.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { setupPermissionDirective } from '@/directives/permission'

// 创建应用实例
const app = createApp(App)

// 创建全局事件总线
window.eventBus = app.config.globalProperties.$bus = {
  _events: {},
  $on(event: string, callback: Function) {
    if (!this._events[event]) this._events[event] = []
    this._events[event].push(callback)
  },
  $off(event: string, callback?: Function) {
    if (!this._events[event]) return
    if (callback) {
      this._events[event] = this._events[event].filter((cb: Function) => cb !== callback)
    } else {
      this._events[event] = []
    }
  },
  $emit(event: string, ...args: any[]) {
    if (!this._events[event]) return
    this._events[event].forEach((callback: Function) => callback(...args))
  }
}

// 使用Element Plus
app.use(ElementPlus)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
setupPermissionDirective(app)

// 使用路由
app.use(router)

// 挂载应用
app.mount('#app')
