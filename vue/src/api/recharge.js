import request from '@/utils/request'

// 用户自己充值
export function userRecharge(data) {
  return request({
    url: '/api/recharge/user',
    method: 'post',
    data
  })
}

// 管理员为会员充值
export function adminRecharge(data) {
  return request({
    url: '/api/recharge/admin',
    method: 'post',
    data
  })
}

// 获取当前用户的充值记录（用户/管理员均可，管理员返回全部）
export function getRechargeRecords(params) {
  return request({
    url: '/api/recharge/records',
    method: 'get',
    params
  })
}

// 管理员按会员查询充值记录
export function getRechargeRecordsByMemberId(memberId, params) {
  return request({
    url: `/api/recharge/records/${memberId}`,
    method: 'get',
    params
  })
}
