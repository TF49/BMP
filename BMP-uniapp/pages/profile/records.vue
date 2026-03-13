<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">消费记录</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Filter Tabs -->
    <view class="filter-tabs">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        {{ tab }}
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y @scrolltolower="loadMore">
      <view v-if="records.length === 0" class="empty-state">
        <text class="empty-text">暂无消费记录</text>
      </view>
      
      <view v-else class="records-list">
        <view 
          v-for="(record, index) in records" 
          :key="index"
          class="record-item"
        >
          <view class="record-header">
            <text class="record-type">{{ record.typeText }}</text>
            <text class="record-amount" :class="record.isIncome ? 'income' : 'expense'">
              {{ record.isIncome ? '+' : '-' }}¥{{ record.amount }}
            </text>
          </view>
          <view class="record-details">
            <text class="record-description">{{ record.description }}</text>
            <text class="record-time">{{ record.time }}</text>
          </view>
          <view class="record-status">
            <text class="status-text">{{ record.statusText }}</text>
            <text class="record-id">订单号: {{ record.orderId }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getMemberConsumeRecords } from '@/api/member'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const currentTab = ref(0)
const tabs = ref(['全部', '充值', '消费', '退款'])
const records = ref<any[]>([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const userStore = useUserStore()

// 切换标签
const switchTab = (index: number) => {
  currentTab.value = index
  currentPage.value = 1
  records.value = []
  loadRecords()
}

// 加载消费记录
const loadRecords = async () => {
  if (loading.value || !hasMore.value) return

  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    // 根据当前标签筛选类型
    if (currentTab.value === 1) { // 充值
      params.type = 'RECHARGE'
    } else if (currentTab.value === 2) { // 消费
      params.type = 'CONSUME'
    } else if (currentTab.value === 3) { // 退款
      params.type = 'REFUND'
    }

    const result = await getMemberConsumeRecords({
      memberId: userStore.userId,
      ...params
    })

    const newRecords = result.data.map((record: any) => ({
      id: record.id,
      typeText: getRecordTypeText(record.type),
      amount: record.amount,
      description: record.description || getRecordDescription(record),
      time: formatDate(record.createTime),
      isIncome: record.type === 'RECHARGE' || record.type === 'REFUND',
      statusText: getRecordStatusText(record.status),
      orderId: record.orderId || record.id,
      type: record.type
    }))

    records.value = [...records.value, ...newRecords]
    hasMore.value = records.value.length < result.total
  } catch (error) {
    console.error('加载消费记录失败:', error)
    uni.showToast({
      title: '加载消费记录失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 获取记录类型文本
const getRecordTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'RECHARGE': '充值',
    'CONSUME': '消费',
    'REFUND': '退款',
    'BOOKING': '场地预约',
    'COURSE': '课程预约',
    'EQUIPMENT': '器材租借',
    'TOURNAMENT': '赛事报名'
  }
  return typeMap[type] || '其他'
}

// 获取记录描述
const getRecordDescription = (record: any) => {
  if (record.type === 'RECHARGE') {
    return `会员充值 - ${record.method || '在线支付'}`
  } else if (record.type === 'CONSUME') {
    return `场地预约 - ${record.venueName || record.courtName || '羽毛球场地'}`
  } else if (record.type === 'BOOKING') {
    return `场地预约 - ${record.courtName || '羽毛球场地'}`
  } else if (record.type === 'COURSE') {
    return `课程预约 - ${record.courseName || '羽毛球课程'}`
  } else if (record.type === 'EQUIPMENT') {
    return `器材租借 - ${record.equipmentName || '运动器材'}`
  } else if (record.type === 'TOURNAMENT') {
    return `赛事报名 - ${record.tournamentName || '羽毛球赛事'}`
  }
  return '消费记录'
}

// 获取记录状态文本
const getRecordStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '失败',
    1: '成功',
    2: '处理中',
    3: '已退款'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) + ' ' +
           date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    currentPage.value++
    loadRecords()
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
  
  await loadRecords()
})

// 启用下拉刷新
onPullDownRefresh(() => {
  currentPage.value = 1
  records.value = []
  loadRecords().finally(() => {
    uni.stopPullDownRefresh()
  })
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

.filter-tabs {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  display: flex;
  gap: 18rpx;
  overflow-x: auto;
  border-bottom: 1rpx solid #e6e6e6;
}

.tab-item {
  flex-shrink: 0;
  padding: 10rpx 20rpx;
  font-size: 24rpx;
  color: #999999;
  border-radius: 9999rpx;
  transition: all 0.3s;
  
  &.active {
    background-color: #3cc51f;
    color: #ffffff;
  }
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
  padding: 24rpx 28rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 160rpx 0;
}

.empty-text {
  font-size: 24rpx;
  color: #999999;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.record-item {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.record-type {
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
}

.record-amount {
  font-size: 24rpx;
  font-weight: bold;

  &.income {
    color: #3cc51f;
  }

  &.expense {
    color: #ef4444;
  }
}

.record-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.record-description {
  font-size: 22rpx;
  color: #666666;
  flex: 1;
}

.record-time {
  font-size: 20rpx;
  color: #999999;
  white-space: nowrap;
  margin-left: 12rpx;
}

.record-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-text {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  background-color: #f5f5f5;
  color: #999999;
}

.record-id {
  font-size: 18rpx;
  color: #999999;
}
</style>