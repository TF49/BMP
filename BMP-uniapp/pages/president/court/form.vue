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
            <text class="nav-title">{{ pageTitle }}</text>
          </view>
          <view class="nav-actions">
            <view v-if="isEdit && !loading" class="status-badge" :class="statusMeta.className">
              <text>{{ statusMeta.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false">
        <view class="content">
          <view v-if="loading" class="state-card">
            <text class="state-text">正在加载场地信息...</text>
          </view>

          <view v-else-if="loadError" class="state-card error">
            <text class="state-text">{{ loadError }}</text>
            <button class="retry-btn" @click="loadInitialData">重新加载</button>
          </view>

          <template v-else>
            <view class="hero-card">
              <view>
                <text class="hero-label">真实场地配置</text>
                <text class="hero-title">{{ isEdit ? '编辑场地信息' : '新增场地' }}</text>
                <text class="hero-subtitle">提交后将直接写入真实场地管理数据</text>
              </view>
              <view class="hero-price">
                <text class="hero-price-label">{{ billingTypeLabel }}</text>
                <text class="hero-price-value">¥{{ pricePreview }}</text>
              </view>
            </view>

            <view class="card">
              <text class="card-title">基础信息</text>
              <view class="field-list">
                <view class="field-item">
                  <text class="field-label">场地编号</text>
                  <input
                    v-model.trim="form.courtCode"
                    class="field-input"
                    maxlength="30"
                    placeholder="例如 A01"
                  />
                </view>
                <view class="field-item">
                  <text class="field-label">场地名称</text>
                  <input
                    v-model.trim="form.courtName"
                    class="field-input"
                    maxlength="50"
                    placeholder="例如 1 号羽毛球场"
                  />
                </view>
                <view class="field-item">
                  <text class="field-label">所属场馆</text>
                  <picker mode="selector" :range="venueLabels" :value="venueIndex" @change="onVenueChange">
                    <view class="picker-field">
                      <text :class="['picker-text', { placeholder: venueIndex < 0 }]">
                        {{ venueIndex >= 0 ? venueLabels[venueIndex] : '请选择场馆' }}
                      </text>
                      <uni-icons type="bottom" size="14" color="#71717a" />
                    </view>
                  </picker>
                </view>
              </view>
            </view>

            <view class="card">
              <text class="card-title">计费与状态</text>
              <view class="field-list">
                <view class="field-item">
                  <text class="field-label">计费方式</text>
                  <picker mode="selector" :range="billingTypeOptions" :value="billingTypeIndex" @change="onBillingTypeChange">
                    <view class="picker-field">
                      <text class="picker-text">{{ billingTypeOptions[billingTypeIndex] }}</text>
                      <uni-icons type="bottom" size="14" color="#71717a" />
                    </view>
                  </picker>
                </view>
                  <view class="field-item">
                    <text class="field-label">兼容基础价格</text>
                    <input
                      v-model.trim="form.pricePerHour"
                      class="field-input"
                      type="digit"
                      placeholder="请输入价格"
                    />
                  </view>
                  <view class="field-item">
                    <text class="field-label">包场每小时价格</text>
                    <view class="marketing-row">
                      <input
                        v-model.trim="form.packagePricePerHour"
                        class="field-input marketing-input"
                        type="digit"
                        placeholder="请输入包场每小时价格"
                      />
                      <view class="marketing-switch">
                        <text>开放包场按小时</text>
                        <switch :checked="form.enablePackageHour" color="#ff6600" @change="onMarketingToggle('enablePackageHour', $event)" />
                      </view>
                    </view>
                  </view>
                  <view class="field-item">
                    <text class="field-label">拼场每小时价格</text>
                    <view class="marketing-row">
                      <input
                        v-model.trim="form.sharedPricePerHour"
                        class="field-input marketing-input"
                        type="digit"
                        placeholder="请输入拼场每小时价格"
                      />
                      <view class="marketing-switch">
                        <text>开放拼场按小时</text>
                        <switch :checked="form.enableSharedHour" color="#ff6600" @change="onMarketingToggle('enableSharedHour', $event)" />
                      </view>
                    </view>
                  </view>
                  <view class="field-item">
                    <text class="field-label">拼场按次价格</text>
                    <view class="marketing-row">
                      <input
                        v-model.trim="form.sharedPricePerTime"
                        class="field-input marketing-input"
                        type="digit"
                        placeholder="请输入拼场按次价格"
                      />
                      <view class="marketing-switch">
                        <text>开放拼场按次</text>
                        <switch :checked="form.enableSharedTime" color="#ff6600" @change="onMarketingToggle('enableSharedTime', $event)" />
                      </view>
                    </view>
                  </view>
                <view class="field-item">
                  <text class="field-label">状态</text>
                  <picker mode="selector" :range="statusLabels" :value="statusIndex" @change="onStatusChange">
                    <view class="picker-field">
                      <text class="picker-text">{{ statusLabels[statusIndex] }}</text>
                      <uni-icons type="bottom" size="14" color="#71717a" />
                    </view>
                  </picker>
                </view>
              </view>
            </view>

            <view class="summary-card">
              <view class="summary-item">
                <text class="summary-label">场馆</text>
                <text class="summary-value">{{ currentVenueLabel }}</text>
              </view>
              <view class="summary-item">
                <text class="summary-label">计费方式</text>
                <text class="summary-value">{{ billingTypeLabel }}</text>
              </view>
              <view class="summary-item">
                <text class="summary-label">包场每小时</text>
                <text class="summary-value">¥{{ formatAmount(Number(form.packagePricePerHour || 0)) }}</text>
              </view>
              <view class="summary-item">
                <text class="summary-label">拼场每小时 / 按次</text>
                <text class="summary-value">¥{{ formatAmount(Number(form.sharedPricePerHour || 0)) }} / ¥{{ formatAmount(Number(form.sharedPricePerTime || 0)) }}</text>
              </view>
              <view class="summary-item">
                <text class="summary-label">已开放营销方式</text>
                <text class="summary-value">{{ marketingSummary }}</text>
              </view>
              <view class="summary-item">
                <text class="summary-label">状态</text>
                <text class="summary-value">{{ statusMeta.label }}</text>
              </view>
            </view>

            <view class="footer-actions">
              <button class="secondary-btn" :disabled="submitting" @click="goBack">取消</button>
              <button class="primary-btn" :disabled="submitting" @click="submitForm">
                {{ submitting ? '提交中...' : isEdit ? '保存修改' : '确认新增' }}
              </button>
            </view>
          </template>
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
  addCourt,
  getCourtDetail,
  getCourtVenueOptions,
  updateCourt,
  type CourtPayload
} from '@/api/court'
import { formatAmount } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getCourtStatusMeta } from '@/utils/presidentStatus'

