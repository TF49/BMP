<template>
  <MobileLayout className="query-shell">
    <view class="query-page">
      <scroll-view scroll-y class="page-scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-card">
            <view class="hero-top">
              <view class="hero-brand" @tap="handleBack">
                <uni-icons type="left" size="18" color="#a33e00" />
                <text class="hero-brand-text">KINETIC LOGIC</text>
              </view>
              <text class="hero-side-label">STRINGING TRACK</text>
            </view>

            <view class="hero-copy">
              <text class="hero-eyebrow">SERVICE LOOKUP</text>
              <text class="hero-title">输入工单编号，快速查询穿线进度</text>
              <text class="hero-subtitle">
                把旧式功能页升级成品牌化查询入口，让用户在搜索、查看和跳转详情时都保持同一套 Stitch 体验。
              </text>
            </view>

            <view class="hero-grid">
              <view class="hero-metric">
                <text class="metric-label">FORMAT</text>
                <text class="metric-value">ST + 日期 + 序号</text>
              </view>
              <view class="hero-metric">
                <text class="metric-label">SCAN</text>
                <text class="metric-value">支持扫码识别</text>
              </view>
            </view>
          </view>

          <view class="panel-card search-panel">
            <view class="panel-head">
              <text class="panel-title">服务编号查询</text>
              <text class="panel-tag">LIVE QUERY</text>
            </view>

            <text class="panel-hint">格式示例：ST202401010001。你也可以直接扫码读取工单编号。</text>

            <view class="input-wrap">
              <uni-icons type="search" size="18" color="#94a3b8" />
              <input
                v-model="serviceNo"
                class="service-input"
                placeholder="请输入服务编号"
                placeholder-style="color: #94a3b8"
                confirm-type="search"
                @confirm="handleQuery"
              />
              <view v-if="serviceNo" class="clear-input" @tap="clearInput">
                <uni-icons type="clear" size="16" color="#64748b" />
              </view>
            </view>

            <view class="action-row">
              <view class="scan-action" @tap="handleScan">
                <uni-icons type="scan" size="18" color="#a33e00" />
                <text class="scan-text">扫码识别</text>
              </view>
              <view class="query-action" @tap="handleQuery">
                <text class="query-action-text">开始查询</text>
                <uni-icons type="right" size="16" color="#ffffff" />
              </view>
            </view>
          </view>

          <view v-if="loading" class="panel-card state-card">
            <view class="spinner"></view>
            <text class="state-title">正在同步工单状态</text>
            <text class="state-subtitle">我们正在获取最新的穿线服务信息。</text>
          </view>

          <view v-else-if="showResult && resultView" class="panel-card result-panel">
            <view class="panel-head">
              <text class="panel-title">查询结果</text>
              <text class="panel-tag">SERVICE SNAPSHOT</text>
            </view>

            <view class="service-top">
              <view class="service-top-left">
                <text class="service-code">{{ resultView.serviceNo }}</text>
                <text class="service-racket">{{ resultView.racketTitle }}</text>
              </view>
              <view class="status-pill" :style="{ backgroundColor: resultView.statusBg, color: resultView.statusColor }">
                {{ resultView.statusText }}
              </view>
            </view>

            <view class="summary-grid">
              <view class="summary-box">
                <text class="summary-label">线材</text>
                <text class="summary-value">{{ resultView.stringLabel }}</text>
              </view>
              <view class="summary-box">
                <text class="summary-label">磅数</text>
                <text class="summary-value">{{ resultView.tensionLabel }}</text>
              </view>
              <view class="summary-box">
                <text class="summary-label">总价</text>
                <text class="summary-value accent">¥{{ resultView.totalPrice }}</text>
              </view>
              <view class="summary-box">
                <text class="summary-label">支付方式</text>
                <text class="summary-value">{{ resultView.paymentMethodText }}</text>
              </view>
            </view>

            <view class="detail-section">
              <view class="detail-row">
                <text class="detail-label">球拍品牌</text>
                <text class="detail-value">{{ resultView.racketBrand }}</text>
              </view>
              <view class="detail-row">
                <text class="detail-label">球拍型号</text>
                <text class="detail-value">{{ resultView.racketModel }}</text>
              </view>
              <view class="detail-row">
                <text class="detail-label">线材信息</text>
                <view class="detail-value detail-value-block">
                  <text v-if="resultView.isOwnString" class="own-string-badge">自带线材</text>
                  <text v-else>{{ resultView.stringLabel }}</text>
                </view>
              </view>
              <view class="detail-row">
                <text class="detail-label">服务费</text>
                <text class="detail-value">¥{{ resultView.servicePrice }}</text>
              </view>
              <view v-if="resultView.stringPrice !== ''" class="detail-row">
                <text class="detail-label">线材价格</text>
                <text class="detail-value">¥{{ resultView.stringPrice }}</text>
              </view>
              <view v-if="resultView.remark" class="detail-row">
                <text class="detail-label">备注</text>
                <text class="detail-value detail-value-multiline">{{ resultView.remark }}</text>
              </view>
              <view class="detail-row">
                <text class="detail-label">创建时间</text>
                <text class="detail-value">{{ resultView.createTime }}</text>
              </view>
              <view class="detail-row">
                <text class="detail-label">更新时间</text>
                <text class="detail-value">{{ resultView.updateTime }}</text>
              </view>
            </view>

            <view class="detail-cta" @tap="handleViewDetail">
              <text class="detail-cta-text">查看完整详情</text>
              <uni-icons type="right" size="16" color="#ffffff" />
            </view>
          </view>

          <view v-else-if="searched" class="panel-card state-card">
            <text class="state-title">没有找到对应工单</text>
            <text class="state-subtitle">请确认服务编号是否正确，或使用扫码功能重新识别。</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { getStringingByNo, type StringingService } from '@/api/stringing'
