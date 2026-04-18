<template>
  <view class="empty-state">
    <view class="empty-icon">
      <text class="icon-text">{{ icon }}</text>
    </view>
    <view class="empty-text">
      <text class="empty-title">{{ title }}</text>
      <text v-if="description" class="empty-description">{{ description }}</text>
    </view>
    <view v-if="showAction && actionText" class="empty-action">
      <button class="action-btn" @click="$emit('action')">
        {{ actionText }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'default' | 'search' | 'data' | 'network'
  title?: string
  description?: string
  actionText?: string
  showAction?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  title: '暂无数据',
  description: '',
  actionText: '',
  showAction: false
})

const icon = computed(() => {
  const iconMap: Record<string, string> = {
    default: '📦',
    search: '🔍',
    data: '📊',
    network: '🌐'
  }
  return iconMap[props.type] || '📦'
})

defineEmits<{
  action: []
}>()
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.empty-icon {
  margin-bottom: 40rpx;
}

.icon-text {
  font-size: 120rpx;
  opacity: 0.5;
}

.empty-text {
  margin-bottom: 48rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.empty-title {
  font-size: 32rpx;
  color: #909399;
  font-weight: 500;
  display: block;
}

.empty-description {
  font-size: 28rpx;
  color: #c0c4cc;
  display: block;
}

.empty-action {
  margin-top: 16rpx;
}

.action-btn {
  padding: 24rpx 48rpx;
  background: linear-gradient(135deg, #c2410c 0%, #ea580c 55%, #fb923c 100%);
  color: #ffffff;
  border-radius: 36rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  box-shadow: 0 8rpx 20rpx rgba(234, 88, 12, 0.24);
}
</style>
