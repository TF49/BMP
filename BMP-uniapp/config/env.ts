// 开发环境
// 注意：微信小程序无法访问 localhost，请使用局域网IP或配置域名
// 获取本机IP方法：Windows: ipconfig，Mac/Linux: ifconfig
// 例如：http://192.168.1.100:9090/api
// 参考CMS实现，使用 127.0.0.1 或局域网IP
const DEV = {
  // 方式1：使用 127.0.0.1（微信开发者工具可用，真机需改为局域网IP）
  BASE_URL: 'http://127.0.0.1:9090/api',
  IMAGE_URL: 'http://127.0.0.1:9090'
  
  // 方式2：使用局域网IP（推荐用于真机调试）
  // BASE_URL: 'http://192.168.1.100:9090/api',  // 请替换为您的实际IP
  // IMAGE_URL: 'http://192.168.1.100:9090'
  
  // 方式3：使用内网穿透工具（如 ngrok、natapp）生成的域名
  // BASE_URL: 'https://your-domain.ngrok.io/api',
  // IMAGE_URL: 'https://your-domain.ngrok.io'
}

// 生产环境
// 注意：请将此处的域名替换为您实际的服务器地址
const PROD = {
  BASE_URL: 'https://api.badminton.com',
  IMAGE_URL: 'https://api.badminton.com'
}

// 根据编译环境选择配置
let isDev = true

// #ifdef MP-WEIXIN
isDev = import.meta.env.DEV
// #endif

// #ifndef MP-WEIXIN
isDev = true // 非小程序环境默认开发环境
// #endif

export const config = isDev ? DEV : PROD

/**
 * 使用说明：
 * 
 * 开发环境：
 * - 自动使用 http://localhost:9090/api
 * - 确保后端服务运行在该地址
 * 
 * 生产环境：
 * 方式一（推荐）- 使用环境变量：
 * - 在构建时设置：VUE_APP_API_URL=https://your-api-domain.com
 * - 例如：npm run build -- --mode production --define VUE_APP_API_URL=https://api.yourdomain.com
 * 
 * 方式二 - 直接修改此文件：
 * - 修改上面 PROD 对象中的 BASE_URL 为您的实际服务器地址
 * 
 * 方式三 - 创建 .env.production 文件：
 * - VUE_APP_API_URL=https://your-api-domain.com
 * - VUE_APP_IMAGE_URL=https://your-api-domain.com
 */
