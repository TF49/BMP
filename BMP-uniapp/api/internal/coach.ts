import { get, post, put, request } from '@/utils/request'

/** 与后端 `Coach` 实体 JSON 一致的字段（camelCase） */
export interface CoachDto {
  id?: number
  coachName: string
  gender?: number
  phone?: string
  idCard?: string
  specialty?: string
  experience?: string
  hourlyPrice?: number
  rating?: number
  totalStudents?: number
  status?: number
  avatar?: string
  venueId?: number
  venueName?: string
  /** 传 `null` 表示解绑账号；省略则可能被后端视为未设置（更新时请显式带上原值） */
  userId?: number | null
}

export interface CoachVenueOption {
  id: number
  venueName: string
  status?: number
}

export function getCoachDetail(id: number) {
  return get<CoachDto>(`/coach/info/${id}`)
}

export function getCoachVenueOptions() {
  return get<CoachVenueOption[]>(`/coach/venues`)
}

export function getCoachList(params?: {
  venueId?: number
  status?: number
  keyword?: string
  gender?: number
  page?: number
  size?: number
}) {
  return request<{
    data: CoachDto[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: '/coach/list',
    method: 'GET',
    data: params
  })
}

export function addCoach(data: CoachDto) {
  return post<{ id: number }>('/coach/add', data)
}

export function updateCoach(data: CoachDto) {
  return put<null>('/coach/update', data)
}
