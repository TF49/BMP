<template>
  <PresidentLayout :showTabBar="false">
    <view class="equipment-detail-page">
      <view class="status-bar-placeholder" />

      <view class="top-shell">
        <view class="brand-left">
          <image class="president-avatar" :src="presidentAvatar" mode="aspectFill" />
          <text class="brand-title">Kinetic Logic</text>
        </view>
        <view class="notify-btn">
          <uni-icons type="notification" size="20" color="#ea580c" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="breadcrumb" @click="onBack">
          <uni-icons type="arrow-left" size="18" color="#ff6600" />
          <text>返回列表 / 器材详情</text>
        </view>

        <view v-if="loading && !equipment" class="state-wrap">
          <view class="spinner" />
          <text>加载器材详情中…</text>
        </view>

        <view v-else-if="!equipment" class="state-wrap">
          <text>器材不存在或已删除</text>
        </view>

        <template v-else>
          <view class="hero-img-wrap">
            <image class="hero-img" :src="activeImage" mode="aspectFill" />
            <view class="hero-tag">{{ levelTag }}</view>
          </view>

          <view class="thumb-row">
            <view
              class="thumb-item"
              :class="{ active: idx === activeImageIndex }"
              v-for="(img, idx) in images"
              :key="`${img}-${idx}`"
              @click="activeImageIndex = idx"
            >
              <image class="thumb-img" :src="img" mode="aspectFill" />
            </view>
            <view class="thumb-more">
              <uni-icons type="image" size="22" color="#71717a" />
              <text>更多</text>
            </view>
          </view>

          <view class="stats-grid">
            <view class="stat-card left">
              <view class="stat-head">
                <uni-icons type="bars" size="18" color="#a33e00" />
                <text>最近30天</text>
              </view>
              <text class="stat-val">128</text>
              <text class="stat-label">累计租借</text>
            </view>
            <view class="stat-card right">
              <view class="stat-head">
                <uni-icons type="info" size="18" color="#0062a1" />
                <text>平均时长</text>
              </view>
              <text class="stat-val">2.4h</text>
              <text class="stat-label">每次租借</text>
            </view>
          </view>

          <view class="name-block">
            <text class="name">{{ equipment.equipmentName || '未命名器材' }}</text>
            <view class="chips">
              <text class="chip">品牌: {{ equipment.brand || '-' }}</text>
              <text class="chip orange">系列: {{ equipment.equipmentType || '-' }}</text>
            </view>
          </view>

          <view class="price-card">
            <view>
              <text class="price-label">市场估值</text>
              <text class="market-price">¥{{ money(equipment.price) }}</text>
            </view>
            <view class="rent-side">
              <text class="price-label">租借价格</text>
              <text class="rent-price">¥{{ money(equipment.rentalPrice) }} <text class="small">/小时</text></text>
            </view>
          </view>

          <view class="inventory-card">
            <view class="inv-head">
              <text class="inv-title">库存状态 (Inventory)</text>
              <view class="inv-state" :class="{ low: stockPercent < 30 }">
                <view class="dot" />
                <text>{{ stockPercent > 0 ? '有货' : '缺货' }}</text>
              </view>
            </view>

            <view class="inv-main">
              <view class="inv-row">
                <text class="inv-total">{{ equipment.availableQuantity }} <text class="slash">/ {{ equipment.totalQuantity }}</text></text>
                <text class="inv-pct">{{ stockPercent }}%</text>
              </view>
              <view class="track">
                <view class="fill" :style="{ width: `${stockPercent}%` }" />
              </view>
              <view class="inv-legend">
                <text>可租借数量</text>
                <text>总资产池</text>
              </view>
            </view>

            <view class="inv-bottom">
              <view>
                <text class="k">维护中</text>
                <text class="v">3 Units</text>
              </view>
              <view>
                <text class="k">损坏/丢失</text>
                <text class="v">4 Units</text>
              </view>
            </view>
          </view>

          <view class="actions">
            <view class="primary-action">{{ levelTag }}</view>
            <view class="action-grid">
              <view class="secondary edit" @click="onEdit">
                <uni-icons type="compose" size="16" color="#0062a1" />
                <text>编辑</text>
              </view>
              <view class="secondary delete" @click="onDelete">
                <uni-icons type="trash" size="16" color="#ba1a1a" />
                <text>删除</text>
              </view>
            </view>
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
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const loading = ref(true)
const equipment = ref<EquipmentItem | null>(null)
const equipmentId = ref(0)
const activeImageIndex = ref(0)

