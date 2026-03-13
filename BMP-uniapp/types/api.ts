/**
 * API统一返回格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应格式
 */
export interface PageResponse<T = any> {
  data: T[]
  total: number
  page: number
  size: number
  pages: number
}
