<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view v-if="drawerOpen" class="drawer-root" @click="drawerOpen = false">
        <view class="drawer-panel" @click.stop>
          <view class="drawer-head">
            <text class="drawer-brand">KINETIC LOGIC</text>
            <view class="drawer-close" @click="drawerOpen = false">
              <uni-icons type="closeempty" size="26" color="#5f5e5e" />
            </view>
          </view>
          <text class="drawer-section-hint">导航</text>
          <view class="drawer-nav">
            <view class="drawer-item" @click="goDashboard">
              <uni-icons type="home" size="22" color="#5f5e5e" />
              <text class="drawer-item-text">仪表盘</text>
            </view>
            <view class="drawer-item" @click="toastSoon('协会')">
              <uni-icons type="staff" size="22" color="#5f5e5e" />
              <text class="drawer-item-text">协会</text>
            </view>
            <view class="drawer-item active">
              <uni-icons type="flag" size="22" color="#ff6600" />
              <text class="drawer-item-text active">沟通中心</text>
            </view>
          </view>
        </view>
      </view>

      <view class="top-bar">
        <view class="top-bar-left">
          <view class="icon-round" @click="drawerOpen = true">
            <uni-icons type="bars" size="22" color="#ff6600" />
          </view>
          <text class="top-title">管理</text>
        </view>
        <view class="top-bar-right">
          <view class="icon-round">
            <uni-icons type="search" size="22" color="#ff6600" />
          </view>
          <view class="icon-round bell-wrap" @click="toastSoon('通知中心')">
            <uni-icons type="notification" size="22" color="#ff6600" />
            <view class="bell-dot" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-row">
            <view class="hero-copy">
              <text class="kicker">沟通中心</text>
              <text class="hero-title">已发公告</text>
              <text class="hero-desc">管理并追踪向协会、联盟及运动员发送的官方广播。</text>
            </view>
            <button class="btn-publish" @click="goCreate">
              <uni-icons type="plusempty" size="22" color="#561d00" />
              <text class="btn-publish-text">发布新公告</text>
            </button>
          </view>

          <view class="stats-row">
            <view class="stat-card">
              <text class="stat-label">总覆盖人数</text>
              <view class="stat-line">
                <text class="stat-num">42,890</text>
                <text class="stat-trend">+12%</text>
              </view>
            </view>
            <view class="stat-card">
              <text class="stat-label">打开率</text>
              <view class="stat-line">
                <text class="stat-num">68.4%</text>
                <text class="stat-badge">优秀</text>
              </view>
            </view>
            <view class="stat-card">
              <text class="stat-label">活跃主题</text>
              <view class="stat-line">
                <text class="stat-num">14</text>
                <text class="stat-muted">播报中</text>
              </view>
            </view>
          </view>

          <view class="list-head">
            <text class="lh-item w-title">公告标题</text>
            <text class="lh-item w-aud">目标受众</text>
            <text class="lh-item w-date">发送日期</text>
            <text class="lh-item w-cover">覆盖</text>
          </view>

          <view class="notice-list">
            <view
              v-for="n in notices"
              :key="n.id"
              class="notice-card"
              @click="openDetail(n)"
            >
              <view class="nc-main">
                <view class="ico-box" :class="n.iconTone">
                  <uni-icons :type="n.iconType" size="22" :color="n.iconColor" />
                </view>
                <view class="nc-text">
                  <text class="nc-title">{{ n.title }}</text>
                  <text class="nc-snippet">{{ n.snippet }}</text>
                </view>
              </view>
              <view class="nc-tags">
                <view v-for="(t, i) in n.tags" :key="i" class="tag-pill">
                  <text class="tag-text">{{ t }}</text>
                </view>
              </view>
              <view class="nc-date">
                <text class="date-main">{{ n.date }}</text>
                <text class="date-sub">{{ n.time }}</text>
              </view>
              <view class="nc-metrics">
                <view class="views-row">
                  <uni-icons type="staff" size="16" color="#ff6600" />
                  <text class="views-num">{{ n.views }}</text>
                </view>
                <text class="inter-text">{{ n.interaction }}</text>
              </view>
            </view>
          </view>

          <view class="archive-wrap">
            <button class="btn-archive" @click="onArchive">
              <text class="btn-archive-text">查看存档</text>
            </button>
          </view>

          <view class="bottom-space" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

