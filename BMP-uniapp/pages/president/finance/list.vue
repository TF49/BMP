<template>
  <PresidentLayout :showTabBar="true">
    <view class="finance-list-page">
      <PresidentNavBar title="财务管理" :showBack="false" />
      <view class="list-content">
        <view class="action-row">
          <view class="btn-recon glass-card" @click="goRecon">全面对账</view>
        </view>
        <view v-if="loading" class="loading-wrap"><uni-icons type="spinner-cycle" size="32" color="#3cc51f" class="spin"></uni-icons></view>
        <view v-else-if="list.length === 0" class="empty">暂无财务记录</view>
        <scroll-view v-else class="list-scroll" scroll-y :lower-threshold="80" @scrolltolower="loadMore">
          <view v-for="item in list" :key="item.id" class="list-item glass-card" @click="goDetail(item.id)">
            <view class="item-row">
              <text class="item-type">{{ item.businessType || '—' }}</text>
              <text class="item-amount" :class="item.incomeExpenseType">{{ item.incomeExpenseType === 'INCOME' ? '+' : '-' }}¥{{ formatNum(item.amount) }}</text>
            </view>
            <view class="item-meta">{{ item.venueName || '—' }} · {{ item.createTime || '' }}</view>
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
import { getFinanceList, type FinanceItem } from '@/api/president/finance'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { parsePagedList } from '@/utils/parsePagedList'

const loading = ref(false)
const list = ref<FinanceItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const hasMore = computed(() => list.value.length < total.value)

function formatNum(v: unknown) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) : '0'
}

async function loadList(append = false) {
  if (loading.value && !append) return
  if (!append) { page.value = 1; list.value = [] }
  loading.value = true
  try {
    const res = await getFinanceList({ page: page.value, size }) as any
    const parsed = parsePagedList<FinanceItem>(res)
    total.value = parsed.total
    if (append) list.value = list.value.concat(parsed.list)
    else list.value = parsed.list
    page.value += 1
  } catch (e) { console.error(e) } finally { loading.value = false }
}

function loadMore() { if (hasMore.value && !loading.value) loadList(true) }
function goDetail(id: number) { uni.navigateTo({ url: `${PRESIDENT_PAGES.FINANCE_DETAIL}?id=${id}` }) }
function goRecon() { uni.navigateTo({ url: PRESIDENT_PAGES.FINANCE_RECONCILIATION }) }

onMounted(() => loadList())
</script>

<style lang="scss" scoped>
.finance-list-page { min-height: 100vh; }
.list-content { padding: 24rpx; padding-top: 120rpx; }
.action-row { margin-bottom: 24rpx; }
.btn-recon { display: inline-block; padding: 20rpx 36rpx; border-radius: 16rpx; color: #3cc51f; font-size: 28rpx; }
.list-scroll { height: calc(100vh - 220rpx); }
.list-item { padding: 28rpx; margin-bottom: 20rpx; border-radius: 20rpx; }
.item-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.item-type { font-size: 28rpx; color: #1E293B; }
.item-amount { font-size: 32rpx; font-weight: 600; }
.item-amount.INCOME { color: #22c55e; }
.item-meta { font-size: 24rpx; color: #475569; }
.loading-wrap, .empty { padding: 80rpx; text-align: center; color: #475569; }
.load-more { padding: 24rpx; text-align: center; font-size: 24rpx; color: #475569; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
