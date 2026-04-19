/**
 * 用户端器材 API（仅导出会员页使用的符号；实现见 @/api/internal/equipment）
 */
export type { EquipmentItem } from '@/api/internal/equipment'
export {
  getEquipmentList,
  getEquipmentDetail,
  createEquipmentRental
} from '@/api/internal/equipment'
