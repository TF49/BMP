<template>
  <view class="notice-page bg-surface min-h-screen pb-24">
    <scroll-view scroll-y class="main-content" lower-threshold="120" @scrolltolower="loadMore">
      <!-- Top Navbar -->
      <view class="w-full bg-[#F8FAFC] flex justify-between items-center px-6 py-4 pb-4 sticky-nav" :style="{ paddingTop: (statusBarHeight || 44) + 'px' }">
        <view class="text-[#1a1a1a] transition-transform duration-200 w-10 h-10 flex items-center" hover-class="scale-95" @tap="goBack">
          <uni-icons type="left" size="22" color="#1a1a1a"></uni-icons>
        </view>
        <text class="font-bold text-lg text-[#1a1a1a]">通知中心</text>
        <view class="text-primary text-sm font-bold w-fit whitespace-nowrap transition-transform duration-200" :style="{ marginRight: navBarMarginRight + 'px' }" hover-class="scale-95" @tap="markAllAsRead">
          <text>全部已读</text>
        </view>
      </view>

      <view class="px-6 space-y-6 pt-6 pb-12">
        <!-- Title Area -->
        <view class="flex flex-col gap-1">
          <text class="text-primary text-xs-10 font-bold tracking-widest uppercase">收件箱动态</text>
          <text class="text-3xl font-black text-on-surface tracking-tight mt-1">时刻掌握最新动态</text>
        </view>

        <!-- Tabs（P0：后端无类型字段，仅保留“全部”避免误导） -->
        <view class="flex flex-row gap-3 mt-6">
          <view class="tab-pill active">全部</view>
        </view>

        <!-- Notice List -->
        <view class="flex flex-col gap-4 mt-6">
          <view v-if="loading && list.length === 0" class="py-8 flex justify-center">
            <text class="text-xs text-secondary opacity-60 tracking-wider">加载中...</text>
          </view>

          <view v-else-if="!loading && list.length === 0" class="py-8 flex justify-center">
            <text class="text-xs text-secondary opacity-60 tracking-wider">暂无通知</text>
          </view>

          <view
            v-for="item in list"
            :key="item.id"
            class="notice-card shadow-sm flex flex-row p-5 rounded-2xl bg-white relative overflow-hidden active-scale"
            :class="{ 'border-left-primary': !isRead(item.id) }"
            @tap="openDetail(item)"
          >
            <view class="icon-box" :class="!isRead(item.id) ? 'bg-orange-50 text-orange-600' : 'bg-gray-100 text-gray-500'">
              <uni-icons type="chatbubble" size="20" :color="!isRead(item.id) ? '#ea580c' : '#64748b'"></uni-icons>
              <view v-if="!isRead(item.id)" class="red-dot"></view>
            </view>
            <view class="flex flex-col flex-1 pl-4">
              <view class="flex justify-between items-center">
                <text class="font-bold text-on-surface text-base">{{ item.title }}</text>
                <text class="text-xs text-secondary opacity-70">{{ formatTime(item.createTime) }}</text>
              </view>
              <text class="text-sm text-secondary mt-2 leading-relaxed">
                {{ item.content }}
              </text>
            </view>
          </view>

        </view>

        <!-- Bottom Load More -->
        <view class="py-8 flex justify-center">
          <text class="text-xs text-secondary opacity-60 tracking-wider">{{ hasMore ? (loading ? '加载中...' : '上拉加载更多') : '没有更多了' }}</text>
        </view>
        
      </view>
      
      <!-- Safe Area Spacer -->
      <view class="h-10"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getNotificationList, type NotificationItem } from '@/api/notification'
import { safeNavigateBack } from '@/utils/navigation'

const statusBarHeight = ref(44)
const navBarMarginRight = ref(0) // right margin to avoid capsule
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const list = ref<NotificationItem[]>([])

const READ_IDS_KEY = 'notice_read_ids_v1'
const readIds = ref<Set<number>>(new Set())

