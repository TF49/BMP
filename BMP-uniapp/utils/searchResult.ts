export interface SearchResult {
  id: number
  name: string
  [key: string]: any
}

export interface VenueSearchCard {
  id: number
  name: string
  location: string
  price: number
  image?: string
  rating: number
  tags: Array<{ text: string }>
}

export interface CourseSearchCard {
  id: number
  name: string
  coachName: string
  price: number
  level: string
  date: string
  time: string
}

export interface TournamentSearchCard {
  id: number
  name: string
  date: string
  fee: number
  status: number
  participants: number
  maxParticipants: number
}

export interface EquipmentSearchCard {
  id: number
  name: string
  price: number
  deposit: number
  availableQuantity: number
  brand: string
  image?: string
  tags: Array<{ text: string }>
}

export function mapVenueSearchResult(item: SearchResult): VenueSearchCard {
  return {
    id: item.id,
    name: item.venueName || item.name || '未命名场馆',
    location: item.address || item.location || '',
    price: item.hourlyPrice || item.price || 0,
    image: item.venueImage || item.image,
    rating: item.rating || 4.6,
    tags: [
      { text: item.surfaceType || '室内' },
      { text: item.hasParking ? '停车' : '预约' }
    ]
  }
}

export function mapCourseSearchResult(item: SearchResult): CourseSearchCard {
  return {
    id: item.id,
    name: item.courseName || item.name || '未命名课程',
    coachName: item.coachName || '教练待定',
    price: item.coursePrice || item.price || 0,
    level: item.level || '标准课程',
    date: item.courseDate || item.date || '',
    time: `${item.startTime || ''}${item.startTime || item.endTime ? ' - ' : ''}${item.endTime || ''}`.trim()
  }
}

export function mapTournamentSearchResult(item: SearchResult): TournamentSearchCard {
  return {
    id: item.id,
    name: item.tournamentName || item.name || '未命名赛事',
    date: item.startDate || item.tournamentStart || item.date || '',
    fee: item.entryFee || item.fee || 0,
    status: item.status ?? 1,
    participants: item.currentParticipants || item.participants || 0,
    maxParticipants: item.maxParticipants || 0
  }
}

export function mapEquipmentSearchResult(item: SearchResult): EquipmentSearchCard {
  return {
    id: item.id,
    name: item.equipmentName || item.name || '未命名器材',
    price: item.rentalPrice || item.price || 0,
    deposit: item.rentalDeposit || item.deposit || 0,
    availableQuantity: item.availableQuantity || item.stock || 0,
    brand: item.brand || '品牌待补充',
    image: item.equipmentImage || item.image,
    tags: [
      { text: item.equipmentType || item.type || '器材' },
      { text: (item.availableQuantity || 0) > 0 ? '可租借' : '库存紧张' }
    ]
  }
}
