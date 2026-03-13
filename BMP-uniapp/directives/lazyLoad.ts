/**
 * 图片懒加载指令
 * 使用方式: <image v-lazy-load="imageUrl" />
 */

import type { DirectiveBinding } from 'vue'

interface LazyLoadElement extends HTMLElement {
  _lazyLoadHandler?: IntersectionObserverCallback
}

let observer: IntersectionObserver | null = null

const initObserver = () => {
  if (observer) return

  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const img = entry.target as any
        const src = img.dataset.src

        if (src) {
          img.src = src
          img.removeAttribute('data-src')
          observer?.unobserve(img)
        }
      }
    })
  }, {
    rootMargin: '50px'
  })
}

export const lazyLoadDirective = {
  mounted(el: LazyLoadElement, binding: DirectiveBinding<string>) {
    initObserver()

    if (binding.value) {
      el.dataset.src = binding.value
      // 设置占位图
      el.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect fill="%23f0f0f0" width="100" height="100"/%3E%3C/svg%3E'
      observer?.observe(el)
    }
  },

  updated(el: LazyLoadElement, binding: DirectiveBinding<string>) {
    if (binding.value && binding.value !== binding.oldValue) {
      el.dataset.src = binding.value
      observer?.observe(el)
    }
  },

  unmounted(el: LazyLoadElement) {
    observer?.unobserve(el)
  }
}

export default lazyLoadDirective
