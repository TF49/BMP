<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="header-inner">
        <view class="nav-left" @tap="handleBack">
          <view class="icon-btn">
            <uni-icons type="left" size="22" color="#ff6600" />
          </view>
          <view>
            <text class="eyebrow">ELITE STAFF</text>
            <text class="nav-title">教练列表</text>
          </view>
        </view>
      </view>

      <view class="search-panel">
        <view class="search-box">
          <uni-icons type="search" size="18" color="#94a3b8" />
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="搜索教练姓名、专长、场馆"
            confirm-type="search"
            @confirm="reloadList"
          />
        </view>
        <view class="tabs">
          <view class="tab" :class="{ active: statusFilter === -1 }" @tap="setStatus(-1)">全部</view>
          <view class="tab" :class="{ active: statusFilter === 1 }" @tap="setStatus(1)">在岗</view>
          <view class="tab" :class="{ active: statusFilter === 0 }" @tap="setStatus(0)">暂停</view>
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      @scrolltolower="loadMore"
    >
      <view class="content">
        <view v-if="loading && coachList.length === 0" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载教练列表...</text>
        </view>

        <view v-else-if="errorText && coachList.length === 0" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="reloadList">重新加载</view>
        </view>

        <view v-else-if="coachList.length === 0" class="state-card">
          <text class="state-text">暂无符合条件的教练</text>
        </view>

        <view v-else class="list">
          <view
            v-for="item in coachList"
            :key="item.id"
            class="coach-card"
            hover-class="coach-card-active"
            @tap="goDetail(item.id)"
          >
            <image class="avatar" :src="item.avatar" mode="aspectFill" />
            <view class="card-main">
              <view class="card-head">
                <text class="coach-name">{{ item.name }}</text>
                <view class="rating-pill">
                  <uni-icons type="star-filled" size="12" color="#ea580c" />
                  <text>{{ item.ratingText }}</text>
                </view>
              </view>
              <text class="venue">{{ item.venueName || '未关联场馆' }}</text>
              <view class="tag-wrap">
                <text v-for="tag in item.tags" :key="`${item.id}-${tag}`" class="tag">{{ tag }}</text>
              </view>
              <view class="meta-row">
                <text class="price">¥{{ item.priceText }}/小时</text>
                <text class="students">{{ item.totalStudents }} 名学员</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#cbd5e1" />
          </view>

          <view v-if="loading" class="load-more">加载中...</view>
          <view v-else-if="hasMore" class="load-more" @tap="loadMore">加载更多</view>
          <view v-else class="load-more muted">已显示全部教练</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getCoachList, type CoachDto } from '@/api/coach'
import { parsePagedList } from '@/utils/parsePagedList'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useUserStore } from '@/store/modules/user'

type CoachCard = {
  id: number
  name: string
  avatar: string
  venueName: string
  tags: string[]
  totalStudents: number
  ratingText: string
  priceText: string
}

const userStore = useUserStore()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 164)
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const errorText = ref('')
const keyword = ref('')
const statusFilter = ref(-1)
const coachList = ref<CoachCard[]>([])

const hasMore = computed(() => coachList.value.length < total.value)

function splitTags(value?: string, totalStudents = 0) {
  const tags = (value || '')
    .split(/[，,、/|；;\s]+/)
    .map((item) => item.trim())
    .filter(Boolean)
  if (tags.length >= 2) return tags.slice(0, 3)
  if (tags.length === 1) return [tags[0], `${totalStudents}名学员`]
  return ['专业教练', `${totalStudents}名学员`]
}

function mapCoach(item: CoachDto): CoachCard {
  const totalStudents = Number(item.totalStudents || 0)
  return {
    id: Number(item.id || 0),
    name: item.coachName || '未命名教练',
    avatar: resolveImageUrl(item.avatar) || '/static/placeholders/avatar.svg',
    venueName: item.venueName || '',
    tags: splitTags(item.specialty, totalStudents),
    totalStudents,
    ratingText: item.rating != null ? Number(item.rating).toFixed(1) : '暂无',
    priceText: item.hourlyPrice != null ? Number(item.hourlyPrice).toFixed(2) : '0.00'
  }
}

