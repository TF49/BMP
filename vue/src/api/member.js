import request from '@/utils/request'

// 会员列表
export function getMemberList(params) {
  return request({
    url: '/api/member/list',
    method: 'get',
    params
  })
}

// 会员详情
export function getMemberInfo(id) {
  return request({
    url: `/api/member/info/${id}`,
    method: 'get'
  })
}

// 当前登录用户的会员信息（含余额，用户端首页等使用）
export function getCurrentMember() {
  return request({
    url: '/api/member/current',
    method: 'get'
  })
}

// 新增会员
export function addMember(data) {
  return request({
    url: '/api/member/add',
    method: 'post',
    data
  })
}

// 更新会员
export function updateMember(data) {
  return request({
    url: '/api/member/update',
    method: 'put',
    data
  })
}

// 删除会员（逻辑删除）
export function deleteMember(id) {
  return request({
    url: `/api/member/${id}`,
    method: 'delete'
  })
}

// 会员统计
export function getMemberStatistics(params) {
  return request({
    url: '/api/member/statistics',
    method: 'get',
    params
  })
}

// 会员等级/类型分布（Dashboard饼图）
export function getMemberDistribution() {
  return request({
    url: '/api/member/distribution',
    method: 'get'
  })
}

// 会员漏斗（Dashboard漏斗图：注册→活跃→高频→VIP）
export function getMemberFunnel() {
  return request({
    url: '/api/member/funnel',
    method: 'get'
  })
}

// 会员消费记录（按会员聚合）
export function getMemberConsumeList(memberId, params) {
  return request({
    url: `/api/member/${memberId}/consume-records`,
    method: 'get',
    params
  })
}

// 会员来源分布
export function getMemberSource() {
  return request({
    url: '/api/member/source',
    method: 'get'
  })
}

// 即将到期会员列表
export function getExpiringMembers(params) {
  return request({
    url: '/api/member/expiring',
    method: 'get',
    params
  })
}
