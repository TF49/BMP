<template>
  <MobileLayout :showTabBar="false">
    <view class="faq-container">
      <view class="search-section">
        <uni-search-bar
          v-model="searchKeyword"
          placeholder="搜索常见问题"
          @confirm="handleSearch"
        />
      </view>

      <view class="faq-list">
        <view
          v-for="(item, index) in filteredFaqs"
          :key="index"
          class="faq-item"
          :class="{ active: expandedIndex === index }"
        >
          <view class="faq-header" @click="toggleExpand(index)">
            <view class="faq-category">
              <text class="category-tag">{{ item.category }}</text>
            </view>
            <text class="faq-question">{{ item.question }}</text>
            <text class="expand-icon">{{ expandedIndex === index ? '▼' : '▶' }}</text>
          </view>

          <view v-if="expandedIndex === index" class="faq-content">
            <text class="faq-answer">{{ item.answer }}</text>
            <view v-if="item.tips" class="faq-tips">
              <view class="tips-title"><uni-icons type="info" size="16" color="#f59e0b"></uni-icons><text> 提示：</text></view>
              <text class="tips-text">{{ item.tips }}</text>
            </view>
          </view>
        </view>

        <view v-if="filteredFaqs.length === 0" class="empty-state">
          <text class="empty-text">未找到相关问题</text>
        </view>
      </view>

      <view class="contact-section">
        <view class="contact-card">
          <text class="contact-title">仍未解决？</text>
          <text class="contact-desc">联系我们的客服团队获取帮助</text>
          <button class="contact-btn" @click="handleContactService">
            联系客服
          </button>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'

interface FAQ {
  category: string
  question: string
  answer: string
  tips?: string
}

const searchKeyword = ref('')
const expandedIndex = ref<number | null>(null)

const faqs: FAQ[] = [
  {
    category: '账户',
    question: '如何修改我的账户信息？',
    answer: '您可以在"我的" > "个人中心" > "个人信息"中修改您的账户信息，包括昵称、头像等。',
    tips: '修改后需要重新登录才能生效。'
  },
  {
    category: '账户',
    question: '忘记密码怎么办？',
    answer: '在登录页面点击"忘记密码"，按照提示输入注册手机号和验证码，即可重置密码。',
    tips: '请确保您的手机号能正常接收短信。'
  },
  {
    category: '预约',
    question: '如何预约场地？',
    answer: '1. 进入"场馆"页面\n2. 选择您想要的场馆\n3. 选择日期和时间\n4. 确认预约并支付\n5. 预约成功',
    tips: '预约成功后会收到确认短信。'
  },
  {
    category: '预约',
    question: '如何取消预约？',
    answer: '进入"我的预约"，找到要取消的预约，点击"取消预约"按钮即可。取消后会退款到您的账户。',
    tips: '预约开始前2小时可以免费取消。'
  },
  {
    category: '支付',
    question: '支持哪些支付方式？',
    answer: '我们支持以下支付方式：\n• 微信支付\n• 支付宝\n• 银行卡\n• 账户余额',
    tips: '首次支付可能需要进行身份验证。'
  },
  {
    category: '支付',
    question: '支付失败怎么办？',
    answer: '如果支付失败，请检查：\n1. 网络连接是否正常\n2. 账户余额是否充足\n3. 支付方式是否有效\n\n如问题仍未解决，请联系客服。',
    tips: '支付失败不会扣款，您可以安全重试。'
  },
  {
    category: '充值',
    question: '如何充值账户余额？',
    answer: '进入"我的" > "充值中心"，选择充值金额，选择支付方式，完成支付即可。充值后立即到账。',
    tips: '充值金额可用于预约、课程、器材租借等消费。'
  },
  {
    category: '充值',
    question: '充值的钱可以退款吗？',
    answer: '充值的余额可以用于消费。如需退款，请联系客服，我们会在7个工作日内处理。',
    tips: '退款需要提供充值记录和身份验证。'
  },
  {
    category: '课程',
    question: '如何报名课程？',
    answer: '1. 进入"课程"页面\n2. 选择您感兴趣的课程\n3. 查看课程详情和教练信息\n4. 点击"立即报名"\n5. 完成支付',
    tips: '课程报名后会收到确认信息。'
  },
  {
    category: '赛事',
    question: '如何报名赛事？',
    answer: '1. 进入"赛事"页面\n2. 选择要参加的赛事\n3. 查看赛事规则和报名要求\n4. 点击"立即报名"\n5. 填写参赛信息并支付报名费',
    tips: '报名截止前可以修改参赛信息。'
  }
]

