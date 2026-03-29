<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">课程预约</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Filter Section -->
    <view class="filter-section">
      <view class="filter-bar">
        <view class="filter-item" @click="showCoachFilter = true">
          <text class="filter-label">教练</text>
          <text class="filter-value">{{ selectedCoach || '全部' }}</text>
          <text class="chevron">▼</text>
        </view>
        <view class="filter-item" @click="showLevelFilter = true">
          <text class="filter-label">级别</text>
          <text class="filter-value">{{ selectedLevel || '全部' }}</text>
          <text class="chevron">▼</text>
        </view>
        <view class="filter-item" @click="showDateFilter = true">
          <text class="filter-label">日期</text>
          <text class="filter-value">{{ selectedDate || '全部' }}</text>
          <text class="chevron">▼</text>
        </view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <view v-if="courseList.length === 0" class="empty-state">
        <text class="empty-text">暂无课程</text>
      </view>
      
      <view v-else class="course-list">
        <view 
          v-for="(course, index) in courseList" 
          :key="index"
          class="course-item"
          @click="handleCourseClick(course)"
        >
          <view class="course-image">
            <text class="image-placeholder">Course Image</text>
          </view>
          <view class="course-info">
            <view class="course-header">
              <text class="course-name">{{ course.name }}</text>
              <text class="course-price">¥{{ course.price }}</text>
            </view>
            <view class="course-meta">
              <text class="coach-name">教练: {{ course.coachName }}</text>
              <text class="course-level">{{ course.level }}</text>
            </view>
            <view class="course-time">
              <text class="time-text">{{ course.date }} {{ course.time }}</text>
            </view>
            <view class="course-stats">
              <text class="capacity">已约 {{ course.currentStudents }}/{{ course.maxStudents }}</text>
              <view class="progress-bar">
                <view class="progress" :style="{ width: course.progress + '%' }"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Filters Popup -->
    <uni-popup :show="showCoachFilter" mode="bottom" @close="showCoachFilter = false">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">选择教练</text>
          <text class="popup-close" @click="showCoachFilter = false">×</text>
        </view>
        <scroll-view class="popup-list" scroll-y>
          <view 
            v-for="(coach, index) in coachList" 
            :key="index"
            class="popup-item"
            :class="{ active: selectedCoach === coach.name }"
            @click="selectCoach(coach.name)"
          >
            {{ coach.name }}
          </view>
        </scroll-view>
      </view>
    </uni-popup>

    <uni-popup :show="showLevelFilter" mode="bottom" @close="showLevelFilter = false">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">选择级别</text>
          <text class="popup-close" @click="showLevelFilter = false">×</text>
        </view>
        <scroll-view class="popup-list" scroll-y>
          <view 
            v-for="(level, index) in levelList" 
            :key="index"
            class="popup-item"
            :class="{ active: selectedLevel === level }"
            @click="selectLevel(level)"
          >
            {{ level }}
          </view>
        </scroll-view>
      </view>
    </uni-popup>
    <!-- Custom BottomNavBar Shell -->
    <CustomTabBar :current="2" />
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { getCourseList, getCoachList } from '@/api/course'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const courseList = ref<any[]>([])
const coachList = ref<any[]>([])
const loading = ref(false)
const userStore = useUserStore()

// 筛选条件
const selectedCoach = ref<string | null>(null)
const selectedLevel = ref<string | null>(null)
const selectedDate = ref<string | null>(null)

// 筛选弹窗
const showCoachFilter = ref(false)
const showLevelFilter = ref(false)
const showDateFilter = ref(false)

// 级别列表
const levelList = ref(['初级', '中级', '高级', '专业'])

