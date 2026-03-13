type AnyObj = Record<string, any>

function normalizeUrl(url: string): string {
  const trimmed = (url || '').trim()
  if (!trimmed) return ''
  const withLeading = trimmed.startsWith('/') ? trimmed : `/${trimmed}`
  // keep query/hash, but normalize duplicate slashes in path part
  const [path, rest] = withLeading.split(/(?=[?#])/)
  return path.replace(/\/{2,}/g, '/') + (rest || '')
}

function getRegisteredPages(): string[] | null {
  const g = globalThis as any as AnyObj
  const pages = g.__wxConfig?.pages || g.__uniConfig?.pages
  return Array.isArray(pages) ? pages : null
}

function isRegistered(url: string): boolean {
  const pages = getRegisteredPages()
  if (!pages) return true // can't determine -> don't block navigation
  const pathOnly = normalizeUrl(url).split(/[?#]/)[0].replace(/^\//, '')
  return pages.includes(pathOnly)
}

function reLaunchAsync(url: string) {
  return new Promise<void>((resolve, reject) => {
    uni.reLaunch({
      url,
      success: () => resolve(),
      fail: (err: any) => reject(err)
    })
  })
}

function redirectToAsync(url: string) {
  return new Promise<void>((resolve, reject) => {
    uni.redirectTo({
      url,
      success: () => resolve(),
      fail: (err: any) => reject(err)
    })
  })
}

function navigateToAsync(url: string) {
  return new Promise<void>((resolve, reject) => {
    uni.navigateTo({
      url,
      success: () => resolve(),
      fail: (err: any) => reject(err)
    })
  })
}

function errMsgOf(e: unknown): string {
  const anyE = e as any
  return String(anyE?.errMsg || anyE?.message || '')
}

export async function safeReLaunch(url: string, fallbackUrl: string = '/pages/login/login') {
  const target = normalizeUrl(url)
  const fallback = normalizeUrl(fallbackUrl) || '/pages/login/login'

  if (!target) {
    console.error('[safeReLaunch] empty url, fallback ->', fallback)
    try {
      await reLaunchAsync(fallback)
    } catch (e) {
      console.error('[safeReLaunch] fallback reLaunch failed:', fallback, e)
    }
    return
  }

  if (!isRegistered(target)) {
    console.error('[safeReLaunch] url not registered:', target, 'fallback ->', fallback)
    try {
      await reLaunchAsync(fallback)
    } catch (e) {
      console.error('[safeReLaunch] fallback reLaunch failed:', fallback, e)
    }
    return
  }

  try {
    await reLaunchAsync(target)
  } catch (e) {
    console.error('[safeReLaunch] reLaunch failed:', target, e, 'fallback ->', fallback)
    // 某些环境/基础库在 reLaunch 内部会抛出与文件句柄相关的异常（例如 "fd" must be number），
    // 这里做兼容性降级，避免卡在登录页。
    const msg = errMsgOf(e)
    try {
      if (msg.includes('"fd"') || msg.includes('fd') || msg.includes('file descriptor')) {
        await redirectToAsync(target)
        return
      }
    } catch (eRedirect) {
      console.error('[safeReLaunch] redirectTo fallback failed:', target, eRedirect)
      try {
        await navigateToAsync(target)
        return
      } catch (eNav) {
        console.error('[safeReLaunch] navigateTo fallback failed:', target, eNav)
      }
    }
    try {
      await reLaunchAsync(fallback)
    } catch (e2) {
      console.error('[safeReLaunch] fallback reLaunch failed:', fallback, e2)
      // fallback 也失败时，同样尝试降级
      try {
        await redirectToAsync(fallback)
      } catch (e3) {
        console.error('[safeReLaunch] fallback redirectTo failed:', fallback, e3)
      }
    }
  }
}

