<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#1a1c1c"></uni-icons>
            </view>
            <text class="nav-title">穿线服务（会长端）</text>
          </view>
        </view>
      </view>

      <view class="content">
        <view class="card">
          <text class="title">服务单列表（占位）</text>
          <text class="desc">用户端已有穿线模块；会长端通常用于管理订单、价格策略与进度。当前为占位页，避免路由跳转失败。</text>
          <view class="actions">
            <button class="ghost-btn" @click="openDetailDemo">查看工单详情（示例）</button>
            <button class="primary-btn" @click="goBack">我知道了</button>
          </view>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function openDetailDemo() {
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.STRINGING_DETAIL}?id=demo`
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f9f9f9;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}
.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(248, 250, 252, 0.85);
  backdrop-filter: blur(12px);
  padding: 24rpx 48rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}
.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.back-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background-color: rgba(0, 0, 0, 0.06);
  }
}
.nav-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #ea580c;
}
.content {
  padding: 32rpx;
}
.card {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(2, 6, 23, 0.06);
}
.title {
  font-size: 34rpx;
  font-weight: 900;
  color: #0f172a;
}
.desc {
  display: block;
  margin-top: 12rpx;
  color: #64748b;
  line-height: 1.6;
  font-size: 26rpx;
}
.actions {
  margin-top: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.ghost-btn {
  width: 100%;
  height: 92rpx;
  border-radius: 9999px;
  background: #f3f3f3;
  color: #a33e00;
  font-weight: 800;
  font-size: 28rpx;
  border: 2rpx solid rgba(163, 62, 0, 0.2);
}
.primary-btn {
  width: 100%;
  height: 92rpx;
  border-radius: 9999px;
  background: linear-gradient(135deg, #ea580c, #a33e00);
  color: #fff;
  font-weight: 800;
  font-size: 30rpx;
}
</style>

