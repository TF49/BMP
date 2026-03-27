<template>
  <PresidentLayout :showTabBar="true">
    <view class="venue-list-page">
      <PresidentNavBar title="场馆管理" />
      <view class="list-content">
        <view class="action-bar">
          <view class="btn-add glass-card" @click="goAdd">添加场馆</view>
        </view>
        <view v-if="loading" class="loading-wrap">
          <uni-icons type="spinner-cycle" size="32" color="#3cc51f" class="spin"></uni-icons>
        </view>
        <view v-else-if="list.length === 0" class="empty">暂无场馆</view>
        <scroll-view
          v-else
          class="list-scroll"
          scroll-y
          :lower-threshold="80"
          @scrolltolower="loadMore"
        >
          <view
            v-for="item in list"
            :key="item.id"
            class="list-item glass-card"
            @click="goDetail(item.id)"
          >
            <view class="item-row">
              <text class="item-name">{{ item.venueName }}</text>
              <text class="item-status" :class="{ on: item.status === 1 }">{{ item.status === 1 ? '营业中' : '已关闭' }}</text>
            </view>
            <view class="item-meta">{{ item.address || '—' }}</view>
          </view>
          <view v-if="hasMore && list.length > 0" class="load-more">加载更多...</view>
        </scroll-view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentNavBar from '@/components/president/PresidentNavBar.vue'
import { getVenueList, type VenueItem } from '@/api/president/venue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { parsePagedList } from '@/utils/parsePagedList'

const loading = ref(false)
const list = ref<VenueItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const hasMore = computed(() => list.value.length < total.value)

async function loadList(append = false) {
  if (loading.value && !append) return
  if (!append) { page.value = 1; list.value = [] }
  loading.value = true
  try {
    const res = await getVenueList({ page: page.value, size }) as any
    const parsed = parsePagedList<VenueItem>(res)
    total.value = parsed.total
    if (append) list.value = list.value.concat(parsed.list)
    else list.value = parsed.list
    page.value += 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (hasMore.value && !loading.value) loadList(true)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.VENUE_DETAIL}?id=${id}` })
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.VENUE_FORM })
}

onMounted(() => loadList())
</script>

<style lang="scss" scoped>
.venue-list-page { min-height: 100vh; }
.list-content { padding: 24rpx; padding-top: calc(120rpx + env(safe-area-inset-top)); }
.action-bar { margin-bottom: 24rpx; }
.btn-add {
  display: inline-block;
  padding: 20rpx 36rpx;
  background: var(--glass-bg);
  border-radius: 16rpx;
  color: var(--color-primary, #3cc51f);
  font-size: 28rpx;
}
.list-scroll { height: calc(100vh - 220rpx); }
.list-item {
  padding: 28rpx;
  margin-bottom: 20rpx;
  border-radius: 24rpx;
}
.item-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.item-name { font-size: 32rpx; font-weight: 600; color: #1E293B; }
.item-meta { font-size: 24rpx; color: #475569; display: block; line-height: 1.5; }
.item-status { font-size: 24rpx; color: #94a3b8; }
.item-status.on { color: #22c55e; }
.loading-wrap, .empty { padding: 80rpx; text-align: center; color: #475569; }
.load-more { padding: 24rpx; text-align: center; font-size: 24rpx; color: #475569; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
