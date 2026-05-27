/**
 * 联系方式配置
 * 统一管理官网的联系信息（演示占位，公开发布前请替换为真实或客服信息）
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
  // 客服电话（演示）
  phone: '400-000-0000',
  phoneDisplay: '400-000-0000',

  // 联系邮箱（演示）
  email: 'contact@example.com',

  // 微信客服号（演示）
  wechat: 'BMP_Demo_Service',

  // 微信二维码图片路径（可选）
  wechatQrCode: '/images/wechat-qrcode.png',

  // 公司地址（可选）
  address: '某省某市某区演示路 1 号',

  // 工作时间（可选）
  workingHours: '工作日 9:00-18:00'
}

export const socialLinks = {
  // 微信公众号
  wechatOfficialAccount: 'BMP_Official',

  // GitHub（如果开源）
  github: 'https://github.com/TF49/BMP',

  // 微博
  weibo: 'https://weibo.com/bmp',

  // 知乎
  zhihu: 'https://www.zhihu.com/org/bmp'
}

export default {
  contactInfo,
  socialLinks
}
