import { getBookingList } from '@/api/booking'
import { getCourseBookingList } from '@/api/courseBooking'
import { getEquipmentRentalList } from '@/api/equipmentRental'
import { getTournamentRegistrationList } from '@/api/tournamentRegistration'
import { getStringingList } from '@/api/stringing'
import { shouldShowStringingPaymentCountdown } from '@/composables/usePaymentAutoCancel'

function normalizeList(payload) {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.records)) return payload.records
  if (Array.isArray(payload?.list)) return payload.list
  if (Array.isArray(payload?.rows)) return payload.rows
  return []
}

function isUnpaidPending(row) {
  return Number(row?.status ?? -1) === 1 && Number(row?.paymentStatus ?? 0) === 0
}

export async function fetchUserPendingPaymentOrders(limitPerType = 5) {
  const baseParams = { page: 1, size: limitPerType, status: 1, paymentStatus: 0 }
  const [bookingRes, courseRes, rentalRes, tournamentRes, stringingRes] = await Promise.all([
    getBookingList(baseParams).catch(() => ({ data: [] })),
    getCourseBookingList(baseParams).catch(() => ({ data: [] })),
    getEquipmentRentalList(baseParams).catch(() => ({ data: [] })),
    getTournamentRegistrationList(baseParams).catch(() => ({ data: [] })),
    getStringingList(baseParams).catch(() => ({ data: [] }))
  ])

  const items = []

  normalizeList(bookingRes?.data).filter(isUnpaidPending).forEach((row) => {
    items.push({
      orderType: 'booking',
      route: '/user/booking',
      title: '场地预约',
      orderNo: row.bookingNo,
      amount: row.orderAmount,
      subtitle: [row.venueName, row.courtName].filter(Boolean).join(' · '),
      createTime: row.createTime,
      raw: row
    })
  })

  normalizeList(courseRes?.data).filter(isUnpaidPending).forEach((row) => {
    items.push({
      orderType: 'courseBooking',
      route: '/user/course',
      title: '课程预约',
      orderNo: row.bookingNo,
      amount: row.orderAmount,
      subtitle: row.courseName || row.courseTitle || '',
      createTime: row.createTime,
      raw: row
    })
  })

  normalizeList(rentalRes?.data).filter(isUnpaidPending).forEach((row) => {
    items.push({
      orderType: 'equipmentRental',
      route: '/user/equipment',
      title: '器材租借',
      orderNo: row.rentalNo,
      amount: row.totalAmount ?? row.rentalAmount,
      subtitle: row.equipmentName || '',
      createTime: row.createTime,
      raw: row
    })
  })

  normalizeList(tournamentRes?.data).filter(isUnpaidPending).forEach((row) => {
    items.push({
      orderType: 'tournamentRegistration',
      route: '/user/tournament',
      title: '赛事报名',
      orderNo: row.registrationNo,
      amount: row.registrationFee ?? row.orderAmount,
      subtitle: row.tournamentName || '',
      createTime: row.createTime,
      raw: row
    })
  })

  normalizeList(stringingRes?.data)
    .filter((row) => isUnpaidPending(row) && shouldShowStringingPaymentCountdown(row))
    .forEach((row) => {
      items.push({
        orderType: 'stringingService',
        route: '/user/stringing',
        title: '穿线服务',
        orderNo: row.serviceNo,
        amount: row.servicePrice,
        subtitle: row.venueName || '',
        createTime: row.createTime,
        raw: row
      })
    })

  return items.sort((a, b) => {
    const ta = new Date(a.createTime || 0).getTime()
    const tb = new Date(b.createTime || 0).getTime()
    return tb - ta
  })
}
