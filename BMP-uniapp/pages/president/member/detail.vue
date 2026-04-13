<template>
  <PresidentLayout :showTabBar="false">
    <view class="member-detail-page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-inner">
          <view class="top-left" @click="onBack">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
            <text class="top-title">会员详情</text>
          </view>
          <view class="top-right">
            <view class="edit-btn" @click="onEdit">
              <uni-icons type="compose" size="18" color="#5f5e5e" />
            </view>
            <image class="president-avatar" :src="presidentAvatar" mode="aspectFill" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading && !member" class="state-wrap">
          <view class="spinner" />
          <text>加载会员详情中…</text>
        </view>

        <view v-else-if="!member" class="state-wrap">
          <text>会员信息不存在或已删除</text>
          <view class="retry-btn" @click="reloadPage">重新加载</view>
        </view>

        <template v-else>
          <view class="id-card">
            <view class="watermark">
              <uni-icons type="contact" size="80" color="#000000" />
            </view>
            <image class="member-avatar" :src="memberAvatar" mode="aspectFill" />
            <text class="member-name">{{ member.memberName || '未命名会员' }}</text>
            <text class="member-no">会员 ID: {{ member.id }}</text>
            <view class="tags">
              <text class="tag vip">{{ levelLabel(member.memberLevel) }}</text>
              <text class="tag normal">{{ statusLabel(member.status) }}</text>
            </view>

            <view class="meta-list">
              <view class="meta-row">
                <text class="meta-k">注册时间</text>
                <text class="meta-v">{{ formatDate(member.createTime) }}</text>
              </view>
              <view class="meta-row">
                <text class="meta-k">联系电话</text>
                <text class="meta-v">{{ maskPhone(member.phone) }}</text>
              </view>
              <view class="meta-row">
                <text class="meta-k">会员类型</text>
                <text class="meta-v">{{ memberTypeLabel(member.memberType) }}</text>
              </view>
            </view>
          </view>

          <view class="balance-card">
            <view class="balance-mark">
              <uni-icons type="wallet" size="90" color="#ffffff" />
            </view>
            <text class="balance-label">当前余额</text>
            <view class="balance-row">
              <text class="balance-yen">¥</text>
              <text class="balance-num">{{ money(member.balance) }}</text>
            </view>
            <view class="balance-actions">
              <view class="action white" @click="goRecharge">立即充值</view>
              <view class="action glass" @click="onAdjust">调整余额</view>
            </view>
          </view>

          <view class="consume-card">
            <text class="consume-label">累计消费</text>
            <view class="consume-row">
              <text class="consume-yen">¥</text>
              <text class="consume-num">{{ money(totalConsumption) }}</text>
            </view>
            <view class="progress-track">
              <view class="progress-fill" :style="{ width: `${levelProgress}%` }" />
            </view>
            <text class="consume-tip">距下一等级 {{ levelProgress }}%</text>
          </view>

          <view class="record-card">
            <view class="tabs">
              <view class="tab" :class="{ active: activeTab === 'consume' }" @click="activeTab = 'consume'">最近消费</view>
              <view class="tab" :class="{ active: activeTab === 'recharge' }" @click="activeTab = 'recharge'">充值流水</view>
            </view>

            <view class="records" v-if="activeTab === 'consume'">
              <view v-if="consumeLoading" class="records-state">加载中…</view>
              <view v-else-if="consumeError" class="records-state">{{ consumeError }}</view>
              <view v-else-if="consumeList.length === 0" class="records-state">暂无消费记录</view>
              <view v-else class="record-item" v-for="item in consumeList" :key="`c-${item.id}`">
                <view class="item-left">
                  <view class="item-icon orange">
                    <uni-icons type="flag" size="16" color="#ea580c" />
                  </view>
                  <view class="item-main">
                    <text class="item-title">{{ consumeTitle(item) }}</text>
                    <text class="item-sub">{{ formatDateTime(item.createTime) }} · {{ item.paymentMethod || '余额支付' }}</text>
                  </view>
                </view>
                <view class="item-right">
                  <text class="item-amount">- ¥{{ money(item.amount) }}</text>
                  <text class="item-status">交易成功</text>
                </view>
              </view>
            </view>

            <view class="records" v-else-if="activeTab === 'recharge'">
              <view v-if="rechargeLoading" class="records-state">加载中…</view>
              <view v-else-if="rechargeError" class="records-state">{{ rechargeError }}</view>
              <view v-else-if="rechargeList.length === 0" class="records-state">暂无充值流水</view>
              <view v-else class="record-item" v-for="item in rechargeList" :key="`r-${item.id}`">
                <view class="item-left">
                  <view class="item-icon green">
                    <uni-icons type="wallet" size="16" color="#16a34a" />
                  </view>
                  <view class="item-main">
                    <text class="item-title">会员充值 {{ payMethodLabel(item.rechargeMethod) }}</text>
                    <text class="item-sub">{{ formatDateTime(item.createTime) }} · 单号 {{ item.rechargeNo || '-' }}</text>
                  </view>
                </view>
                <view class="item-right">
                  <text class="item-amount plus">+ ¥{{ money(item.rechargeAmount) }}</text>
                  <text class="item-status">已到账</text>
                </view>
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
import { getMemberInfo, getMemberConsumeRecords, type MemberInfo, type ConsumeRecord } from '@/api/member'
import { getRechargeRecordsByMemberId, type RechargeRecord } from '@/api/recharge'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeNavigateBack } from '@/utils/navigation'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const defaultAvatar = '/static/placeholders/avatar.svg'
const defaultPresidentAvatar =
  '/static/placeholders/hero.svg'

