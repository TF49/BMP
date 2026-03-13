/**
 * 用户权限相关工具函数
 * 支持双Token认证机制（AccessToken + RefreshToken）
 */

/**
 * 获取AccessToken
 */
export function getToken() {
  return localStorage.getItem('token')
}

/**
 * 设置AccessToken
 * @param {string} token
 */
export function setToken(token) {
  return localStorage.setItem('token', token)
}

/**
 * 移除AccessToken
 */
export function removeToken() {
  return localStorage.removeItem('token')
}

/**
 * 获取RefreshToken
 */
export function getRefreshToken() {
  return localStorage.getItem('refreshToken')
}

/**
 * 设置RefreshToken
 * @param {string} refreshToken
 */
export function setRefreshToken(refreshToken) {
  return localStorage.setItem('refreshToken', refreshToken)
}

/**
 * 移除RefreshToken
 */
export function removeRefreshToken() {
  return localStorage.removeItem('refreshToken')
}

/**
 * 获取Token有效期（秒）
 */
export function getTokenExpiresIn() {
  return localStorage.getItem('tokenExpiresIn')
}

/**
 * 设置Token有效期（秒）
 * @param {number} expiresIn
 */
export function setTokenExpiresIn(expiresIn) {
  return localStorage.setItem('tokenExpiresIn', expiresIn)
}

/**
 * 判断是否已登录
 */
export function isLoggedIn() {
  return !!localStorage.getItem('token')
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * @param {Object} userInfo
 */
export function setUserInfo(userInfo) {
  return localStorage.setItem('userInfo', JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  return localStorage.removeItem('userInfo')
}

/**
 * 解析头像 URL（相对路径补全为当前 origin，完整 URL 原样返回）
 * @param {string} [url]
 * @returns {string}
 */
export function resolveAvatarUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const t = url.trim()
  if (t.startsWith('http://') || t.startsWith('https://')) return t
  const base = (typeof window !== 'undefined' && window.location?.origin) ? window.location.origin : ''
  return base + (t.startsWith('/') ? t : '/' + t)
}

/**
 * 退出登录（清除所有认证信息）
 */
export function logout() {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
  localStorage.removeItem('tokenExpiresIn')
  // 跳转到官网（用户可从官网点「去登录」再登录）
  window.location.href = (window.location.origin + window.location.pathname).replace(/\/$/, '') + '#/site'
}

/**
 * 保存登录信息（包含双Token）
 * @param {Object} loginData 登录返回的数据
 */
export function saveLoginData(loginData) {
  if (loginData.token) {
    setToken(loginData.token)
  }
  if (loginData.refreshToken) {
    setRefreshToken(loginData.refreshToken)
  }
  if (loginData.expiresIn) {
    setTokenExpiresIn(loginData.expiresIn)
  }
  if (loginData.user) {
    setUserInfo(loginData.user)
  }
}
