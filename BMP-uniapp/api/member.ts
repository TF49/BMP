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
}

export interface ConsumeRecord {
  id: number
  memberId: number
  memberName: string
  businessType: string
  businessNo: string
  amount: number
  paymentMethod: string
  remark: string
  createTime: string
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
