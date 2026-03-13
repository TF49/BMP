<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-top">
        <view class="location-info" @click="handleLocationClick">
          <uni-icons type="location" size="18" color="#3cc51f"></uni-icons>
          <text class="location-text">杭州市体育中心</text>
          <uni-icons type="arrowdown" size="14" color="#475569"></uni-icons>
        </view>
        <view class="header-actions">
          <view class="action-icon" @click="handleMessageClick">
            <uni-icons type="chatbubble" size="20" color="#475569"></uni-icons>
            <view class="badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
          </view>
        </view>
      </view>
      <view class="search-box" @click="handleSearchClick">
        <uni-icons type="search" size="18" color="#475569"></uni-icons>
        <text class="search-placeholder">搜索场馆、教练或赛事</text>
      </view>
    </view>

    <view class="content">
      <!-- Banner -->
      <view class="banner" @click="handleBannerClick">
        <view class="banner-bg"></view>
        <view class="banner-content">
          <view class="banner-tag-row">
            <text class="banner-tag">羽毛球服务</text>
            <text class="banner-tag-secondary">一站式平台</text>
          </view>
          <text class="banner-title">{{ bannerData?.title || '羽毛球场馆与赛事服务平台' }}</text>
          <text class="banner-desc">{{ bannerData?.desc || '赛事报名 · 场地预订 · 课程与会员服务一站式办理' }}</text>
          <button class="banner-btn">立即进入</button>
        </view>
      </view>

      <!-- Grid Navigation：协会主功能 -->
      <view class="section section-no-padding">
        <view class="grid-nav">
          <view 
            v-for="(service, index) in services" 
            :key="index"
            class="grid-item"
            @click="handleServiceClick(service.path)"
          >
            <view class="grid-icon" :class="service.bgClass">
              <uni-icons :type="service.icon" :size="28" :color="service.iconColor"></uni-icons>
            </view>
            <text class="grid-text">{{ service.name }}</text>
          </view>
        </view>
      </view>

      <!-- Feature Cards：协会特色模块 -->
      <view class="section feature-section">
        <view class="feature-grid">
          <view
            v-for="(item, index) in featureCards"
            :key="index"
            class="feature-card"
            :class="item.bgClass"
            @click="handleFeatureClick(item)"
          >
            <view class="feature-icon">
              <uni-icons :type="item.icon" size="22" color="#ffffff"></uni-icons>
            </view>
            <view class="feature-texts">
              <text class="feature-title">{{ item.title }}</text>
              <text class="feature-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Tournament Section：赛事 -->
      <view class="section" v-if="upcomingTournaments.length > 0">
        <view class="section-header">
          <text class="section-title">赛事</text>
          <view class="section-more" @click="() => uni.switchTab({ url: '/pages/tournament/list' })">
            <text>更多</text>
            <text class="chevron">›</text>
          </view>
        </view>
        <scroll-view class="horizontal-scroll" scroll-x="true" show-scrollbar="false">
          <view
            v-for="(item, index) in upcomingTournaments"
            :key="index"
            class="horizontal-card tournament-card"
            @click="handleTournamentClick(item)"
          >
            <text class="horizontal-title">{{ item.tournamentName }}</text>
            <text class="horizontal-sub">报名截止：{{ item.registrationEnd }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- Recommended Venues -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">场馆</text>
          <view class="section-more" @click="handleViewAll">
            <text>更多</text>
            <text class="chevron">›</text>
          </view>
        </view>
        <scroll-view class="horizontal-scroll" scroll-x="true" show-scrollbar="false" v-if="recommendedVenues.length > 0">
          <view 
            v-for="(venue, index) in recommendedVenues" 
            :key="index"
            class="horizontal-card venue-card"
            @click="handleVenueClick(venue)"
          >
            <view class="venue-image">
              <image
                v-if="venue.imageUrl"
                :src="venue.imageUrl"
                mode="aspectFill"
                class="venue-img"
              />
              <view v-else class="image-placeholder">
                <uni-icons type="image" size="26" color="#94a3b8"></uni-icons>
              </view>
            </view>
            <view class="venue-info">
              <text class="venue-name">{{ venue.name }}</text>
              <view class="venue-location">
                <uni-icons type="location" size="14" color="#475569"></uni-icons>
                <text>{{ venue.location }}</text>
              </view>
              <view class="venue-tags">
                <text 
                  v-for="(tag, tagIndex) in venue.tags" 
                  :key="tagIndex"
                  class="venue-tag"
                  :class="tag.class"
                >
                  {{ tag.text }}
                </text>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="empty-recommend" v-else>
          <uni-icons type="info" size="48" color="#94a3b8"></uni-icons>
          <text class="empty-text">暂无推荐场馆</text>
          <text class="empty-desc">快去发现更多精彩内容吧</text>
        </view>
      </view>

      <!-- Hot Courses -->
      <view class="section" v-if="hotCourses.length > 0">
        <view class="section-header">
          <text class="section-title">热门课程</text>
          <view class="section-more" @click="() => uni.switchTab({ url: '/pages/course/list' })">
            <text>全部</text>
            <text class="chevron">›</text>
          </view>
        </view>

        <view class="course-list">
          <view
            v-for="(course, index) in hotCourses"
            :key="index"
            class="course-card"
            @click="handleCourseClick(course)"
          >
            <view class="course-header">
              <text class="course-name">{{ course.name }}</text>
              <text class="course-price">¥{{ course.price }}</text>
            </view>
            <view class="course-info">
              <view class="course-meta">
                <view class="meta-item">
                  <uni-icons type="person" size="14" color="#475569"></uni-icons>
                  <text>{{ course.coach }}</text>
                </view>
                <view class="meta-item">
                  <uni-icons type="calendar" size="14" color="#475569"></uni-icons>
                  <text>{{ course.date }}</text>
                </view>
              </view>
              <view class="course-meta">
                <view class="meta-item">
                  <uni-icons type="calendar" size="14" color="#475569"></uni-icons>
                  <text>{{ course.time }}</text>
                </view>
                <view class="meta-item">
                  <uni-icons type="person" size="14" color="#475569"></uni-icons>
                  <text>{{ course.students }}/{{ course.maxStudents }}人</text>
                </view>
              </view>
            </view>
            <view class="course-progress">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: (course.students / course.maxStudents * 100) + '%' }"
                ></view>
              </view>
              <text class="progress-text">剩余{{ course.maxStudents - course.students }}个名额</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getVenueList, getVenueImages } from '@/api/venue'
