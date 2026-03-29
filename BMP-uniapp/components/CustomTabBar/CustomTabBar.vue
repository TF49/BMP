<template>
  <view class="custom-tab-bar">
    <view 
      v-for="(item, index) in list" 
      :key="index"
      class="tab-item"
      :class="{ 'active': current === index }"
      @tap="switchTab(item.pagePath, index)"
    >
      <text class="tab-icon">{{ item.icon }}</text>
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  current: {
    type: Number,
    default: 0
  }
})

const list = ref([
  { pagePath: '/pages/index/index', text: '首页', icon: '🏠' },
  { pagePath: '/pages/venue/list', text: '场馆', icon: '🏢' },
  { pagePath: '/pages/course/list', text: '课程', icon: '👨‍🏫' },
  { pagePath: '/pages/tournament/list', text: '赛事', icon: '🏆' },
  { pagePath: '/pages/profile/index', text: '我的', icon: '👤' }
])

const switchTab = (url: string, index: number) => {
  if (props.current !== index) {
    uni.reLaunch({
      url: url
    })
  }
}
</script>

<style lang="scss" scoped>
.custom-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 150;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 24rpx 16rpx 48rpx;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 48rpx 48rpx 0 0;
  box-shadow: 0 -8rpx 40rpx rgba(0, 0, 0, 0.05);

  .tab-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 16rpx 24rpx;
    border-radius: 32rpx;
    color: #a1a1aa;
    transition: all 0.3s ease-out;

    .tab-icon {
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }

    .tab-text {
      font-size: 20rpx;
      font-weight: 500;
      letter-spacing: 2rpx;
    }

    &.active {
      background-color: #ffedd5;
      color: #ea580c;
      transform: scale(1.05);

      .tab-text {
        font-weight: bold;
      }
    }
  }
}
</style>
