<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="round-btn" @tap="handleBack">
          <uni-icons type="left" size="22" color="#1a1c1c" />
        </view>
        <text class="header-title">赛事报名</text>
        <view class="header-spacer" />
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
        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载赛事报名信息…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadTournamentDetail">重新加载</view>
        </view>

        <template v-else-if="detail">
          <view class="hero-card">
            <image class="hero-image" src="/static/placeholders/hero.svg" mode="aspectFill" />
            <view class="hero-overlay" />
            <view class="hero-content">
              <text class="hero-tag">官方排名赛</text>
              <text class="hero-title">{{ detail.name }}</text>
              <view class="hero-meta">
                <view class="hero-meta-item">
                  <uni-icons type="calendar" size="14" color="#ffffff" />
                  <text>{{ detail.dateRange }}</text>
                </view>
                <view class="hero-meta-item">
                  <uni-icons type="location" size="14" color="#ffffff" />
                  <text>{{ detail.location }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="section-card">
            <view class="section-head">
              <uni-icons type="person-filled" size="20" color="#a33e00" />
              <text class="section-title">基本信息</text>
            </view>

            <view class="field-wrap">
              <text class="field-label">参赛者姓名</text>
              <input
                v-model="form.name"
                class="field-input"
                type="text"
                placeholder="请输入真实姓名"
                maxlength="20"
              />
            </view>

            <view class="field-wrap">
              <text class="field-label">身份证号码 (用于购买保险)</text>
              <input
                v-model="form.idCard"
                class="field-input"
                type="text"
                placeholder="请输入18位身份证号"
                maxlength="18"
              />
            </view>

            <view class="field-wrap">
              <text class="field-label">参赛组别</text>
              <view class="category-grid">
                <view
                  v-for="item in categories"
                  :key="item.value"
                  class="category-item"
                  :class="{ active: form.category === item.value }"
                  @tap="form.category = item.value"
                >
                  <text>{{ item.label }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="section-card partner-card">
            <view class="partner-indicator" />
            <view class="section-head section-head-between">
              <view class="section-head-left">
                <uni-icons type="staff-filled" size="20" color="#a33e00" />
                <text class="section-title">搭档信息</text>
              </view>
              <text class="optional-tag">选填</text>
            </view>

            <text class="partner-tip">
              如果您报名的是双打项目，请在此填写搭档信息。如暂无搭档可留空，后续由系统随机分配。
            </text>

            <view class="field-wrap">
              <text class="field-label">搭档姓名</text>
              <input
                v-model="form.partnerName"
                class="field-input"
                type="text"
                placeholder="搭档真实姓名"
                maxlength="20"
              />
            </view>

            <view class="field-wrap">
              <text class="field-label">联系电话</text>
              <input
                v-model="form.partnerPhone"
                class="field-input"
                type="number"
                placeholder="搭档手机号"
                maxlength="11"
              />
            </view>
          </view>

          <view class="section-card agreement-card">
            <view class="agreement-row" @tap="toggleAgreement">
              <view class="agreement-box" :class="{ checked: agreementChecked }">
                <uni-icons
                  v-if="agreementChecked"
                  type="checkbox-filled"
                  size="18"
                  color="#ff6600"
                />
              </view>
              <text class="agreement-text">
                本人已阅读并完全理解
                <text class="agreement-link" @tap.stop="openRule('rule')">《2023秋季公开赛竞赛规程》</text>
                及
                <text class="agreement-link" @tap.stop="openRule('disclaimer')">《参赛免责声明》</text>。
                我确认本人身体健康，适合参加剧烈体育运动，并自愿承担比赛期间可能发生的任何意外风险。
              </text>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="detail" class="bottom-bar">
      <view class="bottom-price">
        <text class="bottom-label">报名费用合计</text>
        <view class="bottom-amount-row">
          <text class="bottom-currency">¥</text>
          <text class="bottom-amount">{{ detail.feeText }}</text>
        </view>
      </view>
      <view class="submit-btn" :class="{ disabled: !canSubmit || isSubmitting }" @tap="handleSubmit">
        <text>{{ isSubmitting ? '提交中...' : '提交报名' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { createTournamentRegistration, getTournamentDetail, type TournamentItem } from '@/api/tournament'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useCurrentMember } from '@/composables/useCurrentMember'

type CategoryOption = {
  label: string
  value: string
}

type TournamentRegisterVm = {
  id: number
  name: string
  dateRange: string
  location: string
  feeText: string
}

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const refreshing = ref(false)
const loading = ref(true)
const errorText = ref('')
const isSubmitting = ref(false)
const agreementChecked = ref(false)
const tournamentId = ref(0)
const tournament = ref<TournamentItem | null>(null)

const categories: CategoryOption[] = [
  { label: '男子单打', value: 'MS' },
  { label: '女子单打', value: 'WS' },
  { label: '男子双打', value: 'MD' }
]

const form = ref({
  name: '',
  idCard: '',
  category: 'MD',
  partnerName: '',
  partnerPhone: ''
})

function toDate(raw?: string) {
  if (!raw) return null
  const date = new Date(String(raw).replace(/-/g, '/'))
  return Number.isNaN(date.getTime()) ? null : date
}

function formatMoney(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function formatDateRange(start?: string, end?: string) {
  const startDate = toDate(start)
  const endDate = toDate(end)
  if (!startDate && !endDate) return '日期待定'
  if (startDate && !endDate) return `${startDate.getMonth() + 1}月${startDate.getDate()}日`
  if (!startDate && endDate) return `${endDate.getMonth() + 1}月${endDate.getDate()}日`
  if (!startDate || !endDate) return '日期待定'
  return `${startDate.getMonth() + 1}月${startDate.getDate()}日 - ${endDate.getMonth() + 1}月${endDate.getDate()}日`
}

function resolvePhone() {
  return userStore.userInfo?.phone || ''
}

const detail = computed<TournamentRegisterVm | null>(() => {
  if (!tournament.value) return null
  return {
    id: tournament.value.id,
    name: tournament.value.tournamentName || '赛事报名',
    dateRange: formatDateRange(tournament.value.tournamentStart || tournament.value.startDate, tournament.value.tournamentEnd),
    location: tournament.value.venueName || tournament.value.location || '场馆待补充',
    feeText: formatMoney(Number(tournament.value.entryFee || 0))
  }
})

const canSubmit = computed(() => {
  return Boolean(
    detail.value &&
      form.value.name.trim() &&
      form.value.idCard.trim().length >= 15 &&
      agreementChecked.value &&
      resolvePhone()
  )
})

const getTournamentErrorMessage = (error: unknown): string => {
  const rawMsg = String((error as { message?: string; errMsg?: string })?.message || (error as { errMsg?: string })?.errMsg || '')
  const msg = rawMsg.toLowerCase()

  if (msg.includes('full') || rawMsg.includes('满员') || rawMsg.includes('名额')) {
    return '赛事名额已满，请关注后续场次'
  }
  if (msg.includes('duplicate') || rawMsg.includes('重复') || rawMsg.includes('已报名')) {
    return '您已报名该赛事，请勿重复提交'
  }
  if (msg.includes('expired') || rawMsg.includes('截止') || rawMsg.includes('结束')) {
    return '赛事报名已截止'
  }
  return rawMsg || '报名失败，请重试'
}

async function loadTournamentDetail() {
  if (!tournamentId.value) {
    loading.value = false
    errorText.value = '缺少赛事参数'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    tournament.value = await getTournamentDetail(tournamentId.value)
    form.value.name = form.value.name || userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  } catch (error) {
    console.error('加载赛事详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载赛事详情失败'
  } finally {
    loading.value = false
  }
}

function toggleAgreement() {
  agreementChecked.value = !agreementChecked.value
}

function openRule(type: 'rule' | 'disclaimer') {
  uni.showModal({
    title: type === 'rule' ? '竞赛规程' : '参赛免责声明',
    content: '当前页面未提供独立详情页，请以赛事详情页、报名页当前说明和赛事主办方最新通知为准。',
    showCancel: false
  })
}

async function handleSubmit() {
  if (isSubmitting.value || !detail.value) return

  if (!resolvePhone()) {
    uni.showToast({
      title: '请先在个人资料中补充手机号',
      icon: 'none'
    })
    return
  }

  if (!canSubmit.value) {
    uni.showToast({
      title: '请完善报名信息',
      icon: 'none'
    })
    return
  }

  try {
    isSubmitting.value = true
    uni.showLoading({ title: '报名中...' })
    const member = await fetchCurrentMember()

    await createTournamentRegistration({
      memberId: Number(member.id || 0),
      tournamentId: detail.value.id,
      name: form.value.name.trim(),
      phone: resolvePhone(),
      skillLevel: form.value.category,
      emergencyContact: form.value.partnerName.trim() || undefined,
      emergencyPhone: form.value.partnerPhone.trim() || undefined,
      orderAmount: Number(tournament.value?.entryFee || 0),
      paymentMethod: 'BALANCE'
    })

    uni.hideLoading()
    uni.showToast({
      title: '报名成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/tournament/detail?id=${detail.value?.id}`
      })
    }, 1200)
  } catch (error) {
    console.error('赛事报名失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: getTournamentErrorMessage(error),
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/tournament/detail')
}

function handleRefresh() {
  refreshing.value = true
  loadTournamentDetail().finally(() => {
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

  tournamentId.value = Number(options?.id || 0)
  await loadTournamentDetail()
})

onPullDownRefresh(() => {
  loadTournamentDetail().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fafafa 0%, #f4f4f4 100%);
  color: #1a1c1c;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.88);
  backdrop-filter: blur(18px);
}

.header-inner {
  min-height: 112rpx;
  padding: 10rpx 28rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.round-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 40rpx;
  font-weight: 900;
  color: #111111;
  letter-spacing: -1rpx;
}

.header-spacer {
  width: 72rpx;
  height: 72rpx;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 22rpx 18rpx 240rpx;
}

.hero-card,
.section-card,
.state-card {
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 12rpx 36rpx rgba(15, 23, 42, 0.04);
}

.hero-card {
  position: relative;
  height: 440rpx;
  border-radius: 28rpx;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(10, 10, 10, 0.84) 100%);
}

.hero-content {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  bottom: 24rpx;
  z-index: 2;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 140rpx;
  height: 50rpx;
  padding: 0 18rpx;
  border-radius: 8rpx;
  background: #ff6600;
  color: #561d00;
  font-size: 22rpx;
  font-weight: 900;
}

.hero-title {
  display: block;
  margin-top: 20rpx;
  font-size: 58rpx;
  line-height: 1.1;
  font-weight: 900;
  color: #ffffff;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
}

.hero-meta {
  margin-top: 18rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.hero-meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.92);
  font-weight: 600;
}

.section-card {
  margin-top: 22rpx;
  border-radius: 28rpx;
  padding: 30rpx 26rpx;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.section-head-between {
  justify-content: space-between;
}

.section-head-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.section-title {
  font-size: 40rpx;
  font-weight: 900;
  color: #111111;
}

.field-wrap {
  margin-top: 28rpx;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 22rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.field-input {
  width: 100%;
  height: 96rpx;
  padding: 0 22rpx;
  border-radius: 8rpx 8rpx 0 0;
  background: #f3f3f3;
  color: #1a1c1c;
  font-size: 28rpx;
  border: none;
  border-bottom: 2rpx solid #8e7164;
  box-sizing: border-box;
}

.field-input:focus {
  border-bottom-color: #a33e00;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.category-item {
  min-height: 88rpx;
  border-radius: 18rpx;
  background: #f3f3f3;
  color: #1a1c1c;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 12rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.category-item.active {
  background: #ff6600;
  color: #561d00;
  box-shadow: 0 8rpx 16rpx rgba(163, 62, 0, 0.15);
}

.partner-card {
  position: relative;
  overflow: hidden;
}

.partner-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6rpx;
  background: #a33e00;
}

.optional-tag {
  min-width: 64rpx;
  height: 38rpx;
  padding: 0 12rpx;
  border-radius: 9999rpx;
  background: #eeeeee;
  color: #5f5e5e;
  font-size: 18rpx;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.partner-tip {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #5f5e5e;
}

.agreement-card {
  padding-top: 28rpx;
  padding-bottom: 28rpx;
}

.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.agreement-box {
  width: 34rpx;
  height: 34rpx;
  margin-top: 4rpx;
  border-radius: 4rpx;
  background: #f3f3f3;
  border: 2rpx solid #f3f3f3;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.agreement-box.checked {
  background: #ffffff;
  border-color: #ff6600;
}

.agreement-text {
  flex: 1;
  font-size: 28rpx;
  line-height: 1.8;
  color: #474746;
}

.agreement-link {
  color: #a33e00;
  font-weight: 700;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(18px);
  box-shadow: 0 -10rpx 30rpx rgba(0, 0, 0, 0.05);
  padding: 18rpx 26rpx calc(26rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.bottom-price {
  min-width: 210rpx;
}

.bottom-label {
  display: block;
  font-size: 18rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.bottom-amount-row {
  margin-top: 8rpx;
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.bottom-currency {
  font-size: 30rpx;
  font-weight: 900;
  color: #111111;
}

.bottom-amount {
  font-size: 62rpx;
  line-height: 1;
  font-weight: 900;
  color: #111111;
  letter-spacing: -2rpx;
}

.submit-btn {
  min-width: 250rpx;
  height: 92rpx;
  padding: 0 26rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #561d00;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 900;
  box-shadow: 0 8rpx 20rpx rgba(163, 62, 0, 0.2);
}

.submit-btn.disabled {
  background: #d7d7d7;
  color: #8c8c8c;
  box-shadow: none;
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
