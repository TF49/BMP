import { request } from '@/utils/request'
import { API_PATHS } from '@/config/api'

export interface StringingService {
  id: number
  serviceNo: string
  memberId?: number
  memberName?: string
  memberPhone?: string
  userId?: number
  userName?: string
  venueId?: number
  venueName?: string
  racketBrand: string
  racketModel: string
  racketDescription?: string
  stringId?: number
  stringName?: string
  stringEquipmentName?: string
  pound?: number | string
  tension?: number
  isOwnString?: number
  ownString?: number
  stringingMethod?: 'TWO_SECTION' | 'FOUR_SECTION' | 'AUTO' | string
  hasBreakage?: number
  hasCollapse?: number
  servicePrice?: number
  totalPrice?: number
  paymentStatus?: number
  paymentMethod?: string
  status: number
  remark?: string
  createTime: string
  updateTime: string
  startTime?: string
  stringBrand?: string
  stringGauge?: string
  stringPrice?: number
}

export interface StringInfo {
  id: number
  equipmentName?: string
  equipmentCode?: string
  price: number
  /** 部分接口返回的展示名 */
  stringName?: string
  brand?: string
  gauge?: string
}

export interface StringingListParams {
  memberId?: number
  status?: number
  keyword?: string
  page?: number
  size?: number
}

export type StringingMethod = 'TWO_SECTION' | 'FOUR_SECTION' | 'AUTO'
export type StringingPaymentMethod = 'BALANCE'

/**
 * 线材信息（带显示名称）
 * 用于前端选择器展示
 */
export interface StringInfoWithDisplay extends StringInfo {
  displayName: string
}

export interface CreateStringingParams {
  /** 会员业务 ID（若登录用户已绑定会员） */
  memberId?: number
  memberPhone?: string
  userId?: number
  userName?: string
  venueId?: number
  racketBrand: string
  racketModel: string
  racketDescription?: string
  stringId?: number
  stringName?: string
  isOwnString?: 0 | 1 | boolean
  ownString?: 0 | 1 | boolean
  pound?: number
  tension?: number
  stringingMethod?: StringingMethod
  hasBreakage?: 0 | 1
  hasCollapse?: 0 | 1
  status?: number
  servicePrice?: number
  paymentMethod?: StringingPaymentMethod
  paymentStatus?: number
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
export function getStringingList(params?: StringingListParams) {
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
  return request<{ id?: number; serviceNo?: string; servicePrice?: number }>({
    url: API_PATHS.STRINGING.ADD,
    method: 'POST',
    data: params
  })
}

export function processStringingPayment(serviceId: number, paymentMethod: StringingPaymentMethod) {
  return request<null>({
    url: `/stringing/payment?serviceId=${serviceId}&paymentMethod=${paymentMethod}`,
    method: 'POST',
    data: undefined
  })
}

export function processStringingRefund(serviceId: number) {
  return request<null>({
    url: `/stringing/refund?serviceId=${serviceId}`,
    method: 'POST',
    data: undefined
  })
}

/**
 * 更新穿线服务状态
 */
export function updateStringingStatus(id: number, status: number) {
  return request<string>({
    url: `${API_PATHS.STRINGING.STATUS}?id=${id}&status=${status}`,
    method: 'PUT',
    data: undefined
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
