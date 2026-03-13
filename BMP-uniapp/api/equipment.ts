import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface EquipmentItem {
  id: number
  equipmentCode: string
  equipmentName: string
  equipmentType: string
  brand: string
  price: number
  rentalPrice: number
  totalQuantity: number
  availableQuantity: number
  status: number
  description: string
  createTime: string
}

export interface EquipmentRentalParams {
  memberId: number
  equipmentId: number
  quantity: number
  rentalDate: string
  rentalAmount: number
  paymentMethod: string
}

export interface EquipmentRentalItem {
  id: number
  rentalNo: string
  memberId: number
  memberName: string
  equipmentId: number
  equipmentName: string
  quantity: number
  rentalDate: string
  rentalAmount: number
  paymentMethod: string
  status: number
  createTime: string
}

/**
 * 获取器材列表
 */
export function getEquipmentList(params?: {
  equipmentName?: string
  equipmentType?: string
  status?: number
  page?: number
  size?: number
}) {
  return request<{
    data: EquipmentItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.EQUIPMENT.LIST,
    method: 'GET',
    data: params
  })
}

/**
 * 获取器材详情
 */
export function getEquipmentDetail(id: number) {
  return request<EquipmentItem>({
    url: `${API_PATHS.EQUIPMENT.DETAIL}/${id}`,
    method: 'GET'
  })
}

/**
 * 获取器材租借列表
 */
export function getEquipmentRentalList(params?: {
  memberId?: number
  equipmentId?: number
  status?: number
  page?: number
  size?: number
}) {
  return request<{
    data: EquipmentRentalItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.EQUIPMENT.RENTAL.LIST,
    method: 'GET',
    data: params
  })
}

/**
 * 创建器材租借
 */
export function createEquipmentRental(params: EquipmentRentalParams) {
  return request<EquipmentRentalItem>({
    url: API_PATHS.EQUIPMENT.RENTAL.ADD,
    method: 'POST',
    data: params
  })
}

/**
 * 获取器材租借详情
 */
export function getEquipmentRentalDetail(id: number) {
  return request<EquipmentRentalItem>({
    url: `${API_PATHS.EQUIPMENT.RENTAL.DETAIL}/${id}`,
    method: 'GET'
  })
}