const memberId = ref(0)
const member = ref<MemberInfo | null>(null)
const loading = ref(true)

const activeTab = ref<'consume' | 'recharge'>('consume')
const consumeLoading = ref(false)
const rechargeLoading = ref(false)
const consumeError = ref('')
const rechargeError = ref('')
const consumeList = ref<ConsumeRecord[]>([])
const rechargeList = ref<RechargeRecord[]>([])

const presidentAvatar = computed(() => userStore.userInfo?.avatar || defaultPresidentAvatar)
const memberAvatar = computed(() => member.value?.avatar || defaultAvatar)
const totalConsumption = computed(() => {
  const apiValue = Number(member.value?.totalConsumption)
  if (Number.isFinite(apiValue) && apiValue > 0) return apiValue
  return consumeList.value.reduce((sum, i) => sum + Number(i.amount || 0), 0)
})

/** 简化展示：根据等级映射 25/50/75/90/100 */
const levelProgress = computed(() => {
  const lv = member.value?.memberLevel || 1
  return [25, 50, 75, 90, 100][Math.max(0, Math.min(4, lv - 1))]
})

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.MEMBER_LIST)
}

function onEdit() {
  if (!memberId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.MEMBER_FORM}?id=${memberId.value}` })
}

function goRecharge() {
  if (!memberId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.MEMBER_RECHARGE}?memberId=${memberId.value}` })
}

function onAdjust() {
  goRecharge()
}

function reloadPage() {
  if (!memberId.value) return
  Promise.all([loadDetail(), loadConsume(), loadRecharge()])
}

