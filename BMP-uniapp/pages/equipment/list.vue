<template>
  <view class="page">
    <!-- Top bar -->
    <view class="top-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="top-inner">
        <view class="brand-row" @tap="goProfile">
          <image class="avatar" :src="avatarUrl" mode="aspectFill" />
          <text class="brand">KINETIC LOGIC</text>
        </view>
        <view class="notify-btn" @tap="goNotice">
          <uni-icons type="notification" size="22" color="#a33e00" />
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main" :style="{ paddingTop: topOffset + 'px' }" :show-scrollbar="false">
      <view class="inner">
        <!-- Title + pills row -->
        <view class="hero-block">
          <view class="hero-text">
            <text class="h1">器材租借</text>
            <text class="sub">高品质专业装备，助你赛场表现更出色。</text>
          </view>
          <scroll-view class="pills-scroll" scroll-x :show-scrollbar="false">
            <view class="pills">
              <view
                v-for="(tab, i) in categoryTabs"
                :key="tab.key"
                class="pill"
                :class="{ active: selectedCategory === i }"
                @tap="selectCategory(i)"
              >
                <text class="pill-t">{{ tab.label }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view v-if="loading && rows.length === 0" class="state">
          <text>加载中…</text>
        </view>

        <view v-else-if="rows.length === 0" class="state">
          <text>暂无器材</text>
        </view>

        <view v-else class="card-list">
          <view
            v-for="item in rows"
            :key="item.id"
            class="card"
            @tap="openDetail(item)"
          >
            <view class="media">
              <image
                v-if="item.imageUrl"
                class="media-img"
                :src="item.imageUrl"
                mode="aspectFill"
              />
              <view v-else class="media-fallback">
                <uni-icons type="image" size="56" color="#c8c6c5" />
              </view>
              <view class="media-gradient" />
              <view class="badge-wrap">
                <text class="badge" :class="item.badgeVariant">{{ item.badgeText }}</text>
              </view>
            </view>

            <view class="body">
              <view class="title-line">
                <text class="name">{{ item.name }}</text>
                <uni-icons
                  v-if="item.equipmentType === 'RACKET'"
                  type="loop"
                  size="22"
                  color="#9ca3af"
                />
              </view>
              <text class="spec">{{ item.specLine }}</text>

              <view class="footer-row">
                <view class="price-block">
                  <text class="price-label">{{ item.priceLabel }}</text>
                  <view class="price-line">
                    <text class="price-num" :class="{ rent: item.isRentable }">
                      ¥{{ item.displayPrice }}
                    </text>
                    <text class="price-unit">{{ item.unitSuffix }}</text>
                  </view>
                </view>
                <view
                  class="fab"
                  :class="item.isRentable ? 'fab-rent' : 'fab-buy'"
                  @tap.stop="onAction(item)"
                >
                  <uni-icons
                    :type="item.isRentable ? 'plusempty' : 'cart'"
                    size="24"
                    :color="item.isRentable ? '#561d00' : '#1a1c1c'"
                  />
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="bottom-spacer" />
      </view>
    </scroll-view>

    <CustomTabBar :current="1" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { useUserStore } from '@/store/modules/user'
import { getEquipmentList, type EquipmentItem } from '@/api/equipment'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { getAvatarImage } from '@/utils/displayImage'
import { EQUIPMENT_TYPE_TEXT } from '@/utils/constant'

const userStore = useUserStore()

const statusBarHeight = ref(44)
const loading = ref(false)
const selectedCategory = ref(0)

const categoryTabs = [
  { key: 'all', label: '全部 (All)', type: null as string | null },
  { key: 'racket', label: '球拍 (Rackets)', type: 'RACKET' },
  { key: 'shuttle', label: '羽毛球 (Shuttles)', type: 'SHUTTLE' },
  { key: 'acc', label: '配件 (Accessories)', type: 'OTHER' }
]

const rawList = ref<EquipmentItem[]>([])

const topOffset = computed(() => statusBarHeight.value + 52)
const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))

