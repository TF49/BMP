<template>
  <view class="coach-profile-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="brandAvatar"
          brand="KINETIC LOGIC"
          action-icon="notification-filled"
          @action="handleNotice"
        />

        <view class="hero-card">
          <view class="hero-avatar-wrap">
            <image class="hero-avatar" :src="profile.avatar" mode="aspectFill" />
            <view class="verified-badge">
              <uni-icons type="checkbox-filled" size="16" color="#ffffff" />
            </view>
          </view>

          <text class="hero-name">{{ profile.name }}</text>
          <text class="hero-role">{{ profile.role }}</text>

          <view class="rating-row">
            <uni-icons type="star-filled" size="16" color="#ff6600" />
            <text class="rating-value">{{ profile.rating }}</text>
            <text class="rating-count">({{ profile.reviewCount }} 评价)</text>
          </view>

          <view class="badge-row">
            <view v-for="badge in profile.badges" :key="badge" class="hero-badge">
              <text>{{ badge }}</text>
            </view>
          </view>

          <button class="edit-btn" @tap="handleEditProfile">
            <text>EDIT PROFILE</text>
          </button>
        </view>

        <view class="stats-card">
          <view class="stat-block">
            <text class="stat-label">TOTAL STUDENTS</text>
            <text class="stat-value">{{ profile.totalStudents }}</text>
          </view>

          <view class="stat-divider" />

          <view class="stat-block">
            <text class="stat-label">HOURS COACHED</text>
            <text class="stat-value stat-value-primary">{{ profile.hoursCoached }}</text>
          </view>
        </view>

        <view class="content-card">
          <view class="section-head">
            <uni-icons type="compose" size="18" color="#ff6600" />
            <text class="section-title">Professional Bio</text>
          </view>
          <text class="section-copy">{{ profile.bio }}</text>
        </view>

        <view class="content-card">
          <view class="section-head">
            <uni-icons type="lightning" size="18" color="#ff6600" />
            <text class="section-title">Specialties</text>
          </view>

          <view class="specialties-wrap">
            <view
              v-for="specialty in profile.specialties"
              :key="specialty"
              class="specialty-chip"
            >
              <text>{{ specialty }}</text>
            </view>
          </view>
        </view>

        <view class="menu-card">
          <view
            v-for="(item, index) in menuItems"
            :key="item.label"
            class="menu-item"
            :class="{ danger: item.danger }"
            @tap="handleMenu(item.key)"
          >
            <view class="menu-left">
              <view class="menu-icon-wrap">
                <uni-icons :type="item.icon" size="20" :color="item.danger ? '#ba1a1a' : '#5f5e5e'" />
              </view>
              <text class="menu-label">{{ item.label }}</text>
            </view>

            <uni-icons
              v-if="!item.danger"
              type="right"
              size="18"
              color="#5f5e5e"
            />
          </view>
        </view>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="profile" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'

type MenuKey = 'security' | 'notification' | 'help' | 'logout'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const brandAvatar = '/static/placeholders/avatar.svg'

const profile = ref({
  avatar: '/static/placeholders/avatar.svg',
  name: 'Li Wei',
  role: 'Head Coach',
  rating: '4.9',
  reviewCount: 128,
  badges: ['BWF CERTIFIED', '10+ YEARS EXP'],
  totalStudents: '450+',
  hoursCoached: '2.4k',
  bio: '前国家队队员，拥有超过10年的专业羽毛球教学经验。专注于提升学员的实战能力和技术细节。致力于为每位学员量身定制训练计划，帮助他们突破技术瓶颈，享受羽毛球的乐趣。',
  specialties: [
    '单打战术 (Singles Tactics)',
    '步伐训练 (Footwork)',
    '杀球爆发力 (Smash Power)',
    '体能拉练 (Stamina)'
  ]
})

const menuItems = [
  { key: 'security' as MenuKey, label: 'Account Security', icon: 'locked', danger: false },
  { key: 'notification' as MenuKey, label: 'Notification Settings', icon: 'notification', danger: false },
  { key: 'help' as MenuKey, label: 'Help Center', icon: 'help', danger: false },
  { key: 'logout' as MenuKey, label: 'Log Out', icon: 'redo', danger: true }
]

