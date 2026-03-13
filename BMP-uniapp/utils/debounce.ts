/**
 * 防抖和节流工具
 */

/**
 * 防抖函数
 * @param func 要执行的函数
 * @param wait 等待时间（毫秒）
 * @param immediate 是否立即执行
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  immediate: boolean = false
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  
  return function(this: any, ...args: Parameters<T>) {
    const context = this
    
    const later = () => {
      timeout = null
      if (!immediate) {
        func.apply(context, args)
      }
    }
    
    const callNow = immediate && !timeout
    
    if (timeout) {
      clearTimeout(timeout)
    }
    
    timeout = setTimeout(later, wait)
    
    if (callNow) {
      func.apply(context, args)
    }
  }
}

/**
 * 节流函数
 * @param func 要执行的函数
 * @param wait 等待时间（毫秒）
 * @param leading 是否在开始时执行
 * @param trailing 是否在结束时执行
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  leading: boolean = true,
  trailing: boolean = true
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous: number = 0
  
  return function(this: any, ...args: Parameters<T>) {
    const context = this
    const now = Date.now()
    
    if (!previous && !leading) {
      previous = now
    }
    
    const remaining = wait - (now - previous)
    
    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(context, args)
    } else if (!timeout && trailing) {
      timeout = setTimeout(() => {
        previous = leading ? Date.now() : 0
        timeout = null
        func.apply(context, args)
      }, remaining)
    }
  }
}
