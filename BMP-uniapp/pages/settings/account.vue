<template>
  <MobileLayout>
    <view class="account-page">
      <view class="topbar">
        <view class="topbar-inner">
          <view class="icon-btn" @click="handleBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">账户设置</text>
            <text class="topbar-sub">ACCOUNT CENTER</text>
          </view>
          <view class="icon-btn ghost" @click="handleChangePassword">
            <uni-icons type="locked" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
        <view class="hero-card">
          <view class="hero-glow" />
          <image v-if="avatarUrl" class="hero-avatar-image" :src="avatarUrl" mode="aspectFill" />
          <view v-else class="hero-avatar">
            <uni-icons type="person-filled" size="34" color="#ffffff" />
          </view>
          <text class="hero-name">{{ userInfo.nickname || userInfo.username || '未设置昵称' }}</text>
          <text class="hero-copy">这里展示账户摘要，资料编辑统一收口到个人资料页，避免两处状态分叉。</text>
          <view class="hero-tags">
            <text class="hero-tag">{{ userInfo.phone || '未绑定手机号' }}</text>
            <text class="hero-tag">{{ genderText }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Current Profile</text>
              <text class="section-title">当前资料</text>
            </view>
          </view>

          <view class="field-grid">
            <view class="field-card">
              <text class="field-label">用户名</text>
              <text class="field-value">{{ userInfo.username || '未设置' }}</text>
            </view>

            <view class="field-card">
              <text class="field-label">昵称</text>
              <text class="field-value">{{ userInfo.nickname || '未设置' }}</text>
            </view>

            <view class="field-card">
              <text class="field-label">个性签名</text>
              <text class="field-value multiline">{{ userInfo.signature || '未设置' }}</text>
            </view>

            <view class="field-card">
              <text class="field-label">头像</text>
              <text class="field-value">{{ userInfo.avatar ? '已设置' : '未设置' }}</text>
            </view>
          </view>
          <text class="section-note">用户名、昵称、邮箱、生日等字段当前为资料展示项，本轮不提供假编辑入口。</text>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Editable Scope</text>
              <text class="section-title">当前可编辑范围</text>
            </view>
          </view>

          <view class="field-grid">
            <view class="field-card">
              <text class="field-label">手机号</text>
              <text class="field-value">{{ userInfo.phone || '未设置' }}</text>
            </view>

            <view class="field-card">
              <text class="field-label">性别</text>
              <text class="field-value">{{ genderText }}</text>
            </view>

            <view class="field-card">
              <text class="field-label">个性签名</text>
              <text class="field-value multiline">{{ userInfo.signature || '未设置' }}</text>
            </view>

            <view class="field-card">
              <text class="field-label">头像</text>
              <text class="field-value">{{ userInfo.avatar ? '支持更新' : '支持上传' }}</text>
            </view>
          </view>
          <text class="section-note">手机号、性别、头像、个性签名统一在个人资料页保存并同步本地用户状态。</text>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Actions</text>
              <text class="section-title">资料与安全入口</text>
            </view>
          </view>

          <view class="quick-row" @click="goProfileInfo">
            <view class="quick-left">
              <view class="quick-icon" style="background: rgba(255, 102, 0, 0.12);">
                <uni-icons type="compose" size="18" color="#ff6600" />
              </view>
              <view class="quick-copy">
                <text class="quick-title">编辑个人资料</text>
                <text class="quick-desc">统一维护头像、手机号、性别和个性签名</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
          </view>

          <view class="quick-row" @click="handleChangePassword">
            <view class="quick-left">
              <view class="quick-icon" style="background: rgba(163, 62, 0, 0.12);">
                <uni-icons type="locked" size="18" color="#a33e00" />
              </view>
              <view class="quick-copy">
                <text class="quick-title">修改密码</text>
                <text class="quick-desc">定期更新密码能进一步提高账号安全性</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
          </view>

          <view class="quick-row" @click="goSecurityCenter">
            <view class="quick-left">
              <view class="quick-icon" style="background: rgba(95, 94, 94, 0.12);">
                <uni-icons type="auth" size="18" color="#5f5e5e" />
              </view>
              <view class="quick-copy">
                <text class="quick-title">安全中心</text>
                <text class="quick-desc">查看绑定状态、登录记录和高级安全项</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
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
import { getCurrentUser } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'
import { getAvatarImage } from '@/utils/displayImage'

const userInfo = reactive({
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  gender: 0,
  birthday: '',
  signature: ''
})

const userStore = useUserStore()
const genders = ['保密', '男', '女']

const genderText = computed(() => genders[userInfo.gender] || '保密')
const avatarUrl = computed(() => getAvatarImage(userInfo.avatar))

const handleChangePassword = () => {
  uni.navigateTo({ url: '/pages/settings/change-password' })
}

const goProfileInfo = () => {
  uni.navigateTo({ url: '/pages/profile/info' })
}

const goSecurityCenter = () => {
  uni.navigateTo({ url: '/pages/settings/security' })
}

const loadUserInfo = async () => {
  try {
    const user = await getCurrentUser()
    Object.assign(userInfo, {
      username: user.username,
      nickname: user.nickname || user.username,
      avatar: user.avatar || '',
      phone: user.phone || '',
      gender: user.gender || 0,
      birthday: user.birthday || '',
      signature: user.signature || ''
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载用户信息失败',
      icon: 'none'
    })
  }
}

const handleBack = () => {
  safeNavigateBack()
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
.account-page {
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

.hero-card {
  position: relative;
  margin-top: 12rpx;
  padding: 36rpx 28rpx;
  border-radius: 36rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 18rpx 40rpx rgba(163, 62, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.hero-glow {
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(36rpx);
}

.hero-avatar,
.hero-avatar-image,
.hero-name,
.hero-copy,
.hero-tags {
  position: relative;
  z-index: 1;
}

.hero-avatar,
.hero-avatar-image {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
}

.hero-avatar {
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-name {
  margin-top: 18rpx;
  font-size: 38rpx;
  font-weight: 900;
  color: #ffffff;
}

.hero-copy {
  margin-top: 14rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.86);
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12rpx;
  margin-top: 22rpx;
}

.hero-tag {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
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

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.field-card {
  padding: 22rpx 20rpx;
  border-radius: 28rpx;
  background: #faf8f6;
}

.field-label {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.field-value {
  display: block;
  margin-top: 16rpx;
  min-height: 56rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.field-value.multiline {
  min-height: auto;
  line-height: 1.5;
}

.quick-row {
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

.quick-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  flex: 1;
  min-width: 0;
}

.quick-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-copy {
  flex: 1;
  min-width: 0;
}

.quick-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.quick-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.section-note {
  display: block;
  margin-top: 18rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #8e7164;
}
</style>
