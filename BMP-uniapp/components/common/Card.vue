<template>
  <view class="card" :class="customClass">
    <slot></slot>
  </view>
</template>

<script setup lang="ts">
interface Props {
  customClass?: string
}

defineProps<Props>()
</script>

<style lang="scss" scoped>
.card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}
</style>
