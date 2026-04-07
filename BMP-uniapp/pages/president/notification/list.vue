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
            <text class="nav-title">通知管理</text>
          </view>
          <view class="nav-right">
            <view class="icon-btn" @click="goCreate">
              <uni-icons type="plusempty" size="22" color="#71717a"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="content">
        <view class="card">
          <text class="title">通知列表（占位）</text>
          <text class="desc">该页面用于会长端发布/管理通知公告。当前先保证路由可打开，后续再接接口。</text>
          <view class="actions">
            <button class="primary-btn" @click="goCreate">发布通知</button>
            <button class="ghost-btn" @click="goBack">返回</button>
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

function goCreate() {
  uni.navigateTo({ url: PRESIDENT_PAGES.NOTIFICATION_FORM })
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
.nav-right {
  display: flex;
  align-items: center;
}
.icon-btn {
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
  gap: 18rpx;
}
.primary-btn {
  flex: 1;
  height: 92rpx;
  border-radius: 9999px;
  background: linear-gradient(135deg, #ea580c, #a33e00);
  color: #fff;
  font-weight: 800;
  font-size: 30rpx;
}
.ghost-btn {
  flex: 1;
  height: 92rpx;
  border-radius: 9999px;
  background: rgba(2, 6, 23, 0.04);
  color: #0f172a;
  font-weight: 800;
  font-size: 30rpx;
}
</style>

