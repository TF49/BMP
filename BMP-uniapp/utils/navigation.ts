/**
 * 安全的返回导航函数
 * 检查页面栈深度，如果在第一页则导航到首页
 * @param fallbackUrl 当无法返回时的跳转地址，默认为首页
 */
function normalizePath(path: string): string {
  return path.replace(/^\//, '').split('?')[0]
}

function getNativeTabBarPaths(): string[] {
  const globalConfig = globalThis as {
    __uniConfig?: { tabBar?: { list?: Array<{ pagePath?: string }> } }
    __wxConfig?: { tabBar?: { list?: Array<{ pagePath?: string }> } }
  }
  const tabBarList =
    globalConfig.__uniConfig?.tabBar?.list || globalConfig.__wxConfig?.tabBar?.list || []

  return tabBarList
    .map((item) => normalizePath(item?.pagePath || ''))
    .filter(Boolean)
}

function isTabBarPath(path: string): boolean {
  const normalized = normalizePath(path)
  return getNativeTabBarPaths().includes(normalized)
}

function reportDebug(_payload: Record<string, unknown>) {}

export const safeNavigateBack = (fallbackUrl: string = '/pages/index/index') => {
  const pages = getCurrentPages() as unknown as Array<{ route?: string }>
  const currentRoute = pages[pages.length - 1]?.route || null
  const useSwitchTab = isTabBarPath(fallbackUrl)

  // #region agent log
  reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H1',location:'utils/navigation.ts:safeNavigateBack:entry',message:'safeNavigateBack called',data:{fallbackUrl,pagesLength:pages.length,currentRoute,useSwitchTab},timestamp:Date.now()})
  // #endregion

  if (pages.length > 1) {
    // #region agent log
    reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H2',location:'utils/navigation.ts:safeNavigateBack:navigateBack',message:'using navigateBack due to stack depth',data:{fallbackUrl,pagesLength:pages.length,currentRoute},timestamp:Date.now()})
    // #endregion
    uni.navigateBack()
    return
  }

  if (useSwitchTab) {
    // #region agent log
    reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H3',location:'utils/navigation.ts:safeNavigateBack:switchTabAttempt',message:'attempting switchTab on tabBar fallback',data:{fallbackUrl,pagesLength:pages.length,currentRoute},timestamp:Date.now()})
    // #endregion
    uni.switchTab({
      url: fallbackUrl,
      success: () => {
        // #region agent log
        reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H4',location:'utils/navigation.ts:safeNavigateBack:switchTabSuccess',message:'switchTab success',data:{fallbackUrl,currentRoute},timestamp:Date.now()})
        // #endregion
      },
      fail: (error) => {
        // #region agent log
        reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H3',location:'utils/navigation.ts:safeNavigateBack:switchTabFail',message:'switchTab failed and fallback to reLaunch',data:{fallbackUrl,currentRoute,error},timestamp:Date.now()})
        // #endregion
        uni.reLaunch({
          url: fallbackUrl
        })
      }
    })
    return
  }

  // #region agent log
  reportDebug({sessionId:'dd076f',runId:'post-fix',hypothesisId:'H3',location:'utils/navigation.ts:safeNavigateBack:reLaunchDirect',message:'non-tab fallback uses reLaunch directly',data:{fallbackUrl,currentRoute,pagesLength:pages.length},timestamp:Date.now()})
  // #endregion
  uni.reLaunch({
    url: fallbackUrl
  })
}
