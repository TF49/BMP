<template>
  <view class="skeleton" :class="{ 'skeleton-animated': animated }">
    <view v-for="i in count" :key="i" class="skeleton-item">
      <view class="skeleton-line" :style="{ width: getRandomWidth() }"></view>
      <view class="skeleton-line" :style="{ width: getRandomWidth() }"></view>
      <view class="skeleton-line" :style="{ width: getRandomWidth() }"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  count?: number
  animated?: boolean
}

withDefaults(defineProps<Props>(), {
  count: 5,
  animated: true
})

const getRandomWidth = () => {
  const widths = ['100%', '90%', '85%', '95%']
  return widths[Math.floor(Math.random() * widths.length)]
}
</script>

<style lang="scss" scoped>
.skeleton {
  padding: 20rpx;

  .skeleton-item {
    margin-bottom: 30rpx;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 8rpx;

    .skeleton-line {
      height: 16rpx;
      background: linear-gradient(
        90deg,
        #f0f0f0 25%,
        #e0e0e0 50%,
        #f0f0f0 75%
      );
      background-size: 200% 100%;
      border-radius: 4rpx;
      margin-bottom: 12rpx;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  &.skeleton-animated .skeleton-line {
    animation: skeleton-loading 1.5s infinite;
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
