import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface StringingService {
  id: number
  serviceNo: string
  memberId: number
  memberName?: string
  racketBrand: string
  racketModel: string
  stringId?: number
  stringName?: string
  stringBrand?: string
  stringGauge?: string
  stringPrice?: number
  tension: number
  ownString: boolean
  servicePrice: number
  totalPrice: number
  paymentMethod: string
  status: number
  remark?: string
  createTime: string
  updateTime: string
}

export interface StringInfo {
  id: number
  stringName: string
  brand: string
  gauge: string
  price: number
  stock: number
  status: number
}

/**
 * 线材信息（带显示名称）
 * 用于前端选择器展示
 */
export interface StringInfoWithDisplay extends StringInfo {
  displayName: string
}

export interface CreateStringingParams {
  racketBrand: string
  racketModel: string
  stringId?: number
  tension: number
  ownString: boolean
  paymentMethod: string
  remark?: string
}

export interface CalculatePriceParams {
  stringId?: number
  ownString: boolean
}

export interface PriceCalculation {
  stringPrice: number
  servicePrice: number
  totalPrice: number
}

/**
 * 获取穿线服务列表
 */
export function getStringingList(params?: {
  memberId?: number
  status?: number
  keyword?: string
  page?: number
  size?: number
}) {
  return request<{
    data: StringingService[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.STRINGING.LIST,
    method: 'GET',
    data: params
  })
}

/**
 * 获取穿线服务详情
 */
export function getStringingDetail(id: number) {
  return request<StringingService>({
    url: `${API_PATHS.STRINGING.INFO}/${id}`,
    method: 'GET'
  })
}

/**
 * 根据服务编号获取穿线服务详情
 */
export function getStringingByNo(serviceNo: string) {
  return request<StringingService>({
    url: `${API_PATHS.STRINGING.INFO_NO}/${serviceNo}`,
    method: 'GET'
  })
}

/**
 * 创建穿线服务
 */
export function createStringing(params: CreateStringingParams) {
  return request<StringingService>({
    url: API_PATHS.STRINGING.ADD,
    method: 'POST',
    data: params
  })
}

/**
 * 更新穿线服务状态
 */
export function updateStringingStatus(id: number, status: number) {
  return request<string>({
    url: API_PATHS.STRINGING.STATUS,
    method: 'PUT',
    data: { id, status }
  })
}

/**
 * 获取线材列表
 */
export function getStringList() {
  return request<StringInfo[]>({
    url: API_PATHS.STRINGING.STRINGS,
    method: 'GET'
  })
}

/**
 * 计算价格
 */
export function calculatePrice(params: CalculatePriceParams) {
  // 后端为 GET /api/stringing/calculate-price?stringId=&isOwnString=
  return request<{ price: number | string }>({
    url: API_PATHS.STRINGING.CALCULATE_PRICE,
    method: 'GET',
    data: {
      stringId: params.stringId,
      isOwnString: params.ownString ? 1 : 0
    }
  }).then((res) => {
    const total = typeof res?.price === 'string' ? Number(res.price) : (res?.price ?? 0)
    const own = !!params.ownString
    const result: PriceCalculation = {
      stringPrice: own ? 0 : total,
      servicePrice: own ? total : 0,
      totalPrice: total
    }
    return result
  })
}
