/**
 * 用户信息类型
 */
export interface User {
  id: number
  username: string
  role: 'PRESIDENT' | 'VENUE_MANAGER' | 'USER' | 'MEMBER' | 'COACH'
  status: number
  venueId?: number
}

/**
 * 登录参数
 */
export interface LoginParams {
  username: string
  password: string
}

/**
 * 注册参数
 */
export interface RegisterParams {
  username: string
  password: string
  confirmPassword: string
  idCard: string
  role: string
}
