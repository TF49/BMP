export function getCourtStatusMeta(status?: number | null) {
  switch (status) {
    case 0:
      return { label: '维护中', className: 'maintenance', color: '#4b5563' }
    case 1:
      return { label: '可预约', className: 'available', color: '#166534' }
    case 2:
      return { label: '使用中', className: 'inuse', color: '#9a3412' }
    case 3:
      return { label: '已预约', className: 'booked', color: '#1d4ed8' }
    default:
      return { label: '未知状态', className: 'unknown', color: '#71717a' }
  }
}

export function getCoachStatusMeta(status?: number | null) {
  switch (status) {
    case 1:
      return { label: '在职', key: 'active' as const }
    case 0:
      return { label: '停用', key: 'inactive' as const }
    default:
      return { label: '未知状态', key: 'unknown' as const }
  }
}

export function getCourseStatusMeta(status?: number | null) {
  switch (status) {
    case 1:
      return { label: '报名中', key: 'enrolling' as const }
    case 2:
      return { label: '进行中', key: 'ongoing' as const }
    case 3:
      return { label: '已结束', key: 'finished' as const }
    case 0:
      return { label: '已取消', key: 'cancelled' as const }
    default:
      return { label: '未知状态', key: 'unknown' as const }
  }
}

export function getCourseBookingStatusMeta(status?: number | null) {
  switch (status) {
    case 1:
      return { label: '待支付', key: 'pending' as const }
    case 2:
      return { label: '已支付', key: 'paid' as const }
    case 3:
      return { label: '进行中', key: 'ongoing' as const }
    case 4:
      return { label: '已完成', key: 'completed' as const }
    case 0:
      return { label: '已取消', key: 'cancelled' as const }
    default:
      return { label: '未知状态', key: 'unknown' as const }
  }
}

export function getEquipmentStatusMeta(status?: number | null, availableQuantity?: number | null) {
  if ((availableQuantity ?? 0) <= 0) {
    return { label: '缺货', key: 'out' as const }
  }

  switch (status) {
    case 1:
      return { label: '可用', key: 'available' as const }
    case 0:
      return { label: '停用', key: 'disabled' as const }
    case 2:
      return { label: '维护中', key: 'maintenance' as const }
    default:
      return { label: '库存正常', key: 'normal' as const }
  }
}

export function getEquipmentRentalStatusMeta(status?: number | null) {
  switch (status) {
    case 0:
      return { label: '已取消', key: 'cancelled' as const }
    case 1:
      return { label: '租借中', key: 'on_rent' as const }
    case 2:
      return { label: '已归还', key: 'returned' as const }
    case 3:
      return { label: '已逾期', key: 'overdue' as const }
    default:
      return { label: '未知状态', key: 'unknown' as const }
  }
}

export function getStringingStatusMeta(status?: number | null) {
  switch (status) {
    case 0:
      return { label: '已取消', key: 'cancelled' as const }
    case 1:
      return { label: '等待穿线', key: 'pending' as const }
    case 2:
      return { label: '正在穿线', key: 'in_progress' as const }
    case 3:
      return { label: '已完成', key: 'completed' as const }
    default:
      return { label: '未知状态', key: 'unknown' as const }
  }
}

export function getTournamentStatusMeta(status?: number | null) {
  switch (status) {
    case 1:
      return { label: '报名中', key: 'registration' as const }
    case 2:
      return { label: '进行中', key: 'ongoing' as const }
    case 3:
      return { label: '已结束', key: 'ended' as const }
    case 0:
      return { label: '已取消', key: 'cancelled' as const }
    default:
      return { label: '筹备中', key: 'draft' as const }
  }
}

export function getFinanceBusinessTypeLabel(type?: string | null) {
  const map: Record<string, string> = {
    BOOKING: '场地预约',
    COURSE: '课程预约',
    EQUIPMENT: '器材租借',
    TOURNAMENT: '赛事报名',
    STRINGING: '穿线服务',
    RECHARGE: '会员充值',
    OTHER: '其他'
  }
  return map[type || ''] || type || '未知类型'
}

export function getIncomeExpenseTypeLabel(type?: number | string | null) {
  if (type === 1 || type === '1') return '收入'
  if (type === 0 || type === '0') return '支出'
  return '未知'
}

export function getPaymentMethodLabel(method?: string | null) {
  const map: Record<string, string> = {
    CASH: '现金',
    ALIPAY: '支付宝',
    WECHAT: '微信支付',
    BALANCE: '余额支付'
  }
  return map[method || ''] || method || '未知方式'
}
