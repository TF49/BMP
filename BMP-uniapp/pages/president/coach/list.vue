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
          <uni-icons type="plusempty" size="22" color="#ea580c" />
        </view>
      </view>

      <view class="toolbar">
        <text class="search-label">搜索教练</text>
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
          <view class="tab" :class="{ active: statusFilter === 0 }" @click="setStatus(0)">休息中</view>
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
          <view v-for="item in coachList" :key="item.id" class="card" @click="goDetail(item.id)">
            <view class="card-head">
              <view class="avatar-wrap">
                <image class="avatar" :src="item.avatar" mode="aspectFill" />
                <view class="status-dot" :class="item.statusMeta.key" />
              </view>
              <view class="main">
                <view class="row">
                  <text class="name">{{ item.name }}</text>
                  <view class="score-pill">
                    <uni-icons type="star-filled" size="12" color="#ea580c" />
                    <text class="score-text">{{ item.ratingText }}</text>
                  </view>
                </view>

                <text class="sub">{{ item.venueName || '未绑定场馆' }}</text>
                <view class="tag-wrap">
                  <text v-for="(tag, idx) in getCoachTags(item)" :key="`${item.id}-${idx}`" class="tag">{{ tag }}</text>
                </view>
              </view>
            </view>

            <view class="meta-row">
              <view>
                <text class="meta-label">价格</text>
                <view class="price-line">
                  <text class="price">¥{{ item.priceText }}</text>
                  <text class="unit">/ 小时</text>
                </view>
              </view>
              <view class="meta-right">
                <text class="meta-label">活跃度</text>
                <text class="meta-value">累计 {{ item.totalStudents }}+ 学员</text>
              </view>
            </view>
          </view>

          <view class="add-card" @click="goAdd">
            <uni-icons type="plusempty" size="30" color="#9ca3af" />
            <text>添加新教练</text>
          </view>

          <view v-if="hasMore" class="more-btn" @click="loadMore">
            加载更多
          </view>
        </view>

        <view class="bottom-space" />
      </scroll-view>

      <view class="fab" @click="goAdd">
        <uni-icons type="plusempty" size="30" color="#ffffff" />
      </view>
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

function getCoachTags(item: CoachCard) {
  const raw = (item.specialty || '')
    .split(/[，,、/|；;\s]+/)
    .map(v => v.trim())
    .filter(Boolean)
  if (raw.length >= 2) return raw.slice(0, 2)
  if (raw.length === 1) return [raw[0], `${item.totalStudents || 0}名学员`]
  return ['专业教练', `${item.totalStudents || 0}名学员`]
}

onShow(() => {
  reloadList()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f9f9f9; color: #111827; position: relative; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f9f9f9; }
.top-bar { display: flex; align-items: center; justify-content: space-between; padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
.add-btn { background: #ffedd5; }
.title { font-size: 42rpx; font-weight: 800; }
.toolbar { padding: 0 24rpx 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.search-label { color: #6b7280; font-size: 18rpx; letter-spacing: 2rpx; font-weight: 700; text-transform: uppercase; }
.search-box { display: flex; align-items: center; gap: 12rpx; padding: 0 24rpx; height: 88rpx; border-radius: 20rpx; background: #ffffff; border: 1rpx solid #f1f5f9; }
.search-input { flex: 1; font-size: 26rpx; }
.tabs { display: flex; gap: 8rpx; padding: 8rpx; background: #ececec; border-radius: 20rpx; }
.tab { flex: 1; text-align: center; padding: 12rpx 12rpx; border-radius: 14rpx; color: #4b5563; font-size: 24rpx; font-weight: 700; }
.tab.active { background: #ffffff; color: #c2410c; box-shadow: 0 4rpx 12rpx rgba(15, 23, 42, 0.06); }
.scroll { height: calc(100vh - var(--status-bar-height) - 248rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn, .more-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.list { display: flex; flex-direction: column; gap: 18rpx; }
.card { display: flex; flex-direction: column; gap: 18rpx; padding: 28rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.04); }
.card-head { display: flex; gap: 20rpx; align-items: flex-start; }
.avatar-wrap { position: relative; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 24rpx; background: #e5e7eb; flex-shrink: 0; border: 4rpx solid #fff7ed; }
.status-dot { position: absolute; right: -6rpx; bottom: -6rpx; width: 24rpx; height: 24rpx; border-radius: 9999rpx; border: 4rpx solid #ffffff; background: #9ca3af; }
.status-dot.active { background: #22c55e; }
.status-dot.inactive { background: #9ca3af; }
.status-dot.unknown { background: #d1d5db; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8rpx; }
.row { display: flex; justify-content: space-between; gap: 12rpx; align-items: center; }
.name { font-size: 32rpx; font-weight: 800; color: #111827; }
.sub { font-size: 22rpx; color: #6b7280; }
.score-pill { display: flex; align-items: center; gap: 4rpx; background: #fff7ed; padding: 4rpx 10rpx; border-radius: 10rpx; }
.score-text { font-size: 20rpx; color: #c2410c; font-weight: 700; }
.tag-wrap { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 6rpx; }
.tag { font-size: 18rpx; color: #52525b; background: #f1f5f9; border-radius: 9999rpx; padding: 6rpx 12rpx; font-weight: 600; }
.meta-row { display: flex; justify-content: space-between; gap: 16rpx; margin-top: 2rpx; padding-top: 14rpx; border-top: 1rpx solid #f5f5f5; }
.meta-label { display: block; font-size: 16rpx; color: #a1a1aa; font-weight: 700; text-transform: uppercase; letter-spacing: 1rpx; margin-bottom: 8rpx; }
.price-line { display: flex; align-items: baseline; gap: 6rpx; }
.price { font-size: 34rpx; font-weight: 900; color: #ea580c; }
.unit { font-size: 18rpx; color: #9ca3af; }
.meta-right { text-align: right; }
.meta-value { font-size: 28rpx; color: #111827; font-weight: 800; }
.add-card { height: 148rpx; border-radius: 24rpx; border: 2rpx dashed #d1d5db; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6rpx; color: #9ca3af; background: #f9fafb; font-size: 24rpx; font-weight: 700; }
.fab { position: fixed; right: 28rpx; bottom: calc(150rpx + env(safe-area-inset-bottom)); width: 96rpx; height: 96rpx; border-radius: 24rpx; background: linear-gradient(135deg, #ff7a00, #ff5a00); box-shadow: 0 12rpx 24rpx rgba(255, 106, 0, 0.3); display: flex; align-items: center; justify-content: center; z-index: 30; }
.bottom-space { height: 168rpx; }
</style>
