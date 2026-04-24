import { describe, expect, it } from 'vitest'
import {
  filterUnifiedOrders,
  mapBookingOrder,
  mapEquipmentOrder,
  mapTournamentOrder
} from '@/utils/orderCenter'

describe('orderCenter helpers', () => {
  it('maps booking orders into unified shape', () => {
    const result = mapBookingOrder({
      id: 10,
      bookingNo: 'BK20260424001',
      venueName: '奥体中心',
      courtName: '2 号场',
      bookingDate: '2026-04-24',
      startTime: '09:00:00',
      endTime: '10:00:00',
      orderAmount: 120,
      status: 1,
      paymentStatus: 1,
      createTime: '2026-04-24 08:00:00'
    } as any)

    expect(result.businessType).toBe('BOOKING')
    expect(result.orderNo).toBe('BK20260424001')
    expect(result.amountText).toBe('¥120.00')
    expect(result.detailUrl).toBe('/pages/booking/detail?id=10')
  })

  it('maps equipment and tournament lifecycle correctly', () => {
    const equipment = mapEquipmentOrder({
      id: 12,
      equipmentId: 7,
      rentalNo: 'EQ001',
      equipmentName: '球拍',
      quantity: 2,
      rentalDate: '2026-04-24',
      expectedReturnDate: '2026-04-25',
      rentalAmount: 60,
      status: 4,
      paymentStatus: 1,
      createTime: '2026-04-24 09:00:00'
    } as any)
    const tournament = mapTournamentOrder({
      id: 13,
      tournamentId: 9,
      registrationNo: 'TOUR001',
      tournamentName: '公开赛',
      venueName: '主馆',
      tournamentStartTime: '2026-05-01 09:00:00',
      entryFee: 99,
      status: 4,
      paymentStatus: 2,
      createTime: '2026-04-24 10:00:00'
    } as any)

    expect(equipment.lifecycle).toBe('COMPLETED')
    expect(tournament.lifecycle).toBe('CANCELLED')
    expect(equipment.detailUrl).toBe('/pages/equipment/rental-detail?id=12')
  })

  it('filters orders by lifecycle and business type', () => {
    const items = [
      { businessType: 'BOOKING', lifecycle: 'ACTIVE' },
      { businessType: 'EQUIPMENT', lifecycle: 'COMPLETED' },
      { businessType: 'BOOKING', lifecycle: 'COMPLETED' }
    ] as any

    expect(filterUnifiedOrders(items, 'COMPLETED', 'ALL')).toHaveLength(2)
    expect(filterUnifiedOrders(items, 'ALL', 'BOOKING')).toHaveLength(2)
    expect(filterUnifiedOrders(items, 'ACTIVE', 'BOOKING')).toHaveLength(1)
  })
})
