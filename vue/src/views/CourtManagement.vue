<template>
  <div class="court-management">
    <div class="court-management-background">
      <div class="gradient-overlay"></div>
    </div>

    <div class="court-management-content">
      <!-- 页面标题 -->
      <div class="page-header glass-card">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32">
                <Grid />
              </el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">场地管理</h2>
              <p class="page-subtitle">管理各场馆下的羽毛球场地信息，支持搜索、添加、编辑和删除操作</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总场地数</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(1)">
              <span class="stat-label">空闲</span>
              <span class="stat-value">{{ statistics.available || 0 }}</span>
            </div>
            <div class="stat-item stat-item-primary" @click="handleStatClick(2)">
              <span class="stat-label">预约中</span>
              <span class="stat-value">{{ statistics.reserved || 0 }}</span>
            </div>
            <div class="stat-item stat-item-warning" @click="handleStatClick(3)">
              <span class="stat-label">使用中</span>
              <span class="stat-value">{{ statistics.inUse || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">维护中</span>
              <span class="stat-value">{{ statistics.maintenance || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="glass-card search-card">
        <div class="search-header">
          <h3 class="search-title">搜索过滤</h3>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="search-container">
            <div class="search-fields">
              <el-form-item label="关键词" class="search-item">
                <el-input v-model="searchForm.keyword" placeholder="场地编号/名称" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="所属场馆" class="search-item">
                <el-select
                  v-model="searchForm.venueId"
                  placeholder="请选择场馆"
                  :clearable="!isVenueManager"
                  :disabled="isVenueManager"
                  class="search-select"
                >
                  <el-option v-for="venue in venueOptions" :key="venue.id" :label="venue.venueName" :value="venue.id">
                    <span>{{ venue.venueName }}</span>
                    <el-tag v-if="venue.status === 0" type="danger" size="small" style="margin-left: 8px">关闭</el-tag>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="search-select-small">
                  <el-option label="维护中" :value="0" />
                  <el-option label="空闲" :value="1" />
                  <el-option label="预约中" :value="2" />
                  <el-option label="使用中" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="isAdmin" label="查看日期" class="search-item">
                <el-date-picker
                  v-model="viewDateForBookings"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :clearable="false"
                  @change="onViewDateChange"
                  class="search-date-picker"
                />
              </el-form-item>
            </div>
            <div class="search-buttons">
              <el-button type="primary" @click="handleSearch" :icon="Search" class="search-btn bmp-uiv-btn bmp-uiv-btn-primary">
                搜索
              </el-button>
              <el-button type="primary" @click="handleReset" :icon="Refresh" class="reset-btn bmp-uiv-btn bmp-uiv-btn-primary">重置</el-button>
            </div>
          </div>
          <div v-if="isAdmin" class="view-date-hint">
            状态与“当天使用者”均按上方选择的日期统计（维护中场地始终显示为维护中）。
          </div>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="glass-card action-card" v-if="userRole === 'PRESIDENT' || userRole === 'VENUE_MANAGER'">
        <el-button type="primary" @click="handleAdd" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary">
          添加场地
        </el-button>
      </div>

      <!-- 场地列表表格 -->
      <div class="glass-card table-card">
        <el-table :data="courtList" v-loading="loading" element-loading-text="加载中..." style="width: 100%" stripe border
          highlight-current-row :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }" row-key="id" :height="tableHeight">
          <el-table-column
            label="序号"
            type="index"
            min-width="80"
            align="center"
            :index="(index) => (pagination.page - 1) * pagination.size + index + 1"
          />
          <el-table-column prop="courtCode" label="场地编号" min-width="100" align="center">
            <template #default="scope">
              <el-tag type="info" size="small">{{ scope.row.courtCode }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="courtName" label="场地名称" min-width="120" align="center" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.courtName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="venueName" label="所属场馆" min-width="150" align="center">
            <template #default="scope">
              <el-tag type="primary" size="small">{{ scope.row.venueName || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="billingType" label="计费方式" min-width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.billingType === 'HOUR' ? 'success' : 'warning'" size="small">
                {{ getBillingTypeText(scope.row.billingType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="pricePerHour" label="价格" min-width="100" align="center">
            <template #default="scope">
              <span class="price-text">¥{{ scope.row.pricePerHour }}</span>
              <span class="price-unit">/{{ scope.row.billingType === 'HOUR' ? '小时' : '次' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100" align="center">
            <template #default="scope">
              <el-tag :type="getDisplayStatus(scope.row).type" size="small">
                {{ getDisplayStatus(scope.row).text }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="当天使用者" min-width="140" align="center">
            <template #default="scope">
              <el-button
                :type="getBookingCount(scope.row.id) > 0 ? 'primary' : 'info'"
                size="small"
                plain
                class="booking-count-btn"
                @click="openBookingDialog(scope.row)"
              >
                <el-icon><User /></el-icon>
                <span class="booking-count-text">{{ getBookingCount(scope.row.id) }} 人</span>
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200" fixed="right" align="center">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button v-if="userRole === 'PRESIDENT' || userRole === 'VENUE_MANAGER'" type="success" size="small" @click="handleEdit(scope.row)" :icon="Edit" plain>
                  编辑
                </el-button>
                <el-button v-if="userRole === 'PRESIDENT' || userRole === 'VENUE_MANAGER'" type="danger" size="small" @click="handleDelete(scope.row)" :icon="Delete" plain>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange" @current-change="handlePageChange" class="pagination" />
        </div>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" :close-on-click-modal="false"
      class="court-dialog modern-dialog" @close="handleDialogClose" width="600px">
      <el-form ref="courtFormRef" :model="courtForm" :rules="courtFormRules" label-width="auto" label-position="top"
        class="court-form modern-form">
        <!-- 基本信息 -->
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">基本信息</h4>
          
          <el-form-item label="所属场馆" prop="venueId" class="form-item-enhanced modern-form-item">
            <el-select
              v-model="courtForm.venueId"
              placeholder="请选择所属场馆"
              class="form-select"
              style="width: 100%"
              :disabled="isVenueManager"
            >
              <el-option v-for="venue in venueOptions" :key="venue.id" :label="venue.venueName" :value="venue.id"
                :disabled="venue.status === 0">
                <span>{{ venue.venueName }}</span>
                <el-tag v-if="venue.status === 0" type="danger" size="small" style="margin-left: 8px">关闭</el-tag>
                <el-tag v-else-if="venue.status === 2" type="warning" size="small" style="margin-left: 8px">暂停</el-tag>
              </el-option>
            </el-select>
            <span class="field-hint modern-field-hint">选择场地所属的场馆</span>
          </el-form-item>

          <el-form-item label="场地编号" prop="courtCode" class="form-item-enhanced modern-form-item">
            <el-input v-model="courtForm.courtCode" placeholder="请输入场地编号（如：A1、B2）" clearable maxlength="50"
              show-word-limit class="form-input modern-form-input">
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <Grid />
                </el-icon>
              </template>
            </el-input>
            <span class="field-hint modern-field-hint">同一场馆内场地编号不能重复</span>
          </el-form-item>

          <el-form-item label="场地名称" prop="courtName" class="form-item-enhanced modern-form-item">
            <el-input v-model="courtForm.courtName" placeholder="请输入场地名称（可选）" clearable maxlength="100"
              show-word-limit class="form-input modern-form-input">
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <Tickets />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <!-- 计费设置 -->
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">计费设置</h4>

          <el-form-item label="计费方式" prop="billingType" class="form-item-enhanced modern-form-item">
            <el-radio-group v-model="courtForm.billingType" class="billing-type-group">
              <el-radio label="HOUR" class="billing-radio">
                <span class="billing-label">
                  <el-icon class="billing-icon"><Clock /></el-icon>
                  按小时
                </span>
              </el-radio>
              <el-radio label="TIME" class="billing-radio">
                <span class="billing-label">
                  <el-icon class="billing-icon"><Ticket /></el-icon>
                  按次
                </span>
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="价格" prop="pricePerHour" class="form-item-enhanced modern-form-item">
            <el-input-number v-model="courtForm.pricePerHour" :min="0.01" :precision="2" :step="10"
              placeholder="请输入价格" class="price-input" />
            <span class="price-suffix">元 / {{ courtForm.billingType === 'HOUR' ? '小时' : '次' }}</span>
            <span class="field-hint modern-field-hint">设置场地的基础价格，会员可享受折扣优惠</span>
          </el-form-item>
        </div>

        <!-- 状态设置 -->
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">状态设置</h4>

          <el-form-item label="场地状态" prop="status" class="form-item-enhanced modern-form-item">
            <div class="status-selector modern-status-selector">
              <el-radio-group v-model="courtForm.status" class="radio-group-enhanced modern-radio-group">
                <el-radio :label="1" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon available"><SuccessFilled /></el-icon>
                    空闲
                  </span>
                </el-radio>
                <el-radio :label="0" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon maintenance"><Warning /></el-icon>
                    维护中
                  </span>
                </el-radio>
              </el-radio-group>
            </div>
            <span class="field-hint modern-field-hint">维护中的场地无法被预约</span>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-cancel modern-btn-cancel">
            取消
          </el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="btn-submit modern-btn-submit">
            {{ isEdit ? '更新场地' : '创建场地' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 当天使用者弹窗（借鉴 Galaxy 卡片与 React Bits 动效：居中浮层 + 缩放淡入） -->
    <el-dialog
      v-model="bookingDialogVisible"
      :title="bookingDialogTitle"
      width="540px"
      top="15vh"
      class="booking-dialog modern-dialog"
      :close-on-click-modal="true"
    >
      <transition name="booking-dialog-zoom">
        <div v-if="bookingDialogVisible" class="booking-users-modal">
          <div class="modal-header">
            <div class="modal-header-main">
              <div class="modal-icon">
                <el-icon><View /></el-icon>
              </div>
              <div class="modal-title-group">
                <h4 class="modal-title">当天使用者 - {{ bookingDialogCourt?.courtCode || '-' }}</h4>
                <p class="modal-subtitle">
                  {{ viewDateForBookings }} · {{ bookingDialogCourt?.venueName || '所属场馆' }}
                </p>
              </div>
            </div>
            <div class="modal-header-meta">
              <el-tag size="small" type="info">
                共 {{ currentBookingUsers.length }} 条记录
              </el-tag>
            </div>
          </div>

          <div v-loading="bookingUsersLoading" class="modal-content">
            <div v-if="currentBookingUsers.length === 0 && !bookingUsersLoading" class="no-booking">
              <el-empty description="该日暂无预约" :image-size="80" />
            </div>
            <div v-else class="booking-user-list">
              <div
                v-for="user in currentBookingUsers"
                :key="user.bookingId"
                class="booking-user-item booking-user-card"
              >
                <div class="user-info">
                  <span class="user-name">{{ user.memberName }}</span>
                  <el-tag
                    v-if="user.memberType === 'MEMBER'"
                    :type="getMemberTagType(user)"
                    size="small"
                    :class="{ 'vip-tag': user.memberLevel >= 5 }"
                  >
                    {{ getMemberLevelText(user) }}
                  </el-tag>
                </div>
                <div class="booking-info">
                  <span class="time-range">{{ user.startTime }} - {{ user.endTime }}</span>
                  <el-tag :type="getBookingStatusTagType(user.status)" size="small">
                    {{ user.statusText }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <template #footer>
        <div class="booking-dialog-footer">
          <el-button @click="bookingDialogVisible = false" class="modern-btn-cancel">
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  Grid,
  Tickets,
  Clock,
  Ticket,
  SuccessFilled,
  Warning,
  User,
  View
} from '@element-plus/icons-vue'
import {
  getCourtList,
  addCourt,
  updateCourt,
  deleteCourt,
  getCourtStatistics,
  getVenueOptions,
  getTodayBookingUsers,
  getTodayBookingCounts
} from '@/api/court'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  venueId: null,
  status: null
})

// 场馆下拉选项
const venueOptions = ref([])

// 场地列表
const courtList = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 统计数据
const statistics = reactive({
  total: 0,
  maintenance: 0,
  available: 0,
  reserved: 0,
  inUse: 0
})

// 获取用户本地「今天」的日期 yyyy-MM-dd
const getLocalTodayDateStr = () => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

// 用于「当天使用者」查询的日期，默认用户本地今天；可切换以查看其他日期
const viewDateForBookings = ref(getLocalTodayDateStr())

// 当天预约数量映射（courtId -> count）
const todayBookingCounts = ref({})

// 当天使用者信息
const currentBookingUsers = ref([])
const bookingUsersLoading = ref(false)

// 当天使用者弹窗状态
const bookingDialogVisible = ref(false)
const bookingDialogCourt = ref(null)
const bookingDialogTitle = computed(() => '当天使用者')

// 表格高度
const tableHeight = ref(600)

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

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加场地')
const isEdit = ref(false)
const submitLoading = ref(false)
const courtFormRef = ref(null)

// 场地表单
const courtForm = reactive({
  id: null,
  venueId: null,
  courtCode: '',
  courtName: '',
  billingType: 'HOUR',
  pricePerHour: 50,
  status: 1
})

// 表单验证规则
const courtFormRules = {
  venueId: [
    { required: true, message: '请选择所属场馆', trigger: 'change' }
  ],
  courtCode: [
    { required: true, message: '请输入场地编号', trigger: 'blur' },
    { max: 50, message: '场地编号长度不能超过50个字符', trigger: 'blur' }
  ],
  courtName: [
    { max: 100, message: '场地名称长度不能超过100个字符', trigger: 'blur' }
  ],
  billingType: [
    { required: true, message: '请选择计费方式', trigger: 'change' }
  ],
  pricePerHour: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 表格样式
const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

// 获取计费方式文本
const getBillingTypeText = (type) => {
  const typeMap = {
    'HOUR': '按小时',
    'TIME': '按次'
  }
  return typeMap[type] || '未知'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '维护中',
    1: '空闲',
    2: '预约中',
    3: '使用中'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    2: 'primary',
    3: 'warning'
  }
  return typeMap[status] || 'info'
}

// 根据查看日期与预约数量计算用于展示的状态（不改变后端真实状态含义）
// 规则：
// - 维护中：始终显示维护中（与日期无关）
// - 其他状态：完全按“查看日期”的预约情况展示
//   - 该日有预约 → 显示“有预约”
//   - 该日无预约 → 显示“空闲”
const getDisplayStatus = (row) => {
  const rawStatus = row?.status

  // 维护中始终优先生效，与日期无关
  if (rawStatus === 0) {
    return {
      text: getStatusText(0),
      type: getStatusType(0)
    }
  }

  // 按当前查看日期的预约数量派生展示
  const count = getBookingCount(row.id)
  if (count > 0) {
    return {
      text: '有预约',
      type: 'primary'
    }
  }

  return {
    text: '空闲',
    type: 'success'
  }
}

// 加载场馆下拉列表
const loadVenueOptions = async () => {
  try {
    const response = await getVenueOptions()
    if (response.code === 200) {
      venueOptions.value = response.data || []
    }
  } catch (error) {
    console.error('加载场馆列表失败:', error)
  }
}

// 加载场地列表
const loadCourtList = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.keyword || null,
      venueId: searchForm.venueId,
      status: searchForm.status,
      page: pagination.page,
      size: pagination.size
    }
    const response = await getCourtList(params)
    if (response.code === 200) {
      // 确保 courtList 始终是数组，防止 el-table 报错 "data2 is not iterable"
      const data = response?.data?.data
      courtList.value = Array.isArray(data) ? data : (Array.isArray(response?.data) ? response.data : [])
      pagination.total = response?.data?.total || 0
      // 加载完场地列表后，获取当天预约数量
      loadTodayBookingCounts()
    } else {
      ElMessage.error(response.message || '获取场地列表失败')
      courtList.value = []
    }
  } catch (error) {
    console.error('加载场地列表失败:', error)
    ElMessage.error('加载场地列表失败，请稍后重试')
    courtList.value = []  // 出错时重置为空数组
  } finally {
    loading.value = false
  }
}

// 查看日期变更时重新加载预约数量
const onViewDateChange = () => {
  loadTodayBookingCounts()
}

// 加载当天预约数量（使用 viewDateForBookings）
const loadTodayBookingCounts = async () => {
  if (courtList.value.length === 0) {
    todayBookingCounts.value = {}
    return
  }
  try {
    const courtIds = courtList.value.map(court => court.id)
    const response = await getTodayBookingCounts(courtIds, viewDateForBookings.value || getLocalTodayDateStr())
    if (response.code === 200) {
      todayBookingCounts.value = response.data || {}
    }
  } catch (error) {
    console.error('加载预约数量失败:', error)
    // 不显示错误提示，静默失败
  }
}

// 获取场地当天预约数量
const getBookingCount = (courtId) => {
  return todayBookingCounts.value[courtId] || 0
}

// 打开当天使用者弹窗并加载数据
const openBookingDialog = (row) => {
  bookingDialogCourt.value = row
  bookingDialogVisible.value = true
  loadBookingUsers(row.id)
}

// 加载指定场地的当天使用者列表
const loadBookingUsers = async (courtId) => {
  bookingUsersLoading.value = true
  currentBookingUsers.value = []
  try {
    const response = await getTodayBookingUsers(courtId, viewDateForBookings.value || getLocalTodayDateStr())
    if (response.code === 200) {
      currentBookingUsers.value = response.data || []
    }
  } catch (error) {
    console.error('加载使用者列表失败:', error)
  } finally {
    bookingUsersLoading.value = false
  }
}

// 获取会员等级显示文本
const getMemberLevelText = (user) => {
  if (user.memberType === 'NORMAL' || !user.memberType) {
    return '普通用户'
  }
  if (!user.memberLevel) {
    return '会员'
  }
  if (user.memberLevel >= 3) {
    return `VIP Lv.${user.memberLevel}`
  }
  return `会员 Lv.${user.memberLevel}`
}

// 获取会员标签类型
const getMemberTagType = (user) => {
  if (user.memberType === 'NORMAL' || !user.memberType) {
    return 'info'
  }
  if (!user.memberLevel) {
    return 'primary'
  }
  const levelMap = {
    1: '',        // Lv.1 - 默认蓝色
    2: 'success', // Lv.2 - 绿色
    3: 'warning', // Lv.3 - 橙色
    4: 'danger',  // Lv.4 - 红色（表示高级）
    5: 'danger'   // Lv.5 - 金色（用 danger + 自定义样式）
  }
  return levelMap[user.memberLevel] || ''
}

// 获取预约状态标签类型
const getBookingStatusTagType = (status) => {
  const typeMap = {
    1: 'warning', // 待支付 - 橙色
    2: 'success', // 已支付 - 绿色
    3: 'primary', // 进行中 - 蓝色
    4: 'info'     // 已完成 - 灰色
  }
  return typeMap[status] || 'info'
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await getCourtStatistics()
    if (response.code === 200) {
      Object.assign(statistics, response.data || {})
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 统计卡片点击处理
const handleStatClick = (status) => {
  searchForm.status = status
  pagination.page = 1
  loadCourtList()
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadCourtList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.venueId = null
  searchForm.status = null
  pagination.page = 1
  loadCourtList()
}

// 添加场地
const handleAdd = () => {
  dialogTitle.value = '添加场地'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑场地
const handleEdit = (row) => {
  dialogTitle.value = '编辑场地'
  isEdit.value = true
  Object.assign(courtForm, {
    id: row.id,
    venueId: row.venueId,
    courtCode: row.courtCode || '',
    courtName: row.courtName || '',
    billingType: row.billingType || 'HOUR',
    pricePerHour: row.pricePerHour || 50,
    status: row.status !== undefined ? row.status : 1
  })
  dialogVisible.value = true
}

// 删除场地
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除场地 "${row.courtCode}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteCourt(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadCourtList()
        loadStatistics()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除场地失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => { })
}

// 提交表单
const handleSubmit = async () => {
  if (!courtFormRef.value) return

  // 场馆管理员兜底：如果未选择场馆，则自动填充为当前登录用户所属场馆
  if (isVenueManager.value && currentVenueId.value && !courtForm.venueId) {
    courtForm.venueId = currentVenueId.value
  }

  await courtFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const formData = { ...courtForm }

        const response = isEdit.value
          ? await updateCourt(formData)
          : await addCourt(formData)

        if (response.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadCourtList()
          loadStatistics()
        } else {
          ElMessage.error(response.message || (isEdit.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error('提交失败:', error)
        // 业务错误（如编号重复）由 request 拦截器已提示，不再重复
        if (!error.message || error.message === 'Error') {
          ElMessage.error('操作失败，请稍后重试')
        }
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(courtForm, {
    id: null,
    // 场馆管理员重置表单时，自动带上自己所属场馆；其他角色保持为空自行选择
    venueId: isVenueManager.value && currentVenueId.value ? currentVenueId.value : null,
    courtCode: '',
    courtName: '',
    billingType: 'HOUR',
    pricePerHour: 50,
    status: 1
  })
  if (courtFormRef.value) {
    courtFormRef.value.resetFields()
  }
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadCourtList()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  loadCourtList()
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 计算表格高度
const calculateTableHeight = () => {
  const windowHeight = window.innerHeight
  tableHeight.value = Math.max(400, windowHeight - 450)
}

// 初始化
onMounted(() => {
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
  loadVenueOptions()
  loadCourtList()
  loadStatistics()
})

// VM 默认锁定自己场馆（只做前端便捷性，后端仍兜底）
onMounted(() => {
  if (isVenueManager.value && currentVenueId.value) {
    searchForm.venueId = currentVenueId.value
    courtForm.venueId = currentVenueId.value
  }
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('resize', calculateTableHeight)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');

.court-management {
  padding: 20px;
  background-color: var(--color-background, #f0f2f5);
  height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
}

.court-management::-webkit-scrollbar {
  width: 8px;
}

.court-management::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.court-management::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 4px;
}

.court-management::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted, #94A3B8);
}

.court-management-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--color-background, #F8FAFC);
  z-index: 0;
  pointer-events: none;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent;
}

.court-management-content {
  position: relative;
  z-index: 1;
  padding: 0;
  max-width: 1600px;
  margin: 0 auto;
}

.glass-card {
  background: var(--color-card-bg, #FFFFFF);
  backdrop-filter: none;
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
}

.glass-card:hover {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-border-hover, #CBD5E1);
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.page-header {
  padding: 28px;
  animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  transition: all 0.3s ease;
  color: #FFFFFF;
}

.header-icon:hover {
  transform: scale(1.1) rotate(5deg);
}

.header-text {
  flex: 1;
}

.page-title {
  font-family: 'Fira Sans', sans-serif;
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

.header-stats {
  /**
   * 统计卡片等宽优化：使用固定宽度确保所有按钮大小一致
   */
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: nowrap;
}

.stat-item {
  /**
   * 固定宽度方案：确保所有按钮视觉上完全一致
   */
  width: 72px;
  min-width: 72px;
  max-width: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 14px 8px;
  background: var(--gradient-members, linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%));
  border-radius: 14px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.stat-item-success {
  background: linear-gradient(135deg, #D1FAE5 0%, #ECFDF5 100%) !important;
}

.stat-item-success:hover {
  box-shadow: 0 8px 16px rgba(16, 185, 129, 0.2) !important;
}

.stat-item-success .stat-value {
  color: var(--color-success, #10b981);
}

.stat-item-primary {
  background: var(--gradient-members, linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%)) !important;
}

.stat-item-primary:hover {
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.2) !important;
}

.stat-item-primary .stat-value {
  color: var(--color-primary, #3b82f6);
}

.stat-item-warning {
  background: linear-gradient(135deg, #FEF3C7 0%, #FFFBEB 100%) !important;
}

.stat-item-warning:hover {
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.2) !important;
}

.stat-item-warning .stat-value {
  color: var(--color-warning, #f59e0b);
}

.stat-item-danger {
  background: linear-gradient(135deg, #FEE2E2 0%, #FEF2F2 100%) !important;
}

.stat-item-danger:hover {
  box-shadow: 0 8px 16px rgba(239, 68, 68, 0.2) !important;
}

.stat-item-danger .stat-value {
  color: var(--color-danger, #ef4444);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
  text-align: center;
  line-height: 1.4;
  word-break: keep-all;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary, #3b82f6);
  font-family: 'Fira Code', monospace;
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
  font-family: 'Fira Sans', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.search-form {
  margin: 0;
}

.search-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
}

.search-fields {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: flex-end;
  flex: 1;
}

.search-item {
  margin-bottom: 0 !important;
}

.search-input {
  width: 180px;
}

.search-select {
  width: 200px;
}

.search-select-small {
  width: 140px;
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

.action-card {
  padding: 16px 24px;
}

.table-card {
  padding: 24px;
}

.table-card :deep(.el-table) {
  background: transparent;
  color: var(--color-text-primary, #1E293B);
}

.table-card :deep(.el-table th) {
  background: var(--color-card-bg-hover, #EFF6FF) !important;
  color: var(--color-text-primary, #1E293B) !important;
  border-bottom: 1px solid var(--color-border, #E2E8F0) !important;
  font-weight: 600;
  padding: 14px 0 !important;
}

.table-card :deep(.el-table td) {
  border-bottom: 1px solid var(--color-border, #E2E8F0) !important;
  padding: 14px 0 !important;
  color: var(--color-text-primary, #1E293B);
}

.table-card :deep(.el-table tr:hover > td) {
  background: var(--color-background, #F8FAFC) !important;
}

.price-text {
  font-weight: 600;
  color: var(--color-danger, #ef4444);
  font-size: 15px;
}

.price-unit {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  margin-left: 2px;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding-top: 16px;
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

/* 对话框样式 */
.court-dialog :deep(.el-dialog) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
}

.court-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  padding: 20px 24px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 16px 16px 0 0;
}

.court-dialog :deep(.el-dialog__title) {
  color: #ffffff;
  font-family: 'Fira Sans', sans-serif;
  font-size: 18px;
  font-weight: 600;
}

.court-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.modern-form-section {
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, var(--color-card-bg, #ffffff) 0%, var(--color-background, rgba(248, 250, 252, 0.95)) 100%);
  border-radius: 16px;
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
}

.modern-section-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  display: flex;
  align-items: center;
  gap: 12px;
}

.modern-section-title::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 2px;
}

.modern-form-item {
  margin-bottom: 20px !important;
}

.modern-form-input {
  width: 100%;
}

.modern-input-icon {
  color: var(--color-primary, #3B82F6);
  font-size: 16px;
}

.modern-field-hint {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  font-style: italic;
}

/* 计费方式样式 */
.billing-type-group {
  display: flex;
  gap: 20px;
  width: 100%;
}

.billing-radio {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border-radius: 12px;
  background: var(--color-card-bg, #ffffff);
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
  transition: all 0.3s ease;
}

.billing-radio:hover {
  border-color: var(--color-primary, rgba(59, 130, 246, 0.4));
}

.billing-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.billing-icon {
  font-size: 18px;
  color: var(--color-primary, #3B82F6);
}

/* 价格输入样式 */
.price-input {
  width: 200px;
}

.price-suffix {
  margin-left: 12px;
  font-size: 14px;
  color: var(--color-text-secondary, #64748b);
  font-weight: 500;
}

/* 状态选择器样式 */
.modern-status-selector {
  display: flex;
  gap: 20px;
  padding: 16px;
  background: var(--color-background, rgba(248, 250, 252, 0.9));
  border-radius: 12px;
}

.modern-radio-group {
  display: flex;
  gap: 20px;
  width: 100%;
}

.modern-radio-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 20px;
  border-radius: 12px;
  background: var(--color-card-bg, #ffffff);
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
  cursor: pointer;
  transition: all 0.3s ease;
}

.modern-radio-item:hover {
  border-color: var(--color-primary, rgba(59, 130, 246, 0.4));
}

.modern-radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.status-icon {
  font-size: 20px;
}

.status-icon.available {
  color: var(--color-success, #10b981);
}

.status-icon.maintenance {
  color: var(--color-warning, #f59e0b);
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 20px 0 0 0;
}

.modern-btn-cancel {
  padding: 12px 28px;
  font-weight: 600;
  border-radius: 12px;
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
  background: var(--color-card-bg, #ffffff);
  color: var(--color-text-secondary, #64748b);
}

.modern-btn-submit {
  padding: 12px 32px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border: none;
  color: #ffffff;
}

/* 当天使用者相关样式 */
.booking-count-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.booking-count-btn:hover {
  transform: scale(1.05);
}

.booking-count-text {
  font-weight: 500;
}

/* 弹窗整体样式（参考 Galaxy 卡片 + React Bits 弹出动画） */
.booking-dialog :deep(.el-dialog) {
  border-radius: 18px;
  border: 1px solid var(--color-border, #E2E8F0);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
  background: radial-gradient(circle at top left, rgba(59, 130, 246, 0.08), transparent 55%),
    radial-gradient(circle at bottom right, rgba(16, 185, 129, 0.08), transparent 55%),
    var(--color-card-bg, #FFFFFF);
}

.booking-dialog :deep(.el-dialog__header) {
  border-bottom: none;
  padding-bottom: 4px;
}

.booking-dialog :deep(.el-dialog__body) {
  padding-top: 0;
}

.booking-users-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.modal-header-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-icon {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12), rgba(59, 130, 246, 0.02));
  color: var(--color-primary, #2563EB);
}

.modal-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #0F172A);
}

.modal-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
}

.modal-header-meta {
  display: flex;
  align-items: center;
}

.modal-content {
  min-height: 120px;
  max-height: 420px;
}

.booking-dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

/* 复用原有列表样式，稍加增强为卡片效果 */
.no-booking {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
}

.booking-user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 360px;
  overflow-y: auto;
}

.booking-user-list::-webkit-scrollbar {
  width: 6px;
}

.booking-user-list::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 3px;
}

.booking-user-list::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 3px;
}

.booking-user-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #F9FAFB 0%, #EFF6FF 50%, #F9FAFB 100%);
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  transition: all 0.2s ease;
}

.booking-user-item:hover {
  background: var(--color-card-bg-hover, #EFF6FF);
  border-color: var(--color-border-hover, #BFDBFE);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-size: 14px;
}

.booking-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.time-range {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  font-family: 'Fira Code', monospace;
}

.booking-user-card {
  position: relative;
  overflow: hidden;
}

.booking-user-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top right, rgba(59, 130, 246, 0.18), transparent 55%);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.booking-user-card:hover::before {
  opacity: 1;
}

/* 弹窗缩放淡入动画（参考 React Bits 的 modal 动效节奏） */
.booking-dialog-zoom-enter-active,
.booking-dialog-zoom-leave-active {
  transition: all 0.22s ease-out;
}

.booking-dialog-zoom-enter-from,
.booking-dialog-zoom-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.96);
}

/* VIP 金色标签样式 */
.vip-tag {
  background: linear-gradient(135deg, var(--color-warning, #FFD700) 0%, var(--color-accent, #FFA500) 100%) !important;
  border: none !important;
  color: #FFFFFF !important;
  font-weight: 600 !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-stats {
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .stat-item {
    width: 68px;
    min-width: 68px;
    max-width: 68px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-container {
    flex-direction: column;
    align-items: stretch;
  }

  .search-fields {
    flex-direction: column;
  }

  .search-input,
  .search-select,
  .search-select-small {
    width: 100%;
  }

  .operation-buttons {
    flex-direction: column;
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
