<template>
  <MobileLayout>
    <view class="page">
      <!-- Header -->
      <view class="header">
        <view class="icon-btn" @click="handleBack">
          <uni-icons type="left" size="22" color="#1a1c1c" />
        </view>
        <text class="header-title">租借确认</text>
        <view class="icon-btn placeholder" />
      </view>

      <scroll-view class="main" scroll-y :show-scrollbar="false">
        <!-- 商品摘要 -->
        <view class="card card-row">
          <image class="thumb" :src="coverUrl" mode="aspectFill" />
          <view class="summary-body">
            <view class="title-row">
              <text class="item-title">{{ equipment.equipmentName }}</text>
              <text class="tag">{{ typeTag }}</text>
            </view>
            <text class="subtitle">{{ subtitle }}</text>
            <view class="price-row">
              <text class="price-num">¥{{ formatMoney(hourlyPrice) }}</text>
              <text class="price-unit">/ 小时</text>
            </view>
          </view>
        </view>

        <!-- 数量 + 时段 -->
        <view class="card card-stack">
          <view class="row-between">
            <view>
              <text class="block-title">租借数量</text>
              <text class="hint">最多可借 {{ maxQuantity }} 把</text>
            </view>
            <view class="stepper">
              <view class="step-btn" @click="decreaseQuantity">
                <text class="step-symbol">−</text>
              </view>
              <text class="step-val">{{ rentQuantity }}</text>
              <view class="step-btn step-btn-strong" @click="increaseQuantity">
                <text class="step-symbol strong">+</text>
              </view>
            </view>
          </view>

          <view class="divider-gap" />

          <view class="slot-block">
            <view class="row-between">
              <text class="block-title">租借时段</text>
              <view class="edit-btn" @click="slotEditing = !slotEditing">
                <text class="edit-text">修改</text>
                <uni-icons type="compose" size="16" color="#a33e00" />
              </view>
            </view>

            <view v-if="slotEditing" class="picker-panel">
              <view class="picker-row">
                <text class="picker-label">开始日期</text>
                <picker mode="date" :value="startDate" @change="onStartDate">
                  <view class="picker-value">{{ startDate }}</view>
                </picker>
              </view>
              <view class="picker-row">
                <text class="picker-label">开始时间</text>
                <picker mode="time" :value="startTime" @change="onStartTime">
                  <view class="picker-value">{{ startTime }}</view>
                </picker>
              </view>
              <view class="picker-row">
                <text class="picker-label">结束日期</text>
                <picker mode="date" :value="endDate" @change="onEndDate">
                  <view class="picker-value">{{ endDate }}</view>
                </picker>
              </view>
              <view class="picker-row">
                <text class="picker-label">结束时间</text>
                <picker mode="time" :value="endTime" @change="onEndTime">
                  <view class="picker-value">{{ endTime }}</view>
                </picker>
              </view>
            </view>

            <view class="time-row">
              <view class="time-box time-box-start">
                <view class="accent-bar" />
                <text class="time-cap">提取时间 (START)</text>
                <text class="time-date">{{ startDateLabel }}</text>
                <text class="time-clock start">{{ startTime }}</text>
              </view>
              <view class="arrow-wrap">
                <uni-icons type="right" size="22" color="#5f5e5e" />
              </view>
              <view class="time-box">
                <text class="time-cap">归还时间 (END)</text>
                <text class="time-date">{{ endDateLabel }}</text>
                <text class="time-clock">{{ endTime }}</text>
              </view>
            </view>
            <text class="duration-hint">总时长: {{ durationHours }} 小时</text>
          </view>
        </view>

        <!-- 费用明细 -->
        <view class="card card-stack fee-card">
          <text class="block-title fee-title">费用明细</text>
          <view class="fee-line">
            <text class="fee-label">基础租金 (¥{{ formatMoney(hourlyPrice) }} × {{ durationHours }}小时 × {{ rentQuantity }}件)</text>
            <text class="fee-value">¥{{ formatMoney(baseRent) }}</text>
          </view>
          <view class="fee-line">
            <view class="fee-label-row" @click="onDepositInfo">
              <text class="fee-label">可退还押金</text>
              <uni-icons type="info" size="14" color="#5f5e5e" />
            </view>
            <text class="fee-value">¥{{ formatMoney(depositTotal) }}</text>
          </view>
          <view class="fee-line">
            <text class="fee-label">平台服务费</text>
            <text class="fee-value">¥{{ formatMoney(platformFee) }}</text>
          </view>
        </view>

        <view class="scroll-spacer" />
      </scroll-view>

      <!-- 底部栏 -->
      <view class="footer">
        <view class="footer-left">
          <text class="total-cap">应付总计 (含押金)</text>
          <view class="total-row">
            <text class="currency">¥</text>
            <text class="total-num">{{ formatMoney(totalPayable) }}</text>
          </view>
        </view>
        <button class="cta" :disabled="!canSubmit" @click="handleSubmit">立即租借</button>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MobileLayout from '@/components/MobileLayout.vue'
