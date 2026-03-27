<template>
  <PresidentLayout :showTabBar="false">
    <PresidentNavBar title="预约管理" />
    <view class="list-content">
      <view v-if="loading" class="loading-wrap"><uni-icons type="spinner-cycle" size="32" color="#3cc51f" class="spin"></uni-icons></view>
      <view v-else-if="list.length === 0" class="empty">暂无预约</view>
        <scroll-view v-else class="list-scroll" scroll-y :lower-threshold="80" @scrolltolower="loadMore">
          <view v-for="item in list" :key="item.id" class="list-item glass-card" @click="goDetail(item.id)">
            <view class="item-row">
              <text class="item-type">场地预约</text>
              <text class="item-amount">¥{{ formatNum(item.amount) }}</text>
            </view>
            <view class="item-meta">{{ item.venueName || '—' }} · {{ item.courtName || '' }}</view>
            <view class="item-meta">{{ item.bookingDate }} {{ item.startTime }}-{{ item.endTime }} · {{ statusText(item.status) }}</view>
          </view>
          <view v-if="hasMore && list.length > 0" class="load-more">加载更多...</view>
        </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentNavBar from '@/components/president/PresidentNavBar.vue'
import { getBookingList, type BookingItem } from '@/api/president/booking'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { parsePagedList } from '@/utils/parsePagedList'

const loading = ref(false)
const list = ref<BookingItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const hasMore = computed(() => list.value.length < total.value)

function formatNum(v: unknown) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) : '0'
}
function statusText(s: unknown) {
  const m: Record<number, string> = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return m[Number(s)] ?? '—'
}

async function loadList(append = false) {
  if (loading.value && !append) return
  if (!append) { page.value = 1; list.value = [] }
  loading.value = true
  try {
    const res = await getBookingList({ page: page.value, size }) as any
    const parsed = parsePagedList<BookingItem>(res)
    total.value = parsed.total
    if (append) list.value = list.value.concat(parsed.list)
    else list.value = parsed.list
    page.value += 1
  } catch (e) { console.error(e) } finally { loading.value = false }
}
function loadMore() { if (hasMore.value && !loading.value) loadList(true) }
function goDetail(id: number) { uni.navigateTo({ url: `${PRESIDENT_PAGES.BOOKING_DETAIL}?id=${id}` }) }

onMounted(() => loadList())
</script>

<style lang="scss" scoped>
.list-content { padding: 24rpx; padding-top: calc(120rpx + env(safe-area-inset-top)); }
.list-scroll { height: calc(100vh - 220rpx); }
.list-item { padding: 28rpx; margin-bottom: 20rpx; border-radius: 24rpx; }
.item-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.item-type { font-size: 28rpx; color: #1E293B; }
.item-amount { font-size: 32rpx; font-weight: 600; color: #1E293B; }
.item-meta { font-size: 24rpx; color: #475569; line-height: 1.5; }
.loading-wrap, .empty { padding: 80rpx; text-align: center; color: #475569; }
.load-more { padding: 24rpx; text-align: center; font-size: 24rpx; color: #475569; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