type VenueOption = {
  id: number
  venueName: string
}

const routeId = ref<number | null>(null)
const isEdit = computed(() => routeId.value !== null)
const loading = ref(false)
const loadError = ref('')
const submitting = ref(false)

const venueOptions = ref<VenueOption[]>([])
const venueIndex = ref(-1)
const billingTypeIndex = ref(0)
const statusIndex = ref(1)

const billingTypeValues: Array<'HOUR' | 'TIME'> = ['HOUR', 'TIME']
const billingTypeOptions = ['按小时计费', '按次计费']
const statusValues = [0, 1, 2, 3]
const statusLabels = ['维护中', '可预约', '使用中', '已预约']

const form = reactive({
  courtCode: '',
  courtName: '',
  venueId: 0,
  pricePerHour: '',
  packagePricePerHour: '',
  sharedPricePerHour: '',
  sharedPricePerTime: '',
  enablePackageHour: true,
  enableSharedHour: true,
  enableSharedTime: true
})

const pageTitle = computed(() => (isEdit.value ? '编辑场地' : '新增场地'))
const venueLabels = computed(() => venueOptions.value.map((item) => item.venueName))
const currentVenueLabel = computed(() =>
  venueIndex.value >= 0 ? venueLabels.value[venueIndex.value] : '未选择场馆'
)
const billingTypeLabel = computed(() => billingTypeOptions[billingTypeIndex.value])
const pricePreview = computed(() => formatAmount(Number(form.pricePerHour || 0)))
const statusMeta = computed(() => getCourtStatusMeta(statusValues[statusIndex.value]))
const marketingSummary = computed(() => {
  const items: string[] = []
  if (form.enablePackageHour) items.push('包场按小时')
  if (form.enableSharedHour) items.push('拼场按小时')
  if (form.enableSharedTime) items.push('拼场按次')
  return items.length ? items.join(' / ') : '未开放'
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURT_LIST)
}

function onVenueChange(e: { detail?: { value?: string } }) {
  const index = Number(e.detail?.value ?? -1)
  venueIndex.value = index
  form.venueId = venueOptions.value[index]?.id || 0
}

function onBillingTypeChange(e: { detail?: { value?: string } }) {
  billingTypeIndex.value = Number(e.detail?.value ?? 0)
}

function onStatusChange(e: { detail?: { value?: string } }) {
  statusIndex.value = Number(e.detail?.value ?? 1)
}

function onMarketingToggle(field: 'enablePackageHour' | 'enableSharedHour' | 'enableSharedTime', event: any) {
  form[field] = !!event.detail?.value
}

function fillForm(detail: {
  courtCode?: string
  courtName?: string
  venueId?: number
  billingType?: string
  pricePerHour?: number
  pricePerTime?: number
  packagePricePerHour?: number
  sharedPricePerHour?: number
  sharedPricePerTime?: number
  enablePackageHour?: boolean
  enableSharedHour?: boolean
  enableSharedTime?: boolean
  status?: number
}) {
  form.courtCode = detail.courtCode || ''
  form.courtName = detail.courtName || ''
  form.venueId = Number(detail.venueId || 0)
  form.pricePerHour = String(detail.pricePerHour ?? detail.pricePerTime ?? '')
  form.packagePricePerHour = String(detail.packagePricePerHour ?? detail.pricePerHour ?? '')
  form.sharedPricePerHour = String(detail.sharedPricePerHour ?? detail.pricePerHour ?? '')
  form.sharedPricePerTime = String(detail.sharedPricePerTime ?? detail.pricePerTime ?? '')
  form.enablePackageHour = detail.enablePackageHour !== false
  form.enableSharedHour = detail.enableSharedHour !== false
  form.enableSharedTime = detail.enableSharedTime !== false
  billingTypeIndex.value = detail.billingType === 'TIME' ? 1 : 0
  const nextStatusIndex = statusValues.findIndex((item) => item === Number(detail.status))
  statusIndex.value = nextStatusIndex >= 0 ? nextStatusIndex : 1
  const nextVenueIndex = venueOptions.value.findIndex((item) => item.id === form.venueId)
  venueIndex.value = nextVenueIndex
}