// 加载课程列表
const loadCourseList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: 1,
      size: 20
    }

    if (selectedCoach.value) {
      params.coachName = selectedCoach.value
    }
    if (selectedLevel.value) {
      params.level = selectedLevel.value
    }

    const result = await getCourseList(params)
    
    // 将API数据转换为页面所需格式
    courseList.value = result.data.map((course: any) => ({
      id: course.id,
      name: course.courseName,
      coachName: course.coachName,
      price: course.coursePrice,
      date: course.courseDate,
      time: `${course.startTime} - ${course.endTime}`,
      maxStudents: course.maxStudents,
      currentStudents: course.currentStudents,
      level: course.level || '初级',
      progress: course.maxStudents > 0 ? Math.round((course.currentStudents / course.maxStudents) * 100) : 0
    }))
  } catch (error) {
    console.error('加载课程列表失败:', error)
    uni.showToast({
      title: '加载课程列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 加载教练列表
const loadCoachList = async () => {
  try {
    const result = await getCoachList({ page: 1, size: 50 })
    
    if (result && Array.isArray(result)) {
      coachList.value = result.map((coach: any) => ({
        id: coach.id,
        name: coach.coachName || coach.name,
        specialty: coach.specialty || '专业教练'
      }))
      // 缓存教练列表数据（不经常变化）
      uni.setStorageSync('coaches_cache', coachList.value)
    } else {
      loadCoachListFromCache()
    }
  } catch (error) {
    console.error('加载教练列表失败:', error)
    // 尝试从缓存读取
    loadCoachListFromCache()
  }
}

// 从缓存加载教练列表
const loadCoachListFromCache = () => {
  const cached = uni.getStorageSync('coaches_cache')
  if (cached && Array.isArray(cached)) {
    coachList.value = cached
  } else {
    coachList.value = []
  }
}

// 选择教练
const selectCoach = (coachName: string) => {
  selectedCoach.value = coachName
  showCoachFilter.value = false
  loadCourseList() // 重新加载课程列表
}

// 选择级别
const selectLevel = (level: string) => {
  selectedLevel.value = level
  showLevelFilter.value = false
  loadCourseList() // 重新加载课程列表
}

// 课程点击事件
const handleCourseClick = (course: any) => {
  uni.navigateTo({
    url: `/pages/course/detail?id=${course.id}`
  })
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
  
  await loadCourseList()
  await loadCoachList()
})

// 启用下拉刷新
onPullDownRefresh(() => {
  loadCourseList().finally(() => {
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

.filter-section {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  border-bottom: 1rpx solid #e6e6e6;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.filter-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 12rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  background-color: #f9f9f9;
}

.filter-label {
  font-size: 20rpx;
  color: #999999;
}

.filter-value {
  font-size: 22rpx;
  color: #333333;
  flex: 1;
  text-align: center;
}

.chevron {
  font-size: 20rpx;
  color: #999999;
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

.course-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.course-item {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20rpx;
}

.course-image {
  width: 140rpx;
  height: 140rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(153, 153, 153, 0.3);
  font-size: 20rpx;
}

.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.course-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.course-price {
  font-size: 26rpx;
  font-weight: bold;
  color: #ef4444;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.coach-name {
  font-size: 22rpx;
  color: #666666;
}

.course-level {
  font-size: 20rpx;
  color: #3cc51f;
  background-color: #e8f5e9;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.course-time {
  margin-bottom: 12rpx;
}

.time-text {
  font-size: 22rpx;
  color: #999999;
}

.course-stats {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.capacity {
  font-size: 20rpx;
  color: #999999;
}

.progress-bar {
  height: 8rpx;
  background-color: #f3f4f6;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #3cc51f;
  border-radius: 4rpx;
  transition: width 0.3s;
}

.popup-content {
  background-color: #ffffff;
  border-top-left-radius: 24rpx;
  border-top-right-radius: 24rpx;
  max-height: 80vh;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.popup-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.popup-close {
  font-size: 40rpx;
  color: #999999;
  width: 40rpx;
  text-align: center;
}

.popup-list {
  max-height: 60vh;
}

.popup-item {
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;
  font-size: 26rpx;
  color: #333333;
  
  &.active {
    color: #3cc51f;
    background-color: #f0f9f2;
  }
  
  &:last-child {
    border-bottom: none;
  }
}
</style>