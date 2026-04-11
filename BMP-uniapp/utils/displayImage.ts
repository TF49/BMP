const DEFAULT_AVATAR = '/static/placeholders/avatar.svg'
const DEFAULT_COVER = '/static/placeholders/hero.svg'

/** Google 用户内容图床（lh3/lh4 等子域）在无 Google 访问时不可用 */
function isGoogleUserContentHost(hostname: string): boolean {
  const h = hostname.toLowerCase()
  return h === 'googleusercontent.com' || h.endsWith('.googleusercontent.com')
}

function isBlockedRemoteHost(url: string) {
  try {
    return isGoogleUserContentHost(new URL(url).hostname)
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
