/**
 * 用户角色
 */
export const USER_ROLES = {
  PRESIDENT: 'PRESIDENT', // 协会会长
  VENUE_MANAGER: 'VENUE_MANAGER', // 场馆管理者
  USER: 'USER', // 普通用户
  MEMBER: 'MEMBER', // 会员
  COACH: 'COACH' // 教练
} as const

/**
 * 预约状态
 */
export const BOOKING_STATUS = {
  CANCELLED: 0, // 已取消
  PENDING_PAYMENT: 1, // 待支付
  PAID: 2, // 已支付
  IN_PROGRESS: 3, // 进行中
  COMPLETED: 4 // 已完成
} as const

export const BOOKING_STATUS_TEXT = {
  [BOOKING_STATUS.CANCELLED]: '已取消',
  [BOOKING_STATUS.PENDING_PAYMENT]: '待支付',
  [BOOKING_STATUS.PAID]: '已支付',
  [BOOKING_STATUS.IN_PROGRESS]: '进行中',
  [BOOKING_STATUS.COMPLETED]: '已完成'
} as const

/**
 * 场地状态
 */
export const COURT_STATUS = {
  MAINTENANCE: 0, // 维护中
  IDLE: 1, // 空闲
  BOOKED: 2, // 预约中
  IN_USE: 3 // 使用中
} as const

export const COURT_STATUS_TEXT = {
  [COURT_STATUS.MAINTENANCE]: '维护中',
  [COURT_STATUS.IDLE]: '空闲',
  [COURT_STATUS.BOOKED]: '预约中',
  [COURT_STATUS.IN_USE]: '使用中'
} as const

/**
 * 场馆状态
 */
export const VENUE_STATUS = {
  CLOSED: 0, // 关闭
  OPEN: 1, // 营业中
  PAUSED: 2 // 暂停
} as const

export const VENUE_STATUS_TEXT = {
  [VENUE_STATUS.CLOSED]: '关闭',
  [VENUE_STATUS.OPEN]: '营业中',
  [VENUE_STATUS.PAUSED]: '暂停'
} as const

/**
 * 支付方式
 */
export const PAYMENT_METHOD = {
  CASH: 'CASH', // 现金
  ALIPAY: 'ALIPAY', // 支付宝
  WECHAT: 'WECHAT', // 微信
  BALANCE: 'BALANCE' // 余额
} as const

export const PAYMENT_METHOD_TEXT = {
  [PAYMENT_METHOD.CASH]: '现金',
  [PAYMENT_METHOD.ALIPAY]: '支付宝',
  [PAYMENT_METHOD.WECHAT]: '微信',
  [PAYMENT_METHOD.BALANCE]: '余额'
} as const

export const BUSINESS_PAYMENT_METHOD = PAYMENT_METHOD.BALANCE

/**
 * 器材类型
 */
export const EQUIPMENT_TYPE = {
  RACKET: 'RACKET', // 球拍
  SHUTTLE: 'SHUTTLE', // 羽毛球
  STRING: 'STRING' // 球线
} as const

export const EQUIPMENT_TYPE_TEXT = {
  [EQUIPMENT_TYPE.RACKET]: '球拍',
  [EQUIPMENT_TYPE.SHUTTLE]: '羽毛球',
  [EQUIPMENT_TYPE.STRING]: '球线'
} as const

/**
 * 赛事类型
 */
export const TOURNAMENT_TYPE = {
  SINGLE: 'SINGLE', // 单打
  DOUBLE: 'DOUBLE', // 双打
  MIXED: 'MIXED', // 混双
  TEAM: 'TEAM' // 团体
} as const

export const TOURNAMENT_TYPE_TEXT = {
  [TOURNAMENT_TYPE.SINGLE]: '单打',
  [TOURNAMENT_TYPE.DOUBLE]: '双打',
  [TOURNAMENT_TYPE.MIXED]: '混双',
  [TOURNAMENT_TYPE.TEAM]: '团体'
} as const

/**
 * 穿线服务状态
 */
export const STRINGING_STATUS = {
  CANCELLED: 0,      // 已取消
  WAITING: 1,        // 等待穿线
  IN_PROGRESS: 2,    // 正在穿线
  COMPLETED: 3       // 已完成
} as const

export const STRINGING_STATUS_TEXT: Record<number, string> = {
  [STRINGING_STATUS.CANCELLED]: '已取消',
  [STRINGING_STATUS.WAITING]: '等待穿线',
  [STRINGING_STATUS.IN_PROGRESS]: '正在穿线',
  [STRINGING_STATUS.COMPLETED]: '已完成'
} as const

export const STRINGING_STATUS_COLOR: Record<number, string> = {
  [STRINGING_STATUS.CANCELLED]: '#999999',
  [STRINGING_STATUS.WAITING]: '#ff9800',
  [STRINGING_STATUS.IN_PROGRESS]: '#2196f3',
  [STRINGING_STATUS.COMPLETED]: '#3cc51f'
} as const

/**
 * 磅数范围
 */
export const TENSION_RANGE = {
  MIN: 18,
  MAX: 35
} as const
