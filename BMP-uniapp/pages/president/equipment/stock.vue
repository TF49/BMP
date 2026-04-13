<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-inner">
          <view class="top-left" @click="goBack">
            <view class="icon-btn">
              <uni-icons type="arrow-left" size="22" color="#ea580c" />
            </view>
            <text class="title">库存调整</text>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>正在加载库存信息...</text>
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
              <text class="label">当前总库存</text>
              <text class="value">{{ equipment.totalQuantity || 0 }}</text>
            </view>
            <view class="info-card">
              <text class="label">当前可用库存</text>
              <text class="value">{{ equipment.availableQuantity || 0 }}</text>
            </view>
            <view class="info-card">
              <text class="label">占用库存</text>
              <text class="value">{{ occupiedCount }}</text>
            </view>
            <view class="info-card">
              <text class="label">可用率</text>
              <text class="value">{{ stockPercent }}%</text>
            </view>
          </view>

          <view class="panel">
            <text class="panel-title">手动调整</text>

            <view class="field">
              <text class="field-label">总库存</text>
              <input class="input" v-model="form.totalQuantity" type="number" placeholder="请输入总库存" />
            </view>

            <view class="field">
              <text class="field-label">可用库存</text>
              <input class="input" v-model="form.availableQuantity" type="number" placeholder="请输入可用库存" />
            </view>

            <view class="field">
              <text class="field-label">器材状态</text>
              <picker mode="selector" :range="statusLabels" :value="statusIndex" @change="onStatusChange">
                <view class="input picker-row">
                  <text>{{ statusLabels[statusIndex] }}</text>
                  <uni-icons type="arrowdown" size="16" color="#71717a" />
                </view>
              </picker>
            </view>

            <text class="panel-tip">
              当前页面通过现有器材更新接口回写库存数据，可用于手动修正总库存、可用库存与器材状态。
            </text>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>

      <view v-if="equipment" class="footer">
        <view class="footer-btn ghost" @click="loadData">刷新</view>
        <view class="footer-btn primary" :class="{ disabled: submitting }" @click="submitStockUpdate">
          {{ submitting ? '保存中...' : '保存库存' }}
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getEquipmentDetail, updateEquipment, type EquipmentItem } from '@/api/equipment'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getEquipmentStatusMeta } from '@/utils/presidentStatus'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

const STATUS_OPTIONS = [
  { label: '停用', value: 0 },
  { label: '启用', value: 1 },
  { label: '维护中', value: 2 }
] as const

const equipmentId = ref(0)
const loading = ref(true)
const submitting = ref(false)
const errorText = ref('')
const equipment = ref<(EquipmentItem & { equipmentImage?: string }) | null>(null)

const form = reactive({
  totalQuantity: '',
  availableQuantity: '',
  status: 1
})

const statusLabels = STATUS_OPTIONS.map(item => item.label)
const statusIndex = computed(() => {
  const index = STATUS_OPTIONS.findIndex(item => item.value === form.status)
  return index >= 0 ? index : 1
})
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

function onStatusChange(e: { detail: { value: string } }) {
  const next = STATUS_OPTIONS[Number(e.detail.value)]
  if (!next) return
  form.status = next.value
}

function syncForm(info: EquipmentItem & { equipmentImage?: string }) {
  form.totalQuantity = String(info.totalQuantity ?? 0)
  form.availableQuantity = String(info.availableQuantity ?? 0)
  form.status = Number(info.status ?? 1)
}

