import type { BookingItem } from '@/api/booking'
import type { CourseBookingItem } from '@/api/course'
import type { EquipmentRentalItem } from '@/api/equipment'
import type { TournamentRegistrationItem } from '@/api/tournament'
import type { StringingService } from '@/api/stringing'
import { BOOKING_STATUS_TEXT, STRINGING_STATUS_TEXT } from '@/utils/constant'
import { formatAmount, formatDateTime } from '@/utils/format'

export type OrderBusinessType = 'BOOKING' | 'COURSE' | 'TOURNAMENT' | 'EQUIPMENT' | 'STRINGING'
export type OrderLifecycle = 'ACTIVE' | 'COMPLETED' | 'CANCELLED'

export interface UnifiedOrderItem {
  businessType: OrderBusinessType
  lifecycle: OrderLifecycle
  id: number
  orderNo: string
  title: string
  subtitle: string
  amount: number
  amountText: string
  statusText: string
  paymentText: string
  timeText: string
  detailUrl: string
}

function mapCommon(
  businessType: OrderBusinessType,
  lifecycle: OrderLifecycle,
  id: number,
  orderNo: string,
  title: string,
  subtitle: string,
  amount: number,
  statusText: string,
  paymentText: string,
  timeText: string,
  detailUrl: string
): UnifiedOrderItem {
  return {
    businessType,
    lifecycle,
    id,
    orderNo,
    title,
    subtitle,
    amount,
    amountText: `¥${formatAmount(amount)}`,
    statusText,
    paymentText,
    timeText,
    detailUrl
  }
}

function bookingLifecycle(status: number): OrderLifecycle {
  if (status === 0) return 'CANCELLED'
  if (status === 4) return 'COMPLETED'
  return 'ACTIVE'
}

function paymentText(paymentStatus?: number) {
  if (paymentStatus === 1) return '已支付'
  if (paymentStatus === 2) return '已退款'
  return '待支付'
}

export function mapBookingOrder(item: BookingItem): UnifiedOrderItem {
  const bookingStatusText = BOOKING_STATUS_TEXT as Record<number, string>
  return mapCommon(
    'BOOKING',
    bookingLifecycle(Number(item.status || 0)),
    item.id,
    item.bookingNo || `BK-${item.id}`,
    item.venueName || item.courtName || '场地预约',
    `${item.courtName || '场地'} · ${item.bookingDate || ''} ${String(item.startTime || '').slice(0, 5)}-${String(item.endTime || '').slice(0, 5)}`.trim(),
    Number(item.orderAmount || 0),
    bookingStatusText[Number(item.status || 0)] || '未知状态',
    paymentText(item.paymentStatus),
    formatDateTime(item.createTime),
    `/pages/booking/detail?id=${item.id}`
  )
}

export function mapCourseOrder(item: CourseBookingItem): UnifiedOrderItem {
  const status = Number(item.status || 0)
  const bookingStatusText = BOOKING_STATUS_TEXT as Record<number, string>
  return mapCommon(
    'COURSE',
    bookingLifecycle(status),
    item.id,
    item.bookingNo || `COURSE-${item.id}`,
    item.courseName || '课程预约',
    `${item.coachName || '课程教练'} · ${item.courseDate || ''} ${String(item.courseStartTime || '').slice(0, 5)}-${String(item.courseEndTime || '').slice(0, 5)}`.trim(),
    Number(item.orderAmount || 0),
    bookingStatusText[status] || '未知状态',
    paymentText(item.paymentStatus),
    formatDateTime(item.createTime),
    `/pages/course/booking-detail?id=${item.id}`
  )
}

export function mapEquipmentOrder(item: EquipmentRentalItem): UnifiedOrderItem {
  const status = Number(item.status || 0)
  const lifecycle: OrderLifecycle = status === 0 ? 'CANCELLED' : status >= 4 ? 'COMPLETED' : 'ACTIVE'
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已租借',
    3: '使用中',
    4: '已归还'
  }
  return mapCommon(
    'EQUIPMENT',
    lifecycle,
    item.id,
    item.rentalNo || `EQ-${item.id}`,
    item.equipmentName || '器材租借',
    `${item.quantity || 1}件 · ${item.rentalDate || ''} 至 ${item.expectedReturnDate || '待归还'}`,
    Number(item.rentalAmount || 0),
    statusMap[status] || '未知状态',
    paymentText(item.paymentStatus),
    formatDateTime(item.createTime),
    `/pages/equipment/rental-detail?id=${item.id}`
  )
}

export function mapTournamentOrder(item: TournamentRegistrationItem): UnifiedOrderItem {
  const status = Number(item.status || 0)
  const lifecycle: OrderLifecycle = status === 0 || status === 4 ? 'CANCELLED' : status >= 3 ? 'COMPLETED' : 'ACTIVE'
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已报名',
    3: '已参赛',
    4: '已退赛'
  }
  return mapCommon(
    'TOURNAMENT',
    lifecycle,
    item.id,
    item.registrationNo || `TOUR-${item.id}`,
    item.tournamentName || '赛事报名',
    `${item.venueName || '赛事场馆'} · ${formatDateTime(item.tournamentStartTime || item.tournamentStart || item.createTime)}`,
    Number(item.entryFee || 0),
    statusMap[status] || '未知状态',
    paymentText(item.paymentStatus),
    formatDateTime(item.createTime),
    `/pages/tournament/registration-detail?id=${item.id}&tournamentId=${item.tournamentId}`
  )
}

export function mapStringingOrder(item: StringingService): UnifiedOrderItem {
  const status = Number(item.status || 0)
  const stringingStatusText = STRINGING_STATUS_TEXT as Record<number, string>
  const lifecycle: OrderLifecycle = status === 0 ? 'CANCELLED' : status === 3 ? 'COMPLETED' : 'ACTIVE'
  return mapCommon(
    'STRINGING',
    lifecycle,
    item.id,
    item.serviceNo || `STR-${item.id}`,
    [item.racketBrand, item.racketModel].filter(Boolean).join(' ') || '穿线服务',
    `${item.venueName || '门店工单'} · ${item.stringName || item.stringEquipmentName || '线材待定'}`,
    Number(item.totalPrice || item.servicePrice || 0),
    stringingStatusText[status] || '未知状态',
    paymentText(item.paymentStatus),
    formatDateTime(item.createTime),
    `/pages/stringing/detail?id=${item.id}`
  )
}

export function filterUnifiedOrders(items: UnifiedOrderItem[], lifecycle: 'ALL' | OrderLifecycle, businessType: 'ALL' | OrderBusinessType) {
  return items.filter((item) => {
    const lifecycleMatched = lifecycle === 'ALL' ? true : item.lifecycle === lifecycle
    const businessMatched = businessType === 'ALL' ? true : item.businessType === businessType
    return lifecycleMatched && businessMatched
  })
}
