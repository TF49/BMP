<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon"><el-icon :size="32"><Tickets /></el-icon></div>
            <div class="header-text">
              <h2 class="page-title">课程预约</h2>
              <p class="page-subtitle">管理课程预约、状态变更与支付信息，支持会员联动</p>
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
              <span class="stat-value">{{ statistics.running || 0 }}</span>
            </div>
            <div class="stat-item stat-item-info" @click="handleStatClick(4)">
              <span class="stat-label">已完成</span>
              <span class="stat-value">{{ statistics.finished || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">已取消</span>
              <span class="stat-value">{{ statistics.canceled || 0 }}</span>
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
              <el-form-item label="预约单号" class="search-item">
                <el-input v-model="searchForm.bookingNo" placeholder="CB 开头" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="课程" class="search-item">
                <el-select v-model="searchForm.courseId" placeholder="全部课程" clearable class="search-select">
                  <el-option v-for="item in courseOptions" :key="item.id" :label="item.courseName" :value="item.id" />
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
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">新增预约</el-button>
      </div>

      <div class="glass-card table-card">
        <el-table
          :data="bookingList"
          v-loading="loading"
          border
          stripe
          :height="tableHeight"
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
        >
          <el-table-column prop="bookingNo" label="预约单号" min-width="140" />
          <el-table-column prop="courseName" label="课程" min-width="140" />
          <el-table-column prop="memberName" label="会员" min-width="120" />
          <el-table-column label="金额" min-width="110">
            <template #default="scope">
              <span class="price-text">¥{{ formatCurrency(scope.row.orderAmount) }}</span>
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
            <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" min-width="300" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button v-if="isAdmin" type="success" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <!-- 待支付显示支付，已支付显示退款（同一位置互斥，与赛事报名/场地预约一致） -->
                <el-button
                  v-if="isAdmin && scope.row.status === 1"
                  type="primary"
                  size="small"
                  plain
                  @click="openPay(scope.row)"
                >
                  支付
                </el-button>
                <el-button
                  v-else-if="isAdmin && scope.row.paymentStatus === 1"
                  type="warning"
                  size="small"
                  plain
                  @click="openRefund(scope.row)"
                >
                  退款
                </el-button>
                <el-button v-if="isAdmin || isUser" type="warning" size="small" plain @click="changeStatus(scope.row, 0)">取消</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="640px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" class="modern-form">
        <div class="modern-form-section">
          <h4 class="modern-section-title">预约信息</h4>
          <el-form-item label="预约单号" class="modern-form-item">
            <el-input :model-value="form.bookingNo || '提交后自动生成'" disabled />
          </el-form-item>
          <el-form-item label="课程" prop="courseId" class="modern-form-item">
            <el-select v-model="form.courseId" placeholder="选择课程" filterable>
              <el-option
                v-for="item in courseOptions"
                :key="item.id"
                :label="item.courseName"
                :value="item.id"
              >
                <span>{{ item.courseName }}</span>
                <span style="margin-left: 8px; color: var(--color-text-secondary, #64748B)">
                  教练：{{ item.coachName || '未分配' }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="会员" prop="memberId" class="modern-form-item">
            <el-input v-model="memberKeyword" placeholder="输入姓名/手机号搜索" @change="loadMembers" clearable />
            <el-select v-model="form.memberId" placeholder="选择会员" filterable style="width: 100%; margin-top: 8px">
              <el-option v-for="item in memberOptions" :key="item.id" :label="item.memberName" :value="item.id">
                <span>{{ item.memberName }}</span>
                <span style="margin-left: 8px; color: var(--color-text-secondary, #64748B)">{{ item.phone }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="金额" prop="amount" class="modern-form-item">
            <el-input-number v-model="form.amount" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="支付方式" class="modern-form-item">
            <!-- 创建时为空；已支付的预约在这里只做查看，支付/修改仍通过“支付/退款”弹窗处理 -->
            <el-select v-model="form.paymentMethod" placeholder="创建后在支付弹窗中选择" disabled>
              <el-option label="余额支付" value="BALANCE" />
              <el-option label="现金" value="CASH" />
              <el-option label="支付宝" value="ALIPAY" />
              <el-option label="微信" value="WECHAT" />
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
            {{ isEdit ? '更新预约' : '创建预约' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 支付弹窗（与场地预约/器材租借一致） -->
    <el-dialog v-model="payDialogVisible" title="课程预约支付" width="420px">
      <el-form label-width="100px">
        <el-form-item label="预约单号">
          <el-tag type="info">{{ currentPay?.bookingNo || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input-number v-model="payForm.amount" :min="0.01" :precision="2" :step="10" disabled />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.method" placeholder="请选择">
            <el-option label="余额" value="BALANCE" />
            <el-option label="现金" value="CASH" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="submitPay">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 退款弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="课程预约退款" width="420px">
      <el-form label-width="100px">
        <el-form-item label="预约单号">
          <el-tag type="warning">{{ currentRefund?.bookingNo || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="0.01" :precision="2" :step="10" disabled />
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Tickets } from '@element-plus/icons-vue'
import {
  getCourseBookingList,
  getCourseBookingInfo,
  addCourseBooking,
  updateCourseBooking,
  deleteCourseBooking,
  updateCourseBookingStatus,
  getCourseBookingStatistics,
  getCourseBookingCourses,
  getCourseBookingMembers,
  payCourseBooking,
  refundCourseBooking
} from '@/api/courseBooking'

const searchForm = reactive({
  bookingNo: '',
  courseId: null,
  memberKeyword: '',
  status: null,
  timeRange: []
})

const bookingList = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statistics = reactive({ total: 0, pending: 0, paid: 0, running: 0, finished: 0, canceled: 0 })
const courseOptions = ref([])
const memberOptions = ref([])
const memberKeyword = ref('')
const tableHeight = ref(600)
const { isAdmin, isUser } = useAuth()

const dialogVisible = ref(false)
const dialogTitle = ref('新增预约')
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  bookingNo: '',
  courseId: null,
  memberId: null,
  amount: 0,
  paymentMethod: null,
  status: 1
})
const formRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const submitLoading = ref(false)

// 支付/退款（与器材租借、场地预约一致）
const payDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const payLoading = ref(false)
const refundLoading = ref(false)
const currentPay = ref(null)
const currentRefund = ref(null)
const payForm = reactive({ amount: 0, method: 'BALANCE' })
const refundForm = reactive({ amount: 0 })

const statusOptions = [
  { label: '已取消', value: 0 },
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '进行中', value: 3 },
  { label: '已完成', value: 4 }
]

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: '' }
  return map[status] || 'info'
}

const getPaymentMethodText = (method) => {
  const map = { BALANCE: '余额支付', CASH: '现金', WECHAT: '微信', ALIPAY: '支付宝' }
  return map[method] || method || '-'
}

const getPaymentMethodType = (method) => {
  // 区分不同支付方式的颜色：现金红色，余额灰色，微信绿色，支付宝蓝色
  const map = { BALANCE: 'info', CASH: 'danger', WECHAT: 'success', ALIPAY: 'primary' }
  return map[method] || 'info'
}

const getPaymentStatusText = (status) => {
  const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
  return map[status] ?? '未知'
}

const getPaymentStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] ?? 'info'
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

const loadCourses = async () => {
  try {
    const res = await getCourseBookingCourses()
    if (res.code === 200) {
      courseOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载课程下拉失败:', e)
  }
}

const loadMembers = async () => {
  if (!memberKeyword.value) {
    memberOptions.value = []
    return
  }
  try {
    const res = await getCourseBookingMembers(memberKeyword.value)
    if (res.code === 200) {
      memberOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载会员失败:', e)
  }
}

const loadStatistics = async () => {
  try {
    const res = await getCourseBookingStatistics()
    if (res.code === 200) {
      const data = res.data || {}
      // 后端返回 cancelled/ongoing，前端展示用 canceled/running
      statistics.total = data.total ?? 0
      statistics.pending = data.pending ?? 0
      statistics.paid = data.paid ?? 0
      statistics.running = data.ongoing ?? 0
      statistics.finished = data.finished ?? 0
      statistics.canceled = data.cancelled ?? 0
    }
  } catch (e) {
    console.error('加载统计失败:', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.bookingNo || searchForm.memberKeyword || undefined,
      courseId: searchForm.courseId || undefined,
      status: searchForm.status ?? undefined,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getCourseBookingList(params)
    if (res.code === 200) {
      bookingList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取预约列表失败')
      bookingList.value = []
    }
  } catch (e) {
    console.error('加载预约列表失败:', e)
    ElMessage.error('加载预约列表失败，请稍后重试')
    bookingList.value = []
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
    bookingNo: '',
    courseId: null,
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
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑预约'
  isEdit.value = true
  try {
    const res = await getCourseBookingInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      Object.assign(form, {
        id: data.id,
        bookingNo: data.bookingNo,
        courseId: data.courseId,
        memberId: data.memberId,
        amount: data.orderAmount ?? 0,
        paymentMethod: data.paymentMethod || null,
        status: data.status
      })
    }
  } catch (e) {
    console.error('加载预约详情失败:', e)
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除预约 ${row.bookingNo} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteCourseBooking(row.id)
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
      const payload = {
        id: form.id,
        courseId: form.courseId,
        memberId: form.memberId,
        orderAmount: form.amount,
        status: form.status
      }
      const res = isEdit.value ? await updateCourseBooking(payload) : await addCourseBooking(payload)
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

const changeStatus = async (row, status) => {
  try {
    const res = await updateCourseBookingStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success('状态已更新')
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '更新状态失败')
    }
  } catch (e) {
    console.error('更新状态失败:', e)
    const errorMessage = e.response?.data?.message || e.message || '更新状态失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

// 支付（与场地预约一致：弹窗选支付方式）
const openPay = (row) => {
  currentPay.value = row
  payForm.amount = row.orderAmount || 0
  payForm.method = 'BALANCE'
  payDialogVisible.value = true
}

const submitPay = async () => {
  if (!currentPay.value?.id) return
  payLoading.value = true
  try {
    const res = await payCourseBooking({
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

// 退款（与器材租借/场地预约一致：后端按订单金额退款）
const openRefund = (row) => {
  currentRefund.value = row
  refundForm.amount = row.orderAmount || 0
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  if (!currentRefund.value?.id) return
  refundLoading.value = true
  try {
    const res = await refundCourseBooking({ bookingId: currentRefund.value.id })
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

const resetForm = () => {
  Object.assign(form, {
    id: null,
    bookingNo: '',
    courseId: null,
    memberId: null,
    amount: 0,
    paymentMethod: null,
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
  loadCourses()
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

.stat-item-warning {
  background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%) !important;
}

.stat-item-success {
  background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%) !important;
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
  width: 170px;
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
  color: var(--color-text-primary, #666666);
  transition: all 0.3s ease;
  font-family: 'Fira Sans', sans-serif;
}

.pagination :deep(.el-pagination__total) {
  color: var(--color-text-secondary, #666666);
  font-size: 13px;
  font-weight: 500;
}

.pagination :deep(.el-pagination__jump) {
  color: var(--color-text-secondary, #666666);
  font-size: 13px;
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #DCDFE6);
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
  border: 1px solid var(--color-border, #DCDFE6);
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
  color: var(--color-text-primary, #666666);
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
