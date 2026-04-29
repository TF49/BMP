<template>
  <MobileLayout :showTabBar="false">
    <view class="password-page">
      <view class="topbar">
        <view class="topbar-inner">
          <view class="icon-btn" @click="handleBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">修改密码</text>
            <text class="topbar-sub">SECURITY RESET</text>
          </view>
          <view class="icon-btn ghost">
            <uni-icons type="locked" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
        <view class="hero-card">
          <view class="hero-glow" />
          <text class="hero-kicker">KINETIC LOGIC</text>
          <text class="hero-title">更新你的登录密码</text>
          <text class="hero-copy">
            使用旧密码完成身份确认，再设置新的安全密码。修改成功后需要重新登录。
          </text>
          <view class="hero-tags">
            <text class="hero-tag">{{ strengthText }}</text>
            <text class="hero-tag subtle">至少 8 位字符</text>
          </view>
          <view class="hero-progress">
            <view class="progress-track">
              <view
                class="progress-fill"
                :class="`strength-${passwordStrength}`"
                :style="{ width: strengthPercentage + '%' }"
              />
            </view>
            <text class="progress-text">{{ strengthPercentage }}%</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Credentials</text>
              <text class="section-title">身份验证</text>
            </view>
          </view>

          <view class="field-card">
            <text class="field-label">旧密码</text>
            <uni-easyinput
              v-model="formData.oldPassword"
              type="password"
              placeholder="请输入当前登录密码"
              clearable
            />
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">New Password</text>
              <text class="section-title">设置新密码</text>
            </view>
          </view>

          <view class="field-grid">
            <view class="field-card">
              <text class="field-label">新密码</text>
              <uni-easyinput
                v-model="formData.newPassword"
                type="password"
                placeholder="请输入新密码（至少 8 位）"
                clearable
              />
            </view>

            <view class="field-card">
              <text class="field-label">确认密码</text>
              <uni-easyinput
                v-model="formData.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                clearable
              />
            </view>
          </view>
        </view>

        <view class="tips-card">
          <view class="tips-head">
            <text class="tips-kicker">Password Rules</text>
            <text class="tips-title">设置建议</text>
          </view>
          <view class="tips-list">
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>至少 8 个字符，并尽量混合大小写字母与数字</text>
            </view>
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>建议加入特殊符号，提升密码强度</text>
            </view>
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>新密码不要与旧密码重复，也不要与常用密码相同</text>
            </view>
          </view>
        </view>

        <view class="submit-panel">
          <button class="submit-btn" @click="handleSubmit" :disabled="loading">
            <text class="submit-top">{{ loading ? 'Updating Password' : 'Save New Password' }}</text>
            <text class="submit-bottom">{{ loading ? '修改中，请稍候' : '确认修改并重新登录' }}</text>
          </button>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { useUserStore } from '@/store/modules/user'
import { isManagementRole } from '@/utils/roleCheck'
import { updatePassword } from '@/api/auth'
import { safeReLaunch } from '@/utils/safeRoute'
import { safeNavigateBack } from '@/utils/navigation'