async function fetchList(reset = false) {
  if (loading.value) return
  loading.value = true
  errorText.value = ''

  if (reset) {
    page.value = 1
  }

  try {
    const result = await getCoachList({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      status: statusFilter.value >= 0 ? statusFilter.value : undefined
    })
    const parsed = parsePagedList<CoachDto>(result)
    const mapped = parsed.list.map(mapCoach).filter((item) => item.id > 0)
    total.value = parsed.total
    coachList.value = reset ? mapped : coachList.value.concat(mapped)
  } catch (error) {
    if (coachList.value.length === 0) {
      errorText.value = error instanceof Error ? error.message : '加载教练列表失败'
    }
  } finally {
    loading.value = false
  }
}

function reloadList() {
  void fetchList(true)
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  page.value += 1
  void fetchList()
}

function setStatus(status: number) {
  if (statusFilter.value === status) return
  statusFilter.value = status
  reloadList()
}

function handleBack() {
  safeNavigateBack('/pages/course/list')
}

function goDetail(id: number) {
  if (!id) return
  uni.navigateTo({
    url: `/pages/coach/detail?id=${id}`
  })
}

onLoad(() => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  reloadList()
})

onPullDownRefresh(() => {
  fetchList(true).finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #111111;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.96);
  backdrop-filter: blur(18px);
}

.header-inner {
  min-height: 96rpx;
  padding: 10rpx 26rpx 12rpx;
  display: flex;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.eyebrow {
  display: block;
  font-size: 20rpx;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: 2rpx;
}

.nav-title {
  display: block;
  margin-top: 4rpx;
  font-size: 42rpx;
  line-height: 1.1;
  font-weight: 900;
}

.search-panel {
  padding: 0 26rpx 20rpx;
}

.search-box {
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 22rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  gap: 14rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.04);
}

.search-input {
  flex: 1;
  height: 88rpx;
  font-size: 26rpx;
  color: #111111;
}

.tabs {
  margin-top: 16rpx;
  padding: 8rpx;
  border-radius: 20rpx;
  background: #ececec;
  display: flex;
  gap: 8rpx;
}

.tab {
  flex: 1;
  height: 56rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
  color: #666666;
}

.tab.active {
  background: #ffffff;
  color: #a33e00;
  box-shadow: 0 4rpx 12rpx rgba(15, 23, 42, 0.06);
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 22rpx 26rpx 64rpx;
}

.state-card {
  min-height: 520rpx;
  border-radius: 28rpx;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  box-shadow: 0 12rpx 36rpx rgba(15, 23, 42, 0.04);
}

.state-text {
  font-size: 28rpx;
  color: #777777;
}

.state-action {
  min-width: 220rpx;
  height: 76rpx;
  padding: 0 28rpx;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.coach-card {
  min-height: 170rpx;
  padding: 24rpx;
  border-radius: 26rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.04);
}

.coach-card-active {
  transform: scale(0.985);
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 24rpx;
  background: #f3f3f3;
  flex-shrink: 0;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.coach-name {
  font-size: 32rpx;
  line-height: 1.3;
  font-weight: 900;
  color: #111111;
}

.rating-pill {
  flex-shrink: 0;
  height: 40rpx;
  padding: 0 12rpx;
  border-radius: 9999rpx;
  background: #fff7ed;
  color: #c2410c;
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 20rpx;
  font-weight: 800;
}

.venue {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #666666;
}

.tag-wrap {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag {
  padding: 6rpx 12rpx;
  border-radius: 9999rpx;
  background: #fff3eb;
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 800;
}

.meta-row {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.price {
  font-size: 24rpx;
  font-weight: 900;
  color: #111111;
}

.students {
  font-size: 22rpx;
  color: #888888;
}

.load-more {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a33e00;
  font-size: 24rpx;
  font-weight: 800;
}

.load-more.muted {
  color: #9ca3af;
}
</style>
