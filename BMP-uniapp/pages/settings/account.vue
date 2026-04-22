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
          <view class="hero-avatar">
            <uni-icons type="person-filled" size="34" color="#ffffff" />
          </view>
          <text class="hero-name">{{ userInfo.nickname || userInfo.username || '未设置昵称' }}</text>
          <text class="hero-copy">查看账号资料展示，并维护当前后端已支持的手机号与性别信息。</text>
          <view class="hero-tags">
            <text class="hero-tag">{{ userInfo.phone || '未绑定手机号' }}</text>
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
          </view>
          <text class="section-note">用户名和昵称当前为系统资料展示项，如需调整请联系管理员。</text>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Editable</text>
              <text class="section-title">当前可编辑项</text>
            </view>
          </view>

          <view class="field-grid">
            <view class="field-card">
              <text class="field-label">手机号</text>
              <input
                class="field-input"
                type="number"
                :value="userInfo.phone"
                @input="onPhoneChange"
                placeholder="请输入手机号"
              />
            </view>
            <view class="field-card picker-card">
              <text class="field-label">性别</text>
              <picker mode="selector" :range="genders" :value="userInfo.gender" @change="onGenderChange">
                <view class="picker-value">
                  <text>{{ genderText }}</text>
                  <uni-icons type="right" size="14" color="#8e7164" />
                </view>
              </picker>
            </view>

            <view class="field-card">
              <text class="field-label">生日</text>
              <text class="field-value">{{ userInfo.birthday || '未设置' }}</text>
            </view>
          </view>
          <text class="section-note">生日当前仅展示后端资料，不在本页保存范围内。</text>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Bindings</text>
              <text class="section-title">绑定信息</text>
            </view>
          </view>

          <view class="binding-row">
            <view class="binding-left">
              <view class="binding-icon" style="background: rgba(255, 102, 0, 0.12);">
                <uni-icons type="phone" size="18" color="#ff6600" />
              </view>
              <view class="binding-copy">
                <text class="binding-title">手机号</text>
                <text class="binding-desc">{{ userInfo.phone || '未绑定手机号' }}</text>
              </view>
            </view>
            <text class="binding-action">{{ userInfo.phone ? '已绑定' : '未绑定' }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Security</text>
              <text class="section-title">安全入口</text>
            </view>
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

        <view class="save-panel">
          <button class="save-btn" @click="handleSave">
            <text class="save-btn-top">Save Account</text>
            <text class="save-btn-bottom">保存设置</text>
          </button>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser, updateUserInfo } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'

const userInfo = reactive({
  username: '',
  nickname: '',
  phone: '',
  gender: 0,
  birthday: ''
})

const userStore = useUserStore()
const genders = ['保密', '男', '女']

const genderText = computed(() => genders[userInfo.gender] || '保密')

const onPhoneChange = (e: any) => {
  userInfo.phone = e.detail?.value ?? e.target?.value ?? ''
}

const onGenderChange = (e: any) => {
  userInfo.gender = parseInt(e.detail.value)
}

const handleChangePassword = () => {
  uni.navigateTo({ url: '/pages/settings/change-password' })
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
      phone: user.phone || '',
      gender: user.gender || 0,
      birthday: user.birthday || ''
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载用户信息失败',
      icon: 'none'
    })
  }
}

const handleSave = async () => {
  try {
    uni.showLoading({ title: '保存中...' })

    await updateUserInfo({
      phone: userInfo.phone,
      gender: String(userInfo.gender)
    })

    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })

    if (userStore.userInfo) {
      userStore.userInfo.phone = userInfo.phone
      userStore.userInfo.gender = userInfo.gender
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '保存失败',
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
.hero-name,
.hero-copy,
.hero-tags {
  position: relative;
  z-index: 1;
}

.hero-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
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

.field-input,
.picker-value {
  margin-top: 16rpx;
  min-height: 56rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.field-value {
  display: block;
  margin-top: 16rpx;
  min-height: 56rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.field-input {
  width: 100%;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.binding-row,
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

.binding-left,
.quick-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  flex: 1;
  min-width: 0;
}

.binding-icon,
.quick-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.binding-copy,
.quick-copy {
  flex: 1;
  min-width: 0;
}

.binding-title,
.quick-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.binding-desc,
.quick-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.binding-action {
  font-size: 22rpx;
  color: #a33e00;
  font-weight: 800;
}

.section-note {
  display: block;
  margin-top: 18rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #8e7164;
}

.save-panel {
  margin-top: 28rpx;
  padding-bottom: 32rpx;
}

.save-btn {
  width: 100%;
  border: none;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  box-shadow: 0 14rpx 30rpx rgba(255, 102, 0, 0.22);
}

.save-btn-top,
.save-btn-bottom {
  color: #ffffff;
}

.save-btn-top {
  font-size: 20rpx;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.save-btn-bottom {
  font-size: 30rpx;
  font-weight: 900;
}
</style>
