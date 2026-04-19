/**
 * 用户端赛事 API（仅导出会员页使用的符号；实现见 @/api/internal/tournament）
 */
export type { TournamentItem, TournamentRegistrationParams } from '@/api/internal/tournament'
export {
  getTournamentList,
  getTournamentDetail,
  createTournamentRegistration
} from '@/api/internal/tournament'
