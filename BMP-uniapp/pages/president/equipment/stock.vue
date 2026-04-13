<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">库存视图</text>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载库存信息中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="loadData">重试</view>
        </view>

        <view v-else-if="!equipment" class="state-wrap">
          <text>未找到器材信息</text>
        </view>

        <template v-else>
          <view class="hero-card">
            <image class="thumb" :src="imageUrl" mode="aspectFill" />
            <view class="hero-main">
              <text class="name">{{ equipment.equipmentName }}</text>
              <text class="sub">{{ equipment.equipmentCode || '-' }}</text>
              <view class="status-pill" :class="statusMeta.key">{{ statusMeta.label }}</view>
            </view>
          </view>

          <view class="grid">
            <view class="info-card">
              <text class="label">总库存</text>
              <text class="value">{{ equipment.totalQuantity || 0 }}</text>
            </view>
            <view class="info-card">
              <text class="label">可用库存</text>
              <text class="value">{{ equipment.availableQuantity || 0 }}</text>
            </view>
            <view class="info-card">
              <text class="label">占用库存</text>
              <text class="value">{{ occupiedCount }}</text>
            </view>
            <view class="info-card">
              <text class="label">库存可用率</text>
              <text class="value">{{ stockPercent }}%</text>
            </view>
          </view>

          <view class="panel">
            <text class="panel-title">当前说明</text>
            <text class="panel-content">
              当前版本暂未接入独立的库存变更接口，因此该页面仅展示真实库存信息，不执行入库、出库或批量调整操作。
            </text>
          </view>

          <view class="panel">
            <text class="panel-title">建议操作</text>
            <text class="panel-content">
              如需调整库存，请先在后台补充库存变更接口；当前可通过器材详情和列表页完成查看、筛选和基础编辑。
            </text>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>
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
const occupiedCount = computed(() => {
  const total = Number(equipment.value?.totalQuantity || 0)
  const available = Number(equipment.value?.availableQuantity || 0)
  return Math.max(0, total - available)
})
const stockPercent = computed(() => {
  const total = Number(equipment.value?.totalQuantity || 0)
  const available = Number(equipment.value?.availableQuantity || 0)
  return total > 0 ? Math.min(100, Math.round((available / total) * 100)) : 0
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_LIST)
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
.page { min-height: 100vh; background: #f8fafc; color: #111827; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #ffffff; display: flex; align-items: center; justify-content: center; }
.title { font-size: 38rpx; font-weight: 800; }
.scroll { height: calc(100vh - var(--status-bar-height) - 108rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.hero-card { display: flex; gap: 18rpx; padding: 24rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); }
.thumb { width: 132rpx; height: 132rpx; border-radius: 24rpx; background: #e5e7eb; flex-shrink: 0; }
.hero-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 10rpx; justify-content: center; }
.name { font-size: 32rpx; font-weight: 800; }
.sub { font-size: 22rpx; color: #6b7280; }
.status-pill { display: inline-flex; align-items: center; justify-content: center; width: fit-content; padding: 8rpx 16rpx; border-radius: 9999px; font-size: 18rpx; font-weight: 800; }
.status-pill.available, .status-pill.normal { background: #dcfce7; color: #166534; }
.status-pill.out { background: #fee2e2; color: #b91c1c; }
.status-pill.disabled { background: #e5e7eb; color: #4b5563; }
.status-pill.maintenance { background: #fef3c7; color: #b45309; }
.grid { margin-top: 18rpx; display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.info-card, .panel { background: #ffffff; border-radius: 24rpx; padding: 22rpx; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); }
.label { display: block; font-size: 20rpx; color: #6b7280; margin-bottom: 8rpx; }
.value { font-size: 26rpx; font-weight: 800; }
.panel { margin-top: 18rpx; display: flex; flex-direction: column; gap: 12rpx; }
.panel-title { font-size: 26rpx; font-weight: 800; }
.panel-content { font-size: 24rpx; color: #4b5563; line-height: 1.7; }
.bottom-space { height: 36rpx; }
</style>
