<template>
  <MobileLayout className="help-shell">
    <view class="help-page">
      <scroll-view
        scroll-y
        class="page-scroll"
        :show-scrollbar="false"
        :scroll-into-view="scrollTarget"
        scroll-with-animation
      >
        <view class="content">
          <view class="hero-card">
            <view class="hero-top">
              <view class="hero-brand" @tap="handleBack">
                <uni-icons type="left" size="18" color="#a33e00" />
                <text class="hero-brand-text">KINETIC LOGIC</text>
              </view>
              <text class="hero-side-label">HELP DESK</text>
            </view>

            <view class="hero-copy">
              <text class="hero-eyebrow">SUPPORT CENTER</text>
              <text class="hero-title">把帮助说明、联系方式和反馈收纳到一个地方</text>
              <text class="hero-subtitle">
                这是独立给用户端使用的帮助页。你可以先查看常见问题，也可以直接把异常、建议和使用体验发给我们。
              </text>
            </view>

            <view class="hero-grid">
              <view class="hero-metric">
                <text class="metric-label">FAQ</text>
                <text class="metric-value">{{ faqs.length }} 项常见问题</text>
              </view>
              <view class="hero-metric">
                <text class="metric-label">SERVICE</text>
                <text class="metric-value">09:00 - 21:00 在线支持</text>
              </view>
            </view>
          </view>

          <view id="feedback-form" class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">快捷入口</text>
                <text class="panel-subtitle">遇到问题时优先给你最短的处理路径。</text>
              </view>
              <text class="panel-tag">QUICK ACTIONS</text>
            </view>

            <view class="quick-grid">
              <view class="quick-card" @tap="handleTopic('预约相关')">
                <text class="quick-title">预约帮助</text>
                <text class="quick-desc">快速查看约场、取消和时间规则说明</text>
              </view>
              <view class="quick-card" @tap="handleFeedback">
                <text class="quick-title">提交反馈</text>
                <text class="quick-desc">直接反馈建议、异常和体验问题</text>
              </view>
              <view class="quick-card" @tap="handleCustomerService">
                <text class="quick-title">在线客服</text>
                <text class="quick-desc">工作时间内获取人工服务指引</text>
              </view>
              <view class="quick-card" @tap="handleCall">
                <text class="quick-title">电话客服</text>
                <text class="quick-desc">400-888-8888</text>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">常见问题</text>
                <text class="panel-subtitle">先看一下大家最常遇到的几个问题。</text>
              </view>
              <text class="panel-tag">FAQ SNAPSHOT</text>
            </view>

            <view class="faq-list">
              <view
                v-for="(faq, index) in faqs"
                :key="faq.question"
                class="faq-item"
                @tap="toggleFaq(index)"
              >
                <view class="faq-head">
                  <text class="faq-question">{{ faq.question }}</text>
                  <text class="faq-toggle" :class="{ expanded: faq.expanded }">›</text>
                </view>
                <view v-if="faq.expanded" class="faq-answer">
                  <text class="faq-answer-text">{{ faq.answer }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">问题分类</text>
                <text class="panel-subtitle">按主题快速定位你最接近的问题类型。</text>
              </view>
              <text class="panel-tag">TOPICS</text>
            </view>

            <view class="topic-list">
              <view v-for="topic in topics" :key="topic.key" class="topic-row" @tap="handleTopic(topic.label)">
                <view class="topic-copy">
                  <text class="topic-title">{{ topic.label }}</text>
                  <text class="topic-desc">{{ topic.description }}</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">提交反馈</text>
                <text class="panel-subtitle">把问题、建议或异常情况直接发给我们。</text>
              </view>
              <text class="panel-tag">FEEDBACK</text>
            </view>

            <view class="form-stack">
              <view class="field-card">
                <text class="field-label">问题描述</text>
                <textarea
                  v-model="feedbackContent"
                  class="feedback-textarea"
                  placeholder="请详细描述你遇到的问题、建议或使用场景..."
                  maxlength="500"
                />
                <text class="field-tip">{{ feedbackContent.length }}/500</text>
              </view>

              <view class="field-card">
                <text class="field-label">联系方式（选填）</text>
                <input
                  v-model="contactInfo"
                  class="field-input"
                  type="text"
                  placeholder="手机号或邮箱，方便我们联系你"
                />
              </view>
            </view>

            <button class="submit-btn" :disabled="submitting" @tap="handleSubmitFeedback">
              <text class="submit-top">Send Feedback</text>
              <text class="submit-bottom">{{ submitting ? '提交中...' : '提交反馈' }}</text>
            </button>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">联系我们</text>
                <text class="panel-subtitle">需要进一步帮助时，可以通过下面的方式找到我们。</text>
              </view>
              <text class="panel-tag">CONTACT</text>
            </view>

            <view class="contact-list">
              <view class="contact-row" @tap="handleCall">
                <text class="contact-label">客服热线</text>
                <text class="contact-value">400-888-8888</text>
              </view>
              <view class="contact-row">
                <text class="contact-label">服务时间</text>
                <text class="contact-value">09:00 - 21:00</text>
              </view>
              <view class="contact-row" @tap="handleCopyEmail">
                <text class="contact-label">客服邮箱</text>
                <text class="contact-value">support@bmp.com</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { submitFeedback } from '@/api/auth'

const faqs = reactive([
  {
    question: '如何预约场地？',
    answer: '进入首页后点击场馆预订，选择场馆、日期和时间段后确认下单即可。',
    expanded: true
  },
  {
    question: '预约后可以取消吗？',
    answer: '可以。在预约开始前 2 小时通常支持免费取消，临近开场可能会产生手续费。',
    expanded: false
  },
  {
    question: '如何充值会员余额？',
    answer: '进入我的页或充值中心，选择金额并完成支付后即可到账。',
    expanded: false
  },
  {
    question: '忘记密码怎么办？',
    answer: '在登录页点击找回密码，按绑定手机号或其他验证方式重置密码。',
    expanded: false
  }
])

const topics = [
  {
    key: 'account',
    label: '账户相关',
    description: '登录、密码、资料和会员身份问题'
  },
  {
    key: 'booking',
    label: '预约相关',
    description: '场地、课程、赛事与预约记录问题'
  },
  {
    key: 'payment',
    label: '支付相关',
    description: '充值、退款、消费和钱包余额问题'
  },
  {
    key: 'other',
    label: '其他问题',
    description: '功能建议、体验意见和异常反馈'
  }
]

const feedbackContent = ref('')
const contactInfo = ref('')
const submitting = ref(false)
const scrollTarget = ref('')

function toggleFaq(index: number) {
  faqs[index].expanded = !faqs[index].expanded
}

function handleCustomerService() {
  uni.showModal({
    title: '在线客服',
    content: '客服工作时间为 09:00 - 21:00。如需帮助，也可以直接拨打客服热线。',
    showCancel: false
  })
}

function handleCall() {
  uni.makePhoneCall({
    phoneNumber: '4008888888',
    fail: () => {
      uni.showToast({
        title: '拨打失败',
        icon: 'none'
      })
    }
  })
}

function handleCopyEmail() {
  uni.setClipboardData({
    data: 'support@bmp.com',
    success: () => {
      uni.showToast({
        title: '邮箱已复制',
        icon: 'success'
      })
    }
  })
}

function handleTopic(label: string) {
  const content = `${label}：`
  feedbackContent.value = feedbackContent.value.trim() ? feedbackContent.value : content
  uni.showToast({
    title: `${label}可先查看常见问题或继续填写反馈`,
    icon: 'none'
  })
}

function handleFeedback() {
  scrollTarget.value = 'feedback-form'
  setTimeout(() => {
    scrollTarget.value = ''
  }, 350)
}

async function handleSubmitFeedback() {
  const content = feedbackContent.value.trim()
  if (!content) {
    uni.showToast({
      title: '请输入问题描述',
      icon: 'none'
    })
    return
  }

  try {
    submitting.value = true
    uni.showLoading({
      title: '提交中...'
    })
    await submitFeedback({
      content,
      contact: contactInfo.value.trim() || undefined
    })
    uni.hideLoading()
    uni.showToast({
      title: '反馈提交成功',
      icon: 'success'
    })
    feedbackContent.value = ''
    contactInfo.value = ''
  } catch (error) {
    console.error('提交反馈失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error instanceof Error ? error.message : '提交失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/settings/index')
}
</script>

<style lang="scss" scoped>
.help-shell {
  background:
    radial-gradient(circle at top left, rgba(255, 170, 112, 0.28), transparent 28%),
    radial-gradient(circle at top right, rgba(251, 146, 60, 0.16), transparent 22%),
    linear-gradient(180deg, #fff7ed 0%, #f8fafc 36%, #eef2ff 100%);
}

.help-page {
  min-height: 100vh;
}

.page-scroll {
  height: 100vh;
}

.content {
  padding: 30rpx 24rpx 48rpx;
}

.hero-card,
.panel-card {
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24rpx 60rpx rgba(15, 23, 42, 0.08);
}

.hero-card {
  position: relative;
  overflow: hidden;
  padding: 32rpx;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 237, 0.98));
}

.hero-card::before {
  content: '';
  position: absolute;
  right: -84rpx;
  top: -80rpx;
  width: 250rpx;
  height: 250rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.22), rgba(251, 146, 60, 0));
}

