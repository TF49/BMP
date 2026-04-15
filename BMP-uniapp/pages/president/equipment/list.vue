<template>
  <PresidentLayout :showTabBar="true">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">器材管理</text>
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
            placeholder="搜索器材名称或编码"
            confirm-type="search"
            @confirm="reloadList"
          />
        </view>

        <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
          <view class="tabs">
            <view class="tab" :class="{ active: typeFilter === '' }" @click="setType('')">全部</view>
            <view class="tab" :class="{ active: typeFilter === 'RACKET' }" @click="setType('RACKET')">球拍</view>
            <view class="tab" :class="{ active: typeFilter === 'SHUTTLE' }" @click="setType('SHUTTLE')">羽毛球</view>
            <view class="tab" :class="{ active: typeFilter === 'STRING' }" @click="setType('STRING')">球线</view>
            <view class="tab" :class="{ active: typeFilter === 'OTHER' }" @click="setType('OTHER')">其他</view>
          </view>
        </scroll-view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载器材列表中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="reloadList">重试</view>
        </view>

        <view v-else-if="equipmentList.length === 0" class="state-wrap">
          <text>暂无符合条件的器材</text>
        </view>

        <view v-else class="list">
          <view class="summary">
            <text>共 {{ total }} 件器材</text>
            <text>当前展示 {{ equipmentList.length }} 件</text>
          </view>

          <view v-for="item in equipmentList" :key="item.id" class="card" @click="goDetail(item.id)">
            <image class="thumb" :src="item.image" mode="aspectFill" />

            <view class="main">
              <view class="row">
                <text class="name">{{ item.name }}</text>
                <view class="status-pill" :class="item.statusMeta.key">
                  {{ item.statusMeta.label }}
                </view>
              </view>

              <text class="sub">{{ item.code }}</text>
              <text class="sub">{{ item.typeLabel }} · {{ item.brand || '未填写品牌' }}</text>

              <view class="meta-row">
                <text>可用 {{ item.available }}/{{ item.total }}</text>
                <text>租金 ¥{{ item.rentalPrice }}/小时</text>
              </view>

              <view class="actions">
                <view class="small-btn" @click.stop="goStock(item.id)">库存视图</view>
                <view class="small-btn primary" @click.stop="goEdit(item.id)">编辑</view>
              </view>
            </view>
          </view>

          <view v-if="hasMore" class="more-btn" @click="loadMore">加载更多</view>
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
import { getEquipmentList, type EquipmentItem } from '@/api/president/equipment'
import { parsePagedList } from '@/utils/parsePagedList'
import { getEquipmentStatusMeta } from '@/utils/presidentStatus'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

type EquipmentCard = {
  id: number
  name: string
  code: string
  typeLabel: string
  brand: string
  image: string
  total: number
  available: number
  rentalPrice: string
  statusMeta: ReturnType<typeof getEquipmentStatusMeta>
}

const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const errorText = ref('')
const keyword = ref('')
const typeFilter = ref('')
const equipmentList = ref<EquipmentCard[]>([])

const hasMore = computed(() => equipmentList.value.length < total.value)

function getTypeLabel(type?: string) {
  const map: Record<string, string> = {
    RACKET: '球拍',
    SHUTTLE: '羽毛球',
    STRING: '球线',
    OTHER: '其他'
  }
  return map[type || ''] || '未分类'
}

function mapEquipment(item: EquipmentItem): EquipmentCard {
  return {
    id: Number(item.id || 0),
    name: item.equipmentName || '未命名器材',
    code: item.equipmentCode || '-',
    typeLabel: getTypeLabel(item.equipmentType),
    brand: item.brand || '',
    image: resolveImageUrl((item as EquipmentItem & { equipmentImage?: string }).equipmentImage) || '/static/placeholders/hero.svg',
    total: Number(item.totalQuantity || 0),
    available: Number(item.availableQuantity || 0),
    rentalPrice: Number(item.rentalPrice || 0).toFixed(2),
    statusMeta: getEquipmentStatusMeta(item.status, item.availableQuantity)
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
    const res = await getEquipmentList({
      page: page.value,
      size,
      equipmentType: typeFilter.value || undefined,
      keyword: keyword.value || undefined
    })
    const parsed = parsePagedList<EquipmentItem>(res)
    const mapped = parsed.list.map(mapEquipment).filter(item => item.id > 0)
    total.value = parsed.total
    equipmentList.value = reset ? mapped : equipmentList.value.concat(mapped)
  } catch (error) {
    if (!equipmentList.value.length) {
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

function setType(type: string) {
  if (typeFilter.value === type) return
  typeFilter.value = type
  reloadList()
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.EQUIPMENT_FORM })
}

function goEdit(id: number) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_FORM}?id=${id}` })
}

function goDetail(id: number) {
  if (!id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_DETAIL}?id=${id}` })
}

function goStock(id: number) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_STOCK}?id=${id}` })
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
.tabs-scroll { white-space: nowrap; }
.tabs { display: inline-flex; gap: 12rpx; }
.tab { padding: 14rpx 28rpx; border-radius: 9999px; background: #e5e7eb; color: #4b5563; font-size: 24rpx; font-weight: 700; white-space: nowrap; }
.tab.active { background: #ffedd5; color: #c2410c; }
.scroll { height: calc(100vh - var(--status-bar-height) - 232rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn, .more-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.list { display: flex; flex-direction: column; gap: 18rpx; }
.summary { display: flex; justify-content: space-between; color: #6b7280; font-size: 22rpx; margin-bottom: 4rpx; }
.card { display: flex; gap: 18rpx; padding: 24rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); }
.thumb { width: 128rpx; height: 128rpx; border-radius: 24rpx; background: #e5e7eb; flex-shrink: 0; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8rpx; }
.row { display: flex; justify-content: space-between; gap: 12rpx; align-items: center; }
.name { font-size: 30rpx; font-weight: 800; }
.sub { font-size: 22rpx; color: #6b7280; }
.meta-row { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 8rpx; font-size: 20rpx; color: #4b5563; }
.actions { display: flex; gap: 12rpx; margin-top: 10rpx; }
.small-btn { height: 64rpx; padding: 0 22rpx; border-radius: 14rpx; background: #e5e7eb; display: inline-flex; align-items: center; justify-content: center; font-size: 22rpx; font-weight: 700; color: #111827; }
.small-btn.primary { background: #ea580c; color: #ffffff; }
.status-pill { padding: 8rpx 16rpx; border-radius: 9999px; font-size: 18rpx; font-weight: 800; }
.status-pill.available, .status-pill.normal { background: #dcfce7; color: #166534; }
.status-pill.out { background: #fee2e2; color: #b91c1c; }
.status-pill.disabled { background: #e5e7eb; color: #4b5563; }
.status-pill.maintenance { background: #fef3c7; color: #b45309; }
.bottom-space { height: 36rpx; }
</style>
