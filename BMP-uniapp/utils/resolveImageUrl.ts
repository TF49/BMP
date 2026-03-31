import { IMAGE_BASE_URL } from '@/config/api'

/**
 * 将后端返回的图片地址解析成小程序可访问的完整 URL。
 *
 * 后端通常返回相对路径，如：/api/uploads/venues/xxx.png
 * 小程序需要拼接域名后才能加载。
 */
export function resolveImageUrl(url?: string | null) {
  if (!url) return ''

  // 已经是完整 http(s) 地址：直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  // 后端相对路径：以 / 开头
  if (url.startsWith('/')) {
    return `${IMAGE_BASE_URL}${url}`
  }

  // 兜底：尝试拼接（避免误传导致空白）
  return `${IMAGE_BASE_URL}/${url}`
}

