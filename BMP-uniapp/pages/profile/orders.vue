<template>
  <MobileLayout>
    <view class="orders-page">
      <view class="topbar">
        <view class="topbar-inner">
          <view class="icon-btn" @click="handleBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">订单中心</text>
            <text class="topbar-sub">ORDER HUB</text>
          </view>
          <view class="icon-btn ghost" @click="reloadAll">
            <uni-icons type="reload" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
        <view class="hero-card">
          <view class="hero-glow" />
          <text class="hero-kicker">Unified Orders</text>
          <text class="hero-title">统一查看你的业务订单</text>
          <text class="hero-copy">场地预约、课程预约、赛事报名、器材租借和穿线服务都集中在这里。</text>
          <view class="hero-stats">
            <view class="stat-chip">
              <text class="stat-label">当前筛选</text>
              <text class="stat-value">{{ activeLifecycleLabel }}</text>
            </view>
            <view class="stat-chip">
              <text class="stat-label">订单数量</text>
              <text class="stat-value">{{ filteredOrders.length }}</text>
            </view>
          </view>
        </view>

        <scroll-view scroll-x class="filter-scroll" :show-scrollbar="false">
          <view class="filter-row">
            <view
              v-for="item in lifecycleTabs"
              :key="item.value"
              class="filter-pill"
              :class="{ active: activeLifecycle === item.value }"
              @click="activeLifecycle = item.value"
            >
              {{ item.label }}
            </view>
          </view>
        </scroll-view>

        <scroll-view scroll-x class="filter-scroll compact" :show-scrollbar="false">
          <view class="filter-row">
            <view
              v-for="item in businessTabs"
              :key="item.value"
              class="filter-pill light"
              :class="{ active: activeBusiness === item.value }"
              @click="activeBusiness = item.value"
            >
              {{ item.label }}
            </view>
          </view>
        </scroll-view>

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-title">加载订单中</text>
          <text class="state-desc">正在聚合各业务模块的订单记录。</text>
        </view>

        <view v-else-if="loadError" class="state-card">
          <text class="state-title">加载失败</text>
          <text class="state-desc">{{ loadError }}</text>
          <view class="retry-btn" @click="reloadAll">重新加载</view>
        </view>

        <view v-else-if="filteredOrders.length === 0" class="state-card">
          <text class="state-title">暂无订单</text>
          <text class="state-desc">当前筛选下还没有可展示的业务记录。</text>
        </view>

        <view v-else class="order-list">
          <view
            v-for="item in filteredOrders"
            :key="`${item.businessType}-${item.id}`"
            class="order-card"
            @click="openDetail(item.detailUrl)"
          >
            <view class="order-top">
              <view>
                <text class="order-type">{{ businessTextMap[item.businessType] }}</text>
                <text class="order-title">{{ item.title }}</text>
              </view>
              <text class="order-amount">{{ item.amountText }}</text>
            </view>

            <text class="order-subtitle">{{ item.subtitle }}</text>

            <view class="meta-row">
              <text class="meta-pill">{{ item.orderNo }}</text>
              <text class="meta-pill">{{ item.statusText }}</text>
              <text class="meta-pill">{{ item.paymentText }}</text>
            </view>

            <view class="order-footer">
              <text class="footer-time">{{ item.timeText || '时间待补充' }}</text>
              <view class="footer-link">
                <text>查看详情</text>
                <uni-icons type="right" size="14" color="#a33e00" />
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { useUserStore } from '@/store/modules/user'
import { useCurrentMember } from '@/composables/useCurrentMember'
import { getBookingList } from '@/api/booking'
import { getCourseBookingList } from '@/api/course'
import { getEquipmentRentalList } from '@/api/equipment'
import { getTournamentRegistrationList } from '@/api/tournament'
import { getStringingList } from '@/api/stringing'
import {
  filterUnifiedOrders,
  mapBookingOrder,
  mapCourseOrder,
  mapEquipmentOrder,
  mapStringingOrder,
  mapTournamentOrder,
  type OrderBusinessType,
  type UnifiedOrderItem
} from '@/utils/orderCenter'

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const loading = ref(false)
const loadError = ref('')
const orders = ref<UnifiedOrderItem[]>([])
const activeLifecycle = ref<'ALL' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'>('ALL')
const activeBusiness = ref<'ALL' | OrderBusinessType>('ALL')

const lifecycleTabs = [
  { label: '全部', value: 'ALL' },
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
] as const

const businessTabs = [
  { label: '全部业务', value: 'ALL' },
  { label: '场地预约', value: 'BOOKING' },
  { label: '课程预约', value: 'COURSE' },
  { label: '赛事报名', value: 'TOURNAMENT' },
  { label: '器材租借', value: 'EQUIPMENT' },
  { label: '穿线服务', value: 'STRINGING' }
] as const

const businessTextMap: Record<OrderBusinessType, string> = {
  BOOKING: '场地预约',
  COURSE: '课程预约',
  TOURNAMENT: '赛事报名',
  EQUIPMENT: '器材租借',
  STRINGING: '穿线服务'
}

const activeLifecycleLabel = computed(() => lifecycleTabs.find((item) => item.value === activeLifecycle.value)?.label || '全部')
const filteredOrders = computed(() => filterUnifiedOrders(orders.value, activeLifecycle.value, activeBusiness.value))

type PagedListResult<T> = {
  data?: T[]
  total?: number
  page?: number
  size?: number
  pages?: number
}