async function loadData() {
  if (!equipmentId.value) return
  loading.value = true
  errorText.value = ''
  equipment.value = null

  try {
    const info = await getEquipmentDetail(equipmentId.value)
    equipment.value = info
    syncForm(info)
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function validateForm() {
  const totalQuantity = Number(form.totalQuantity)
  const availableQuantity = Number(form.availableQuantity)

  if (!Number.isInteger(totalQuantity) || totalQuantity < 0) {
    return '总库存必须是大于等于 0 的整数'
  }
  if (!Number.isInteger(availableQuantity) || availableQuantity < 0) {
    return '可用库存必须是大于等于 0 的整数'
  }
  if (availableQuantity > totalQuantity) {
    return '可用库存不能大于总库存'
  }
  return ''
}

async function submitStockUpdate() {
  if (!equipment.value || submitting.value) return

  const validationMessage = validateForm()
  if (validationMessage) {
    uni.showToast({ title: validationMessage, icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await updateEquipment({
      id: equipment.value.id,
      equipmentCode: equipment.value.equipmentCode,
      equipmentImage: equipment.value.equipmentImage,
      equipmentName: equipment.value.equipmentName,
      equipmentType: equipment.value.equipmentType as 'RACKET' | 'SHUTTLE' | 'STRING' | 'OTHER',
      brand: equipment.value.brand || '',
      price: Number(equipment.value.price || 0),
      rentalPrice: Number(equipment.value.rentalPrice || 0),
      totalQuantity: Number(form.totalQuantity),
      availableQuantity: Number(form.availableQuantity),
      status: form.status,
      description: equipment.value.description || ''
    })

    uni.showToast({ title: '库存更新成功', icon: 'success' })
    await loadData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '库存更新失败'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    submitting.value = false
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
.page {
  min-height: 100vh;
  background: #f8fafc;
  color: #111827;
  padding-bottom: 132rpx;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f8fafc;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 40;
  background: rgba(248, 250, 252, 0.92);
  backdrop-filter: blur(12px);
}

.top-inner {
  padding: 16rpx 24rpx;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 38rpx;
  font-weight: 800;
}

.scroll {
  height: calc(100vh - var(--status-bar-height) - 108rpx - 132rpx);
  padding: 0 24rpx;
  box-sizing: border-box;
}

.state-wrap {
  min-height: 420rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  color: #6b7280;
}

.spinner {
  width: 44rpx;
  height: 44rpx;
  border: 4rpx solid #e5e7eb;
  border-top-color: #ea580c;
  border-radius: 9999px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.retry-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  padding: 0 32rpx;
  border-radius: 16rpx;
  background: #ea580c;
  color: #ffffff;
  font-weight: 700;
}

.hero-card {
  display: flex;
  gap: 18rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
  margin-top: 12rpx;
}

.thumb {
  width: 132rpx;
  height: 132rpx;
  border-radius: 24rpx;
  background: #e5e7eb;
  flex-shrink: 0;
}

.hero-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  justify-content: center;
}

.name {
  font-size: 32rpx;
  font-weight: 800;
}

.sub {
  font-size: 22rpx;
  color: #6b7280;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  padding: 8rpx 16rpx;
  border-radius: 9999px;
  font-size: 18rpx;
  font-weight: 800;
}

.status-pill.available,
.status-pill.normal {
  background: #dcfce7;
  color: #166534;
}

.status-pill.out {
  background: #fee2e2;
  color: #b91c1c;
}

.status-pill.disabled {
  background: #e5e7eb;
  color: #4b5563;
}

.status-pill.maintenance {
  background: #fef3c7;
  color: #b45309;
}

.grid {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.info-card,
.panel {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 22rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.label {
  display: block;
  font-size: 20rpx;
  color: #6b7280;
  margin-bottom: 8rpx;
}

.value {
  font-size: 26rpx;
  font-weight: 800;
}

.panel {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fffaf5 100%);
}

.panel-title {
  font-size: 28rpx;
  font-weight: 800;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.field-label {
  font-size: 22rpx;
  font-weight: 700;
  color: #374151;
}

.input {
  min-height: 88rpx;
  border-radius: 18rpx;
  background: #f3f4f6;
  padding: 0 24rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
}

.picker-row {
  justify-content: space-between;
}

.panel-tip {
  font-size: 22rpx;
  color: #6b7280;
  line-height: 1.7;
}

.bottom-space {
  height: 36rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 12rpx;
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
}

.footer-btn {
  height: 88rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
}

.footer-btn.ghost {
  background: #e5e7eb;
  color: #111827;
}

.footer-btn.primary {
  background: #ea580c;
  color: #ffffff;
}

.footer-btn.disabled {
  opacity: 0.6;
  pointer-events: none;
}
</style>