type Notice = {
  id: string
  title: string
  snippet: string
  iconType: string
  iconColor: string
  iconTone: 'tone-orange' | 'tone-blue' | 'tone-gray'
  tags: string[]
  date: string
  time: string
  views: string
  interaction: string
}

const drawerOpen = ref(false)

const notices: Notice[] = [
  {
    id: '1',
    title: '2024 全国锦标赛赛程更新',
    snippet: '所有区域协会的修订后对阵时间表...',
    iconType: 'sound',
    iconColor: '#ff6600',
    iconTone: 'tone-orange',
    tags: ['协会', '联盟'],
    date: '2023年10月24日',
    time: '09:15 AM',
    views: '12,402',
    interaction: '82% 交互'
  },
  {
    id: '2',
    title: '更新运动员行为准则',
    snippet: '所有注册高水平运动员必须查阅。',
    iconType: 'auth',
    iconColor: '#0062a1',
    iconTone: 'tone-blue',
    tags: ['全体成员'],
    date: '2023年10月21日',
    time: '02:30 PM',
    views: '38,910',
    interaction: '94% 交互'
  },
  {
    id: '3',
    title: '第一阶段季度资金拨付',
    snippet: '第四季度竞赛周期补助金分配确认。',
    iconType: 'wallet',
    iconColor: '#ff6600',
    iconTone: 'tone-orange',
    tags: ['精英协会'],
    date: '2023年10月18日',
    time: '11:00 AM',
    views: '452',
    interaction: '100% 交互'
  },
  {
    id: '4',
    title: '服务器维护：系统停机公告',
    snippet: '例行性能升级，定于周日午夜进行。',
    iconType: 'info',
    iconColor: '#5f5e5e',
    iconTone: 'tone-gray',
    tags: ['全体用户'],
    date: '2023年10月15日',
    time: '04:45 PM',
    views: '21,003',
    interaction: '55% 交互'
  }
]

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goDashboard() {
  drawerOpen.value = false
  goBack()
}

function toastSoon(name: string) {
  uni.showToast({ title: `${name} 开发中`, icon: 'none' })
}

function goCreate() {
  uni.navigateTo({ url: PRESIDENT_PAGES.NOTIFICATION_FORM })
}

