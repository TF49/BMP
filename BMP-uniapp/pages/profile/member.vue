<template>
  <MobileLayout>
    <view class="member-page">
      <view class="topbar">
        <view class="topbar-inner">
          <view class="icon-btn" @click="handleBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">会员信息</text>
            <text class="topbar-sub">MEMBER PROFILE</text>
          </view>
          <view class="icon-btn ghost" @click="handleRecharge">
            <uni-icons type="wallet" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
        <view class="member-hero">
          <view class="member-hero-glow" />
          <view class="member-hero-head">
            <view class="member-avatar">
              <uni-icons type="vip" size="30" color="#ffffff" />
            </view>
            <view class="member-meta">
              <text class="member-name">{{ memberInfo.memberName || '未设置会员名' }}</text>
              <text class="member-type">{{ memberTypeLabel }}</text>
            </view>
            <view class="level-pill">Lv.{{ memberInfo.memberLevel || 1 }}</view>
          </view>

          <view class="balance-block">
            <text class="balance-label">Available Balance</text>
            <view class="balance-row">
              <text class="balance-currency">¥</text>
              <text class="balance-value">{{ formatCurrency(memberInfo.balance || 0) }}</text>
            </view>
          </view>

          <view class="member-stats">
            <view class="stat-card">
              <text class="stat-title">Recharge</text>
              <text class="stat-value">¥{{ formatCurrency(memberInfo.totalRecharge || 0) }}</text>
            </view>
            <view class="stat-card">
              <text class="stat-title">Consumption</text>
              <text class="stat-value">¥{{ formatCurrency(memberInfo.totalConsumption || 0) }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Identity</text>
              <text class="section-title">会员档案</text>
            </view>
          </view>

          <view class="info-grid">
            <view class="info-item">
              <text class="info-label">会员姓名</text>
              <text class="info-value">{{ memberInfo.memberName || '未设置' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">手机号</text>
              <text class="info-value">{{ memberInfo.phone || '未设置' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">会员类型</text>
              <text class="info-value">{{ memberTypeLabel }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">会员等级</text>
              <text class="info-value">Lv.{{ memberInfo.memberLevel || 1 }}</text>
            </view>
            <view class="info-item full">
              <text class="info-label">注册时间</text>
              <text class="info-value">{{ formatDate(memberInfo.createTime) }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Account Overview</text>
              <text class="section-title">当前可查看的数据</text>
            </view>
          </view>

          <view class="benefit-list">
            <view v-for="item in accountSummaryList" :key="item.title" class="benefit-item">
              <view class="benefit-icon" :style="{ background: item.bgColor }">
                <uni-icons :type="item.icon" size="18" :color="item.iconColor" />
              </view>
              <view class="benefit-copy">
                <text class="benefit-title">{{ item.title }}</text>
                <text class="benefit-desc">{{ item.desc }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section-card action-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Actions</text>
              <text class="section-title">常用操作</text>
            </view>
          </view>

          <view class="action-grid">
            <view class="action-tile primary" @click="handleRecharge">
              <view class="action-icon filled">
                <uni-icons type="wallet" size="18" color="#ffffff" />
              </view>
              <text class="action-name">立即充值</text>
              <text class="action-desc">快速补充余额用于预约和消费</text>
            </view>

            <view class="action-tile" @click="goConsumeRecords">
              <view class="action-icon">
                <uni-icons type="bars" size="18" color="#ff6600" />
              </view>
              <text class="action-name">消费记录</text>
              <text class="action-desc">查看余额变动和历史消费明细</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { useCurrentMember } from '@/composables/useCurrentMember'

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const memberInfo = reactive({
  id: 0,
  memberName: '',
  gender: 0,
  phone: '',
  idCard: '',
  memberType: '',
  memberLevel: 1,
  balance: 0,
  status: 1,
  createTime: '',
  totalRecharge: 0,
  totalConsumption: 0
})

const accountSummaryList = [
  {
    title: '会员类型与等级',
    desc: '当前页面展示后端返回的会员类型、等级和注册信息。',
    icon: 'person-filled',
    bgColor: 'rgba(255, 102, 0, 0.12)',
    iconColor: '#ff6600'
  },
  {
    title: '账户余额',
    desc: '可查看当前余额，并前往充值中心补充账户金额。',
    icon: 'wallet',
    bgColor: 'rgba(0, 98, 161, 0.12)',
    iconColor: '#0062a1'
  },
  {
    title: '累计充值与消费',
    desc: '累计充值和累计消费来自当前会员账户的真实记录汇总。',
    icon: 'medal',
    bgColor: 'rgba(163, 62, 0, 0.12)',
    iconColor: '#a33e00'
  }
] as const

const memberTypeLabel = computed(() => {
  const typeMap: Record<string, string> = {
    NORMAL: '普通会员',
    MEMBER: '正式会员',
    VIP: 'VIP会员',
    GOLD: '黄金会员',
    PLATINUM: '白金会员',
    DIAMOND: '钻石会员'
  }
  return typeMap[memberInfo.memberType] || '普通会员'
})

const formatCurrency = (amount: number) => Number(amount || 0).toFixed(2)

const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const loadMemberInfo = async () => {
  try {
    if (!userStore.isLoggedIn) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      uni.redirectTo({ url: '/pages/login/login' })
      return
    }

    const info = await fetchCurrentMember(true)
    Object.assign(memberInfo, {
      ...info,
      totalRecharge: info.totalRecharge || 0,
      totalConsumption: info.totalConsumption || 0
    })
  } catch (error) {
    console.error('加载会员信息失败:', error)
    uni.showToast({ title: '加载会员信息失败', icon: 'none' })
  }
}

const handleRecharge = () => {
  uni.navigateTo({ url: '/pages/recharge/index' })
}

const goConsumeRecords = () => {
  uni.navigateTo({ url: '/pages/profile/records' })
}

const handleBack = () => {
  safeNavigateBack()
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  await loadMemberInfo()
})
</script>

<style lang="scss" scoped>
.member-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%),
    #f9f9f9;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(249, 249, 249, 0.82);
  backdrop-filter: blur(20rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);

  &.ghost {
    background: rgba(255, 241, 234, 0.9);
  }
}

.topbar-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.topbar-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.topbar-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.page-scroll {
  height: calc(100vh - 196rpx);
  padding: 0 24rpx 40rpx;
  box-sizing: border-box;
}

.member-hero {
  position: relative;
  margin-top: 12rpx;
  padding: 34rpx 28rpx;
  border-radius: 36rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 18rpx 40rpx rgba(163, 62, 0, 0.2);
}

.member-hero-glow {
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(36rpx);
}

.member-hero-head,
.balance-block,
.member-stats {
  position: relative;
  z-index: 1;
}

.member-hero-head {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.member-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
}

.member-meta {
  flex: 1;
  min-width: 0;
}

.member-name {
  display: block;
  font-size: 36rpx;
  font-weight: 900;
  color: #ffffff;
}

.member-type {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.86);
}

.level-pill {
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
  white-space: nowrap;
}

.balance-block {
  margin-top: 28rpx;
}

.balance-label {
  display: block;
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.balance-row {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  margin-top: 10rpx;
}

.balance-currency {
  font-size: 24rpx;
  color: #ffffff;
}

.balance-value {
  font-size: 58rpx;
  line-height: 1;
  font-weight: 900;
  color: #ffffff;
}

.member-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 28rpx;
}

.stat-card {
  padding: 18rpx 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.14);
}

.stat-title {
  display: block;
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.stat-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 900;
  color: #ffffff;
}

.section-card {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.section-head {
  margin-bottom: 20rpx;

  &.compact {
    margin-bottom: 14rpx;
  }
}

.section-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 3rpx;
  text-transform: uppercase;
}

.section-title {
  display: block;
  margin-top: 8rpx;
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.info-item {
  padding: 22rpx 20rpx;
  border-radius: 28rpx;
  background: #faf8f6;

  &.full {
    grid-column: 1 / -1;
  }
}

.info-label {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.info-value {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  line-height: 1.5;
  font-weight: 800;
  color: #1a1c1c;
}

.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 20rpx;
  border-radius: 24rpx;
  background: #faf8f6;
}

.benefit-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.benefit-copy {
  flex: 1;
}

.benefit-title {
  display: block;
  font-size: 26rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.benefit-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.action-card {
  margin-bottom: 32rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.action-tile {
  min-height: 208rpx;
  padding: 22rpx 18rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.92);
  border: 2rpx solid rgba(255, 102, 0, 0.08);
  display: flex;
  flex-direction: column;

  &.primary {
    background: linear-gradient(180deg, #fff3eb 0%, #ffffff 100%);
  }

  &:active {
    transform: scale(0.98);
  }
}

.action-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  background: rgba(255, 102, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;

  &.filled {
    background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  }
}

.action-name {
  margin-top: 18rpx;
  font-size: 24rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.action-desc {
  margin-top: 10rpx;
  font-size: 20rpx;
  line-height: 1.6;
  color: #6b625c;
}
</style>
