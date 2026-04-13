<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">器材详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载器材详情中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="loadData">重试</view>
        </view>

        <view v-else-if="!equipment" class="state-wrap">
          <text>未找到器材信息</text>
        </view>

        <template v-else>
          <view class="hero">
            <image class="hero-img" :src="imageUrl" mode="aspectFill" />
            <view class="badge" :class="statusMeta.key">{{ statusMeta.label }}</view>
          </view>

          <view class="headline">
            <view>
              <text class="name">{{ equipment.equipmentName }}</text>
              <text class="sub">{{ equipment.equipmentCode || '-' }}</text>
            </view>
            <view class="price-card">
              <text class="price-label">租借价格</text>
              <text class="price-value">¥{{ rentalPriceText }}/小时</text>
            </view>
          </view>

          <view class="grid">
            <view class="info-card">
              <text class="label">器材类型</text>
              <text class="value">{{ typeLabel }}</text>
            </view>
            <view class="info-card">
              <text class="label">品牌</text>
              <text class="value">{{ equipment.brand || '未填写品牌' }}</text>
            </view>
            <view class="info-card">
              <text class="label">总库存</text>
              <text class="value">{{ equipment.totalQuantity || 0 }}</text>
            </view>
            <view class="info-card">
              <text class="label">可用库存</text>
              <text class="value">{{ equipment.availableQuantity || 0 }}</text>
            </view>
          </view>

          <view class="panel">
            <view class="panel-head">
              <text class="panel-title">库存状态</text>
              <text class="panel-value">{{ stockPercent }}%</text>
            </view>
            <view class="progress-track">
              <view class="progress-fill" :style="{ width: `${stockPercent}%` }" />
            </view>
            <text class="panel-tip">可用库存 {{ equipment.availableQuantity || 0 }} / {{ equipment.totalQuantity || 0 }}</text>
          </view>

          <view class="panel">
            <text class="panel-title">采购价格</text>
            <text class="desc">¥{{ priceText }}</text>
          </view>

          <view class="panel">
            <text class="panel-title">器材描述</text>
            <text class="desc">{{ equipment.description || '暂无器材描述。' }}</text>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>

      <view v-if="equipment" class="footer">
        <view class="footer-btn ghost" @click="goStock">库存视图</view>
        <view class="footer-btn primary" @click="goEdit">编辑器材</view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getEquipmentDetail, type EquipmentItem } from '@/api/equipment'
import { getEquipmentStatusMeta } from '@/utils/presidentStatus'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

const equipmentId = ref(0)
const loading = ref(true)
const errorText = ref('')
const equipment = ref<(EquipmentItem & { equipmentImage?: string }) | null>(null)

const imageUrl = computed(() => resolveImageUrl(equipment.value?.equipmentImage) || '/static/placeholders/hero.svg')
const statusMeta = computed(() => getEquipmentStatusMeta(equipment.value?.status, equipment.value?.availableQuantity))
const stockPercent = computed(() => {
  const total = Number(equipment.value?.totalQuantity || 0)
  const available = Number(equipment.value?.availableQuantity || 0)
  return total > 0 ? Math.min(100, Math.round((available / total) * 100)) : 0
})
const rentalPriceText = computed(() => Number(equipment.value?.rentalPrice || 0).toFixed(2))
const priceText = computed(() => Number(equipment.value?.price || 0).toFixed(2))
const typeLabel = computed(() => {
  const map: Record<string, string> = {
    RACKET: '球拍',
    SHUTTLE: '羽毛球',
    STRING: '球线',
    OTHER: '其他'
  }
  return map[equipment.value?.equipmentType || ''] || '未分类'
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_LIST)
}

function goEdit() {
  if (!equipmentId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_FORM}?id=${equipmentId.value}` })
}

function goStock() {
  if (!equipmentId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_STOCK}?id=${equipmentId.value}` })
}

async function loadData() {
  if (!equipmentId.value) return
  loading.value = true
  errorText.value = ''
  equipment.value = null

  try {
    equipment.value = await getEquipmentDetail(equipmentId.value)
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onLoad((query?: Record<string, string | undefined>) => {
  const id = Number(query?.id || 0)
  if (!id) {
    loading.value = false
    errorText.value = '缺少器材参数'
    return
  }
  equipmentId.value = id
  loadData()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f8fafc; color: #111827; padding-bottom: 132rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #ffffff; display: flex; align-items: center; justify-content: center; }
.title { font-size: 38rpx; font-weight: 800; }
.scroll { height: calc(100vh - var(--status-bar-height) - 108rpx - 132rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.hero { position: relative; height: 320rpx; border-radius: 28rpx; overflow: hidden; background: #e5e7eb; }
.hero-img { width: 100%; height: 100%; }
.badge { position: absolute; left: 18rpx; top: 18rpx; padding: 10rpx 18rpx; border-radius: 9999px; font-size: 20rpx; font-weight: 800; }
.badge.available, .badge.normal { background: #dcfce7; color: #166534; }
.badge.out { background: #fee2e2; color: #b91c1c; }
.badge.disabled { background: #e5e7eb; color: #4b5563; }
.badge.maintenance { background: #fef3c7; color: #b45309; }
.headline { margin-top: 18rpx; display: flex; gap: 16rpx; justify-content: space-between; align-items: flex-start; }
.name { display: block; font-size: 42rpx; font-weight: 900; }
.sub { display: block; margin-top: 8rpx; font-size: 22rpx; color: #6b7280; }
.price-card, .panel, .info-card { background: #ffffff; border-radius: 24rpx; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); }
.price-card { padding: 18rpx 20rpx; min-width: 260rpx; }
.price-label { display: block; font-size: 18rpx; color: #6b7280; }
.price-value { display: block; margin-top: 8rpx; font-size: 30rpx; color: #c2410c; font-weight: 900; }
.grid { margin-top: 18rpx; display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.info-card { padding: 22rpx; }
.label { display: block; font-size: 20rpx; color: #6b7280; margin-bottom: 8rpx; }
.value { font-size: 24rpx; font-weight: 700; line-height: 1.5; }
.panel { margin-top: 18rpx; padding: 22rpx; display: flex; flex-direction: column; gap: 12rpx; }
.panel-head { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.panel-title { font-size: 26rpx; font-weight: 800; }
.panel-value { font-size: 22rpx; font-weight: 700; color: #c2410c; }
.progress-track { height: 12rpx; border-radius: 9999px; background: #e5e7eb; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 9999px; background: linear-gradient(90deg, #a33e00, #ff6600); }
.panel-tip, .desc { font-size: 24rpx; color: #4b5563; line-height: 1.7; }
.bottom-space { height: 36rpx; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; display: flex; gap: 12rpx; padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom)); background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(16px); }
.footer-btn { height: 88rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 800; }
.footer-btn.ghost { flex: 1; background: #e5e7eb; color: #111827; }
.footer-btn.primary { flex: 1; background: #ea580c; color: #ffffff; }
</style>
