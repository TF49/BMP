<template>
  <div class="user-equipment">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">器材租借</h1>
      <p class="page-subtitle">租借运动器材，享受运动乐趣</p>
    </div>

    <!-- Tab切换：租借器材 / 我的租借 -->
    <el-tabs v-model="activeTab" class="equipment-tabs">
      <el-tab-pane label="租借器材" name="rent">
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

        <!-- 步骤1: 选择器材 -->
        <div v-if="step === 1" class="equipment-step">
          <h3 class="step-title">选择器材</h3>
          <!-- 器材分类筛选 -->
          <div class="filter-section">
            <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 200px" @change="loadEquipment">
              <el-option
                v-for="type in equipmentTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </div>

          <!-- 器材卡片网格 -->
          <div class="equipment-grid">
            <div
              v-for="equipment in equipmentList"
              :key="equipment.id"
              class="equipment-card"
              :class="{ 
                unavailable: !getStock(equipment) || getStock(equipment) <= 0,
                selected: selectedEquipment?.id === equipment.id
              }"
              @click="selectEquipment(equipment)"
            >
              <div class="equipment-image">
                <el-icon :size="48"><ShoppingBag /></el-icon>
              </div>
              <div class="equipment-info">
                <h4 class="equipment-name">{{ equipment.equipmentName || equipment.equipmentCode }}</h4>
                <p class="equipment-type">{{ equipment.equipmentType || '未分类' }}</p>
                <div class="equipment-details">
                  <div class="detail-item">
                    <span class="detail-label">库存</span>
                    <span
                      class="detail-value"
                      :class="{ 'out-of-stock': !getStock(equipment) || getStock(equipment) <= 0 }"
                    >
                      {{ getStock(equipment) }} 件
                    </span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">租金</span>
                    <span class="detail-value price">¥{{ formatCurrency(equipment.rentalPrice) }}/天</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-if="equipmentList.length === 0" description="暂无可用器材" :image-size="120" />
        </div>

        <!-- 步骤2: 选择数量 -->
        <div v-if="step === 2" class="equipment-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 1" text>返回选择器材</el-button>
            <h3 class="step-title">{{ selectedEquipment?.equipmentName || selectedEquipment?.equipmentCode }} - 选择数量</h3>
          </div>
          <div class="quantity-section">
            <div class="selected-info-card">
              <div class="info-item">
                <span class="info-label">器材名称</span>
                <span class="info-value">{{ selectedEquipment?.equipmentName || selectedEquipment?.equipmentCode }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">可用库存</span>
                <span class="info-value">{{ getStock(selectedEquipment) }} 件</span>
              </div>
              <div class="info-item">
                <span class="info-label">租金</span>
                <span class="info-value price">¥{{ formatCurrency(selectedEquipment?.rentalPrice) }}/天</span>
              </div>
            </div>
            <div class="quantity-input-section">
              <el-form-item label="租借数量" style="margin-bottom: 0;">
                <el-input-number
                  v-model="rentalForm.quantity"
                  :min="1"
                  :max="getStock(selectedEquipment) || 1"
                  style="width: 200px"
                  size="large"
                />
              </el-form-item>
              <div class="step-actions">
                <el-button @click="step = 1" size="large">返回</el-button>
                <el-button type="primary" @click="step = 3" size="large">下一步</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤3: 选择时间 -->
        <div v-if="step === 3" class="equipment-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 2" text>返回选择数量</el-button>
            <h3 class="step-title">选择租借时间</h3>
          </div>
          <div class="time-section">
            <div class="selected-info-card">
              <div class="info-item">
                <span class="info-label">器材</span>
                <span class="info-value">{{ selectedEquipment?.equipmentName || selectedEquipment?.equipmentCode }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">数量</span>
                <span class="info-value">{{ rentalForm.quantity }} 件</span>
              </div>
            </div>
            <el-form :model="rentalForm" label-width="120px" class="time-form">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="rentalForm.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
              <el-form-item label="结束时间">
                <el-date-picker
                  v-model="rentalForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
              <el-form-item label="预计费用">
                <div class="estimated-cost">
                  <span class="cost-value">¥{{ estimatedRentalCost }}</span>
                </div>
              </el-form-item>
            </el-form>
            <div class="step-actions">
              <el-button @click="step = 2" size="large">返回</el-button>
              <el-button 
                type="primary" 
                @click="step = 4" 
                :disabled="!rentalForm.startTime || !rentalForm.endTime"
                size="large"
              >
                下一步
              </el-button>
            </div>
          </div>
        </div>

        <!-- 步骤4: 确认租借 -->
        <div v-if="step === 4" class="equipment-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 3" text>返回选择时间</el-button>
            <h3 class="step-title">确认租借信息</h3>
          </div>
          <div class="confirm-section">
            <div class="confirm-card">
              <h4 class="confirm-title">租借信息</h4>
              <div class="confirm-info">
                <div class="confirm-item">
                  <span class="confirm-label">器材名称</span>
                  <span class="confirm-value">{{ selectedEquipment?.equipmentName || selectedEquipment?.equipmentCode }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">租借数量</span>
                  <span class="confirm-value">{{ rentalForm.quantity }} 件</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">开始时间</span>
                  <span class="confirm-value">{{ rentalForm.startTime }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">结束时间</span>
                  <span class="confirm-value">{{ rentalForm.endTime }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">预计费用</span>
                  <span class="confirm-value price">¥{{ estimatedRentalCost }}</span>
                </div>
              </div>

              <!-- 业务订单统一使用余额支付 -->
              <div class="payment-section">
                <h4 class="payment-title">余额支付</h4>
                <div class="payment-options">
                  <div
                    v-for="option in paymentMethodOptions"
                    :key="option.value"
                    class="payment-option"
                    :class="{ selected: rentalForm.paymentMethod === option.value }"
                    @click="rentalForm.paymentMethod = option.value"
                  >
                    <el-icon :size="24" class="payment-icon">
                      <component :is="option.icon" />
                    </el-icon>
                    <span class="payment-label">{{ option.label }}</span>
                    <el-icon v-if="rentalForm.paymentMethod === option.value" class="payment-check">
                      <CircleCheck />
                    </el-icon>
                  </div>
                </div>
              </div>

              <p class="confirm-hint">提交后将生成待支付订单，请到「我的租借」中点击「支付」完成付款。</p>
              <div class="confirm-actions">
                <el-button @click="step = 3" size="large">返回修改</el-button>
                <el-button
                  type="primary"
                  :loading="submitting"
                  @click="submitRental"
                  size="large"
                >
                  确认租借
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的租借" name="my-rentals">
        <div class="my-rentals-section">
          <div class="filter-bar">
            <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px" @change="loadMyRentals">
              <el-option label="租借中" value="1" />
              <el-option label="已归还" value="2" />
              <el-option label="逾期" value="3" />
              <el-option label="已取消" value="0" />
            </el-select>
          </div>

          <div v-if="myRentals.length > 0" class="rentals-list">
            <div
              v-for="rental in myRentals"
              :key="rental.id"
              class="rental-item-card"
            >
              <div class="rental-main">
                <div class="rental-header">
                  <span class="rental-no">{{ rental.rentalNo }}</span>
                  <el-tag :type="getRentalStatusType(rental.status)" size="small">
                    {{ getRentalStatusText(rental.status) }}
                  </el-tag>
                </div>
                <div class="rental-details">
                  <p class="rental-equipment">{{ rental.equipmentName }}</p>
                  <p class="rental-time">租借时间：{{ formatTimeRange(rental.startTime, rental.endTime) }}</p>
                  <p class="rental-amount">押金：¥{{ formatCurrency(rental.depositAmount || rental.deposit) }} | 租金：¥{{ formatCurrency(rental.rentalAmount) }}</p>
                  <p class="rental-payment">
                    支付状态：<el-tag :type="rental.paymentStatus === 1 ? 'success' : 'warning'" size="small">{{ rental.paymentStatus === 1 ? '已支付' : '待支付' }}</el-tag>
                    <span v-if="rental.paymentStatus === 1" style="margin-left: 8px">支付方式：{{ getPaymentMethodText(rental.paymentMethod) }}</span>
                  </p>
                </div>
              </div>
              <div class="rental-actions">
                <el-button
                  v-if="rental.paymentStatus === 0 && rental.status === 1"
                  type="primary"
                  size="small"
                  @click="handleOpenPay(rental)"
                >
                  支付
                </el-button>
                <el-button
                  v-if="rental.status === 1"
                  type="warning"
                  size="small"
                  @click="handleReturn(rental)"
                >
                  归还
                </el-button>
                <el-button
                  v-if="rental.status === 1"
                  type="info"
                  size="small"
                  @click="handleCancelRental(rental)"
                >
                  取消
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无租借记录" :image-size="120" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 支付弹窗：业务订单统一使用余额支付 -->
    <el-dialog v-model="payDialogVisible" title="器材租借支付" width="420px">
      <el-form label-width="100px">
        <el-form-item label="租借单号">
          <el-tag type="info">{{ currentPayRental?.rentalNo || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input-number v-model="payForm.amount" :min="0" :precision="2" :step="10" disabled style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.method" placeholder="余额支付" style="width: 100%">
            <el-option v-for="item in paymentMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="submitPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingBag, CircleCheck, ArrowLeft, Wallet } from '@element-plus/icons-vue'
import { getEquipmentList, getEquipmentTypes } from '@/api/equipment'
import { getEquipmentRentalList, addEquipmentRental, updateEquipmentRentalStatus, processEquipmentRentalPayment } from '@/api/equipmentRental'

const activeTab = ref('rent')
const step = ref(1) // 1-选择器材, 2-选择数量, 3-选择时间, 4-确认租借

// 步骤配置
const steps = ['选择器材', '选择数量', '选择时间', '确认租借']

const filterType = ref(null)
const filterStatus = ref(null)
const equipmentList = ref([])
const equipmentTypes = ref([])
const myRentals = ref([])
const selectedEquipment = ref(null)
const submitting = ref(false)

const payDialogVisible = ref(false)
const payLoading = ref(false)
const currentPayRental = ref(null)
const payForm = ref({ amount: 0, method: 'BALANCE' })

const rentalForm = ref({
  equipmentId: null,
  quantity: 1,
  startTime: '',
  endTime: '',
  paymentMethod: 'BALANCE'
})

const paymentMethodOptions = [
  { label: '余额支付', value: 'BALANCE', icon: Wallet }
]

const estimatedRentalCost = computed(() => {
  if (!selectedEquipment.value || !rentalForm.value.startTime || !rentalForm.value.endTime) {
    return '0.00'
  }
  const start = new Date(rentalForm.value.startTime)
  const end = new Date(rentalForm.value.endTime)
  const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  const cost = days * (selectedEquipment.value.rentalPrice || 0) * (rentalForm.value.quantity || 1)
  return cost.toFixed(2)
})

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
  return num.toFixed(2)
}

const formatTimeRange = (start, end) => {
  if (!start || !end) return '-'
  const startDate = new Date(start)
  const endDate = new Date(end)
  const dateStr = `${startDate.getMonth() + 1}月${startDate.getDate()}日`
  const startTime = `${String(startDate.getHours()).padStart(2, '0')}:${String(startDate.getMinutes()).padStart(2, '0')}`
  const endTime = `${String(endDate.getHours()).padStart(2, '0')}:${String(endDate.getMinutes()).padStart(2, '0')}`
  return `${dateStr} ${startTime}-${endTime}`
}

// 统一获取库存字段：优先使用后端的 availableQuantity，兼容可能存在的 stock 字段
const getStock = (equipment) => {
  if (!equipment) return 0
  if (equipment.availableQuantity !== undefined && equipment.availableQuantity !== null) {
    return Number(equipment.availableQuantity) || 0
  }
  if (equipment.stock !== undefined && equipment.stock !== null) {
    return Number(equipment.stock) || 0
  }
  return 0
}

const getRentalStatusText = (status) => {
  const map = { 0: '已取消', 1: '租借中', 2: '已归还', 3: '逾期' }
  return map[status] || '未知'
}

const getRentalStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const getPaymentMethodText = (method) => {
  const map = { BALANCE: '余额支付' }
  return map[method] || method || '-'
}

const loadEquipmentTypes = async () => {
  try {
    const res = await getEquipmentTypes()
    if (res.code === 200) {
      // 处理返回的数据格式，可能是数组或对象
      const types = res.data || []
      equipmentTypes.value = Array.isArray(types) 
        ? types.map(t => typeof t === 'string' ? { label: t, value: t } : t)
        : []
    }
  } catch (e) {
    console.error('加载器材类型失败:', e)
    // 如果API失败，使用空数组
    equipmentTypes.value = []
  }
}

const loadEquipment = async () => {
  try {
    const params = { page: 1, size: 100 }
    if (filterType.value) {
      params.equipmentType = filterType.value
    }
    const res = await getEquipmentList(params)
    if (res.code === 200) {
      equipmentList.value = res.data?.data || res.data?.records || res.data || []
    }
  } catch (e) {
    console.error('加载器材列表失败:', e)
    ElMessage.error('加载器材列表失败')
  }
}

const selectEquipment = (equipment) => {
  if (!getStock(equipment) || getStock(equipment) <= 0) {
    ElMessage.warning('该器材暂无库存')
    return
  }
  selectedEquipment.value = equipment
  rentalForm.value = {
    equipmentId: equipment.id,
    quantity: 1,
    startTime: '',
    endTime: '',
    paymentMethod: 'BALANCE'
  }
  step.value = 2
}

const resetRentalForm = () => {
  selectedEquipment.value = null
  rentalForm.value = {
    equipmentId: null,
    quantity: 1,
    startTime: '',
    endTime: '',
    paymentMethod: 'BALANCE'
  }
  step.value = 1
}

// 监听数量变化，自动进入下一步
watch(() => rentalForm.value.quantity, (newVal) => {
  if (step.value === 2 && newVal > 0 && selectedEquipment.value) {
    // 可以添加自动进入下一步的逻辑，或者让用户手动点击
  }
})

// 监听时间变化，自动进入下一步
watch([() => rentalForm.value.startTime, () => rentalForm.value.endTime], ([start, end]) => {
  if (step.value === 3 && start && end) {
    // 可以添加自动进入下一步的逻辑，或者让用户手动点击
  }
})

const submitRental = async () => {
  if (!rentalForm.value.startTime || !rentalForm.value.endTime) {
    ElMessage.warning('请选择租借时间')
    step.value = 3
    return
  }
  if (!rentalForm.value.paymentMethod) {
    ElMessage.warning('请确认余额支付')
    return
  }

  submitting.value = true
  try {
    const res = await addEquipmentRental({
      equipmentId: rentalForm.value.equipmentId,
      quantity: rentalForm.value.quantity,
      startTime: rentalForm.value.startTime,
      endTime: rentalForm.value.endTime,
      paymentMethod: rentalForm.value.paymentMethod
    })
    
    if (res.code === 200) {
      ElMessage.success('租借成功！')
      resetRentalForm()
      loadEquipment()
      activeTab.value = 'my-rentals'
      loadMyRentals()
    } else {
      ElMessage.error(res.message || '租借失败')
    }
  } catch (e) {
    console.error('提交租借失败:', e)
    ElMessage.error('租借失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const loadMyRentals = async () => {
  try {
    const params = { page: 1, size: 20 }
    if (filterStatus.value) {
      params.status = parseInt(filterStatus.value)
    }
    const res = await getEquipmentRentalList(params)
    if (res.code === 200) {
      myRentals.value = res.data?.data || []
    }
  } catch (e) {
    console.error('加载租借列表失败:', e)
  }
}

const handleReturn = async (rental) => {
  try {
    await ElMessageBox.confirm('确定要归还这个器材吗？', '提示', {
      type: 'warning'
    })
    
    const res = await updateEquipmentRentalStatus(rental.id, 2)
    if (res.code === 200) {
      ElMessage.success('归还成功')
      loadMyRentals()
    } else {
      ElMessage.error(res.message || '归还失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('归还失败:', e)
      ElMessage.error('归还失败，请稍后重试')
    }
  }
}

const handleOpenPay = (rental) => {
  currentPayRental.value = rental
  payForm.value.amount = Number(rental.rentalAmount) || 0
  payForm.value.method = rental.paymentMethod || 'BALANCE'
  payDialogVisible.value = true
}

const submitPay = async () => {
  if (!currentPayRental.value?.id) return
  payLoading.value = true
  try {
    const res = await processEquipmentRentalPayment(currentPayRental.value.id, payForm.value.method)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      loadMyRentals()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (e) {
    console.error('支付失败:', e)
    ElMessage.error(e.response?.data?.message || e.message || '支付失败')
  } finally {
    payLoading.value = false
  }
}

const handleCancelRental = async (rental) => {
  try {
    await ElMessageBox.confirm('确定要取消这个租借吗？', '提示', {
      type: 'warning'
    })
    
    const res = await updateEquipmentRentalStatus(rental.id, 0)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadMyRentals()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消租借失败:', e)
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'my-rentals') {
    loadMyRentals()
  }
})

onMounted(() => {
  loadEquipmentTypes()
  loadEquipment()
  if (activeTab.value === 'my-rentals') {
    loadMyRentals()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-equipment {
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

/* 标题动画定义 */
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

@keyframes titleShimmer {
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
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
  
  /* 渐变文字效果 */
  background: linear-gradient(135deg, 
    var(--color-text-primary, #1E293B) 0%, 
    var(--color-primary, #2563EB) 50%,
    var(--color-secondary, #8B5CF6) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  
  /* 文字阴影（作为后备） */
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
  
  /* 渐变文字效果 */
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

.equipment-tabs {
  margin-top: 24px;
}

/* 步骤指示器 */
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

/* 步骤内容 */
.equipment-step {
  padding: 24px 0;
  animation: fadeInUp 0.4s ease-out;
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

.step-header .step-title {
  margin: 0;
}

.quantity-section,
.time-section,
.confirm-section {
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
  font-weight: 500;
}

.info-value {
  font-size: 16px;
  color: var(--color-text-primary, #1E293B);
  font-weight: 600;
}

.info-value.price {
  color: var(--color-primary, #2563EB);
  font-size: 18px;
}

.quantity-input-section {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
}

.time-form {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
}

.estimated-cost {
  text-align: center;
  padding: 16px;
}

.cost-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary, #2563EB);
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
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 24px 0;
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
  color: var(--color-text-primary, #1E293B);
  font-weight: 600;
}

.confirm-value.price {
  color: var(--color-primary, #2563EB);
  font-size: 20px;
}

.confirm-hint {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 0 0 16px 0;
  text-align: center;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

/* 余额支付展示样式 */
.payment-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}

.payment-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 16px 0;
  text-align: center;
}

.payment-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-background, #F8FAFC);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.payment-option:hover {
  border-color: var(--color-primary-light, #60A5FA);
  background: var(--color-card-bg-hover, #EFF6FF);
}

.payment-option.selected {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg, #FFFFFF) 0%, rgba(37, 99, 235, 0.08) 100%);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
}

.payment-icon {
  color: var(--color-text-secondary, #64748B);
  transition: color 0.3s ease;
}

.payment-option.selected .payment-icon {
  color: var(--color-primary, #2563EB);
}

.payment-label {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
}

.payment-check {
  color: var(--color-primary, #2563EB);
  font-size: 20px;
}

@media (max-width: 480px) {
  .payment-options {
    grid-template-columns: 1fr;
  }
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.filter-section {
  margin-bottom: 20px;
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.equipment-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 20px;
  padding: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.equipment-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.05), transparent);
  transition: left 0.5s ease;
}

.equipment-card:hover:not(.unavailable) {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.equipment-card:hover:not(.unavailable)::before {
  left: 100%;
}

.equipment-card.selected {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg, #FFFFFF) 0%, rgba(37, 99, 235, 0.05) 100%);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
  animation: selectedPulse 2s ease-in-out infinite;
}

@keyframes selectedPulse {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
  }
  50% {
    box-shadow: 0 4px 28px rgba(37, 99, 235, 0.3);
  }
}

.equipment-card.unavailable {
  opacity: 0.5;
  filter: grayscale(0.6);
  cursor: not-allowed;
}

.equipment-image {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #8B5CF6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  margin: 0 auto;
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.equipment-card:hover:not(.unavailable) .equipment-image {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.4);
}

.equipment-card.unavailable .equipment-image {
  background: linear-gradient(135deg, #94A3B8 0%, #64748B 100%);
  filter: grayscale(0.8);
}

.equipment-info {
  text-align: center;
}

.equipment-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 8px 0;
}

.equipment-type {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 0 0 16px 0;
}

.equipment-details {
  display: flex;
  justify-content: space-around;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  transition: all 0.3s ease;
}

.detail-value.price {
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 700;
}

.detail-value.out-of-stock {
  color: var(--color-danger, #EF4444);
  animation: stockWarning 2s ease-in-out infinite;
}

@keyframes stockWarning {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.equipment-card:hover:not(.unavailable) .detail-value.price {
  transform: scale(1.1);
}

.equipment-actions {
  display: flex;
  justify-content: center;
}

.my-rentals-section {
  padding: 24px 0;
}

.filter-bar {
  margin-bottom: 20px;
}

.rentals-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rental-item-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.rental-item-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--color-primary, #2563EB);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.rental-item-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(37, 99, 235, 0.15);
  border-color: var(--color-primary-light, #60A5FA);
  transform: translateX(4px);
}

.rental-item-card:hover::before {
  transform: scaleY(1);
}

.rental-main {
  flex: 1;
}

.rental-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.rental-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Code', monospace;
}

.rental-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rental-equipment {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.rental-time {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

.rental-amount {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 4px 0 0 0;
}

.rental-payment {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 4px 0 0 0;
}

.rental-actions {
  display: flex;
  gap: 8px;
}

.estimated-cost {
  display: flex;
  align-items: baseline;
}

.cost-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
}

/* 响应式 - 移动端优化 */
@media (max-width: 768px) {
  .equipment-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .equipment-card {
    padding: 20px;
  }
  
  .rental-item-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .rental-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  /* 移动端触摸优化 */
  .equipment-card,
  .rental-item-card {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}
</style>