type RowVm = {
  id: number
  name: string
  equipmentType: string
  specLine: string
  imageUrl: string
  isRentable: boolean
  displayPrice: string
  priceLabel: string
  unitSuffix: string
  badgeText: string
  badgeVariant: 'low' | 'ok' | 'out'
  qty: number
}

function money(n: number) {
  const v = Number(n)
  if (!Number.isFinite(v)) return '0'
  return (Math.round(v * 100) / 100).toFixed(0)
}

function unitForType(type: string, rent: boolean) {
  if (!rent) {
    if (type === 'SHUTTLE') return '/筒'
    if (type === 'STRING') return '/次'
    return '/件'
  }
  return '/天'
}

function mapRow(eq: EquipmentItem): RowVm {
  const rp = Number(eq.rentalPrice ?? 0)
  const pp = Number(eq.price ?? 0)
  const isRentable = rp > 0
  const qty = eq.availableQuantity ?? eq.quantity ?? 0
  const spec =
    (eq as EquipmentItem & { specifications?: string }).specifications ||
    eq.model ||
    (eq.equipmentType ? (EQUIPMENT_TYPE_TEXT as Record<string, string>)[eq.equipmentType] || eq.equipmentType : '')

  let badgeText = '库存充足'
  let badgeVariant: RowVm['badgeVariant'] = 'ok'
  const unitWord = eq.equipmentType === 'RACKET' ? '把' : eq.equipmentType === 'SHUTTLE' ? '筒' : '件'
  if (qty <= 0) {
    badgeText = '暂时缺货'
    badgeVariant = 'out'
  } else if (qty <= 5) {
    badgeText = `仅剩${qty}${unitWord}`
    badgeVariant = 'low'
  }

  const display = isRentable ? rp : pp

  return {
    id: eq.id,
    name: eq.equipmentName,
    equipmentType: eq.equipmentType,
    specLine: spec || '专业运动器材',
    imageUrl: resolveImageUrl(eq.equipmentImage) || '',
    isRentable,
    displayPrice: money(display),
    priceLabel: isRentable ? '日租金' : '购买价',
    unitSuffix: unitForType(eq.equipmentType, isRentable),
    badgeText,
    badgeVariant,
    qty
  }
}

const rows = computed(() => rawList.value.map(mapRow))

