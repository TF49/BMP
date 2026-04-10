<template>
  <PresidentLayout :showTabBar="false" className="president-tournament-reg-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="icon-btn">
              <uni-icons type="arrow-left" size="24" color="#ff6600" />
            </view>
            <text class="nav-title">详情回顾</text>
          </view>
          <view class="icon-btn" @click="onShare">
            <uni-icons type="paperplane" size="22" color="#5f5e5e" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <!-- 赛事主卡片 -->
          <view class="card card-white hero-tournament">
            <view class="hero-top">
              <text class="pill-tournament">{{ detail.tournamentPill }}</text>
              <view class="pill-reviewed">
                <uni-icons type="checkbox-filled" size="14" color="#a33e00" />
                <text class="reviewed-text">{{ detail.reviewStatusLabel }}</text>
              </view>
            </view>
            <text class="tournament-title">{{ detail.tournamentName }}</text>
            <view class="tournament-meta">
              <view class="meta-item">
                <uni-icons type="flag" size="14" color="#5f5e5e" />
                <text class="meta-t">{{ detail.categoryLabel }}</text>
              </view>
              <view class="meta-item">
                <uni-icons type="location" size="14" color="#5f5e5e" />
                <text class="meta-t">{{ detail.venueName }}</text>
              </view>
            </view>
            <uni-icons type="star-filled" size="120" color="#a33e00" class="hero-watermark" />
          </view>

          <!-- 运动员资料 -->
          <view class="card card-white athlete-card">
            <view class="athlete-row">
              <view class="avatar-wrap">
                <image
                  class="athlete-avatar"
                  :src="detail.athleteAvatar"
                  mode="aspectFill"
                  @error="onAvatarError"
                />
                <view class="avatar-badge">
                  <uni-icons type="medal" size="16" color="#ffffff" />
                </view>
              </view>
              <view class="athlete-info">
                <text class="athlete-label">ATHLETE PROFILE</text>
                <text class="athlete-name">{{ detail.athleteName }}</text>
                <text class="athlete-sub">{{ detail.athleteSub }}</text>
              </view>
            </view>
          </view>

          <!-- 积分排名 -->
          <view class="rank-card">
            <text class="rank-label">RANK SCORE</text>
            <view class="rank-value-row">
              <text class="rank-num">{{ detail.rankScore }}</text>
              <text class="rank-unit">PTS</text>
            </view>
            <view class="rank-badge">
              <uni-icons type="arrow-up" size="12" color="#ffffff" />
              <text class="rank-badge-t">{{ detail.rankBadge }}</text>
            </view>
          </view>

          <!-- 历史战绩 -->
          <view class="card card-muted records-section">
            <text class="section-label">HISTORICAL RECORDS</text>
            <view class="records-list">
              <view class="record-row" v-for="rec in detail.records" :key="rec.title">
                <view class="record-left">
                  <view class="record-icon-wrap">
                    <uni-icons :type="rec.icon" size="16" color="#5f5e5e" />
                  </view>
                  <text class="record-name">{{ rec.title }}</text>
                </view>
                <text class="record-result" :class="rec.resultClass">{{ rec.result }}</text>
              </view>
            </view>
          </view>

          <!-- 支付与财务 -->
          <view class="card card-white pay-card">
            <view class="pay-head">
              <uni-icons type="list" size="18" color="#a33e00" />
              <text class="pay-title">支付与财务详情</text>
            </view>
            <view class="pay-body">
              <view class="pay-block">
                <text class="pay-k">TRANSACTION ID</text>
                <text class="pay-mono">{{ detail.transactionId }}</text>
              </view>
              <view class="pay-block">
                <text class="pay-k">ENTRY FEE</text>
                <view class="pay-fee-row">
                  <text class="pay-fee">{{ detail.entryFee }}</text>
                  <text class="success-badge">{{ detail.payStatusLabel }}</text>
                </view>
              </view>
              <view class="pay-block">
                <text class="pay-k">TIMESTAMP</text>
                <text class="pay-time">{{ detail.payTime }}</text>
              </view>
            </view>
          </view>

          <view class="scroll-pad" />
        </view>
      </scroll-view>

      <view class="footer-bar">
        <view class="footer-inner">
          <button class="btn-reject" @click="onReject">拒绝申请</button>
          <button class="btn-approve" @click="onApprove">审核通过</button>
        </view>
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

