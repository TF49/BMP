/**
 * 加载动画工具
 */

/**
 * 显示加载提示
 * @param title 提示文字，默认为"加载中..."
 * @param mask 是否显示透明蒙层
 */
export function showLoading(title: string = '加载中...', mask: boolean = true): void {
  uni.showLoading({
    title,
    mask
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading(): void {
  uni.hideLoading()
}

/**
 * 显示成功提示
 * @param title 提示文字
 * @param duration 显示时长（毫秒）
 */
export function showSuccess(title: string, duration: number = 2000): void {
  uni.showToast({
    title,
    icon: 'success',
    duration,
    mask: false
  })
}

/**
 * 显示错误提示
 * @param title 提示文字
 * @param duration 显示时长（毫秒）
 */
export function showError(title: string, duration: number = 2000): void {
  uni.showToast({
    title,
    icon: 'none',
    duration,
    mask: false
  })
}

/**
 * 显示普通提示
 * @param title 提示文字
 * @param duration 显示时长（毫秒）
 */
export function showMessage(title: string, duration: number = 2000): void {
  uni.showToast({
    title,
    icon: 'none',
    duration,
    mask: false
  })
}