async function loadInitialData() {
  loading.value = true
  loadError.value = ''
  try {
    const venues = await getCourtVenueOptions()
    venueOptions.value = Array.isArray(venues) ? venues.filter((item) => item?.id) : []

    if (routeId.value) {
      const detail = await getCourtDetail(routeId.value)
      fillForm(detail)
    }
  } catch (error) {
    console.error('Failed to load court form data:', error)
    loadError.value = '场地表单加载失败'
  } finally {
    loading.value = false
  }
}

function validateForm() {
  if (!form.courtCode) return '请输入场地编号'
  if (!form.courtName) return '请输入场地名称'
  if (!form.venueId) return '请选择所属场馆'
  const price = Number(form.pricePerHour)
  const packagePrice = Number(form.packagePricePerHour)
  const sharedHourPrice = Number(form.sharedPricePerHour)
  const sharedTimePrice = Number(form.sharedPricePerTime)
  if (!Number.isFinite(price) || price < 0) return '请输入有效兼容基础价格'
  if (!form.enablePackageHour && !form.enableSharedHour && !form.enableSharedTime) return '至少需要开放一种营销方式'
  if (form.enablePackageHour && (!Number.isFinite(packagePrice) || packagePrice <= 0)) return '开放包场按小时后，包场每小时价格必须大于0'
  if (form.enableSharedHour && (!Number.isFinite(sharedHourPrice) || sharedHourPrice <= 0)) return '开放拼场按小时后，拼场每小时价格必须大于0'
  if (form.enableSharedTime && (!Number.isFinite(sharedTimePrice) || sharedTimePrice <= 0)) return '开放拼场按次后，拼场按次价格必须大于0'
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
    const payload: CourtPayload = {
      id: routeId.value || undefined,
      courtCode: form.courtCode,
      courtName: form.courtName,
      venueId: form.venueId,
      billingType: billingTypeValues[billingTypeIndex.value],
      pricePerHour: Number(form.pricePerHour),
      packagePricePerHour: Number(form.packagePricePerHour),
      sharedPricePerHour: Number(form.sharedPricePerHour),
      sharedPricePerTime: Number(form.sharedPricePerTime),
      enablePackageHour: form.enablePackageHour,
      enableSharedHour: form.enableSharedHour,
      enableSharedTime: form.enableSharedTime,
      status: statusValues[statusIndex.value]
    }

    if (isEdit.value) {
      await updateCourt(payload)
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: `${PRESIDENT_PAGES.COURT_DETAIL}?id=${routeId.value}` })
      }, 400)
      return
    }

    const result = await addCourt(payload)
    uni.showToast({ title: '新增成功', icon: 'success' })
    setTimeout(() => {
      if (result?.id) {
        uni.redirectTo({ url: `${PRESIDENT_PAGES.COURT_DETAIL}?id=${result.id}` })
      } else {
        goBack()
      }
    }, 400)
  } catch (error) {
    console.error('Failed to submit court form:', error)
  } finally {
    submitting.value = false
  }
}

onLoad((options) => {
  const rawId = Number(options?.id)
  if (Number.isFinite(rawId) && rawId > 0) {
    routeId.value = rawId
  }
  loadInitialData()
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
.picker-field,
.footer-actions {
  display: flex;
  align-items: center;
}

.nav-row,
.footer-actions {
  justify-content: space-between;
}

.nav-left,
.nav-actions {
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

.status-badge {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  color: #ffffff;
  font-size: 22rpx;
}

.status-badge.available {
  background: #16a34a;
}

.status-badge.inuse {
  background: #ea580c;
}

.status-badge.booked {
  background: #2563eb;
}

.status-badge.maintenance,
.status-badge.unknown {
  background: #6b7280;
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
.hero-price-label,
.field-label,
.summary-label,
.state-text {
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

.hero-price {
  min-width: 180rpx;
  text-align: right;
}

.hero-price-value {
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

.field-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 24rpx;
}

.field-item {
  padding: 24rpx;
  border-radius: 18rpx;
  background: #f8fafc;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
}

.field-input,
.picker-field {
  min-height: 72rpx;
  font-size: 28rpx;
  color: #1a1c1c;
}

.marketing-row {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.marketing-input {
  width: 100%;
}

.marketing-switch {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  font-size: 24rpx;
  color: #71717a;
}

.picker-field {
  justify-content: space-between;
}

.picker-text.placeholder {
  color: #a1a1aa;
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
  font-size: 28rpx;
  font-weight: 700;
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
