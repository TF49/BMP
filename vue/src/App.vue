<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useTheme } from '@/composables/useTheme'

const { loadTheme } = useTheme()

// 组件挂载时的初始化操作
onMounted(() => {
  // 加载用户保存的主题
  loadTheme()
  console.log('[App] 全局主题系统已初始化')
})

// 组件卸载时的清理操作
onUnmounted(() => {
  // 可以在这里添加清理逻辑
})
</script>

<style>
/* 全局样式 - 使用 CSS 变量支持主题切换 */
#app {
  font-family: 'Poppins', 'Open Sans', Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--color-text-primary, #2c3e50);
  background: var(--color-background, #F8FAFC);
  height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

* {
  margin: 0;
  padding: 0;
}

/* 全局滚动条样式 - 美化 */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 6px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, var(--color-border, #CBD5E1) 0%, var(--color-border-hover, #94A3B8) 100%);
  border-radius: 6px;
  transition: background 0.3s ease;
  border: 2px solid var(--color-background, #F1F5F9);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, var(--color-primary-light, #60A5FA) 0%, var(--color-primary, #2563EB) 100%);
}

/* 全局空状态优化 */
:deep(.el-empty) {
  padding: 40px 20px;
}

:deep(.el-empty__image) {
  opacity: 0.6;
  transition: opacity 0.3s ease;
}

:deep(.el-empty__description) {
  color: var(--color-text-secondary, #64748B);
  font-size: 14px;
  margin-top: 16px;
}

:deep(.el-empty__image:hover) {
  opacity: 0.8;
}

/* 全局加载状态优化 */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

html.theme-dark-mode :deep(.el-loading-mask) {
  background-color: rgba(30, 41, 59, 0.8);
}

/* 全局按钮优化 */
:deep(.el-button) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px;
}

:deep(.el-button:hover) {
  transform: translateY(-1px);
}

:deep(.el-button:active) {
  transform: translateY(0);
}

/* 全局卡片优化 */
:deep(.el-card) {
  border-radius: 16px;
  border-color: var(--color-border, #E2E8F0);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* 减少动画（尊重用户的偏好设置） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 暗夜模式补丁：提升对比度与组件背景一致性 */
html.theme-dark-mode {
  color-scheme: dark;
}

html.theme-dark-mode .el-popper,
html.theme-dark-mode .el-dropdown__popper,
html.theme-dark-mode .el-select__popper {
  --el-bg-color-overlay: var(--color-card-bg, #1E293B);
  --el-fill-color-light: rgba(255, 255, 255, 0.06);
  --el-border-color-light: var(--color-border, #334155);
  --el-text-color-regular: var(--color-text-primary, #F1F5F9);
  --el-text-color-secondary: var(--color-text-secondary, #94A3B8);
}

html.theme-dark-mode .el-dropdown-menu,
html.theme-dark-mode .el-select-dropdown,
html.theme-dark-mode .el-dialog {
  background-color: var(--color-card-bg, #1E293B);
  border-color: var(--color-border, #334155);
}

html.theme-dark-mode .el-dropdown-menu__item:not(.is-disabled):hover,
html.theme-dark-mode .el-dropdown-menu__item:not(.is-disabled):focus {
  background-color: rgba(255, 255, 255, 0.06);
}

html.theme-dark-mode .el-dialog__header,
html.theme-dark-mode .el-dialog__footer {
  border-color: var(--color-border, #334155);
}
</style>
