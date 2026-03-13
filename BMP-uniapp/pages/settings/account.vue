<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">账户设置</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Profile Section -->
      <view class="section profile-section">
        <view class="profile-header">
          <view class="avatar-container">
            <view class="avatar">
              <uni-icons type="person" size="28" color="#94a3b8" class="avatar-icon"></uni-icons>
            </view>
            <text class="change-avatar">点击更换头像</text>
          </view>
        </view>
      </view>

      <!-- Account Info -->
      <view class="section account-info">
        <view class="info-item">
          <text class="info-label">用户名</text>
          <input 
            class="info-input" 
            type="text" 
            :value="userInfo.username" 
            @input="onUsernameChange"
            placeholder="请输入用户名"
          />
        </view>
        <view class="info-item">
          <text class="info-label">昵称</text>
          <input 
            class="info-input" 
            type="text" 
            :value="userInfo.nickname" 
            @input="onNicknameChange"
            placeholder="请输入昵称"
          />
        </view>
        <view class="info-item">
          <text class="info-label">手机号</text>
          <text class="info-value">{{ userInfo.phone || '未绑定' }}</text>
          <text class="edit-btn" @click="handleEditPhone">更改</text>
        </view>
        <view class="info-item">
          <text class="info-label">邮箱</text>
          <text class="info-value">{{ userInfo.email || '未绑定' }}</text>
          <text class="edit-btn" @click="handleEditEmail">更改</text>
        </view>
        <view class="info-item">
          <text class="info-label">性别</text>
          <picker mode="selector" :range="genders" @change="onGenderChange">
            <view class="picker">
              <text class="picker-text">{{ genderText }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
        <view class="info-item">
          <text class="info-label">生日</text>
          <picker mode="date" :value="userInfo.birthday" @change="onBirthdayChange">
            <view class="picker">
              <text class="picker-text">{{ userInfo.birthday || '未设置' }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- Security Settings -->
      <view class="section security-settings">
        <view class="setting-item" @click="handleChangePassword">
          <view class="setting-left">
            <view class="setting-icon">
              <uni-icons type="locked" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="setting-info">
              <text class="setting-title">修改密码</text>
              <text class="setting-desc">定期更换密码更安全</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="setting-item" @click="handleChangePhone">
          <view class="setting-left">
            <view class="setting-icon">
              <uni-icons type="phone" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="setting-info">
              <text class="setting-title">更换手机号</text>
              <text class="setting-desc">当前绑定: {{ userInfo.phone || '未绑定' }}</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="setting-item" @click="handleChangeEmail">
          <view class="setting-left">
            <view class="setting-icon">
              <uni-icons type="email" size="20" color="#3cc51f"></uni-icons>
            </view>
            <view class="setting-info">
              <text class="setting-title">更换邮箱</text>
              <text class="setting-desc">当前绑定: {{ userInfo.email || '未绑定' }}</text>
            </view>
          </view>
          <text class="chevron">›</text>
        </view>
      </view>
    </scroll-view>

    <!-- Save Button -->
    <view class="save-container">
      <button class="save-btn" @click="handleSave">
        保存设置
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser, updateUserInfo } from '@/api/auth'

// 用户信息
const userInfo = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  birthday: ''
})

// 响应式数据
const userStore = useUserStore()
const genders = ['保密', '男', '女']

// 性别文本
const genderText = computed(() => {
  return genders[userInfo.gender] || '保密'
})

// 更新用户名
const onUsernameChange = (e: any) => {
  userInfo.username = e.target.value
}

// 更新昵称
const onNicknameChange = (e: any) => {
  userInfo.nickname = e.target.value
}

// 更新性别
const onGenderChange = (e: any) => {
  userInfo.gender = parseInt(e.detail.value)
}

// 更新生日
const onBirthdayChange = (e: any) => {
  userInfo.birthday = e.detail.value
}

