<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-bar-left">
          <view class="icon-round" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
          </view>
          <text class="screen-title">器材租借管理</text>
        </view>
        <view class="top-bar-right">
          <button class="cta-btn" @click="openForm">新增租借</button>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false" @scrolltolower="loadMore">
        <view class="content">
          <view class="stats-grid">
            <StatCard label="当前租借总数" :value="String(stats.totalRentals)" variant="primary" iconType="shop" />
            <StatCard label="逾期未还" :value="pad2(stats.overdue)" variant="error" iconType="notification" />
            <StatCard label="可用库存" :value="String(stats.availableStock)" variant="tertiary" iconType="list" />
          </view>

          <view class="search-panel">
            <view class="search-field">
              <uni-icons class="search-ico" type="search" size="20" color="#5f5e5e" />
              <input
                v-model="keyword"
                class="search-input"
                type="text"
                placeholder="搜索会员姓名或器材..."
                confirm-type="search"
                @input="onSearchInput"
              />
              <view v-if="keyword" class="clear-btn" @click="clearSearch">
                <uni-icons type="clear" size="18" color="#5f5e5e" />
              </view>
            </view>
            <view class="filter-row">
              <view class="chip-btn" :class="{ active: filterStatus !== undefined }" @click="onFilter">
                <uni-icons type="settings" size="16" :color="filterStatus !== undefined ? '#ff6600' : '#5f5e5e'" />
                <text class="chip-text">{{ filterStatus !== undefined ? statusLabel(filterStatus) : '筛选' }}</text>
              </view>
            </view>
          </view>

          <view v-if="loading && list.length === 0" class="state-wrap">
            <text class="state-text">正在加载租借记录...</text>
          </view>

          <view v-else-if="!loading && list.length === 0" class="state-wrap">
            <text class="state-text">暂无租借记录</text>
          </view>

          <view v-else class="list-block">
            <view v-for="row in list" :key="row.id" class="list-gap">
              <RentalListItem
                :member-name="row.memberName"
                :member-id="String(row.memberId)"
                :initials="getInitials(row.memberName)"
                :equipment-name="row.equipmentName"
                :tag="row.rentalNo"
                :date-range="formatDateRange(row.rentalDate, row.expectedReturnDate)"
                :sub-text="getSubText(row)"
                :sub-text-tone="getSubTextTone(row)"
                :status="getStatus(row)"
                @click="openDetail(row)"
              />
            </view>
          </view>

          <view v-if="hasMore && list.length > 0" class="load-more">
            <text>{{ loading ? '加载中...' : '上拉加载更多' }}</text>
          </view>

          <view class="bottom-space" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import StatCard from '@/components/president/equipment-rental/StatCard.vue'
import RentalListItem, { type RentalListStatus } from '@/components/president/equipment-rental/RentalListItem.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getEquipmentRentalList, getEquipmentRentalStatistics, type EquipmentRentalItem } from '@/api/equipment'
import { formatDate } from '@/utils/format'

const stats = ref({
  totalRentals: 0,
  overdue: 0,
  availableStock: 0
})

const keyword = ref('')
const filterStatus = ref<number | undefined>(undefined)
const loading = ref(false)
const list = ref<EquipmentRentalItem[]>([])
const page = ref(1)
const size = 20
const total = ref(0)
let searchTimer: number | null = null

const hasMore = computed(() => list.value.length < total.value)

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '租借中', value: 0 },
  { label: '已归还', value: 1 },
  { label: '逾期', value: 2 }
]

function statusLabel(status: number) {
  return statusOptions.find(o => o.value === status)?.label || '未知'
}

function getInitials(name: string) {
  if (!name) return '?'
  const words = name.trim().split(/\s+/)
  if (words.length >= 2) {
    return (words[0][0] + words[1][0]).toUpperCase()
  }
  return name.substring(0, 2).toUpperCase()
}

function formatDateRange(start?: string, end?: string) {
  if (!start || !end) return '日期未知'
  return `${formatDate(start)} — ${formatDate(end)}`
}

