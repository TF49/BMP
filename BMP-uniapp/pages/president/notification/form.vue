<template>
  <PresidentLayout :showTabBar="false" className="president-notification-form-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left">
            <view class="nav-icon-btn" @click="goBack">
              <uni-icons type="arrow-left" size="22" color="#1a1c1c" />
            </view>
            <image class="nav-avatar" :src="profileAvatar" mode="aspectFill" />
            <text class="nav-title">发布通知公告</text>
          </view>
          <view class="nav-icon-btn" @click="onSettings">
            <uni-icons type="gear" size="22" color="#5f5e5e" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <!-- 通知详情 -->
          <view class="card">
            <text class="card-kinetic">通知详情 / Notification Details</text>
            <view class="field-block">
              <text class="field-label">公告标题</text>
              <input
                v-model="form.title"
                class="input-underline"
                type="text"
                placeholder="输入引人注目的标题..."
                placeholder-class="ph"
              />
            </view>
            <view class="field-block">
              <text class="field-label">详细内容</text>
              <textarea
                v-model="form.body"
                class="textarea-underline"
                placeholder="在此编写您的通知正文..."
                placeholder-class="ph"
                :maxlength="5000"
              />
            </view>
          </view>

          <!-- 附件媒体 -->
          <view class="card">
            <text class="card-kinetic">附件媒体 / Attachments</text>
            <view class="attach-grid">
              <view class="upload-zone" @click="pickImages">
                <uni-icons type="upload" size="48" color="#5f5e5e" />
                <text class="upload-main">点击上传图片</text>
                <text class="upload-sub">支持 JPG, PNG, WEBP（最大 10MB）</text>
              </view>
              <view v-if="attachments.length" class="thumb-row">
                <image
                  v-for="(p, i) in attachments"
                  :key="i"
                  class="thumb"
                  :src="p"
                  mode="aspectFill"
                  @click="removeImage(i)"
                />
              </view>
              <view class="attach-tip">
                <image class="tip-bg" :src="marketingThumb" mode="aspectFill" />
                <view class="tip-mask">
                  <text class="tip-txt">高画质图片将提升通告专业感</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 目标群体 -->
          <view class="card">
            <text class="card-kinetic">目标群体 / Audience</text>
            <view
              v-for="opt in audienceOptions"
              :key="opt.value"
              class="radio-card"
              :class="{ on: audience === opt.value }"
              @click="audience = opt.value"
            >
              <view class="radio-dot" :class="{ on: audience === opt.value }" />
              <text class="radio-lab">{{ opt.label }}</text>
            </view>
            <view v-if="audience === 'venue'" class="venue-pick">
              <text class="field-label">选择场馆</text>
              <picker mode="selector" :range="venueNames" :value="venueIndex" @change="onVenuePick">
                <view class="picker-line">
                  <uni-icons type="location" size="18" color="#5f5e5e" />
                  <text class="picker-val">{{ venueNames[venueIndex] }}</text>
                  <uni-icons type="arrowdown" size="14" color="#5f5e5e" />
                </view>
              </picker>
            </view>
          </view>

          <!-- 发布排期 -->
          <view class="card">
            <text class="card-kinetic">发布排期 / Schedule</text>
            <view class="field-block" :class="{ muted: immediatePublish }">
              <text class="field-label">发布时间</text>
              <view class="datetime-row">
                <uni-icons type="calendar" size="20" color="#5f5e5e" />
                <picker mode="date" :value="publishDate" @change="onDateChange">
                  <text class="dt-part">{{ publishDate }}</text>
                </picker>
                <view class="dt-sep" />
                <picker mode="time" :value="publishTime" @change="onTimeChange">
                  <text class="dt-part">{{ publishTime }}</text>
                </picker>
              </view>
            </view>
            <view class="switch-row">
              <view class="switch-texts">
                <text class="switch-title">立即发布</text>
                <text class="switch-sub">保存后立即同步至所有终端</text>
              </view>
              <switch
                :checked="immediatePublish"
                color="#a33e00"
                @change="onImmediateChange"
              />
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="actions">
            <button class="btn-publish" @click="onPublish">
              <text class="btn-publish-txt">立即发布</text>
              <uni-icons type="paperplane" size="22" color="#ffffff" />
            </button>
            <button class="btn-draft" @click="onSaveDraft">存为草稿</button>
          </view>

          <!-- 发布指南 -->
          <view class="guide-card">
            <view class="guide-head">
              <uni-icons type="info" size="16" color="#a33e00" />
              <text class="guide-title">发布指南</text>
            </view>
            <text class="guide-body">
              通知将通过系统推送、小程序弹窗及邮件形式发送至目标群体。请确保内容准确无误。
            </text>
          </view>

          <view class="footer-brand">羽擎管理系统</view>
          <view class="scroll-pad" />
        </view>
      </scroll-view>

      <!-- 发布成功 -->
      <view v-if="showSuccess" class="overlay" @click.self="closeSuccess">
        <view class="overlay-card">
          <view class="overlay-icon-wrap">
            <uni-icons type="checkbox-filled" size="56" color="#a33e00" />
          </view>
          <text class="overlay-title">发布成功</text>
          <text class="overlay-desc">您的通知已成功进入推送队列，预计将在 2 分钟内送达。</text>
          <button class="overlay-btn" @click="closeSuccess">返回列表</button>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const profileAvatar =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuAR1mkAx9nQKfBnQakGqEiXqO5tfDjsnI7lMqGdLb09vgk4xn_lvnkSQhWdXdNodUIQkzH7SYrS7q1jN9N9KmGNP9wMXC6qLNsrRIXxv-Zpjfddm3bjdB1rnR9faVPTYR_ZP2qmMYGE6hJz1XjWE_Yn8aEMCtOr7XY_6w19StRe2cRWNqc2qDWf3Niyuzv-2tGM11an2cE_Yz_EsFJ8e1XMOyoalf3LZ8JO4V-U6RU5n2fm4NwxI4AORiE2V5dG19QqFd6CqOafR31u'

