import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type ThemeMode = 'light' | 'dark'

export interface ThemeColors {
  primary: string
  primaryLight: string
  primaryDark: string
  secondary: string
  background: string
  surface: string
  text: string
  textSecondary: string
  border: string
  error: string
  success: string
  warning: string
  info: string
}

const lightTheme: ThemeColors = {
  primary: '#3cc51f',
  primaryLight: '#4ade80',
  primaryDark: '#2ea816',
  secondary: '#6366f1',
  background: '#F8FAFC',
  surface: '#ffffff',
  text: '#1E293B',
  textSecondary: '#475569',
  border: '#E2E8F0',
  error: '#ef4444',
  success: '#22c55e',
  warning: '#f59e0b',
  info: '#64748b'
}

const darkTheme: ThemeColors = {
  primary: '#4ade80',
  primaryLight: '#6ee7b7',
  primaryDark: '#22c55e',
  secondary: '#818cf8',
  background: '#0f172a',
  surface: '#1e293b',
  text: '#f1f5f9',
  textSecondary: '#cbd5e1',
  border: '#334155',
  error: '#f87171',
  success: '#4ade80',
  warning: '#fbbf24',
  info: '#94a3b8'
}

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const mode = ref<ThemeMode>('light')
  const customPrimaryColor = ref<string>('') // 自定义主色（可选）

  // 计算属性
  const colors = computed<ThemeColors>(() => {
    const baseColors = mode.value === 'light' ? lightTheme : darkTheme
    
    // 如果设置了自定义主色，则覆盖
    if (customPrimaryColor.value) {
      return {
        ...baseColors,
        primary: customPrimaryColor.value,
        primaryLight: adjustBrightness(customPrimaryColor.value, 20),
        primaryDark: adjustBrightness(customPrimaryColor.value, -20)
      }
    }
    
    return baseColors
  })

  const isDark = computed(() => mode.value === 'dark')
  const isLight = computed(() => mode.value === 'light')

  // 方法
  function setMode(newMode: ThemeMode) {
    mode.value = newMode
    saveToStorage()
    applyTheme()
  }

  function toggleMode() {
    mode.value = mode.value === 'light' ? 'dark' : 'light'
    saveToStorage()
    applyTheme()
  }

  function setPrimaryColor(color: string) {
    customPrimaryColor.value = color
    saveToStorage()
    applyTheme()
  }

  function resetPrimaryColor() {
    customPrimaryColor.value = ''
    saveToStorage()
    applyTheme()
  }

  function initTheme() {
    // 从本地存储加载主题设置
    try {
      const savedMode = uni.getStorageSync('theme_mode') as ThemeMode
      const savedColor = uni.getStorageSync('theme_primary_color')
      
      if (savedMode && (savedMode === 'light' || savedMode === 'dark')) {
        mode.value = savedMode
      }
      
      if (savedColor) {
        customPrimaryColor.value = savedColor
      }
    } catch (error) {
      console.error('加载主题设置失败:', error)
    }
    
    applyTheme()
  }

  function saveToStorage() {
    try {
      uni.setStorageSync('theme_mode', mode.value)
      if (customPrimaryColor.value) {
        uni.setStorageSync('theme_primary_color', customPrimaryColor.value)
      } else {
        uni.removeStorageSync('theme_primary_color')
      }
    } catch (error) {
      console.error('保存主题设置失败:', error)
    }
  }

  function applyTheme() {
    // 应用CSS变量到页面
    // 设置CSS变量（如果在小程序环境，这些可能不会生效，但保留用于H5）
    // #ifdef H5
    if (typeof document !== 'undefined') {
      const root = document.documentElement || document.body
      const themeColors = colors.value
      
      root.style.setProperty('--color-primary', themeColors.primary)
      root.style.setProperty('--color-primary-light', themeColors.primaryLight)
      root.style.setProperty('--color-primary-dark', themeColors.primaryDark)
      root.style.setProperty('--color-secondary', themeColors.secondary)
      root.style.setProperty('--color-background', themeColors.background)
      root.style.setProperty('--color-surface', themeColors.surface)
      root.style.setProperty('--color-text', themeColors.text)
      root.style.setProperty('--color-text-secondary', themeColors.textSecondary)
      root.style.setProperty('--color-border', themeColors.border)
      root.style.setProperty('--color-error', themeColors.error)
      root.style.setProperty('--color-success', themeColors.success)
      root.style.setProperty('--color-warning', themeColors.warning)
      root.style.setProperty('--color-info', themeColors.info)
    }
    // #endif
    
    // 小程序环境不需要设置CSS变量，因为小程序不支持CSS变量
    // 主题颜色会通过组件props或内联样式传递
  }

  // 辅助函数：调整颜色亮度
  function adjustBrightness(hex: string, percent: number): string {
    const num = parseInt(hex.replace('#', ''), 16)
    const r = Math.min(255, Math.max(0, (num >> 16) + percent))
    const g = Math.min(255, Math.max(0, ((num >> 8) & 0x00FF) + percent))
    const b = Math.min(255, Math.max(0, (num & 0x0000FF) + percent))
    return '#' + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)
  }

  return {
    mode,
    colors,
    isDark,
    isLight,
    customPrimaryColor,
    setMode,
    toggleMode,
    setPrimaryColor,
    resetPrimaryColor,
    initTheme,
    applyTheme
  }
})