import { validateServiceNo } from '@/utils/validate'
import { safeNavigateBack } from '@/utils/navigation'
import {
  PAYMENT_METHOD_TEXT,
  STRINGING_STATUS,
  STRINGING_STATUS_COLOR,
  STRINGING_STATUS_TEXT
} from '@/utils/constant'

type QueryResultView = {
  id: number
  serviceNo: string
  racketBrand: string
  racketModel: string
  racketTitle: string
  stringLabel: string
  isOwnString: boolean
  tensionLabel: string
  servicePrice: string
  stringPrice: string
  totalPrice: string
  paymentMethodText: string
  remark: string
  createTime: string
  updateTime: string
  statusText: string
  statusColor: string
  statusBg: string
}

const serviceNo = ref('')
const queryResult = ref<StringingService | null>(null)
const showResult = ref(false)
const searched = ref(false)
const loading = ref(false)

const resultView = computed<QueryResultView | null>(() => {
  if (!queryResult.value) return null

  const raw = queryResult.value as unknown as Record<string, unknown>
  const status = Number(raw.status ?? -1)
  const isOwnString = raw.isOwnString === 1 || raw.isOwnString === true || raw.ownString === 1 || raw.ownString === true
  const racketBrand = toDisplayText(raw.racketBrand)
  const racketModel = toDisplayText(raw.racketModel)
  const stringName = toDisplayText(raw.stringName || raw.stringEquipmentName)
  const stringBrand = toDisplayText(raw.stringBrand)
  const stringGauge = toDisplayText(raw.stringGauge)
  const stringLabel = isOwnString
    ? '自带线材'
    : [stringName, stringBrand !== '-' || stringGauge !== '-' ? `${stringBrand} / ${stringGauge}` : '']
        .filter(part => part && part !== '- / -')
        .join(' · ') || '-'

  return {
    id: Number(raw.id || 0),
    serviceNo: toDisplayText(raw.serviceNo),
    racketBrand,
    racketModel,
    racketTitle: [racketBrand, racketModel].filter(part => part !== '-').join(' ') || '未填写球拍信息',
    stringLabel,
    isOwnString,
    tensionLabel: formatTension(raw.pound ?? raw.tension),
    servicePrice: formatMoney(raw.servicePrice),
    stringPrice: isOwnString ? '' : formatOptionalMoney(raw.stringPrice),
    totalPrice: formatMoney(raw.totalPrice ?? raw.servicePrice),
    paymentMethodText: getPaymentMethodText(raw.paymentMethod),
    remark: toDisplayText(raw.remark, ''),
    createTime: formatDateTime(raw.createTime),
    updateTime: formatDateTime(raw.updateTime),
    statusText: getStatusText(status),
    statusColor: getStatusColor(status),
    statusBg: getStatusBgColor(status)
  }
})