import { getCourseList } from '@/api/course'
import { getTournamentList } from '@/api/tournament'
import { IMAGE_BASE_URL } from '@/config/api'
import { safeReLaunch } from '@/utils/safeRoute'

const searchKeyword = ref('')
const userStore = useUserStore()
const statusBarHeight = ref(0)
const unreadCount = ref(0)

const services = [
  { name: '赛事报名', icon: 'medal', iconColor: '#22c55e', bgClass: 'bg-green', path: '/pages/tournament/list' },
  { name: '场地预订', icon: 'calendar', iconColor: '#06b6d4', bgClass: 'bg-blue-soft', path: '/pages/booking/list' },
  { name: '课程预约', icon: 'calendar', iconColor: '#6366f1', bgClass: 'bg-purple', path: '/pages/course/list' },
  { name: '场馆列表', icon: 'home', iconColor: '#2196f3', bgClass: 'bg-blue', path: '/pages/venue/list' },
  { name: '器材租借', icon: 'bag', iconColor: '#f97316', bgClass: 'bg-orange', path: '/pages/equipment/list' },
  { name: '穿线服务', icon: 'tools', iconColor: '#0ea5e9', bgClass: 'bg-sky', path: '/pages/stringing/list' },
  { name: '充值中心', icon: 'wallet', iconColor: '#4caf50', bgClass: 'bg-green', path: '/pages/recharge/index' },
  { name: '会员中心', icon: 'person', iconColor: '#f59e0b', bgClass: 'bg-yellow', path: '/pages/profile/index' }
]

const featureCards = [
  {
    title: '我的预约',
    desc: '查看和管理场地预约',
    icon: 'document',
    bgClass: 'feature-purple',
    path: '/pages/booking/list'
  },
  {
    title: '充值记录',
    desc: '账户充值流水一目了然',
    icon: 'wallet',
    bgClass: 'feature-green',
    path: '/pages/recharge/records'
  },
  {
    title: '穿线进度',
    desc: '查询球拍穿线服务状态',
    icon: 'search',
    bgClass: 'feature-orange',
    path: '/pages/stringing/query'
  },
  {
    title: '会员信息',
    desc: '会员等级与权益总览',
    icon: 'person',
    bgClass: 'feature-yellow',
    path: '/pages/profile/member'
  },
  {
    title: '帮助与反馈',
    desc: '常见问题与意见反馈',
    icon: 'help',
    bgClass: 'feature-blue',
    path: '/pages/settings/help'
  }
]

