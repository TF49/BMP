import dayjs from 'dayjs'

/**
 * 格式化日期
 * @param date 日期字符串或Date对象
 * @param format 格式，默认 'YYYY-MM-DD'
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: string | Date | null | undefined, format: string = 'YYYY-MM-DD'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化时间
 * @param time 时间字符串
 * @param format 格式，默认 'HH:mm'
 * @returns 格式化后的时间字符串
 */
export function formatTime(time: string | null | undefined, format: string = 'HH:mm'): string {
  if (!time) return ''
  return dayjs(time, 'HH:mm:ss').format(format)
}

/**
 * 格式化日期时间
 * @param datetime 日期时间字符串
 * @param format 格式，默认 'YYYY-MM-DD HH:mm'
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(datetime: string | Date | null | undefined, format: string = 'YYYY-MM-DD HH:mm'): string {
  if (!datetime) return ''
  return dayjs(datetime).format(format)
}

/**
 * 格式化金额
 * @param amount 金额
 * @param decimals 小数位数，默认2位
 * @returns 格式化后的金额字符串
 */
export function formatAmount(amount: number | null | undefined, decimals: number = 2): string {
  if (amount === null || amount === undefined) return '0.00'
  return amount.toFixed(decimals)
}

/**
 * 格式化手机号（脱敏）
 * @param phone 手机号
 * @returns 脱敏后的手机号
 */
export function formatPhone(phone: string | null | undefined): string {
  if (!phone) return ''
  if (phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 格式化姓名（脱敏）
 * @param name 姓名
 * @returns 脱敏后的姓名
 */
export function formatName(name: string | null | undefined): string {
  if (!name) return ''
  if (name.length <= 2) return name
  return name.substring(0, 1) + '*'.repeat(name.length - 1)
}