function getSubText(item: EquipmentRentalItem): string {
  if (item.status === 2) {
    return '逾期未还'
  }
  if (item.status === 1) {
    return `已于 ${formatDate(item.actualReturnDate || '')} 归还`
  }
  // 租借中，计算剩余天数
  if (item.expectedReturnDate) {
    const now = new Date()
    const expected = new Date(item.expectedReturnDate)
    const diffDays = Math.ceil((expected.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
    if (diffDays > 0) {
      return `还有 ${diffDays} 天到期`
    } else if (diffDays === 0) {
      return '今天到期'
    }
  }
  return '租借中'
}

function getSubTextTone(item: EquipmentRentalItem): 'error' | 'muted' {
  return item.status === 2 ? 'error' : 'muted'
}

function getStatus(item: EquipmentRentalItem): RentalListStatus {
  if (item.status === 2) return 'overdue'
  if (item.status === 1) return 'returned'
  return 'on_rent'
}

async function loadStatistics() {
  try {
    const res = await getEquipmentRentalStatistics()
    stats.value = {
      totalRentals: res.totalRentals || 0,
      overdue: res.overdue || 0,
      availableStock: res.availableStock || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

async function loadList(append = false) {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getEquipmentRentalList({
      keyword: keyword.value.trim() || undefined,
      status: filterStatus.value,
      page: page.value,
      size
    })
    const records = Array.isArray(res.data) ? res.data : []
    total.value = Number(res.total || 0)
    list.value = append ? list.value.concat(records) : records
  } catch (error) {
    console.error('加载租借列表失败:', error)
    if (!append) {
      list.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadList(false)
  }, 500)
}

function clearSearch() {
  keyword.value = ''
  page.value = 1
  loadList(false)
}

function onFilter() {
  uni.showActionSheet({
    itemList: statusOptions.map(o => o.label),
    success: (res) => {
      const selected = statusOptions[res.tapIndex]
      filterStatus.value = selected.value
      page.value = 1
      loadList(false)
    }
  })
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  page.value += 1
  loadList(true)
}

function pad2(n: number) {
  return n < 10 ? `0${n}` : String(n)
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function openForm() {
  uni.navigateTo({ url: PRESIDENT_PAGES.EQUIPMENT_RENTAL_FORM })
}

function openDetail(row: EquipmentRentalItem) {
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.EQUIPMENT_RENTAL_DETAIL}?id=${row.id}`
  })
}

onMounted(() => {
  loadStatistics()
  loadList(false)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}
.top-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx 24rpx;
  background: #f9f9f9;
  position: sticky;
  top: 0;
  z-index: 20;
}
.top-bar-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}
.top-bar-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}
.icon-round {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-round:active {
  background: rgba(0, 0, 0, 0.05);
}
.screen-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}
.cta-btn {
  margin: 0;
  padding: 0 28rpx;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
  font-weight: 800;
  color: #561d00;
  background: #ff6600;
  border-radius: 16rpx;
  border: none;
}
.cta-btn::after {
  border: none;
}
.scroll {
  flex: 1;
  height: 0;
}
.content {
  padding: 8rpx 32rpx 32rpx;
  max-width: 1600rpx;
  margin: 0 auto;
}
.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
  margin-bottom: 40rpx;
}
.search-panel {
  background: #f3f3f3;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 36rpx;
}
.search-field {
  position: relative;
  width: 100%;
}
.search-ico {
  position: absolute;
  left: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1;
}
.search-input {
  width: 100%;
  padding: 24rpx 72rpx 24rpx 72rpx;
  background: #ffffff;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  border: none;
}
.clear-btn {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}
.clear-btn:active {
  background: #f3f3f3;
}
.filter-row {
  margin-top: 20rpx;
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}
.chip-btn {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  padding: 22rpx 16rpx;
  background: #e2e2e2;
  border-radius: 16rpx;
  transition: all 0.2s;
}
.chip-btn:active {
  background: #e2dfde;
}
.chip-btn.active {
  background: #ffe8d6;
  border: 2rpx solid #ff6600;
}
.chip-text {
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.chip-btn.active .chip-text {
  color: #ff6600;
}
.list-block {
  display: flex;
  flex-direction: column;
}
.list-gap {
  margin-bottom: 24rpx;
}
.state-wrap {
  padding: 100rpx 0;
  text-align: center;
}
.state-text {
  color: #5f5e5e;
  font-size: 28rpx;
}
.load-more {
  padding: 40rpx 0;
  text-align: center;
  color: #5f5e5e;
  font-size: 26rpx;
}
.bottom-space {
  height: 48rpx;
}
</style>
