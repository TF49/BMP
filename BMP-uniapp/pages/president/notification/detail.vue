<template>
  <PresidentLayout :showTabBar="false" className="president-notification-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
            <text class="brand-title">BMP Executive</text>
          </view>
          <view class="icon-btn" @click="onSettings">
            <uni-icons type="gear" size="22" color="#ff6600" />
          </view>
        </view>
        <view class="nav-divider" />
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view class="head-section">
            <view class="tag-row">
              <text class="official-pill">{{ detail.badgeLabel }}</text>
              <text class="id-tag">{{ detail.idLabel }}</text>
            </view>
            <text class="main-title">{{ detail.title }}</text>
            <view class="meta-row">
              <view class="meta-item">
                <uni-icons type="calendar" size="18" color="#5f5e5e" />
                <text class="meta-t">{{ detail.publishedAt }}</text>
              </view>
              <view class="meta-item">
                <uni-icons type="staff" size="18" color="#5f5e5e" />
                <text class="meta-t">{{ detail.targetLabel }}</text>
              </view>
            </view>
          </view>

          <view class="stats-grid">
            <view class="stat-card">
              <text class="stat-k">TOTAL SENT</text>
              <view class="stat-row">
                <text class="stat-num">{{ detail.totalSent }}</text>
                <view class="stat-ico">
                  <uni-icons type="paperplane" size="22" color="#5f5e5e" />
                </view>
              </view>
            </view>
            <view class="stat-card stat-read">
              <text class="stat-k stat-k-read">TOTAL READ</text>
              <view class="stat-row">
                <text class="stat-num">{{ detail.totalRead }}</text>
                <text class="read-rate">{{ detail.readRate }}</text>
              </view>
            </view>
            <view class="stat-card">
              <text class="stat-k">UNREAD</text>
              <view class="stat-row">
                <text class="stat-num">{{ detail.unread }}</text>
                <view class="avatar-stack">
                  <view class="av av-1" />
                  <view class="av av-2" />
                </view>
              </view>
            </view>
          </view>

          <view class="visual-card">
            <image
              v-if="!bannerFailed"
              class="visual-img"
              :src="detail.bannerImage"
              mode="aspectFill"
              @error="bannerFailed = true"
            />
            <view class="visual-overlay" />
            <view class="visual-text">
              <text class="visual-k">{{ detail.visualKicker }}</text>
              <text class="visual-title">{{ detail.visualTitle }}</text>
            </view>
          </view>

          <view class="body-card">
            <view class="body-head">
              <view class="body-bar" />
              <text class="body-title">Detailed Message</text>
            </view>
            <view class="body-content">
              <text class="p">尊敬的各位会员：</text>
              <text class="p">
                为了庆祝即将到来的2026年国庆节，同时也为了确保场馆设施的维护与保养，BMP Executive
                旗下各直属场馆的营业时间将进行如下调整：
              </text>
              <view class="bullet-list">
                <text class="li">
                  <text class="li-strong">10月1日 - 10月3日：</text>
                  仅下午场开放 (14:00 - 18:00)
                </text>
                <text class="li">
                  <text class="li-strong">10月4日 - 10月7日：</text>
                  全天正常营业 (08:00 - 22:00)
                </text>
              </view>
              <text class="p">
                请各位会员提前通过APP预约系统合理安排您的运动时间。在此期间，VIP专属通道将保持畅通，我们将一如既往地为您提供高品质的运动服务。祝您节日快乐，球技大增！
              </text>
              <view class="body-footer">
                <text class="sign">BMP 行政管理部 宣</text>
              </view>
            </view>
          </view>

          <view class="scroll-pad" />
        </view>
      </scroll-view>

      <view class="footer-bar">
        <button class="btn-edit" @click="onEditAgain">
          <uni-icons type="compose" size="18" color="#1a1c1c" />
          <text>二次编辑</text>
        </button>
        <button class="btn-withdraw" @click="onWithdraw">
          <uni-icons type="redo" size="18" color="#ffffff" />
          <text>撤回通知</text>
        </button>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

interface NotificationDetailMock {
  noticeId: string
  badgeLabel: string
  idLabel: string
  title: string
  publishedAt: string
  targetLabel: string
  totalSent: string
  totalRead: string
  readRate: string
  unread: string
  visualKicker: string
  visualTitle: string
  bannerImage: string
}

const noticeId = ref('')
const bannerFailed = ref(false)

function mockDetail(): NotificationDetailMock {
  return {
    noticeId: noticeId.value || 'demo',
    badgeLabel: 'OFFICIAL NOTICE',
    idLabel: 'ID: #NOTIF-2026-089',
    title: '关于国庆节期间场馆营业时间调整的通知',
    publishedAt: '2026-09-25 10:00',
    targetLabel: 'Target: All Members / Specific Venues',
    totalSent: '1,540',
    totalRead: '1,200',
    readRate: '78% Rate',
    unread: '340',
    visualKicker: 'VENUE UPDATES',
    visualTitle: 'Holiday Schedule Alignment',
    bannerImage:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuD4gW1_VLOF26IqmmXU00V-MRZdSPXiO5PozjYEPWMsQ4-_Pxs1U6SRaSegt6kmMl_Dp2kr0JQAk4kGPNSov0ClqPzWA54HAVifdmJS0DUR88LXjPNRDx2VBbtRLr027FLQm6VVbiobSr6ZXRfLoTP5wq8bURDWa6_guEGW7EAyGR__T_TcLbIjqmGmNoT4NTPTpkfpEqAjuZMWEryO5yosNtIR7gdEpF0ARMCz0lVYf1YAB-jCH8KHYoNOwNXjl3oPnL2QA5B--RqO'
  }
}

