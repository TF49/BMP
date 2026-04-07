const DEFAULT_AVATAR = '/static/placeholders/avatar.svg'
const DEFAULT_COVER = '/static/placeholders/hero.svg'

const BLOCKED_REMOTE_HOSTS = ['lh3.googleusercontent.com']

function isBlockedRemoteHost(url: string) {
  try {
    const parsed = new URL(url)
    return BLOCKED_REMOTE_HOSTS.includes(parsed.hostname)
  } catch {
    return false
  }
}

export function getDisplayImage(url?: string | null, fallback = DEFAULT_COVER) {
  if (!url) return fallback

  // Mini Program environments are more sensitive to remote image domain reachability.
  // Fall back to bundled assets for known unstable demo hosts.
  // #ifdef MP
  if (url.startsWith('http://') || url.startsWith('https://')) {
    if (isBlockedRemoteHost(url)) {
      return fallback
    }
  }
  // #endif

  return url
}

export function getAvatarImage(url?: string | null) {
  return getDisplayImage(url, DEFAULT_AVATAR)
}

export function getCoverImage(url?: string | null) {
  return getDisplayImage(url, DEFAULT_COVER)
}