function toDisplayText(value: unknown, fallback = '-') {
  if (value === null || value === undefined || value === '') return fallback
  return String(value)
}

function formatMoney(value: unknown) {
  const num = Number(value ?? 0)
  if (Number.isNaN(num)) return '0.00'
  return (Math.round(num * 100) / 100).toFixed(2)
}

function formatOptionalMoney(value: unknown) {
  if (value === null || value === undefined || value === '') return ''
  return formatMoney(value)
}

function formatDateTime(value: unknown) {
  if (!value) return '-'
  const text = String(value)
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return text
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

function formatTension(value: unknown) {
  if (value === null || value === undefined || value === '') return '-'
  const numeric = Number(value)
  if (Number.isNaN(numeric)) return `${value} 磅`
  return `${String(numeric).replace(/\.0$/, '')} 磅`
}

function getStatusText(status: number) {
  const map = STRINGING_STATUS_TEXT as unknown as Record<string, string>
  return map[String(status)] || '未知'
}

function getStatusColor(status: number) {
  const map = STRINGING_STATUS_COLOR as unknown as Record<string, string>
  return map[String(status)] || '#64748b'
}

function getStatusBgColor(status: number) {
  const colorMap: Record<number, string> = {
    [STRINGING_STATUS.CANCELLED]: '#f1f5f9',
    [STRINGING_STATUS.WAITING]: '#fff3e0',
    [STRINGING_STATUS.IN_PROGRESS]: '#e0f2fe',
    [STRINGING_STATUS.COMPLETED]: '#dcfce7'
  }
  return colorMap[status] || '#f1f5f9'
}

function getPaymentMethodText(method: unknown) {
  if (!method) return '未知'
  const key = String(method)
  const map = PAYMENT_METHOD_TEXT as unknown as Record<string, string>
  return map[key] || key
}

function clearInput() {
  serviceNo.value = ''
  showResult.value = false
  searched.value = false
  queryResult.value = null
}

function handleBack() {
  safeNavigateBack('/pages/profile/index')
}

async function handleQuery() {
  const currentNo = serviceNo.value.trim()
  if (!currentNo) {
    uni.showToast({ title: '请输入服务编号', icon: 'none' })
    return
  }

  if (!validateServiceNo(currentNo)) {
    uni.showToast({ title: '服务编号格式不正确', icon: 'none' })
    return
  }

  loading.value = true
  searched.value = true
  try {
    queryResult.value = await getStringingByNo(currentNo)
    showResult.value = true
  } catch (error) {
    console.error('查询服务失败:', error)
    queryResult.value = null
    showResult.value = false
    uni.showToast({ title: '未找到该服务记录', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function handleScan() {
  uni.scanCode({
    success: (res) => {
      serviceNo.value = res.result || ''
      void handleQuery()
    },
    fail: (err) => {
      console.error('扫码失败:', err)
      uni.showToast({ title: '扫码失败', icon: 'none' })
    }
  })
}

function handleViewDetail() {
  if (!resultView.value?.id) return
  uni.navigateTo({
    url: `/pages/stringing/detail?id=${resultView.value.id}`
  })
}
</script>

<style lang="scss" scoped>
.query-shell {
  background:
    radial-gradient(circle at top left, rgba(255, 170, 112, 0.3), transparent 28%),
    radial-gradient(circle at top right, rgba(251, 146, 60, 0.18), transparent 20%),
    linear-gradient(180deg, #fff7ed 0%, #f8fafc 35%, #eef2ff 100%);
}

.query-page {
  min-height: 100vh;
}

.page-scroll {
  height: 100vh;
}

.content {
  padding: 30rpx 24rpx 48rpx;
}

.hero-card,
.panel-card {
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24rpx 60rpx rgba(15, 23, 42, 0.08);
}

.hero-card {
  position: relative;
  overflow: hidden;
  padding: 32rpx;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 237, 0.98));
}

.hero-card::before {
  content: '';
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.22), rgba(251, 146, 60, 0));
}

.hero-top,
.panel-head,
.service-top,
.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-top {
  position: relative;
  z-index: 1;
  margin-bottom: 36rpx;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 237, 213, 0.86);
}

.hero-brand-text,
.hero-side-label,
.hero-eyebrow,
.panel-tag,
.metric-label,
.summary-label {
  letter-spacing: 0.18em;
}

.hero-brand-text {
  font-size: 20rpx;
  color: #9a3412;
  font-weight: 800;
}

.hero-side-label {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 700;
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  display: block;
  margin-bottom: 16rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.hero-title {
  display: block;
  font-size: 54rpx;
  line-height: 1.16;
  color: #0f172a;
  font-weight: 800;
}

.hero-subtitle {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.65;
  color: #64748b;
}

.hero-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 24rpx;
}

.hero-metric,
.summary-box {
  padding: 20rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.16);
}

