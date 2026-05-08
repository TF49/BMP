import { IMAGE_BASE_URL } from '@/config/api'

/**
 * Convert backend image paths into URLs that the current client can safely load.
 */
export function resolveImageUrl(url?: string | null) {
  if (!url) return ''

  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  if (url.startsWith('/')) {
    return `${IMAGE_BASE_URL}${url}`
  }

  return `${IMAGE_BASE_URL}/${url}`
}
