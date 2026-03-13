<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">个人信息</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Avatar Section -->
      <view class="section avatar-section">
        <view class="avatar-container">
          <view class="avatar">
            <uni-icons type="person" size="28" color="#94a3b8" class="avatar-icon"></uni-icons>
          </view>
          <text class="upload-text">点击上传头像</text>
        </view>
      </view>

      <!-- Info Form -->
      <view class="section info-section">
        <view class="form-item">
          <text class="label">用户名</text>
          <input 
            class="input" 
            type="text" 
            :value="userInfo.username" 
            placeholder="请输入用户名"
            @input="onUsernameChange"
          />
        </view>
        
        <view class="form-item">
          <text class="label">手机号</text>
          <input 
            class="input" 
            type="number" 
            :value="userInfo.phone" 
            placeholder="请输入手机号"
            @input="onPhoneChange"
          />
        </view>
        
        <view class="form-item">
          <text class="label">性别</text>
          <picker mode="selector" :range="genders" @change="onGenderChange">
            <view class="picker">
              <text class="picker-text">{{ genderText }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">生日</text>
          <picker mode="date" :value="userInfo.birthday" @change="onBirthdayChange">
            <view class="picker">
              <text class="picker-text">{{ userInfo.birthday || '请选择生日' }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">邮箱</text>
          <input 
            class="input" 
            type="text" 
            :value="userInfo.email" 
            placeholder="请输入邮箱"
            @input="onEmailChange"
          />
        </view>
      </view>

      <!-- Security Section -->
      <view class="section security-section">
        <view class="security-item" @click="handleChangePassword">
          <text class="security-label">修改密码</text>
          <text class="chevron">›</text>
        </view>
        
        <view class="security-divider"></view>
        
        <view class="security-item" @click="handleUpdateProfile">
          <text class="security-label">更新资料</text>
          <text class="chevron">›</text>
        </view>
      </view>
    </scroll-view>

    <!-- Save Button -->
    <view class="save-container">
      <button class="save-btn" @click="handleSave">
        保存信息
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser, updateUserInfo } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'

// 用户信息
const userInfo = reactive({
  username: '',
  phone: '',
  gender: 0,
  birthday: '',
  email: ''
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

// 更新手机号
const onPhoneChange = (e: any) => {
  userInfo.phone = e.target.value
}

// 更新性别
const onGenderChange = (e: any) => {
  userInfo.gender = parseInt(e.detail.value)
}

// 更新生日
const onBirthdayChange = (e: any) => {
  userInfo.birthday = e.detail.value
}

// 更新邮箱
const onEmailChange = (e: any) => {
  userInfo.email = e.target.value
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const user = await getCurrentUser()
    Object.assign(userInfo, {
      username: user.username,
      phone: user.phone || '',
      gender: user.gender || 0,
      birthday: user.birthday || '',
      email: user.email || ''
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载用户信息失败',
      icon: 'none'
    })
  }
}

// 保存信息
const handleSave = async () => {
  try {
    uni.showLoading({
      title: '保存中...'
    })

    // 调用API更新用户信息
    await updateUserInfo({
      id: userStore.userInfo?.id,
      username: userInfo.username,
      phone: userInfo.phone,
      gender: userInfo.gender,
      birthday: userInfo.birthday,
      email: userInfo.email
    })

    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })

    // 更新store中的用户信息
    if (userStore.userInfo) {
      userStore.userInfo.username = userInfo.username
    }
  } catch (error) {
    console.error('保存用户信息失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

// 修改密码
const handleChangePassword = () => {
  uni.navigateTo({
    url: '/pages/profile/change-password'
  })
}

// 更新资料
const handleUpdateProfile = () => {
  handleSave()
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
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  background-color: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
  border: 4rpx solid #e6e6e6;
}

.avatar-icon {
  font-size: 64rpx;
  color: #999999;
}

.upload-text {
  font-size: 24rpx;
  color: #999999;
}

.info-section {
  padding: 0 28rpx;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.label {
  font-size: 24rpx;
  color: #333333;
  width: 120rpx;
}

.input {
  flex: 1;
  font-size: 24rpx;
  color: #333333;
  text-align: right;
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

.security-section {
  padding: 0 28rpx;
}

.security-item {
  padding: 24rpx 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.security-label {
  font-size: 24rpx;
  color: #333333;
}

.security-divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0 -28rpx;
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

.chevron {
  font-size: 26rpx;
  color: #999999;
  margin-left: 12rpx;
}
</style>