function money(v?: number) {
  if (v == null || Number.isNaN(Number(v))) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function maskPhone(phone?: string) {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1 **** $2')
}

function formatDate(raw?: string) {
  if (!raw) return '-'
  return raw.slice(0, 10).replace(/-/g, '.')
}

function formatDateTime(raw?: string) {
  if (!raw) return '-'
  const s = raw.replace('T', ' ')
  return s.length >= 16 ? s.slice(0, 16) : s
}

function levelLabel(level?: number) {
  const lv = level || 1
  const map = ['青铜会员', '白银会员', '黄金会员', '铂金会员', '钻石会员']
  return map[Math.max(0, Math.min(4, lv - 1))]
}

function statusLabel(status?: number) {
  if (status === 0) return '已冻结'
  if (status === 2) return '已到期'
  return '活跃中'
}

function memberTypeLabel(type?: string) {
  if (type === 'MEMBER') return '正式会员'
  if (type === 'NORMAL') return '普通用户'
  return type || '未知'
}

function consumeTitle(item: ConsumeRecord) {
  return item.remark || item.businessNo || '会员消费'
}

function payMethodLabel(method?: string) {
  if (method === 'CASH') return '现金'
  if (method === 'ALIPAY') return '支付宝'
  if (method === 'WECHAT') return '微信'
  if (method === 'BANK') return '银行转账'
  return method || '其他'
}

async function loadDetail() {
  loading.value = true
  try {
    member.value = await getMemberInfo(memberId.value)
  } catch (error) {
    console.error('Failed to load member detail:', error)
    member.value = null
  } finally {
    loading.value = false
  }
}

async function loadConsume() {
  consumeLoading.value = true
  consumeError.value = ''
  try {
    const res = await getMemberConsumeRecords(memberId.value, { page: 1, size: 20 })
    consumeList.value = res.data || []
  } catch (error) {
    console.error('Failed to load member consume records:', error)
    consumeList.value = []
    consumeError.value = '消费记录加载失败，请稍后重试'
  } finally {
    consumeLoading.value = false
  }
}

async function loadRecharge() {
  rechargeLoading.value = true
  rechargeError.value = ''
  try {
    const res = await getRechargeRecordsByMemberId(memberId.value, { page: 1, size: 20 })
    rechargeList.value = res.data || []
  } catch (error) {
    console.error('Failed to load member recharge records:', error)
    rechargeList.value = []
    rechargeError.value = '充值流水加载失败，请稍后重试'
  } finally {
    rechargeLoading.value = false
  }
}

onLoad(async (q?: Record<string, string | undefined>) => {
  const raw = q?.memberId ?? q?.id
  const id = raw ? parseInt(String(raw), 10) : NaN
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: '缺少会员参数', icon: 'none' })
    setTimeout(() => onBack(), 800)
    return
  }
  memberId.value = id
  await Promise.all([loadDetail(), loadConsume(), loadRecharge()])
})
</script>

<style lang="scss" scoped>
.member-detail-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f8fafc;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 40;
  background: rgba(248, 250, 252, 0.9);
  backdrop-filter: blur(12px);
}

