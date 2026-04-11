<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-bar-left">
          <view class="icon-round" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
          </view>
          <text class="screen-title">管理</text>
        </view>
        <view class="top-bar-right">
          <view class="icon-round">
            <uni-icons type="search" size="22" color="#ff6600" />
          </view>
          <button class="cta-btn" @click="openForm">新增租借</button>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
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
              />
            </view>
            <view class="filter-row">
              <view class="chip-btn" @click="onFilter">
                <uni-icons type="settings" size="16" color="#5f5e5e" />
                <text class="chip-text">筛选</text>
              </view>
              <view class="chip-btn" @click="onSort">
                <uni-icons type="arrowdown" size="16" color="#5f5e5e" />
                <text class="chip-text">排序</text>
              </view>
            </view>
          </view>

          <view class="list-block">
            <view v-for="row in pagedRows" :key="row.key" class="list-gap">
              <RentalListItem
                :member-name="row.memberName"
                :member-id="row.memberId"
                :initials="row.initials"
                :avatar-url="row.avatarUrl"
                :equipment-name="row.equipmentName"
                :tag="row.tag"
                :date-range="row.dateRange"
                :sub-text="row.subText"
                :sub-text-tone="row.subTextTone"
                :status="row.status"
                @click="openDetail(row)"
                @more="onRowMore(row)"
              />
            </view>
          </view>

          <Pagination v-model="currentPage" :total-pages="totalPages" />

          <view class="bottom-space" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import StatCard from '@/components/president/equipment-rental/StatCard.vue'
import RentalListItem, { type RentalListStatus } from '@/components/president/equipment-rental/RentalListItem.vue'
import Pagination from '@/components/president/equipment-rental/Pagination.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

type RentalRow = {
  key: string
  memberName: string
  memberId: string
  initials: string
  avatarUrl?: string
  equipmentName: string
  tag: string
  dateRange: string
  subText: string
  subTextTone: 'error' | 'muted'
  status: RentalListStatus
}

const stats = ref({
  totalRentals: 42,
  overdue: 8,
  availableStock: 124
})

const keyword = ref('')
const currentPage = ref(1)
const pageSize = 4
const allRows = ref<RentalRow[]>([
  {
    key: '1',
    memberName: 'Marcus Aris',
    memberId: 'KL-2024-0082',
    initials: 'MA',
    equipmentName: '尤尼克斯 天斧 88D Pro',
    tag: '#EQU-9921',
    dateRange: '10月12日 — 10月19日',
    subText: '逾期 3 天',
    subTextTone: 'error',
    status: 'overdue'
  },
  {
    key: '2',
    memberName: 'Sarah Ling',
    memberId: 'KL-2024-0154',
    initials: 'SL',
    equipmentName: '胜利 突击 F 增强版 (Victor Thruster F)',
    tag: '#EQU-1042',
    dateRange: '10月20日 — 10月23日',
    subText: '还有 1 天到期',
    subTextTone: 'muted',
    status: 'on_rent'
  },
  {
    key: '3',
    memberName: 'David Kim',
    memberId: 'KL-2024-0012',
    initials: 'DK',
    equipmentName: '李宁 突袭 7 (Li-Ning Tectonic 7)',
    tag: '#EQU-5532',
    dateRange: '10月15日 — 10月18日',
    subText: '已于 10月18日 归还',
    subTextTone: 'muted',
    status: 'returned'
  },
  {
    key: '4',
    memberName: 'Tobias Chen',
    memberId: 'KL-2024-0091',
    initials: 'TC',
    avatarUrl:
      '/static/placeholders/hero.svg',
    equipmentName: '练习装备 (全套)',
    tag: '#EQU-B002',
    dateRange: '10月21日 — 10月28日',
    subText: '还有 6 天到期',
    subTextTone: 'muted',
    status: 'on_rent'
  },
  {
    key: '5',
    memberName: 'Elena Wu',
    memberId: 'KL-2024-0201',
    initials: 'EW',
    equipmentName: '尤尼克斯 弓箭11 Pro',
    tag: '#EQU-2201',
    dateRange: '10月22日 — 10月25日',
    subText: '还有 2 天到期',
    subTextTone: 'muted',
    status: 'on_rent'
  },
  {
    key: '6',
    memberName: 'James Park',
    memberId: 'KL-2024-0198',
    initials: 'JP',
    equipmentName: '李宁 雷霆 80',
    tag: '#EQU-8891',
    dateRange: '10月01日 — 10月08日',
    subText: '逾期 5 天',
    subTextTone: 'error',
    status: 'overdue'
  },
  {
    key: '7',
    memberName: 'Nina Zhao',
    memberId: 'KL-2024-0110',
    initials: 'NZ',
    equipmentName: 'Victor 神速100X',
    tag: '#EQU-4410',
    dateRange: '10月10日 — 10月17日',
    subText: '已于 10月17日 归还',
    subTextTone: 'muted',
    status: 'returned'
  },
  {
    key: '8',
    memberName: 'Leo Zhang',
    memberId: 'KL-2024-0077',
    initials: 'LZ',
    equipmentName: '球拍 + 球鞋套装',
    tag: '#EQU-P077',
    dateRange: '10月24日 — 10月30日',
    subText: '还有 8 天到期',
    subTextTone: 'muted',
    status: 'on_rent'
  }
])

const filteredRows = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return allRows.value
  return allRows.value.filter(
    r =>
      r.memberName.toLowerCase().includes(q) ||
      r.equipmentName.toLowerCase().includes(q) ||
      r.tag.toLowerCase().includes(q) ||
      r.memberId.toLowerCase().includes(q)
  )
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize)))

const pagedRows = computed(() => {
  const page = Math.min(currentPage.value, totalPages.value)
  const start = (page - 1) * pageSize
  return filteredRows.value.slice(start, start + pageSize)
})

watch(keyword, () => {
  currentPage.value = 1
})

function pad2(n: number) {
  return n < 10 ? `0${n}` : String(n)
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function openForm() {
  uni.navigateTo({ url: PRESIDENT_PAGES.EQUIPMENT_RENTAL_FORM })
}

function openDetail(row: RentalRow) {
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.EQUIPMENT_RENTAL_DETAIL}?id=${encodeURIComponent(row.key)}`
  })
}

function onRowMore(row: RentalRow) {
  uni.showActionSheet({
    itemList: ['查看详情', '联系会员'],
    success(res) {
      if (res.tapIndex === 0) openDetail(row)
    }
  })
}

function onFilter() {
  uni.showToast({ title: '筛选条件开发中', icon: 'none' })
}

function onSort() {
  uni.showToast({ title: '排序方式开发中', icon: 'none' })
}
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
  padding: 24rpx 24rpx 24rpx 72rpx;
  background: #ffffff;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  border: none;
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
}
.chip-btn:active {
  background: #e2dfde;
}
.chip-text {
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.list-block {
  display: flex;
  flex-direction: column;
}
.list-gap {
  margin-bottom: 24rpx;
}
.bottom-space {
  height: 48rpx;
}
</style>
