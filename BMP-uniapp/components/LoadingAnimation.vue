<template>
  <view class="loading-animation">
    <view class="spinner" :class="`spinner-${type}`">
      <view v-for="i in 12" :key="i" class="spinner-dot"></view>
    </view>
    <text v-if="text" class="loading-text">{{ text }}</text>
  </view>
</template>

<script setup lang="ts">
interface Props {
  type?: 'dots' | 'ring' | 'pulse'
  text?: string
}

withDefaults(defineProps<Props>(), {
  type: 'dots'
})
</script>

<style lang="scss" scoped>
.loading-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;

  .spinner {
    position: relative;
    width: 60rpx;
    height: 60rpx;
    margin-bottom: 20rpx;

    .spinner-dot {
      position: absolute;
      width: 8rpx;
      height: 8rpx;
      background: #ea580c;
      border-radius: 50%;
    }
  }

  // 点状加载动画
  .spinner-dots {
    .spinner-dot {
      $count: 12;
      @for $i from 1 through $count {
        &:nth-child(#{$i}) {
          $angle: ($i - 1) * (360deg / $count);
          transform: rotate($angle) translateY(-20rpx);
          animation: spinner-dots-animation 1.2s linear infinite;
          animation-delay: ($i - 1) * 0.1s;
        }
      }
    }
  }

  // 环形加载动画
  .spinner-ring {
    border: 4rpx solid rgba(234, 88, 12, 0.12);
    border-top-color: #ea580c;
    border-radius: 50%;
    animation: spinner-ring-animation 1s linear infinite;
  }

  // 脉冲加载动画
  .spinner-pulse {
    background: linear-gradient(135deg, #c2410c 0%, #ea580c 60%, #fb923c 100%);
    border-radius: 50%;
    animation: spinner-pulse-animation 1.5s ease-in-out infinite;
  }

  .loading-text {
    font-size: 28rpx;
    color: #666;
  }
}

@keyframes spinner-dots-animation {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0.3;
  }
}

@keyframes spinner-ring-animation {
  to {
    transform: rotate(360deg);
  }
}

@keyframes spinner-pulse-animation {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(0.8);
    opacity: 0.5;
  }
}
</style>