import { useUserStore } from '@/store/modules/user'
import { createEquipmentRental, getEquipmentDetail, type EquipmentItem } from '@/api/equipment'
import { safeNavigateBack } from '@/utils/navigation'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

const userStore = useUserStore()

const equipmentId = ref(0)
const equipment = ref<EquipmentItem>({
  id: 0,
  equipmentCode: '',
  equipmentName: '',
  equipmentType: 'OTHER',
  brand: '',
  price: 0,
  rentalPrice: 0,
  totalQuantity: 0,
  availableQuantity: 0,
  status: 1,
  description: '',
  createTime: '1970-01-01T00:00:00'
})

const rentQuantity = ref(1)
const slotEditing = ref(false)

const startDate = ref('')
const startTime = ref('14:00')
const endDate = ref('')
const endTime = ref('16:00')

const platformFee = ref(5)

const pad2 = (n: number) => (n < 10 ? `0${n}` : `${n}`)

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

const hourlyPrice = computed(() => {
  const p = equipment.value.rentalPrice ?? equipment.value.price ?? 0
  return Number(p) || 0
})

const maxQuantity = computed(() =>
  Math.max(1, equipment.value.availableQuantity ?? equipment.value.quantity ?? 1)
)

const coverUrl = computed(
  () => resolveImageUrl(equipment.value.equipmentImage) || '/static/placeholders/hero.svg'
)

const typeTag = computed(() => {
  const t = equipment.value.equipmentType
  const map: Record<string, string> = {
    RACKET: '专业',
    SHUTTLE: '球',
    STRING: '穿线',
    OTHER: '器材'
  }
  return map[t] || '器材'
})

const subtitle = computed(() => {
  const spec = (equipment.value as EquipmentItem & { specifications?: string }).specifications
  if (spec) return spec
  const d = equipment.value.description
  if (!d) return '高品质运动器材'
  return d.length > 36 ? `${d.slice(0, 36)}…` : d
})

const startMs = computed(() => new Date(`${startDate.value}T${startTime.value}:00`).getTime())
const endMs = computed(() => new Date(`${endDate.value}T${endTime.value}:00`).getTime())

const durationHours = computed(() => {
  const diff = (endMs.value - startMs.value) / (1000 * 60 * 60)
  if (!Number.isFinite(diff) || diff <= 0) return 0
  return Math.max(1, Math.ceil(diff))
})

const baseRent = computed(() => hourlyPrice.value * durationHours.value * rentQuantity.value)

const depositPerUnit = computed(() => {
  const d = equipment.value.rentalDeposit
  if (d != null && Number(d) >= 0) return Number(d)
  return 0
})

const depositTotal = computed(() => depositPerUnit.value * rentQuantity.value)

const totalPayable = computed(() => baseRent.value + depositTotal.value + platformFee.value)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

function formatDateLabel(isoDate: string) {
  if (!isoDate) return ''
  const [y, m, d] = isoDate.split('-').map(Number)
  if (!y || !m || !d) return isoDate
  const dt = new Date(y, m - 1, d)
  const w = weekdays[dt.getDay()]
  return `${m}月${d}日 (周${w})`
}

