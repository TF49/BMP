<template>
  <PresidentLayout :showTabBar="false" className="president-finance-audit-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">财务审计日志</text>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex @scrolltolower="loadMore">
        <view class="scroll-inner">
          <view class="filter-card">
            <text class="section-title">筛选条件</text>

            <view class="input-row">
              <input
                v-model.trim="filters.financeNo"
                class="input"
                type="text"
                placeholder="按流水号筛选"
                placeholder-class="input-placeholder"
              />
              <input
                v-model.trim="filters.operator"
                class="input"
                type="text"
                placeholder="操作人"
                placeholder-class="input-placeholder"
              />
            </view>

            <view class="input-row">
              <picker mode="selector" :range="operationTypeOptions" :value="operationTypeIndex" @change="onTypeChange">
                <view class="picker">{{ operationTypeOptions[operationTypeIndex] }}</view>
              </picker>
              <picker mode="date" :value="filters.startTime" @change="onStartTimeChange">
                <view class="picker">{{ filters.startTime || '开始日期' }}</view>
              </picker>
            </view>

            <view class="input-row">
              <picker mode="date" :value="filters.endTime" @change="onEndTimeChange">
                <view class="picker">{{ filters.endTime || '结束日期' }}</view>
              </picker>
            </view>

            <view class="action-row">
              <button class="action-btn primary" @click="submitFilters">查询</button>
              <button class="action-btn" @click="resetFilters">重置</button>
            </view>
          </view>

          <view v-if="loading && list.length === 0" class="state-card">
            <text class="state-text">正在加载审计日志...</text>
          </view>

          <view v-else-if="!loading && list.length === 0" class="state-card">
            <text class="state-text">暂无审计日志</text>
          </view>

          <view v-else class="list-wrap">
            <view v-for="item in list" :key="item.id" class="entry-card" @click="openDetail(item)">
              <view class="entry-head">
                <view>
                  <text class="entry-title">{{ operationLabel(item.operationType) }}</text>
                  <text class="entry-sub">{{ item.financeNo || `财务记录 #${item.financeId || '-'}` }}</text>
                </view>
                <text class="entry-time">{{ formatDateTime(item.operationTime) || '未知时间' }}</text>
              </view>
              <text class="entry-summary">{{ item.changeSummary || item.remark || '无变更摘要' }}</text>
              <view class="entry-foot">
                <text class="operator-tag">{{ item.operator || '未知操作人' }}</text>
                <text class="detail-link">查看详情</text>
              </view>
            </view>
          </view>

          <view v-if="list.length > 0" class="load-more">
            <text>{{ hasMore ? (loading ? '加载中...' : '上拉加载更多') : '没有更多日志了' }}</text>
          </view>
        </view>
      </scroll-view>

      <view v-if="detailVisible && detailItem" class="overlay" @click.self="closeDetail">
        <view class="overlay-card">
          <text class="overlay-title">审计详情</text>

          <view class="overlay-row">
            <text class="overlay-label">操作类型</text>
            <text class="overlay-value">{{ operationLabel(detailItem.operationType) }}</text>
          </view>
          <view class="overlay-row">
            <text class="overlay-label">财务流水</text>
            <text class="overlay-value">{{ detailItem.financeNo || `#${detailItem.financeId || '-'}` }}</text>
          </view>
          <view class="overlay-row">
            <text class="overlay-label">操作人</text>
            <text class="overlay-value">{{ detailItem.operator || '未知操作人' }}</text>
          </view>
          <view class="overlay-row">
            <text class="overlay-label">操作时间</text>
            <text class="overlay-value">{{ formatDateTime(detailItem.operationTime) || '未知时间' }}</text>
          </view>
          <view class="overlay-row multiline">
            <text class="overlay-label">变更摘要</text>
            <text class="overlay-value">{{ detailItem.changeSummary || detailItem.remark || '无' }}</text>
          </view>

          <button class="close-btn" @click="closeDetail">关闭</button>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getFinanceAuditDetail, getFinanceAuditPage, type FinanceAuditLogItem } from '@/api/president/audit'
import { formatDateTime } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<FinanceAuditLogItem[]>([])
const detailVisible = ref(false)
const detailItem = ref<FinanceAuditLogItem | null>(null)

const filters = reactive({
  financeNo: '',
  operator: '',
  operationType: '',
  startTime: '',
  endTime: ''
})

const operationTypeOptions = ['全部类型', 'CREATE', 'UPDATE', 'DELETE', 'RECONCILE']

const operationTypeIndex = computed(() => {
  const index = operationTypeOptions.findIndex((item) => item === filters.operationType)
  return index >= 0 ? index : 0
})

const hasMore = computed(() => list.value.length < total.value)

function buildParams() {
  return {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    financeNo: filters.financeNo || undefined,
    operator: filters.operator || undefined,
    operationType: filters.operationType || undefined,
    startTime: filters.startTime || undefined,
    endTime: filters.endTime || undefined
  }
}

