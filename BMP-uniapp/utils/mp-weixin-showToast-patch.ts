/** 指向真正的原生 showToast，避免多次 install 叠代理后链路过长 */
const BMP_NATIVE_SHOW_TOAST = Symbol('bmpNativeShowToast')

const WX_TOAST_ICONS = ['success', 'error', 'loading', 'none'] as const

/**
 * 微信小程序：showToast 若 duration 等数值位为 undefined，或混入 uni 专有字段，底层桥接可能报错：
 * The "fd" argument must be of type number. Received undefined。
 * 只向底层传递微信支持的字段，并强制 duration 为合法正整数。
 */
export function sanitizeShowToastOptions(opts: any): Record<string, any> {
  const src = opts != null && typeof opts === 'object' && !Array.isArray(opts) ? opts : null

  const out: Record<string, any> = {}

  for (const k of ['success', 'fail', 'complete'] as const) {
    if (src && typeof (src as any)[k] === 'function') {
      out[k] = (src as any)[k]
    }
  }

  let title: string
  if (src) {
    const t = (src as any).title
    if (t == null || String(t).trim() === '') {
      title = '\u3000'
    } else {
      title = String(t)
    }
  } else if (opts == null) {
    title = '提示'
  } else {
    title = String(opts)
  }
  out.title = title

  const rawIcon = src ? (src as any).icon : undefined
  out.icon = WX_TOAST_ICONS.includes(rawIcon) ? rawIcon : 'none'

  if (src) {
    const img = (src as any).image
    if (typeof img === 'string' && img.length > 0) {
      out.image = img
    }
  }

  let d = src ? (src as any).duration : undefined
  if (typeof d === 'string' && d.trim() !== '' && !Number.isNaN(Number(d))) {
    d = Number(d)
  }
  if (typeof d !== 'number' || !Number.isFinite(d)) {
    d = 2000
  }
  const ms = Math.round(d)
  out.duration = Math.min(10000, Math.max(1500, ms))

  if (src && (src as any).mask === true) {
    out.mask = true
  }

  return out
}

export function installShowToastSanitizer() {
  // 非微信小程序环境无需处理。
  // #ifndef MP-WEIXIN
  return
  // #endif

  const wrap = (target: { showToast?: (opts: any) => void } | null | undefined) => {
    if (!target || typeof target.showToast !== 'function') return
    const current = target.showToast as any
    const native: (opts: any) => any =
      current[BMP_NATIVE_SHOW_TOAST] || current.bind(target)
    target.showToast = function (opts: any) {
      return native(sanitizeShowToastOptions(opts))
    } as any
    ;(target.showToast as any)[BMP_NATIVE_SHOW_TOAST] = native
  }
  wrap((globalThis as any).uni)
  wrap((globalThis as any).wx)
}

installShowToastSanitizer()
