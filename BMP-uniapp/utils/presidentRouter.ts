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
  COURT_LIST: '/pages/president/court/list',
  MEMBER_LIST: '/pages/president/member/list',
  EQUIPMENT_LIST: '/pages/president/equipment/list',
  EQUIPMENT_RENTAL_LIST: '/pages/president/equipment-rental/list',
  COACH_LIST: '/pages/president/coach/list',
  COURSE_LIST: '/pages/president/course/list',
  COURSE_BOOKING_LIST: '/pages/president/course-booking/list',
  TOURNAMENT_LIST: '/pages/president/tournament/list',
  TOURNAMENT_REGISTRATION_LIST: '/pages/president/tournament-registration/list',
  STRINGING_LIST: '/pages/president/stringing/list',
  NOTIFICATION_LIST: '/pages/president/notification/list',
  NOTIFICATION_FORM: '/pages/president/notification/form',
  PROFILE: '/pages/president/profile/index'
} as const

/** 会长端 tabBar 页面路径（用于判断当前是否在 tabBar 页） */
export const PRESIDENT_TAB_PATHS = [
  'pages/index/index',
  'pages/president/user/list',
  'pages/president/venue/list',
  'pages/president/finance/list',
  'pages/president/profile/index'
] as const

/**
 * 判断路径是否为会长端 tabBar 页面
 */
export function isPresidentTabPage(path: string): boolean {
  return PRESIDENT_TAB_PATHS.some(p => path.includes(p))
}

/**
 * 判断路径是否为会长端页面
 */
export function isPresidentPage(path: string): boolean {
  return path.includes('/pages/president/')
}