async function loadList(append = false) {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getFinanceAuditPage(buildParams())
    const records = Array.isArray(res.records) ? res.records : []
    total.value = Number(res.total || 0)
    list.value = append ? list.value.concat(records) : records
  } catch (error) {
    console.error('Failed to load finance audit logs:', error)
    if (!append) {
      list.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

function submitFilters() {
  pageNum.value = 1
  loadList(false)
}

function resetFilters() {
  filters.financeNo = ''
  filters.operator = ''
  filters.operationType = ''
  filters.startTime = ''
  filters.endTime = ''
  pageNum.value = 1
  loadList(false)
}

function onTypeChange(event: { detail: { value: string } }) {
  const index = Number(event.detail.value)
  filters.operationType = index > 0 ? operationTypeOptions[index] : ''
}

function onStartTimeChange(event: { detail: { value: string } }) {
  filters.startTime = event.detail.value || ''
}

function onEndTimeChange(event: { detail: { value: string } }) {
  filters.endTime = event.detail.value || ''
}

function loadMore() {
  if (loading.value || !hasMore.value) return
  pageNum.value += 1
  loadList(true)
}

async function openDetail(item: FinanceAuditLogItem) {
  if (!item.id) return
  detailVisible.value = true
  detailItem.value = item
  try {
    detailItem.value = await getFinanceAuditDetail(item.id)
  } catch (error) {
    console.error('Failed to load finance audit detail:', error)
  }
}

function closeDetail() {
  detailVisible.value = false
  detailItem.value = null
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.FINANCE_LIST)
}

function operationLabel(type?: string | null) {
  const value = String(type || '').toUpperCase()
  if (value === 'CREATE') return '新增记录'
  if (value === 'UPDATE') return '修改记录'
  if (value === 'DELETE') return '删除记录'
  if (value === 'RECONCILE') return '对账操作'
  return '审计记录'
}

onLoad(() => {
  loadList(false)
})

onPullDownRefresh(() => {
  pageNum.value = 1
  loadList(false).finally(() => uni.stopPullDownRefresh())
})
</script>

<style lang="scss" scoped>
.president-finance-audit-layout {
  :deep(.president-layout-content) {
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
}

.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
}

.nav-header {
  padding: 20rpx 32rpx 16rpx;
  background: #f9f9f9;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.main-scroll {
  height: calc(100vh - var(--status-bar-height) - 88rpx);
}

.scroll-inner {
  padding: 12rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.filter-card,
.state-card,
.entry-card,
.overlay-card {
  border-radius: 24rpx;
  background: #ffffff;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.section-title {
  display: block;
  margin-bottom: 20rpx;
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 700;
}

.input-row,
.action-row,
.entry-head,
.entry-foot,
.overlay-row,
.nav-left {
  display: flex;
  align-items: center;
}

.input-row,
.entry-head,
.entry-foot,
.overlay-row {
  justify-content: space-between;
}

.input-row {
  gap: 16rpx;
  margin-top: 16rpx;
}

.input,
.picker {
  flex: 1;
  min-height: 80rpx;
  border-radius: 18rpx;
  background: #f7f7f7;
  padding: 0 24rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  display: flex;
  align-items: center;
}

.input-placeholder {
  color: #9ca3af;
}

.action-row {
  gap: 16rpx;
  margin-top: 20rpx;
}

.action-btn {
  flex: 1;
  margin: 0;
  border: none;
  border-radius: 9999px;
  background: #f0f0f0;
  color: #1a1c1c;
  font-size: 26rpx;
}

.action-btn::after,
.close-btn::after {
  border: none;
}

.action-btn.primary,
.close-btn {
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
}

.state-text,
.load-more text {
  color: #5f5e5e;
  text-align: center;
  font-size: 28rpx;
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.entry-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.entry-sub,
.entry-time,
.entry-summary,
.operator-tag,
.detail-link {
  color: #5f5e5e;
  font-size: 24rpx;
}

.entry-sub,
.entry-summary {
  display: block;
  margin-top: 10rpx;
}

.detail-link {
  color: #a33e00;
}

.load-more {
  padding: 12rpx 0;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.32);
  display: flex;
  align-items: flex-end;
  padding: 24rpx;
}

.overlay-card {
  width: 100%;
}

.overlay-title {
  display: block;
  margin-bottom: 16rpx;
  font-size: 32rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.overlay-row {
  gap: 20rpx;
  padding: 16rpx 0;
  border-top: 1rpx solid #f1f1f1;
}

.overlay-row:first-of-type {
  border-top: none;
}

.overlay-label {
  color: #8a8a8a;
  font-size: 24rpx;
}

.overlay-value {
  flex: 1;
  text-align: right;
  color: #1a1c1c;
  font-size: 26rpx;
}

.multiline {
  align-items: flex-start;
}

.close-btn {
  margin-top: 20rpx;
  border: none;
  border-radius: 9999px;
}
</style>
