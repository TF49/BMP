import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface RechargeParams {
  memberId: number
  rechargeAmount: number
  rechargeMethod: string
  remark?: string
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