.hero-top,
.panel-head,
.faq-head,
.contact-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-top {
  position: relative;
  z-index: 1;
  margin-bottom: 36rpx;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 237, 213, 0.86);
}

.hero-brand-text,
.hero-side-label,
.hero-eyebrow,
.panel-tag,
.metric-label {
  letter-spacing: 0.18em;
}

.hero-brand-text {
  font-size: 20rpx;
  color: #9a3412;
  font-weight: 800;
}

.hero-side-label {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 700;
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  display: block;
  margin-bottom: 16rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.hero-title {
  display: block;
  font-size: 54rpx;
  line-height: 1.16;
  color: #0f172a;
  font-weight: 800;
}

.hero-subtitle {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.65;
  color: #64748b;
}

.hero-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 24rpx;
}

.hero-metric,
.quick-card {
  padding: 20rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.16);
  box-sizing: border-box;
}

.metric-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.metric-value {
  display: block;
  font-size: 26rpx;
  color: #0f172a;
  font-weight: 800;
  line-height: 1.45;
}

.panel-card {
  margin-top: 24rpx;
  padding: 28rpx;
}

.panel-title {
  display: block;
  font-size: 32rpx;
  color: #0f172a;
  font-weight: 800;
}

.panel-subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.panel-tag {
  font-size: 18rpx;
  color: #94a3b8;
  font-weight: 700;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 22rpx;
}

