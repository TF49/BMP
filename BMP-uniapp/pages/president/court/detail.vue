<template>
  <PresidentLayout :showTabBar="false" className="president-court-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
            <text class="nav-sub">场地详情</text>
          </view>
          <view class="nav-right">
            <text class="brand">BMP Executive</text>
            <view class="icon-btn" @click="onSettings">
              <uni-icons type="gear" size="22" color="#ff6600" />
            </view>
          </view>
        </view>
        <view class="nav-divider" />
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view class="hero-block">
            <view class="hero-text">
              <view class="hero-badges">
                <text class="status-pill">{{ detail.statusLabel }}</text>
                <text class="tier-label">{{ detail.tierLabel }}</text>
              </view>
              <text class="court-title">{{ detail.courtTitle }}</text>
            </view>
            <button class="btn-maint" @click="onMaintenanceRegister">
              <uni-icons type="compose" size="20" color="#ffffff" />
              <text>维护登记</text>
            </button>
          </view>

          <view class="config-card">
            <view class="config-head">
              <text class="config-title">场地配置</text>
              <uni-icons type="info" size="22" color="#e3bfb1" />
            </view>
            <view class="config-grid">
              <view>
                <text class="cfg-k">BILLING TYPE</text>
                <text class="cfg-v">{{ detail.billingType }}</text>
              </view>
              <view>
                <text class="cfg-k">PRICE RATE</text>
                <view class="price-row">
                  <text class="price-num">{{ detail.priceRate }}</text>
                  <text class="price-unit">/ 小时</text>
                </view>
              </view>
            </view>
            <view class="equip-block">
              <text class="cfg-k equip-k">EQUIPMENT STATUS</text>
              <view class="equip-tags">
                <view class="equip-tag" v-for="tag in detail.equipmentTags" :key="tag.label">
                  <uni-icons :type="tag.icon" size="18" color="#a33e00" />
                  <text class="equip-t">{{ tag.label }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="stats-col">
            <view class="revenue-card">
              <view class="rev-inner">
                <text class="rev-k">TODAY'S REVENUE</text>
                <text class="rev-val">{{ detail.todayRevenue }}</text>
                <view class="rev-trend">
                  <uni-icons type="arrow-up" size="16" color="#ff6600" />
                  <text class="rev-trend-t">{{ detail.revenueTrend }}</text>
                </view>
              </view>
              <uni-icons type="wallet" size="120" color="#ffffff" class="rev-watermark" />
            </view>
            <view class="bookings-card">
              <text class="bk-k">TOTAL BOOKINGS</text>
              <view class="bk-row">
                <text class="bk-num">{{ detail.totalBookings }}</text>
                <text class="bk-unit">次预订</text>
              </view>
              <view class="bk-track">
                <view class="bk-fill" :style="{ width: detail.loadPercent + '%' }" />
              </view>
              <text class="bk-sub">今日负荷率 {{ detail.loadPercent }}%</text>
            </view>
          </view>

          <view class="log-card">
            <view class="log-head">
              <text class="log-title">维护日志</text>
              <text class="log-all" @click="onViewAllLogs">查看全部</text>
            </view>
            <view class="log-list">
              <view class="log-item" v-for="log in detail.maintenanceLogs" :key="log.id">
                <view class="log-left">
                  <view class="log-ico">
                    <uni-icons :type="log.icon" size="22" color="#a33e00" />
                  </view>
                  <view>
                    <text class="log-name">{{ log.title }}</text>
                    <text class="log-op">操作人: {{ log.operator }}</text>
                  </view>
                </view>
                <view class="log-right">
                  <text class="log-date">{{ log.date }}</text>
                  <text class="log-done">{{ log.statusLabel }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="visual-footer">
            <image
              v-if="!bannerFailed"
              class="visual-img"
              :src="detail.bannerImage"
              mode="aspectFill"
              @error="bannerFailed = true"
            />
            <view class="visual-fade" />
            <text class="visual-caption">COURT {{ detail.courtCode }}</text>
          </view>

          <view class="bottom-pad" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

interface EquipTag {
  icon: string
  label: string
}

interface MaintLog {
  id: string
  icon: string
  title: string
  operator: string
  date: string
  statusLabel: string
}

interface CourtDetailMock {
  courtId: string
  courtCode: string
  statusLabel: string
  tierLabel: string
  courtTitle: string
  billingType: string
  priceRate: string
  equipmentTags: EquipTag[]
  todayRevenue: string
  revenueTrend: string
  totalBookings: string
  loadPercent: number
  maintenanceLogs: MaintLog[]
  bannerImage: string
}

const courtId = ref('')
const bannerFailed = ref(false)

function mockDetail(): CourtDetailMock {
  const id = courtId.value || 'demo'
  return {
    courtId: id,
    courtCode: '#A1',
    statusLabel: '使用中',
    tierLabel: 'PREMIUM COURT',
    courtTitle: '#A1 VIP 场地',
    billingType: '按小时计费',
    priceRate: '¥80',
    equipmentTags: [
      { icon: 'star-filled', label: '灯光正常' },
      { icon: 'checkbox-filled', label: '地面防滑良好' }
    ],
    todayRevenue: '¥640',
    revenueTrend: '较昨日上涨 12%',
    totalBookings: '8',
    loadPercent: 75,
    maintenanceLogs: [
      {
        id: '1',
        icon: 'settings',
        title: '灯管更换',
        operator: '管理员张三',
        date: '2026-02-10',
        statusLabel: '已完成'
      },
      {
        id: '2',
        icon: 'loop',
        title: '地板深度清洁与防滑维护',
        operator: '外包保洁服务',
        date: '2026-01-25',
        statusLabel: '已完成'
      },
      {
        id: '3',
        icon: 'compose',
        title: '网架稳固性检查',
        operator: '巡检组',
        date: '2026-01-12',
        statusLabel: '已完成'
      }
    ],
    bannerImage:
      '/static/placeholders/hero.svg'
  }
}

const detail = ref<CourtDetailMock>(mockDetail())

onLoad((options) => {
  if (options?.id) {
    courtId.value = decodeURIComponent(String(options.id))
    detail.value = mockDetail()
    // TODO: 按 courtId 拉取场地详情
  }
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURT_LIST)
}

function onSettings() {
  uni.showToast({ title: '场地设置开发中', icon: 'none' })
}

function onMaintenanceRegister() {
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.COURT_FORM}?id=${encodeURIComponent(detail.value.courtId)}&from=detail`
  })
}

function onViewAllLogs() {
  uni.showToast({ title: '维护日志列表开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.president-court-detail-layout {
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

.nav-sub {
  font-size: 24rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.brand {
  font-size: 32rpx;
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
  padding: 32rpx 48rpx 48rpx;
}

.hero-block {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  margin-bottom: 40rpx;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.status-pill {
  padding: 10rpx 24rpx;
  border-radius: 9999px;
  background: #ff6600;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.1em;
}

.tier-label {
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.court-title {
  display: block;
  font-size: 72rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -0.04em;
  line-height: 1.1;
}

.btn-maint {
  width: 100%;
  height: 100rpx;
  border-radius: 16rpx;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 800;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.25);
  margin: 0;
  padding: 0;
  &::after {
    border: none;
  }
}

.config-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.config-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.config-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.config-grid {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.cfg-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
}

.cfg-v {
  display: block;
  margin-top: 8rpx;
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.price-row {
  margin-top: 8rpx;
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.price-num {
  font-size: 36rpx;
  font-weight: 600;
  color: #a33e00;
}

.price-unit {
  font-size: 26rpx;
  color: #5f5e5e;
}

.equip-block {
  margin-top: 40rpx;
  padding-top: 40rpx;
  border-top: 2rpx solid #eeeeee;
}

.equip-k {
  margin-bottom: 24rpx;
}

.equip-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
}

.equip-tag {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 28rpx;
  background: #eeeeee;
  border-radius: 16rpx;
}

.equip-t {
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.stats-col {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.revenue-card {
  position: relative;
  overflow: hidden;
  background: #1a1c1c;
  border-radius: 24rpx;
  padding: 48rpx;
}

.rev-inner {
  position: relative;
  z-index: 2;
}

.rev-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: rgba(249, 249, 249, 0.7);
  letter-spacing: 0.1em;
}

.rev-val {
  display: block;
  margin-top: 12rpx;
  font-size: 72rpx;
  font-weight: 800;
  color: #f9f9f9;
  letter-spacing: -0.03em;
}

.rev-trend {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 28rpx;
}

.rev-trend-t {
  font-size: 26rpx;
  color: #f9f9f9;
}

.rev-watermark {
  position: absolute;
  right: -16rpx;
  bottom: -16rpx;
  opacity: 0.1;
  pointer-events: none;
}

.bookings-card {
  background: #f3f3f3;
  border-radius: 24rpx;
  padding: 48rpx;
}

.bk-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
}

.bk-row {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 8rpx;
}

.bk-num {
  font-size: 56rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.bk-unit {
  font-size: 26rpx;
  color: #5f5e5e;
}

.bk-track {
  margin-top: 24rpx;
  height: 8rpx;
  background: #e8e8e8;
  border-radius: 9999px;
  overflow: hidden;
}

.bk-fill {
  height: 100%;
  background: #ff6600;
  border-radius: 9999px;
}

.bk-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}

.log-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.log-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.log-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.log-all {
  font-size: 26rpx;
  font-weight: 800;
  color: #a33e00;
}

.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
  padding: 32rpx 0;
  border-bottom: 2rpx solid #f3f3f3;
}

.log-item:last-child {
  border-bottom: none;
}

.log-left {
  display: flex;
  align-items: center;
  gap: 28rpx;
  flex: 1;
  min-width: 0;
}

.log-ico {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  background: #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.log-name {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.log-op {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}

.log-right {
  text-align: right;
  flex-shrink: 0;
}

.log-date {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.log-done {
  display: inline-block;
  margin-top: 8rpx;
  padding: 6rpx 12rpx;
  background: #e2dfde;
  color: #636262;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
  border-radius: 6rpx;
}

.visual-footer {
  position: relative;
  height: 320rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background: #e5e5e5;
}

.visual-img {
  width: 100%;
  height: 100%;
  opacity: 0.35;
  filter: grayscale(100%);
}

.visual-fade {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #f9f9f9, transparent 55%);
}

.visual-caption {
  position: absolute;
  left: 32rpx;
  bottom: 32rpx;
  font-size: 48rpx;
  font-weight: 900;
  color: #1a1c1c;
  opacity: 0.1;
  letter-spacing: -0.02em;
}

.bottom-pad {
  height: 32rpx;
}
</style>
