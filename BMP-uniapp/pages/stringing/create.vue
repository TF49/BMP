<template>
  <view class="page">
    <!-- Top bar -->
    <view class="top-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="top-inner">
        <image class="avatar" :src="avatarUrl" mode="aspectFill" @tap="goProfile" />
        <text class="brand">KINETIC LOGIC</text>
        <view class="notify-btn" @tap="goNotice">
          <uni-icons type="notification" size="22" color="#a33e00" />
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main" :style="{ paddingTop: topOffset + 'px' }" :show-scrollbar="false">
      <view class="inner">
        <view class="hero">
          <view class="pill">
            <uni-icons type="gear" size="14" color="#5a4136" />
            <text class="pill-t">Professional Service</text>
          </view>
          <text class="h1">穿线服务预约</text>
          <text class="sub">选择最适合您的球线与磅数，我们的专业穿线师将为您提供顶级竞技体验。</text>
        </view>

        <!-- 01 球拍型号 -->
        <view class="card">
          <view class="card-head">
            <uni-icons type="loop" size="16" color="#5a4136" />
            <text class="card-title">01. Racket Model 球拍型号</text>
          </view>
          <view class="field-wrap">
            <text class="float-label">输入您的球拍型号</text>
            <input
              v-model="racketFullLine"
              class="field-input"
              placeholder="例如: Yonex Astrox 99 Pro"
              placeholder-class="ph"
              maxlength="80"
            />
          </view>
        </view>

        <!-- 02 球线 -->
        <view class="card">
          <view class="card-head-between">
            <view class="card-head">
              <uni-icons type="bars" size="16" color="#5a4136" />
              <text class="card-title">02. String Selection 球线选择</text>
            </view>
            <text class="stock-pill">库存充足</text>
          </view>

          <view v-if="stringList.length === 0 && !stringsLoading" class="empty-strings">
            <text>暂无可选线材，请稍后再试</text>
          </view>

          <view v-else class="string-grid">
            <view
              v-for="(s, idx) in gridStrings"
              :key="s.id"
              class="string-tile"
              :class="{ active: selectedStringId === s.id }"
              @tap="selectString(s.id)"
            >
              <view class="tile-glow" />
              <view class="tile-top">
                <text class="tile-name">{{ displayName(s) }}</text>
                <uni-icons
                  v-if="selectedStringId === s.id"
                  type="checkbox-filled"
                  size="22"
                  color="#561d00"
                />
              </view>
              <text class="tile-tag">{{ stringMeta(s).tag }}</text>
              <text class="tile-spec">{{ stringMeta(s).spec }}</text>
            </view>

            <view v-if="hasMoreStrings" class="string-tile more-tile" @tap="openMoreStrings">
              <text class="more-plus">+</text>
              <text class="more-text">查看更多型号</text>
            </view>
          </view>
        </view>

        <view class="two-col">
          <!-- 03 磅数 -->
          <view class="card half">
            <view class="card-head">
              <uni-icons type="settings" size="16" color="#5a4136" />
              <text class="card-title">03. Tension 磅数</text>
            </view>
            <view class="tension-display">
              <text class="tension-num">{{ tensionDisplay }}</text>
              <text class="tension-unit">lbs</text>
            </view>
            <view class="tension-btns">
              <view
                v-for="opt in tensionOptions"
                :key="opt.value"
                class="tbtn"
                :class="{ on: selectedPound === opt.value }"
                @tap="selectedPound = opt.value"
              >
                <text>{{ opt.label }}</text>
              </view>
            </view>
          </view>

          <!-- 04 取拍时间 -->
          <view class="card half">
            <view class="card-head">
              <uni-icons type="calendar" size="16" color="#5a4136" />
              <text class="card-title">04. Pickup 取拍时间</text>
            </view>
            <view class="pick-row">
              <text class="pick-label">期望取拍日期</text>
              <picker mode="date" :value="pickupDate" @change="onPickDate">
                <view class="pick-val">{{ pickupDate }}</view>
              </picker>
            </view>
            <view class="pick-row">
              <text class="pick-label">期望取拍时间</text>
              <picker mode="time" :value="pickupTime" @change="onPickTime">
                <view class="pick-val">{{ pickupTime }}</view>
              </picker>
            </view>
            <view class="info-line">
              <uni-icons type="info" size="12" color="#a33e00" />
              <text class="info-t">加急服务需额外支付 ¥30</text>
            </view>
          </view>
        </view>

        <view v-if="priceHint" class="price-hint">
          <text>预估费用 ¥{{ priceHint }}</text>
        </view>

        <button class="cta" :disabled="submitting || !canSubmit" @tap="handleSubmit">
          <text>{{ submitting ? '提交中…' : '确认预约提交' }}</text>
          <uni-icons type="arrow-right" size="22" color="#561d00" />
        </button>

        <!-- Tension guide -->
        <view class="guide">
          <view class="guide-head">
            <uni-icons type="compose" size="18" color="#a33e00" />
            <text class="guide-title">Tension Guide</text>
          </view>
          <view class="guide-block">
            <view class="guide-range">
              <text class="guide-num">27+</text>
              <text class="guide-unit">lbs</text>
            </view>
            <text class="guide-tag">专业 / 精准控球</text>
            <text class="guide-desc">甜区较小，需要使用者具备优秀的爆发力和发力技巧。击球感硬朗，落点控制极佳。</text>
          </view>
          <view class="guide-block">
            <view class="guide-range">
              <text class="guide-num">24-26</text>
              <text class="guide-unit">lbs</text>
            </view>
            <text class="guide-tag dim">进阶 / 攻守兼备</text>
            <text class="guide-desc">大众最常用的磅数区间。兼顾了球线的弹性和击球的硬度，适合有一定基础的业余爱好者。</text>
          </view>
          <view class="guide-block">
            <view class="guide-range">
              <text class="guide-num">20-23</text>
              <text class="guide-unit">lbs</text>
            </view>
            <text class="guide-tag dim">初级 / 轻松借力</text>
            <text class="guide-desc">甜区面积大，击球容易。弹性极佳，适合初学者、力量较小的女性或青少年球友。</text>
          </view>
          <view class="guide-foot">
            <text>Kinetic Logic</text>
            <text>Est. 2024</text>
          </view>
        </view>

        <view class="bottom-spacer" />
      </view>
    </scroll-view>

    <CustomTabBar :current="1" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { useCurrentMember } from '@/composables/useCurrentMember'
