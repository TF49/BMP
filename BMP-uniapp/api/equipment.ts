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

export interface EquipmentPayload {
  id?: number
  equipmentCode: string
  equipmentImage?: string
  equipmentName: string
  equipmentType: 'RACKET' | 'SHUTTLE' | 'STRING' | 'OTHER'
  brand?: string
  price: number
  rentalPrice?: number
  rentalDeposit?: number
  totalQuantity: number
  availableQuantity?: number
  status?: number
  description?: string
}

export interface EquipmentRentalParams {
  memberId: number
  equipmentId: number
  quantity: number
  rentalDate: string
  expectedReturnDate: string
  rentalAmount: number
  paymentMethod: string
  unitPrice?: number
  depositAmount?: number
  durationHours?: number
  paymentStatus?: number
  status?: number
  remark?: string
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
 * 新增器材（会长 / 场馆管理员）
 */
export function addEquipment(data: EquipmentPayload) {
  return request<{ id: number }>({
    url: '/equipment/add',
    method: 'POST',
    data
  })
}

/**
 * 更新器材（会长 / 场馆管理员）
 */
export function updateEquipment(data: EquipmentPayload) {
  return request<null>({
    url: '/equipment/update',
    method: 'PUT',
    data
  })
}

/**
 * 获取器材租借列表
 */
export function getEquipmentRentalList(params?: {
  memberId?: number
  equipmentId?: number
  status?: number
  keyword?: string
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
