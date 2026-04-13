import { IMAGE_BASE_URL } from '@/config/api'

/**
 * Convert backend image paths into URLs that the current client can safely load.
 */
export function resolveImageUrl(url?: string | null) {
  if (!url) return ''

  // 微信小程序不支持普通 HTTP 远程图片，交给调用方回退到占位图。
  // #ifdef MP-WEIXIN
  if (url.startsWith('http://')) {
    return ''
  }
  // #endif

  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  if (url.startsWith('/')) {
    return `${IMAGE_BASE_URL}${url}`
  }

  return `${IMAGE_BASE_URL}/${url}`
}
