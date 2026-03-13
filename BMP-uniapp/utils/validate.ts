// 验证规则
const rules = {
  // 手机号验证
  phone: /^1[3-9]\d{9}$/,
  // 身份证号验证
  idCard: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
  // 邮箱验证
  email: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/,
  // 整数验证
  integer: /^\d+$/,
  // 密码验证 (6-20位)
  password: /^.{6,20}$/,
  // 用户名验证 (3-20位)
  username: /^.{3,20}$/,
  // 日期格式验证 (YYYY-MM-DD)
  date: /^\d{4}-\d{2}-\d{2}$/
}

/**
 * 验证手机号
 * @param phone 手机号
 * @returns 是否有效
 */
export function validatePhone(phone: string): boolean {
  return rules.phone.test(phone)
}

/**
 * 验证手机号（别名）
 */
export const isMobile = validatePhone

/**
 * 验证身份证号
 * @param idCard 身份证号
 * @returns 是否有效
 */
export function validateIdCard(idCard: string): boolean {
  return rules.idCard.test(idCard)
}

/**
 * 验证身份证号（别名）
 */
export const isIdCard = validateIdCard

/**
 * 验证邮箱
 * @param email 邮箱
 * @returns 是否有效
 */
export function validateEmail(email: string): boolean {
  return rules.email.test(email)
}

/**
 * 验证邮箱（别名）
 */
export const isEmail = validateEmail

/**
 * 验证是否为整数
 */
export function validateInteger(value: string | number): boolean {
  return rules.integer.test(String(value))
}

/**
 * 验证是否为整数（别名）
 */
export const isInteger = validateInteger

/**
 * 验证密码格式
 * @param password 密码
 * @returns 是否有效（6-20位）
 */
export function validatePassword(password: string): boolean {
  return rules.password.test(password)
}

/**
 * 验证密码格式（别名）
 */
export const isPassword = validatePassword

/**
 * 验证用户名格式
 * @param username 用户名
 * @returns 是否有效（3-20位）
 */
export function validateUsername(username: string): boolean {
  return rules.username.test(username)
}

/**
 * 验证用户名格式（别名）
 */
export const isUsername = validateUsername

/**
 * 验证日期格式 (YYYY-MM-DD)
 * @param date 日期字符串
 * @returns 是否有效
 */
export function validateDate(date: string): boolean {
  return rules.date.test(date)
}

/**
 * 必填项验证
 * @param value 值
 * @returns 是否有效
 */
export function validateRequired(value: any): boolean {
  if (value === undefined || value === null) return false
  if (typeof value === 'string' && value.trim() === '') return false
  if (Array.isArray(value) && value.length === 0) return false
  return true
}

/**
 * 验证磅数范围 (18-35)
 */
export function validateTension(tension: number): boolean {
  return typeof tension === 'number' && tension >= 18 && tension <= 35
}

/**
 * 验证服务编号格式 (ST+8位日期+序号)
 */
export function validateServiceNo(serviceNo: string): boolean {
  const pattern = /^ST\d{8}\d+$/
  return pattern.test(serviceNo)
}

/**
 * 验证球拍品牌
 */
export function validateRacketBrand(brand: string): boolean {
  if (!brand || typeof brand !== 'string') return false
  return brand.length > 0 && brand.length <= 50
}

/**
 * 验证球拍型号
 */
export function validateRacketModel(model: string): boolean {
  if (!model || typeof model !== 'string') return false
  return model.length > 0 && model.length <= 50
}

/**
 * 验证备注
 */
export function validateRemark(remark: string): boolean {
  if (!remark) return true
  return typeof remark === 'string' && remark.length <= 200
}