const defaultImage =
  '/static/placeholders/hero.svg'
const presidentAvatar = computed(() => userStore.userInfo?.avatar || '/static/placeholders/avatar.svg')

const images = computed(() => {
  const img = (equipment.value as any)?.image || defaultImage
  return [img, img, img]
})
const activeImage = computed(() => images.value[activeImageIndex.value] || defaultImage)

const stockPercent = computed(() => {
  const total = Number(equipment.value?.totalQuantity || 0)
  const available = Number(equipment.value?.availableQuantity || 0)
  if (total <= 0) return 0
  return Math.max(0, Math.min(100, Math.round((available / total) * 100)))
})

const levelTag = computed(() => {
  const type = (equipment.value?.equipmentType || '').toLowerCase()
  if (type.includes('pro') || type.includes('专业')) return '专业级'
  return '标准级'
})

function money(v?: number) {
  if (v == null || Number.isNaN(Number(v))) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_LIST)
}

function onEdit() {
  if (!equipmentId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_FORM}?id=${equipmentId.value}` })
}

function onDelete() {
  uni.showToast({ title: '删除器材功能开发中', icon: 'none' })
}

function applyQueryFallback(q?: Record<string, string | undefined>) {
  equipment.value = {
    id: Number(q?.id || 0),
    equipmentCode: decodeURIComponent(q?.equipmentCode || '-'),
    equipmentName: decodeURIComponent(q?.equipmentName || '未命名器材'),
    equipmentType: decodeURIComponent(q?.equipmentType || '标准'),
    brand: decodeURIComponent(q?.brand || 'UNKNOWN'),
    price: Number(q?.price || 0),
    rentalPrice: Number(q?.rentalPrice || 0),
    totalQuantity: Number(q?.totalQuantity || 0),
    availableQuantity: Number(q?.availableQuantity || 0),
    status: Number(q?.status || 1),
    description: decodeURIComponent(q?.description || ''),
    createTime: ''
  }
}

onLoad(async (q?: Record<string, string | undefined>) => {
  const rawId = q?.id
  const id = rawId ? parseInt(String(rawId), 10) : NaN
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: '缺少器材参数', icon: 'none' })
    setTimeout(() => onBack(), 800)
    return
  }
  equipmentId.value = id
  loading.value = true
  try {
    equipment.value = await getEquipmentDetail(id)
  } catch {
    applyQueryFallback(q)
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.equipment-detail-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-shell {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16rpx 24rpx 8rpx;
}
.brand-left { display: flex; align-items: center; gap: 12rpx; }
.president-avatar { width: 48rpx; height: 48rpx; border-radius: 9999px; }
.brand-title { font-size: 34rpx; color: #ea580c; font-weight: 800; }
.notify-btn {
  width: 52rpx; height: 52rpx; border-radius: 9999px;
  display: flex; align-items: center; justify-content: center;
}
.scroll { height: calc(100vh - var(--status-bar-height) - 86rpx); padding: 12rpx 24rpx 0; box-sizing: border-box; }
.breadcrumb { display: flex; align-items: center; gap: 8rpx; color: #5f5e5e; font-size: 20rpx; font-weight: 700; margin: 6rpx 0 16rpx; }
.state-wrap {
  min-height: 360rpx; display: flex; flex-direction: column; justify-content: center; align-items: center;
  gap: 14rpx; color: #71717a;
}
.spinner {
  width: 42rpx; height: 42rpx; border-radius: 9999px; border: 4rpx solid #e5e7eb; border-top-color: #ea580c;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.hero-img-wrap {
  position: relative; border-radius: 24rpx; overflow: hidden; background: #f1f5f9; aspect-ratio: 16/10;
}
.hero-img { width: 100%; height: 100%; }
.hero-tag {
  position: absolute; left: 14rpx; top: 14rpx; background: #ff6600; color: #561d00; font-size: 18rpx; font-weight: 800;
  padding: 8rpx 14rpx; border-radius: 9999px;
}
.thumb-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12rpx; margin-top: 14rpx; }
.thumb-item { border-radius: 14rpx; overflow: hidden; aspect-ratio: 1; border: 2rpx solid transparent; background: #e5e7eb; }
.thumb-item.active { border-color: #ea580c; }
.thumb-img { width: 100%; height: 100%; }
.thumb-more {
  border-radius: 14rpx; background: #e5e7eb; aspect-ratio: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: #71717a;
}
.thumb-more text { font-size: 16rpx; font-weight: 700; margin-top: 2rpx; }
.stats-grid { margin-top: 20rpx; display: grid; grid-template-columns: 1fr 1fr; gap: 12rpx; }
.stat-card {
  background: #fff; border-radius: 18rpx; padding: 18rpx; box-shadow: 0 4rpx 14rpx rgba(2, 6, 23, 0.04);
}
.stat-card.left { border-left: 6rpx solid #a33e00; }
.stat-card.right { border-left: 6rpx solid #0062a1; }
.stat-head { display: flex; align-items: center; justify-content: space-between; color: #71717a; font-size: 16rpx; font-weight: 700; }
.stat-val { display: block; margin-top: 12rpx; font-size: 48rpx; font-weight: 900; }
.stat-label { display: block; margin-top: 4rpx; color: #5f5e5e; font-size: 18rpx; }
.name-block { margin-top: 22rpx; }
.name { display: block; font-size: 56rpx; font-weight: 900; letter-spacing: -0.03em; }
.chips { display: flex; gap: 8rpx; margin-top: 10rpx; flex-wrap: wrap; }
.chip {
  background: #e2dfde; color: #474746; font-size: 16rpx; font-weight: 800; padding: 6rpx 10rpx; border-radius: 9999px;
}
.chip.orange { background: #ffedd5; color: #9a3412; }
.price-card, .inventory-card {
  margin-top: 18rpx; background: #fff; border-radius: 20rpx; padding: 20rpx; box-shadow: 0 4rpx 14rpx rgba(2, 6, 23, 0.04);
}
.price-card { display: flex; justify-content: space-between; align-items: flex-end; }
.price-label { display: block; color: #71717a; font-size: 16rpx; font-weight: 800; margin-bottom: 4rpx; }
.market-price { font-size: 60rpx; color: #a33e00; font-weight: 900; letter-spacing: -0.03em; }
.rent-side { text-align: right; }
.rent-price { font-size: 34rpx; font-weight: 900; }
.small { font-size: 20rpx; color: #71717a; font-weight: 600; }
.inv-head { display: flex; justify-content: space-between; align-items: center; }
.inv-title { font-size: 28rpx; font-weight: 800; }
.inv-state { display: flex; align-items: center; gap: 6rpx; color: #16a34a; font-size: 16rpx; font-weight: 800; }
.inv-state.low { color: #ea580c; }
.dot { width: 10rpx; height: 10rpx; border-radius: 9999px; background: currentColor; }
.inv-main { margin-top: 16rpx; }
.inv-row { display: flex; align-items: baseline; justify-content: space-between; }
.inv-total { font-size: 46rpx; font-weight: 900; }
.slash { font-size: 28rpx; color: #71717a; font-weight: 700; }
.inv-pct { font-size: 24rpx; font-weight: 800; color: #a33e00; }
.track { margin-top: 10rpx; height: 12rpx; background: #e5e7eb; border-radius: 9999px; overflow: hidden; }
.fill { height: 100%; background: #ff6600; border-radius: 9999px; }
.inv-legend { margin-top: 6rpx; display: flex; justify-content: space-between; color: #71717a; font-size: 14rpx; font-weight: 700; }
.inv-bottom { margin-top: 16rpx; padding-top: 12rpx; border-top: 1rpx solid #e4c9bc; display: grid; grid-template-columns: 1fr 1fr; }
.k { display: block; font-size: 16rpx; color: #71717a; font-weight: 800; }
.v { display: block; margin-top: 4rpx; font-size: 28rpx; font-weight: 800; }
.actions { margin-top: 18rpx; }
.primary-action {
  height: 80rpx; border-radius: 16rpx; background: #ff6600; color: #561d00; font-size: 30rpx; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
}
.action-grid { margin-top: 10rpx; display: grid; grid-template-columns: 1fr 1fr; gap: 10rpx; }
.secondary {
  height: 72rpx; border-radius: 14rpx; background: #fff; display: flex; align-items: center; justify-content: center; gap: 8rpx;
  font-size: 24rpx; font-weight: 800;
}
.secondary.delete { color: #991b1b; }
.bottom-space { height: 36rpx; }
</style>
