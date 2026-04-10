<template>
  <view class="stat-card" :class="`accent-${variant}`">
    <text class="stat-label">{{ label }}</text>
    <view class="stat-row">
      <text class="stat-value" :class="`tone-${variant}`">{{ value }}</text>
      <uni-icons :type="iconType" :size="iconSize" :color="iconColor" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    label: string
    value: string
    variant?: 'primary' | 'error' | 'tertiary'
    iconType?: string
    iconSize?: number
  }>(),
  {
    variant: 'primary',
    iconType: 'shop',
    iconSize: 40
  }
)

const iconColor = computed(() => {
  if (props.variant === 'error') return '#ba1a1a'
  if (props.variant === 'tertiary') return '#0062a1'
  return '#a33e00'
})
</script>

<style lang="scss" scoped>
.stat-card {
  background: #ffffff;
  padding: 48rpx 56rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.06);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 200rpx;
  border-bottom: 4rpx solid transparent;
}
.accent-primary {
  border-bottom-color: #ff6600;
}
.accent-error {
  border-bottom-color: #ba1a1a;
}
.accent-tertiary {
  border-bottom-color: #0062a1;
}
.stat-label {
  font-size: 22rpx;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-weight: 600;
  margin-bottom: 28rpx;
}
.stat-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}
.stat-value {
  font-size: 72rpx;
  font-weight: 800;
  line-height: 1;
}
.tone-primary {
  color: #1a1c1c;
}
.tone-error {
  color: #ba1a1a;
}
.tone-tertiary {
  color: #0062a1;
}
</style>
