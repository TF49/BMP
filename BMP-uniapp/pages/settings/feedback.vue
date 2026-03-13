<template>
  <MobileLayout :showTabBar="false">
    <view class="feedback-container">
      <view class="form-section">
        <view class="section-title">用户反馈</view>

        <uni-forms ref="formRef" :model="formData">
          <uni-forms-item label="反馈类型" name="type" required>
            <uni-data-select
              v-model="formData.type"
              :localdata="feedbackTypes"
              @change="handleTypeChange"
            />
          </uni-forms-item>

          <uni-forms-item label="问题描述" name="description" required>
            <textarea
              v-model="formData.description"
              class="feedback-textarea"
              placeholder="请详细描述您遇到的问题或建议（最多500字）"
              maxlength="500"
            />
            <view class="char-count">
              {{ formData.description.length }}/500
            </view>
          </uni-forms-item>

          <uni-forms-item label="联系方式" name="contact" required>
            <uni-easyinput
              v-model="formData.contact"
              type="tel"
              placeholder="请输入手机号或邮箱"
              clearable
            />
          </uni-forms-item>

          <uni-forms-item label="上传截图（可选）" name="images">
            <view class="image-upload">
              <view
                v-for="(img, index) in formData.images"
                :key="index"
                class="image-item"
              >
                <image :src="img" class="image-preview" />
                <view class="image-delete" @click="handleDeleteImage(index)">
                  <uni-icons type="clear" size="14" color="#ffffff"></uni-icons>
                </view>
              </view>

              <view
                v-if="formData.images.length < 3"
                class="image-add"
                @click="handleAddImage"
              >
                <text class="add-icon">+</text>
                <text class="add-text">添加图片</text>
              </view>
            </view>
          </uni-forms-item>
        </uni-forms>

        <button class="submit-btn" @click="handleSubmit" :disabled="loading">
          {{ loading ? '提交中...' : '提交反馈' }}
        </button>

        <view class="tips">
          <text class="tips-title">反馈说明：</text>
          <text class="tips-item">• 我们会认真对待每一条反馈</text>
          <text class="tips-item">• 反馈将在24小时内得到回复</text>
          <text class="tips-item">• 请提供尽可能详细的信息以便我们快速解决</text>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { submitFeedback } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'

const formRef = ref()
const loading = ref(false)

const feedbackTypes = [
  { text: '功能建议', value: 'suggestion' },
  { text: '问题反馈', value: 'bug' },
  { text: '体验优化', value: 'experience' },
  { text: '其他', value: 'other' }
]

const formData = ref({
  type: '',
  description: '',
  contact: '',
  images: [] as string[]
})

const handleTypeChange = (e: any) => {
  formData.value.type = e.detail.value
}

// 添加图片
const handleAddImage = async () => {
  try {
    const res = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })

    if (res.tempFilePaths && res.tempFilePaths.length > 0) {
      formData.value.images.push(res.tempFilePaths[0])
    }
  } catch (error) {
    console.error('选择图片失败:', error)
  }
}

// 删除图片
const handleDeleteImage = (index: number) => {
  formData.value.images.splice(index, 1)
}

// 提交反馈
const handleSubmit = async () => {
  if (!formData.value.type) {
    uni.showToast({ title: '请选择反馈类型', icon: 'none' })
    return
  }

  if (!formData.value.description || formData.value.description.trim().length === 0) {
    uni.showToast({ title: '请输入问题描述', icon: 'none' })
    return
  }

  if (!formData.value.contact) {
    uni.showToast({ title: '请输入联系方式', icon: 'none' })
    return
  }

  // 验证联系方式
  const phoneRegex = /^1[3-9]\d{9}$/
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!phoneRegex.test(formData.value.contact) && !emailRegex.test(formData.value.contact)) {
    uni.showToast({ title: '请输入有效的手机号或邮箱', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await submitFeedback({
      type: formData.value.type,
      description: formData.value.description,
      contact: formData.value.contact,
      images: formData.value.images
    })

    uni.showToast({ title: '反馈提交成功，感谢您的建议！', icon: 'success' })
    setTimeout(() => {
      safeNavigateBack()
    }, 1500)
  } catch (error) {
    uni.showToast({ title: '反馈提交失败，请稍后重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.feedback-container {
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

    .feedback-textarea {
      width: 100%;
      min-height: 200rpx;
      padding: 15rpx;
      border: 1rpx solid #e0e0e0;
      border-radius: 8rpx;
      font-size: 28rpx;
      font-family: inherit;
    }

    .char-count {
      text-align: right;
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }

    .image-upload {
      display: flex;
      flex-wrap: wrap;
      gap: 15rpx;

      .image-item {
        position: relative;
        width: 150rpx;
        height: 150rpx;

        .image-preview {
          width: 100%;
          height: 100%;
          border-radius: 8rpx;
          object-fit: cover;
        }

        .image-delete {
          position: absolute;
          top: -10rpx;
          right: -10rpx;
          width: 40rpx;
          height: 40rpx;
          background: #ff6b6b;
          color: white;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 28rpx;
          font-weight: bold;
        }
      }

      .image-add {
        width: 150rpx;
        height: 150rpx;
        border: 2rpx dashed #3cc51f;
        border-radius: 8rpx;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .add-icon {
          font-size: 60rpx;
          color: #3cc51f;
          line-height: 1;
        }

        .add-text {
          font-size: 24rpx;
          color: #3cc51f;
          margin-top: 10rpx;
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
