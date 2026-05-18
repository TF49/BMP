import { getBookingList } from '@/api/president/booking'
import { getCourseBookingList } from '@/api/president/course'
import { getEquipmentRentalList } from '@/api/president/equipment'
import { getTournamentRegistrationList } from '@/api/president/tournament'
import { getStringingList } from '@/api/president/stringing'
import {
  getPaymentAutoCancelInfo,
  resolvePaymentCountdownInfo,
  shouldShowStringingPaymentCountdown,
  type PaymentCountdownOptions
} from '@/composables/usePaymentAutoCancel'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { STRINGING_STATUS } from '@/utils/constant'

export type PresidentPendingPayType =
  | 'BOOKING'
  | 'COURSE'
  | 'EQUIPMENT'
  | 'TOURNAMENT'
  | 'STRINGING'

export type PresidentPendingPayItem = {
  type: PresidentPendingPayType
  typeLabel: string
  title: string
  subTitle: string
  orderId: number
  detailPath: string
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

export async function fetchPresidentPendingPayItems(): Promise<PresidentPendingPayItem[]> {
  const [bookingRes, courseRes, equipmentRes, tournamentRes, stringingRes] = await Promise.all([
    getBookingList({ status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getCourseBookingList({ status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getEquipmentRentalList({ status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getTournamentRegistrationList({ status: 1, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] })),
    getStringingList({ status: STRINGING_STATUS.WAITING, page: 1, size: PAGE_SIZE }).catch(() => ({ data: [] }))
  ])

  const items: PresidentPendingPayItem[] = []

  pickListData(bookingRes).forEach((row) => {
    if (!isUnpaidPending(row)) return
    items.push({
      type: 'BOOKING',
      typeLabel: '场地预约',
      title: row.venueName || row.courtName || row.primaryCourtName || '场地预约',
      subTitle: row.bookingNo || `预约 #${row.id}`,
      orderId: row.id,
      detailPath: `${PRESIDENT_PAGES.BOOKING_DETAIL}?id=${row.id}`,
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
      detailPath: `${PRESIDENT_PAGES.COURSE_BOOKING_DETAIL}?bookingId=${row.id}`,
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
      detailPath: `${PRESIDENT_PAGES.EQUIPMENT_RENTAL_DETAIL}?id=${row.id}`,
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
      detailPath: `${PRESIDENT_PAGES.TOURNAMENT_REGISTRATION_DETAIL}?registrationId=${row.id}`,
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
      detailPath: `${PRESIDENT_PAGES.STRINGING_DETAIL}?id=${row.id}`,
      order: row
    })
  })

  return items
}

export function resolvePresidentPendingCountdown(
  item: PresidentPendingPayItem | null | undefined,
  countdownOptions: PaymentCountdownOptions
) {
  if (!item) return getPaymentAutoCancelInfo(null, countdownOptions)
  if (item.type === 'STRINGING') {
    return resolvePaymentCountdownInfo(item.order, countdownOptions, 'STRINGING')
  }
  return getPaymentAutoCancelInfo(item.order, countdownOptions)
}

export function pickMostUrgentPendingPay(
  items: PresidentPendingPayItem[],
  countdownOptions: PaymentCountdownOptions
): PresidentPendingPayItem | null {
  let best: PresidentPendingPayItem | null = null
  let bestRemaining = Number.POSITIVE_INFINITY

  items.forEach((item) => {
    const info = resolvePresidentPendingCountdown(item, countdownOptions)
    if (!info.show || info.expired) return
    const remaining = Number(info.remainingMs ?? Number.POSITIVE_INFINITY)
    if (remaining < bestRemaining) {
      bestRemaining = remaining
      best = item
    }
  })

  return best
}

export function hasExpiredPresidentPending(
  items: PresidentPendingPayItem[],
  countdownOptions: PaymentCountdownOptions
) {
  return items.some((item) => resolvePresidentPendingCountdown(item, countdownOptions).expired)
}