export interface TournamentRegRecordRow {
  icon: string
  title: string
  result: string
  resultClass: 'result-gold' | 'result-bronze'
}

export interface PresidentTournamentRegistrationDetail {
  id: string
  tournamentPill: string
  reviewStatusLabel: string
  tournamentName: string
  categoryLabel: string
  venueName: string
  athleteAvatar: string
  athleteName: string
  athleteSub: string
  rankScore: string
  rankBadge: string
  records: TournamentRegRecordRow[]
  transactionId: string
  entryFee: string
  payStatusLabel: string
  payTime: string
}

const registrationId = ref('')

const mockDetail = (): PresidentTournamentRegistrationDetail => ({
  id: registrationId.value || 'demo-1',
  tournamentPill: 'TOURNAMENT DETAILS',
  reviewStatusLabel: '已审核',
  tournamentName: '2026夏季羽毛球大师赛',
  categoryLabel: '男子单打 (A组)',
  venueName: '国家体育馆羽毛球中心',
  athleteAvatar:
    'https://lh3.googleusercontent.com/aida-public/AB6AXuDw0phIJV_xkg3B7It5ZeN3Qdj188p8njmcNaY04XcpB5M97kTIhZryX5mrjGQ61bvA-tcXdEPlGYIa1QIM9h90smLmdIuZS5NiottixgHQjLQp3C-FgN7Tu9LVl0PQzdqTQ0xQaycJ-8a7lGK5IOrGPf1bVny0HvNgX4-8RLlo2FaeoxQooyv0U6e9uah8fYeBMAbpCdZnWaBlnO2yF3kg1XeLJPb_Y-6o_4MkGjkqu3jcZPLMLhodaZhxnuuKxYQ2KDKlXJxFEMKG',
  athleteName: '陈子昂 (Leon Chen)',
  athleteSub: '职业组 | 注册球员 #88201',
  rankScore: '2,840',
  rankBadge: 'TOP 5%',
  records: [
    {
      icon: 'medal',
      title: '2025 全省公开赛',
      result: '冠军 (Champion)',
      resultClass: 'result-gold'
    },
    {
      icon: 'star-filled',
      title: '2024 春季精英挑战赛',
      result: '季军 (Top 3)',
      resultClass: 'result-bronze'
    }
  ],
  transactionId: 'TXN-20260421-988310024',
  entryFee: '¥ 199.00',
  payStatusLabel: 'SUCCESS',
  payTime: '2026-04-15 14:32:10'
})

const detail = ref<PresidentTournamentRegistrationDetail>(mockDetail())

onLoad((options) => {
  if (options?.id) {
    registrationId.value = String(options.id)
    detail.value = mockDetail()
  }
  // TODO: getPresidentTournamentRegistrationDetail(registrationId.value)
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.TOURNAMENT_REGISTRATION_LIST)
}

function onShare() {
  const summary = `${detail.value.tournamentName} · ${detail.value.athleteName}`
  uni.setClipboardData({
    data: summary,
    success: () => uni.showToast({ title: '已复制摘要', icon: 'none' }),
    fail: () => uni.showToast({ title: '分享开发中', icon: 'none' })
  })
}

function onAvatarError() {
  detail.value = { ...detail.value, athleteAvatar: '/static/placeholders/avatar.svg' }
}

function onReject() {
  uni.showModal({
    title: '拒绝申请',
    content: '确认拒绝该报名申请？',
    success: (res) => {
      if (!res.confirm) return
      // TODO: 拒绝接口
      uni.showToast({ title: '已提交（示例）', icon: 'none' })
    }
  })
}