const recommendedVenues = ref<any[]>([])
const hotCourses = ref<any[]>([])
const upcomingTournaments = ref<any[]>([])
const bannerData = ref<any>(null)

// 加载推荐场馆数据
const loadRecommendedVenues = async () => {
  try {
    const result = await getVenueList({
      page: 1,
      size: 5,
      status: 1 // 只获取营业中的场馆
    })

    if (result && result.data && Array.isArray(result.data)) {
      // 为每个场馆加载图片
      const venuesWithImages = await Promise.all(
        result.data.map(async (venue: any) => {
          let imageUrl = ''
          try {
            const images = await getVenueImages(venue.id)
            if (images && images.length > 0) {
              imageUrl = `${IMAGE_BASE_URL}${images[0].imageUrl}`
            }
          } catch (err) {
            console.error(`加载场馆${venue.id}图片失败:`, err)
          }

          return {
            id: venue.id,
            name: venue.venueName,
            location: venue.address,
            distance: '1.2km',
            price: 50,
            imageUrl,
            tags: [
              { text: '空调', class: 'tag-green' },
              { text: '停车', class: 'tag-orange' }
            ]
          }
        })
      )

      recommendedVenues.value = venuesWithImages
      uni.setStorageSync('venues_cache', venuesWithImages)
    } else {
      showEmptyRecommendedVenues()
    }
  } catch (error) {
    console.error('加载推荐场馆失败:', error)
    const cached = uni.getStorageSync('venues_cache')
    if (cached && Array.isArray(cached)) {
      recommendedVenues.value = cached
    } else {
      showEmptyRecommendedVenues()
    }
  }
}

// 加载热门课程
const loadHotCourses = async () => {
  try {
    const result = await getCourseList({
      page: 1,
      size: 3,
      status: 1
    })

    if (result && result.data && Array.isArray(result.data)) {
      hotCourses.value = result.data.map((course: any) => ({
        id: course.id,
        name: course.courseName,
        coach: course.coachName,
        price: course.coursePrice,
        students: course.currentStudents,
        maxStudents: course.maxStudents,
        date: course.courseDate,
        time: `${course.startTime}-${course.endTime}`
      }))
      uni.setStorageSync('courses_cache', hotCourses.value)
    }
  } catch (error) {
    console.error('加载热门课程失败:', error)
    const cached = uni.getStorageSync('courses_cache')
    if (cached && Array.isArray(cached)) {
      hotCourses.value = cached
    }
  }
}

// 加载即将开始的赛事
const loadUpcomingTournaments = async () => {
  try {
    const result = await getTournamentList({
      page: 1,
      size: 5,
      status: 1
    })

    if (result && result.data && Array.isArray(result.data) && result.data.length > 0) {
      const tournament = result.data[0]
      bannerData.value = {
        id: tournament.id,
        title: tournament.tournamentName,
        desc: `报名截止：${tournament.registrationEnd}`,
        type: tournament.tournamentType
      }
      upcomingTournaments.value = result.data
    }
  } catch (error) {
    console.error('加载赛事失败:', error)
  }
}

// 显示"暂无推荐"提示
const showEmptyRecommendedVenues = () => {
  recommendedVenues.value = []
  uni.showToast({
    title: '暂无推荐场馆，请检查网络',
    icon: 'none',
    duration: 2000
  })
}

const handleFeatureClick = (item: any) => {
  if (!item || !item.path || item.path === '#') {
    uni.showToast({
      title: '功能开发中，敬请期待',
      icon: 'none'
    })
    return
  }

  uni.navigateTo({
    url: item.path
  })
}

const handleTournamentClick = (item: any) => {
  if (!item || !item.id) return
  uni.navigateTo({
    url: `/pages/tournament/detail?id=${item.id}`
  })
}

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    uni.navigateTo({
      url: `/pages/search/result?keyword=${encodeURIComponent(searchKeyword.value)}`
    })
  }
}

// 点击搜索框
const handleSearchClick = () => {
  uni.navigateTo({
    url: '/pages/search/index'
  })
}

// 点击位置信息
const handleLocationClick = () => {
  uni.showToast({
    title: '定位功能开发中',
    icon: 'none'
  })
}

// 点击消息
const handleMessageClick = () => {
  uni.showToast({
    title: '消息功能开发中',
    icon: 'none'
  })
}

