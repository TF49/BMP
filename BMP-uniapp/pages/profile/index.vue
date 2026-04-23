<template>
  <view class="profile-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="hero-shell">
        <view class="hero-glow" />
        <view class="hero-topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
          <view class="brand-wrap">
            <text class="brand-title">KINETIC</text>
            <text class="brand-sub">MY PROFILE</text>
          </view>
          <view class="settings-btn" @click="handleMenuClick('/pages/settings/index')">
            <uni-icons type="gear" size="18" color="#ea580c" />
          </view>
        </view>

        <view class="hero-main">
          <view class="avatar-wrap">
            <view class="avatar-core">
              <uni-icons type="person-filled" size="34" color="#ffffff" />
            </view>
          </view>
          <view class="user-copy">
            <text class="user-name">{{ displayName }}</text>
            <view class="user-sub">
              <text class="role-badge">{{ memberTypeText }}</text>
              <text class="user-id">ID: {{ userInfo.memberId || '—' }}</text>
            </view>
          </view>
        </view>

        <view class="stats-ribbon">
          <view class="stat-card">
            <text class="stat-label">Balance</text>
            <text class="stat-value">¥{{ formatMoney(userInfo.balance) }}</text>
          </view>
          <view class="stat-card">
            <text class="stat-label">Points</text>
            <text class="stat-value">{{ userInfo.points }}</text>
          </view>
          <view class="stat-card">
            <text class="stat-label">Coupons</text>
            <text class="stat-value">{{ userInfo.coupons }}</text>
          </view>
        </view>
      </view>

      <view class="page-body">
        <view class="vip-banner" @click="handleOpenMember">
          <view class="vip-copy">
            <text class="vip-kicker">Membership Access</text>
            <text class="vip-title">{{ vipTitle }}</text>
            <text class="vip-desc">享订场优惠、专属权益和更完整的会员身份服务。</text>
          </view>
          <view class="vip-cta">
            <text>{{ isVipMember ? '查看权益' : '立即开通' }}</text>
            <uni-icons type="right" size="16" color="#1f1f1f" />
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Quick Access</text>
              <text class="section-title">常用入口</text>
            </view>
          </view>

          <view class="grid-menu">
            <view
              v-for="item in quickMenus"
              :key="item.label"
              class="grid-tile"
              @click="handleMenuClick(item.path)"
            >
              <view class="tile-icon" :style="{ background: item.bgColor }">
                <uni-icons :type="item.iconType" size="20" :color="item.iconColor" />
              </view>
              <text class="tile-label">{{ item.label }}</text>
              <text class="tile-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Profile Menu</text>
              <text class="section-title">账户与记录</text>
            </view>
          </view>

          <view
            v-for="item in menuItems"
            :key="item.label"
            class="list-row"
            @click="handleMenuClick(item.path)"
          >
            <view class="list-left">
              <view class="list-icon" :style="{ background: item.bgColor }">
                <uni-icons :type="item.iconType" size="18" :color="item.iconColor" />
              </view>
              <view class="list-copy">
                <text class="list-title">{{ item.label }}</text>
                <text v-if="item.desc" class="list-desc">{{ item.desc }}</text>
              </view>
            </view>
            <view class="list-right">
              <text v-if="item.value" class="list-value">{{ item.value }}</text>
              <uni-icons type="right" size="16" color="#8e7164" />
            </view>
          </view>
        </view>

        <view class="logout-panel">
          <button class="logout-btn" @click="handleLogout">
            <uni-icons type="redo" size="18" color="#1a1c1c" />
            <text>退出登录</text>
          </button>
        </view>
      </view>

      <view class="scroll-buffer" />
    </scroll-view>

    <CustomTabBar :current="4" />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { useUserStore } from '@/store/modules/user'
import { safeReLaunch } from '@/utils/safeRoute'
import { useCurrentMember } from '@/composables/useCurrentMember'

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const userInfo = ref({
  username: '微信用户',
  balance: 0,
  points: 0,
  coupons: 0,
  memberId: 0,
  memberType: 'NORMAL'
})

