import request from '@/utils/request'

// 注意：如果后端没有验证码功能，可以暂时注释掉或返回模拟数据
export function getCodeImg() {
  // 暂时返回模拟数据，避免 404 错误
  return Promise.resolve({
    code: 200,
    captchaEnabled: false,
    img: '',
    uuid: ''
  })
  // 如果后端有验证码接口，使用下面的代码：
  // return request({
  //   url: '/api/auth/captcha',
  //   headers: {
  //     isToken: false
  //   },
  //   method: 'get'
  // })
}

export function login(data) {
  // 如果传入的是对象，直接使用；如果是单独的参数，转换为对象
  const loginData = typeof data === 'object' ? data : {
    username: data,
    password: arguments[1],
    code: arguments[2],
    uuid: arguments[3]
  }
  return request({
    url: '/api/auth/login',
    headers: {
      isToken: false
    },
    method: 'post',
    data: loginData
  })
}

export function getInfo() {
  return request({
    url: '/api/auth/current',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

export function register(data) {
  return request({
    url: '/api/auth/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data
  })
}

/**
 * 刷新AccessToken
 * @param {string} refreshToken RefreshToken
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/api/auth/refresh',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 更新用户信息（当前用户可编辑字段：phone、gender、avatar、signature）
 * @param {object} userInfo 用户信息
 */
export function updateUserInfo(userInfo) {
  return request({
    url: '/api/auth/update',
    method: 'post',
    data: userInfo
  })
}

/**
 * 上传头像
 * @param {File} file 图片文件
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/auth/upload-avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 忘记密码（通过用户名+身份证验证身份后重置）
 * @param {object} data { username, idCard, newPassword, confirmPassword }
 */
export function forgotPassword(data) {
  return request({
    url: '/api/auth/forgot-password',
    headers: { isToken: false },
    method: 'post',
    data
  })
}

/**
 * 修改密码
 * @param {object} data { oldPassword, newPassword }
 */
export function changePassword(data) {
  return request({
    url: '/api/auth/change-password',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户设置（安全/通知等）
 */
export function getSettings() {
  return request({
    url: '/api/auth/settings',
    method: 'get'
  })
}

/**
 * 保存当前用户设置
 * @param {object} data 设置项 key-value
 */
export function updateSettings(data) {
  return request({
    url: '/api/auth/settings',
    method: 'put',
    data
  })
}

/**
 * 提交反馈
 * @param {object} data { content, contact? }
 */
export function submitFeedback(data) {
  return request({
    url: '/api/feedback',
    method: 'post',
    data
  })
}
