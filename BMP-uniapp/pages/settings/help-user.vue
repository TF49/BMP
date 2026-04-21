<template>
  <MobileLayout className="help-shell">
    <view class="help-page">
      <scroll-view scroll-y class="page-scroll" :show-scrollbar="false">
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
              <text class="hero-title">把帮助说明和联系方式收纳到一个地方</text>
              <text class="hero-subtitle">
                这是独立给用户端使用的帮助页，用来承接常见说明、客服联系方式和常用问题快照，不影响会长端原有帮助入口。
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

          <view class="panel-card">
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
              <view class="quick-card" @tap="handleCustomerService">
                <text class="quick-title">在线客服</text>
                <text class="quick-desc">工作时间内获取人工服务指引</text>
              </view>
              <view class="quick-card" @tap="handleCall">
                <text class="quick-title">电话客服</text>
                <text class="quick-desc">400-888-8888</text>
              </view>
              <view class="quick-card" @tap="handleCopyEmail">
                <text class="quick-title">客服邮箱</text>
                <text class="quick-desc">support@bmp.com</text>
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
import { reactive } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

const userStore = useUserStore()

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
  uni.showToast({
    title: `${label}可先查看下方常见问题`,
    icon: 'none'
  })
}

function handleBack() {
  safeNavigateBack('/pages/settings/index')
}

if (!userStore.isLoggedIn) {
  uni.redirectTo({
    url: '/pages/login/login'
  })
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
.contact-label {
  display: block;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 800;
}

.quick-desc,
.faq-answer-text,
.topic-desc,
.contact-value {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.faq-list,
.topic-list {
  margin-top: 20rpx;
}

.faq-item,
.topic-row,
.contact-row {
  padding: 22rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.82);
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

.contact-value {
  margin-top: 0;
  color: #c2410c;
  text-align: right;
}
</style>
