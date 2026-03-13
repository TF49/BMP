/**
 * useIntersectionObserver Composable
 * 用于检测元素是否进入视口，常用于滚动动画
 */

import { ref, onMounted, onUnmounted, type Ref } from 'vue'

export interface UseIntersectionObserverOptions {
  threshold?: number | number[]
  rootMargin?: string
  root?: Element | null
  once?: boolean // 是否只触发一次
}

export function useIntersectionObserver(
  target: Ref<Element | null> | Element | null,
  callback: (isIntersecting: boolean, entry: IntersectionObserverEntry) => void,
  options: UseIntersectionObserverOptions = {}
) {
  const {
    threshold = 0.1,
    rootMargin = '0px',
    root = null,
    once = false
  } = options

  const isIntersecting = ref(false)
  let observer: IntersectionObserver | null = null

  const stop = () => {
    if (observer) {
      observer.disconnect()
      observer = null
    }
  }

  const getElement = (): Element | null => {
    if (target === null) return null
    if (typeof target === 'object' && 'value' in target) return (target as Ref<Element | null>).value
    return target as Element
  }

  const observe = () => {
    const element = getElement()
    if (!element) return

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const intersecting = entry.isIntersecting
          isIntersecting.value = intersecting
          callback(intersecting, entry)

          // 如果设置了 once，且已进入视口，则停止观察
          if (once && intersecting) {
            stop()
          }
        })
      },
      {
        threshold,
        rootMargin,
        root
      }
    )

    observer.observe(element)
  }

  // Ref 需在 onMounted 后才有 value；直接传 Element 也可在 onMounted 时观察
  onMounted(() => {
    observe()
  })

  onUnmounted(() => {
    stop()
  })

  return {
    isIntersecting,
    stop,
    observe
  }
}
