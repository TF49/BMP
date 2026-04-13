<template>
  <PresidentLayout :showTabBar="true">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">教练管理</text>
        </view>
        <view class="icon-btn add-btn" @click="goAdd">
          <uni-icons type="plusempty" size="22" color="#ffffff" />
        </view>
      </view>

      <view class="toolbar">
        <view class="search-box">
          <uni-icons type="search" size="18" color="#71717a" />
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="搜索教练姓名、专长、场馆"
            confirm-type="search"
            @confirm="reloadList"
          />
        </view>
        <view class="tabs">
          <view class="tab" :class="{ active: statusFilter === -1 }" @click="setStatus(-1)">全部</view>
          <view class="tab" :class="{ active: statusFilter === 1 }" @click="setStatus(1)">在职</view>
          <view class="tab" :class="{ active: statusFilter === 0 }" @click="setStatus(0)">停用</view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载教练列表中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="reloadList">重试</view>
        </view>

        <view v-else-if="coachList.length === 0" class="state-wrap">
          <text>暂无符合条件的教练</text>
        </view>

        <view v-else class="list">
          <view class="summary">
            <text>共 {{ total }} 位教练</text>
            <text>当前展示 {{ coachList.length }} 位</text>
          </view>

          <view v-for="item in coachList" :key="item.id" class="card" @click="goDetail(item.id)">
            <image class="avatar" :src="item.avatar" mode="aspectFill" />

            <view class="main">
              <view class="row">
                <text class="name">{{ item.name }}</text>
                <view class="status-pill" :class="item.statusMeta.key">
                  {{ item.statusMeta.label }}
                </view>
              </view>

              <text class="sub">{{ item.venueName || '未绑定场馆' }}</text>
              <text class="sub">{{ item.specialty || '未填写专长' }}</text>

              <view class="meta-row">
                <text>评分 {{ item.ratingText }}</text>
                <text>课时费 ¥{{ item.priceText }}/小时</text>
                <text>学员 {{ item.totalStudents }}</text>
              </view>
            </view>
          </view>

          <view v-if="hasMore" class="more-btn" @click="loadMore">
            加载更多
          </view>
        </view>

        <view class="bottom-space" />
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getCoachList, type CoachDto } from '@/api/coach'
import { parsePagedList } from '@/utils/parsePagedList'
import { getCoachStatusMeta } from '@/utils/presidentStatus'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

type CoachCard = {
  id: number
  name: string
  avatar: string
  venueName: string
  specialty: string
  totalStudents: number
  ratingText: string
  priceText: string
  statusMeta: ReturnType<typeof getCoachStatusMeta>
}

const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const errorText = ref('')
const keyword = ref('')
const statusFilter = ref(-1)
const coachList = ref<CoachCard[]>([])

const hasMore = computed(() => coachList.value.length < total.value)

function mapCoach(item: CoachDto): CoachCard {
  return {
    id: Number(item.id || 0),
    name: item.coachName || '未命名教练',
    avatar: resolveImageUrl(item.avatar) || '/static/placeholders/avatar.svg',
    venueName: item.venueName || '',
    specialty: item.specialty || '',
    totalStudents: Number(item.totalStudents || 0),
    ratingText: item.rating != null ? Number(item.rating).toFixed(1) : '暂无',
    priceText: item.hourlyPrice != null ? Number(item.hourlyPrice).toFixed(2) : '0.00',
    statusMeta: getCoachStatusMeta(item.status)
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
    const res = await getCoachList({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      status: statusFilter.value >= 0 ? statusFilter.value : undefined
    })
    const parsed = parsePagedList<CoachDto>(res)
    const mapped = parsed.list.map(mapCoach).filter(item => item.id > 0)
    total.value = parsed.total
    coachList.value = reset ? mapped : coachList.value.concat(mapped)
  } catch (error) {
    if (!coachList.value.length) {
      errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

function reloadList() {
  fetchList(true)
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  page.value += 1
  fetchList()
}

function setStatus(status: number) {
  if (statusFilter.value === status) return
  statusFilter.value = status
  reloadList()
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COACH_FORM })
}

function goDetail(id: number) {
  if (!id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COACH_DETAIL}?id=${id}` })
}

onShow(() => {
  reloadList()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f8fafc; color: #111827; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { display: flex; align-items: center; justify-content: space-between; padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #ffffff; display: flex; align-items: center; justify-content: center; }
.add-btn { background: #ea580c; }
.title { font-size: 38rpx; font-weight: 800; }
.toolbar { padding: 0 24rpx 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.search-box { display: flex; align-items: center; gap: 12rpx; padding: 0 24rpx; height: 88rpx; border-radius: 20rpx; background: #ffffff; }
.search-input { flex: 1; font-size: 26rpx; }
.tabs { display: flex; gap: 12rpx; }
.tab { padding: 14rpx 28rpx; border-radius: 9999px; background: #e5e7eb; color: #4b5563; font-size: 24rpx; font-weight: 700; }
.tab.active { background: #ffedd5; color: #c2410c; }
.scroll { height: calc(100vh - var(--status-bar-height) - 232rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn, .more-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.summary { display: flex; justify-content: space-between; color: #6b7280; font-size: 22rpx; margin-bottom: 20rpx; }
.list { display: flex; flex-direction: column; gap: 18rpx; }
.card { display: flex; gap: 20rpx; padding: 24rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); }
.avatar { width: 120rpx; height: 120rpx; border-radius: 24rpx; background: #e5e7eb; flex-shrink: 0; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8rpx; }
.row { display: flex; justify-content: space-between; gap: 12rpx; align-items: center; }
.name { font-size: 32rpx; font-weight: 800; color: #111827; }
.sub { font-size: 22rpx; color: #6b7280; }
.meta-row { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 8rpx; font-size: 20rpx; color: #4b5563; }
.status-pill { padding: 8rpx 16rpx; border-radius: 9999px; font-size: 18rpx; font-weight: 800; }
.status-pill.active { background: #dcfce7; color: #166534; }
.status-pill.inactive { background: #fee2e2; color: #b91c1c; }
.status-pill.unknown { background: #e5e7eb; color: #4b5563; }
.bottom-space { height: 36rpx; }
</style>
