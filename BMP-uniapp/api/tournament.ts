/**
 * 用户端赛事 API（仅导出会员页使用的符号；实现见 @/api/internal/tournament）
 */
export type {
  TournamentItem,
  TournamentRegistrationParams,
  TournamentRegistrationItem,
  TournamentRegistrationPaymentMethod
} from '@/api/internal/tournament'
export {
  getTournamentList,
  getTournamentDetail,
  createTournamentRegistration,
  getTournamentRegistrationDetail,
  processMemberTournamentRegistrationPayment
} from '@/api/internal/tournament'