async function loadEquipmentList() {
  loading.value = true
  try {
    const type = categoryTabs[selectedCategory.value]?.type
    const result = await getEquipmentList({
      page: 1,
      size: 50,
      status: 1,
      ...(type ? { equipmentType: type } : {})
    })
    rawList.value = result.data || []
  } catch (error) {
    console.error('加载器材列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function selectCategory(i: number) {
  selectedCategory.value = i
  void loadEquipmentList()
}

function openDetail(item: RowVm) {
  uni.navigateTo({ url: `/pages/equipment/detail?id=${item.id}` })
}

function onAction(item: RowVm) {
  if (item.qty <= 0) {
    uni.showToast({ title: '暂时无货', icon: 'none' })
    return
  }
  if (item.isRentable) {
    uni.navigateTo({ url: `/pages/equipment/rental?id=${item.id}&quantity=1` })
  } else {
    uni.showToast({ title: '购买请咨询场馆前台', icon: 'none' })
  }
}

function goNotice() {
  uni.navigateTo({ url: '/pages/notice/index' })
}

function goProfile() {
  uni.navigateTo({ url: '/pages/profile/index' })
}

onMounted(() => {
  try {
    const sys = getSafeSystemInfo()
    statusBarHeight.value = sys.statusBarHeight || 44
  } catch {
    statusBarHeight.value = 44
  }

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  void loadEquipmentList()
})

onPullDownRefresh(() => {
  loadEquipmentList().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f9f9f9;
  color: #1a1c1c;
}

.top-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background-color: #f4f4f5;
  box-shadow: 0 20rpx 80rpx rgba(0, 0, 0, 0.04);
}

.top-inner {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 48rpx 20rpx;
}

.brand-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 9999rpx;
  background-color: #e8e8e8;
  border: 1rpx solid rgba(227, 191, 177, 0.25);
}

.brand {
  font-size: 36rpx;
  font-weight: 700;
  font-style: italic;
  letter-spacing: -1rpx;
  color: #18181b;
}

.notify-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main {
  box-sizing: border-box;
  height: 100vh;
}

.inner {
  padding: 0 32rpx 32rpx;
}

.hero-block {
  margin-bottom: 40rpx;
}

.hero-text {
  margin-bottom: 32rpx;
}

.h1 {
  display: block;
  font-size: 72rpx;
  font-weight: 700;
  letter-spacing: -2rpx;
  line-height: 1.15;
  color: #1a1c1c;
}

.sub {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #5f5e5e;
  line-height: 1.5;
  max-width: 640rpx;
}

.pills-scroll {
  width: 100%;
  white-space: nowrap;
}

.pills {
  display: inline-flex;
  flex-direction: row;
  gap: 16rpx;
  padding-bottom: 8rpx;
}

.pill {
  display: inline-flex;
  padding: 20rpx 40rpx;
  border-radius: 9999rpx;
  background-color: #e8e8e8;
}

.pill.active {
  background-color: #a33e00;
}

.pill-t {
  font-size: 26rpx;
  font-weight: 500;
  color: #1a1c1c;
  white-space: nowrap;
}

.pill.active .pill-t {
  color: #ffffff;
}

.state {
  padding: 120rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #9ca3af;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 48rpx;
}

.card {
  background-color: #ffffff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 40rpx 80rpx rgba(26, 28, 28, 0.04);
}

.media {
  position: relative;
  height: 512rpx;
  width: 100%;
  background-color: #f3f3f3;
}

.media-img {
  width: 100%;
  height: 100%;
}

.media-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eeeeee 0%, #f3f3f3 50%, #e8e8e8 100%);
}

.media-gradient {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  top: 0;
  background: linear-gradient(to top, rgba(255, 255, 255, 0.95) 0%, transparent 55%);
  pointer-events: none;
}

.badge-wrap {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  z-index: 2;
}

.badge {
  font-size: 22rpx;
  font-weight: 700;
  padding: 12rpx 24rpx;
  border-radius: 9999rpx;
  letter-spacing: 0.5px;
}

.badge.low {
  background-color: rgba(249, 249, 249, 0.75);
  color: #a33e00;
}

.badge.ok {
  background-color: #e2dfde;
  color: #636262;
}

.badge.out {
  background-color: #fee2e2;
  color: #b91c1c;
}

.body {
  position: relative;
  margin-top: -32rpx;
  padding: 40rpx 48rpx 48rpx;
  background-color: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  z-index: 3;
}

.title-line {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.name {
  flex: 1;
  font-size: 40rpx;
  font-weight: 700;
  color: #1a1c1c;
  line-height: 1.25;
}

.spec {
  display: block;
  font-size: 22rpx;
  color: #5f5e5e;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  margin-bottom: 40rpx;
}

.footer-row {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: space-between;
}

.price-label {
  display: block;
  font-size: 24rpx;
  color: #5f5e5e;
  margin-bottom: 8rpx;
}

.price-line {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 8rpx;
}

.price-num {
  font-size: 48rpx;
  font-weight: 700;
  color: #1a1c1c;
  letter-spacing: -1rpx;
}

.price-num.rent {
  color: #a33e00;
}

.price-unit {
  font-size: 28rpx;
  color: #5f5e5e;
}

.fab {
  width: 96rpx;
  height: 96rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fab-rent {
  background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
  box-shadow: 0 8rpx 24rpx rgba(255, 102, 0, 0.25);
}

.fab-buy {
  background-color: #e8e8e8;
}

.bottom-spacer {
  height: 200rpx;
}
</style>
