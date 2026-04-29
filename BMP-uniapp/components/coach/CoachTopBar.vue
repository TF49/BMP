<template>
  <view class="topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
    <view class="brand-wrap">
      <view class="avatar-wrap">
        <image class="avatar-image" :src="avatar" mode="aspectFill" />
      </view>
      <text class="brand-name">{{ brand }}</text>
    </view>

    <view class="action-btn" @tap="$emit('action')">
      <uni-icons :type="actionIcon" size="22" color="#ff6600" />
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  statusBarHeight: number
  avatar: string
  brand: string
  actionIcon: string
}>()

defineEmits<{
  (e: 'action'): void
}>()
</script>

<style lang="scss" scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 10rpx;
  padding-right: 10rpx;
  padding-bottom: 18rpx;
}

.brand-wrap {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.avatar-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 999rpx;
  overflow: hidden;
  background: #e2e2e2;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.brand-name {
  font-size: 24px;
  font-weight: 900;
  letter-spacing: -1rpx;
}

.action-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
