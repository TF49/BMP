<template>
  <MobileLayout :showTabBar="false">
    <view class="change-phone-container">
      <view class="form-section">
        <view class="section-title">更换手机号</view>

        <view class="current-phone">
          <text class="label">当前手机号</text>
          <text class="phone">{{ currentPhone }}</text>
        </view>

        <uni-forms ref="formRef" :model="formData">
          <uni-forms-item label="新手机号" name="newPhone" required>
            <uni-easyinput
              v-model="formData.newPhone"
              type="tel"
              placeholder="请输入新手机号"
              clearable
            />
          </uni-forms-item>

          <uni-forms-item label="验证码" name="verifyCode" required>
            <view class="verify-code-input">
              <uni-easyinput
                v-model="formData.verifyCode"
                type="text"
                placeholder="请输入验证码"
                clearable
              />
              <button
                class="send-code-btn"
                :disabled="codeLoading || countdown > 0"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </view>
          </uni-forms-item>

          <uni-forms-item label="密码确认" name="password" required>
            <uni-easyinput
              v-model="formData.password"
              type="password"
              placeholder="请输入密码以确认身份"
              clearable
            />
          </uni-forms-item>
        </uni-forms>

        <button class="submit-btn" @click="handleSubmit" :disabled="loading">
          {{ loading ? '更换中...' : '确认更换' }}
        </button>

        <view class="tips">
          <text class="tips-title">注意事项：</text>
          <text class="tips-item">• 更换手机号后需要重新登录</text>
          <text class="tips-item">• 新手机号将用于账号登录和找回密码</text>
          <text class="tips-item">• 请确保新手机号能正常接收短信</text>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { useUserStore } from '@/store/modules/user'
import { sendVerifyCode, updatePhone } from '@/api/auth'
import { safeReLaunch } from '@/utils/safeRoute'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const codeLoading = ref(false)
const countdown = ref(0)

const currentPhone = ref(userStore.userInfo?.phone || '****')

const formData = ref({
  newPhone: '',
  verifyCode: '',
  password: ''
})

// 发送验证码
const handleSendCode = async () => {
  if (!formData.value.newPhone) {
    uni.showToast({ title: '请输入新手机号', icon: 'none' })
    return
  }

  if (!/^1[3-9]\d{9}$/.test(formData.value.newPhone)) {
    uni.showToast({ title: '请输入有效的手机号', icon: 'none' })
    return
  }

  codeLoading.value = true
  try {
    await sendVerifyCode({ phone: formData.value.newPhone })
    uni.showToast({ title: '验证码已发送', icon: 'success' })

    // 倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    uni.showToast({ title: '发送验证码失败', icon: 'none' })
  } finally {
    codeLoading.value = false
  }
}

// 提交更换
const handleSubmit = async () => {
  if (!formData.value.newPhone) {
    uni.showToast({ title: '请输入新手机号', icon: 'none' })
    return
  }

  if (!formData.value.verifyCode) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }

  if (!formData.value.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await updatePhone({
      newPhone: formData.value.newPhone,
      verifyCode: formData.value.verifyCode,
      password: formData.value.password
    })

    uni.showToast({ title: '更换成功，请重新登录', icon: 'success' })
    setTimeout(() => {
      safeReLaunch('/pages/login/login', '/pages/login/login')
    }, 1500)
  } catch (error) {
    uni.showToast({ title: '更换失败，请检查信息是否正确', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.change-phone-container {
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

    .current-phone {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx;
      background: #f5f7fa;
      border-radius: 8rpx;
      margin-bottom: 30rpx;

      .label {
        font-size: 26rpx;
        color: #666;
      }

      .phone {
        font-size: 28rpx;
        color: #333;
        font-weight: bold;
      }
    }

    .verify-code-input {
      display: flex;
      gap: 10rpx;

      :deep(.uni-easyinput) {
        flex: 1;
      }

      .send-code-btn {
        width: 160rpx;
        height: 80rpx;
        background: #3cc51f;
        color: white;
        border: none;
        border-radius: 8rpx;
        font-size: 24rpx;

        &:disabled {
          opacity: 0.6;
        }
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
