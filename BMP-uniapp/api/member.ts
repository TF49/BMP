import { request } from '../utils/request'
import { API_PATHS } from '../config/api'

export interface MemberInfo {
  id: number
  memberName: string
  gender: number
  phone: string
  idCard: string
  memberType: string
  memberLevel: number
  balance: number
  status: number
  createTime: string
  avatar?: string
  totalRecharge?: number
  totalConsumption?: number
}

/**
 * 获取当前登录用户的会员信息
 */
export function getCurrentMember() {
  return request<MemberInfo>({
    url: API_PATHS.MEMBER.CURRENT,
    method: 'GET'
  })
}

export interface ConsumeRecord {
  id: number
  memberId: number
  memberName?: string
  businessType: string
  businessId?: number
  businessNo?: string
  amount: number
  paymentMethod: string
  paymentStatus?: number
  beforeBalance?: number
  afterBalance?: number
  description?: string
  remark?: string
  createTime: string
  incomeExpenseType?: number
}

/**
 * 获取会员信息
 */
export function getMemberInfo(id: number) {
  return request<MemberInfo>({
    url: `${API_PATHS.MEMBER.INFO}/${id}`,
    method: 'GET'
  })
}

/**
 * 获取会员消费记录
 */
export function getMemberConsumeRecords(memberId: number, params?: {
  page?: number
  size?: number
}) {
  return request<{
    data: ConsumeRecord[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: `${API_PATHS.MEMBER.CONSUME_RECORDS}/${memberId}/consume-records`,
    method: 'GET',
    data: params
  })
}