import { useUserStore } from '@/store/modules/user'
import {
  getStringList,
  calculatePrice,
  createStringing,
  type StringInfo,
  type CreateStringingParams,
  getStringInfoDisplayName
} from '@/api/stringing'
import { getVenueList } from '@/api/venue'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { getAvatarImage } from '@/utils/displayImage'
import { validateRacketBrand, validateRacketModel, validateRemark } from '@/utils/validate'
import type { MemberInfo } from '@/api/member'

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const topOffset = computed(() => statusBarHeight.value + 52)
const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))

const racketFullLine = ref('')
const stringList = ref<StringInfo[]>([])
const stringsLoading = ref(false)
const selectedStringId = ref<number | null>(null)
const selectedPound = ref(26)
const tensionOptions = [
  { label: '24', value: 24 },
  { label: '26', value: 26 },
  { label: '28', value: 28 },
  { label: '30+', value: 32 }
]

const pickupDate = ref('')
const pickupTime = ref('18:00')

const defaultVenueId = ref<number>(0)
const submitting = ref(false)
const priceHint = ref('')
const currentMember = ref<MemberInfo | null>(null)

const gridStrings = computed(() => stringList.value.slice(0, 3))
const hasMoreStrings = computed(() => stringList.value.length > 3)

const tensionDisplay = computed(() => (selectedPound.value === 32 ? '30+' : String(selectedPound.value)))

function pad2(n: number) {
  return n < 10 ? `0${n}` : `${n}`
}

function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

function displayName(s: StringInfo) {
  return getStringInfoDisplayName(s)
}

