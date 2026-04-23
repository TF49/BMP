<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="header-left" @tap="goProfile">
          <image class="avatar" :src="avatarUrl" mode="aspectFill" />
          <text class="brand-title">Kinetic Logic</text>
        </view>
        <view class="notify-btn" @tap="goNotice">
          <uni-icons type="notification-filled" size="20" color="#ff6600" />
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="content">
        <view class="back-row" @tap="handleBack">
          <uni-icons type="left" size="18" color="#a33e00" />
          <text>返回列表 / 器材详情</text>
        </view>

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载器材详情…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadEquipmentDetail">重新加载</view>
        </view>

        <template v-else-if="detail">
          <view class="hero-card">
            <image class="hero-image" :src="detail.heroImage" mode="aspectFill" />
            <view class="hero-badge">{{ detail.levelTag }}</view>
          </view>

          <view class="thumb-grid">
            <view
              v-for="(item, index) in detail.gallery"
              :key="`${index}-${item}`"
              class="thumb-item"
              :class="{ active: selectedImageIndex === index }"
              @tap="selectedImageIndex = index"
            >
              <image class="thumb-image" :src="item" mode="aspectFill" />
            </view>
            <view class="thumb-more">
              <uni-icons type="image" size="28" color="#7a7a7a" />
              <text>更多</text>
            </view>
          </view>

          <view class="stats-grid">
            <view class="stat-card stat-orange">
              <view class="stat-head">
                <uni-icons type="bars" size="18" color="#a33e00" />
                <text>器材类型</text>
              </view>
              <text class="stat-value">{{ detail.typeText }}</text>
              <text class="stat-label">当前真实分类</text>
            </view>

            <view class="stat-card stat-blue">
              <view class="stat-head">
                <uni-icons type="clock-filled" size="18" color="#0062a1" />
                <text>所属场馆</text>
              </view>
              <text class="stat-value">{{ detail.venueText }}</text>
              <text class="stat-label">按真实资料展示</text>
            </view>
          </view>

          <view class="title-block">
            <text class="equipment-name">{{ detail.name }}</text>
            <view class="badge-row">
              <text class="small-badge">品牌: {{ detail.brand }}</text>
              <text class="small-badge accent">型号: {{ detail.modelText }}</text>
            </view>
          </view>

          <view class="price-card">
            <view>
              <text class="price-caption">资产价格</text>
              <text class="market-price">¥{{ detail.assetPrice }}</text>
            </view>
            <view class="rent-price-box">
              <text class="price-caption">租借价格</text>
              <view class="rent-price-row">
                <text class="rent-price">¥{{ detail.rentalPrice }}</text>
                <text class="rent-unit">/小时</text>
              </view>
            </view>
          </view>

          <view class="inventory-card">
            <view class="inventory-head">
              <text class="inventory-title">库存状态 (Inventory)</text>
              <view class="stock-indicator" :class="{ low: !detail.inStock }">
                <text class="dot" />
                <text>{{ detail.inStock ? '有货' : '缺货' }}</text>
              </view>
            </view>

            <view class="inventory-main">
              <view class="inventory-numbers">
                <text class="inventory-count">{{ detail.availableQuantity }}</text>
                <text class="inventory-total">/ {{ detail.totalQuantity }}</text>
              </view>
              <text class="inventory-percent">{{ detail.stockPercent }}%</text>
            </view>

            <view class="progress-track">
              <view class="progress-fill" :style="{ width: `${detail.stockPercent}%` }" />
            </view>

            <view class="inventory-foot">
              <text>可租借数量</text>
              <text>总资产池</text>
            </view>
          </view>

          <view class="action-block">
            <view class="primary-btn" :class="{ disabled: !detail.canRent }" @tap="handleRent">
              <text>{{ detail.canRent ? '立即租借' : '当前不可租借' }}</text>
            </view>
            <text class="action-note">当前页面仅支持查看器材详情与发起真实租借，不再保留收藏、分享等未落地动作。</text>
          </view>
        </template>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getEquipmentDetail, type EquipmentItem } from '@/api/equipment'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { getAvatarImage } from '@/utils/displayImage'

type EquipmentDetailVm = {
  id: number
  name: string
  brand: string
  modelText: string
  typeText: string
  venueText: string
  levelTag: string
  heroImage: string
  gallery: string[]
  assetPrice: string
  rentalPrice: string
  availableQuantity: number
  totalQuantity: number
  stockPercent: number
  inStock: boolean
  canRent: boolean
}