const marketingThumb =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuC2Al_njB--9bpZFRspM0M3A3LKQB--uPiPloT6xLCGxxYFXI8y_ybU0cugsqUPMznm1nXHzCARAnekFOe7suvTLU0hLlzJ3v2QpbID6orarnb68Y_5eNQaajOx8_fLa07urwUrReYw6oc2T-K_NBJhnkPHXr4Tr2EX4pOBMrQNEegiV8a6JvA5Bo2OBK6MJpJuVx-1ZE2uDA9JpOTwDqt0V3PDQK4EJRkuGI-i11MPnkvLFbhUdoaa2_c1fP4hHHHnFBhuRIXs4DBB'

const form = reactive({
  title: '',
  body: ''
})

const audienceOptions = [
  { value: 'all' as const, label: '全体人员 (All)' },
  { value: 'members' as const, label: '注册会员 (Members)' },
  { value: 'venue' as const, label: '特定场馆 (Specific Venue)' }
]

const audience = ref<'all' | 'members' | 'venue'>('all')
const venueNames = ['旗舰馆 A', '训练馆 B', '社区球馆 C']
const venueIndex = ref(0)

const publishDate = ref('2026-04-10')
const publishTime = ref('09:00')
const immediatePublish = ref(true)

const attachments = ref<string[]>([])
const showSuccess = ref(false)

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.NOTIFICATION_LIST)
}

function onSettings() {
  uni.showToast({ title: '通知设置（占位）', icon: 'none' })
}

function onDateChange(e: { detail: { value: string } }) {
  publishDate.value = e.detail.value
}

function onTimeChange(e: { detail: { value: string } }) {
  publishTime.value = e.detail.value
}

function onImmediateChange(e: { detail: { value: boolean } }) {
  immediatePublish.value = e.detail.value
}

function onVenuePick(e: { detail: { value: string } }) {
  venueIndex.value = Number(e.detail.value) || 0
}

function pickImages() {
  uni.chooseImage({
    count: 9 - attachments.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const paths = res.tempFilePaths || []
      attachments.value = [...attachments.value, ...paths].slice(0, 9)
    }
  })
}

function removeImage(i: number) {
  attachments.value.splice(i, 1)
}

function onSaveDraft() {
  // TODO: POST 草稿
  uni.showToast({ title: '已存草稿（示例）', icon: 'success' })
}

function onPublish() {
  if (!form.title.trim()) {
    uni.showToast({ title: '请填写公告标题', icon: 'none' })
    return
  }
  if (!form.body.trim()) {
    uni.showToast({ title: '请填写详细内容', icon: 'none' })
    return
  }
  // TODO: POST 发布通知
  showSuccess.value = true
}

function closeSuccess() {
  showSuccess.value = false
  safeNavigateBack(PRESIDENT_PAGES.NOTIFICATION_LIST)
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
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f9f9f9;
  font-family: Lexend, 'PingFang SC', system-ui, sans-serif;
  color: #1a1c1c;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  flex-shrink: 0;
  background: #f9f9f9;
  padding: 20rpx 28rpx 16rpx;
  z-index: 50;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.nav-icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  &:active {
    background: #e8e8e8;
  }
}

.nav-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #e2e2e2;
  flex-shrink: 0;
}

.nav-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 900;
  letter-spacing: -0.02em;
  text-transform: uppercase;
  color: #1a1c1c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-scroll {
  flex: 1;
  height: 0;
  min-height: 200rpx;
}

.scroll-inner {
  padding: 24rpx 32rpx 48rpx;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.04);
}

.card-kinetic {
  display: block;
  font-size: 22rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 40rpx;
}

.field-block {
  margin-bottom: 40rpx;
  &.muted {
    opacity: 0.45;
    pointer-events: none;
  }
}

