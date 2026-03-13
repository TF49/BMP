declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 全局事件总线类型声明
interface EventBus {
  _events: Record<string, Function[]>
  $on(event: string, callback: Function): void
  $off(event: string, callback?: Function): void
  $emit(event: string, ...args: any[]): void
}

declare global {
  interface Window {
    eventBus: EventBus
  }
}

export {}
