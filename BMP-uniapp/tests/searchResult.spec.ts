import { describe, expect, it } from 'vitest'
import {
  mapCourseSearchResult,
  mapEquipmentSearchResult,
  mapTournamentSearchResult,
  mapVenueSearchResult
} from '@/utils/searchResult'

describe('searchResult mappers', () => {
  it('maps venue fields into unified venue card', () => {
    const result = mapVenueSearchResult({
      id: 1,
      name: '奥体中心',
      venueName: '奥体中心',
      address: '中心路 1 号',
      hourlyPrice: 88,
      venueImage: '/uploads/venue.png',
      surfaceType: '木地板',
      hasParking: true
    })

    expect(result).toMatchObject({
      id: 1,
      name: '奥体中心',
      location: '中心路 1 号',
      price: 88,
      image: '/uploads/venue.png'
    })
    expect(result.tags.map((item) => item.text)).toEqual(['木地板', '停车'])
  })

  it('maps course and tournament fallbacks', () => {
    const course = mapCourseSearchResult({
      id: 2,
      name: '周末训练营',
      coachName: '王教练',
      startTime: '09:00:00',
      endTime: '10:30:00'
    })
    const tournament = mapTournamentSearchResult({
      id: 3,
      name: '春季公开赛',
      tournamentStart: '2026-04-28 09:00:00',
      fee: 120
    })

    expect(course.name).toBe('周末训练营')
    expect(course.time).toBe('09:00:00 - 10:30:00')
    expect(tournament.date).toBe('2026-04-28 09:00:00')
    expect(tournament.fee).toBe(120)
  })

  it('maps equipment cards with inventory tags', () => {
    const result = mapEquipmentSearchResult({
      id: 4,
      name: '尤尼克斯球拍',
      equipmentName: '尤尼克斯球拍',
      rentalPrice: 25,
      rentalDeposit: 100,
      availableQuantity: 3,
      brand: 'YONEX',
      equipmentType: '球拍'
    })

    expect(result).toMatchObject({
      id: 4,
      name: '尤尼克斯球拍',
      price: 25,
      deposit: 100,
      availableQuantity: 3,
      brand: 'YONEX'
    })
    expect(result.tags.map((item) => item.text)).toEqual(['球拍', '可租借'])
  })
})
