<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#1a1c1c"></uni-icons>
            </view>
            <text class="nav-title">发布通知</text>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="content">
        <view class="card">
          <view class="field">
            <text class="label">标题</text>
            <input class="input" v-model="form.title" placeholder="请输入通知标题" />
          </view>
          <view class="field">
            <text class="label">内容</text>
            <textarea class="textarea" v-model="form.content" placeholder="请输入通知内容" />
          </view>
          <view class="hint">
            <text class="hint-text">这是占位发布页：目前只做 UI 与路由承接，后续可接入发布接口与草稿能力。</text>
          </view>
        </view>

        <view class="safe-area-spacer" />
      </scroll-view>

      <view class="footer">
        <button class="primary-btn" @click="onSubmit">
          <text class="btn-text">立即发布（占位）</text>
        </button>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const form = reactive({
  title: '',
  content: ''
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.NOTIFICATION_LIST)
}

function onSubmit() {
  uni.showToast({ title: '已发布（占位）', icon: 'none' })
  setTimeout(() => goBack(), 500)
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 140rpx;
  color: #1a1c1c;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}
.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(248, 250, 252, 0.9);
  backdrop-filter: blur(12px);
  padding: 24rpx 48rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}
.nav-row {
  display: flex;
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
  height: calc(100vh - var(--status-bar-height) - 120rpx);
  padding: 32rpx 32rpx 0;
}
.card {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(2, 6, 23, 0.06);
}
.field {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  padding: 18rpx 0;
}
.label {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 700;
}
.input {
  height: 84rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: rgba(248, 250, 252, 0.9);
  font-size: 30rpx;
}
.textarea {
  width: 100%;
  min-height: 240rpx;
  padding: 18rpx 22rpx;
  border-radius: 18rpx;
  background: rgba(248, 250, 252, 0.9);
  font-size: 28rpx;
  line-height: 1.6;
}
.hint {
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  background: rgba(234, 88, 12, 0.08);
}
.hint-text {
  color: #9a3412;
  font-size: 24rpx;
  line-height: 1.5;
}
.safe-area-spacer {
  height: 40rpx;
}
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18rpx 32rpx calc(env(safe-area-inset-bottom) + 18rpx);
  background: rgba(249, 249, 249, 0.92);
  border-top: 1rpx solid rgba(2, 6, 23, 0.06);
  backdrop-filter: blur(12px);
}
.primary-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 9999px;
  background: linear-gradient(135deg, #ea580c, #a33e00);
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-text {
  color: #fff;
  font-size: 30rpx;
  font-weight: 800;
}
</style>