const quickMenus = [
  {
    label: '会员中心',
    desc: '查看等级、余额和权益',
    iconType: 'vip',
    iconColor: '#ff6600',
    bgColor: 'rgba(255, 102, 0, 0.12)',
    path: '/pages/profile/member'
  },
  {
    label: '个人资料',
    desc: '查看资料展示并维护手机号、性别',
    iconType: 'compose',
    iconColor: '#a33e00',
    bgColor: 'rgba(163, 62, 0, 0.12)',
    path: '/pages/profile/info'
  },
  {
    label: '充值中心',
    desc: '快速补充账户余额',
    iconType: 'wallet',
    iconColor: '#0062a1',
    bgColor: 'rgba(0, 98, 161, 0.12)',
    path: '/pages/recharge/index'
  },
  {
    label: '设置中心',
    desc: '安全、通知与数据管理',
    iconType: 'gear',
    iconColor: '#5f5e5e',
    bgColor: 'rgba(95, 94, 94, 0.12)',
    path: '/pages/settings/index'
  }
] as const

const menuItems = computed(() => [
  {
    label: '预订记录',
    iconType: 'calendar',
    iconColor: '#ff6600',
    bgColor: 'rgba(255, 102, 0, 0.12)',
    value: '',
    desc: '查看你的场地预约与历史订单',
    path: '/pages/booking/list'
  },
  {
    label: '充值中心',
    iconType: 'wallet',
    iconColor: '#0062a1',
    bgColor: 'rgba(0, 98, 161, 0.12)',
    value: `余额 ¥${formatMoney(userInfo.value.balance)}`,
    desc: '账户余额充值与支付管理',
    path: '/pages/recharge/index'
  },
  {
    label: '消费记录',
    iconType: 'bars',
    iconColor: '#a33e00',
    bgColor: 'rgba(163, 62, 0, 0.12)',
    value: '',
    desc: '查看全部余额变动和消费流水',
    path: '/pages/profile/records'
  },
  {
    label: '会员信息',
    iconType: 'vip',
    iconColor: '#ff6600',
    bgColor: 'rgba(255, 102, 0, 0.12)',
    value: memberTypeText.value,
    desc: '查看等级、权益和账户概况',
    path: '/pages/profile/member'
  },
  {
    label: '个人信息',
    iconType: 'person',
    iconColor: '#5f5e5e',
    bgColor: 'rgba(95, 94, 94, 0.12)',
    value: '',
    desc: '查看资料展示与当前可编辑项',
    path: '/pages/profile/info'
  },
  {
    label: '设置中心',
    iconType: 'gear',
    iconColor: '#0062a1',
    bgColor: 'rgba(0, 98, 161, 0.12)',
    value: '',
    desc: '安全、通知、数据管理和帮助设置',
    path: '/pages/settings/index'
  }
])

const displayName = computed(() => userInfo.value.username || '微信用户')
const isVipMember = computed(() => userInfo.value.memberType !== 'NORMAL')
const vipTitle = computed(() => (isVipMember.value ? '尊享会员权益' : '开通尊享会员'))
const memberTypeText = computed(() => {
  const map: Record<string, string> = {
    NORMAL: '普通会员',
    MEMBER: '正式会员',
    VIP: 'VIP会员',
    GOLD: '黄金会员',
    PLATINUM: '白金会员',
    DIAMOND: '钻石会员'
  }
  return map[userInfo.value.memberType] || '普通会员'
})

function formatMoney(value: number) {
  return Number(value || 0).toFixed(2)
}

const loadUserInfo = async () => {
  try {
    const user = userStore.userInfo
    const memberInfo = await fetchCurrentMember(true)

    userInfo.value = {
      username: user?.nickname || user?.username || '微信用户',
      balance: memberInfo.balance || 0,
      points: 0,
      coupons: 0,
      memberId: memberInfo.id,
      memberType: memberInfo.memberType || 'NORMAL'
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const handleOpenMember = () => {
  uni.navigateTo({ url: '/pages/profile/member' })
}

const handleMenuClick = (path: string) => {
  if (!path) {
    uni.showToast({ title: '功能开发中', icon: 'none' })
    return
  }
  uni.navigateTo({ url: path })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#FF7A00',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        safeReLaunch('/pages/login/login', '/pages/login/login')
      }
    }
  })
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  await loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%),
    #f9f9f9;
}

