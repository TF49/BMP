<template>
  <el-dialog
    :model-value="modelValue"
    width="640px"
    align-center
    :show-close="false"
    class="notification-popup-dialog"
    overlay-class-name="notification-popup-overlay"
    :destroy-on-close="false"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <!-- 自定义头部：Tab（参考 Galaxy 标签 + 下划线激活态）+ 发布/关闭 -->
    <template #header>
      <div class="notification-popup-header">
        <div class="header-tabs">
          <button
            type="button"
            class="tab-item active"
            aria-label="通知"
          >
            <el-icon :size="18"><Bell /></el-icon>
            <span>通知</span>
            <span class="tab-indicator" />
          </button>
          <button
            type="button"
            class="tab-item"
            aria-label="系统公告"
          >
            <el-icon :size="18"><Promotion /></el-icon>
            <span>系统公告</span>
            <span class="tab-indicator" />
          </button>
        </div>
        <div class="header-actions">
          <el-button
            v-if="canPublish"
            type="primary"
            link
            size="small"
            class="header-publish-btn"
            @click="$emit('publish')"
          >
            发布通知
          </el-button>
          <button
            type="button"
            class="header-close"
            aria-label="关闭"
            @click="handleClose"
          >
            <el-icon :size="18"><Close /></el-icon>
          </button>
        </div>
      </div>
    </template>

    <div v-loading="loading" class="notification-popup-body">
      <!-- 绿色横幅：渐变 + 轻微内光（参考 React Bits 背景层次） -->
      <div class="popup-banner">
        <div class="banner-shine" />
        <div class="banner-content">
          <div class="banner-title">{{ effectiveBannerTitle }}</div>
          <div class="banner-subtitle">{{ effectiveBannerSubtitle }}</div>
        </div>
      </div>

      <!-- 渐变卡片区：柔和阴影 + 纯 CSS 装饰点（无 Unicode/emoji，符合 a11y） -->
      <div class="popup-card">
        <div class="popup-card-decoration" aria-hidden="true">
          <span class="deco-dot deco-dot-1" />
          <span class="deco-dot deco-dot-2" />
          <span class="deco-dot deco-dot-3" />
          <span class="deco-dot deco-dot-4" />
          <span class="deco-dot deco-dot-5" />
          <span class="deco-dot deco-dot-6" />
        </div>
        <div class="popup-list">
          <template v-if="list.length > 0">
            <div
              v-for="(item, index) in list"
              :key="item.id || index"
              class="popup-item"
            >
              <div class="popup-item-title">{{ item.title }}</div>
              <div class="popup-item-content">{{ item.content }}</div>
              <div class="popup-item-meta">
                {{ item.publisherName || '系统' }} · {{ formatTime(item.createTime) }}
              </div>
            </div>
          </template>
          <div v-else class="popup-empty">
            <div class="popup-empty-icon-wrap">
              <el-icon :size="48" class="popup-empty-icon"><Bell /></el-icon>
            </div>
            <p class="popup-empty-text">暂无通知</p>
            <p class="popup-empty-hint">有新消息时会在这里显示</p>
          </div>
        </div>
      </div>

      <!-- 核心优势：小卡片感 -->
      <div class="popup-advantage">
        <span class="popup-advantage-icon">
          <el-icon :size="18"><Aim /></el-icon>
        </span>
        <span>及时获取场馆预订、课程与赛事等重要消息</span>
      </div>
    </div>

    <!-- 底部按钮：Galaxy 风格主/次层次 + 悬停抬升 -->
    <template #footer>
      <div class="notification-popup-footer">
        <button type="button" class="footer-btn footer-btn-secondary" @click="handleCloseToday">
          {{ footerTodayText }}
        </button>
        <button type="button" class="footer-btn footer-btn-primary" @click="handleClose">
          {{ footerCloseText }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bell, Promotion, Close, Aim } from '@element-plus/icons-vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    loading?: boolean
    list?: Array<{ id?: number; title: string; content: string; publisherName?: string; createTime?: string }>
    bannerTitle?: string
    bannerSubtitle?: string
    footerTodayText?: string
    footerCloseText?: string
    canPublish?: boolean
  }>(),
  {
    loading: false,
    list: () => [],
    bannerTitle: '羽毛球管理系统',
    bannerSubtitle: '及时了解场馆动态、预订与活动信息',
    footerTodayText: '今日关闭',
    footerCloseText: '关闭公告',
    canPublish: false
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
  closeToday: []
  publish: []
}>()