const detail = ref<NotificationDetailMock>(mockDetail())

onLoad((options) => {
  if (options?.id) {
    noticeId.value = String(options.id)
    detail.value = mockDetail()
  }
  // TODO: GET /api/president/notification/:id
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.NOTIFICATION_LIST)
}

function onSettings() {
  uni.showToast({ title: '通知设置开发中', icon: 'none' })
}

function onEditAgain() {
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.NOTIFICATION_FORM}?id=${encodeURIComponent(detail.value.noticeId)}&mode=edit`
  })
}

function onWithdraw() {
  uni.showModal({
    title: '撤回通知',
    content: '确定撤回该通知？已读用户仍将保留本地记录提示。',
    success: (res) => {
      if (!res.confirm) return
      // TODO: 撤回接口
      uni.showToast({ title: '已提交撤回（示例）', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
.president-notification-detail-layout {
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
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  flex-shrink: 0;
  background: #f9f9f9;
  z-index: 50;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 48rpx 20rpx;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.brand-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -0.02em;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background: rgba(0, 0, 0, 0.06);
  }
}

.nav-divider {
  height: 2rpx;
  background: #f3f3f3;
  margin: 0 48rpx;
}

.main-scroll {
  flex: 1;
  height: 0;
  min-height: 200rpx;
}

.scroll-inner {
  padding: 32rpx 48rpx 24rpx;
  padding-bottom: 240rpx;
}

.scroll-pad {
  height: 16rpx;
}

.head-section {
  margin-bottom: 48rpx;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.official-pill {
  padding: 8rpx 20rpx;
  border-radius: 9999px;
  background: #ff6600;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.id-tag {
  font-size: 24rpx;
  font-weight: 600;
  color: #5f5e5e;
}

.main-title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
  line-height: 1.35;
  margin-bottom: 24rpx;
}

.meta-row {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.meta-t {
  font-size: 26rpx;
  font-weight: 600;
  color: #5f5e5e;
}

.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 48rpx;
}

.stat-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 36rpx 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.stat-read {
  border-bottom: 8rpx solid #ff6600;
}

.stat-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
  margin-bottom: 12rpx;
}

.stat-k-read {
  color: #a33e00;
}

.stat-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.stat-num {
  font-size: 56rpx;
  font-weight: 900;
  color: #1a1c1c;
  letter-spacing: -0.02em;
}

.stat-ico {
  padding: 12rpx;
  background: #e8e8e8;
  border-radius: 12rpx;
}

.read-rate {
  font-size: 26rpx;
  font-weight: 800;
  color: #ff6600;
  margin-bottom: 8rpx;
}

.avatar-stack {
  display: flex;
  flex-direction: row-reverse;
  margin-right: 12rpx;
}

.av {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  margin-left: -16rpx;
}

.av-1 {
  background: #e2e2e2;
}

.av-2 {
  background: #c8c6c5;
}

.visual-card {
  position: relative;
  height: 280rpx;
  border-radius: 32rpx;
  overflow: hidden;
  margin-bottom: 48rpx;
  background: #e8e8e8;
}

.visual-img {
  width: 100%;
  height: 100%;
  opacity: 0.55;
}

.visual-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #f9f9f9 0%, transparent 55%);
}

.visual-text {
  position: absolute;
  left: 32rpx;
  bottom: 32rpx;
  right: 32rpx;
}

.visual-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #ff6600;
  letter-spacing: 0.14em;
  margin-bottom: 8rpx;
}

.visual-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.body-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 48rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.body-head {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.body-bar {
  width: 8rpx;
  height: 40rpx;
  background: #a33e00;
  border-radius: 9999px;
}

.body-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.body-content {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.p {
  font-size: 28rpx;
  line-height: 1.65;
  color: #5a4136;
}

.bullet-list {
  padding-left: 8rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.li {
  font-size: 28rpx;
  line-height: 1.55;
  color: #5a4136;
  padding-left: 24rpx;
  position: relative;
}

.li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #a33e00;
  font-weight: 800;
}

.li-strong {
  font-weight: 800;
  color: #1a1c1c;
}

.body-footer {
  margin-top: 16rpx;
  padding-top: 32rpx;
  border-top: 2rpx solid #e8e8e8;
}

.sign {
  font-size: 24rpx;
  font-style: italic;
  color: #5f5e5e;
  font-weight: 600;
}

.footer-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  padding: 20rpx 48rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(249, 249, 249, 0.96);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.btn-edit {
  width: 100%;
  height: 96rpx;
  border-radius: 16rpx;
  background: #e8e8e8;
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: 0.08em;
  border: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  &::after {
    border: none;
  }
}

.btn-withdraw {
  width: 100%;
  height: 96rpx;
  border-radius: 16rpx;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
  font-size: 26rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.08em;
  border: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.25);
  &::after {
    border: none;
  }
}
</style>