const handleBannerClick = () => {
  if (bannerData.value && bannerData.value.id) {
    uni.navigateTo({
      url: `/pages/tournament/detail?id=${bannerData.value.id}`
    })
  } else {
    uni.switchTab({
      url: '/pages/tournament/list'
    })
  }
}

const handleServiceClick = (path: string) => {
  if (path === '#') {
    uni.showToast({
      title: '功能开发中',
      icon: 'none'
    })
    return
  }

  // tabbar页面列表
  const tabbarPages = [
    '/pages/index/index',
    '/pages/venue/list',
    '/pages/course/list',
    '/pages/tournament/list',
    '/pages/profile/index'
  ]

  // 判断是否为tabbar页面
  if (tabbarPages.includes(path)) {
    uni.switchTab({
      url: path
    })
  } else {
    uni.navigateTo({
      url: path
    })
  }
}

const handleViewAll = () => {
  uni.switchTab({
    url: '/pages/venue/list'
  })
}

const handleVenueClick = (venue: any) => {
  uni.navigateTo({
    url: `/pages/venue/detail?id=${venue.id}`
  })
}

const handleCourseClick = (course: any) => {
  uni.navigateTo({
    url: `/pages/course/detail?id=${course.id}`
  })
}

// 页面加载时获取数据
onMounted(async () => {
  // 获取状态栏高度
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0

  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    // 未登录用户重定向到登录页
    safeReLaunch('/pages/login/login', '/pages/login/login')
    return
  }

  // 并行加载所有数据
  try {
    await Promise.all([
      loadRecommendedVenues(),
      loadHotCourses(),
      loadUpcomingTournaments()
    ])
  } catch (error) {
    console.error('加载首页数据失败:', error)
    // 继续显示页面，使用缓存数据
  }
})