// 更改手机号
const handleEditPhone = () => {
  uni.navigateTo({
    url: '/pages/settings/change-phone'
  })
}

// 更改邮箱
const handleEditEmail = () => {
  uni.navigateTo({
    url: '/pages/settings/change-email'
  })
}

// 修改密码
const handleChangePassword = () => {
  uni.navigateTo({
    url: '/pages/settings/change-password'
  })
}

// 更换手机号
const handleChangePhone = () => {
  handleEditPhone()
}

// 更换邮箱
const handleChangeEmail = () => {
  handleEditEmail()
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const user = await getCurrentUser()
    Object.assign(userInfo, {
      username: user.username,
      nickname: user.nickname || user.username,
      phone: user.phone || '',
      email: user.email || '',
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

// 保存设置
const handleSave = async () => {
  try {
    uni.showLoading({
      title: '保存中...'
    })

    await updateUserInfo({
      id: userStore.userInfo?.id,
      username: userInfo.username,
      nickname: userInfo.nickname,
      phone: userInfo.phone,
      email: userInfo.email,
      gender: userInfo.gender,
      birthday: userInfo.birthday
    })

    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })

    // 更新store中的用户信息
    if (userStore.userInfo) {
      userStore.userInfo.username = userInfo.username
      userStore.userInfo.nickname = userInfo.nickname
      userStore.userInfo.phone = userInfo.phone
      userStore.userInfo.email = userInfo.email
      userStore.userInfo.gender = userInfo.gender
      userStore.userInfo.birthday = userInfo.birthday
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

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载时获取数据
onMounted(async () => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    // 未登录用户重定向到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  await loadUserInfo()
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.header {
  background-color: #ffffff;
  padding: 20rpx 28rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  border-bottom: 1rpx solid #e6e6e6;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-icon {
  font-size: 40rpx;
  color: #333333;
  font-weight: bold;
  width: 56rpx;
}

.header-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
  text-align: center;
}

.header-placeholder {
  width: 56rpx;
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.section {
  background-color: #ffffff;
  margin-bottom: 20rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.profile-section {
  padding: 60rpx 0;
  text-align: center;
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  color: #ffffff;
  margin: 24rpx;
  border-radius: 24rpx;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  margin-bottom: 20rpx;
}

.avatar-icon {
  font-size: 64rpx;
  color: rgba(255, 255, 255, 0.8);
}

.change-avatar {
  font-size: 24rpx;
  opacity: 0.8;
}

.account-info {
  margin: 0 24rpx 20rpx;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 24rpx;
  color: #333333;
  width: 120rpx;
}

.info-input {
  flex: 1;
  font-size: 24rpx;
  color: #333333;
  text-align: right;
}

.info-value {
  flex: 1;
  font-size: 24rpx;
  color: #666666;
  text-align: right;
}

.edit-btn {
  font-size: 24rpx;
  color: #3cc51f;
  margin-left: 16rpx;
}

.picker {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 24rpx;
  color: #333333;
}

.picker-text {
  flex: 1;
  text-align: right;
  color: #666666;
}

.chevron {
  font-size: 26rpx;
  color: #999999;
  margin-left: 12rpx;
}

.security-settings {
  margin: 0 24rpx 20rpx;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f9fafb;
  }
}

.setting-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.setting-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3cc51f;
}

.icon-text {
  font-size: 28rpx;
}

.setting-info {
  display: flex;
  flex-direction: column;
}

.setting-title {
  font-size: 24rpx;
  font-weight: 500;
  color: #333333;
}

.setting-desc {
  font-size: 20rpx;
  color: #999999;
  margin-top: 4rpx;
}

.divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0 28rpx;
}

.save-container {
  padding: 28rpx;
}

.save-btn {
  width: 100%;
  height: 80rpx;
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 12rpx;
  border: none;
  box-shadow: 0 2rpx 6rpx rgba(60, 197, 31, 0.2);
}
</style>