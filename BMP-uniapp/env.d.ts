/// <reference types="vite/client" />

/**
 * uni-app 模块类型声明
 * 用于在 @dcloudio/uni-app 未正确解析时提供类型支持
 */
declare module '@dcloudio/uni-app' {
  export function onLaunch(callback: () => void): void
  export function onShow(callback: () => void): void
  export function onHide(callback: () => void): void
}