const startDateLabel = computed(() => formatDateLabel(startDate.value))
const endDateLabel = computed(() => formatDateLabel(endDate.value))

function formatMoney(n: number) {
  return (Math.round(n * 100) / 100).toFixed(2)
}

const canSubmit = computed(() => {
  if (!equipment.value.id) return false
  if (rentQuantity.value < 1 || rentQuantity.value > maxQuantity.value) return false
  if (durationHours.value < 1) return false
  if (endMs.value <= startMs.value) return false
  if (hourlyPrice.value <= 0) return false
  return true
})

onLoad((options?: Record<string, string | undefined>) => {
  if (options?.id) equipmentId.value = Number(options.id)
  if (options?.quantity) {
    const q = Number(options.quantity)
    if (q > 0) rentQuantity.value = q
  }
  const t = todayStr()
  startDate.value = t
  endDate.value = t
})

function onStartDate(e: { detail: { value: string } }) {
  startDate.value = e.detail.value
}
function onStartTime(e: { detail: { value: string } }) {
  startTime.value = e.detail.value
}
function onEndDate(e: { detail: { value: string } }) {
  endDate.value = e.detail.value
}
function onEndTime(e: { detail: { value: string } }) {
  endTime.value = e.detail.value
}

function decreaseQuantity() {
  if (rentQuantity.value > 1) rentQuantity.value--
}

function increaseQuantity() {
  if (rentQuantity.value < maxQuantity.value) rentQuantity.value++
}

function onDepositInfo() {
  uni.showModal({
    title: '可退还押金',
    content: '押金在器材完好归还后按原支付路径退回；如有损坏或逾期，将按协议扣减。',
    showCancel: false
  })
}

async function loadEquipmentDetail() {
  try {
    const result = await getEquipmentDetail(equipmentId.value)
    equipment.value = result
  } catch (error) {
    console.error('加载器材详情失败:', error)
    uni.showToast({ title: '加载器材详情失败', icon: 'none' })
  }
}

function handleBack() {
  safeNavigateBack()
}

async function handleSubmit() {
  if (!canSubmit.value) {
    uni.showToast({
      title: endMs.value <= startMs.value ? '归还时间需晚于提取时间' : '请完善租借信息',
      icon: 'none'
    })
    return
  }

  const rentalAmountNum = baseRent.value + platformFee.value
  const payload = {
    memberId: userStore.userId,
    equipmentId: equipment.value.id,
    quantity: rentQuantity.value,
    rentalDate: startDate.value,
    expectedReturnDate: endDate.value,
    rentalAmount: rentalAmountNum,
    unitPrice: hourlyPrice.value,
    depositAmount: depositTotal.value,
    durationHours: durationHours.value,
    paymentMethod: 'BALANCE',
    paymentStatus: 0,
    status: 1,
    remark: `按小时计费；平台服务费¥${formatMoney(platformFee.value)}`
  }

  try {
    uni.showLoading({ title: '提交中...' })
    await createEquipmentRental(payload)
    uni.hideLoading()
    uni.showToast({ title: '租借成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/profile/records' })
    }, 1200)
  } catch (error) {
    console.error('租借器材失败:', error)
    uni.hideLoading()
    uni.showToast({ title: '租借失败，请重试', icon: 'none' })
  }
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  if (equipmentId.value) {
    await loadEquipmentDetail()
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f9f9f9;
  color: #1a1c1c;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 48rpx;
  background-color: #f9f9f9;
  position: sticky;
  top: 0;
  z-index: 40;
}

.icon-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 9999rpx;
  background-color: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn.placeholder {
  visibility: hidden;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.main {
  flex: 1;
  height: 0;
  padding: 0 32rpx;
  box-sizing: border-box;
}

.card {
  background-color: #ffffff;
  border-radius: 24rpx;
  margin-bottom: 48rpx;
}

.card-row {
  padding: 32rpx;
  display: flex;
  flex-direction: row;
  gap: 40rpx;
  align-items: center;
}

.thumb {
  width: 192rpx;
  height: 192rpx;
  border-radius: 16rpx;
  background-color: #f3f3f3;
  flex-shrink: 0;
}

.summary-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 180rpx;
}

