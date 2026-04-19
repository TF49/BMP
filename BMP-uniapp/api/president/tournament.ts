/**
 * 会长端赛事 API（仅从 internal 转发）
 */
export type {
  TournamentItem,
  TournamentPayload,
  TournamentRegistrationParams,
  TournamentRegistrationItem
} from '@/api/internal/tournament'
export {
  getTournamentList,
  getTournamentDetail,
  addTournament,
  updateTournament,
  getTournamentRegistrationList,
  createTournamentRegistration,
  getTournamentRegistrationDetail,
  updateTournamentRegistrationStatus
} from '@/api/internal/tournament'