.quick-title,
.topic-title,
.field-label,
.contact-label {
  display: block;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 800;
}

.quick-desc,
.faq-answer-text,
.topic-desc,
.field-tip,
.contact-value {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.faq-list,
.topic-list,
.contact-list,
.form-stack {
  margin-top: 20rpx;
}

.quick-card,
.topic-row,
.faq-item,
.contact-row,
.field-card {
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #ffffff 100%);
  border: 1rpx solid rgba(255, 102, 0, 0.08);
}

.faq-list,
.topic-list,
.contact-list,
.form-stack {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.faq-item,
.topic-row,
.contact-row,
.field-card {
  padding: 24rpx;
}

.contact-list {
  margin-top: 22rpx;
}

.faq-item:last-child,
.topic-row:last-child,
.contact-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.faq-question {
  flex: 1;
  font-size: 26rpx;
  color: #0f172a;
  font-weight: 800;
}

.faq-toggle {
  font-size: 28rpx;
  color: #94a3b8;
  transition: transform 0.2s;
}

.faq-toggle.expanded {
  transform: rotate(90deg);
}

.faq-answer {
  padding-top: 14rpx;
}

.topic-copy {
  flex: 1;
}

.picker-value,
.contact-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.field-input,
.feedback-textarea {
  width: 100%;
  margin-top: 16rpx;
  padding: 0;
  font-size: 26rpx;
  color: #1f2937;
  background: transparent;
  box-sizing: border-box;
}

.feedback-textarea {
  min-height: 200rpx;
}

.submit-btn {
  width: 100%;
  min-height: 104rpx;
  margin-top: 28rpx;
  border: none;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #ff8a4c 0%, #ff6600 55%, #a33e00 100%);
  box-shadow: 0 22rpx 38rpx rgba(255, 102, 0, 0.22);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.submit-btn::after {
  border: none;
}

.submit-btn[disabled] {
  opacity: 0.7;
}

.submit-bottom {
  font-size: 30rpx;
  color: #ffffff;
  font-weight: 700;
}

.submit-top {
  color: rgba(255, 255, 255, 0.88);
  font-size: 20rpx;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.contact-value {
  margin-top: 0;
  color: #c2410c;
  text-align: right;
}
</style>
