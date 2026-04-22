import { post, get } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  refreshToken: string
  expiresIn: number
  message: string
  user: {
    id: number
    username: string
    role: string
    status: number
    avatar?: string
  }
}

export interface RegisterParams {
  username: string
  password: string
  confirmPassword: string
  idCard: string
  role: string
}

export interface UserInfo {
  id: number
  username: string
  role: string
  status: number
  avatar?: string
  venueId?: number
  phone?: string
  email?: string
  gender?: number
  birthday?: string
  nickname?: string
  signature?: string
}

export interface UpdateUserInfoParams {
  phone?: string
  gender?: number | string
  avatar?: string
  signature?: string
}

/**
 * 用户登录
 */
export function login(params: LoginParams) {
  return post<LoginResult>(API_PATHS.AUTH.LOGIN, params, { needAuth: false })
}

/**
 * 用户注册
 */
export function register(params: RegisterParams) {
  return post<string>(API_PATHS.AUTH.REGISTER, params, { needAuth: false })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return get<UserInfo>(API_PATHS.AUTH.CURRENT)
}

/**
 * 刷新Token
 */
export function refreshToken(refreshToken: string) {
  return post<LoginResult>(API_PATHS.AUTH.REFRESH, { refreshToken }, { needAuth: false })
}

/**
 * 退出登录
 */
export function logout() {
  return post<string>(API_PATHS.AUTH.LOGOUT)
}

/**
 * 更新用户信息
 * 后端当前仅支持：phone、gender、avatar、signature
 */
export function updateUserInfo(userInfo: UpdateUserInfoParams) {
  return post<string>(API_PATHS.AUTH.UPDATE_INFO, userInfo)
}

/**
 * 修改密码
 */
export function updatePassword(params: { oldPassword: string; newPassword: string }) {
  return post<string>(API_PATHS.AUTH.UPDATE_PASSWORD, params)
}

/**
 * 忘记密码
 */
export function forgotPassword(params: {
  username: string
  idCard: string
  newPassword: string
  confirmPassword: string
}) {
  return post<string>(API_PATHS.AUTH.FORGOT_PASSWORD, params, { needAuth: false })
}

/**
 * 提交用户反馈
 */
export function submitFeedback(params: {
  content: string
  contact?: string
}) {
  return post<string>(API_PATHS.AUTH.SUBMIT_FEEDBACK, params)
}
