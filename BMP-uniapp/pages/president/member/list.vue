<template>
  <PresidentLayout :showTabBar="false">
    <PresidentNavBar title="会员管理" />
    <view class="list-content">
      <view v-if="loading" class="loading-wrap"><uni-icons type="spinner-cycle" size="32" color="#3cc51f" class="spin"></uni-icons></view>
      <view v-else-if="list.length === 0" class="empty">暂无会员数据</view>
      <scroll-view v-else class="list-scroll" scroll-y :lower-threshold="80" @scrolltolower="loadMore">
        <view v-for="item in list" :key="item.id" class="list-item glass-card">
          <text class="item-name">{{ item.memberName || ('会员#' + item.id) }}</text>
          <text class="item-meta" v-if="item.phone">电话 {{ item.phone }}</text>
          <text class="item-meta">余额 ¥{{ formatNum(item.balance) }} · {{ memberTypeLabel(item.memberType) }}</text>
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
import { getMemberList, type MemberListItem } from '@/api/president/member'
import { parsePagedList } from '@/utils/parsePagedList'

const loading = ref(false)
const list = ref<MemberListItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const hasMore = computed(() => list.value.length < total.value)

function formatNum(v: unknown) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) : '0'
}

function memberTypeLabel(t?: string) {
  const m: Record<string, string> = { NORMAL: '普通', MEMBER: '会员' }
  return (t && m[t]) || t || '—'
}

async function loadList(append = false) {
  if (loading.value && !append) return
  if (!append) { page.value = 1; list.value = [] }
  loading.value = true
  try {
    const res = await getMemberList({ page: page.value, size }) as any
    const parsed = parsePagedList<MemberListItem>(res)
    total.value = parsed.total
    if (append) list.value = list.value.concat(parsed.list)
    else list.value = parsed.list
    page.value += 1
  } catch (e) { console.error(e) } finally { loading.value = false }
}
function loadMore() { if (hasMore.value && !loading.value) loadList(true) }

onMounted(() => loadList())
</script>

<style lang="scss" scoped>
.list-content { padding: 24rpx; padding-top: 120rpx; }
.list-scroll { height: calc(100vh - 180rpx); }
.list-item { padding: 28rpx; margin-bottom: 20rpx; border-radius: 20rpx; }
.item-name { font-size: 30rpx; font-weight: 600; display: block; margin-bottom: 8rpx; }
.item-meta { font-size: 24rpx; color: #475569; }
.loading-wrap, .empty { padding: 80rpx; text-align: center; color: #475569; }
.load-more { padding: 24rpx; text-align: center; font-size: 24rpx; color: #475569; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
