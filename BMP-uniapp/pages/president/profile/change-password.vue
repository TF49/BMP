<template>
  <PresidentLayout :showTabBar="false">
    <view class="change-password-page">
      <view class="status-bar-placeholder" />
      <view class="pwd-nav" @click="onBack">
        <uni-icons type="arrow-left" size="24" color="#ff6600" />
        <text class="pwd-nav-title">修改密码</text>
      </view>
      <view class="form-card glass-card">
        <view class="form-item">
          <text class="form-label">旧密码</text>
          <input
            v-model="form.oldPassword"
            type="password"
            class="form-input"
            placeholder="请输入旧密码"
          />
        </view>
        <view class="form-item">
          <text class="form-label">新密码（至少8位）</text>
          <input
            v-model="form.newPassword"
            type="password"
            class="form-input"
            placeholder="请输入新密码"
          />
        </view>
        <view class="form-item">
          <text class="form-label">确认新密码</text>
          <input
            v-model="form.confirmPassword"
            type="password"
            class="form-input"
            placeholder="请再次输入新密码"
          />
        </view>
        <view class="btn-row">
          <view class="btn-submit" @click="handleSubmit" :class="{ loading }">
            {{ loading ? '修改中...' : '确认修改' }}
          </view>
        </view>
        <view class="tips">
          <text class="tips-title">密码要求：</text>
          <text class="tips-item">• 至少8个字符</text>
          <text class="tips-item">• 建议包含大小写字母、数字或特殊符号</text>
          <text class="tips-item">• 不能与旧密码相同</text>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { updatePassword } from '@/api/auth'
import { safeReLaunch } from '@/utils/safeRoute'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.PROFILE)
}

const loading = ref(false)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

async function handleSubmit() {
  if (!form.value.oldPassword) {
    uni.showToast({ title: '请输入旧密码', icon: 'none' })
    return
  }
  if (!form.value.newPassword || form.value.newPassword.length < 8) {
    uni.showToast({ title: '新密码至少8个字符', icon: 'none' })
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
    return
  }
  if (form.value.oldPassword === form.value.newPassword) {
    uni.showToast({ title: '新密码不能与旧密码相同', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await updatePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    })
    uni.showToast({
      title: '密码已修改，请使用新密码重新登录会长工作台',
      icon: 'success',
      duration: 2500
    })
    setTimeout(() => {
      safeReLaunch('/pages/login/login', '/pages/login/login')
    }, 1500)
  } catch {
    uni.showToast({ title: '修改失败，请检查旧密码是否正确', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.change-password-page {
  padding: 24rpx;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
}
.pwd-nav {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.pwd-nav-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #1e293b;
}
.form-card {
  padding: 32rpx;
  border-radius: 24rpx;
}
.form-item {
  margin-bottom: 32rpx;
}
.form-label {
  display: block;
  font-size: 28rpx;
  color: var(--color-text-secondary, #475569);
  margin-bottom: 12rpx;
}
.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.btn-row {
  margin-top: 40rpx;
}
.btn-submit {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: linear-gradient(135deg, #a33e00, #ff6600);
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 12rpx 28rpx rgba(163, 62, 0, 0.22);
}
.btn-submit.loading {
  opacity: 0.8;
}
.tips {
  margin-top: 32rpx;
  padding: 24rpx;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 12rpx;
}
.tips-title {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: var(--color-text, #1E293B);
  margin-bottom: 12rpx;
}
.tips-item {
  display: block;
  font-size: 24rpx;
  color: var(--color-text-secondary, #475569);
  line-height: 1.6;
}
</style>
