<template>
  <MobileLayout :showTabBar="false">
    <view class="change-password-container">
      <view class="form-section">
        <view class="section-title">修改密码</view>

        <uni-forms ref="formRef" :model="formData" @submit="handleSubmit">
          <uni-forms-item label="旧密码" name="oldPassword" required>
            <uni-easyinput
              v-model="formData.oldPassword"
              type="password"
              placeholder="请输入旧密码"
              clearable
            />
          </uni-forms-item>

          <uni-forms-item label="新密码" name="newPassword" required>
            <uni-easyinput
              v-model="formData.newPassword"
              type="password"
              placeholder="请输入新密码（至少8位）"
              clearable
            />
            <view class="password-strength">
              <view class="strength-bar">
                <view
                  class="strength-fill"
                  :class="`strength-${passwordStrength}`"
                  :style="{ width: strengthPercentage + '%' }"
                ></view>
              </view>
              <text class="strength-text">{{ strengthText }}</text>
            </view>
          </uni-forms-item>

          <uni-forms-item label="确认密码" name="confirmPassword" required>
            <uni-easyinput
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              clearable
            />
          </uni-forms-item>
        </uni-forms>

        <button class="submit-btn" @click="handleSubmit" :disabled="loading">
          {{ loading ? '修改中...' : '确认修改' }}
        </button>

        <view class="tips">
          <text class="tips-title">密码要求：</text>
          <text class="tips-item">• 至少8个字符</text>
          <text class="tips-item">• 包含大小写字母、数字和特殊符号</text>
          <text class="tips-item">• 不能与旧密码相同</text>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { useUserStore } from '@/store/modules/user'
import { isPresidentRole } from '@/utils/roleCheck'
import { updatePassword } from '@/api/auth'
import { safeReLaunch } from '@/utils/safeRoute'

const formRef = ref()
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

const handleSubmit = async () => {
  // 验证表单
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
    const tip = isPresidentRole(userStore.userRole)
      ? '密码已修改，请使用新密码重新登录会长工作台'
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
.change-password-container {
  padding: 20rpx;

  .form-section {
    background: white;
    border-radius: 12rpx;
    padding: 30rpx;
    margin-top: 20rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      margin-bottom: 30rpx;
      color: #333;
    }

    .password-strength {
      margin-top: 10rpx;

      .strength-bar {
        height: 6rpx;
        background: #f0f0f0;
        border-radius: 3rpx;
        overflow: hidden;
        margin-bottom: 8rpx;

        .strength-fill {
          height: 100%;
          transition: width 0.3s ease;

          &.strength-weak {
            background: #ff6b6b;
          }

          &.strength-medium {
            background: #ffa940;
          }

          &.strength-strong {
            background: #52c41a;
          }
        }
      }

      .strength-text {
        font-size: 24rpx;
        color: #999;
      }
    }

    .submit-btn {
      width: 100%;
      height: 88rpx;
      background: #3cc51f;
      color: white;
      border: none;
      border-radius: 8rpx;
      font-size: 32rpx;
      font-weight: bold;
      margin-top: 40rpx;

      &:disabled {
        opacity: 0.6;
      }
    }

    .tips {
      margin-top: 30rpx;
      padding: 20rpx;
      background: #f5f7fa;
      border-radius: 8rpx;
      display: flex;
      flex-direction: column;

      .tips-title {
        font-size: 26rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 10rpx;
      }

      .tips-item {
        font-size: 24rpx;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}
</style>
