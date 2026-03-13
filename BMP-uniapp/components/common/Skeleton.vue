<template>
  <view class="skeleton-container">
    <!-- 卡片骨架 -->
    <view v-if="type === 'card'" class="skeleton-card">
      <view class="skeleton-header">
        <view class="skeleton-avatar skeleton-shimmer"></view>
        <view class="skeleton-header-content">
          <view class="skeleton-title skeleton-shimmer"></view>
          <view class="skeleton-subtitle skeleton-shimmer"></view>
        </view>
      </view>
      <view class="skeleton-body">
        <view 
          class="skeleton-line skeleton-shimmer" 
          v-for="i in lines" 
          :key="i" 
          :style="{ width: getLineWidth(i) }"
        ></view>
      </view>
    </view>

    <!-- 列表骨架 -->
    <view v-else-if="type === 'list'" class="skeleton-list">
      <view class="skeleton-list-item" v-for="i in rows" :key="i">
        <view class="skeleton-icon skeleton-shimmer"></view>
        <view class="skeleton-item-content">
          <view class="skeleton-title skeleton-shimmer"></view>
          <view class="skeleton-subtitle skeleton-shimmer"></view>
        </view>
      </view>
    </view>

    <!-- 默认骨架 -->
    <view v-else class="skeleton-default">
      <view 
        class="skeleton-line skeleton-shimmer" 
        v-for="i in lines" 
        :key="i" 
        :style="{ width: getLineWidth(i) }"
      ></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'card' | 'list' | 'default'
  rows?: number
  lines?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  rows: 3,
  lines: 3
})

const getLineWidth = (index: number): string => {
  const widths = ['100%', '80%', '90%', '70%', '85%']
  return widths[index % widths.length]
}
</script>

<style lang="scss" scoped>
.skeleton-container {
  width: 100%;
}

.skeleton-shimmer {
  background: linear-gradient(
    90deg,
    #f0f0f0 25%,
    #e0e0e0 50%,
    #f0f0f0 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

// 卡片骨架
.skeleton-card {
  padding: 40rpx;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.skeleton-header {
  display: flex;
  align-items: center;
  gap: 32rpx;
  margin-bottom: 40rpx;
}

.skeleton-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
}

.skeleton-header-content {
  flex: 1;
}

.skeleton-title {
  height: 32rpx;
  margin-bottom: 16rpx;
  border-radius: 8rpx;
}

.skeleton-subtitle {
  height: 24rpx;
  width: 60%;
  border-radius: 8rpx;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.skeleton-line {
  height: 28rpx;
  border-radius: 8rpx;
}

// 列表骨架
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: 32rpx;
  padding: 32rpx;
  background: #ffffff;
  border-radius: 16rpx;
}

.skeleton-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
}

.skeleton-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

// 默认骨架
.skeleton-default {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 40rpx;
}
</style>