.metric-label,
.summary-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.metric-value,
.summary-value {
  display: block;
  font-size: 26rpx;
  color: #0f172a;
  font-weight: 800;
  line-height: 1.45;
}

.panel-card {
  margin-top: 24rpx;
  padding: 28rpx;
}

.panel-title {
  font-size: 32rpx;
  color: #0f172a;
  font-weight: 800;
}

.panel-tag {
  font-size: 18rpx;
  color: #94a3b8;
  font-weight: 700;
}

.panel-hint {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #64748b;
}

.input-wrap {
  display: flex;
  align-items: center;
  height: 96rpx;
  padding: 0 20rpx;
  margin-top: 24rpx;
  border-radius: 24rpx;
  background: #f8fafc;
  border: 2rpx solid rgba(148, 163, 184, 0.12);
}

.service-input {
  flex: 1;
  margin-left: 14rpx;
  font-size: 28rpx;
  color: #0f172a;
}

.clear-input {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-row {
  gap: 16rpx;
  margin-top: 20rpx;
}

.scan-action,
.query-action,
.detail-cta {
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.scan-action {
  flex: 0 0 220rpx;
  background: #fff7ed;
}

.scan-text {
  font-size: 24rpx;
  color: #9a3412;
  font-weight: 800;
}

.query-action,
.detail-cta {
  flex: 1;
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  box-shadow: 0 16rpx 32rpx rgba(234, 88, 12, 0.24);
}

.query-action-text,
.detail-cta-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 800;
}

.state-card {
  text-align: center;
}

.spinner {
  width: 64rpx;
  height: 64rpx;
  margin: 6rpx auto 22rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(251, 146, 60, 0.14);
  border-top-color: #ea580c;
  animation: spin 0.8s linear infinite;
}

.state-title {
  display: block;
  font-size: 30rpx;
  color: #0f172a;
  font-weight: 800;
}

.state-subtitle {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #94a3b8;
}

.service-top {
  margin-top: 24rpx;
  gap: 18rpx;
  align-items: flex-start;
}

.service-code {
  display: block;
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.service-racket {
  display: block;
  margin-top: 12rpx;
  font-size: 40rpx;
  line-height: 1.2;
  color: #0f172a;
  font-weight: 800;
}

.status-pill {
  flex-shrink: 0;
  min-height: 56rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 800;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 24rpx;
}

.summary-value.accent {
  color: #c2410c;
}

.detail-section {
  margin-top: 24rpx;
  padding-top: 10rpx;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.85);
}

.detail-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.detail-label {
  flex: 0 0 160rpx;
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 700;
}

.detail-value {
  flex: 1;
  text-align: right;
  font-size: 24rpx;
  line-height: 1.6;
  color: #0f172a;
  font-weight: 700;
}

.detail-value-block {
  display: flex;
  justify-content: flex-end;
}

.detail-value-multiline {
  white-space: pre-wrap;
}

.own-string-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  background: #fff7ed;
  color: #9a3412;
  font-size: 20rpx;
  font-weight: 800;
}

.detail-cta {
  margin-top: 24rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
