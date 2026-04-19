/**
 * 会长端通知 API（仅从 internal 转发）
 */
export type { NotificationItem, NotificationListResult } from '@/api/internal/notification'
export {
  getNotificationList,
  getNotificationDetail,
  createNotification,
  updateNotification,
  deleteNotification
} from '@/api/internal/notification'
