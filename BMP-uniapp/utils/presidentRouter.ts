/**
 * 协会会长端路由配置
 * 会长端与用户端完全分离的页面路径与 tabBar 配置
 */

export const PRESIDENT_PAGES = {
  // 会长端“工作台”已回归到首页（index 内按角色渲染 PresidentIndex）
  DASHBOARD: '/pages/index/index',
  USER_LIST: '/pages/president/user/list',
  USER_DETAIL: '/pages/president/user/detail',
  USER_FORM: '/pages/president/user/form',
  VENUE_LIST: '/pages/president/venue/list',
  VENUE_DETAIL: '/pages/president/venue/detail',
  VENUE_FORM: '/pages/president/venue/form',
  FINANCE_LIST: '/pages/president/finance/list',
  FINANCE_DETAIL: '/pages/president/finance/detail',
  FINANCE_RECONCILIATION: '/pages/president/finance/reconciliation',
  FINANCE_AUDIT_LOG: '/pages/president/finance/audit-log',
  COURT_LIST: '/pages/president/court/list',
  COURT_DETAIL: '/pages/president/court/detail',
  COURT_FORM: '/pages/president/court/form',
  BOOKING_LIST: '/pages/president/booking/list',
  BOOKING_DETAIL: '/pages/president/booking/detail',
  MEMBER_LIST: '/pages/president/member/list',
  MEMBER_FORM: '/pages/president/member/form',
  MEMBER_DETAIL: '/pages/president/member/detail',
  MEMBER_RECHARGE: '/pages/president/member/recharge',
  EQUIPMENT_LIST: '/pages/president/equipment/list',
  EQUIPMENT_FORM: '/pages/president/equipment/form',
  EQUIPMENT_DETAIL: '/pages/president/equipment/detail',
  EQUIPMENT_STOCK: '/pages/president/equipment/stock',
  EQUIPMENT_RENTAL_LIST: '/pages/president/equipment-rental/list',
  EQUIPMENT_RENTAL_DETAIL: '/pages/president/equipment-rental/detail',
  EQUIPMENT_RENTAL_FORM: '/pages/president/equipment-rental/form',
  COACH_LIST: '/pages/president/coach/list',
  COACH_DETAIL: '/pages/president/coach/detail',
  COACH_FORM: '/pages/president/coach/form',
  COURSE_LIST: '/pages/president/course/list',
  COURSE_FORM: '/pages/president/course/form',
  COURSE_DETAIL: '/pages/president/course/detail',
  COURSE_BOOKING_LIST: '/pages/president/course-booking/list',
  COURSE_BOOKING_DETAIL: '/pages/president/course-booking/detail',
  TOURNAMENT_LIST: '/pages/president/tournament/list',
  TOURNAMENT_FORM: '/pages/president/tournament/form',
  TOURNAMENT_DETAIL: '/pages/president/tournament/detail',
  TOURNAMENT_REGISTRATION_LIST: '/pages/president/tournament-registration/list',
  TOURNAMENT_REGISTRATION_DETAIL: '/pages/president/tournament-registration/detail',
  STRINGING_LIST: '/pages/president/stringing/list',
  STRINGING_DETAIL: '/pages/president/stringing/detail',
  STRINGING_FORM: '/pages/president/stringing/form',
  NOTIFICATION_LIST: '/pages/president/notification/list',
  NOTIFICATION_FORM: '/pages/president/notification/form',
  NOTIFICATION_DETAIL: '/pages/president/notification/detail',
  PROFILE: '/pages/president/profile/index'
} as const

/** 会长端 tabBar 页面路径（用于判断当前是否在 tabBar 页） */
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

/**
 * 判断路径是否为会长端 tabBar 页面
 */
export function isPresidentTabPage(path: string): boolean {
  return getNativeTabBarPaths().includes(normalizePath(path))
}

/**
 * 判断路径是否为会长端页面
 */
export function isPresidentPage(path: string): boolean {
  return path.includes('/pages/president/')
}