const loading = ref(false)
const formData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算密码强度
const passwordStrength = computed(() => {
  const pwd = formData.value.newPassword
  if (!pwd) return 'weak'

  let strength = 0
  if (pwd.length >= 8) strength++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++
  if (/\d/.test(pwd)) strength++
  if (/[!@#$%^&*]/.test(pwd)) strength++

  if (strength <= 1) return 'weak'
  if (strength <= 2) return 'medium'
  return 'strong'
})

const strengthPercentage = computed(() => {
  const strengthMap = { weak: 33, medium: 66, strong: 100 }
  return strengthMap[passwordStrength.value as keyof typeof strengthMap]
})

const strengthText = computed(() => {
  const textMap = { weak: '弱', medium: '中', strong: '强' }
  return `密码强度：${textMap[passwordStrength.value as keyof typeof textMap]}`
})

const handleBack = () => {
  safeNavigateBack()
}

const handleSubmit = async () => {
  if (!formData.value.oldPassword) {
    uni.showToast({ title: '请输入旧密码', icon: 'none' })
    return
  }

  if (!formData.value.newPassword || formData.value.newPassword.length < 8) {
    uni.showToast({ title: '新密码至少8个字符', icon: 'none' })
    return
  }

  if (formData.value.newPassword !== formData.value.confirmPassword) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
    return
  }

  if (formData.value.oldPassword === formData.value.newPassword) {
    uni.showToast({ title: '新密码不能与旧密码相同', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await updatePassword({
      oldPassword: formData.value.oldPassword,
      newPassword: formData.value.newPassword
    })

    const userStore = useUserStore()
    const tip = isManagementRole(userStore.userRole)
      ? '密码已修改，请使用新密码重新登录移动管理端'
      : '修改成功，请使用新密码重新登录'
    uni.showToast({ title: tip, icon: 'success', duration: 2500 })
    setTimeout(() => {
      safeReLaunch('/pages/login/login', '/pages/login/login')
    }, 1500)
  } catch (error) {
    uni.showToast({ title: '修改失败，请检查旧密码是否正确', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.password-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(255, 138, 76, 0.24), transparent 34%),
    linear-gradient(180deg, #fff7ed 0%, #fffaf5 24%, #f6f7fb 100%);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 24rpx 24rpx 0;
  background: linear-gradient(180deg, rgba(255, 247, 237, 0.96) 0%, rgba(255, 247, 237, 0.72) 100%);
  backdrop-filter: blur(18rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 14rpx 34rpx rgba(15, 23, 42, 0.08);
}

.icon-btn.ghost {
  background: rgba(255, 237, 213, 0.95);
}

.topbar-copy {
  flex: 1;
  padding: 0 20rpx;
}

.topbar-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2937;
}

.topbar-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  letter-spacing: 4rpx;
  color: #c2410c;
}

.page-scroll {
  height: calc(100vh - 96rpx);
}

.hero-card,
.section-card,
.tips-card,
.submit-panel {
  margin: 24rpx;
}

.hero-card {
  position: relative;
  overflow: hidden;
  padding: 38rpx 34rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #1f2937 0%, #7c2d12 58%, #ea580c 100%);
  box-shadow: 0 28rpx 60rpx rgba(124, 45, 18, 0.24);
}

.hero-glow {
  position: absolute;
  top: -120rpx;
  right: -40rpx;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
}

.hero-kicker,
.hero-title,
.hero-copy,
.hero-tag,
.progress-text {
  position: relative;
  z-index: 1;
}

.hero-kicker {
  display: inline-flex;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  font-size: 20rpx;
  letter-spacing: 3rpx;
  color: rgba(255, 255, 255, 0.9);
}

.hero-title {
  display: block;
  margin-top: 22rpx;
  font-size: 44rpx;
  font-weight: 700;
  line-height: 1.2;
  color: #ffffff;
}

.hero-copy {
  display: block;
  margin-top: 18rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.78);
}

.hero-tags {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 28rpx;
}

.hero-tag {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  font-size: 22rpx;
  color: #ffffff;
}

.hero-tag.subtle {
  color: rgba(255, 255, 255, 0.74);
}

.hero-progress {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 28rpx;
}

.progress-track {
  flex: 1;
  height: 12rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  transition: width 0.3s ease;
}

.progress-fill.strength-weak {
  background: #fda4af;
}

.progress-fill.strength-medium {
  background: #fdba74;
}

.progress-fill.strength-strong {
  background: #86efac;
}

.progress-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
}

.section-card,
.tips-card,
.submit-panel {
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 22rpx 48rpx rgba(15, 23, 42, 0.08);
}

.section-card {
  padding: 30rpx;
}

.section-head {
  margin-bottom: 24rpx;
}

.section-head.compact {
  margin-bottom: 20rpx;
}

.section-kicker,
.tips-kicker {
  display: block;
  font-size: 20rpx;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  color: #c2410c;
}

.section-title,
.tips-title {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2937;
}

.field-grid {
  display: grid;
  gap: 20rpx;
}

.field-card {
  padding: 22rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #ffffff 100%);
  border: 1rpx solid rgba(255, 102, 0, 0.08);
}

.field-label {
  display: block;
  margin-bottom: 16rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #7c2d12;
}

:deep(.uni-easyinput) {
  .uni-easyinput__content {
    min-height: 92rpx;
    border-radius: 20rpx;
    border: 1rpx solid #fed7aa !important;
    background: #ffffff !important;
    box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.6);
  }

  .uni-easyinput__content-input {
    height: 92rpx;
    font-size: 28rpx;
    color: #1f2937;
  }

  .uni-easyinput__placeholder-class {
    color: #94a3b8;
  }
}

.tips-card {
  padding: 30rpx;
}

.tips-head {
  margin-bottom: 22rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
  font-size: 25rpx;
  line-height: 1.7;
  color: #475569;
}

.submit-panel {
  padding: 18rpx;
  margin-bottom: 36rpx;
}

.submit-btn {
  width: 100%;
  min-height: 104rpx;
  border: none;
  border-radius: 26rpx;
  background: linear-gradient(135deg, #ff8a4c 0%, #ff6600 55%, #a33e00 100%);
  box-shadow: 0 22rpx 36rpx rgba(255, 102, 0, 0.24);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
}

.submit-btn::after {
  border: none;
}

.submit-btn:disabled {
  opacity: 0.7;
}

.submit-top,
.submit-bottom {
  display: block;
  color: #ffffff;
}

.submit-top {
  font-size: 24rpx;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.submit-bottom {
  font-size: 30rpx;
  font-weight: 700;
}

@media screen and (max-width: 375px) {
  .hero-title {
    font-size: 40rpx;
  }

  .topbar-copy {
    padding: 0 14rpx;
  }
}
</style>
