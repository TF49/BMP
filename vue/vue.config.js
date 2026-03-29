const path = require('path')

module.exports = {
  // 生产环境不生成 source map：显著减少构建体积与首屏下载
  productionSourceMap: false,
  // 配置需要转译的依赖包
  transpileDependencies: [
    '@stomp/stompjs'
  ],
  chainWebpack: config => {
    config
      .plugin('html')
      .tap(args => {
        args[0].title = '羽擎'
        return args
      })

    // 禁用 prefetch：避免路由较多时“后台下载大量异步 chunk”导致网络与 CPU 压力
    // 如需开启，可按需在关键路由处用 webpackPrefetch: true
    config.plugins.delete('prefetch')

    // 配置@别名指向src目录
    config.resolve.alias
      .set('@', path.resolve(__dirname, 'src'))

    if (process.env.NODE_ENV === 'production') {
      config.optimization.runtimeChunk('single')

      // 更明确的拆包策略：把大依赖拆成稳定 chunk，提升缓存命中，降低单次更新影响范围
      config.optimization.splitChunks({
        chunks: 'all',
        maxInitialRequests: 8,
        minSize: 20000,
        cacheGroups: {
          elementPlus: {
            name: 'chunk-element-plus',
            test: /[\\/]node_modules[\\/](element-plus|@element-plus)[\\/]/,
            priority: 40,
            chunks: 'all'
          },
          echarts: {
            name: 'chunk-echarts',
            test: /[\\/]node_modules[\\/]echarts[\\/]/,
            priority: 30,
            chunks: 'all'
          },
          vendors: {
            name: 'chunk-vendors',
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: 'all'
          },
          common: {
            name: 'chunk-common',
            minChunks: 2,
            priority: 5,
            chunks: 'all',
            reuseExistingChunk: true
          }
        }
      })
    }
  },
  devServer: {
    host: '0.0.0.0',
    // 与「局域网 IP + 可变端口」一致：PORT=8082 npm run serve 等
    port: Number(process.env.PORT) || 8080,
    allowedHosts: 'all',
    // HMR 不要用 /ws（会与后端 SockJS 代理冲突）。auto 使用当前页面主机名与端口，不依赖启动时扫网卡
    client: {
      webSocketURL: 'auto://0.0.0.0:0/sockjs-node'
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
