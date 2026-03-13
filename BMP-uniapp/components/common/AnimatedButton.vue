<template>
  <button
    :class="['animated-button', buttonClass, { 'is-loading': loading, 'is-disabled': disabled }]"
    :disabled="disabled || loading"
    :style="buttonStyle"
    @click="handleClick"
    @touchstart="handleTouchStart"
    @touchend="handleTouchEnd"
  >
    <view v-if="loading" class="loading-spinner">
      <view class="spinner"></view>
    </view>
    <view v-else class="button-content">
      <text v-if="icon" class="button-icon">{{ icon }}</text>
      <text class="button-text">{{ text }}</text>
    </view>
    <view v-if="showRipple" class="ripple" :style="rippleStyle"></view>
  </button>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useThemeStore } from '@/store/modules/theme'

interface Props {
  text: string
  type?: 'primary' | 'secondary' | 'outline' | 'text'
  size?: 'small' | 'medium' | 'large'
  icon?: string
  loading?: boolean
  disabled?: boolean
  fullWidth?: boolean
  ripple?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'medium',
  loading: false,
  disabled: false,
  fullWidth: true,
  ripple: true
})

const emit = defineEmits<{
  click: []
}>()

const themeStore = useThemeStore()
const showRipple = ref(false)
const rippleStyle = ref<Record<string, string>>({})
const touchStartTime = ref(0)

const buttonClass = computed(() => {
  return `button-${props.type} button-${props.size}`
})

const buttonStyle = computed(() => {
  const styles: Record<string, string> = {}
  if (props.fullWidth) {
    styles.width = '100%'
  }
  return styles
})

const handleClick = (e: any) => {
  if (props.disabled || props.loading) return
  
  // 创建波纹效果
  if (props.ripple) {
    const button = e.currentTarget || e.target
    const rect = button.getBoundingClientRect?.() || { left: 0, top: 0, width: 0, height: 0 }
    const x = (e.detail?.x || e.clientX || 0) - rect.left
    const y = (e.detail?.y || e.clientY || 0) - rect.top
    
    showRipple.value = true
    rippleStyle.value = {
      left: `${x}px`,
      top: `${y}px`,
      width: `${Math.max(rect.width, rect.height)}px`,
      height: `${Math.max(rect.width, rect.height)}px`
    }
    
    setTimeout(() => {
      showRipple.value = false
    }, 600)
  }
  
  emit('click')
}

const handleTouchStart = () => {
  touchStartTime.value = Date.now()
}

const handleTouchEnd = () => {
  const touchDuration = Date.now() - touchStartTime.value
  // 如果触摸时间很短，可能是点击
  if (touchDuration < 200) {
    // 触摸反馈已在CSS中处理
  }
}
</script>

<style lang="scss">
@import '@/styles/theme.scss';
@import '@/styles/animations.scss';

.animated-button {
  position: relative;
  height: 96rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &::after {
    border: none;
  }
  
  &:active {
    transform: scale(0.98);
  }
  
  &.is-loading {
    cursor: not-allowed;
    pointer-events: none;
  }
  
  &.is-disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }
}

/* 按钮类型 */
.button-primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(60, 197, 31, 0.3);
  
  &:hover {
    box-shadow: 0 12rpx 32rpx rgba(60, 197, 31, 0.4);
    transform: translateY(-2rpx);
  }
  
  &:active {
    box-shadow: 0 4rpx 16rpx rgba(60, 197, 31, 0.3);
  }
}

.button-secondary {
  background: var(--color-secondary);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.3);
  
  &:hover {
    box-shadow: 0 12rpx 32rpx rgba(99, 102, 241, 0.4);
  }
}

.button-outline {
  background: transparent;
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
  
  &:hover {
    background: rgba(60, 197, 31, 0.1);
  }
}

.button-text {
  background: transparent;
  color: var(--color-primary);
  
  &:hover {
    background: rgba(60, 197, 31, 0.1);
  }
}

/* 按钮尺寸 */
.button-small {
  height: 72rpx;
  font-size: 28rpx;
  padding: 0 32rpx;
}

.button-medium {
  height: 96rpx;
  font-size: 32rpx;
  padding: 0 48rpx;
}

.button-large {
  height: 120rpx;
  font-size: 36rpx;
  padding: 0 64rpx;
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  z-index: 1;
}

.button-icon {
  font-size: 36rpx;
}

.button-text {
  line-height: 1;
}

.loading-spinner {
  z-index: 1;
}

.spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.ripple {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  transform: translate(-50%, -50%) scale(0);
  animation: ripple 0.6s ease-out;
  pointer-events: none;
  z-index: 0;
}
</style>