.field-label {
  display: block;
  font-size: 20rpx;
  font-weight: 600;
  color: #5f5e5e;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 12rpx;
}

.input-underline,
.textarea-underline {
  width: 100%;
  padding: 16rpx 0;
  border: none;
  border-bottom: 4rpx solid #e2e2e2;
  background: transparent;
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1c1c;
  box-sizing: border-box;
}

.textarea-underline {
  min-height: 280rpx;
  font-size: 28rpx;
  font-weight: 500;
  line-height: 1.55;
}

.ph {
  color: rgba(95, 94, 94, 0.45);
}

.attach-grid {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.upload-zone {
  border: 4rpx dashed #e3bfb1;
  border-radius: 16rpx;
  padding: 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f3f3f3;
  &:active {
    background: #e8e8e8;
  }
}

.upload-main {
  margin-top: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.upload-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}

.thumb-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background: #eee;
}

.attach-tip {
  position: relative;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #e8e8e8;
}

.tip-bg {
  width: 100%;
  height: 100%;
  opacity: 0.35;
}

.tip-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  background: rgba(163, 62, 0, 0.08);
}

.tip-txt {
  font-size: 20rpx;
  font-weight: 800;
  color: #a33e00;
  text-align: center;
  line-height: 1.4;
  text-transform: uppercase;
}

.radio-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  background: #f3f3f3;
  border: 2rpx solid transparent;
  &:last-of-type {
    margin-bottom: 0;
  }
}

.radio-card.on {
  background: rgba(163, 62, 0, 0.06);
  border-color: rgba(163, 62, 0, 0.2);
}

.radio-dot {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  border: 4rpx solid #c8c6c5;
  box-sizing: border-box;
}

.radio-dot.on {
  border-color: #a33e00;
  background: #a33e00;
  box-shadow: inset 0 0 0 6rpx #fff;
}

.radio-lab {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.venue-pick {
  margin-top: 28rpx;
  padding-top: 28rpx;
  border-top: 2rpx solid #eeeeee;
}

.picker-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 0;
  border-bottom: 4rpx solid #e2e2e2;
}

.picker-val {
  flex: 1;
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.datetime-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 12rpx 0;
  border-bottom: 4rpx solid #e2e2e2;
}

.dt-part {
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
  min-width: 160rpx;
}

.dt-sep {
  width: 2rpx;
  height: 28rpx;
  background: #e2e2e2;
}

.switch-row {
  margin-top: 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid rgba(142, 113, 100, 0.15);
  background: #f3f3f3;
}

.switch-title {
  display: block;
  font-size: 24rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.switch-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}

.actions {
  margin-bottom: 28rpx;
}

.btn-publish {
  width: 100%;
  height: 100rpx;
  border-radius: 16rpx;
  border: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: linear-gradient(45deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 16rpx 40rpx rgba(163, 62, 0, 0.22);
  &::after {
    border: none;
  }
}

.btn-publish-txt {
  font-size: 30rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.btn-draft {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  margin-top: 20rpx;
  border-radius: 16rpx;
  background: #e2e2e2;
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
  border: none;
  &::after {
    border: none;
  }
}

.guide-card {
  padding: 36rpx 32rpx;
  border-radius: 24rpx;
  background: rgba(163, 62, 0, 0.06);
  border: 2rpx solid rgba(163, 62, 0, 0.12);
  margin-bottom: 32rpx;
}

.guide-head {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 12rpx;
}

.guide-title {
  font-size: 20rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.guide-body {
  font-size: 22rpx;
  color: rgba(26, 28, 28, 0.72);
  line-height: 1.55;
}

.footer-brand {
  text-align: center;
  font-size: 20rpx;
  font-weight: 800;
  color: rgba(95, 94, 94, 0.35);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  padding: 24rpx 0 8rpx;
}

.scroll-pad {
  height: 32rpx;
}

.overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
  background: rgba(249, 249, 249, 0.85);
  backdrop-filter: blur(8px);
}

.overlay-card {
  width: 100%;
  max-width: 600rpx;
  padding: 64rpx 48rpx;
  background: #ffffff;
  border-radius: 32rpx;
  box-shadow: 0 24rpx 64rpx rgba(0, 0, 0, 0.12);
  text-align: center;
}

.overlay-icon-wrap {
  width: 140rpx;
  height: 140rpx;
  margin: 0 auto 32rpx;
  border-radius: 50%;
  background: rgba(163, 62, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.overlay-title {
  display: block;
  font-size: 40rpx;
  font-weight: 900;
  text-transform: uppercase;
  color: #1a1c1c;
  margin-bottom: 16rpx;
}

.overlay-desc {
  display: block;
  font-size: 26rpx;
  color: #5f5e5e;
  line-height: 1.5;
  margin-bottom: 48rpx;
}

.overlay-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 56rpx;
  border-radius: 9999rpx;
  background: #e2e2e2;
  font-size: 24rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border: none;
  margin: 0;
  &::after {
    border: none;
  }
}
</style>
