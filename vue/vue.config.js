const path = require('path')
const os = require('os')

// 获取本机局域网 IP
let localIp = 'localhost'
const interfaces = os.networkInterfaces()
for (const devName in interfaces) {
  const iface = interfaces[devName]
  for (let i = 0; i < iface.length; i++) {
    const alias = iface[i]
    if (alias.family === 'IPv4' && alias.address !== '127.0.0.1' && !alias.internal) {
      localIp = alias.address
    }
  }
}

module.exports = {
  // 配置需要转译的依赖包
  transpileDependencies: [
    '@stomp/stompjs'
  ],
  chainWebpack: config => {
    config
      .plugin('html')
      .tap(args => {
        args[0].title = '羽毛球管理系统'
        return args
      })

    // 配置@别名指向src目录
    config.resolve.alias
      .set('@', path.resolve(__dirname, 'src'))
  },
  devServer: {
    host: '0.0.0.0',
    port: 8080,
    allowedHosts: 'all',
    // HMR 的 WebSocket 不要用 /ws，否则会被 proxy 转到后端导致 Invalid frame header；用独立路径
    client: {
      webSocketURL: `ws://${localIp}:8080/sockjs-node`
    },
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:9090',
        ws: true
      }
    }
  }
}
