/**
 * 预约状态枚举
 */
export enum BookingStatus {
  CANCELLED = 0, // 已取消
  PENDING_PAYMENT = 1, // 待支付
  PAID = 2, // 已支付
  IN_PROGRESS = 3, // 进行中
  COMPLETED = 4 // 已完成
}

/**
 * 支付方式枚举
 */
export enum PaymentMethod {
  BALANCE = 'BALANCE' // 余额
}
