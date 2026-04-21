<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32"><ShoppingBag /></el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">器材租借</h2>
              <p class="page-subtitle">管理租借单、归还、续租与状态，快速查看押金与金额</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总租借</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-primary" @click="handleStatClick(1)">
              <span class="stat-label">租借中</span>
              <span class="stat-value">{{ statistics.renting || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(2)">
              <span class="stat-label">已归还</span>
              <span class="stat-value">{{ statistics.returned || 0 }}</span>
            </div>
            <div class="stat-item stat-item-warning" @click="handleStatClick(3)">
              <span class="stat-label">逾期</span>
              <span class="stat-value">{{ statistics.overdue || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">已取消</span>
              <span class="stat-value">{{ statistics.cancelled || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="glass-card search-card">
        <div class="search-header">
          <h3 class="search-title">搜索过滤</h3>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="search-container">
            <div class="search-fields">
              <el-form-item label="租借单号" class="search-item">
                <el-input v-model="searchForm.rentalNo" placeholder="输入租借单号" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="器材" class="search-item">
                <el-select v-model="searchForm.equipmentId" placeholder="全部器材" clearable class="search-select">
                  <el-option
                    v-for="item in equipmentOptions"
                    :key="item.id"
                    :label="item.equipmentName || item.equipmentCode"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="会员" class="search-item">
                <el-input v-model="searchForm.memberKeyword" placeholder="姓名/手机号" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="search-select-small">
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间" class="search-item">
                <el-date-picker
                  v-model="searchForm.timeRange"
                  type="datetimerange"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  start-placeholder="开始"
                  end-placeholder="结束"
                />
              </el-form-item>
            </div>
            <div class="search-buttons">
              <el-button type="primary" :icon="Search" class="search-btn bmp-uiv-btn bmp-uiv-btn-primary" @click="handleSearch">搜索</el-button>
              <el-button type="primary" :icon="Refresh" class="reset-btn bmp-uiv-btn bmp-uiv-btn-primary" @click="handleReset">重置</el-button>
            </div>
          </div>
        </el-form>
      </div>

      <div class="glass-card action-card">
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">发起租借</el-button>
      </div>

      <div class="glass-card table-card">
        <el-table
          :data="rentalList"
          v-loading="loading"
          border
          stripe
          :height="tableHeight"
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
        >
          <el-table-column prop="rentalNo" label="租借单号" min-width="140" />
          <el-table-column prop="equipmentName" label="器材" min-width="140" />
          <el-table-column prop="memberName" label="会员" min-width="120" />
          <el-table-column label="时间" min-width="220">
            <template #default="scope">
              <span>{{ formatDateTime(scope.row.rentalDate) }} ~ {{ formatDateTime(scope.row.returnDate) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="金额/押金" min-width="180">
            <template #default="scope">
              <span class="price-text">¥{{ formatCurrency(scope.row.rentalAmount) }}</span>
              <el-tag type="info" size="small" style="margin-left: 6px">
                押金 ¥{{ formatCurrency(scope.row.depositAmount) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="支付方式" min-width="110">
            <template #default="scope">
              <el-tag :type="getPaymentMethodType(scope.row.paymentMethod)" size="small">
                {{ getPaymentMethodText(scope.row.paymentMethod) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentStatus" label="支付状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)" size="small">
                {{ getPaymentStatusText(scope.row.paymentStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="租借状态" min-width="110">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="expectedReturnDate" label="预计归还日期" min-width="140">
            <template #default="scope">
              <span v-if="scope.row.expectedReturnDate">{{ formatDate(scope.row.expectedReturnDate) }}</span>
              <span v-else style="color: var(--color-text-secondary, #94A3B8)">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="returnDate" label="实际归还时间" min-width="180">
            <template #default="scope">{{ formatDateTime(scope.row.returnDate) }}</template>
          </el-table-column>
          <el-table-column label="操作" min-width="340" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button type="success" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <!-- 待支付显示支付，已支付显示退款（同一位置互斥，与赛事报名/场地预约一致） -->
                <el-button
                  v-if="scope.row.paymentStatus === 0 && scope.row.status === 1"
                  type="warning"
                  size="small"
                  plain
                  @click="openPay(scope.row)"
                >
                  支付
                </el-button>
                <el-button
                  v-else-if="scope.row.paymentStatus === 1"
                  type="info"
                  size="small"
                  plain
                  @click="handleRefund(scope.row)"
                >
                  退款
                </el-button>
                <el-button type="primary" size="small" plain @click="markStatus(scope.row, 2)">归还</el-button>
                <el-button type="danger" size="small" :icon="Delete" plain @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="pagination"
          />
        </div>
      </div>
    </div>

    <!-- 支付弹窗（与穿线服务/课程预约一致） -->
    <el-dialog v-model="payDialogVisible" title="器材租借支付" width="420px">
      <el-form label-width="100px">
        <el-form-item label="租借单号">
          <el-tag type="info">{{ currentPay?.rentalNo || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input-number v-model="payForm.amount" :min="0" :precision="2" :step="10" disabled />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.method" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in paymentMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="submitPay">确认支付</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" class="modern-form">
        <div class="modern-form-section">
          <h4 class="modern-section-title">租借信息</h4>
          <el-form-item label="租借单号" class="modern-form-item">
            <el-input :model-value="form.rentalNo || '提交后自动生成'" disabled />
          </el-form-item>
          <el-form-item label="器材" prop="equipmentId" class="modern-form-item">
            <el-select v-model="form.equipmentId" placeholder="选择器材" filterable style="width: 100%">
              <el-option
                v-for="item in equipmentOptions"
                :key="item.id"
                :label="item.equipmentName || item.equipmentCode"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="会员" prop="memberId" class="modern-form-item">
            <el-input
              v-model="memberKeyword"
              placeholder="输入姓名/手机号以搜索"
              @input="loadMembers"
              clearable
            />
            <el-select
              v-model="form.memberId"
              placeholder="选择会员"
              filterable
              style="width: 100%; margin-top: 8px"
              @change="handleMemberChange"
            >
              <el-option v-for="item in memberOptions" :key="item.id" :label="item.memberName" :value="item.id">
                <span>{{ item.memberName }}</span>
                <span style="margin-left: 8px; color: var(--color-text-secondary, #64748B)">{{ item.phone }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="租借时间" prop="timeRange" class="modern-form-item">
            <el-date-picker
              v-model="form.timeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始"
              end-placeholder="结束"
              style="width: 100%"
              @change="recalcAmount"
            />
          </el-form-item>
          <el-form-item label="租借数量" prop="quantity" class="modern-form-item">
            <el-input-number v-model="form.quantity" :min="1" :step="1" style="width: 100%" @change="recalcAmount" />
          </el-form-item>
          <el-form-item label="租借金额" prop="rentalAmount" class="modern-form-item">
            <el-input-number v-model="form.rentalAmount" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="押金" prop="depositAmount" class="modern-form-item">
            <el-input-number v-model="form.depositAmount" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="支付方式" prop="paymentMethod" class="modern-form-item">
            <el-select v-model="form.paymentMethod" placeholder="选择支付方式" style="width: 100%">
              <el-option v-for="item in paymentMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="支付状态" prop="paymentStatus" class="modern-form-item">
            <el-select v-model="form.paymentStatus" placeholder="选择支付状态" style="width: 100%">
              <el-option v-for="item in paymentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status" class="modern-form-item">
            <el-select v-model="form.status" placeholder="选择状态">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button class="modern-btn-cancel" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="modern-btn-submit" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '更新租借' : '发起租借' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, ShoppingBag } from '@element-plus/icons-vue'
import request from '@/utils/request'
import {
  getEquipmentRentalList,
  getEquipmentRentalInfo,
  addEquipmentRental,
  updateEquipmentRental,
  deleteEquipmentRental,
  updateEquipmentRentalStatus,
  getEquipmentRentalStatistics,
  getEquipmentRentalOptions,
  getEquipmentRentalMembers
} from '@/api/equipmentRental'

const searchForm = reactive({
  rentalNo: '',
  equipmentId: null,
  memberKeyword: '',
  status: null,
  timeRange: []
})

const rentalList = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statistics = reactive({ total: 0, renting: 0, returned: 0, overdue: 0, cancelled: 0 })
const equipmentOptions = ref([])
const memberOptions = ref([])
const memberKeyword = ref('')
const tableHeight = ref(600)

// 支付弹窗（与穿线服务/课程预约一致）
const payDialogVisible = ref(false)
const payLoading = ref(false)
const currentPay = ref(null)
const payForm = reactive({ amount: 0, method: 'BALANCE' })

const dialogVisible = ref(false)
const dialogTitle = ref('发起租借')
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  rentalNo: '',
  equipmentId: null,
  memberId: null,
  timeRange: [],
  quantity: 1,
  rentalAmount: 0,
  depositAmount: 0,
  paymentMethod: 'BALANCE',
  paymentStatus: 0,
  status: 1
})
const formRules = {
  equipmentId: [{ required: true, message: '请选择器材', trigger: 'change' }],
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择时间', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择余额支付', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const submitLoading = ref(false)

const statusOptions = [
  { label: '已取消', value: 0 },
  { label: '租借中', value: 1 },
  { label: '已归还', value: 2 },
  { label: '逾期', value: 3 }
]

const paymentMethodOptions = [
  { label: '余额支付', value: 'BALANCE' }
]

const paymentStatusOptions = [
  { label: '未支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已退款', value: 2 }
]

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '租借中', 2: '已归还', 3: '逾期' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning' }
  return map[status] || 'info'
}

const getPaymentMethodText = (method) => {
  const map = { BALANCE: '余额支付' }
  return map[method] || method || '-'
}

const getPaymentMethodType = (method) => {
  // 业务订单统一使用余额支付标签
  const map = { BALANCE: 'info' }
  return map[method] || 'info'
}

const getPaymentStatusText = (status) => {
  const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
  return map[status] || '未知'
}

const getPaymentStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0.00'
  const num = Number(value) || 0
  return num.toFixed(2)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${mi}:${s}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const loadEquipmentOptions = async () => {
  try {
    const res = await getEquipmentRentalOptions()
    if (res.code === 200) {
      equipmentOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载器材下拉失败:', e)
  }
}

const recalcAmount = () => {
  const equipment = equipmentOptions.value.find((e) => e.id === form.equipmentId)
  if (!equipment || !form.timeRange || form.timeRange.length !== 2) return
  const [start, end] = form.timeRange
  if (!start || !end) return

  const startDate = new Date(start)
  const endDate = new Date(end)
  if (Number.isNaN(startDate.getTime()) || Number.isNaN(endDate.getTime())) return

  const diffMs = endDate.getTime() - startDate.getTime()
  if (diffMs <= 0) return

  const hours = Math.ceil(diffMs / (1000 * 60 * 60))
  const qty = form.quantity || 1

  const price = Number(equipment.rentalPrice || 0)
  const deposit = Number(equipment.rentalDeposit || 0)

  const amount = price * hours * qty
  const depositTotal = deposit * qty

  if (!Number.isNaN(amount)) {
    form.rentalAmount = Number(amount.toFixed(2))
  }
  if (!Number.isNaN(depositTotal)) {
    form.depositAmount = Number(depositTotal.toFixed(2))
  }
}

const loadMembers = async () => {
  if (!memberKeyword.value) {
    memberOptions.value = []
    return
  }
  try {
    const res = await getEquipmentRentalMembers(memberKeyword.value)
    if (res.code === 200) {
      memberOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载会员失败:', e)
  }
}

const handleMemberChange = (memberId) => {
  const target = memberOptions.value.find((m) => m.id === memberId)
  if (target) {
    memberKeyword.value = target.memberName
  }
}

const loadStatistics = async () => {
  try {
    const res = await getEquipmentRentalStatistics()
    if (res.code === 200) {
      Object.assign(statistics, res.data || {})
    }
  } catch (e) {
    console.error('加载统计失败:', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.rentalNo || searchForm.memberKeyword || undefined,
      equipmentId: searchForm.equipmentId || undefined,
      status: searchForm.status ?? undefined,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getEquipmentRentalList(params)
    if (res.code === 200) {
      rentalList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取租借列表失败')
      rentalList.value = []
    }
  } catch (e) {
    console.error('加载租借列表失败:', e)
    ElMessage.error('加载租借列表失败，请稍后重试')
    rentalList.value = []
  } finally {
    loading.value = false
  }
}

const handleStatClick = (status) => {
  searchForm.status = status
  pagination.page = 1
  loadList()
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    rentalNo: '',
    equipmentId: null,
    memberKeyword: '',
    status: null,
    timeRange: []
  })
  pagination.page = 1
  loadList()
}

const handleAdd = () => {
  dialogTitle.value = '发起租借'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑租借'
  isEdit.value = true
  try {
    const res = await getEquipmentRentalInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      const start =
        data.rentalDate && String(data.rentalDate).length > 0
          ? `${String(data.rentalDate).split(' ')[0]} 00:00:00`
          : ''
      const endSource = data.returnDate || data.rentalDate || ''
      const end =
        endSource && String(endSource).length > 0
          ? String(endSource).replace('T', ' ').replace(/(\d{4}-\d{2}-\d{2})(?! )/, '$1 ') // 兼容 2025-01-01T10:00:00 或 2025-01-01 10:00:00
          : ''

      Object.assign(form, {
        id: data.id,
        rentalNo: data.rentalNo,
        equipmentId: data.equipmentId,
        memberId: data.memberId,
        timeRange: start && end ? [start, end] : [],
        quantity: data.quantity,
        rentalAmount: data.rentalAmount,
        depositAmount: data.depositAmount,
        paymentMethod: data.paymentMethod || 'BALANCE',
        paymentStatus: data.paymentStatus ?? 0,
        status: data.status
      })
    }
  } catch (e) {
    console.error('加载详情失败:', e)
  }
  dialogVisible.value = true
}

// 打开支付弹窗（与穿线服务/课程预约一致）
const openPay = (row) => {
  currentPay.value = row
  // 使用后端记录的租借金额作为支付金额
  payForm.amount = Number(row.rentalAmount) || 0
  // 业务订单统一为余额支付，这里沿用当前值兜底
  payForm.method = row.paymentMethod || 'BALANCE'
  payDialogVisible.value = true
}

const submitPay = async () => {
  if (!currentPay.value?.id) return
  payLoading.value = true
  try {
    const res = await request({
      url: `/api/equipment/rental/payment`,
      method: 'post',
      params: {
        rentalId: currentPay.value.id,
        paymentMethod: payForm.method
      }
    })
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      loadList()
      loadStatistics()
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

const handleRefund = (row) => {
  ElMessageBox.confirm(`确定对租借单 ${row.rentalNo} 进行退款操作吗？`, '退款确认', {
    type: 'warning',
    confirmButtonText: '确认退款',
    cancelButtonText: '取消'
  })
    .then(async () => {
      try {
        const res = await request({
          url: `/api/equipment/rental/refund`,
          method: 'post',
          params: { rentalId: row.id }
        })
        if (res.code === 200) {
          ElMessage.success('退款成功')
          loadList()
          loadStatistics()
        } else {
          ElMessage.error(res.message || '退款失败')
        }
      } catch (e) {
        console.error('退款失败:', e)
        ElMessage.error('退款失败')
      }
    })
    .catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除租借单 ${row.rentalNo} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteEquipmentRental(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadList()
          loadStatistics()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (e) {
        console.error('删除失败:', e)
        ElMessage.error('删除失败，请稍后重试')
      }
    })
    .catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const rentalStart = form.timeRange?.[0]
      const rentalEnd = form.timeRange?.[1]
      const payload = {
        id: form.id,
        equipmentId: form.equipmentId,
        memberId: form.memberId,
        quantity: form.quantity,
        rentalDate: rentalStart ? rentalStart.split(' ')[0] : null,
        returnDate: rentalEnd || null,
        rentalAmount: form.rentalAmount,
        depositAmount: form.depositAmount,
        paymentMethod: form.paymentMethod,
        paymentStatus: form.paymentStatus,
        status: form.status
      }
      const res = isEdit.value ? await updateEquipmentRental(payload) : await addEquipmentRental(payload)
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadList()
        loadStatistics()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (e) {
      console.error('提交失败:', e)
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      submitLoading.value = false
    }
  })
}

const markStatus = async (row, status) => {
  try {
    const res = await updateEquipmentRentalStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success('状态已更新')
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '更新状态失败')
    }
  } catch (e) {
    console.error('更新状态失败:', e)
    // 提取错误信息：优先使用 response.data.message，其次使用 e.message
    const errorMessage = e.response?.data?.message || e.message || '更新状态失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    rentalNo: '',
    equipmentId: null,
    memberId: null,
    timeRange: [],
    quantity: 1,
    rentalAmount: 0,
    depositAmount: 0,
    paymentMethod: 'BALANCE',
    paymentStatus: 0,
    status: 1
  })
  memberKeyword.value = ''
  memberOptions.value = []
  if (formRef.value) formRef.value.resetFields()
}

const handleDialogClose = () => {
  resetForm()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadList()
}

const calculateTableHeight = () => {
  tableHeight.value = Math.max(420, window.innerHeight - 430)
}

onMounted(() => {
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
  loadEquipmentOptions()
  loadStatistics()
  loadList()
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateTableHeight)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Sans:wght@400;500;600;700&display=swap');

.page-wrapper {
  padding: 20px;
  background-color: var(--color-background, #f0f2f5);
  height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
}

.page-bg {
  position: absolute;
  inset: 0;
  background: var(--color-background, #F8FAFC);
  z-index: 0;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
}

.page-content {
  position: relative;
  z-index: 1;
  max-width: 1600px;
  margin: 0 auto;
}

.glass-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
}

.glass-card:hover {
  border-color: var(--color-border-hover, #CBD5E1);
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.page-header {
  padding: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.page-title {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 600;
}

.page-subtitle {
  margin: 0;
  color: var(--color-text-secondary, #64748B);
}

.header-stats {
  display: flex;
  gap: 10px;
  align-items: center;
}

.stat-item {
  width: 78px;
  min-width: 78px;
  max-width: 78px;
  padding: 12px 8px;
  border-radius: 14px;
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.stat-item-primary {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%) !important;
}

.stat-item-success {
  background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%) !important;
}

.stat-item-warning {
  background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%) !important;
}

.stat-item-danger {
  background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 100%) !important;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary, #2563EB);
}

.search-card {
  padding: 20px 24px;
}

.search-header {
  margin-bottom: 12px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  padding-bottom: 10px;
}

.search-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.search-container {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
}

.search-fields {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  align-items: flex-end;
}

.search-item {
  margin-bottom: 0 !important;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 160px;
}

.search-select-small {
  width: 130px;
}

.search-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.action-card {
  padding: 14px 20px;
}

.table-card {
  padding: 20px;
}

.price-text {
  color: var(--color-danger, #ef4444);
  font-weight: 600;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}

.pagination :deep(.el-pagination) {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.pagination :deep(.el-pagination__total),
.pagination :deep(.el-pagination__jump),
.pagination :deep(.btn-prev),
.pagination :deep(.btn-next),
.pagination :deep(.el-pager li) {
  color: var(--color-text-primary, #1E293B);
  transition: all 0.3s ease;
  font-family: 'Fira Sans', sans-serif;
}

.pagination :deep(.el-pagination__total) {
  color: var(--color-text-secondary, #64748B);
  font-size: 13px;
  font-weight: 500;
}

.pagination :deep(.el-pagination__jump) {
  color: var(--color-text-secondary, #64748B);
  font-size: 13px;
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 6px;
  padding: 6px 10px;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
}

.pagination :deep(.btn-prev:hover),
.pagination :deep(.btn-next:hover) {
  background: var(--color-background, #F8FAFC);
  border-color: var(--color-primary, #3B82F6);
  color: var(--color-primary, #3B82F6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.pagination :deep(.btn-prev.disabled),
.pagination :deep(.btn-next.disabled) {
  background: var(--color-background, #F1F5F9);
  border-color: var(--color-border, #E2E8F0);
  color: var(--color-text-muted, #CBD5E1);
  cursor: not-allowed;
}

.pagination :deep(.el-pager li) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #DCDFE6);
  border-radius: 6px;
  padding: 4px 10px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
  color: var(--color-text-primary, #666666);
}

.pagination :deep(.el-pager li:hover) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  color: var(--color-primary, #3B82F6);
  transform: scale(1.05);
}

.pagination :deep(.el-pager li.is-active) {
  background: var(--color-primary, #3B82F6);
  border-color: var(--color-primary, #3B82F6);
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transform: scale(1.08);
}

.pagination :deep(.el-select__wrapper) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #DCDFE6);
  border-radius: 6px;
  transition: all 0.3s ease;
  padding: 4px 8px;
}

.pagination :deep(.el-select__wrapper:hover) {
  background: var(--color-background, #F8FAFC);
  border-color: var(--color-primary, #3B82F6);
}

.pagination :deep(.el-select__wrapper:focus-within) {
  background: var(--color-card-bg, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.pagination :deep(.el-select__placeholder) {
  color: var(--color-text-muted, #94A3B8);
  font-size: 13px;
}

.pagination :deep(.el-input__wrapper) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 6px;
  transition: all 0.3s ease;
}

.pagination :deep(.el-input__wrapper:hover) {
  background: var(--color-background, #F8FAFC);
  border-color: var(--color-primary, #3B82F6);
}

.pagination :deep(.el-input__wrapper:focus-within) {
  background: var(--color-card-bg, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.pagination :deep(.el-input__inner) {
  color: var(--color-text-primary, #1E293B);
  font-size: 13px;
  text-align: center;
}

.pagination :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted, #94A3B8);
}

.modern-form-section {
  margin-bottom: 18px;
  padding: 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-card-bg, #ffffff) 0%, var(--color-background, #f8fafc) 100%);
  border: 1px solid var(--color-border, rgba(226, 232, 240, 0.8));
}

.modern-section-title {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 700;
}

.modern-form-item {
  margin-bottom: 14px !important;
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1024px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  .search-container {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input,
  .search-select,
  .search-select-small {
    width: 100%;
  }
}

/* 按钮悬停上浮效果（与场馆管理页面保持一致的交互感）
 * 说明：仅影响视觉表现，不改变业务逻辑
 */
.search-btn,
.reset-btn,
.action-card :deep(.el-button),
.operation-buttons :deep(.el-button) {
  transition: all 0.3s ease;
}

.search-btn:hover,
.reset-btn:hover,
.action-card :deep(.el-button:hover),
.operation-buttons :deep(.el-button:hover) {
  transform: translateY(-2px);
}

/* 不同类型按钮增加更贴近语义的阴影色 */
.search-btn:hover,
.action-card :deep(.el-button--primary:hover),
.operation-buttons :deep(.el-button--primary:hover) {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.35);
}

.operation-buttons :deep(.el-button--success:hover) {
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.28);
}

.operation-buttons :deep(.el-button--warning:hover) {
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.28);
}

.operation-buttons :deep(.el-button--danger:hover) {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.28);
}

</style>
