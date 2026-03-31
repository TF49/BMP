<template>
  <view id="app">
    <!-- 全局组件或布局 -->
  </view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from './store/modules/user'
import { useThemeStore } from './store/modules/theme'

const userStore = useUserStore()
const themeStore = useThemeStore()

onLaunch(async () => {
  console.log('App Launch')

  // 加载全局字体 - 解决小程序渲染层网络错误
  loadGlobalFonts()

  // 初始化主题
  themeStore.initTheme()

  // 检查登录状态（等待完成）
  await userStore.checkLogin()

  // 可以在这里初始化WebSocket连接（如果需要）
  // initWebSocket()
})

/**
 * 加载系统所需字体
 */
function loadGlobalFonts() {
  const fonts = [
    {
      family: 'Lexend',
      source: 'https://fonts.gstatic.com/s/lexend/v26/wlptgwvFAVdoq2_F94zlCfv0bz1WCzsW_LA.ttf'
    },
    {
      family: 'Material Symbols Outlined',
      source: 'https://fonts.gstatic.com/s/materialsymbolsoutlined/v322/kJF1BvYX7BgnkSrUwT8OhrdQw4oELdPIeeII9v6oDMzByHX9rA6RzaxHMPdY43zj-jCxv3fzvRNU22ZXGJpEpjC_1v-p_4MrImHCIJIZrDCvHOem.ttf'
    }
  ]

  fonts.forEach(font => {
    uni.loadFontFace({
      family: font.family,
      source: `url("${font.source}")`,
      global: true,
      success: () => {
        console.log(`Font ${font.family} loaded successfully`)
      },
      fail: (err) => {
        console.error(`Failed to load font ${font.family}:`, err)
      }
    })
  })
}

// 过滤微信小程序开发环境的非关键错误
// #ifdef MP-WEIXIN
function filterWeChatErrors() {
  try {
    // 保存原始的 console.error
    const originalError = console.error
    
    // 重写 console.error 以过滤非关键错误
    console.error = function(...args: any[]) {
      const errorMessage = args.map(arg => 
        typeof arg === 'string' ? arg : JSON.stringify(arg)
      ).join(' ')
      
      // 过滤掉这些常见的非关键错误
      const ignoredErrors = [
        'routeDone with a webviewId',
        'webapi_getwxaasyncsecinfo:fail',
        'SystemError (appServiceSDKScriptError)',
        '游客模式下，调用 wx.operateWXData 是受限的',
        'webapi_getwxaasyncsecinfo'
      ]
      
      // 检查是否是需要忽略的错误
      const shouldIgnore = ignoredErrors.some(pattern => 
        errorMessage.includes(pattern)
      )
      
      // 如果不是需要忽略的错误，才输出
      if (!shouldIgnore) {
        originalError.apply(console, args)
      }
    }

    // 修复：微信要求 closeSocket code 只能是 1000 或 3000-4999。
    // 某些开发环境/框架内部会错误地传入 1006（异常关闭码，不能主动传）。这里做兜底转换。
    const wxAny = (globalThis as any).wx
    if (wxAny && typeof wxAny.closeSocket === 'function') {
      const originalCloseSocket = wxAny.closeSocket
      wxAny.closeSocket = function(options: any = {}) {
        if (options && options.code === 1006) {
          options.code = 1000
        }
        return originalCloseSocket.call(this, options)
      }
    }

    // 同时兜底 uni.closeSocket（有些栈走的是 uni 而不是 wx）
    const uniAny = (globalThis as any).uni
    if (uniAny && typeof uniAny.closeSocket === 'function') {
      const originalUniCloseSocket = uniAny.closeSocket
      uniAny.closeSocket = function(options: any = {}) {
        if (options && options.code === 1006) {
          options.code = 1000
        }
        return originalUniCloseSocket.call(this, options)
      }
    }
    
    // 处理全局未捕获的错误（如果支持）
    if (typeof uni !== 'undefined' && uni.onError) {
      const originalOnError = uni.onError
      uni.onError = function(error: any) {
        const errorMessage = typeof error === 'string' 
          ? error 
          : (error?.message || error?.errMsg || JSON.stringify(error) || '')
        
        // 过滤非关键错误
        const ignoredErrors = [
          'routeDone with a webviewId',
          'webapi_getwxaasyncsecinfo:fail',
          'SystemError (appServiceSDKScriptError)'
        ]
        
        const shouldIgnore = ignoredErrors.some(pattern => 
          errorMessage.includes(pattern)
        )
        
        if (!shouldIgnore && originalOnError) {
          originalOnError(error)
        }
      }
    }
  } catch (e) {
    // 如果过滤失败，不影响应用启动
    console.warn('错误过滤初始化失败:', e)
  }
}
// #endif

// 需要尽早执行：mp.esm.js 可能在 onLaunch 前就初始化日志 socket
// #ifdef MP-WEIXIN
filterWeChatErrors()
// #endif

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
  // 应用进入后台时可以选择断开WebSocket连接
  // if (wsHelper.isReady()) {
  //   wsHelper.closeConnection()
  // }
})
</script>

<style lang="scss">
/* 全局样式 - 基于 Pixso 设计 */
@import './styles/common.scss';
@import './styles/theme.scss';
@import './styles/animations.scss';

page {
  background-color: var(--color-background, #F8FAFC);
  font-family: 'Lexend', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: var(--color-text, #1E293B);
  transition: background-color 0.2s ease, color 0.2s ease;
}

/* 全局响应式容器 */
.container {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 全局滚动条隐藏 */
.no-scrollbar {
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
