<template>
  <view class="animated-input-wrapper" :class="{ 'has-error': hasError, 'is-focused': isFocused }">
    <view class="input-container">
      <view v-if="iconType" class="input-icon">
        <uni-icons :type="iconType" size="20" :color="iconColor"></uni-icons>
      </view>
      <input
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :placeholder-style="placeholderStyle"
        :disabled="disabled"
        :maxlength="maxlength"
        :cursor-spacing="18"
        class="animated-input"
        :class="{ 'has-icon': iconType }"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      <view v-if="showClear && modelValue" class="clear-icon" @tap.stop="handleClear">
        <uni-icons type="clear" size="16" color="#64748b"></uni-icons>
      </view>
    </view>
    <view v-if="errorMessage" class="error-message animate-shake">
      {{ errorMessage }}
    </view>
    <view v-if="hint && !errorMessage" class="hint-message">
      {{ hint }}
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  modelValue: string
  type?: string
  placeholder?: string
  iconType?: string
  disabled?: boolean
  maxlength?: number
  showClear?: boolean
  errorMessage?: string
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: '',
  iconType: '',
  disabled: false,
  showClear: true,
  errorMessage: '',
  hint: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'focus': []
  'blur': []
  'clear': []
}>()

const isFocused = ref(false)
const hasError = computed(() => !!props.errorMessage)

const iconColor = computed(() => {
  if (props.errorMessage) return '#ef4444'
  if (isFocused.value) return '#3cc51f'
  return '#64748b'
})

// 小程序端更可靠：用 placeholder-style 控制占位文字颜色
const placeholderStyle = computed(() => {
  // 与全局主题色保持一致（slate-600 近似）
  return 'color: rgba(71, 85, 105, 0.65);'
})

const handleInput = (e: any) => {
  const value = e.detail.value || e.target.value
  emit('update:modelValue', value)
}

const handleFocus = () => {
  isFocused.value = true
  emit('focus')
}

const handleBlur = () => {
  isFocused.value = false
  emit('blur')
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}
</script>

<style lang="scss">
@import '@/styles/theme.scss';
@import '@/styles/animations.scss';

.animated-input-wrapper {
  margin-bottom: 32rpx;
  position: relative;
  
  &.has-error {
    .input-container {
      border-color: var(--color-error, #ef4444);
      animation: errorShake 0.5s ease-in-out;
    }
  }
  
  &.is-focused {
    .input-container {
      border-color: var(--color-primary, #3cc51f);
      box-shadow: 0 0 0 4rpx rgba(60, 197, 31, 0.15);
      animation: inputFocus 0.2s ease-out;
      background: rgba(248, 250, 252, 0.98);
    }
    
    .input-icon {
      opacity: 1;
    }
  }
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(248, 250, 252, 0.98);
  border: 2rpx solid var(--color-border, #E2E8F0);
  border-radius: 20rpx;
  padding: 0 28rpx;
  height: 100rpx;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  box-shadow: 0 6rpx 18rpx rgba(15, 23, 42, 0.06);
  
  &:hover {
    border-color: rgba(60, 197, 31, 0.4);
    box-shadow: 0 4rpx 20rpx rgba(60, 197, 31, 0.1);
  }
}

.input-icon {
  margin-right: 16rpx;
  opacity: 0.8;
  transition: opacity 0.2s ease;
}

.animated-input {
  flex: 1;
  height: 100%;
  font-size: 30rpx;
  color: var(--color-text, #1E293B);
  background: transparent;
  border: none;
  
  &::placeholder {
    color: rgba(71, 85, 105, 0.65);
  }
  
  &.has-icon {
    padding-left: 0;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.clear-icon {
  margin-left: 16rpx;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(71, 85, 105, 0.08);
  opacity: 1;
  transition: all 0.3s ease;
  cursor: pointer;
  
  .icon {
    color: #fff;
    font-size: 24rpx;
    line-height: 1;
  }
  
  &:active {
    opacity: 0.85;
  }
}

.error-message {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #ff6b6b;
  padding-left: 8rpx;
  animation: slideDown 0.3s ease-out;
  text-shadow: 0 0 10rpx rgba(255, 107, 107, 0.3);
}

.hint-message {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: rgba(71, 85, 105, 0.65);
  padding-left: 8rpx;
  animation: slideDown 0.3s ease-out;
}

/* 暗色主题适配（H5 可由外层加 .theme-dark） */
.theme-dark {
  .input-container {
    background: rgba(30, 41, 59, 0.55);
    border-color: rgba(255, 255, 255, 0.12);
    box-shadow: 0 10rpx 26rpx rgba(0, 0, 0, 0.28);
  }

  .animated-input {
    color: #f1f5f9;

    &::placeholder {
      color: rgba(203, 213, 225, 0.6);
    }
  }

  .clear-icon {
    background: rgba(148, 163, 184, 0.14);
  }

  .hint-message {
    color: rgba(203, 213, 225, 0.65);
  }
}
</style>
