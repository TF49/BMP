import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

/** 管理员代充结果（与后端 `/api/recharge/admin` 返回 data 一致） */
export interface AdminRechargeResult {
  rechargeRecord: RechargeRecord
  message: string
  memberLevel?: number
  totalRecharge?: number
}

export interface RechargeParams {
  memberId: number
  rechargeAmount?: number
  rechargeMethod?: string
  remark?: string
  amount?: number
  paymentMethod?: string
  orderType?: string
}

export interface RechargeRecord {
  id: number
  rechargeNo: string
  memberId: number
  memberName: string
  rechargeAmount: number
  rechargeMethod: string
  status: number
  remark: string
  createTime: string
  /** 是否触发身份升级（NORMAL→MEMBER），0-否，1-是 */
  isUpgraded?: number
  /** 是否等级升级（本笔充值使会员等级 Lv1~Lv5 提升），0-否，1-是 */
  isLevelUpgraded?: number
  orderId?: number
}

/**
 * 用户自助充值
 */
export function userRecharge(params: RechargeParams) {
  return request<RechargeRecord>({
    url: API_PATHS.RECHARGE.USER,
    method: 'POST',
    data: params
  })
}

/**
 * 创建充值订单
 */
export function createRechargeOrder(params: RechargeParams) {
  return request<RechargeRecord>({
    url: API_PATHS.RECHARGE.USER,
    method: 'POST',
    data: params
  })
}

/**
 * 管理员为指定会员充值（会长 / 场馆管理员）
 */
export function adminRecharge(params: {
  memberId: number
  amount: number
  method: 'CASH' | 'ALIPAY' | 'WECHAT' | 'BANK'
  remark?: string
}) {
  return request<AdminRechargeResult>({
    url: API_PATHS.RECHARGE.ADMIN,
    method: 'POST',
    data: {
      memberId: params.memberId,
      amount: params.amount,
      method: params.method,
      remark: params.remark
    }
  })
}

/**
 * 按会员查询充值记录（会长端详情页使用）
 */
export function getRechargeRecordsByMemberId(memberId: number, params?: { page?: number; size?: number }) {
  return request<{
    data: RechargeRecord[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: `${API_PATHS.RECHARGE.RECORDS}/${memberId}`,
    method: 'GET',
    data: params
  })
}

/**
 * 获取充值记录列表
 */
export function getRechargeRecords(params?: {
  memberId?: number
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}) {
  return request<{
    data: RechargeRecord[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.RECHARGE.RECORDS,
    method: 'GET',
    data: params
  })
}