function onApprove() {
  uni.showModal({
    title: '审核通过',
    content: '确认通过该报名申请？',
    success: (res) => {
      if (!res.confirm) return
      // TODO: 通过接口
      uni.showToast({ title: '已提交（示例）', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
.president-tournament-reg-detail-layout {
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
  background-color: #f9f9f9;
  font-family: Lexend, 'PingFang SC', system-ui, sans-serif;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f9f9f9;
}

.nav-header {
  flex-shrink: 0;
  z-index: 50;
  background-color: #f9f9f9;
  padding: 16rpx 48rpx 24rpx;
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
}

.nav-title {
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
    background-color: rgba(0, 0, 0, 0.06);
  }
}

.main-scroll {
  flex: 1;
  height: 0;
  min-height: 200rpx;
}

.scroll-inner {
  padding: 8rpx 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.scroll-pad {
  height: 32rpx;
}

.card {
  border-radius: 24rpx;
  padding: 48rpx;
  box-sizing: border-box;
}

.card-white {
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(2, 6, 23, 0.06);
}

.card-muted {
  background: #f3f3f3;
}

.hero-tournament {
  position: relative;
  overflow: hidden;
  border-left: 8rpx solid #a33e00;
}

.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.pill-tournament {
  padding: 8rpx 16rpx;
  border-radius: 9999px;
  background: #ffdbcd;
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.pill-reviewed {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 20rpx;
  border-radius: 9999px;
  background: #e8e8e8;
  flex-shrink: 0;
}

.reviewed-text {
  font-size: 20rpx;
  font-weight: 800;
  color: #5a4136;
}

.tournament-title {
  display: block;
  margin-top: 24rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
  line-height: 1.3;
}

.tournament-meta {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 32rpx;
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
  flex: 1;
}

.hero-watermark {
  position: absolute;
  right: -32rpx;
  bottom: -32rpx;
  opacity: 0.06;
  pointer-events: none;
}

.athlete-card {
  padding: 40rpx 48rpx;
}

.athlete-row {
  display: flex;
  align-items: center;
  gap: 36rpx;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.athlete-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 20rpx;
  background: #e8e8e8;
  filter: grayscale(100%);
}

.avatar-badge {
  position: absolute;
  right: -8rpx;
  bottom: -8rpx;
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  background: #a33e00;
  display: flex;
  align-items: center;
  justify-content: center;
}

.athlete-info {
  flex: 1;
  min-width: 0;
}

.athlete-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
}

.athlete-name {
  display: block;
  margin-top: 12rpx;
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.athlete-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #5a4136;
}

.rank-card {
  border-radius: 24rpx;
  padding: 48rpx;
  background: #a33e00;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  box-shadow: 0 12rpx 32rpx rgba(163, 62, 0, 0.28);
}

.rank-label {
  font-size: 20rpx;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 0.14em;
}

.rank-value-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.rank-num {
  font-size: 72rpx;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -0.02em;
}

.rank-unit {
  font-size: 24rpx;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}

.rank-badge {
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.22);
  margin-top: 8rpx;
}

.rank-badge-t {
  font-size: 20rpx;
  font-weight: 800;
  color: #ffffff;
}

.records-section {
  padding: 40rpx 48rpx;
}

.section-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.14em;
  margin-bottom: 28rpx;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.record-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
}

.record-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  min-width: 0;
}

.record-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #e2dfde;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.record-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.record-result {
  font-size: 26rpx;
  font-weight: 800;
  flex-shrink: 0;
  text-align: right;
}

.result-gold {
  color: #a33e00;
}

.result-bronze {
  color: #5a4136;
}

.pay-card {
  padding: 40rpx 48rpx;
}

.pay-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 36rpx;
}

.pay-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.pay-body {
  padding-left: 24rpx;
  border-left: 4rpx solid #e8e8e8;
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.pay-block {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.pay-k {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.08em;
}

.pay-mono {
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
  font-family: ui-monospace, monospace;
}

.pay-fee-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.pay-fee {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.success-badge {
  padding: 6rpx 16rpx;
  border-radius: 9999px;
  background: #dcfce7;
  color: #15803d;
  font-size: 20rpx;
  font-weight: 800;
}

.pay-time {
  font-size: 26rpx;
  color: #5a4136;
}

.footer-bar {
  flex-shrink: 0;
  padding: 24rpx 48rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(249, 249, 249, 0.88);
  backdrop-filter: blur(24px);
  box-shadow: 0 -16rpx 60rpx rgba(0, 0, 0, 0.04);
}

.footer-inner {
  display: flex;
  gap: 28rpx;
}

.btn-reject {
  flex: 1;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
  background: #ffffff;
  border: 2rpx solid #e3bfb1;
  margin: 0;
  padding: 0;
  &::after {
    border: none;
  }
}

.btn-approve {
  flex: 1;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #ffffff;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.25);
  border: none;
  margin: 0;
  padding: 0;
  &::after {
    border: none;
  }
}
</style>