const userStore = useUserStore()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const refreshing = ref(false)
const loading = ref(true)
const errorText = ref('')
const equipmentId = ref(0)
const equipment = ref<EquipmentItem | null>(null)
const selectedImageIndex = ref(0)

const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))

function formatMoney(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function resolveTypeLabel(type?: string) {
  const map: Record<string, string> = {
    RACKET: '球拍',
    SHUTTLE: '羽毛球',
    STRING: '球线',
    OTHER: '其他器材'
  }
  return map[String(type || '').toUpperCase()] || type || '待补充'
}

function buildGallery(item: EquipmentItem) {
  const cover = resolveImageUrl(item.equipmentImage) || '/static/placeholders/hero.svg'
  const extra = (item.images || []).map((image) => resolveImageUrl(image)).filter(Boolean) as string[]
  const base = [cover, ...extra]
  while (base.length < 3) {
    base.push(cover)
  }
  return base.slice(0, 3)
}

const detail = computed<EquipmentDetailVm | null>(() => {
  if (!equipment.value) return null

  const gallery = buildGallery(equipment.value)
  const currentHero = gallery[selectedImageIndex.value] || gallery[0]
  const totalQuantity = Math.max(0, Number(equipment.value.totalQuantity || equipment.value.quantity || 0))
  const availableQuantity = Math.max(0, Number(equipment.value.availableQuantity || equipment.value.quantity || 0))
  const stockPercent = totalQuantity > 0 ? Math.min(100, Math.round((availableQuantity / totalQuantity) * 100)) : 0
  const rentalPriceNum = Number(equipment.value.rentalPrice || equipment.value.price || 0)
  const assetPriceNum = Number(equipment.value.price || equipment.value.rentalPrice || 0)

  return {
    id: equipment.value.id,
    name: equipment.value.equipmentName || '器材详情',
    brand: (equipment.value.brand || '待补充').toUpperCase(),
    modelText: equipment.value.model?.trim() || '待补充',
    typeText: resolveTypeLabel(equipment.value.equipmentType),
    venueText: equipment.value.venueName?.trim() || '待补充',
    levelTag: resolveTypeLabel(equipment.value.equipmentType),
    heroImage: currentHero,
    gallery,
    assetPrice: formatMoney(assetPriceNum),
    rentalPrice: formatMoney(rentalPriceNum),
    availableQuantity,
    totalQuantity,
    stockPercent,
    inStock: availableQuantity > 0,
    canRent: availableQuantity > 0 && rentalPriceNum > 0
  }
})

async function loadEquipmentDetail() {
  if (!equipmentId.value) {
    loading.value = false
    errorText.value = '缺少器材参数'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    equipment.value = await getEquipmentDetail(equipmentId.value)
    selectedImageIndex.value = 0
  } catch (error) {
    console.error('加载器材详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载器材详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/equipment/list')
}

function goProfile() {
  uni.navigateTo({
    url: '/pages/profile/index'
  })
}

function goNotice() {
  uni.navigateTo({
    url: '/pages/notice/index'
  })
}

function handleRent() {
  if (!detail.value) return
  if (!detail.value.canRent) {
    uni.showToast({
      title: '当前器材暂不可租借',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages/equipment/rental?id=${detail.value.id}&quantity=1`
  })
}

function handleRefresh() {
  refreshing.value = true
  loadEquipmentDetail().finally(() => {
    refreshing.value = false
  })
}

onLoad(async (options?: Record<string, string | undefined>) => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  equipmentId.value = Number(options?.id || 0)
  await loadEquipmentDetail()
})

onPullDownRefresh(() => {
  loadEquipmentDetail().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: #fafafa;
}

.header-inner {
  min-height: 112rpx;
  padding: 12rpx 28rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 9999rpx;
  background: #e8e8e8;
}

.brand-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #ff6600;
  letter-spacing: -1rpx;
}

.notify-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 20rpx 18rpx 120rpx;
}

.back-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 26rpx;
  color: #5f5e5e;
  font-size: 24rpx;
  font-weight: 700;
}

.hero-card,
.stat-card,
.price-card,
.inventory-card,
.state-card {
  background: #ffffff;
  box-shadow: 0 12rpx 36rpx rgba(26, 28, 28, 0.04);
}

.hero-card {
  position: relative;
  height: 430rpx;
  border-radius: 28rpx;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-badge {
  position: absolute;
  left: 24rpx;
  top: 24rpx;
  min-width: 130rpx;
  height: 52rpx;
  padding: 0 18rpx;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #561d00;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 900;
}

.thumb-grid {
  margin-top: 20rpx;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14rpx;
}

.thumb-item,
.thumb-more {
  aspect-ratio: 1;
  border-radius: 20rpx;
  overflow: hidden;
  background: #e8e8e8;
}

.thumb-item.active {
  border: 2rpx solid #ff6600;
}

.thumb-image {
  width: 100%;
  height: 100%;
}

.thumb-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #5f5e5e;
  font-size: 18rpx;
  font-weight: 700;
  gap: 6rpx;
}

.stats-grid {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.stat-card {
  border-radius: 28rpx;
  padding: 24rpx 22rpx 26rpx;
  border-left: 6rpx solid #a33e00;
}

.stat-blue {
  border-left-color: #0062a1;
}

.stat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 18rpx;
  color: #5f5e5e;
  font-weight: 800;
}

.stat-value {
  display: block;
  margin-top: 22rpx;
  font-size: 58rpx;
  line-height: 1;
  font-weight: 900;
  color: #1a1c1c;
}

.stat-label {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}

.title-block {
  margin-top: 26rpx;
}

.equipment-name {
  display: block;
  font-size: 66rpx;
  line-height: 1.05;
  font-weight: 900;
  letter-spacing: -2rpx;
  color: #111111;
}

.badge-row {
  margin-top: 18rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.small-badge {
  min-height: 40rpx;
  padding: 0 16rpx;
  border-radius: 9999rpx;
  background: #e2dfde;
  color: #636262;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.small-badge.accent {
  background: #ffe8d8;
  color: #a33e00;
}

.price-card {
  margin-top: 26rpx;
  border-radius: 34rpx;
  padding: 30rpx 28rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 22rpx;
}

.price-caption {
  display: block;
  font-size: 18rpx;
  color: #5f5e5e;
  font-weight: 800;
  letter-spacing: 1rpx;
}

.market-price {
  display: block;
  margin-top: 14rpx;
  font-size: 68rpx;
  line-height: 1;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: -2rpx;
}

.rent-price-box {
  text-align: right;
}

.rent-price-row {
  margin-top: 12rpx;
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  gap: 6rpx;
}

.rent-price {
  font-size: 50rpx;
  line-height: 1;
  font-weight: 900;
  color: #111111;
}

.rent-unit {
  font-size: 26rpx;
  color: #5f5e5e;
}

.inventory-card {
  margin-top: 26rpx;
  border-radius: 34rpx;
  padding: 30rpx 28rpx;
}

.inventory-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.inventory-title {
  font-size: 42rpx;
  font-weight: 900;
  color: #111111;
}

.stock-indicator {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #16a34a;
  font-size: 24rpx;
  font-weight: 800;
}

.stock-indicator.low {
  color: #ba1a1a;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 9999rpx;
  background: currentColor;
}

.inventory-main {
  margin-top: 30rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.inventory-numbers {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.inventory-count {
  font-size: 62rpx;
  line-height: 1;
  font-weight: 900;
}

.inventory-total {
  font-size: 42rpx;
  color: #5f5e5e;
  font-weight: 700;
}

.inventory-percent {
  font-size: 34rpx;
  font-weight: 900;
  color: #a33e00;
}

.progress-track {
  margin-top: 22rpx;
  height: 16rpx;
  border-radius: 9999rpx;
  background: #e2e2e2;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 9999rpx;
  background: linear-gradient(90deg, #ff6600 0%, #ff7f2f 100%);
  box-shadow: 0 0 18rpx rgba(255, 102, 0, 0.25);
}

.inventory-foot {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 18rpx;
  color: #5f5e5e;
  font-weight: 700;
}

.action-block {
  margin-top: 28rpx;
}

.primary-btn {
  width: 100%;
  height: 100rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #ff6600 0%, #ff7f2f 100%);
  color: #561d00;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 900;
  box-shadow: 0 12rpx 30rpx rgba(255, 102, 0, 0.2);
}

.primary-btn.disabled {
  background: #d7d7d7;
  color: #8c8c8c;
  box-shadow: none;
}

.action-note {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b7280;
}

.state-card {
  margin-top: 24rpx;
  border-radius: 28rpx;
  padding: 90rpx 28rpx;
  text-align: center;
}

.state-text {
  font-size: 28rpx;
  color: #777777;
}

.state-action {
  width: 220rpx;
  height: 76rpx;
  margin: 22rpx auto 0;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  margin: 0 auto 18rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