const effectiveBannerTitle = computed(() => {
  if (props.list?.length && props.list[0].title) return props.list[0].title
  return props.bannerTitle
})

const effectiveBannerSubtitle = computed(() => {
  if (props.list?.length && props.list[0].content) {
    const text = props.list[0].content.replace(/\s+/g, ' ').trim()
    return text.length > 48 ? text.slice(0, 48) + '…' : text
  }
  return props.bannerSubtitle
})

const formatTime = (time: string) => {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

const handleCloseToday = () => {
  emit('update:modelValue', false)
  emit('closeToday')
}
</script>

<style scoped>
/* 弹窗容器：Galaxy 风格大圆角 + 多层阴影层次 */
.notification-popup-dialog :deep(.el-dialog) {
  border-radius: 24px;
  box-shadow:
    0 0 0 1px rgba(0, 0, 0, 0.04),
    0 8px 24px rgba(0, 0, 0, 0.06),
    0 24px 48px rgba(0, 0, 0, 0.08),
    0 48px 80px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.notification-popup-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 18px 24px 16px;
  border-bottom: 1px solid var(--el-border-color-light, #e5e7eb);
  background: #fff;
}

.notification-popup-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 80vh;
  min-height: 420px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

.notification-popup-dialog :deep(.el-dialog__footer) {
  padding: 18px 24px 20px;
  border-top: 1px solid var(--el-border-color-light, #e5e7eb);
  background: #fff;
}

/* 遮罩：React Bits 风格柔和模糊 */
.notification-popup-dialog :deep(.el-overlay-dialog) {
  backdrop-filter: blur(8px);
  background: rgba(15, 23, 42, 0.2);
}

.notification-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-tabs {
  display: flex;
  align-items: center;
  gap: 2px;
}

.tab-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: none;
  border-radius: 14px;
  background: transparent;
  color: var(--el-text-color-secondary, #64748b);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease;
}

.tab-item:hover {
  background: var(--el-fill-color-light, #f1f5f9);
  color: var(--el-text-color-primary, #0f172a);
}

.tab-item.active {
  color: #ea580c;
  font-weight: 600;
}

.tab-item.active .tab-indicator {
  opacity: 1;
  transform: scaleX(1);
}

.tab-item:focus-visible {
  outline: 2px solid #f97316;
  outline-offset: 2px;
}

.tab-indicator {
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 4px;
  height: 3px;
  border-radius: 4px;
  background: linear-gradient(90deg, #ea580c, #f97316);
  opacity: 0;
  transform: scaleX(0);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-publish-btn {
  color: #ea580c !important;
  font-weight: 500;
}

.header-publish-btn:hover {
  color: #c2410c !important;
}

.header-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 14px;
  background: transparent;
  color: var(--el-text-color-secondary, #64748b);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.header-close:hover {
  background: var(--el-fill-color-light, #f1f5f9);
  color: var(--el-text-color-primary, #0f172a);
}

.header-close:active {
  transform: scale(0.96);
}

.header-close:focus-visible {
  outline: 2px solid #f97316;
  outline-offset: 2px;
}

/* 弹窗主体 */
.notification-popup-body {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

/* 绿色横幅：渐变 + 内高光 + 底部大圆角（层次感） */
.popup-banner {
  position: relative;
  background: linear-gradient(135deg, #059669 0%, #047857 40%, #065f46 100%);
  color: #fff;
  padding: 26px 28px;
  margin: 0;
  overflow: hidden;
  border-radius: 0 0 20px 20px;
}

.banner-shine {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, transparent 0%, rgba(255, 255, 255, 0.1) 50%, transparent 100%);
  pointer-events: none;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: 19px;
  font-weight: 700;
  margin-bottom: 6px;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}

.banner-subtitle {
  font-size: 13px;
  opacity: 0.95;
  line-height: 1.5;
}

/* 渐变卡片区：柔和阴影 + 大圆角 + 可见边框（符合 light mode 对比） */
.popup-card {
  position: relative;
  margin: 20px 24px;
  padding: 24px;
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #fefefe 50%, #f8fafc 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.popup-card-decoration {
  pointer-events: none;
  position: absolute;
  inset: 0;
  overflow: hidden;
}

/* 纯 CSS 装饰点：无 Unicode/emoji，符合 UI/UX Pro 规范 */
.deco-dot {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, currentColor 0%, transparent 70%);
  opacity: 0.35;
}

.deco-dot-1 { top: 12px; left: 16px; width: 8px; height: 8px; color: #f59e0b; }
.deco-dot-2 { top: 16px; right: 20px; width: 6px; height: 6px; color: #f97316; }
.deco-dot-3 { top: 14px; right: 48px; width: 6px; height: 6px; color: #22c55e; }
.deco-dot-4 { top: 10px; left: 50%; margin-left: -5px; width: 10px; height: 10px; color: #ea580c; }
.deco-dot-5 { bottom: 16px; left: 44px; width: 6px; height: 6px; color: #22c55e; }
.deco-dot-6 { bottom: 12px; right: 28px; width: 8px; height: 8px; color: #f59e0b; }

@media (prefers-reduced-motion: no-preference) {
  .deco-dot { animation: deco-dot-float 4s ease-in-out infinite; }
  .deco-dot-1 { animation-delay: 0s; }
  .deco-dot-2 { animation-delay: 0.5s; }
  .deco-dot-3 { animation-delay: 1s; }
  .deco-dot-4 { animation-delay: 1.5s; }
  .deco-dot-5 { animation-delay: 2s; }
  .deco-dot-6 { animation-delay: 2.5s; }
}

@keyframes deco-dot-float {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.35; }
  50% { transform: translateY(-3px) scale(1.1); opacity: 0.5; }
}

.popup-list {
  position: relative;
  z-index: 1;
  max-height: 360px;
  overflow-y: auto;
}

.popup-list::-webkit-scrollbar {
  width: 6px;
}

.popup-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.04);
  border-radius: 6px;
}

.popup-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.12);
  border-radius: 6px;
}

.popup-item {
  padding: 14px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: background 0.2s ease;
}

.popup-item:last-child {
  border-bottom: none;
}

.popup-item:hover {
  background: rgba(249, 115, 22, 0.04);
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: 14px;
}

.popup-item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary, #0f172a);
  margin-bottom: 6px;
}

.popup-item-content {
  font-size: 13px;
  color: var(--el-text-color-regular, #475569);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.popup-item-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary, #64748b);
  margin-top: 6px;
}

/* 空状态：图标轻微浮动 + 层次文案 */
.popup-empty {
  text-align: center;
  padding: 48px 20px;
}

.popup-empty-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(180deg, #fef3c7 0%, #ffedd5 100%);
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.12);
}

@media (prefers-reduced-motion: no-preference) {
  .popup-empty-icon-wrap {
    animation: empty-icon-float 3s ease-in-out infinite;
  }
}

@keyframes empty-icon-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.popup-empty-icon {
  color: #ea580c !important;
  opacity: 0.9;
}

.popup-empty-text {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary, #0f172a);
}

.popup-empty-hint {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary, #64748b);
}

/* 核心优势：小卡片感 + 大圆角 + 清晰边框 */
.popup-advantage {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px 20px;
  font-size: 13px;
  color: var(--el-text-color-regular, #475569);
  background: rgba(5, 150, 105, 0.06);
  margin: 0 24px 20px;
  border-radius: 16px;
  border: 1px solid rgba(5, 150, 105, 0.14);
}

.popup-advantage-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: rgba(5, 150, 105, 0.12);
  color: #059669;
  flex-shrink: 0;
}

/* 底部按钮：Galaxy 主/次层次 + 悬停抬升 */
.notification-popup-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.footer-btn {
  padding: 12px 24px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: box-shadow 0.2s ease, background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.footer-btn:active {
  transform: scale(0.98);
}

.footer-btn:focus-visible {
  outline: 2px solid #f97316;
  outline-offset: 2px;
}

.footer-btn-secondary {
  background: #fff;
  border: 1px solid var(--el-border-color, #e2e8f0);
  color: var(--el-text-color-regular, #475569);
}

.footer-btn-secondary:hover {
  background: var(--el-fill-color-light, #f8fafc);
  border-color: var(--el-border-color-darker, #cbd5e1);
  color: var(--el-text-color-primary, #0f172a);
}

.footer-btn-primary {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  border: none;
  color: #fff;
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.35);
}

.footer-btn-primary:hover {
  background: linear-gradient(135deg, #ea580c 0%, #c2410c 100%);
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.4);
}

/* 响应式：小屏时弹窗不贴边、无横向滚动 */
@media (max-width: 640px) {
  .notification-popup-dialog :deep(.el-dialog) {
    width: 92vw !important;
    max-width: 640px;
    margin: 4vh auto;
  }

  .notification-popup-body {
    min-height: 320px;
  }

  .popup-card {
    margin: 16px 16px;
    padding: 20px;
  }

  .popup-advantage {
    margin: 0 16px 16px;
    padding: 14px 20px;
  }
}
</style>