function handleNotice() {
  uni.showToast({
    title: '通知入口待接入',
    icon: 'none'
  })
}

function handleEditProfile() {
  uni.showToast({
    title: '编辑资料待接入',
    icon: 'none'
  })
}

function handleMenu(key: MenuKey) {
  const titleMap: Record<MenuKey, string> = {
    security: '账号安全待接入',
    notification: '通知设置待接入',
    help: '帮助中心待接入',
    logout: '退出登录待接入'
  }

  uni.showToast({
    title: titleMap[key],
    icon: 'none'
  })
}

</script>

<style lang="scss" scoped>
.coach-profile-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.page-scroll {
  min-height: 100vh;
}

.page-shell {
  padding: 0 14rpx;
}

.hero-card {
  margin-top: 42rpx;
  border-radius: 30rpx;
  background: linear-gradient(180deg, rgba(255, 219, 205, 0.28) 0%, #ffffff 100%);
  padding: 42rpx 28rpx 28rpx;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.hero-avatar-wrap {
  position: relative;
  width: 208rpx;
  height: 208rpx;
}

.hero-avatar {
  width: 100%;
  height: 100%;
  border-radius: 999rpx;
  border: 6rpx solid #ffffff;
  box-shadow: 0 12rpx 28rpx rgba(26, 28, 28, 0.08);
}

.verified-badge {
  position: absolute;
  right: 0;
  bottom: 6rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 999rpx;
  background: #ff6600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 18rpx rgba(255, 102, 0, 0.26);
}

.hero-name {
  margin-top: 28rpx;
  font-size: 64rpx;
  line-height: 1.05;
  font-weight: 900;
  letter-spacing: -2rpx;
}

.hero-role {
  margin-top: 14rpx;
  font-size: 20px;
  color: #5f5e5e;
}

.rating-row {
  margin-top: 18rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.rating-value {
  color: #ff6600;
  font-size: 18px;
  font-weight: 900;
}

.rating-count {
  color: #5f5e5e;
  font-size: 16px;
}

.badge-row {
  margin-top: 22rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12rpx;
}

.hero-badge {
  min-height: 42rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #e2dfde;
  color: #474746;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  letter-spacing: 2rpx;
}

.edit-btn {
  width: 100%;
  height: 74rpx;
  margin-top: 28rpx;
  border: none;
  border-radius: 10rpx;
  background: #ff6600;
  color: #561d00;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 2rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.22);
}

.stats-card,
.content-card,
.menu-card {
  margin-top: 22rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.05);
}

.stats-card {
  padding: 28rpx 30rpx;
}

.stat-block {
  min-width: 0;
}

.stat-label {
  display: block;
  font-size: 16px;
  color: #5f5e5e;
  letter-spacing: 2rpx;
}

.stat-value {
  display: block;
  margin-top: 20rpx;
  font-size: 78rpx;
  line-height: 1;
  font-weight: 900;
}

.stat-value-primary {
  color: #ff6600;
}

.stat-divider {
  height: 2rpx;
  margin: 28rpx 0;
  background: #f3f3f3;
}

.content-card {
  padding: 28rpx 28rpx 30rpx;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.section-title {
  font-size: 21px;
  font-weight: 900;
}

.section-copy {
  display: block;
  margin-top: 24rpx;
  font-size: 18px;
  line-height: 1.8;
  color: #474746;
}

.specialties-wrap {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 14rpx;
}

.specialty-chip {
  padding: 14rpx 18rpx;
  border-radius: 14rpx;
  background: #f3f3f3;
  color: #1a1c1c;
  font-size: 17px;
}

.menu-card {
  overflow: hidden;
}

.menu-item {
  min-height: 106rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2rpx solid #f3f3f3;

  &:last-child {
    border-bottom: none;
  }

  &.danger {
    color: #ba1a1a;
  }
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.menu-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 19px;
  font-weight: 500;
}

.bottom-space {
  height: 170rpx;
}

</style>