const filteredFaqs = computed(() => {
  if (!searchKeyword.value) return faqs

  const keyword = searchKeyword.value.toLowerCase()
  return faqs.filter(
    (item) =>
      item.question.toLowerCase().includes(keyword) ||
      item.answer.toLowerCase().includes(keyword) ||
      item.category.toLowerCase().includes(keyword)
  )
})

const toggleExpand = (index: number) => {
  expandedIndex.value = expandedIndex.value === index ? null : index
}

const handleSearch = () => {
  // 搜索已通过computed自动处理
}

const handleContactService = () => {
  uni.navigateTo({
    url: '/pages/settings/feedback'
  })
}
</script>

<style lang="scss" scoped>
.faq-container {
  padding: 20rpx;

  .search-section {
    margin-bottom: 20rpx;
  }

  .faq-list {
    .faq-item {
      background: white;
      border-radius: 12rpx;
      margin-bottom: 15rpx;
      overflow: hidden;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;

      &.active {
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
      }

      .faq-header {
        display: flex;
        align-items: center;
        padding: 20rpx;
        cursor: pointer;
        user-select: none;

        .faq-category {
          margin-right: 15rpx;

          .category-tag {
            display: inline-block;
            background: #3cc51f;
            color: white;
            padding: 4rpx 12rpx;
            border-radius: 20rpx;
            font-size: 22rpx;
            font-weight: bold;
          }
        }

        .faq-question {
          flex: 1;
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }

        .expand-icon {
          font-size: 24rpx;
          color: #999;
          margin-left: 10rpx;
          transition: transform 0.3s ease;
        }
      }

      .faq-content {
        padding: 0 20rpx 20rpx;
        border-top: 1rpx solid #f0f0f0;

        .faq-answer {
          display: block;
          font-size: 26rpx;
          color: #666;
          line-height: 1.8;
          white-space: pre-wrap;
          word-break: break-word;
        }

        .faq-tips {
          margin-top: 15rpx;
          padding: 15rpx;
          background: #f5f7fa;
          border-radius: 8rpx;

          .tips-title {
            display: flex;
            align-items: center;
            gap: 6rpx;
            font-size: 24rpx;
            color: #3cc51f;
            font-weight: bold;
            margin-bottom: 8rpx;
          }

          .tips-text {
            display: block;
            font-size: 24rpx;
            color: #666;
            line-height: 1.6;
          }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 60rpx 20rpx;

      .empty-text {
        font-size: 28rpx;
        color: #999;
      }
    }
  }

  .contact-section {
    margin-top: 40rpx;
    margin-bottom: 40rpx;

    .contact-card {
      background: linear-gradient(135deg, #3cc51f 0%, #2aa51a 100%);
      border-radius: 12rpx;
      padding: 30rpx;
      text-align: center;

      .contact-title {
        display: block;
        font-size: 32rpx;
        color: white;
        font-weight: bold;
        margin-bottom: 10rpx;
      }

      .contact-desc {
        display: block;
        font-size: 26rpx;
        color: rgba(255, 255, 255, 0.9);
        margin-bottom: 20rpx;
      }

      .contact-btn {
        background: white;
        color: #3cc51f;
        border: none;
        border-radius: 8rpx;
        padding: 15rpx 40rpx;
        font-size: 28rpx;
        font-weight: bold;
      }
    }
  }
}
</style>
