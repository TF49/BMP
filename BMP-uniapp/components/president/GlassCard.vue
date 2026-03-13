<template>
  <view class="glass-card-wrap glass-card" :class="className" @click="$emit('click')">
    <slot></slot>
  </view>
</template>

<script setup lang="ts">
interface Props {
  className?: string
}
withDefaults(defineProps<Props>(), { className: '' })
defineEmits<{ (e: 'click'): void }>()
</script>

<style lang="scss" scoped>
.glass-card-wrap {
  background: var(--glass-bg, rgba(255, 255, 255, 0.85));
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  border: 1rpx solid var(--glass-border, rgba(226, 232, 240, 0.9));
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  border-radius: 24rpx;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.glass-card-wrap:active {
  transform: scale(0.99);
}
</style>
