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
  if (isFocused.value) return '#34c924'
  return '#6b7280'
})

const placeholderStyle = computed(() => {
  return 'color: #b0b7c3;'
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
@import '@/uni.scss';

.animated-input-wrapper {
  margin-bottom: 32rpx;
  position: relative;
  
  &.has-error {
    .input-container {
      border-color: $bmp-border-error;
      animation: errorShake 0.5s ease-in-out;
      background: #fff5f5;
    }
  }
  
  &.is-focused {
    .input-container {
      border-color: $bmp-border-focus;
      box-shadow: 0 0 0 4rpx rgba(90, 203, 74, 0.12);
      animation: inputFocus 0.2s ease-out;
      background: $bmp-surface-input;
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
  background: $bmp-surface-input;
  border: 2rpx solid $bmp-border-input;
  border-radius: $bmp-radius-md;
  padding: 0 28rpx;
  height: 100rpx;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  box-shadow: $bmp-shadow-soft;
  
  &:hover {
    border-color: rgba(90, 203, 74, 0.4);
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
  color: $bmp-text-secondary;
  background: transparent;
  border: none;
  
  &::placeholder {
    color: $bmp-text-placeholder;
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
  background: rgba(107, 114, 128, 0.08);
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
  color: $bmp-error;
  padding-left: 8rpx;
  animation: slideDown 0.3s ease-out;
  text-shadow: 0 0 10rpx rgba(255, 107, 107, 0.3);
}

.hint-message {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: $bmp-text-disabled;
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
