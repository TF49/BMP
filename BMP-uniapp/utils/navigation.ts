/**
 * 安全的返回导航函数
 * 检查页面栈深度，如果在第一页则导航到首页
 * @param fallbackUrl 当无法返回时的跳转地址，默认为首页
 */
export const safeNavigateBack = (fallbackUrl: string = '/pages/index/index') => {
  const pages = getCurrentPages() as unknown as Array<{ route?: string }>
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    // 首页/Tab 页优先使用 switchTab，避免首屏 navigateBack 报错
    uni.switchTab({
      url: fallbackUrl,
      fail: () => {
        uni.reLaunch({
          url: fallbackUrl
        })
      }
    })
  }
}