.title-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.item-title {
  flex: 1;
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1c;
  line-height: 1.35;
}

.tag {
  font-size: 20rpx;
  font-weight: 700;
  color: #636262;
  background-color: #e2dfde;
  padding: 8rpx 16rpx;
  border-radius: 9999rpx;
  flex-shrink: 0;
}

.subtitle {
  font-size: 28rpx;
  color: #5f5e5e;
  margin-top: 8rpx;
}

.price-row {
  margin-top: 24rpx;
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 8rpx;
}

.price-num {
  font-size: 40rpx;
  font-weight: 700;
  color: #a33e00;
}

.price-unit {
  font-size: 24rpx;
  color: #5f5e5e;
}

.card-stack {
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.row-between {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.block-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.hint {
  display: block;
  font-size: 24rpx;
  color: #5f5e5e;
  margin-top: 4rpx;
}

.stepper {
  display: flex;
  flex-direction: row;
  align-items: center;
  background-color: #f3f3f3;
  border-radius: 16rpx;
  padding: 8rpx;
}

.step-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-btn-strong {
  background-color: #e8e8e8;
}

.step-symbol {
  font-size: 36rpx;
  line-height: 1;
  color: #5f5e5e;
  font-weight: 600;
}

.step-symbol.strong {
  color: #1a1c1c;
}

.step-val {
  min-width: 80rpx;
  text-align: center;
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.divider-gap {
  height: 8rpx;
}

.slot-block {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.edit-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}

.edit-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #a33e00;
}

.picker-panel {
  background-color: #f9f9f9;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.picker-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 0;
}

.picker-label {
  font-size: 26rpx;
  color: #5f5e5e;
}

.picker-value {
  font-size: 28rpx;
  color: #1a1c1c;
  font-weight: 500;
}

.time-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
}

.time-box {
  flex: 1;
  background-color: #f3f3f3;
  border-radius: 16rpx;
  padding: 24rpx;
  position: relative;
  overflow: hidden;
}

.time-box-start {
  padding-left: 28rpx;
}

.accent-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 8rpx;
  background-color: #a33e00;
}

.time-cap {
  font-size: 20rpx;
  color: #5f5e5e;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.time-date {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.time-clock {
  display: block;
  margin-top: 12rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.time-clock.start {
  color: #a33e00;
}

.arrow-wrap {
  width: 64rpx;
  height: 64rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.duration-hint {
  font-size: 24rpx;
  color: #5f5e5e;
  text-align: right;
}

.fee-card {
  gap: 28rpx;
}

.fee-title {
  margin-bottom: 8rpx;
}

.fee-line {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.fee-label {
  flex: 1;
  font-size: 28rpx;
  color: #5f5e5e;
  line-height: 1.45;
}

.fee-label-row {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}

.fee-value {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.scroll-spacer {
  height: 280rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 48rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  background-color: rgba(249, 249, 249, 0.92);
  box-shadow: 0 -20rpx 80rpx rgba(26, 28, 28, 0.06);
}

.footer-left {
  display: flex;
  flex-direction: column;
}

.total-cap {
  font-size: 24rpx;
  color: #5f5e5e;
  font-weight: 500;
  letter-spacing: 0.5px;
  margin-bottom: 4rpx;
}

.total-row {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 4rpx;
}

.currency {
  font-size: 40rpx;
  font-weight: 700;
  color: #a33e00;
}

.total-num {
  font-size: 56rpx;
  font-weight: 700;
  color: #a33e00;
  letter-spacing: -1px;
}

.cta {
  margin: 0;
  border: none;
  border-radius: 24rpx;
  padding: 28rpx 56rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #561d00;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 8rpx 24rpx rgba(163, 62, 0, 0.2);
}

.cta:disabled {
  opacity: 0.45;
  box-shadow: none;
}
</style>
