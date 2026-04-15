type SafeAreaInsets = {
  top?: number
  right?: number
  bottom?: number
  left?: number
}

export interface SafeSystemInfo {
  statusBarHeight: number
  windowWidth: number
  windowHeight: number
  pixelRatio: number
  safeAreaInsets: SafeAreaInsets
}

function tryCall<T>(fn?: () => T): T | undefined {
  try {
    return typeof fn === 'function' ? fn() : undefined
  } catch {
    return undefined
  }
}

export function getSafeSystemInfo(): SafeSystemInfo {
  const windowInfo = tryCall(() => uni.getWindowInfo())
  const systemInfo = tryCall(() => uni.getSystemInfoSync())

  return {
    statusBarHeight: windowInfo?.statusBarHeight ?? 0,
    windowWidth: windowInfo?.windowWidth ?? 0,
    windowHeight: windowInfo?.windowHeight ?? 0,
    pixelRatio: systemInfo?.pixelRatio ?? 1,
    safeAreaInsets: windowInfo?.safeAreaInsets ?? {}
  }
}
