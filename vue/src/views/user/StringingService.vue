<template>
  <div class="user-stringing">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">穿线服务</h1>
      <p class="page-subtitle">专业穿线服务，让您的球拍焕发新生</p>
    </div>

    <!-- Tab切换：申请穿线服务 / 我的穿线服务 -->
    <el-tabs v-model="activeTab" class="stringing-tabs">
      <el-tab-pane label="申请穿线服务" name="apply">
        <!-- 步骤指示器 -->
        <div class="step-indicator" v-if="step > 0">
          <div class="step-indicator-container">
            <div 
              v-for="(stepItem, index) in steps"
              :key="index"
              class="step-item"
              :class="{ 
                active: step > index + 1,
                current: step === index + 1,
                completed: step > index + 1
              }"
            >
              <div class="step-number">
                <el-icon v-if="step > index + 1"><CircleCheck /></el-icon>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="step-label">{{ stepItem }}</div>
              <div class="step-line" v-if="index < steps.length - 1"></div>
            </div>
          </div>
        </div>

        <!-- 步骤1：选择场馆 -->
        <div v-if="step === 1" class="stringing-step">
          <h3 class="step-title">选择场馆</h3>
          <div class="venue-grid">
            <div
              v-for="venue in venueList"
              :key="venue.id"
              class="venue-card"
              :class="{ selected: selectedVenue?.id === venue.id }"
              @click="selectVenue(venue)"
            >
              <div class="venue-icon">
                <el-icon :size="48"><Location /></el-icon>
              </div>
              <div class="venue-info">
                <h4 class="venue-name">{{ venue.venueName }}</h4>
                <p class="venue-address">{{ venue.address || '暂无地址' }}</p>
                <p class="venue-status" :class="getVenueStatusClass(venue.status)">
                  {{ getVenueStatusText(venue.status) }}
                </p>
              </div>
            </div>
          </div>
          <el-empty v-if="venueList.length === 0" description="暂无可用场馆" :image-size="120" />
        </div>

        <!-- 步骤2：填写球拍信息 -->
        <div v-if="step === 2" class="stringing-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 1" text>返回选择场馆</el-button>
            <h3 class="step-title">填写球拍信息</h3>
          </div>
          <div class="form-section">
            <div class="selected-info-card">
              <div class="info-item">
                <span class="info-label">选择场馆</span>
                <span class="info-value">{{ selectedVenue?.venueName }}</span>
              </div>
            </div>
            <el-form :model="serviceForm" label-width="120px" class="stringing-form">
              <el-form-item label="球拍品牌" required>
                <el-input v-model="serviceForm.racketBrand" placeholder="请输入球拍品牌" />
              </el-form-item>
              <el-form-item label="球拍型号" required>
                <el-input v-model="serviceForm.racketModel" placeholder="请输入球拍型号" />
              </el-form-item>
              <el-form-item label="球拍描述">
                <el-input v-model="serviceForm.racketDescription" type="textarea" :rows="3" placeholder="可选，描述球拍的特殊情况" />
              </el-form-item>
              <el-form-item label="是否存在断裂">
                <el-switch v-model="serviceForm.hasBreakage" :active-value="1" :inactive-value="0" />
              </el-form-item>
              <el-form-item label="是否存在塌陷">
                <el-switch v-model="serviceForm.hasCollapse" :active-value="1" :inactive-value="0" />
              </el-form-item>
            </el-form>
            <div class="step-actions">
              <el-button @click="step = 1" size="large">返回</el-button>
              <el-button 
                type="primary" 
                @click="step = 3" 
                :disabled="!serviceForm.racketBrand || !serviceForm.racketModel"
                size="large"
              >
                下一步
              </el-button>
            </div>
          </div>
        </div>

        <!-- 步骤3：选择线材和参数 -->
        <div v-if="step === 3" class="stringing-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 2" text>返回填写球拍信息</el-button>
            <h3 class="step-title">选择线材和参数</h3>
          </div>
          <div class="form-section">
            <div class="selected-info-card">
              <div class="info-item">
                <span class="info-label">球拍</span>
                <span class="info-value">{{ serviceForm.racketBrand }} {{ serviceForm.racketModel }}</span>
              </div>
            </div>
            <el-form :model="serviceForm" label-width="120px" class="stringing-form">
              <el-form-item label="是否自带线材">
                <el-switch v-model="serviceForm.isOwnString" :active-value="1" :inactive-value="0" @change="handleOwnStringChange" />
              </el-form-item>
              <el-form-item v-if="!serviceForm.isOwnString" label="选择线材">
                <el-select v-model="serviceForm.stringId" placeholder="请选择线材" filterable style="width: 100%" @change="handleStringChange">
                  <el-option
                    v-for="string in stringOptions"
                    :key="string.id"
                    :label="`${string.equipmentName} (¥${formatCurrency(string.price)})`"
                    :value="string.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item v-if="serviceForm.isOwnString" label="线材名称" required>
                <el-input v-model="serviceForm.stringName" placeholder="请输入自带线材名称（如：BG65、BG80等）" />
              </el-form-item>
              <el-form-item label="磅数" required>
                <el-input-number
                  v-model="serviceForm.pound"
                  :min="0"
                  :max="50"
                  :precision="1"
                  :step="0.5"
                  style="width: 100%"
                  placeholder="请输入磅数"
                />
              </el-form-item>
              <el-form-item label="穿线法" required>
                <el-radio-group v-model="serviceForm.stringingMethod">
                  <el-radio label="TWO_SECTION">两节</el-radio>
                  <el-radio label="FOUR_SECTION">四节</el-radio>
                  <el-radio label="AUTO">视球拍而定</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="预计价格">
                <div class="price-preview">
                  <span class="price-value">¥{{ formatCurrency(estimatedPrice) }}</span>
                  <span class="price-note">（线材价格 + 手工费20元）</span>
                </div>
              </el-form-item>
            </el-form>
            <div class="step-actions">
              <el-button @click="step = 2" size="large">返回</el-button>
              <el-button 
                type="primary" 
                @click="step = 4" 
                :disabled="!canProceedToStep4"
                size="large"
              >
                下一步
              </el-button>
            </div>
          </div>
        </div>

        <!-- 步骤4：确认信息并提交 -->
        <div v-if="step === 4" class="stringing-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 3" text>返回选择线材</el-button>
            <h3 class="step-title">确认信息并提交</h3>
          </div>
          <div class="confirm-section">
            <div class="confirm-card">
              <h4 class="confirm-title">服务信息</h4>
              <div class="confirm-info">
                <div class="confirm-item">
                  <span class="confirm-label">场馆</span>
                  <span class="confirm-value">{{ selectedVenue?.venueName }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">球拍品牌</span>
                  <span class="confirm-value">{{ serviceForm.racketBrand }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">球拍型号</span>
                  <span class="confirm-value">{{ serviceForm.racketModel }}</span>
                </div>
                <div class="confirm-item" v-if="serviceForm.racketDescription">
                  <span class="confirm-label">球拍描述</span>
                  <span class="confirm-value">{{ serviceForm.racketDescription }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">线材</span>
                  <span class="confirm-value">
                    {{ serviceForm.isOwnString ? `自带：${serviceForm.stringName}` : (selectedString?.equipmentName || '未选择') }}
                  </span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">磅数</span>
                  <span class="confirm-value">{{ serviceForm.pound }} 磅</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">穿线法</span>
                  <span class="confirm-value">{{ getStringingMethodText(serviceForm.stringingMethod) }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">断裂/塌陷</span>
                  <span class="confirm-value">
                    {{ serviceForm.hasBreakage ? '有断裂' : '无断裂' }} / 
                    {{ serviceForm.hasCollapse ? '有塌陷' : '无塌陷' }}
                  </span>
                </div>
              <div class="confirm-item">
                <span class="confirm-label">服务价格</span>
                <span class="confirm-value price">¥{{ formatCurrency(estimatedPrice) }}</span>
              </div>
              <div class="confirm-item">
                <span class="confirm-label">当前会员</span>
                <span class="confirm-value">{{ currentMemberLabel }}</span>
              </div>
              </div>
              <el-form :model="serviceForm" label-width="120px" class="stringing-form" style="margin-top: 24px">
                <el-form-item label="关联会员">
                  <el-input :model-value="currentMemberLabel" disabled />
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="serviceForm.remark" type="textarea" :rows="3" placeholder="可选，填写其他需求或说明" />
                </el-form-item>
              </el-form>
              <div class="confirm-actions">
                <el-button @click="step = 3" size="large">返回修改</el-button>
                <el-button type="primary" :loading="submitting" @click="submitService" size="large">提交申请</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的穿线服务" name="my-services">
        <div class="my-services-section">
          <div class="filter-bar">
            <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px" @change="loadMyServices">
              <el-option label="等待穿线" value="1" />
              <el-option label="正在穿线" value="2" />
              <el-option label="已完成" value="3" />
              <el-option label="已取消" value="0" />
            </el-select>
          </div>

          <div v-if="myServices.length > 0" class="services-list">
            <div
              v-for="service in myServices"
              :key="service.id"
              class="service-item-card"
            >
              <div class="service-main">
                <div class="service-header">
                  <span class="service-no">{{ service.serviceNo }}</span>
                  <el-tag :type="getServiceStatusType(service.status)" size="small">
                    {{ getServiceStatusText(service.status) }}
                  </el-tag>
                </div>
                <div class="service-details">
                  <p class="service-venue">场馆：{{ service.venueName }}</p>
                  <p class="service-racket">球拍：{{ service.racketBrand }} {{ service.racketModel }}</p>
                  <p class="service-string">线材：{{ service.stringName || service.stringEquipmentName || '未选择' }}</p>
                  <p class="service-params">磅数：{{ service.pound }}磅 | 穿线法：{{ getStringingMethodText(service.stringingMethod) }}</p>
                  <p class="service-price">价格：¥{{ formatCurrency(service.servicePrice) }}</p>
                  <p class="service-price">支付状态：{{ getPaymentStatusText(service.paymentStatus) }}</p>
                  <p class="service-time">申请时间：{{ formatDateTime(service.createTime) }}</p>
                </div>
              </div>
              <div class="service-actions">
                <el-button
                  v-if="canPayService(service)"
                  type="primary"
                  size="small"
                  @click="handlePayService(service)"
                >
                  支付
                </el-button>
                <el-button
                  v-if="canCancelService(service)"
                  type="warning"
                  size="small"
                  @click="handleCancelService(service)"
                >
                  取消申请
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无穿线服务记录" :image-size="120" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, CircleCheck, ArrowLeft } from '@element-plus/icons-vue'
import { getVenueList } from '@/api/venue'
import {
  getStringOptions,
  calculateStringingPrice,
  addStringingService,
  getStringingList,
  cancelStringing,
  processMemberStringingPayment
} from '@/api/stringing'
import { getCurrentMember } from '@/api/member'

const activeTab = ref('apply')
const step = ref(1) // 1-选择场馆, 2-填写球拍信息, 3-选择线材和参数, 4-确认信息并提交

// 步骤配置
const steps = ['选择场馆', '填写球拍信息', '选择线材和参数', '确认信息并提交']

const venueList = ref([])
const selectedVenue = ref(null)
const stringOptions = ref([])
const myServices = ref([])
const filterStatus = ref(null)
const submitting = ref(false)
const currentMember = ref(null)
const currentBalance = ref(0)

const serviceForm = ref({
  venueId: null,
  memberId: null,
  racketBrand: '',
  racketModel: '',
  racketDescription: '',
  stringId: null,
  stringName: '',
  isOwnString: 0,
  pound: null,
  stringingMethod: '',
  hasBreakage: 0,
  hasCollapse: 0,
  remark: ''
})

const selectedString = computed(() => {
  if (!serviceForm.value.stringId) return null
  return stringOptions.value.find(s => s.id === serviceForm.value.stringId)
})

const estimatedPrice = ref(20.00) // 默认手工费
const currentMemberLabel = computed(() => {
  const member = currentMember.value
  if (!member?.id) return '当前用户未绑定会员'
  const name = member.memberName || member.nickname || `会员 #${member.id}`
  return member.phone ? `${name} (${member.phone})` : name
})

const canProceedToStep4 = computed(() => {
  if (serviceForm.value.isOwnString === 1) {
    return serviceForm.value.stringName && serviceForm.value.pound && serviceForm.value.stringingMethod
  } else {
    return serviceForm.value.stringId && serviceForm.value.pound && serviceForm.value.stringingMethod
  }
})

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
  return num.toFixed(2)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getVenueStatusText = (status) => {
  const map = { 0: '关闭', 1: '营业中', 2: '暂停' }
  return map[status] || '未知'
}

const getVenueStatusClass = (status) => {
  const map = { 0: 'status-closed', 1: 'status-open', 2: 'status-paused' }
  return map[status] || ''
}

const getServiceStatusText = (status) => {
  const map = { 0: '已取消', 1: '等待穿线', 2: '正在穿线', 3: '已完成' }
  return map[status] || '未知'
}

const getServiceStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
  return map[status] || 'info'
}

const getPaymentStatusText = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '已退款' }
  return map[status] || '未知'
}

const canCancelService = (service) => {
  const status = Number(service?.status ?? -1)
  const paymentStatus = Number(service?.paymentStatus ?? 0)
  return status === 1 && paymentStatus !== 1 && paymentStatus !== 2
}

const canPayService = (service) => {
  const status = Number(service?.status ?? -1)
  const paymentStatus = Number(service?.paymentStatus ?? 0)
  return status === 1 && paymentStatus === 0
}

const getStringingMethodText = (method) => {
  const map = { 'TWO_SECTION': '两节', 'FOUR_SECTION': '四节', 'AUTO': '视球拍而定' }
  return map[method] || method
}

const loadVenues = async () => {
  try {
    const res = await getVenueList({ page: 1, size: 100, status: 1 })
    if (res.code === 200) {
      venueList.value = res.data?.data || res.data || []
    }
  } catch (e) {
    console.error('加载场馆列表失败:', e)
    ElMessage.error('加载场馆列表失败')
  }
}

const loadStringOptions = async () => {
  try {
    const res = await getStringOptions()
    if (res.code === 200) {
      stringOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载线材列表失败:', e)
  }
}

const loadCurrentMemberInfo = async () => {
  try {
    const res = await getCurrentMember()
    if (res.code === 200) {
      currentMember.value = res.data || null
      currentBalance.value = Number(res.data?.balance) || 0
    }
  } catch (e) {
    currentMember.value = null
    currentBalance.value = 0
    console.error('加载当前会员失败:', e)
  }
}

const selectVenue = (venue) => {
  if (venue.status !== 1) {
    ElMessage.warning('该场馆暂未营业')
    return
  }
  selectedVenue.value = venue
  serviceForm.value.venueId = venue.id
  step.value = 2
}

const handleOwnStringChange = async () => {
  if (serviceForm.value.isOwnString === 1) {
    serviceForm.value.stringId = null
    estimatedPrice.value = 20.00 // 自带线材只收手工费
  } else {
    serviceForm.value.stringName = ''
    await handleStringChange()
  }
}

const handleStringChange = async () => {
  if (serviceForm.value.stringId) {
    try {
      const res = await calculateStringingPrice(serviceForm.value.stringId, serviceForm.value.isOwnString)
      if (res.code === 200) {
        estimatedPrice.value = res.data?.price || 20.00
      }
    } catch (e) {
      console.error('计算价格失败:', e)
      estimatedPrice.value = 20.00
    }
  } else {
    estimatedPrice.value = 20.00
  }
}

const submitService = async () => {
  if (!serviceForm.value.racketBrand || !serviceForm.value.racketModel) {
    ElMessage.warning('请填写完整的球拍信息')
    return
  }
  if (!serviceForm.value.pound || !serviceForm.value.stringingMethod) {
    ElMessage.warning('请填写完整的穿线参数')
    return
  }
  if (serviceForm.value.isOwnString === 1 && !serviceForm.value.stringName) {
    ElMessage.warning('自带线材时，请填写线材名称')
    return
  }
  if (serviceForm.value.isOwnString === 0 && !serviceForm.value.stringId) {
    ElMessage.warning('请选择线材')
    return
  }
  if (!currentMember.value?.id) {
    ElMessage.warning('当前用户未绑定会员，无法提交穿线服务')
    return
  }

  submitting.value = true
  try {
    const payload = {
      ...serviceForm.value,
      memberId: currentMember.value.id,
      servicePrice: estimatedPrice.value
    }
    const res = await addStringingService(payload)
    if (res.code === 200) {
      ElMessage.success('申请成功！')
      resetForm()
      activeTab.value = 'my-services'
      loadMyServices()
    } else {
      ElMessage.error(res.message || '申请失败')
    }
  } catch (e) {
    console.error('提交申请失败:', e)
    ElMessage.error('申请失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  selectedVenue.value = null
  serviceForm.value = {
    venueId: null,
    memberId: null,
    racketBrand: '',
    racketModel: '',
    racketDescription: '',
    stringId: null,
    stringName: '',
    isOwnString: 0,
    pound: null,
    stringingMethod: '',
    hasBreakage: 0,
    hasCollapse: 0,
    remark: ''
  }
  estimatedPrice.value = 20.00
  step.value = 1
}

const loadMyServices = async () => {
  try {
    const params = { page: 1, size: 20 }
    if (filterStatus.value) {
      params.status = parseInt(filterStatus.value)
    }
    const res = await getStringingList(params)
    if (res.code === 200) {
      myServices.value = res.data?.data || []
    }
  } catch (e) {
    console.error('加载服务列表失败:', e)
  }
}

const handleCancelService = async (service) => {
  try {
    await ElMessageBox.confirm('确定要取消这个申请吗？', '提示', {
      type: 'warning'
    })
    const res = await cancelStringing(service.id)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadMyServices()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消申请失败:', e)
      ElMessage.error(e?.message || '取消失败，请稍后重试')
    }
  }
}

const handlePayService = (service) => {
  const amount = Number(service?.servicePrice) || 0
  ElMessageBox.confirm(
    `确认使用余额支付该穿线服务吗？\n服务金额：¥${formatCurrency(amount)}\n当前余额：¥${formatCurrency(currentBalance.value)}`,
    '穿线服务支付确认',
    {
      type: 'warning',
      confirmButtonText: '确认支付',
      cancelButtonText: '稍后支付'
    }
  ).then(async () => {
    try {
      const res = await processMemberStringingPayment(service.id, 'BALANCE')
      if (res.code === 200) {
        ElMessage.success('支付成功')
        await Promise.all([loadCurrentMemberInfo(), loadMyServices()])
      } else {
        ElMessage.error(res.message || '支付失败')
      }
    } catch (e) {
      console.error('穿线服务支付失败:', e)
      ElMessage.error(e.response?.data?.message || e.message || '支付失败，请稍后重试')
    }
  }).catch(() => {})
}

watch(activeTab, (newTab) => {
  if (newTab === 'my-services') {
    loadMyServices()
  }
})

onMounted(() => {
  loadVenues()
  loadStringOptions()
  loadCurrentMemberInfo()
  if (activeTab.value === 'my-services') {
    loadMyServices()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-stringing {
  padding: 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 减少动画（尊重用户的偏好设置） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

.page-header {
  margin-bottom: 32px;
  padding: 24px 0;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg,
    var(--color-primary, #2563EB) 0%,
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.6;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg,
    transparent 0%,
    var(--color-border, rgba(226, 232, 240, 0.5)) 50%,
    transparent 100%);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  font-family: 'Poppins', 'Inter', sans-serif;
  position: relative;
  padding-left: 16px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  background: linear-gradient(135deg,
    var(--color-text-primary, #1E293B) 0%,
    var(--color-primary, #2563EB) 50%,
    var(--color-secondary, #8B5CF6) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  text-shadow: 0 2px 8px rgba(37, 99, 235, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg,
    var(--color-primary, #2563EB) 0%,
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.8;
}

.page-title:hover {
  transform: translateX(4px);
  background-position: 100% 50%;
}

.page-subtitle {
  font-size: 17px;
  margin: 0;
  padding-left: 16px;
  line-height: 1.6;
  letter-spacing: 0.3px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s forwards;
  opacity: 0;
  background: linear-gradient(135deg,
    var(--color-text-secondary, #64748B) 0%,
    var(--color-primary, #2563EB) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: var(--color-text-secondary, #64748B);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-subtitle:hover {
  transform: translateX(4px);
  opacity: 1;
}

/* 深色模式适配 */
html.theme-dark-mode .page-title {
  background: linear-gradient(135deg,
    var(--color-text-primary, #F1F5F9) 0%,
    var(--color-primary, #60A5FA) 50%,
    var(--color-secondary, #A78BFA) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 8px rgba(96, 165, 250, 0.2);
}

html.theme-dark-mode .page-subtitle {
  background: linear-gradient(135deg,
    var(--color-text-secondary, #94A3B8) 0%,
    var(--color-primary, #60A5FA) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 无障碍：减少动画 */
@media (prefers-reduced-motion: reduce) {
  .page-title,
  .page-subtitle {
    animation: none;
    opacity: 1;
  }
  .page-title:hover,
  .page-subtitle:hover {
    transform: none;
  }
}

.stringing-tabs {
  margin-top: 24px;
}

.step-indicator {
  margin-bottom: 32px;
  padding: 24px;
  background: var(--color-card-bg, #FFFFFF);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.step-indicator-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  max-width: 800px;
  margin: 0 auto;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background, #F1F5F9);
  border: 3px solid var(--color-border, #E2E8F0);
  color: var(--color-text-secondary, #64748B);
  font-weight: 600;
  font-size: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.step-item.current .step-number {
  background: var(--color-primary, #2563EB);
  border-color: var(--color-primary, #2563EB);
  color: #ffffff;
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  animation: pulseStep 2s ease-in-out infinite;
}

.step-item.completed .step-number {
  background: var(--color-success, #10B981);
  border-color: var(--color-success, #10B981);
  color: #ffffff;
  transform: scale(1.05);
}

.step-item.completed .step-number .el-icon {
  font-size: 20px;
}

@keyframes pulseStep {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  }
  50% {
    box-shadow: 0 4px 24px rgba(37, 99, 235, 0.6);
  }
}

.step-label {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: center;
}

.step-item.current .step-label {
  color: var(--color-primary, #2563EB);
  font-weight: 600;
}

.step-item.completed .step-label {
  color: var(--color-success, #10B981);
}

.step-line {
  position: absolute;
  top: 20px;
  left: calc(50% + 20px);
  right: calc(-50% + 20px);
  height: 3px;
  background: var(--color-border, #E2E8F0);
  z-index: 0;
  transition: all 0.4s ease;
}

.step-item.completed + .step-item .step-line {
  background: var(--color-success, #10B981);
}

.step-item.current .step-line {
  background: linear-gradient(90deg, var(--color-success, #10B981) 0%, var(--color-border, #E2E8F0) 100%);
}

.stringing-step {
  padding: 24px 0;
  animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 24px 0;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.venue-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.venue-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.venue-card:hover {
  border-color: var(--color-primary, #2563EB);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
  transform: translateY(-2px);
}

.venue-card.selected {
  border-color: var(--color-primary, #2563EB);
  background: var(--color-primary-light, #EFF6FF);
}

.venue-icon {
  text-align: center;
  color: var(--color-primary, #2563EB);
  margin-bottom: 16px;
}

.venue-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--color-text-primary, #1E293B);
}

.venue-address {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 0 0 8px 0;
}

.venue-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}

.status-open {
  background: #D1FAE5;
  color: #065F46;
}

.status-closed {
  background: #FEE2E2;
  color: #991B1B;
}

.status-paused {
  background: #FEF3C7;
  color: #92400E;
}

.form-section {
  max-width: 800px;
  margin: 0 auto;
}

.selected-info-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.stringing-form {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
}

.price-preview {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary, #2563EB);
}

.price-note {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.confirm-section {
  max-width: 800px;
  margin: 0 auto;
}

.confirm-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-primary, #2563EB);
  border-radius: 16px;
  padding: 32px;
  animation: scaleIn 0.4s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.confirm-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: var(--color-text-primary, #1E293B);
  text-align: center;
}

.confirm-info {
  margin-bottom: 32px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.confirm-item:last-child {
  border-bottom: none;
}

.confirm-label {
  font-size: 15px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
}

.confirm-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.confirm-value.price {
  font-size: 20px;
  color: var(--color-primary, #2563EB);
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.my-services-section {
  padding: 24px 0;
}

.filter-bar {
  margin-bottom: 24px;
}

.services-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.service-item-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.service-item-card:hover {
  border-color: var(--color-primary, #2563EB);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
}

.service-main {
  flex: 1;
}

.service-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.service-no {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.service-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.service-details p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
}

.service-actions {
  display: flex;
  gap: 8px;
}

/* 响应式 - 移动端优化 */
@media (max-width: 768px) {
  .venue-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .venue-card {
    padding: 20px;
  }

  .service-item-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .service-actions {
    width: 100%;
    justify-content: flex-end;
  }

  /* 移动端触摸优化 */
  .venue-card,
  .service-item-card {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}
</style>
