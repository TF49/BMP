import request from '@/utils/request'

/**
 * 获取官网「运营概览」展示数据（公开接口，无需登录）
 * 返回字段：
 *   courtUtilizationRate  场地利用率（%，整数）
 *   courtTotal            场地总数
 *   bookingSuccessRate    预约成功率（%，1位小数）
 *   bookingTrend          预约量今日 vs 昨日环比（%）
 *   equipmentAvailable    可用器材数
 *   equipmentTotal        器材总数
 *   todayIncome           当日总收入（元，字符串）
 *   incomeTrend           收入今日 vs 昨日环比（%）
 */
export function getSiteOverview() {
  return request({
    url: '/api/site/overview',
    method: 'get',
    skipErrorMessage: true  // 官网访客看不到后台错误弹窗，降级显示固定值即可
  })
}
