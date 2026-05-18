import type { BookingItem } from '@/api/booking'

export type BookingSummaryPayload = {
  venueName?: string
  courtName?: string
  courtNames?: string[]
  courtCount?: number
  primaryCourtName?: string
  bookingMode?: string
  pricingModeSummary?: string
  date?: string
  slot?: string
  bookingId?: number
  totalAmount?: number
  payableAmount?: number
}

export function buildBookingSummaryFromDetail(booking: BookingItem): BookingSummaryPayload {
  const start = booking.startTime?.slice(0, 5) || ''
  const end = booking.endTime?.slice(0, 5) || ''
  const slot = start && end ? `${start} - ${end}` : ''
  return {
    venueName: booking.venueName || booking.courtName || '',
    courtName: booking.courtName || '',
    courtNames: Array.isArray(booking.courtNames) ? booking.courtNames : [],
    courtCount: Number(booking.courtCount || 0),
    primaryCourtName: booking.primaryCourtName || booking.courtName || '',
    bookingMode: booking.bookingMode || 'SHARED',
    pricingModeSummary: booking.pricingModeSummary || '',
    date: booking.bookingDate || '',
    slot,
    bookingId: booking.id,
    totalAmount: Number(booking.orderAmount || 0),
    payableAmount: Number(booking.orderAmount || 0)
  }
}

export function buildBookingConfirmUrl(
  bookingId: number,
  summary: BookingSummaryPayload,
  returnUrl: string
) {
  const data = encodeURIComponent(JSON.stringify(summary))
  const encodedReturn = encodeURIComponent(returnUrl)
  return `/pages/booking/confirm?bookingId=${encodeURIComponent(String(bookingId))}&data=${data}&returnUrl=${encodedReturn}`
}

export function isBookingPendingPayment(booking?: { status?: number | null; paymentStatus?: number | null }) {
  const status = Number(booking?.status ?? -1)
  const paymentStatus = booking?.paymentStatus == null ? 0 : Number(booking.paymentStatus)
  return status === 1 && paymentStatus === 0
}
