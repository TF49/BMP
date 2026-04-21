<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="icon-btn">
              <uni-icons type="arrow-left" size="22" color="#ff6600" />
            </view>
            <text class="nav-title">新增租借</text>
          </view>
          <view class="nav-actions">
            <view class="icon-btn" @click="loadOptions">
              <uni-icons type="refresh" size="20" color="#71717a" />
            </view>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-card">
            <view>
              <text class="hero-label">真实租借录入</text>
              <text class="hero-title">创建器材租借单</text>
              <text class="hero-subtitle">会员和器材候选数据均来自真实接口</text>
            </view>
            <view class="hero-amount">
              <text class="hero-amount-label">预估金额</text>
              <text class="hero-amount-value">¥{{ rentalAmountLabel }}</text>
            </view>
          </view>

          <view v-if="loadError" class="state-card error">
            <text class="state-text">{{ loadError }}</text>
            <button class="retry-btn" @click="loadOptions">重新加载</button>
          </view>

          <view class="card">
            <view class="card-head">
              <text class="card-title">选择会员</text>
              <text class="card-tip">搜索后从真实会员列表中选择</text>
            </view>
            <view class="search-row">
              <input
                v-model.trim="memberKeyword"
                class="search-input"
                type="text"
                placeholder="输入会员姓名或手机号"
                confirm-type="search"
                @confirm="searchMembers"
              />
              <button class="mini-btn" :disabled="memberLoading" @click="searchMembers">搜索</button>
            </view>
            <view v-if="memberLoading" class="sub-state">正在加载会员...</view>
            <view v-else-if="memberOptions.length === 0" class="sub-state">暂无可选会员</view>
            <view v-else class="option-list">
              <view
                v-for="item in memberOptions"
                :key="item.id"
                class="option-item"
                :class="{ active: form.memberId === item.id }"
                @click="selectMember(item)"
              >
                <view>
                  <text class="option-title">{{ item.memberName || `会员 #${item.id}` }}</text>
                  <text class="option-subtitle">{{ item.phone || '未登记手机号' }}</text>
                </view>
                <uni-icons v-if="form.memberId === item.id" type="checkmarkempty" size="20" color="#ff6600" />
              </view>
            </view>
          </view>

          <view class="card">
            <view class="card-head">
              <text class="card-title">选择器材</text>
              <text class="card-tip">按名称加载真实器材库存</text>
            </view>
            <view class="search-row">
              <input
                v-model.trim="equipmentKeyword"
                class="search-input"
                type="text"
                placeholder="输入器材名称"
                confirm-type="search"
                @confirm="searchEquipment"
              />
              <button class="mini-btn" :disabled="equipmentLoading" @click="searchEquipment">搜索</button>
            </view>
            <view v-if="equipmentLoading" class="sub-state">正在加载器材...</view>
            <view v-else-if="equipmentOptions.length === 0" class="sub-state">暂无可选器材</view>
            <view v-else class="option-list">
              <view
                v-for="item in equipmentOptions"
                :key="item.id"
                class="option-item"
                :class="{ active: form.equipmentId === item.id }"
                @click="selectEquipment(item)"
              >
                <view>
                  <text class="option-title">{{ item.equipmentName || `器材 #${item.id}` }}</text>
                  <text class="option-subtitle">
                    可用 {{ item.availableQuantity ?? 0 }} / 租金 ¥{{ formatAmount(Number(item.rentalPrice || item.price || 0)) }}
                  </text>
                </view>
                <uni-icons v-if="form.equipmentId === item.id" type="checkmarkempty" size="20" color="#ff6600" />
              </view>
            </view>
          </view>

          <view class="card">
            <text class="card-title">租借信息</text>
            <view class="field-list">
              <view class="field-item">
                <text class="field-label">数量</text>
                <input v-model.trim="form.quantity" class="field-input" type="number" placeholder="请输入数量" />
              </view>
              <view class="field-item">
                <text class="field-label">租借日期</text>
                <picker mode="date" :value="form.rentalDate" @change="onRentalDateChange">
                  <view class="picker-field">
                    <text class="picker-value">{{ form.rentalDate || '请选择租借日期' }}</text>
                    <uni-icons type="calendar" size="16" color="#71717a" />
                  </view>
                </picker>
              </view>
              <view class="field-item">
                <text class="field-label">预计归还日期</text>
                <picker mode="date" :value="form.expectedReturnDate" @change="onExpectedDateChange">
                  <view class="picker-field">
                    <text class="picker-value">{{ form.expectedReturnDate || '请选择归还日期' }}</text>
                    <uni-icons type="calendar" size="16" color="#71717a" />
                  </view>
                </picker>
              </view>
              <view class="field-item">
                <text class="field-label">支付方式</text>
                <picker mode="selector" :range="paymentLabels" :value="paymentIndex" @change="onPaymentChange">
                  <view class="picker-field">
                    <text class="picker-value">{{ paymentLabels[paymentIndex] }}</text>
                    <uni-icons type="bottom" size="14" color="#71717a" />
                  </view>
                </picker>
              </view>
              <view class="field-item">
                <text class="field-label">备注</text>
                <textarea
                  v-model.trim="form.remark"
                  class="textarea"
                  maxlength="300"
                  placeholder="填写备注信息"
                />
              </view>
            </view>
          </view>

          <view class="summary-card">
            <view class="summary-item">
              <text class="summary-label">单价</text>
              <text class="summary-value">¥{{ unitPriceLabel }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">数量</text>
              <text class="summary-value">{{ safeQuantity }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">预计金额</text>
              <text class="summary-value">¥{{ rentalAmountLabel }}</text>
            </view>
          </view>

          <view class="footer-actions">
            <button class="secondary-btn" :disabled="submitting" @click="goBack">取消</button>
            <button class="primary-btn" :disabled="submitting" @click="submitForm">
              {{ submitting ? '提交中...' : '确认创建' }}
            </button>
          </view>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import {
  createEquipmentRental,
  getEquipmentList,
  type EquipmentItem
} from '@/api/president/equipment'
import { getMemberList, type MemberListItem } from '@/api/president/member'
import { formatAmount, formatDate } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const memberOptions = ref<MemberListItem[]>([])
const equipmentOptions = ref<EquipmentItem[]>([])
const memberKeyword = ref('')
const equipmentKeyword = ref('')
const memberLoading = ref(false)
const equipmentLoading = ref(false)
const loadError = ref('')
const submitting = ref(false)
const paymentIndex = ref(0)

const paymentValues = ['BALANCE']
const paymentLabels = ['余额支付']

const form = reactive({
  memberId: 0,
  equipmentId: 0,
  quantity: '1',
  rentalDate: '',
  expectedReturnDate: '',
  remark: ''
})

const selectedEquipment = computed(() =>
  equipmentOptions.value.find((item) => item.id === form.equipmentId) || null
)
const safeQuantity = computed(() => {
  const value = Number(form.quantity)
  return Number.isFinite(value) && value > 0 ? value : 0
})
const unitPrice = computed(() => Number(selectedEquipment.value?.rentalPrice || selectedEquipment.value?.price || 0))
const rentalAmount = computed(() => safeQuantity.value * unitPrice.value)
const unitPriceLabel = computed(() => formatAmount(unitPrice.value))
const rentalAmountLabel = computed(() => formatAmount(rentalAmount.value))

function setDefaultDates() {
  const today = formatDate(new Date())
  form.rentalDate = form.rentalDate || today
  form.expectedReturnDate = form.expectedReturnDate || today
}

async function searchMembers() {
  memberLoading.value = true
  try {
    const res = await getMemberList({
      memberName: memberKeyword.value || undefined,
      page: 1,
      size: 10
    })
    memberOptions.value = Array.isArray(res?.data) ? res.data : []
  } catch (error) {
    console.error('Failed to search members:', error)
    memberOptions.value = []
    loadError.value = '会员数据加载失败'
  } finally {
    memberLoading.value = false
  }
}

async function searchEquipment() {
  equipmentLoading.value = true
  try {
    const res = await getEquipmentList({
      equipmentName: equipmentKeyword.value || undefined,
      page: 1,
      size: 10,
      status: 1
    })
    equipmentOptions.value = Array.isArray(res?.data) ? res.data : []
  } catch (error) {
    console.error('Failed to search equipment:', error)
    equipmentOptions.value = []
    loadError.value = '器材数据加载失败'
  } finally {
    equipmentLoading.value = false
  }
}

async function loadOptions() {
  loadError.value = ''
  setDefaultDates()
  await Promise.all([searchMembers(), searchEquipment()])
}

function selectMember(item: MemberListItem) {
  form.memberId = Number(item.id || 0)
}

function selectEquipment(item: EquipmentItem) {
  form.equipmentId = Number(item.id || 0)
}

function onRentalDateChange(e: { detail?: { value?: string } }) {
  form.rentalDate = String(e.detail?.value || '')
}

function onExpectedDateChange(e: { detail?: { value?: string } }) {
  form.expectedReturnDate = String(e.detail?.value || '')
}

function onPaymentChange(e: { detail?: { value?: string } }) {
  paymentIndex.value = Number(e.detail?.value ?? 0)
}

function validateForm() {
  if (!form.memberId) return '请选择会员'
  if (!form.equipmentId) return '请选择器材'
  if (!safeQuantity.value) return '请输入有效数量'
  if (!form.rentalDate) return '请选择租借日期'
  if (!form.expectedReturnDate) return '请选择预计归还日期'
  return ''
}

async function submitForm() {
  if (submitting.value) return

  const errorText = validateForm()
  if (errorText) {
    uni.showToast({ title: errorText, icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const result = await createEquipmentRental({
      memberId: form.memberId,
      equipmentId: form.equipmentId,
      quantity: safeQuantity.value,
      rentalDate: form.rentalDate,
      expectedReturnDate: form.expectedReturnDate,
      rentalAmount: rentalAmount.value,
      unitPrice: unitPrice.value,
      depositAmount: 0,
      durationHours: 24,
      paymentMethod: paymentValues[paymentIndex.value],
      paymentStatus: 1,
      status: 1,
      remark: form.remark || undefined
    })
    uni.showToast({ title: '创建成功', icon: 'success' })
    setTimeout(() => {
      if (result?.id) {
        uni.redirectTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_RENTAL_DETAIL}?id=${result.id}` })
      } else {
        goBack()
      }
    }, 400)
  } catch (error) {
    console.error('Failed to create equipment rental:', error)
  } finally {
    submitting.value = false
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_RENTAL_LIST)
}

onLoad(() => {
  loadOptions()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  position: sticky;
  top: 0;
  z-index: 30;
  padding: 16rpx 24rpx;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(12px);
}

.nav-row,
.nav-left,
.nav-actions,
.card-head,
.search-row,
.picker-field,
.footer-actions {
  display: flex;
  align-items: center;
}

.nav-row,
.card-head,
.footer-actions {
  justify-content: space-between;
}

.nav-left {
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
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
  height: calc(100vh - var(--status-bar-height) - 104rpx);
}

.content {
  padding: 12rpx 24rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.hero-card,
.card,
.summary-card,
.state-card {
  padding: 32rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  background: linear-gradient(135deg, #fff3eb 0%, #ffffff 100%);
}

.hero-label,
.hero-amount-label,
.card-tip,
.field-label,
.summary-label,
.sub-state,
.state-text,
.option-subtitle {
  color: #71717a;
  font-size: 24rpx;
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  font-size: 42rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.hero-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #71717a;
}

.hero-amount {
  min-width: 180rpx;
  text-align: right;
}

.hero-amount-value {
  display: block;
  margin-top: 12rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #ff6600;
}

.card-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.search-row {
  gap: 16rpx;
  margin-top: 20rpx;
}

.search-input,
.field-input,
.picker-field,
.textarea {
  width: 100%;
  background: #f8fafc;
  border-radius: 18rpx;
}

.search-input,
.field-input,
.picker-field {
  min-height: 80rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
}

.picker-field {
  justify-content: space-between;
}

.mini-btn {
  min-width: 120rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 18rpx;
  background: #fff7ed;
  color: #a33e00;
  font-size: 24rpx;
}

.option-list,
.field-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 20rpx;
}

.option-item,
.field-item {
  padding: 22rpx 24rpx;
  border-radius: 18rpx;
  background: #f8fafc;
}

.option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.option-item.active {
  background: #fff7ed;
  border: 1rpx solid #fed7aa;
}

.option-title,
.summary-value {
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
}

.picker-value {
  font-size: 28rpx;
  color: #1a1c1c;
}

.textarea {
  min-height: 160rpx;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
}

.summary-card {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.summary-item {
  padding: 20rpx;
  border-radius: 18rpx;
  background: #fff7ed;
}

.summary-value {
  display: block;
  margin-top: 10rpx;
  color: #a33e00;
}

.footer-actions {
  gap: 16rpx;
}

.primary-btn,
.secondary-btn,
.retry-btn {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
}

.primary-btn {
  background: #ff6600;
  color: #ffffff;
}

.secondary-btn {
  background: #ffffff;
  color: #1a1c1c;
  border: 1rpx solid #e5e7eb;
}

.retry-btn {
  margin-top: 20rpx;
  background: #ff6600;
  color: #ffffff;
}

.error .state-text {
  color: #b91c1c;
}
</style>
