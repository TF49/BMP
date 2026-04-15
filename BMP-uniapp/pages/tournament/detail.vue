<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">赛事详情</text>
        <view class="header-actions">
          <view class="action-icon" @click="handleShare"><uni-icons type="paperplane" size="18" color="#475569"></uni-icons></view>
          <view class="action-icon" @click="handleFavorite"><uni-icons type="heart" size="18" color="#475569"></uni-icons></view>
        </view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Tournament Banner -->
      <view class="tournament-banner">
        <view class="banner-image">
          <text class="image-placeholder">Tournament Banner</text>
        </view>
        <view class="banner-content">
          <view class="tournament-main">
            <text class="tournament-name">{{ tournament.name }}</text>
            <text class="tournament-status" :class="getStatusClass(tournament.status)">
              {{ getStatusText(tournament.status) }}
            </text>
          </view>
          <view class="tournament-meta">
            <view class="meta-item">
              <uni-icons type="calendar" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ tournament.date }} {{ tournament.time }}</text>
            </view>
            <view class="meta-item">
              <uni-icons type="location" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ tournament.location }}</text>
            </view>
            <view class="meta-item">
              <uni-icons type="wallet" size="16" color="#475569"></uni-icons>
              <text class="meta-text">报名费 ¥{{ tournament.fee }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Tournament Info -->
      <view class="section tournament-info">
        <view class="info-item">
          <text class="info-label">赛事级别</text>
          <text class="info-value">{{ tournament.level }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">参赛人数</text>
          <text class="info-value">{{ tournament.participants }}/{{ tournament.maxParticipants }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">报名截止</text>
          <text class="info-value">{{ tournament.registrationDeadline }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">主办方</text>
          <text class="info-value">{{ tournament.organizer }}</text>
        </view>
      </view>

      <!-- Description -->
      <view class="section description-section">
        <text class="section-title">赛事介绍</text>
        <text class="description-text">{{ tournament.description }}</text>
      </view>

      <!-- Rules -->
      <view class="section rules-section">
        <text class="section-title">比赛规则</text>
        <text class="rules-text">{{ tournament.rules }}</text>
      </view>

      <!-- Registration Info -->
      <view class="section registration-section">
        <text class="section-title">报名信息</text>
        <view class="info-item">
          <text class="info-label">报名条件</text>
          <text class="info-value">{{ tournament.requirements }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">奖励设置</text>
          <text class="info-value">{{ tournament.prizes }}</text>
        </view>
      </view>

      <!-- Participants -->
      <view class="section participants-section">
        <text class="section-title">参赛选手</text>
        <view class="participants-list">
          <view v-for="(participant, index) in tournament.participantsList" :key="index" class="participant-item">
            <text class="participant-name">{{ participant.name }}</text>
            <text class="participant-level">{{ participant.level }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Action Bar -->
    <view class="action-bar">
      <button class="action-btn favorite-btn" @click="handleFavoriteBtn">
        <uni-icons type="heart" size="18" color="#475569" class="btn-icon"></uni-icons>
        <text class="btn-text">收藏</text>
      </button>
      <button 
        class="action-btn register-btn" 
        :class="{ disabled: !canRegister }"
        :disabled="!canRegister"
        @click="handleRegister"
      >
        <uni-icons type="medal" size="18" color="#ffffff" class="btn-icon"></uni-icons>
        <text class="btn-text">
          {{ registerButtonText }}
        </text>
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getTournamentDetail } from '@/api/tournament'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const tournamentId = ref<number>(0)
const tournament = ref<any>({
  id: 0,
  name: '2026年春季羽毛球友谊赛',
  date: '2026-03-15',
  time: '09:00-17:00',
  location: '市体育馆羽毛球馆',
  level: '业余组',
  fee: 50,
  maxParticipants: 64,
  participants: 32,
  status: 1,
  registrationDeadline: '2026-03-10',
  organizer: '市羽毛球协会',
  description: '本次比赛旨在促进羽毛球爱好者之间的交流，提高技术水平，增进友谊。比赛采用淘汰制，欢迎广大羽毛球爱好者踊跃报名参赛。',
  rules: '1. 比赛采用三局两胜制\n2. 每局21分\n3. 发球权轮换\n4. 选手需自备球拍',
  requirements: '身体健康，无心脏病等不适合剧烈运动的疾病',
  prizes: '冠军：奖金2000元+奖杯\n亚军：奖金1000元+奖杯\n季军：奖金500元+奖杯',
  participantsList: [
    { name: '张三', level: '业余高手' },
    { name: '李四', level: '中级水平' },
    { name: '王五', level: '中级水平' }
  ]
})

const userStore = useUserStore()

// 计算属性
const canRegister = computed(() => {
  return tournament.value.status === 1 && 
         tournament.value.participants < tournament.value.maxParticipants
})

const registerButtonText = computed(() => {
  if (tournament.value.status === 1) {
    if (tournament.value.participants >= tournament.value.maxParticipants) {
      return '人数已满'
    }
    return '立即报名'
  } else if (tournament.value.status === 2) {
    return '进行中'
  } else if (tournament.value.status === 3) {
    return '已结束'
  }
  return '查看详情'
})

// 页面加载
onLoad((options?: Record<string, string | undefined>) => {
  if (options?.id) {
    tournamentId.value = Number(options.id)
  }
})

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '报名中',
    2: '进行中',
    3: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'status-cancelled',
    1: 'status-registering',
    2: 'status-ongoing',
    3: 'status-ended'
  }
  return classMap[status] || ''
}

// 加载赛事详情
const loadTournamentDetail = async () => {
  try {
    const result = await getTournamentDetail(tournamentId.value)
    
    // 将API数据转换为页面所需格式
    tournament.value = {
      id: result.id,
      name: result.tournamentName,
      date: result.startDate,
      time: result.startTime,
      location: result.location,
      level: result.level || '业余',
      fee: result.entryFee || 0,
      maxParticipants: result.maxParticipants,
      participants: result.currentParticipants || 0,
      status: result.status,
      registrationDeadline: result.registrationDeadline,
      organizer: result.organizer || '主办方',
      description: result.description,
      rules: result.rules || '比赛规则',
      requirements: result.requirements || '报名条件',
      prizes: result.prizes || '奖励设置',
      participantsList: result.participantsList || []
    }
  } catch (error) {
    console.error('加载赛事详情失败:', error)
    uni.showToast({
      title: '加载赛事详情失败',
      icon: 'none'
    })
  }
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 分享功能
const handleShare = () => {
  uni.showActionSheet({
    itemList: ['微信好友', '朋友圈', '复制链接'],
    success: (res) => {
      uni.showToast({
        title: `已分享给${['微信好友', '朋友圈', '复制链接'][res.tapIndex]}`,
        icon: 'none'
      })
    }
  })
}

// 收藏功能
const handleFavorite = () => {
  uni.showToast({
    title: '已收藏',
    icon: 'none'
  })
}

// 收藏按钮
const handleFavoriteBtn = () => {
  handleFavorite()
}

// 报名赛事
const handleRegister = () => {
  if (canRegister.value) {
    uni.showModal({
      title: '报名确认',
      content: `确定要报名"${tournament.value.name}"吗？报名费: ¥${tournament.value.fee}`,
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({
            url: `/pages/tournament/register?id=${tournament.value.id}`
          })
        }
      }
    })
  }
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
  
  if (tournamentId.value) {
    await loadTournamentDetail()
  }
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

.header-actions {
  display: flex;
  gap: 24rpx;
}

.action-icon {
  font-size: 32rpx;
  color: #999999;
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.tournament-banner {
  background-color: #ffffff;
  margin-bottom: 20rpx;
}

.banner-image {
  height: 240rpx;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(153, 153, 153, 0.3);
  font-size: 20rpx;
}

.banner-content {
  padding: 28rpx;
}

.tournament-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.tournament-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.tournament-status {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  
  &.status-registering {
    background-color: #e8f5e9;
    color: #3cc51f;
  }
  
  &.status-closed {
    background-color: #fff3e0;
    color: #ff9800;
  }
  
  &.status-ongoing {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  
  &.status-ended {
    background-color: #f5f5f5;
    color: #999999;
  }
  
  &.status-cancelled {
    background-color: #ffebee;
    color: #f44336;
  }
}

.tournament-meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
  color: #999999;
}

.meta-text {
  font-size: 22rpx;
  color: #666666;
}

.section {
  background-color: #ffffff;
  margin-bottom: 20rpx;
  padding: 28rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 24rpx;
  color: #999999;
  width: 120rpx;
}

.info-value {
  font-size: 24rpx;
  color: #333333;
  flex: 1;
  text-align: right;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.description-text, .rules-text {
  font-size: 24rpx;
  color: #666666;
  line-height: 1.6;
}

.participants-section {
  .participants-list {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
  }

  .participant-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12rpx 0;
    border-bottom: 1rpx solid #f3f4f6;

    &:last-child {
      border-bottom: none;
    }
  }

  .participant-name {
    font-size: 24rpx;
    color: #333333;
  }

  .participant-level {
    font-size: 20rpx;
    color: #999999;
  }
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  height: 120rpx;
  background-color: #ffffff;
  border-top: 1rpx solid #e6e6e6;
  padding: 0 28rpx;
  box-sizing: border-box;
  align-items: center;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
  margin: 20rpx 8rpx;

  .btn-icon {
    margin-right: 8rpx;
    font-size: 28rpx;
  }

  .btn-text {
    font-size: 28rpx;
  }
}

.favorite-btn {
  background-color: #f5f5f5;
  color: #333333;
}

.register-btn {
  background-color: #3cc51f;
  color: #ffffff;

  &.disabled {
    background-color: #cccccc;
    color: #999999;
  }
}
</style>
