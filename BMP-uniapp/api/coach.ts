/**
 * 教练 API 实现集中在 internal；根路径整包转发供工具/未来引用。会长端请用 @/api/president/coach。
 */
export type { CoachDto, CoachVenueOption } from '@/api/internal/coach'
export {
  getCoachDetail,
  getCoachVenueOptions,
  getCoachList,
  addCoach,
  updateCoach
} from '@/api/internal/coach'