function loadReadIds() {
  try {
    const raw = uni.getStorageSync(READ_IDS_KEY)
    const arr = Array.isArray(raw) ? raw : []
    readIds.value = new Set(arr.map((x: any) => Number(x)).filter((n: any) => Number.isFinite(n)))
  } catch (e) {
    readIds.value = new Set()
  }
}

function persistReadIds() {
  try {
    uni.setStorageSync(READ_IDS_KEY, Array.from(readIds.value))
  } catch (e) {
    // ignore
  }
}

function isRead(id: number) {
  return readIds.value.has(Number(id))
}

function markRead(id: number) {
  readIds.value.add(Number(id))
  persistReadIds()
}

const hasMore = computed(() => list.value.length < total.value)

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  // #ifdef MP
  try {
    const menuInfo = uni.getMenuButtonBoundingClientRect()
    if (menuInfo) {
      // menuInfo.left is the left edge of the capsule
      // Calculate distance to the right screen edge
      // Subtract 24px because the header has px-6 padding
      // Add roughly 8px buffer
      navBarMarginRight.value = Math.max(0, systemInfo.windowWidth - menuInfo.left - 24 + 8) 
    }
  } catch (e) {}
  // #endif

  loadReadIds()
  loadList(false)
})

const goBack = () => {
  safeNavigateBack('/pages/index/index')
}

function formatTime(createTime?: string) {
  return createTime ? String(createTime).slice(0, 16) : ''
}

const markAllAsRead = () => {
  list.value.forEach((n) => {
    if (n?.id != null) readIds.value.add(Number(n.id))
  })
  persistReadIds()
  uni.showToast({
    title: '已全部标为已读',
    icon: 'success'
  })
}

