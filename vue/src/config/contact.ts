/**
 * 联系方式配置
 * 统一管理官网的联系信息
 */

export interface ContactInfo {
  phone: string
  phoneDisplay: string
  email: string
  wechat: string
  wechatQrCode?: string
  address?: string
  workingHours?: string
}

export const contactInfo: ContactInfo = {
  // 客服电话
  phone: '17378406511',
  phoneDisplay: '17378406511',

  // 联系邮箱
  email: '213196903@qq.com',

  // 微信客服号
  wechat: '17378406511',

  // 微信二维码图片路径（可选）
  wechatQrCode: '/images/wechat-qrcode.png',

  // 公司地址（可选）
  address: '四川省达州市开江县',

  // 工作时间（可选）
  workingHours: '工作日 9:00-18:00'
}

export const socialLinks = {
  // 微信公众号
  wechatOfficialAccount: 'BMP_Official',

  // GitHub（如果开源）
  github: 'https://github.com/your-org/bmp',

  // 微博
  weibo: 'https://weibo.com/bmp',

  // 知乎
  zhihu: 'https://www.zhihu.com/org/bmp'
}

export default {
  contactInfo,
  socialLinks
}