const STRING_COPY: Record<string, { tag: string; spec: string }> = {
  BG80: { tag: 'Offensive Power', spec: '0.68mm | 高弹性 | 硬击球感' },
  EXBOLT63: { tag: 'Repulsion Sound', spec: '0.63mm | 极细 | 高音效' },
  'EXBOLT 63': { tag: 'Repulsion Sound', spec: '0.63mm | 极细 | 高音效' },
  AEROSONIC: { tag: 'Ultimate Control', spec: '0.61mm | 控球 | 极佳弹性' }
}

function stringMeta(s: StringInfo) {
  const key = displayName(s).replace(/\s+/g, '').toUpperCase()
  for (const k of Object.keys(STRING_COPY)) {
    if (key.includes(k.replace(/\s+/g, ''))) {
      return STRING_COPY[k]!
    }
  }
  const p = Number(s.price ?? 0)
  return { tag: 'Recommended', spec: p > 0 ? `门店价 ¥${p.toFixed(0)}` : '专业球线' }
}

function splitRacketLabel(input: string) {
  const text = input.trim()
  if (!text) return { brand: '', model: '' }
  const parts = text.split(/\s+/).filter(Boolean)
  if (parts.length === 1) return { brand: parts[0]!, model: parts[0]! }
  return { brand: parts[0]!, model: parts.slice(1).join(' ') }
}

function selectString(id: number) {
  selectedStringId.value = id
}

function openMoreStrings() {
  const rest = stringList.value.slice(3)
  if (rest.length === 0) return
  uni.showActionSheet({
    itemList: rest.map((s) => displayName(s)),
    success: (res) => {
      const s = rest[res.tapIndex]
      if (s) selectedStringId.value = s.id
    }
  })
}

function onPickDate(e: { detail: { value: string } }) {
  pickupDate.value = e.detail.value
}

function onPickTime(e: { detail: { value: string } }) {
  pickupTime.value = e.detail.value
}

function goNotice() {
  uni.navigateTo({ url: '/pages/notice/index' })
}

function goProfile() {
  uni.navigateTo({ url: '/pages/profile/index' })
}

const canSubmit = computed(() => {
  const { brand, model } = splitRacketLabel(racketFullLine.value)
  if (!validateRacketBrand(brand) || !validateRacketModel(model)) return false
  if (!selectedStringId.value) return false
  if (!defaultVenueId.value) return false
  if (!Number(currentMember.value?.id || 0)) return false
  return true
})

async function ensureCurrentMember(force = false) {
  try {
    const member = await fetchCurrentMember(force)
    currentMember.value = member
    return member
  } catch (error) {
    console.error('获取当前会员失败:', error)
    currentMember.value = null
    return null
  }
}

async function syncPrice() {
  if (!selectedStringId.value) {
    priceHint.value = ''
    return
  }
  try {
    const r = await calculatePrice({ stringId: selectedStringId.value, ownString: false })
    priceHint.value = (r.totalPrice ?? 0).toFixed(2)
  } catch {
    priceHint.value = ''
  }
}

watch([selectedStringId], () => {
  void syncPrice()
})

async function loadStrings() {
  stringsLoading.value = true
  try {
    const result = await getStringList()
    const list = Array.isArray(result) ? result : []
    stringList.value = list
    if (list.length > 0) {
      selectedStringId.value = list[0]!.id
    } else {
      selectedStringId.value = null
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '线材加载失败', icon: 'none' })
  } finally {
    stringsLoading.value = false
  }
}