function openDetail(n: Notice) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.NOTIFICATION_DETAIL}?id=${encodeURIComponent(n.id)}` })
}

function onArchive() {
  uni.showToast({ title: '存档列表开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  color: #1a1c1c;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.drawer-root {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.5);
}
.drawer-panel {
  width: 560rpx;
  max-width: 85vw;
  height: 100%;
  background: #f3f3f3;
  padding: 48rpx 32rpx;
  box-sizing: border-box;
}
.drawer-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40rpx;
}
.drawer-brand {
  font-size: 28rpx;
  font-weight: 900;
  color: #ff6600;
  letter-spacing: -0.03em;
}
.drawer-close {
  padding: 8rpx;
}
.drawer-section-hint {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 16rpx;
}
.drawer-nav {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.drawer-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 20rpx;
  border-radius: 16rpx;
}
.drawer-item.active {
  background: #ffffff;
}
.drawer-item-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #5f5e5e;
}
.drawer-item-text.active {
  color: #ff6600;
}

.top-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx 20rpx;
  background: #f9f9f9;
  position: sticky;
  top: 0;
  z-index: 50;
}
.top-bar-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}
.top-bar-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4rpx;
}
.icon-round {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-round:active {
  background: rgba(0, 0, 0, 0.05);
}
.bell-wrap {
  position: relative;
}
.bell-dot {
  position: absolute;
  top: 14rpx;
  right: 14rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 9999px;
  background: #ff6600;
}
.top-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}

.scroll {
  flex: 1;
  height: 0;
}
.content {
  padding: 16rpx 28rpx 40rpx;
}

.hero-row {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
  margin-bottom: 40rpx;
}
.hero-copy {
  max-width: 100%;
}
.kicker {
  display: block;
  font-size: 24rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 12rpx;
}
.hero-title {
  display: block;
  font-size: 64rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -0.04em;
  line-height: 1.1;
}
.hero-desc {
  display: block;
  margin-top: 20rpx;
  font-size: 26rpx;
  color: #5f5e5e;
  line-height: 1.55;
}
.btn-publish {
  margin: 0;
  align-self: flex-start;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  padding: 28rpx 36rpx;
  border-radius: 16rpx;
  border: none;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.35);
}
.btn-publish::after {
  border: none;
}
.btn-publish-text {
  font-size: 28rpx;
  font-weight: 800;
  color: #561d00;
}

.stats-row {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 40rpx;
}
.stat-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.05);
}
.stat-label {
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.stat-line {
  margin-top: 12rpx;
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 12rpx;
}
.stat-num {
  font-size: 48rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.stat-trend {
  font-size: 22rpx;
  font-weight: 800;
  color: #a33e00;
}
.stat-badge {
  font-size: 22rpx;
  font-weight: 800;
  color: #0062a1;
}
.stat-muted {
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
}

.list-head {
  display: none;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.notice-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.05);
}
.nc-main {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 20rpx;
}
.ico-box {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.tone-orange {
  background: rgba(255, 102, 0, 0.12);
}
.tone-blue {
  background: rgba(0, 156, 252, 0.12);
}
.tone-gray {
  background: rgba(226, 223, 222, 0.65);
}
.nc-text {
  flex: 1;
  min-width: 0;
}
.nc-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.nc-snippet {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #5f5e5e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.nc-tags {
  margin-top: 20rpx;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12rpx;
}
.tag-pill {
  padding: 8rpx 20rpx;
  background: #e2dfde;
  border-radius: 9999px;
}
.tag-text {
  font-size: 18rpx;
  font-weight: 800;
  color: #636262;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.nc-date {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(227, 191, 177, 0.35);
}
.date-main {
  font-size: 26rpx;
  font-weight: 700;
  color: #1a1c1c;
}
.date-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}
.nc-metrics {
  margin-top: 20rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.views-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}
.views-num {
  font-size: 28rpx;
  font-weight: 800;
  color: #ff6600;
}
.inter-text {
  font-size: 20rpx;
  color: #5f5e5e;
}

.archive-wrap {
  margin-top: 48rpx;
  display: flex;
  justify-content: center;
}
.btn-archive {
  margin: 0;
  padding: 24rpx 56rpx;
  background: #e8e8e8;
  border-radius: 16rpx;
  border: none;
}
.btn-archive::after {
  border: none;
}
.btn-archive:active {
  background: #e2e2e2;
}
.btn-archive-text {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.bottom-space {
  height: 32rpx;
}

@media screen and (min-width: 768px) {
  .hero-row {
    flex-direction: row;
    align-items: flex-end;
    justify-content: space-between;
  }
  .stats-row {
    flex-direction: row;
  }
  .stat-card {
    flex: 1;
  }
  .list-head {
    display: grid;
    grid-template-columns: 2fr 1.2fr 1fr 0.9fr;
    gap: 16rpx;
    padding: 20rpx 28rpx 16rpx;
    border-bottom: 1rpx solid rgba(227, 191, 177, 0.25);
    margin-bottom: 8rpx;
  }
  .lh-item {
    font-size: 22rpx;
    font-weight: 800;
    color: #5f5e5e;
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }
  .w-cover {
    text-align: right;
  }
  .notice-card {
    display: grid;
    grid-template-columns: 2fr 1.2fr 1fr 0.9fr;
    gap: 16rpx;
    align-items: center;
    padding: 24rpx 28rpx;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
  }
  .notice-card:active {
    background: #f3f3f3;
  }
  .nc-tags {
    margin-top: 0;
    padding-top: 0;
    border: none;
  }
  .nc-date {
    margin-top: 0;
    padding-top: 0;
    border: none;
  }
  .nc-metrics {
    margin-top: 0;
    flex-direction: column;
    align-items: flex-end;
    gap: 8rpx;
  }
}
</style>
