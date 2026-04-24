import { request } from '@/utils/request'
import { API_PATHS } from '@/config/api'

export interface TournamentItem {
  id: number
  tournamentName: string
  tournamentType: string
  venueId: number
  venueName: string
  maxParticipants: number
  currentParticipants: number
  registrationStart: string
  registrationEnd: string
  tournamentStart: string
  tournamentEnd: string
  entryFee: number
  status: number
  description: string
  createTime: string
  startDate?: string
  startTime?: string
  location?: string
  level?: string
  registrationDeadline?: string
  organizer?: string
  rules?: string
  requirements?: string
  prizes?: string
  prizeInfo?: string
  participantsList?: any[]
}

export interface TournamentPayload {
  id?: number
  tournamentName: string
  tournamentType: string
  venueId: number
  maxParticipants: number
  registrationStart: string
  registrationEnd: string
  tournamentStart: string
  tournamentEnd: string
  entryFee: number
  status?: number
  prizeInfo?: string
  description?: string
}

export interface TournamentRegistrationParams {
  tournamentId: number
  memberId: number
  partnerId?: number
  entryFee?: number
  paymentMethod: string
  name?: string
  phone?: string
  gender?: number
  age?: number
  skillLevel?: string
  emergencyContact?: string
  emergencyPhone?: string
  orderAmount?: number
}

export interface TournamentRegistrationItem {
  id: number
  registrationNo: string
  tournamentId: number
  tournamentName: string
  memberId: number
  memberName: string
  partnerId?: number
  partnerName?: string
  entryFee: number
  paymentMethod: string
  paymentStatus?: number
  status: number
  createTime: string
  updateTime?: string
  remark?: string
  matchResult?: string
  tournamentStart?: string
  tournamentEnd?: string
  tournamentStartTime?: string
  tournamentEndTime?: string
  venueName?: string
}

export type TournamentRegistrationPaymentMethod = 'BALANCE'

/**
 * 获取赛事列表
 */
export function getTournamentList(params?: {
  tournamentName?: string
  venueId?: number
  tournamentType?: string
  status?: number
  page?: number
  size?: number
}) {
  return request<{
    data: TournamentItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.TOURNAMENT.LIST,
    method: 'GET',
    data: params
  })
}

/**
 * 获取赛事详情
 */
export function getTournamentDetail(id: number) {
  return request<TournamentItem>({
    url: `${API_PATHS.TOURNAMENT.DETAIL}/${id}`,
    method: 'GET'
  })
}

/**
 * 新增赛事（会长 / 场馆管理员）
 */
export function addTournament(data: TournamentPayload) {
  return request<{ id: number }>({
    url: '/tournament/add',
    method: 'POST',
    data
  })
}

/**
 * 更新赛事（会长 / 场馆管理员）
 */
export function updateTournament(data: TournamentPayload) {
  return request<null>({
    url: '/tournament/update',
    method: 'PUT',
    data
  })
}

/**
 * 获取赛事报名列表
 */
export function getTournamentRegistrationList(params?: {
  tournamentId?: number
  memberId?: number
  status?: number
  page?: number
  size?: number
}) {
  return request<{
    data: TournamentRegistrationItem[]
    total: number
    page: number
    size: number
    pages: number
  }>({
    url: API_PATHS.TOURNAMENT.REGISTRATION.LIST,
    method: 'GET',
    data: params
  })
}

/**
 * 创建赛事报名
 */
export function createTournamentRegistration(params: TournamentRegistrationParams) {
  return request<TournamentRegistrationItem>({
    url: API_PATHS.TOURNAMENT.REGISTRATION.ADD,
    method: 'POST',
    data: params
  })
}

/**
 * 获取赛事报名详情
 */
export function getTournamentRegistrationDetail(id: number) {
  return request<TournamentRegistrationItem>({
    url: `${API_PATHS.TOURNAMENT.REGISTRATION.DETAIL}/${id}`,
    method: 'GET'
  })
}

export function processTournamentRegistrationPayment(
  registrationId: number,
  paymentMethod: TournamentRegistrationPaymentMethod
) {
  return request<null>({
    url: `${API_PATHS.TOURNAMENT.REGISTRATION.PAYMENT}?registrationId=${registrationId}&paymentMethod=${paymentMethod}`,
    method: 'POST',
    data: undefined
  })
}

export function processMemberTournamentRegistrationPayment(
  registrationId: number,
  paymentMethod: TournamentRegistrationPaymentMethod
) {
  return request<null>({
    url: `${API_PATHS.TOURNAMENT.REGISTRATION.MEMBER_PAYMENT}?registrationId=${registrationId}&paymentMethod=${paymentMethod}`,
    method: 'POST',
    data: undefined
  })
}

export function updateTournamentRegistrationStatus(id: number, status: number) {
  return request<null>({
    url: `/tournament/registration/status?id=${id}&status=${status}`,
    method: 'PUT'
  })
}
