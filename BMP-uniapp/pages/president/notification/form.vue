<template>
  <PresidentLayout :showTabBar="false" className="president-notification-form-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">发布通知</text>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view class="card">
            <text class="section-title">通知内容</text>

            <view class="field-block">
              <text class="field-label">标题</text>
              <input
                v-model.trim="form.title"
                class="input"
                type="text"
                maxlength="100"
                placeholder="请输入通知标题"
                placeholder-class="input-placeholder"
              />
            </view>

            <view class="field-block">
              <text class="field-label">正文</text>
              <textarea
                v-model.trim="form.content"
                class="textarea"
                maxlength="2000"
                placeholder="请输入通知正文"
                placeholder-class="input-placeholder"
              />
            </view>
          </view>

          <view class="card">
            <text class="section-title">发布范围</text>

            <view
              v-for="option in audienceOptions"
              :key="option.value"
              class="audience-item"
              :class="{ active: form.audience === option.value }"
              @click="form.audience = option.value"
            >
              <view class="radio-dot" :class="{ active: form.audience === option.value }" />
              <text class="audience-label">{{ option.label }}</text>
            </view>

            <view v-if="form.audience === 'venue'" class="field-block venue-field">
              <text class="field-label">场馆 ID</text>
              <input
                v-model.trim="form.venueIdInput"
                class="input"
                type="number"
                placeholder="请输入场馆 ID"
                placeholder-class="input-placeholder"
              />
            </view>
          </view>

          <view class="tip-card">
            <text class="tip-title">发布说明</text>
            <text class="tip-text">本轮只保留真实可发布能力，其余未接通功能已从页面移除。</text>
          </view>

          <button class="submit-btn" :disabled="submitting" @click="onPublish">
            {{ submitting ? '发布中...' : '立即发布' }}
          </button>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { createNotification } from '@/api/notification'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const userStore = useUserStore()
const submitting = ref(false)

const form = reactive({
  title: '',
  content: '',
  audience: 'all' as 'all' | 'members' | 'venue',
  venueIdInput: ''
})

const audienceOptions = [
  { value: 'all' as const, label: '全体用户' },
  { value: 'members' as const, label: '会员用户' },
  { value: 'venue' as const, label: '指定场馆' }
]

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.NOTIFICATION_LIST)
}

function resolveVenueId() {
  if (form.audience !== 'venue') return null
  const venueId = Number(form.venueIdInput)
  return Number.isFinite(venueId) && venueId > 0 ? venueId : null
}

async function onPublish() {
  if (!form.title) {
    uni.showToast({ title: '请填写通知标题', icon: 'none' })
    return
  }
  if (!form.content) {
    uni.showToast({ title: '请填写通知正文', icon: 'none' })
    return
  }
  if (form.audience === 'venue' && resolveVenueId() === null) {
    uni.showToast({ title: '请输入有效的场馆 ID', icon: 'none' })
    return
  }
  if (!userStore.userId) {
    uni.showToast({ title: '当前登录信息失效', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const venueId = resolveVenueId()
    await createNotification({
      title: form.title,
      content: form.content,
      publisherId: userStore.userId,
      publisherName: userStore.userInfo?.username || '',
      venueId
    })
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      safeNavigateBack(PRESIDENT_PAGES.NOTIFICATION_LIST)
    }, 300)
  } catch (error) {
    console.error('Failed to create notification:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.president-notification-form-layout {
  :deep(.president-layout-content) {
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
}

.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
}

.nav-header,
.nav-left,
.audience-item {
  display: flex;
  align-items: center;
}

.nav-header {
  padding: 20rpx 32rpx 16rpx;
  background: #f9f9f9;
}

.nav-left {
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.main-scroll {
  height: calc(100vh - var(--status-bar-height) - 88rpx);
}

.scroll-inner {
  padding: 12rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.card,
.tip-card {
  border-radius: 24rpx;
  background: #ffffff;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.section-title,
.tip-title,
.field-label {
  display: block;
  color: #1a1c1c;
}

.section-title,
.tip-title {
  font-size: 28rpx;
  font-weight: 700;
}

.field-block {
  margin-top: 24rpx;
}

.field-label {
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: #8a8a8a;
}

.input,
.textarea {
  width: 100%;
  border-radius: 18rpx;
  background: #f7f7f7;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.textarea {
  min-height: 240rpx;
}

.input-placeholder {
  color: #9ca3af;
}

.audience-item {
  gap: 16rpx;
  padding: 24rpx 0;
  border-top: 1rpx solid #f1f1f1;
}

.audience-item:first-of-type {
  margin-top: 8rpx;
  border-top: none;
}

.audience-item.active .audience-label {
  color: #a33e00;
  font-weight: 700;
}

.radio-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  border: 2rpx solid #d1d5db;
}

.radio-dot.active {
  border-color: #ff6600;
  background: #ff6600;
}

.venue-field {
  margin-top: 8rpx;
}

.tip-text {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #5f5e5e;
}

.submit-btn {
  margin: 0;
  border: none;
  border-radius: 9999px;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  font-size: 28rpx;
}

.submit-btn::after {
  border: none;
}
</style>