.top-inner {
  min-height: 88rpx;
  padding: 16rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.top-title {
  font-size: 26rpx;
  font-weight: 700;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.edit-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.president-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 9999px;
}

.scroll {
  height: calc(100vh - var(--status-bar-height) - 100rpx);
  padding: 24rpx;
  box-sizing: border-box;
}

.state-wrap {
  min-height: 360rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  justify-content: center;
  align-items: center;
  color: #71717a;
}

.retry-btn {
  min-width: 220rpx;
  height: 72rpx;
  padding: 0 32rpx;
  border-radius: 9999px;
  background: #ea580c;
  color: #fff7ed;
  font-size: 26rpx;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 44rpx;
  height: 44rpx;
  border-radius: 9999px;
  border: 4rpx solid #e5e7eb;
  border-top-color: #ea580c;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.id-card,
.quick-panel,
.consume-card,
.record-card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 6rpx 20rpx rgba(2, 6, 23, 0.04);
}

.id-card {
  position: relative;
  overflow: hidden;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.watermark {
  position: absolute;
  right: 20rpx;
  top: 20rpx;
  opacity: 0.06;
}

.member-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 9999px;
  margin: 0 auto 16rpx;
  border: 6rpx solid #ffedd5;
}

.member-name {
  display: block;
  text-align: center;
  font-size: 44rpx;
  font-weight: 900;
}

.member-no {
  display: block;
  text-align: center;
  margin-top: 8rpx;
  color: #71717a;
  font-size: 22rpx;
}

.tags {
  margin-top: 16rpx;
  display: flex;
  justify-content: center;
  gap: 12rpx;
}

.tag {
  padding: 8rpx 16rpx;
  border-radius: 9999px;
  font-size: 20rpx;
  font-weight: 700;
}

.tag.vip {
  color: #ea580c;
  background: #fff7ed;
}

.tag.normal {
  color: #5f5e5e;
  background: #f1f5f9;
}

.meta-list {
  margin-top: 26rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f1f5f9;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 0;
}

.meta-k {
  color: #a1a1aa;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.meta-v {
  font-size: 24rpx;
  font-weight: 700;
}

.quick-panel {
  padding: 16rpx;
  margin-bottom: 20rpx;
}

.quick-item {
  padding: 20rpx;
  border-radius: 16rpx;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quick-item + .quick-item {
  margin-top: 12rpx;
}

.quick-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.quick-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-text {
  font-size: 24rpx;
  font-weight: 700;
}

.balance-card {
  margin-bottom: 20rpx;
  padding: 30rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #a33e00, #ff6600);
  color: #fff;
  position: relative;
  overflow: hidden;
}

.balance-mark {
  position: absolute;
  right: -10rpx;
  bottom: -6rpx;
  opacity: 0.12;
}

.balance-label {
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.85;
}

.balance-row {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  margin: 12rpx 0 20rpx;
}

.balance-yen {
  font-size: 34rpx;
  font-weight: 700;
}

.balance-num {
  font-size: 70rpx;
  font-weight: 900;
  letter-spacing: -0.04em;
}

.balance-actions {
  display: flex;
  gap: 14rpx;
}

.action {
  min-width: 150rpx;
  height: 62rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action.white {
  background: #ffffff;
  color: #a33e00;
}

.action.glass {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
}

.consume-card {
  padding: 26rpx;
  margin-bottom: 20rpx;
}

.consume-label {
  color: #a1a1aa;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.consume-row {
  margin-top: 8rpx;
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.consume-yen {
  font-size: 30rpx;
}

.consume-num {
  font-size: 56rpx;
  font-weight: 900;
}

.progress-track {
  margin-top: 18rpx;
  height: 10rpx;
  background: #f1f5f9;
  border-radius: 9999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #ea580c;
  border-radius: 9999px;
}

.consume-tip {
  display: block;
  margin-top: 8rpx;
  color: #a1a1aa;
  font-size: 18rpx;
}

.record-card {
  padding: 0 0 10rpx;
  overflow: hidden;
}

.tabs {
  display: flex;
  border-bottom: 1rpx solid #f1f5f9;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 20rpx 10rpx;
  color: #a1a1aa;
  font-size: 24rpx;
  font-weight: 700;
}

.tab.active {
  color: #ea580c;
  border-bottom: 3rpx solid #ea580c;
}

.records {
  padding: 12rpx;
}

.records-state {
  text-align: center;
  padding: 48rpx 0;
  color: #a1a1aa;
  font-size: 24rpx;
}

.record-item {
  padding: 16rpx;
  border-radius: 18rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-item + .record-item {
  margin-top: 6rpx;
}

.item-left {
  display: flex;
  gap: 12rpx;
  align-items: center;
  min-width: 0;
}

.item-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-icon.orange { background: #fff7ed; }
.item-icon.green { background: #f0fdf4; }

.item-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 24rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.item-sub {
  font-size: 18rpx;
  color: #a1a1aa;
  margin-top: 4rpx;
}

.item-right {
  text-align: right;
  margin-left: 10rpx;
}

.item-amount {
  display: block;
  font-size: 26rpx;
  font-weight: 800;
}

.item-amount.plus {
  color: #16a34a;
}

.item-status {
  display: inline-block;
  margin-top: 6rpx;
  padding: 2rpx 10rpx;
  border-radius: 9999px;
  font-size: 16rpx;
  color: #71717a;
  background: #f1f5f9;
}

.bottom-space {
  height: 30rpx;
}
</style>
