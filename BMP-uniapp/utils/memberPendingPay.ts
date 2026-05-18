import { getBookingList } from '@/api/booking'
import { getCourseBookingList } from '@/api/course'
import { getEquipmentRentalList } from '@/api/equipment'
import { getTournamentRegistrationList } from '@/api/tournament'
import { getStringingList } from '@/api/stringing'
import {
  getPaymentAutoCancelInfo,
  resolvePaymentCountdownInfo,
  shouldShowStringingPaymentCountdown,
  type PaymentCountdownOptions
} from '@/composables/usePaymentAutoCancel'
import { buildBookingConfirmUrl, buildBookingSummaryFromDetail, isBookingPendingPayment } from '@/utils/bookingPayment'
import { STRINGING_STATUS } from '@/utils/constant'

export type MemberPendingPayType =
  | 'BOOKING'
  | 'COURSE'
  | 'EQUIPMENT'
  | 'TOURNAMENT'
  | 'STRINGING'

export type MemberPendingPayItem = {
  type: MemberPendingPayType
  typeLabel: string
  title: string
  subTitle: string
  orderId: number
  detailUrl: string
  courseId?: number
  tournamentId?: number
  order: {
    status?: number | null
    paymentStatus?: number | null
    createTime?: string | null
    servicePrice?: number | null
    totalPrice?: number | null
  }
}

const PAGE_SIZE = 8

function pickListData<T>(result: { data?: T[] } | T[] | null | undefined): T[] {
  if (Array.isArray(result)) return result
  if (Array.isArray(result?.data)) return result.data
  return []
}

function isUnpaidPending(order: { status?: number | null; paymentStatus?: number | null }) {
  const status = Number(order.status ?? -1)
  const paymentStatus = Number(order.paymentStatus ?? 0)
  return status === 1 && paymentStatus !== 1 && paymentStatus !== 2
}

export async function fetchMemberPendingPayItems(memberId: number): Promise<MemberPendingPayItem[]> {
  const [bookingRes, courseRes, equipmentRes, tournamentRes, stringingRes] = await Promise.all([
    getBookingList({ memberId, status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getCourseBookingList({ memberId, status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getEquipmentRentalList({ memberId, status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getTournamentRegistrationList({ memberId, status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getStringingList({ memberId, status: STRINGING_STATUS.WAITING, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] }))
  ])

  const items: MemberPendingPayItem[] = []

  pickListData(bookingRes).forEach((row) => {
    if (!isBookingPendingPayment(row)) return
    const summary = buildBookingSummaryFromDetail(row)
    items.push({
      type: 'BOOKING',
      typeLabel: '场地预约',
      title: row.venueName || row.courtName || '场地预约',
      subTitle: row.bookingNo || `预约 #${row.id}`,
      orderId: row.id,
      detailUrl: buildBookingConfirmUrl(row.id, summary, '/pages/index/index'),
      order: row
    })
  })

  pickListData(courseRes).forEach((row) => {
    if (!isUnpaidPending(row)) return
    items.push({
      type: 'COURSE',
      typeLabel: '课程预约',
      title: row.courseName || '课程预约',
      subTitle: row.bookingNo || `预约 #${row.id}`,
      orderId: row.id,
      courseId: Number(row.courseId || 0) || undefined,
      detailUrl: `/pages/course/booking-detail?id=${row.id}`,
      order: row
    })
  })

  pickListData(equipmentRes).forEach((row) => {
    if (!isUnpaidPending(row)) return
    items.push({
      type: 'EQUIPMENT',
      typeLabel: '器材租借',
      title: row.equipmentName || '器材租借',
      subTitle: row.rentalNo || `租借 #${row.id}`,
      orderId: row.id,
      detailUrl: `/pages/equipment/rental-detail?id=${row.id}`,
      order: row
    })
  })

  pickListData(tournamentRes).forEach((row) => {
    if (!isUnpaidPending(row)) return
    items.push({
      type: 'TOURNAMENT',
      typeLabel: '赛事报名',
      title: row.tournamentName || '赛事报名',
      subTitle: row.registrationNo || `报名 #${row.id}`,
      orderId: row.id,
      tournamentId: Number(row.tournamentId || 0) || undefined,
      detailUrl: `/pages/tournament/registration-detail?registrationId=${row.id}`,
      order: row
    })
  })

  pickListData(stringingRes).forEach((row) => {
    if (!shouldShowStringingPaymentCountdown(row) || !isUnpaidPending(row)) return
    items.push({
      type: 'STRINGING',
      typeLabel: '穿线服务',
      title: row.racketModel || row.racketBrand || '穿线服务',
      subTitle: row.serviceNo || `工单 #${row.id}`,
      orderId: row.id,
      detailUrl: `/pages/stringing/detail?id=${row.id}`,
      order: row
    })
  })

  return items
}

export function resolveMemberPendingCountdown(
  item: MemberPendingPayItem | null | undefined,
  countdownOptions: PaymentCountdownOptions
) {
  if (!item) return getPaymentAutoCancelInfo(null, countdownOptions)
  if (item.type === 'STRINGING') {
    return resolvePaymentCountdownInfo(item.order, countdownOptions, 'STRINGING')
  }
  return getPaymentAutoCancelInfo(item.order, countdownOptions)
}

export function pickMostUrgentMemberPendingPay(
  items: MemberPendingPayItem[],
  countdownOptions: PaymentCountdownOptions
): MemberPendingPayItem | null {
  let best: MemberPendingPayItem | null = null
  let bestRemaining = Number.POSITIVE_INFINITY

  items.forEach((item) => {
    const info = resolveMemberPendingCountdown(item, countdownOptions)
    if (!info.show || info.expired) return
    const remaining = Number(info.remainingMs ?? Number.POSITIVE_INFINITY)
    if (remaining < bestRemaining) {
      bestRemaining = remaining
      best = item
    }
  })

  return best
}

export function hasExpiredMemberPending(
  items: MemberPendingPayItem[],
  countdownOptions: PaymentCountdownOptions
) {
  return items.some((item) => resolveMemberPendingCountdown(item, countdownOptions).expired)
}

export function buildCourseBookingCountdownMap(
  items: MemberPendingPayItem[],
  countdownOptions: PaymentCountdownOptions
) {
  const map = new Map<number, ReturnType<typeof getPaymentAutoCancelInfo>>()
  items.forEach((item) => {
    if (item.type !== 'COURSE' || !item.orderId) return
    const info = resolveMemberPendingCountdown(item, countdownOptions)
    if (info.show) map.set(item.orderId, info)
  })
  return map
}

export function buildTournamentCountdownMap(
  items: MemberPendingPayItem[],
  countdownOptions: PaymentCountdownOptions
) {
  const map = new Map<number, ReturnType<typeof getPaymentAutoCancelInfo>>()
  items.forEach((item) => {
    if (item.type !== 'TOURNAMENT' || !item.tournamentId) return
    const info = resolveMemberPendingCountdown(item, countdownOptions)
    if (info.show) map.set(item.tournamentId, info)
  })
  return map
}

export function findEquipmentPendingPay(
  items: MemberPendingPayItem[]
): MemberPendingPayItem | null {
  return items.find((item) => item.type === 'EQUIPMENT') || null
}
