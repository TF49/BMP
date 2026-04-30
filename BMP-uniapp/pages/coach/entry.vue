<template>
  <view class="entry-page">
    <view class="entry-card">
      <view class="spinner" />
      <text class="entry-title">正在进入教练端</text>
      <text class="entry-desc">{{ message }}</text>
      <view v-if="loadFailed" class="retry-btn" @tap="enterCoach">
        <text>重新进入</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { navigateToCoachHome } from '@/utils/coachAccess'

const loading = ref(false)
const loadFailed = ref(false)
const message = ref('正在校验教练档案和工作入口...')

async function enterCoach() {
  if (loading.value) return
  loading.value = true
  loadFailed.value = false
  message.value = '正在校验教练档案和工作入口...'
  try {
    await navigateToCoachHome(false)
  } catch (error) {
    console.error('进入教练端失败:', error)
    loadFailed.value = true
    message.value = '入口校验失败，请检查网络后重试。'
    uni.showToast({
      title: '进入教练端失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

onLoad(() => {
  enterCoach()
})

onShow(() => {
  if (!loading.value && !loadFailed.value) {
    enterCoach()
  }
})
</script>

<style lang="scss" scoped>
.entry-page {
  min-height: 100vh;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40rpx;
}

.entry-card {
  width: 100%;
  padding: 80rpx 40rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 34rpx rgba(26, 28, 28, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.spinner {
  width: 64rpx;
  height: 64rpx;
  border: 6rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 999rpx;
  animation: spin 0.8s linear infinite;
}

.entry-title {
  margin-top: 32rpx;
  font-size: 42rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.entry-desc {
  margin-top: 20rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #5f5e5e;
}

.retry-btn {
  min-width: 220rpx;
  height: 84rpx;
  margin-top: 40rpx;
  padding: 0 28rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
