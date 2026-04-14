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

onLaunch(() => {
  console.log('App Launch')

  // #ifdef MP-WEIXIN
  // 微信小程序全局错误处理 - 必须在最早期设置
  const originalOnError = (wx as any).onError
  if (typeof originalOnError === 'function') {
    ;(wx as any).onError = function(error: any) {
      const errorStr = typeof error === 'string' ? error : JSON.stringify(error)
      
      // 过滤框架内部超时错误
      const ignoredPatterns = [
        'timeout',
        'WAServiceMainContext',
        'at Function.<anonymous>',
        'at p (WAServiceMainContext'
      ]
      
      if (!ignoredPatterns.some(pattern => errorStr.includes(pattern))) {
        if (typeof originalOnError === 'function') {
          originalOnError.call(wx, error)
        }
      }
    }
  }

  // 拦截 App 级别的 onError
  const app = getApp({ allowDefault: true }) as any
  if (app && !app.__errorHandlerPatched) {
    const originalAppOnError = app.onError
    app.onError = function(error: string) {
      const ignoredPatterns = [
        'timeout',
        'WAServiceMainContext',
        'at Function.<anonymous>',
        'at p (WAServiceMainContext'
      ]
      
      if (!ignoredPatterns.some(pattern => error.includes(pattern))) {
        if (typeof originalAppOnError === 'function') {
          originalAppOnError.call(this, error)
        } else {
          console.error('[App Error]', error)
        }
      }
    }
    app.__errorHandlerPatched = true
  }
  // #endif

  // 全局自定义字体：不再请求 Google（fonts.gstatic.com），避免无外网/受限网络失败。
  // 小程序端对远程字体域名也更敏感；如需品牌字体请将 .ttf/.woff2 放入 /static/fonts/ 并在 loadGlobalFonts 中注册本地路径。
  // #ifndef MP-WEIXIN
  loadGlobalFonts()
  // #endif

  // 初始化主题
  themeStore.initTheme()

  // 启动阶段不要阻塞在网络请求上，避免小程序生命周期超时。
  void userStore.checkLogin().catch((error) => {
    // 过滤框架超时错误
    if (error?.message !== 'check_timeout' && error?.message !== 'framework_timeout') {
      console.warn('checkLogin during launch failed:', error)
    }
  })
})

/**
 * 加载系统所需字体（仅本地路径；不使用 Google Fonts）
 */
function loadGlobalFonts() {
  const fonts: { family: string; source: string }[] = [
    // 示例：{ family: 'Lexend', source: '/static/fonts/Lexend-Regular.ttf' },
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
        // 不阻塞启动：字体加载失败时保持系统字体
        console.warn(`Failed to load font ${font.family}:`, err)
      }
    })
  })
}

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
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