.page-scroll {
  min-height: 100vh;
}

.hero-shell {
  position: relative;
  padding: 0 24rpx 28rpx;
  background: linear-gradient(180deg, rgba(255, 241, 234, 0.7) 0%, rgba(249, 249, 249, 0) 100%);
}

.hero-glow {
  position: absolute;
  right: -80rpx;
  top: 20rpx;
  width: 280rpx;
  height: 280rpx;
  border-radius: 999rpx;
  background: rgba(255, 102, 0, 0.1);
  filter: blur(36rpx);
}

.hero-topbar,
.hero-main,
.stats-ribbon {
  position: relative;
  z-index: 1;
}

.hero-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 18rpx;
}

.brand-wrap {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.brand-title {
  font-size: 30rpx;
  line-height: 1;
  font-weight: 900;
  letter-spacing: 1rpx;
  color: #a33e00;
}

.brand-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.settings-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);
}

.hero-main {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
  gap: 22rpx;
}

.avatar-wrap {
  width: 132rpx;
  height: 132rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 14rpx 30rpx rgba(255, 102, 0, 0.22);
}

.avatar-core {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
}

.user-copy {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-size: 42rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.user-sub {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 12rpx;
}

.role-badge {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
  color: #a33e00;
  background: #fff1e8;
}

.user-id {
  font-size: 22rpx;
  color: #6b625c;
}

.stats-ribbon {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 28rpx;
}

.stat-card {
  min-height: 130rpx;
  padding: 22rpx 18rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14rpx 28rpx rgba(26, 28, 28, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.stat-label {
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.stat-value {
  font-size: 28rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.page-body {
  padding: 0 24rpx 0;
}

.vip-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 8rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #1f1f1f 0%, #2f3131 100%);
  box-shadow: 0 16rpx 36rpx rgba(26, 28, 28, 0.16);
}

.vip-copy {
  flex: 1;
  min-width: 0;
}

.vip-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: rgba(255, 215, 0, 0.72);
  text-transform: uppercase;
}

.vip-title {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 900;
  color: #ffd700;
}

.vip-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: rgba(255, 215, 0, 0.72);
}

.vip-cta {
  min-width: 170rpx;
  padding: 18rpx 22rpx;
  border-radius: 999rpx;
  background: #ffd700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  color: #1f1f1f;
  font-size: 22rpx;
  font-weight: 900;
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

.grid-menu {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.grid-tile {
  min-height: 206rpx;
  padding: 22rpx 18rpx;
  border-radius: 24rpx;
  background: #faf8f6;
  display: flex;
  flex-direction: column;
}

.tile-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tile-label {
  margin-top: 18rpx;
  font-size: 26rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.tile-desc {
  margin-top: 10rpx;
  font-size: 20rpx;
  line-height: 1.6;
  color: #6b625c;
}

.list-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 22rpx 0;
  border-bottom: 2rpx solid #f4efec;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.list-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  flex: 1;
  min-width: 0;
}

.list-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.list-copy {
  flex: 1;
  min-width: 0;
}

.list-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.list-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.list-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.list-value {
  font-size: 22rpx;
  color: #8e7164;
}

.logout-panel {
  margin-top: 28rpx;
  margin-bottom: 160rpx;
}

.logout-btn {
  width: 100%;
  height: 96rpx;
  border: none;
  border-radius: 28rpx;
  background: #e2e2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  box-shadow: 0 10rpx 22rpx rgba(26, 28, 28, 0.04);
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 800;
}

.scroll-buffer {
  height: calc(180rpx + env(safe-area-inset-bottom));
}
</style>