async function loadList(append: boolean) {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getNotificationList({ page: page.value, size: size.value })
    total.value = res.total || 0
    if (append) {
      list.value = list.value.concat(res.data || [])
    } else {
      list.value = res.data || []
    }
  } catch (e) {
    console.error('加载通知失败:', e)
    uni.showToast({ title: '加载通知失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value) return
  if (!hasMore.value) return
  page.value += 1
  loadList(true)
}

function openDetail(item: NotificationItem) {
  if (item?.id != null) {
    markRead(item.id)
  }
  // P0：先仅弹窗查看内容，避免新增详情页路由
  uni.showModal({
    title: item.title || '通知',
    content: item.content || '',
    showCancel: false,
    confirmText: '我知道了'
  })
}

onPullDownRefresh(() => {
  page.value = 1
  loadList(false).finally(() => uni.stopPullDownRefresh())
})
</script>

<style lang="scss" scoped>
.notice-page {
  font-family: 'Lexend', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* Colors */
.text-on-surface { color: #1a1a1a; }
.bg-surface { background-color: #F8FAFC; }
.text-secondary { color: #64748b; }
.text-primary { color: #ff6600; }
.text-white { color: #ffffff; }
.text-black { color: #000000; }
.bg-white { background-color: #ffffff; }
.bg-gray-900 { background-color: #111827; }
.bg-gray-100 { background-color: #f1f5f9; }
.text-gray-500 { color: #64748b; }
.bg-orange-50 { background-color: #fff7ed; }
.text-orange-600 { color: #ea580c; }

/* Utilities */
.flex { display: flex; }
.flex-col { flex-direction: column; }
.flex-row { flex-direction: row; }
.flex-1 { flex: 1; }
.justify-between { justify-content: space-between; }
.justify-center { justify-content: center; }
.items-center { align-items: center; }
.w-full { width: 100%; }
.h-full { height: 100%; }
.min-h-screen { min-height: 100vh; }
.relative { position: relative; }
.absolute { position: absolute; }
.z-10 { z-index: 10; }
.z-20 { z-index: 20; }
.overflow-hidden { overflow: hidden; }
.inline-flex { display: inline-flex; }
.whitespace-nowrap { white-space: nowrap; }

/* Insets and sizes */
.w-10 { width: 40px; }
.h-10 { height: 40px; }
.w-12 { width: 48px; }
.w-fit { width: fit-content; }
.w-half { width: 50%; }
.w-3-quarters { width: 75%; }
.w-4-fifths { width: 80%; }
.h-10 { height: 40px; }
.px-6 { padding-left: 24px; padding-right: 24px; }
.py-4 { padding-top: 16px; padding-bottom: 16px; }
.pb-4 { padding-bottom: 16px; }
.pb-12 { padding-bottom: 48px; }
.pb-24 { padding-bottom: 96px; }
.pt-6 { padding-top: 24px; }
.py-8 { padding-top: 32px; padding-bottom: 32px; }
.py-2 { padding-top: 8px; padding-bottom: 8px; }
.px-5 { padding-left: 20px; padding-right: 20px; }
.p-5 { padding: 20px; }
.p-6 { padding: 24px; }
.pl-4 { padding-left: 16px; }

/* Spacing */
.mt-1 { margin-top: 4px; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 16px; }
.mt-6 { margin-top: 24px; }
.gap-1 { gap: 4px; }
.gap-3 { gap: 12px; }
.gap-4 { gap: 16px; }

/* Typography */
.font-bold { font-weight: 700; }
.font-black { font-weight: 900; }
.text-3xl { font-size: 28px; line-height: 1.2; }
.text-2xl { font-size: 24px; }
.text-xl { font-size: 20px; line-height: 1.4; }
.text-lg { font-size: 18px; }
.text-base { font-size: 16px; }
.text-sm { font-size: 14px; }
.text-xs { font-size: 12px; }
.text-xs-10 { font-size: 10px; }
.tracking-tight { letter-spacing: -0.02em; }
.tracking-widest { letter-spacing: 0.1em; }
.tracking-wider { letter-spacing: 0.05em; }
.uppercase { text-transform: uppercase; }
.text-right { text-align: right; }
.leading-relaxed { line-height: 1.6; }
.leading-snug { line-height: 1.375; }

/* Decorations */
.rounded-2xl { border-radius: 16px; }
.rounded-full { border-radius: 9999px; }
.shadow-sm { box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03); }
.shadow-md { box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1); }
.opacity-70 { opacity: 0.7; }
.opacity-60 { opacity: 0.6; }
.opacity-40 { opacity: 0.4; }
.object-cover { object-fit: cover; }

.bg-gradient-to-l {
  background: linear-gradient(to left, transparent, #111827);
}

.sticky-nav {
  position: sticky;
  top: 0;
  z-index: 50;
}

/* Custom components */
.tab-pill {
  padding: 6px 16px;
  border-radius: 99px;
  background-color: #e2e8f0;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  
  &.active {
    background-color: #ea580c;
    color: white;
  }
}

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-lg {
  font-size: 24px;
}

.red-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background-color: #ea580c;
  border-radius: 50%;
  border: 2px solid #fff7ed;
}

.border-left-primary::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 60%;
  width: 4px;
  background-color: #a33e00;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}

.premium-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.premium-bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}

.premium-bg-circle-1 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, #ea580c 0%, transparent 70%);
  top: -40px;
  right: -30px;
}

.premium-bg-circle-2 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, #ff6600 0%, transparent 70%);
  bottom: -30px;
  right: 40px;
  opacity: 0.08;
}

.premium-bg-line {
  position: absolute;
  background: rgba(255, 255, 255, 0.06);
  transform: rotate(-35deg);
}

.premium-bg-line-1 {
  width: 200px;
  height: 1px;
  top: 30px;
  right: -20px;
}

.premium-bg-line-2 {
  width: 160px;
  height: 1px;
  top: 50px;
  right: -10px;
}

.active-scale {
  transition: transform 0.2s ease;
  &:active {
    transform: scale(0.98);
  }
}

.scale-95 {
  transform: scale(0.95);
}
.transition-transform {
  transition-property: transform;
}
.duration-200 {
  transition-duration: 200ms;
}
</style>
