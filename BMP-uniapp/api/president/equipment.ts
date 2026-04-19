/**
 * 会长端器材 API（仅从 internal 转发；勿再 export 自 @/api/equipment）
 */
export type {
  EquipmentItem,
  EquipmentPayload,
  EquipmentRentalParams,
  EquipmentRentalItem
} from '@/api/internal/equipment'
export {
  getEquipmentList,
  getEquipmentDetail,
  addEquipment,
  updateEquipment,
  getEquipmentRentalList,
  getEquipmentRentalDetail,
  createEquipmentRental,
  getEquipmentRentalStatistics,
  updateEquipmentRentalStatus,
  processEquipmentRentalPayment,
  processEquipmentRentalRefund,
  deleteEquipmentRental
} from '@/api/internal/equipment'
