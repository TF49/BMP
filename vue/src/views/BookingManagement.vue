<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <!-- 头部 -->
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32"><DataLine /></el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">预约管理</h2>
              <p class="page-subtitle">管理场地预约、支付与退款，快速查看冲突与状态</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总预约</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-warning" @click="handleStatClick(1)">
              <span class="stat-label">待支付</span>
              <span class="stat-value">{{ statistics.pending || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(2)">
              <span class="stat-label">已支付</span>
              <span class="stat-value">{{ statistics.paid || 0 }}</span>
            </div>
            <div class="stat-item stat-item-primary" @click="handleStatClick(3)">
              <span class="stat-label">进行中</span>
              <span class="stat-value">{{ statistics.ongoing || 0 }}</span>
            </div>
            <div class="stat-item stat-item-info" @click="handleStatClick(4)">
              <span class="stat-label">已完成</span>
              <span class="stat-value">{{ statistics.finished || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">已取消</span>
              <span class="stat-value">{{ statistics.cancelled || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索 -->
      <div class="glass-card search-card">
        <div class="search-header">
          <h3 class="search-title">搜索过滤</h3>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="search-container">
            <div class="search-fields">
              <el-form-item label="场馆" class="search-item">
                <el-select
                  v-model="searchForm.venueId"
                  placeholder="全部场馆"
                  :clearable="!isVenueManager"
                  :disabled="isVenueManager"
                  class="search-select"
                  @change="loadCourts"
                >
                  <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="场地" class="search-item">
                <el-select v-model="searchForm.courtId" placeholder="全部场地" clearable class="search-select">
                  <el-option v-for="item in courtOptions" :key="item.id" :label="item.courtName || item.courtCode" :value="item.id" />
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
              <el-form-item label="预约时间" class="search-item">
                <el-date-picker
                  v-model="searchForm.timeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  value-format="YYYY-MM-DD HH:mm:ss"
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

      <!-- 操作栏 -->
      <div class="glass-card action-card" v-if="userRole === 'PRESIDENT' || userRole === 'VENUE_MANAGER' || userRole === 'USER'">
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">新增预约</el-button>
      </div>

      <!-- 表格 -->
      <div class="glass-card table-card">
        <el-table
          :data="bookingList"
          v-loading="loading"
          border
          stripe
          highlight-current-row
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          :height="tableHeight"
          row-key="id"
        >
          <el-table-column prop="bookingNo" label="预约单号" min-width="140" />
          <el-table-column prop="venueName" label="场馆" min-width="120" />
          <el-table-column label="预约模式" min-width="100">
            <template #default="scope">
              <el-tag :type="scope.row.bookingMode === 'PACKAGE' ? 'danger' : 'primary'" size="small">
                {{ getBookingModeText(scope.row.bookingMode) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="场地" min-width="180" show-overflow-tooltip>
            <template #default="scope">
              {{ getCourtSummary(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column prop="memberName" label="会员" min-width="120" />
          <el-table-column label="时间" min-width="180">
            <template #default="scope">
              <span>{{ scope.row.startTime }} ~ {{ scope.row.endTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" min-width="110">
            <template #default="scope">
              <!-- 后端字段为 orderAmount，这里统一用它来展示金额 -->
              <span class="price-text">¥{{ formatCurrency(scope.row.orderAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="计费方式" min-width="120">
            <template #default="scope">
              {{ getPricingModeText(scope.row.pricingMode, scope.row.pricingModeSummary) }}
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
          <el-table-column prop="status" label="状态" min-width="110">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="280" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button v-if="isAdmin" type="primary" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <!-- 待支付显示支付，已支付显示退款（同一位置互斥，与赛事报名/课程预约一致） -->
                <el-button v-if="isAdmin && scope.row.status === 1" type="success" size="small" plain @click="openPay(scope.row)">支付</el-button>
                <el-button v-else-if="isAdmin && scope.row.status === 2" type="warning" size="small" plain @click="openRefund(scope.row)">退款</el-button>
                <el-button v-if="isAdmin" type="danger" size="small" :icon="Delete" plain @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增/编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" class="modern-form">
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">预约信息</h4>
          <el-form-item label="场馆" prop="venueId" class="modern-form-item">
            <el-select
              v-model="form.venueId"
              placeholder="请选择场馆"
              style="width: 100%"
              :disabled="isVenueManager"
              @change="handleFormVenueChange"
            >
              <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="预约模式" prop="bookingMode" class="modern-form-item">
            <el-radio-group v-model="form.bookingMode" @change="handleBookingModeChange">
              <el-radio-button label="SHARED">拼场</el-radio-button>
              <el-radio-button label="PACKAGE">包场</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.bookingMode === 'SHARED'" label="拼场计费方式" prop="pricingMode" class="modern-form-item">
            <el-radio-group v-model="form.pricingMode">
              <el-radio-button label="SHARED_HOUR">按小时</el-radio-button>
              <el-radio-button label="SHARED_TIME">按次</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="场地" prop="courtIds" class="modern-form-item">
            <el-select
              v-model="form.courtIds"
              :multiple="form.bookingMode === 'PACKAGE'"
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择场地"
              style="width: 100%"
              @change="checkFormOccupancy"
            >
              <el-option
                v-for="item in formCourtOptions"
                :key="item.id"
                :label="getFormCourtLabel(item)"
                :value="item.id"
                :disabled="isCourtBlocked(item)"
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
          <el-form-item label="预约时间" prop="timeRange" class="modern-form-item">
            <el-date-picker
              v-model="form.timeRange"
              type="datetimerange"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              @change="checkFormOccupancy"
            />
          </el-form-item>
          <div v-if="showFormConflictAlert" class="booking-conflict-alert">
            <div class="booking-conflict-alert__head">
              <el-icon><WarningFilled /></el-icon>
              <span>{{ form.bookingMode === 'PACKAGE' ? '包场场地存在冲突' : '该时段不可拼场' }}</span>
            </div>
            <div class="booking-conflict-alert__body">
              {{ formConflictMessage }}
            </div>
            <div v-if="formOccupancy.users.length" class="booking-conflict-alert__list">
              <div
                v-for="item in formOccupancy.users"
                :key="`${item.bookingId}-${item.startTime}-${item.endTime}`"
                class="booking-conflict-alert__item"
              >
                <span>{{ item.memberName || '已预约会员' }}</span>
                <span>{{ item.startTime }} - {{ item.endTime }}</span>
              </div>
            </div>
          </div>
          <el-form-item v-if="isAdmin" label="状态" class="modern-form-item">
            <template v-if="isEdit">
              <el-tag :type="getStatusType(form.status)" size="default">{{ getStatusText(form.status) }}</el-tag>
              <span class="form-status-hint">（状态仅通过支付/退款/系统自动更新，不可在此修改）</span>
            </template>
            <template v-else>
              <span class="form-status-hint">创建后状态为待支付，请引导用户完成支付</span>
            </template>
          </el-form-item>
          <el-form-item label="金额" prop="amount" class="modern-form-item">
            <el-input-number v-model="form.amount" :min="0" :step="10" :precision="2" style="width: 100%" disabled />
          </el-form-item>
          <el-form-item label="支付方式" class="modern-form-item">
            <!-- 创建时为空；已支付的预约在这里只做查看，支付/修改仍通过“支付/退款”弹窗处理 -->
            <el-select v-model="form.paymentMethod" placeholder="创建后在支付弹窗中选择" disabled>
              <el-option label="余额支付" value="BALANCE" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="modern-form-item">
            <el-input v-model="form.remark" type="textarea" rows="2" placeholder="可选" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button class="modern-btn-cancel" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="modern-btn-submit" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '更新预约' : '创建预约' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" title="预约支付" width="420px">
      <el-form label-width="100px">
        <el-form-item label="预约单号">
          <el-tag type="info">{{ currentPay?.bookingNo || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input-number v-model="payForm.amount" :min="0.01" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.method" placeholder="请选择">
            <el-option label="余额支付" value="BALANCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="submitPay">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 退款弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="预约退款" width="420px">
      <el-form label-width="100px">
        <el-form-item label="预约单号">
          <el-tag type="warning">{{ currentRefund?.bookingNo || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="0.01" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="refundForm.reason" type="textarea" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="refundLoading" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, DataLine, WarningFilled } from '@element-plus/icons-vue'
import {
  getBookingList,
  getBookingInfo,
  addBooking,
  updateBooking,
  deleteBooking,
  getBookingStatistics,
  getBookingVenues,
  getBookingCourts,
  getBookingMembers,
  getBookingRangeOccupancy,
  payBooking,
  refundBooking
} from '@/api/booking'

// 搜索表单
const searchForm = reactive({
  venueId: null,
  courtId: null,
  memberKeyword: '',
  status: null,
  timeRange: []
})

// 选项数据
const venueOptions = ref([])
const courtOptions = ref([])

// 列表数据
const bookingList = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 统计
const statistics = reactive({
  total: 0,
  pending: 0,
  paid: 0,
  ongoing: 0,
  finished: 0,
  cancelled: 0
})

// 获取用户角色和权限
const { userRole, isAdmin, isVenueManager } = useAuth()

const currentVenueId = computed(() => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr && userInfoStr !== 'null') {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo?.venueId ?? null
    }
  } catch (e) {
    console.error('获取当前用户场馆ID失败:', e)
  }
  return null
})

// 表格高度
const tableHeight = ref(600)

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增预约')
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  venueId: null,
  courtId: null,
  courtIds: [],
  memberId: null,
  bookingMode: 'SHARED',
  pricingMode: 'SHARED_HOUR',
  status: null,
  amount: 0,
   paymentMethod: null,
  timeRange: [],
  remark: ''
})
const formRules = {
  venueId: [{ required: true, message: '请选择场馆', trigger: 'change' }],
  courtIds: [{ required: true, message: '请选择场地', trigger: 'change' }],
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  bookingMode: [{ required: true, message: '请选择预约模式', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择预约时间', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}
const submitLoading = ref(false)

// 会员搜索
const memberKeyword = ref('')
const memberOptions = ref([])
const formCourtOptions = ref([])
const formOccupancyLoading = ref(false)
const formOccupancy = reactive({
  count: 0,
  users: []
})
let formOccupancyRequestId = 0
const showFormConflictAlert = computed(() => !!formConflictMessage.value)
const formConflictMessage = computed(() => {
  if (!formOccupancy.count) return ''
  if (form.bookingMode === 'PACKAGE') {
    return '所选场地中存在已被拼场或包场占用的时段，不能创建包场。'
  }
  return '该场地该时段已存在包场，不能继续拼场。'
})

// 支付/退款
const payDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const payLoading = ref(false)
const refundLoading = ref(false)
const currentPay = ref(null)
const currentRefund = ref(null)
const payForm = reactive({ amount: 0, method: 'BALANCE' })
const refundForm = reactive({ amount: 0, reason: '' })

const statusOptions = [
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '进行中', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 0 }
]

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

// 状态文案
const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return map[status] || '未知'
}

const getBookingModeText = (mode) => {
  return mode === 'PACKAGE' ? '包场' : '拼场'
}

const getPricingModeText = (mode, summary) => {
  const normalize = summary || mode
  const map = {
    PACKAGE_HOUR: '包场按小时',
    SHARED_HOUR: '拼场按小时',
    SHARED_TIME: '拼场按次'
  }
  return map[normalize] || '-'
}

const getCourtSummary = (row) => {
  const primary = row.primaryCourtName || row.courtName || '-'
  const count = Number(row.courtCount || (Array.isArray(row.courtNames) ? row.courtNames.length : 0) || 0)
  return count > 1 ? `${primary} 等 ${count} 块场地` : primary
}

// 状态标签颜色
const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: '' }
  return map[status] || 'info'
}

// 支付方式文案
const getPaymentMethodText = (method) => {
  const map = { BALANCE: '余额支付' }
  return map[method] || method || '-'
}

// 支付方式标签颜色
const getPaymentMethodType = (method) => {
  // 业务订单统一使用余额支付标签
  const map = { BALANCE: 'info' }
  return map[method] || 'info'
}

// 支付状态文案
const getPaymentStatusText = (status) => {
  const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
  return map[status] || '未知'
}

// 支付状态标签颜色
const getPaymentStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
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

// 加载列表
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      venueId: searchForm.venueId,
      courtId: searchForm.courtId,
      memberKeyword: searchForm.memberKeyword || null,
      status: searchForm.status,
      startTime: searchForm.timeRange?.[0] || null,
      endTime: searchForm.timeRange?.[1] || null,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getBookingList(params)
    if (res.code === 200) {
      bookingList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取预约列表失败')
      bookingList.value = []
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败，请稍后重试')
    bookingList.value = []
  } finally {
    loading.value = false
  }
}

// 统计
const loadStatistics = async () => {
  try {
    const res = await getBookingStatistics()
    if (res.code === 200) {
      Object.assign(statistics, res.data || {})
    }
  } catch (e) {
    console.error('加载统计失败:', e)
  }
}

// 场馆
const loadVenues = async () => {
  try {
    const res = await getBookingVenues()
    if (res.code === 200) {
      venueOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载场馆失败:', e)
  }
}

// 场地（用于搜索）
const loadCourts = async () => {
  if (!searchForm.venueId) {
    courtOptions.value = []
    searchForm.courtId = null
    return
  }
  try {
    const res = await getBookingCourts(searchForm.venueId)
    if (res.code === 200) {
      courtOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载场地失败:', e)
  }
}

// 场地（用于表单）
const loadCourtsForForm = async (resetCourt = true) => {
  if (resetCourt) {
    form.courtId = null
  }
  formCourtOptions.value = []
  if (!form.venueId) return
  try {
    const res = await getBookingCourts(form.venueId)
    if (res.code === 200) {
      formCourtOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载表单场地失败:', e)
  }
}

const resetFormOccupancy = () => {
  formOccupancy.count = 0
  formOccupancy.users = []
  formOccupancyLoading.value = false
}

const handleFormVenueChange = async () => {
  resetFormOccupancy()
  form.courtId = null
  form.courtIds = []
  await loadCourtsForForm(true)
}

const extractFormRangePayload = () => {
  const [start, end] = form.timeRange || []
  const bookingDate = start ? start.slice(0, 10) : null
  const startTime = start ? start.slice(11, 19) : null
  const endTime = end ? end.slice(11, 19) : null
  return { bookingDate, startTime, endTime }
}

const computeFormAmount = () => {
  const { startTime, endTime } = extractFormRangePayload()
  const selectedCourts = formCourtOptions.value.filter(item => form.courtIds.includes(item.id))
  if (!selectedCourts.length || !startTime || !endTime) {
    form.amount = 0
    return
  }
  const [sh, sm] = startTime.split(':').map(Number)
  const [eh, em] = endTime.split(':').map(Number)
  const minutes = (eh * 60 + em) - (sh * 60 + sm)
  const duration = minutes > 0 ? Math.ceil(minutes / 60) : 0
  form.amount = selectedCourts.reduce((sum, item) => {
    const packagePrice = Number(item.packagePricePerHour || item.pricePerHour || 0)
    const sharedHourPrice = Number(item.sharedPricePerHour || item.pricePerHour || 0)
    const sharedTimePrice = Number(item.sharedPricePerTime || item.pricePerTime || 0)
    if (form.bookingMode === 'PACKAGE') return sum + packagePrice * duration
    if (form.pricingMode === 'SHARED_TIME') return sum + sharedTimePrice
    return sum + sharedHourPrice * duration
  }, 0)
}

const handleBookingModeChange = () => {
  if (form.bookingMode === 'SHARED') {
    form.courtIds = form.courtIds.length ? [form.courtIds[0]] : []
    form.pricingMode = form.pricingMode === 'SHARED_TIME' ? 'SHARED_TIME' : 'SHARED_HOUR'
  } else {
    form.pricingMode = 'PACKAGE_HOUR'
  }
  computeFormAmount()
  checkFormOccupancy()
}

const getFormCourtLabel = (item) => {
  const name = item.courtName || item.courtCode
  const priceText = form.bookingMode === 'PACKAGE'
    ? `包场 ¥${formatCurrency(item.packagePricePerHour || item.pricePerHour)}/小时`
    : form.pricingMode === 'SHARED_TIME'
      ? `拼场 ¥${formatCurrency(item.sharedPricePerTime || item.pricePerTime)}/次`
      : `拼场 ¥${formatCurrency(item.sharedPricePerHour || item.pricePerHour)}/小时`
  return `${name} · ${priceText}`
}

const isCourtBlocked = (item) => {
  return Number(item.status) === 0
}

const checkFormOccupancy = async () => {
  resetFormOccupancy()
  const { bookingDate, startTime, endTime } = extractFormRangePayload()
  computeFormAmount()
  if (!form.courtIds.length || !bookingDate || !startTime || !endTime) return
  if (endTime <= startTime) return

  const requestId = ++formOccupancyRequestId
  formOccupancyLoading.value = true
  try {
    const results = await Promise.all(
      form.courtIds.map(async (courtId) => {
        const res = await getBookingRangeOccupancy({ courtId, bookingDate, startTime, endTime })
        return { courtId, res }
      })
    )
    if (requestId !== formOccupancyRequestId) return
    const allUsers = []
    let count = 0
    const currentId = isEdit.value ? form.id : null
    results.forEach(({ res }) => {
      if (res.code === 200) {
        const users = Array.isArray(res.data?.users) ? res.data.users : []
        const filteredUsers = currentId ? users.filter(item => item.bookingId !== currentId) : users
        const filteredCount = Number(res.data?.count ?? filteredUsers.length)
        if (form.bookingMode === 'PACKAGE') {
          count += filteredCount
        } else {
          const sharedCount = filteredUsers.length
          if (filteredCount > sharedCount) {
            count += filteredCount - sharedCount
          }
        }
        allUsers.push(...filteredUsers)
      }
    })
    formOccupancy.count = count
    formOccupancy.users = allUsers
  } catch (e) {
    console.error('检查预约占用失败:', e)
  } finally {
    if (requestId === formOccupancyRequestId) {
      formOccupancyLoading.value = false
    }
  }
}

// 会员搜索
const loadMembers = async () => {
  if (!memberKeyword.value) {
    memberOptions.value = []
    return
  }
  try {
    const res = await getBookingMembers(memberKeyword.value)
    if (res.code === 200) {
      memberOptions.value = res.data || []
    }
  } catch (e) {
    console.error('搜索会员失败:', e)
  }
}

// 选择会员后，同步关键字为当前会员姓名，避免上面输入框与下拉显示不一致
const handleMemberChange = (memberId) => {
  const target = memberOptions.value.find((m) => m.id === memberId)
  if (target) {
    memberKeyword.value = target.memberName
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
    venueId: null,
    courtId: null,
    memberKeyword: '',
    status: null,
    timeRange: []
  })
  pagination.page = 1
  loadList()
}

const handleAdd = () => {
  dialogTitle.value = '新增预约'
  isEdit.value = false
  resetForm()
  if (isVenueManager.value && currentVenueId.value) {
    form.venueId = currentVenueId.value
    loadCourtsForForm(true)
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑预约'
  isEdit.value = true
  try {
    const res = await getBookingInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      Object.assign(form, {
        id: data.id,
        courtId: data.courtId,
        courtIds: Array.isArray(data.courtIds) && data.courtIds.length ? data.courtIds : (data.courtId ? [data.courtId] : []),
        memberId: data.memberId,
        bookingMode: data.bookingMode || 'SHARED',
        pricingMode: data.pricingMode || 'SHARED_HOUR',
        status: data.status,
        paymentMethod: data.paymentMethod || null,
        // 详情接口返回的金额字段为 orderAmount，这里映射到表单字段方便展示
        amount: data.orderAmount,
        // 组合预约日期和开始/结束时间，供 datetimerange 组件使用
        timeRange: data.bookingDate && data.startTime && data.endTime
          ? [
              `${data.bookingDate} ${data.startTime}`,
              `${data.bookingDate} ${data.endTime}`
            ]
          : [],
        remark: data.remark || ''
      })
      // 确保下拉中有当前会员选项，避免只显示数字ID
      if (data.memberId && data.memberName) {
        memberOptions.value = [
          {
            id: data.memberId,
            memberName: data.memberName,
            // 如果需要可扩展为从接口返回手机号字段
            phone: data.phone || ''
          }
        ]
        memberKeyword.value = data.memberName
      }
      await loadCourtsForForm(false)
      computeFormAmount()
      await checkFormOccupancy()
    }
  } catch (e) {
    console.error('加载预约详情失败:', e)
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除预约 ${row.bookingNo} 吗？`, '提示', {
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await deleteBooking(row.id)
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
      const { bookingDate, startTime, endTime } = extractFormRangePayload()

      if (formOccupancy.count > 0) {
        ElMessage.warning('该时段已被占用，请调整时间后再提交')
        submitLoading.value = false
        return
      }

      const payload = {
        id: form.id,
        courtId: form.courtIds[0],
        courtIds: form.courtIds,
        memberId: form.memberId,
        bookingMode: form.bookingMode,
        pricingMode: form.bookingMode === 'PACKAGE' ? 'PACKAGE_HOUR' : form.pricingMode,
        status: form.status,
        bookingDate,
        startTime,
        endTime,
        remark: form.remark
      }
      const res = isEdit.value ? await updateBooking(payload) : await addBooking(payload)
      if (res.code === 200) {
        if (isEdit.value) {
          ElMessage.success('更新成功')
        } else {
          ElMessage.success('预约成功，请尽快完成支付')
        }
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

const resetForm = () => {
  formOccupancyRequestId += 1
  Object.assign(form, {
    id: null,
    venueId: isVenueManager.value && currentVenueId.value ? currentVenueId.value : null,
    courtId: null,
    courtIds: [],
    memberId: null,
    bookingMode: 'SHARED',
    pricingMode: 'SHARED_HOUR',
    status: null,
    amount: 0,
    paymentMethod: null,
    timeRange: [],
    remark: ''
  })
  formCourtOptions.value = []
  memberOptions.value = []
  memberKeyword.value = ''
  resetFormOccupancy()
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
  const windowHeight = window.innerHeight
  tableHeight.value = Math.max(420, windowHeight - 430)
}

// 支付
const openPay = (row) => {
  currentPay.value = row
  // 显示后端计算好的订单金额
  payForm.amount = row.orderAmount || 0
  payForm.method = 'BALANCE'
  payDialogVisible.value = true
}

const submitPay = async () => {
  if (!currentPay.value?.id) return
  payLoading.value = true
  try {
    const res = await payBooking({
      bookingId: currentPay.value.id,
      paymentMethod: payForm.method
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
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    payLoading.value = false
  }
}

// 退款
const openRefund = (row) => {
  currentRefund.value = row
  refundForm.amount = row.orderAmount || 0
  refundForm.reason = ''
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (!currentRefund.value?.id) return
  refundLoading.value = true
  try {
    const res = await refundBooking({
      bookingId: currentRefund.value.id
    })
    if (res.code === 200) {
      ElMessage.success('退款成功')
      refundDialogVisible.value = false
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '退款失败')
    }
  } catch (e) {
    console.error('退款失败:', e)
    ElMessage.error('退款失败，请稍后重试')
  } finally {
    refundLoading.value = false
  }
}

onMounted(() => {
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
  loadVenues()
  loadList()
  loadStatistics()
})

// VM 默认锁定自己场馆（只做前端便捷性，后端仍兜底）
onMounted(() => {
  if (isVenueManager.value && currentVenueId.value) {
    searchForm.venueId = currentVenueId.value
    loadCourts()
    form.venueId = currentVenueId.value
    loadCourtsForForm()
  }
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
  pointer-events: none;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: transparent;
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
  transition: all 0.3s ease;
  margin-bottom: 20px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
}

.glass-card:hover {
  border-color: var(--color-border-hover, #CBD5E1);
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.page-header {
  padding: 28px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.header-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 12px;
  color: #fff;
}

.page-title {
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 8px 0;
}

.page-subtitle {
  margin: 0;
  color: var(--color-text-secondary, #64748B);
}

.header-stats {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stat-item {
  width: 80px;
  min-width: 80px;
  max-width: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 12px 8px;
  border-radius: 14px;
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.stat-item-success {
  background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%) !important;
}

.stat-item-warning {
  background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%) !important;
}

.stat-item-primary {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%) !important;
}

.stat-item-info {
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%) !important;
}

.stat-item-danger {
  background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 100%) !important;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary, #2563EB);
}

.search-card {
  padding: 24px;
}

.search-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.search-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.search-container {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-end;
}

.search-fields {
  display: flex;
  gap: 16px;
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
  width: 180px;
}

.search-select-small {
  width: 140px;
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-card {
  padding: 16px 24px;
}

.table-card {
  padding: 24px;
}

.price-text {
  color: var(--color-danger, #ef4444);
  font-weight: 600;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.pagination-wrapper {
  margin-top: 20px;
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
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, var(--color-card-bg, #ffffff) 0%, var(--color-background, #f8fafc) 100%);
  border-radius: 14px;
  border: 1px solid var(--color-border, rgba(226, 232, 240, 0.8));
}

.modern-section-title {
  margin: 0 0 14px 0;
  font-size: 16px;
  font-weight: 700;
}

.modern-form-item {
  margin-bottom: 16px !important;
}

.form-status-hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-left: 8px;
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.booking-conflict-alert {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(239, 68, 68, 0.22);
  background: linear-gradient(180deg, rgba(254, 242, 242, 0.96) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.booking-conflict-alert__head {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #b91c1c;
  font-weight: 700;
}

.booking-conflict-alert__body {
  margin-top: 8px;
  color: #7f1d1d;
  font-size: 13px;
  line-height: 1.6;
}

.booking-conflict-alert__list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.booking-conflict-alert__item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  color: #7f1d1d;
  font-size: 12px;
}

.modern-btn-cancel {
  padding: 10px 22px;
}

.modern-btn-submit {
  padding: 10px 24px;
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
  .search-fields {
    width: 100%;
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