async function fetchAllPages<T>(
  loader: (params: { memberId: number; page: number; size: number }) => Promise<PagedListResult<T>>,
  memberId: number
) {
  const size = 50
  const firstPage = await loader({ memberId, page: 1, size })
  const items = [...(firstPage.data || [])]
  const pages = Math.max(1, Number(firstPage.pages || 1))

  if (pages <= 1) {
    return items
  }

  const restPages = await Promise.all(
    Array.from({ length: pages - 1 }, (_, index) => loader({ memberId, page: index + 2, size }))
  )

  restPages.forEach((pageResult) => {
    items.push(...(pageResult.data || []))
  })

  return items
}

const reloadAll = async () => {
  if (loading.value) return
  loading.value = true
  loadError.value = ''
  try {
    const member = await fetchCurrentMember(true)
    const memberId = Number(member.id || 0)
    const [bookingList, courseList, equipmentList, tournamentList, stringingList] = await Promise.all([
      fetchAllPages(getBookingList, memberId),
      fetchAllPages(getCourseBookingList, memberId),
      fetchAllPages(getEquipmentRentalList, memberId),
      fetchAllPages(getTournamentRegistrationList, memberId),
      fetchAllPages(getStringingList, memberId)
    ])

    orders.value = [
      ...bookingList.map(mapBookingOrder),
      ...courseList.map(mapCourseOrder),
      ...equipmentList.map(mapEquipmentOrder),
      ...tournamentList.map(mapTournamentOrder),
      ...stringingList.map(mapStringingOrder)
    ].sort((a, b) => String(b.timeText).localeCompare(String(a.timeText)))
  } catch (error) {
    console.error('加载订单中心失败:', error)
    loadError.value = error instanceof Error ? error.message : '订单中心加载失败，请稍后重试'
    uni.showToast({
      title: '加载订单中心失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const openDetail = (url: string) => {
  uni.navigateTo({ url })
}

const handleBack = () => {
  safeNavigateBack('/pages/profile/index')
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  await reloadAll()
})
</script>

<style lang="scss" scoped>
.orders-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%),
    #f9f9f9;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(249, 249, 249, 0.82);
  backdrop-filter: blur(20rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);

  &.ghost {
    background: rgba(255, 241, 234, 0.9);
  }
}

.topbar-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.topbar-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.topbar-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.page-scroll {
  height: calc(100vh - 196rpx);
  padding: 0 24rpx 40rpx;
  box-sizing: border-box;
}

.hero-card {
  position: relative;
  margin-top: 12rpx;
  padding: 34rpx 28rpx;
  border-radius: 36rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 18rpx 40rpx rgba(163, 62, 0, 0.2);
}

.hero-glow {
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(36rpx);
}

.hero-kicker,
.hero-title,
.hero-copy,
.hero-stats {
  position: relative;
  z-index: 1;
}

.hero-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 3rpx;
  text-transform: uppercase;
}

.hero-title {
  display: block;
  margin-top: 10rpx;
  font-size: 44rpx;
  font-weight: 900;
  color: #ffffff;
}

.hero-copy {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 24rpx;
}

.stat-chip {
  padding: 18rpx 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.14);
}

.stat-label {
  display: block;
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.72);
}

.stat-value {
  display: block;
  margin-top: 8rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #ffffff;
}

.filter-scroll {
  margin-top: 20rpx;
  white-space: nowrap;

  &.compact {
    margin-top: 14rpx;
  }
}

.filter-row {
  display: inline-flex;
  gap: 14rpx;
  padding-right: 24rpx;
}

.filter-pill {
  min-width: 148rpx;
  padding: 16rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.92);
  text-align: center;
  font-size: 24rpx;
  font-weight: 800;
  color: #1a1c1c;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.05);

  &.light {
    min-width: 168rpx;
    color: #6b625c;
  }

  &.active {
    background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
    color: #ffffff;
  }
}

.state-card,
.order-card {
  margin-top: 22rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.state-card {
  padding: 72rpx 36rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.spinner {
  width: 54rpx;
  height: 54rpx;
  border-radius: 999rpx;
  border: 5rpx solid #ece8e6;
  border-top-color: #ff6600;
  animation: spin 0.8s linear infinite;
}

.state-title {
  margin-top: 18rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.state-desc {
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b625c;
}

.retry-btn {
  margin-top: 20rpx;
  min-width: 220rpx;
  padding: 18rpx 24rpx;
  border-radius: 20rpx;
  background: #fff1e8;
  color: #a33e00;
  text-align: center;
  font-size: 24rpx;
  font-weight: 800;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding-bottom: 20rpx;
}

.order-card {
  padding: 26rpx 24rpx;
}

.order-top,
.order-footer,
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.order-type {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.order-title {
  display: block;
  margin-top: 10rpx;
  font-size: 32rpx;
  line-height: 1.3;
  font-weight: 900;
  color: #1a1c1c;
}

.order-amount {
  font-size: 28rpx;
  font-weight: 900;
  color: #a33e00;
}

.order-subtitle {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b625c;
}

.meta-row {
  flex-wrap: wrap;
  justify-content: flex-start;
  margin-top: 18rpx;
}

.meta-pill {
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: #faf8f6;
  font-size: 20rpx;
  color: #6b625c;
  font-weight: 700;
}

.order-footer {
  margin-top: 18rpx;
}

.footer-time {
  font-size: 20rpx;
  color: #8e7164;
}

.footer-link {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  color: #a33e00;
  font-size: 22rpx;
  font-weight: 800;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
