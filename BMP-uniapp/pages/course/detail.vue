<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">课程详情</text>
        <view class="header-actions">
          <view class="action-icon" @click="handleShare"><uni-icons type="paperplane" size="18" color="#475569"></uni-icons></view>
          <view class="action-icon" @click="handleFavorite"><uni-icons type="heart" size="18" color="#475569"></uni-icons></view>
        </view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Course Banner -->
      <view class="course-banner">
        <view class="banner-image">
          <text class="image-placeholder">Course Banner</text>
        </view>
        <view class="banner-content">
          <view class="course-main">
            <text class="course-name">{{ course.name }}</text>
            <text class="course-price">¥{{ course.price }}<text class="price-unit">/课时</text></text>
          </view>
          <view class="course-meta">
            <view class="meta-item">
              <uni-icons type="person" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ course.coachName }}</text>
            </view>
            <view class="meta-item">
              <uni-icons type="person" size="16" color="#475569"></uni-icons>
              <text class="meta-text">已约 {{ course.currentStudents }}/{{ course.maxStudents }}</text>
            </view>
            <view class="meta-item">
              <uni-icons type="loop" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ course.duration }}分钟</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Course Info -->
      <view class="section course-info">
        <view class="info-item">
          <text class="info-label">课程时间</text>
          <text class="info-value">{{ course.date }} {{ course.time }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">适合人群</text>
          <text class="info-value">{{ course.level }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">课程地点</text>
          <text class="info-value">{{ course.location }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">教练简介</text>
          <text class="info-value">{{ course.coachInfo }}</text>
        </view>
      </view>

      <!-- Description -->
      <view class="section description-section">
        <text class="section-title">课程介绍</text>
        <text class="description-text">{{ course.description }}</text>
      </view>

      <!-- Coach Info -->
      <view class="section coach-section">
        <text class="section-title">教练信息</text>
        <view class="coach-info">
          <view class="coach-avatar">
            <uni-icons type="person" size="28" color="#94a3b8" class="coach-icon"></uni-icons>
          </view>
          <view class="coach-details">
            <text class="coach-name">{{ course.coachName }}</text>
            <view class="coach-stats">
              <uni-icons type="star-filled" size="14" color="#f59e0b"></uni-icons>
              <text class="coach-rating">{{ course.coachRating }}</text>
              <text class="coach-experience">{{ course.coachExperience }}年经验</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Reviews -->
      <view class="section reviews-section">
        <text class="section-title">学员评价</text>
        <view v-for="(review, index) in course.reviews" :key="index" class="review-item">
          <view class="review-header">
            <text class="review-user">{{ review.user }}</text>
            <view class="review-stars">
              <uni-icons v-for="n in 5" :key="n" :type="n <= review.stars ? 'star-filled' : 'star'" size="14" :color="n <= review.stars ? '#f59e0b' : '#E2E8F0'" class="star"></uni-icons>
            </view>
          </view>
          <text class="review-content">{{ review.content }}</text>
          <text class="review-date">{{ review.date }}</text>
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
        class="action-btn book-btn" 
        :class="{ disabled: course.currentStudents >= course.maxStudents }"
        :disabled="course.currentStudents >= course.maxStudents"
        @click="handleBook"
      >
        <uni-icons type="calendar" size="18" color="#ffffff" class="btn-icon"></uni-icons>
        <text class="btn-text">
          {{ course.currentStudents >= course.maxStudents ? '名额已满' : '立即预约' }}
        </text>
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCourseDetail } from '@/api/course'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const courseId = ref<number>(0)
const course = ref<any>({
  id: 0,
  name: '羽毛球基础课程',
  coachName: '张教练',
  price: 200,
  date: '2026-01-28',
  time: '14:00-15:00',
  duration: 60,
  level: '初级',
  location: '奥体中心羽毛球馆-A01',
  maxStudents: 10,
  currentStudents: 6,
  description: '本课程专为羽毛球初学者设计，教授基本握拍、发球、高远球等基础技术，帮助学员掌握羽毛球运动的基本技能。',
  coachInfo: '国家二级羽毛球教练，10年教学经验',
  coachRating: 4.8,
  coachExperience: 10,
  reviews: [
    { user: '学员甲', stars: 5, content: '教练很专业，讲解细致，受益匪浅。', date: '2026-01-20' },
    { user: '学员乙', stars: 4, content: '课程安排合理，循序渐进，适合新手。', date: '2026-01-18' }
  ]
})

const userStore = useUserStore()

// 页面加载
onLoad((options) => {
  if (options.id) {
    courseId.value = Number(options.id)
  }
})

// 加载课程详情
const loadCourseDetail = async () => {
  try {
    const result = await getCourseDetail(courseId.value)
    
    // 将API数据映射到页面所需格式
    course.value = {
      id: result.id,
      name: result.courseName,
      coachName: result.coachName,
      price: result.coursePrice,
      date: result.courseDate,
      time: `${result.startTime} - ${result.endTime}`,
      duration: result.courseDuration,
      level: result.level || '初级',
      location: result.location || result.venueName,
      maxStudents: result.maxStudents,
      currentStudents: result.currentStudents,
      description: result.description,
      coachInfo: result.coachInfo || '专业羽毛球教练',
      coachRating: result.coachRating || 4.8,
      coachExperience: result.coachExperience || 5,
      reviews: result.reviews || []
    }
  } catch (error) {
    console.error('加载课程详情失败:', error)
    uni.showToast({
      title: '加载课程详情失败',
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

// 预约课程
const handleBook = () => {
  uni.showModal({
    title: '预约确认',
    content: `确定要预约"${course.value.name}"吗？`,
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({
          url: `/pages/course/booking?id=${course.value.id}`
        })
      }
    }
  })
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
  
  if (courseId.value) {
    await loadCourseDetail()
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

.course-banner {
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

.course-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.course-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.course-price {
  font-size: 28rpx;
  font-weight: bold;
  color: #ef4444;
}

.price-unit {
  font-size: 20rpx;
  font-weight: normal;
  color: #999999;
}

.course-meta {
  display: flex;
  gap: 32rpx;
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

.description-text {
  font-size: 24rpx;
  color: #666666;
  line-height: 1.6;
}

.coach-section {
  .coach-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  .coach-avatar {
    width: 80rpx;
    height: 80rpx;
    background-color: #f5f5f5;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .coach-icon {
    font-size: 40rpx;
    color: #999999;
  }

  .coach-details {
    flex: 1;
  }

  .coach-name {
    font-size: 26rpx;
    font-weight: bold;
    color: #333333;
    display: block;
    margin-bottom: 8rpx;
  }

  .coach-stats {
    display: flex;
    gap: 16rpx;
  }

  .coach-rating {
    font-size: 22rpx;
    color: #ffc107;
  }

  .coach-experience {
    font-size: 22rpx;
    color: #999999;
  }
}

.reviews-section {
  .review-item {
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f3f4f6;

    &:last-child {
      border-bottom: none;
    }
  }

  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12rpx;
  }

  .review-user {
    font-size: 24rpx;
    color: #333333;
    font-weight: 500;
  }

  .review-stars {
    display: flex;
    gap: 4rpx;
  }

  .star {
    font-size: 20rpx;
    color: #ddd;

    &.active {
      color: #ffc107;
    }
  }

  .review-content {
    font-size: 22rpx;
    color: #666666;
    line-height: 1.5;
    margin-bottom: 8rpx;
  }

  .review-date {
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

.book-btn {
  background-color: #3cc51f;
  color: #ffffff;

  &.disabled {
    background-color: #cccccc;
    color: #999999;
  }
}
</style>