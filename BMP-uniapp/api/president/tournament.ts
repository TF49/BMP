/**
 * 会长端赛事 API（仅从 internal 转发）
 */
export type {
  TournamentItem,
  TournamentPayload,
  TournamentRegistrationParams,
  TournamentRegistrationItem,
  TournamentRegistrationPaymentMethod
} from '@/api/internal/tournament'
export {
  getTournamentList,
  getTournamentDetail,
  addTournament,
  updateTournament,
  getTournamentRegistrationList,
  createTournamentRegistration,
  getTournamentRegistrationDetail,
  processTournamentRegistrationPayment,
  processMemberTournamentRegistrationPayment,
  updateTournamentRegistrationStatus
} from '@/api/internal/tournament'