async function loadDefaultVenue() {
  const fromUser = Number((userStore.userInfo as { venueId?: number } | null)?.venueId || 0)
  if (fromUser > 0) {
    defaultVenueId.value = fromUser
    return
  }
  try {
    const res = await getVenueList({ status: 1, page: 1, size: 1 })
    const first = res.data?.[0]
    if (first?.id) {
      defaultVenueId.value = first.id
    }
  } catch (e) {
    console.error(e)
  }
}

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) {
    uni.showToast({ title: '请完善预约信息', icon: 'none' })
    return
  }

  const member = await ensureCurrentMember(true)
  const memberId = Number(member?.id || 0)
  if (!memberId) {
    uni.showToast({ title: '当前用户未绑定会员，无法提交穿线服务', icon: 'none' })
    return
  }

  const { brand, model } = splitRacketLabel(racketFullLine.value)
  const sel = stringList.value.find((x) => x.id === selectedStringId.value)
  if (!sel) {
    uni.showToast({ title: '请选择线材', icon: 'none' })
    return
  }

  const pickupRemark = `期望取拍: ${pickupDate.value} ${pickupTime.value}`
  const remarkParts = [pickupRemark]
  const remark = remarkParts.join('；')
  if (!validateRemark(remark)) {
    uni.showToast({ title: '取拍信息过长', icon: 'none' })
    return
  }

  const payload: CreateStringingParams = {
    memberId,
    venueId: defaultVenueId.value,
    racketBrand: brand,
    racketModel: model,
    stringId: sel.id,
    stringName: displayName(sel),
    isOwnString: 0,
    pound: selectedPound.value,
    stringingMethod: 'AUTO',
    status: 1,
    paymentMethod: 'BALANCE',
    paymentStatus: 0,
    remark
  }

  submitting.value = true
  try {
    uni.showLoading({ title: '提交中…' })
    const result = await createStringing(payload)
    uni.hideLoading()
    uni.showToast({ title: '预约已提交', icon: 'success' })
    const id = result?.id
    const timer: ReturnType<typeof setTimeout> = setTimeout(() => {
      if (id) {
        uni.redirectTo({ url: `/pages/stringing/detail?id=${id}` })
      } else {
        uni.redirectTo({ url: '/pages/stringing/list' })
      }
    }, 800)
    void timer
  } catch (e) {
    console.error(e)
    uni.hideLoading()
    uni.showToast({ title: '提交失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
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

  pickupDate.value = todayStr()
  currentMember.value = await ensureCurrentMember(true)
  if (!currentMember.value?.id) {
    uni.showToast({ title: '当前用户未绑定会员，无法提交穿线服务', icon: 'none' })
  }
  await loadDefaultVenue()
  if (!defaultVenueId.value) {
    uni.showToast({ title: '未获取到场馆，请稍后再试', icon: 'none' })
  }
  await loadStrings()
  await syncPrice()
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
  background-color: #fafafa;
  box-shadow: 0 20rpx 80rpx rgba(0, 0, 0, 0.04);
}

.top-inner {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 48rpx 20rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 9999rpx;
  background-color: #e8e8e8;
}

.brand {
  flex: 1;
  text-align: center;
  font-size: 36rpx;
  font-weight: 700;
  font-style: italic;
  letter-spacing: -1rpx;
  color: #18181b;
}

.notify-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main {
  box-sizing: border-box;
  height: 100vh;
}

.inner {
  padding: 0 48rpx 48rpx;
}

.hero {
  margin-bottom: 48rpx;
}

.pill {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 20rpx;
  background-color: #e8e8e8;
  border-radius: 9999rpx;
  margin-bottom: 16rpx;
}

.pill-t {
  font-size: 22rpx;
  font-weight: 700;
  color: #5a4136;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.h1 {
  display: block;
  font-size: 72rpx;
  font-weight: 800;
  letter-spacing: -2rpx;
  line-height: 1.1;
  color: #1a1c1c;
}

.sub {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #5f5e5e;
  line-height: 1.55;
  max-width: 640rpx;
}

.card {
  background-color: #ffffff;
  border-radius: 32rpx;
  padding: 48rpx;
  margin-bottom: 48rpx;
  box-shadow: 0 16rpx 60rpx rgba(26, 28, 28, 0.03);
}

.card-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.card-head-between {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32rpx;
}

.card-title {
  font-size: 22rpx;
  font-weight: 700;
  color: #5a4136;
  letter-spacing: 1rpx;
  text-transform: uppercase;
}

.stock-pill {
  font-size: 22rpx;
  font-weight: 700;
  color: #a33e00;
  background-color: #ffdbcd;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.field-wrap {
  position: relative;
  background-color: #f3f3f3;
  border-radius: 16rpx 16rpx 0 0;
  border-bottom: 4rpx solid #e8e8e8;
  padding: 40rpx 28rpx 20rpx;
}

.field-wrap:focus-within {
  border-bottom-color: #a33e00;
  background-color: #e8e8e8;
}

.float-label {
  position: absolute;
  top: 16rpx;
  left: 28rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 1rpx;
  text-transform: uppercase;
}

.field-input {
  font-size: 30rpx;
  color: #1a1c1c;
  width: 100%;
}

.ph {
  color: #9ca3af;
}

.empty-strings {
  padding: 40rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9ca3af;
}

.string-grid {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 24rpx;
}

.string-tile {
  width: calc(50% - 12rpx);
  box-sizing: border-box;
  padding: 32rpx;
  border-radius: 24rpx;
  background-color: #f3f3f3;
  position: relative;
  overflow: hidden;
}

.string-tile.active {
  background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
  box-shadow: 0 16rpx 48rpx rgba(255, 102, 0, 0.15);
}

.string-tile.active .tile-name,
.string-tile.active .tile-tag,
.string-tile.active .tile-spec {
  color: #561d00;
}

.tile-glow {
  position: absolute;
  right: -32rpx;
  top: -32rpx;
  width: 96rpx;
  height: 96rpx;
  background: #ffffff;
  opacity: 0.12;
  border-radius: 9999rpx;
}

.tile-top {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.tile-name {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -1rpx;
}

.tile-tag {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #5a4136;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  margin-bottom: 8rpx;
}

.tile-spec {
  font-size: 22rpx;
  color: #5f5e5e;
  font-weight: 500;
  line-height: 1.4;
}

.more-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200rpx;
  border: 2rpx dashed rgba(142, 113, 100, 0.25);
  background-color: #fafafa;
}

.more-plus {
  font-size: 48rpx;
  color: #5f5e5e;
  margin-bottom: 8rpx;
}

.more-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #5f5e5e;
}

.two-col {
  display: flex;
  flex-direction: column;
  gap: 48rpx;
  margin-bottom: 32rpx;
}

.card.half {
  margin-bottom: 0;
}

.tension-display {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  justify-content: center;
  gap: 8rpx;
  padding: 24rpx 0 32rpx;
}

.tension-num {
  font-size: 96rpx;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: -4rpx;
}

.tension-unit {
  font-size: 24rpx;
  font-weight: 700;
  color: #5a4136;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.tension-btns {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
}

.tbtn {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 16rpx;
  background-color: #f3f3f3;
  font-size: 26rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.tbtn.on {
  background-color: #a33e00;
  color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(163, 62, 0, 0.2);
}

.pick-row {
  margin-bottom: 20rpx;
}

.pick-label {
  display: block;
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  margin-bottom: 8rpx;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.pick-val {
  padding: 20rpx 24rpx;
  background-color: #f3f3f3;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.info-line {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  margin-top: 16rpx;
}

.info-t {
  font-size: 22rpx;
  color: #5f5e5e;
}

.price-hint {
  text-align: center;
  font-size: 26rpx;
  color: #5f5e5e;
  margin-bottom: 24rpx;
}

.cta {
  width: 100%;
  border: none;
  border-radius: 24rpx;
  padding: 36rpx 32rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  font-size: 36rpx;
  font-weight: 800;
  color: #561d00;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 16rpx 80rpx rgba(163, 62, 0, 0.2);
  margin-bottom: 48rpx;
}

.cta:disabled {
  opacity: 0.45;
}

.guide {
  background-color: #eeeeee;
  border-radius: 32rpx;
  padding: 48rpx;
  margin-bottom: 32rpx;
}

.guide-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 40rpx;
}

.guide-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #1a1c1c;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.guide-block {
  margin-bottom: 48rpx;
}

.guide-range {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 8rpx;
  margin-bottom: 8rpx;
}

.guide-num {
  font-size: 56rpx;
  font-weight: 900;
  color: #1a1c1c;
  letter-spacing: -2rpx;
}

.guide-unit {
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  text-transform: uppercase;
}

.guide-tag {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #a33e00;
  margin-bottom: 12rpx;
}

.guide-tag.dim {
  color: #5a4136;
}

.guide-desc {
  font-size: 24rpx;
  color: #5f5e5e;
  line-height: 1.55;
}

.guide-foot {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding-top: 32rpx;
  border-top: 1rpx solid rgba(142, 113, 100, 0.2);
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.bottom-spacer {
  height: 200rpx;
}
</style>