// 启用下拉刷新
onPullDownRefresh(() => {
  Promise.all([
    loadRecommendedVenues(),
    loadHotCourses(),
    loadUpcomingTournaments()
  ]).finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

/* Header样式优化 */
.header {
  background-color: #ffffff;
  padding: 20rpx 28rpx 16rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex: 1;
}

.location-text {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-color;
  flex: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.action-icon {
  position: relative;
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: $muted-bg;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}
.action-icon:active {
  opacity: 0.85;
}

.badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background-color: #ff4444;
  color: #ffffff;
  font-size: 20rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #ffffff;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background-color: $muted-bg;
  border-radius: 48rpx;
  padding: 20rpx 24rpx;
  transition: background-color 0.2s ease;
}

.search-box:active {
  background-color: #E2E8F0;
}

.search-placeholder {
  flex: 1;
  font-size: 26rpx;
  color: $text-color-secondary;
}

.content {
  padding: 24rpx 28rpx;
  padding-bottom: 200rpx;
  background-color: $bg-color;
  min-height: calc(100vh - 200rpx);
}

.banner {
  width: calc(100% - 32rpx);
  height: 240rpx;
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  border-radius: 28rpx;
  padding: 40rpx 36rpx;
  margin: 0 16rpx 40rpx 16rpx;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8rpx 16rpx rgba(60, 197, 31, 0.25);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.banner-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 220rpx;
  height: 220rpx;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  transform: translate(60rpx, -60rpx);
}

.banner-content {
  position: relative;
  z-index: 10;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.banner-title {
  font-size: 36rpx;
  font-weight: 700;
  display: block;
  margin-bottom: 8rpx;
  letter-spacing: 0.5rpx;
}

.banner-desc {
  font-size: 22rpx;
  opacity: 0.9;
  display: block;
  margin-bottom: 20rpx;
  font-weight: 500;
}

.banner-btn {
  background-color: #ffffff;
  color: #3cc51f;
  font-size: 22rpx;
  font-weight: 700;
  padding: 14rpx 32rpx;
  border-radius: 9999rpx;
  border: none;
  align-self: flex-start;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.banner-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.section {
  margin-bottom: 40rpx;
}

.section-no-padding {
  margin-bottom: 28rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 0 4rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-color;
}

.section-more {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 20rpx;
  color: $text-color-secondary;
}

.chevron {
  font-size: 24rpx;
}

.grid-nav {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.grid-item {
  background-color: $bg-white;
  border-radius: 24rpx;
  padding: 36rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid $border-color;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  min-height: 180rpx;
}

.grid-item:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.grid-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.bg-blue {
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  }

  &.bg-orange {
    background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  }

  &.bg-purple {
    background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
  }

  &.bg-red {
    background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  }

  &.bg-green {
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  }

  &.bg-yellow {
    background: linear-gradient(135deg, #fffde7 0%, #fff9c4 100%);
  }

  &.bg-blue-soft {
    background: linear-gradient(135deg, #e0f2fe 0%, #bfdbfe 100%);
  }

  &.bg-pink {
    background: linear-gradient(135deg, #fce7f3 0%, #f9a8d4 100%);
  }

  &.bg-sky {
    background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  }
}

.grid-text {
  font-size: 26rpx;
  font-weight: 600;
  color: $text-color;
  text-align: center;
  line-height: 1.4;
  word-break: break-word;
}

.venue-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.horizontal-scroll {
  white-space: nowrap;
  padding: 8rpx 4rpx 4rpx;
}

.horizontal-card {
  display: inline-flex;
  flex-direction: column;
  width: 260rpx;
  padding: 20rpx;
  margin-right: 20rpx;
  border-radius: 20rpx;
  background-color: $bg-white;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid $border-color;
}

.tournament-card {
  justify-content: space-between;
  min-height: 150rpx;
}

.horizontal-title {
  font-size: 24rpx;
  font-weight: 600;
  color: $text-color;
  margin-bottom: 12rpx;
}

.horizontal-sub {
  font-size: 20rpx;
  color: $text-color-secondary;
}

.venue-card {
  background-color: $bg-white;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid $border-color;
  display: flex;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.venue-card:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.venue-image {
  width: 140rpx;
  height: 140rpx;
  background-color: $muted-bg;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.venue-img {
  width: 100%;
  height: 100%;
}

.venue-info {
  flex: 1;
  padding: 14rpx 16rpx 10rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.venue-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10rpx;
}

.venue-name {
  font-size: 24rpx;
  font-weight: bold;
  color: $text-color;
}

.venue-price {
  font-size: 24rpx;
  font-weight: bold;
  color: $error-color;
}

.price-unit {
  font-size: 20rpx;
  font-weight: normal;
  color: $text-color-secondary;
}

.venue-location {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 22rpx;
  color: $text-color-secondary;
  margin: 8rpx 0 10rpx;
}

.empty-recommend {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  background-color: $muted-bg;
  border-radius: 18rpx;
  margin-top: 16rpx;
  border: 1rpx solid $border-color;
}

.empty-text {
  font-size: 28rpx;
  color: $text-color;
  margin-top: 24rpx;
  font-weight: 500;
}

.empty-desc {
  font-size: 24rpx;
  color: $text-color-secondary;
  margin-top: 12rpx;
}

.venue-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 4rpx;
}

.venue-tag {
  font-size: 18rpx;
  padding: 6rpx 16rpx;
  border-radius: 6rpx;
  
  &.tag-green {
    background-color: #e8f5e9;
    color: #3cc51f;
  }
  
  &.tag-orange {
    background-color: #fff3e0;
    color: #ff9800;
  }
  
  &.tag-blue {
    background-color: #e3f2fd;
    color: #2196f3;
  }
}

// 热门课程样式
.course-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.course-card {
  background-color: $bg-white;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid $border-color;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.course-card:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.course-name {
  font-size: 26rpx;
  font-weight: bold;
  color: $text-color;
  flex: 1;
}

.course-price {
  font-size: 28rpx;
  font-weight: bold;
  color: $warning-color;
}

.course-info {
  margin-bottom: 16rpx;
}

.course-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 8rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 20rpx;
  color: $text-color-secondary;
}
.meta-item text {
  flex: 1;
}

.course-progress {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.progress-bar {
  flex: 1;
  height: 8rpx;
  background-color: $muted-bg;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff9800 0%, #ffc107 100%);
  border-radius: 4rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: 18rpx;
  color: $text-color-secondary;
  white-space: nowrap;
}

/* Feature cards */
.feature-section {
  margin-top: 8rpx;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.feature-card {
  min-height: 120rpx;
  border-radius: 22rpx;
  padding: 20rpx 22rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(15, 23, 42, 0.06);
}

.feature-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 999rpx;
  background-color: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.feature-texts {
  flex: 1;
}

.feature-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 4rpx;
}

.feature-desc {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.85);
}

.feature-purple {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
}

.feature-green {
  background: linear-gradient(135deg, #22c55e 0%, #4ade80 100%);
}

.feature-orange {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
}

.feature-yellow {
  background: linear-gradient(135deg, #facc15 0%, #f97316 100%);
}

.feature-blue {
  background: linear-gradient(135deg, #0ea5e9 0%, #6366f1 100%);
}

</style>
