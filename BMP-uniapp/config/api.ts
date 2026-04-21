import { config } from './env'

export const API_BASE_URL = config.BASE_URL
export const IMAGE_BASE_URL = config.IMAGE_URL

// 导出config供其他模块使用
export { config }

// API路径常量
export const API_PATHS = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    CURRENT: '/auth/current',
    REFRESH: '/auth/refresh',
    LOGOUT: '/auth/logout',
    UPDATE_INFO: '/auth/update',
    UPDATE_PASSWORD: '/auth/change-password',
    SEND_CODE: '/auth/send-code',
    UPDATE_PHONE: '/auth/update-phone',
    SUBMIT_FEEDBACK: '/auth/feedback'
  },
  // 场馆相关
  VENUE: {
    LIST: '/venue/list',
    DETAIL: '/venue/info',
    IMAGES: '/venue',
    SCHEDULES: '/venue'
  },
  // 场地相关
  COURT: {
    LIST: '/court/list',
    DETAIL: '/court/info',
    ADD: '/court/add',
    UPDATE: '/court/update',
    DELETE: '/court',
    STATUS: '/court/status',
    VENUES: '/court/venues',
    TODAY_BOOKINGS: '/court'
  },
  // 预约相关
  BOOKING: {
    LIST: '/booking/list',
    DETAIL: '/booking/info',
    ADD: '/booking/add',
    UPDATE: '/booking/update',
    DELETE: '/booking',
    STATUS: '/booking/status',
    PAYMENT: '/booking/payment',
    MEMBER_PAYMENT: '/booking/member/payment',
    REFUND: '/booking/refund',
    COURTS: '/booking/courts',
    MEMBERS: '/booking/members',
    VENUES: '/booking/venues'
  },
  // 课程相关
  COURSE: {
    LIST: '/course/list',
    DETAIL: '/course/info',
    STATUS: '/course/status',
    BOOKING: {
      LIST: '/course/booking/list',
      ADD: '/course/booking/add',
      DETAIL: '/course/booking/info'
    },
    COACHES: '/course/coaches',
    COURTS: '/course/courts'
  },
  // 教练相关
  COACH: {
    LIST: '/coach/list',
    DETAIL: '/coach/info',
    ADD: '/coach/add',
    UPDATE: '/coach/update',
    DELETE: '/coach'
  },
  // 器材相关
  EQUIPMENT: {
    LIST: '/equipment/list',
    DETAIL: '/equipment/info',
    ADD: '/equipment/add',
    UPDATE: '/equipment/update',
    DELETE: '/equipment',
    STOCK: '/equipment/stock',
    RENTAL: {
      LIST: '/equipment/rental/list',
      ADD: '/equipment/rental/add',
      DETAIL: '/equipment/rental/info'
    }
  },
  // 赛事相关
  TOURNAMENT: {
    LIST: '/tournament/list',
    DETAIL: '/tournament/info',
    ADD: '/tournament/add',
    UPDATE: '/tournament/update',
    DELETE: '/tournament',
    REGISTRATION: {
      LIST: '/tournament/registration/list',
      ADD: '/tournament/registration/add',
      DETAIL: '/tournament/registration/info',
      APPROVE: '/tournament/registration/approve',
      REJECT: '/tournament/registration/reject'
    }
  },
  // 充值相关
  RECHARGE: {
    USER: '/recharge/user',
    ADMIN: '/recharge/admin',
    RECORDS: '/recharge/records'
  },
  // 会员相关
  MEMBER: {
    INFO: '/member/info',
    CURRENT: '/member/current',
    CONSUME_RECORDS: '/member'
  },
  // 搜索相关
  SEARCH: {
    VENUES: '/search/venues',
    COURSES: '/search/courses',
    TOURNAMENTS: '/search/tournaments',
    EQUIPMENT: '/search/equipment'
  },
  // 通知相关
  NOTIFICATION: {
    LIST: '/notifications',
    DETAIL: '/notifications'
  },
  // 穿线服务相关
  STRINGING: {
    LIST: '/stringing/list',
    INFO: '/stringing/info',
    INFO_NO: '/stringing/info/no',
    ADD: '/stringing/add',
    DETAIL: '/stringing/info',
    UPDATE: '/stringing/update',
    DELETE: '/stringing',
    STATUS: '/stringing/status',
    STRINGS: '/stringing/strings',
    CALCULATE_PRICE: '/stringing/calculate-price'
  }
}

// 请求超时时间（毫秒）；与微信小程序 request 常见上限对齐，避免慢网/冷启动鉴权等误杀
export const REQUEST_TIMEOUT = 60000

// Token存储key
export const TOKEN_KEY = 'token'
export const REFRESH_TOKEN_KEY = 'refreshToken'
export const USER_INFO_KEY = 'userInfo'

// 分页默认配置
export const PAGE_CONFIG = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 10,
  SIZE_OPTIONS: [10, 20, 50, 100]
}

// 文件上传配置
export const UPLOAD_CONFIG = {
  MAX_SIZE: 5 * 1024 * 1024, // 5MB
  ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/jpg'],
  ALLOWED_EXTENSIONS: ['.jpg', '.jpeg', '.png']
}

// 缓存配置
export const CACHE_CONFIG = {
  VENUE_LIST: 'venue_list',
  COURSE_LIST: 'course_list',
  EQUIPMENT_LIST: 'equipment_list',
  TOURNAMENT_LIST: 'tournament_list',
  EXPIRE_TIME: 5 * 60 * 1000 // 5分钟
}
