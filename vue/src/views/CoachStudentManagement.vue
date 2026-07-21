<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <!-- 页面头部 -->
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">学员管理</h2>
              <p class="page-subtitle">管理教练与学员的绑定关系，查看学员的排课、考勤及消费记录</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick()">
              <span class="stat-label">绑定总数</span>
              <span class="stat-value">{{ pagination.total || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索过滤 -->
      <div class="glass-card search-card">
        <div class="search-header">
          <h3 class="search-title">搜索过滤</h3>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="search-container">
            <div class="search-fields">
              <el-form-item label="关键字" class="search-item">
                <el-input 
                  v-model="searchForm.keyword" 
                  placeholder="学员姓名/手机号/教练" 
                  clearable 
                  class="search-input" 
                  style="width: 220px;" 
                />
              </el-form-item>
              <el-form-item label="所属场馆" class="search-item">
                <el-select 
                  v-model="searchForm.venueId" 
                  placeholder="全部场馆" 
                  clearable 
                  :disabled="!isPresident"
                  @change="handleVenueChange"
                  class="search-select-small"
                  style="width: 160px;"
                >
                  <el-option v-for="v in venueList" :key="v.id" :label="v.venueName" :value="v.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="教练" class="search-item">
                <el-select 
                  v-model="searchForm.coachId" 
                  placeholder="全部教练" 
                  clearable 
                  class="search-select-small"
                  style="width: 160px;"
                >
                  <el-option v-for="c in filterCoachList" :key="c.id" :label="c.coachName" :value="c.id" />
                </el-select>
              </el-form-item>
            </div>
            <div class="search-buttons">
              <el-button type="primary" :icon="Search" class="search-btn bmp-uiv-btn bmp-uiv-btn-primary" @click="handleSearch">搜索</el-button>
              <el-button type="primary" :icon="Refresh" class="reset-btn bmp-uiv-btn bmp-uiv-btn-primary" @click="handleReset">重置</el-button>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 操作区 -->
      <div class="glass-card action-card">
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleBindClick">手动绑定学员</el-button>
      </div>

      <!-- 列表区 -->
      <div class="glass-card table-card">
        <div class="responsive-table-shell coach-table-shell">
          <el-table
            :data="relationList"
            v-loading="loading"
            border
            stripe
            :height="tableHeight"
            :header-cell-style="tableHeaderStyle"
            :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          >
            <el-table-column label="学员头像" width="90" align="center">
              <template #default="scope">
                <el-avatar :size="40" :src="resolveAvatarUrl(scope.row.memberAvatar)" />
              </template>
            </el-table-column>
            <el-table-column prop="memberName" label="学员姓名" min-width="120" />
            <el-table-column prop="maskedPhone" label="联系电话" min-width="130" />
            <el-table-column prop="coachName" label="教练" min-width="120" />
            <el-table-column prop="venueName" label="所属场馆" min-width="150" show-overflow-tooltip />
            <el-table-column prop="bindType" label="绑定类型" min-width="100">
              <template #default="scope">
                <el-tag :type="scope.row.bindType === 'MANUAL' ? 'success' : 'info'" size="small" effect="light">
                  {{ scope.row.bindType === 'MANUAL' ? '手动绑定' : '自动预约' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalBookings" label="累计预约" min-width="90" />
            <el-table-column prop="attendanceRate" label="出勤率" min-width="90">
              <template #default="scope">
                <span>{{ scope.row.attendanceRate != null ? scope.row.attendanceRate + '%' : '0.00%' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalPaidAmount" label="消费总额" min-width="110">
              <template #default="scope">
                <span class="price-text">¥{{ formatCurrency(scope.row.totalPaidAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="绑定时间" min-width="160">
              <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="180" fixed="right">
              <template #default="scope">
                <div class="operation-buttons">
                  <el-button type="success" size="small" :icon="View" plain @click="handleDetail(scope.row)">详情</el-button>
                  <el-button type="danger" size="small" :icon="Delete" plain @click="handleUnbind(scope.row)">解绑</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

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

    <!-- 手动绑定弹窗 -->
    <el-dialog
      v-model="bindDialogVisible"
      title="手动绑定学员"
      width="500px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="resetBindForm"
    >
      <el-form ref="bindFormRef" :model="bindForm" :rules="bindRules" label-position="top" class="modern-form">
        <el-form-item label="选择教练" prop="coachId" class="modern-form-item">
          <el-select
            v-model="bindForm.coachId"
            placeholder="请选择教练"
            filterable
            remote
            :remote-method="loadBindCoaches"
            :loading="bindCoachLoading"
            @visible-change="handleBindCoachVisible"
            style="width: 100%"
          >
            <el-option 
              v-for="c in bindCoachOptions" 
              :key="c.id" 
              :label="`${c.coachName} (${c.venueName || '未知场馆'})`" 
              :value="c.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择会员/学员" prop="memberId" class="modern-form-item">
          <el-select
            v-model="bindForm.memberId"
            placeholder="请选择学员"
            filterable
            remote
            :remote-method="loadBindMembers"
            :loading="bindMemberLoading"
            @visible-change="handleBindMemberVisible"
            style="width: 100%"
          >
            <el-option 
              v-for="m in bindMemberOptions" 
              :key="m.id" 
              :label="`${m.memberName} (${m.phone})`" 
              :value="m.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button class="modern-btn-cancel" @click="bindDialogVisible = false">取消</el-button>
          <el-button type="primary" class="modern-btn-submit" :loading="bindSubmitLoading" @click="handleBindSubmit">
            提交绑定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="学员关联详情"
      size="820px"
      destroy-on-close
    >
      <div v-if="currentRelation" class="drawer-header-info">
        <div class="info-block">
          <el-avatar :size="50" :src="resolveAvatarUrl(currentRelation.memberAvatar)" />
          <div class="text-block">
            <div class="name-line">
              <span class="member-name">{{ currentRelation.memberName }}</span>
              <el-tag size="small" :type="currentRelation.gender === 1 ? 'primary' : 'danger'">
                {{ currentRelation.gender === 1 ? '男' : '女' }}
              </el-tag>
            </div>
            <div class="sub-line">电话: {{ currentRelation.maskedPhone }} | 场馆: {{ currentRelation.venueName }}</div>
          </div>
        </div>
        <div class="relation-badge">
          <div>绑定教练：<strong style="color: var(--color-primary, #2563EB);">{{ currentRelation.coachName }}</strong></div>
          <div style="font-size: 12px; color: var(--color-text-secondary); margin-top: 4px;">
            首次上课: {{ formatDateTime(currentRelation.firstLessonTime) || '无记录' }}
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="detail-tabs" @tab-change="handleTabChange" style="margin-top: 15px;">
        <!-- 排课课表 Tab -->
        <el-tab-pane label="课程排课" name="schedule">
          <el-table :data="scheduleData" v-loading="detailLoading" stripe border size="small">
            <el-table-column prop="courseName" label="课程名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="courseDate" label="日期" width="100" />
            <el-table-column label="时间段" width="110">
              <template #default="scope">
                {{ scope.row.startTime }}-{{ scope.row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="bookingStatus" label="预约状态" width="100">
              <template #default="scope">
                <el-tag :type="getBookingStatusType(scope.row.bookingStatus)" size="small">
                  {{ getBookingStatusText(scope.row.bookingStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="attendanceStatus" label="考勤" width="90">
              <template #default="scope">
                <el-tag :type="getAttendanceStatusType(scope.row.attendanceStatus)" size="small">
                  {{ getAttendanceStatusText(scope.row.attendanceStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="drawer-pagination">
            <el-pagination
              v-model:current-page="detailPagination.page"
              v-model:page-size="detailPagination.size"
              :total="detailPagination.total"
              layout="total, prev, pager, next"
              size="small"
              @current-change="loadScheduleData"
            />
          </div>
        </el-tab-pane>

        <!-- 考勤历史 Tab -->
        <el-tab-pane label="考勤历史" name="attendance">
          <el-table :data="attendanceData" v-loading="detailLoading" stripe border size="small">
            <el-table-column prop="courseName" label="课程名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="courseDate" label="日期" width="100" />
            <el-table-column label="时间段" width="110">
              <template #default="scope">
                {{ scope.row.startTime }}-{{ scope.row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="durationMinutes" label="时长(分钟)" width="90" />
            <el-table-column prop="attendanceStatus" label="考勤" width="90">
              <template #default="scope">
                <el-tag :type="getAttendanceStatusType(scope.row.attendanceStatus)" size="small">
                  {{ getAttendanceStatusText(scope.row.attendanceStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="drawer-pagination">
            <el-pagination
              v-model:current-page="detailPagination.page"
              v-model:page-size="detailPagination.size"
              :total="detailPagination.total"
              layout="total, prev, pager, next"
              size="small"
              @current-change="loadAttendanceData"
            />
          </div>
        </el-tab-pane>

        <!-- 消费明细 Tab -->
        <el-tab-pane label="消费明细" name="consume">
          <el-table :data="consumeData" v-loading="detailLoading" stripe border size="small">
            <el-table-column prop="courseName" label="关联课程" min-width="150" show-overflow-tooltip />
            <el-table-column prop="amount" label="扣费金额" width="100">
              <template #default="scope">
                <span class="price-text">¥{{ formatCurrency(scope.row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式" width="120">
              <template #default="scope">
                <el-tag type="info" size="small">{{ getPaymentMethodText(scope.row.paymentMethod) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentStatus" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.paymentStatus === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.paymentStatus === 1 ? '已支付' : '未支付' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="扣费时间" width="160">
              <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <div class="drawer-pagination">
            <el-pagination
              v-model:current-page="detailPagination.page"
              v-model:page-size="detailPagination.size"
              :total="detailPagination.total"
              layout="total, prev, pager, next"
              size="small"
              @current-change="loadConsumeData"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View, Delete, User } from '@element-plus/icons-vue'
import { openActionConfirm } from '@/utils/confirm'
import { resolveAvatarUrl } from '@/utils/auth'
import {
  getCoachStudentList,
  bindCoachStudent,
  unbindCoachStudent,
  getStudentSchedule,
  getStudentAttendance,
  getStudentConsumeRecords
} from '@/api/adminCoachStudent'
import { getCoachList, getVenueList } from '@/api/coach'
import { getMemberList } from '@/api/member'
import {
  buildCoachOptionParams,
  buildMemberOptionParams,
  extractPagedRows
} from '@/utils/coachStudentOptions.mjs'

// 权限信息
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const userRole = userInfo.role === 'ADMIN' ? 'PRESIDENT' : (userInfo.role || 'USER')
const myVenueId = userInfo.venueId != null ? Number(userInfo.venueId) : null
const isPresident = computed(() => userRole === 'PRESIDENT')

// 页面基础数据
const loading = ref(false)
const relationList = ref([])
const venueList = ref([])
const allCoaches = ref([])
const bindCoachOptions = ref([])
const bindMemberOptions = ref([])
const bindCoachLoading = ref(false)
const bindMemberLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  venueId: !isPresident.value ? myVenueId : null,
  coachId: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表格高度自适应
const tableHeight = ref('550px')
const calculateTableHeight = () => {
  const windowHeight = window.innerHeight
  // 头部、搜索卡、操作卡及边距的估算高度，保留适合表格的高度
  tableHeight.value = `${windowHeight - 340}px`
}

// 表头样式保持一致
const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

// 级联教练下拉
const filterCoachList = computed(() => {
  if (!searchForm.venueId) {
    return allCoaches.value
  }
  return allCoaches.value.filter(c => Number(c.venueId) === Number(searchForm.venueId))
})

const handleVenueChange = () => {
  searchForm.coachId = null
}

// 加载场馆列表
const loadVenueList = async () => {
  try {
    const res = await getVenueList()
    if (res?.code === 200) {
      venueList.value = res.data || []
    }
  } catch (err) {
    console.error('加载场馆列表失败:', err)
  }
}

// 加载全部教练
const loadCoaches = async () => {
  try {
    const params = { page: 1, size: 1000 }
    if (!isPresident.value && myVenueId) {
      params.venueId = myVenueId
    }
    const res = await getCoachList(params)
    if (res?.code === 200) {
      allCoaches.value = extractPagedRows(res)
    }
  } catch (err) {
    console.error('加载教练列表失败:', err)
  }
}

// 加载关系列表
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      venueId: searchForm.venueId || undefined,
      coachId: searchForm.coachId || undefined
    }
    const res = await getCoachStudentList(params)
    if (res?.code === 200 && res.data) {
      relationList.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (err) {
    console.error('加载绑定关系列表失败:', err)
    ElMessage.error('加载绑定关系列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索和重置
const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.venueId = isPresident.value ? null : myVenueId
  searchForm.coachId = null
  pagination.page = 1
  loadList()
}

const handleStatClick = () => {
  handleReset()
}

// 手动绑定逻辑
const bindDialogVisible = ref(false)
const bindSubmitLoading = ref(false)
const bindFormRef = ref(null)
const bindForm = reactive({
  coachId: null,
  memberId: null
})
const bindRules = {
  coachId: [{ required: true, message: '请选择要绑定的教练', trigger: 'change' }],
  memberId: [{ required: true, message: '请选择要绑定的学员', trigger: 'change' }]
}

const loadBindMembers = async (keyword = '') => {
  bindMemberLoading.value = true
  try {
    const res = await getMemberList(buildMemberOptionParams(keyword))
    if (res?.code === 200) {
      bindMemberOptions.value = extractPagedRows(res)
    }
  } catch (err) {
    console.error('加载会员列表失败:', err)
  } finally {
    bindMemberLoading.value = false
  }
}

const loadBindCoaches = async (keyword = '') => {
  bindCoachLoading.value = true
  try {
    const res = await getCoachList(buildCoachOptionParams(
      keyword,
      isPresident.value ? null : myVenueId
    ))
    if (res?.code === 200) {
      bindCoachOptions.value = extractPagedRows(res)
    }
  } catch (err) {
    console.error('加载教练列表失败:', err)
  } finally {
    bindCoachLoading.value = false
  }
}

const handleBindCoachVisible = (visible) => {
  if (visible && bindCoachOptions.value.length === 0) loadBindCoaches()
}

const handleBindMemberVisible = (visible) => {
  if (visible && bindMemberOptions.value.length === 0) loadBindMembers()
}

const handleBindClick = () => {
  bindDialogVisible.value = true
  loadBindMembers()
  loadBindCoaches()
}

const resetBindForm = () => {
  bindForm.coachId = null
  bindForm.memberId = null
  bindFormRef.value?.resetFields()
}

const handleBindSubmit = async () => {
  if (!bindFormRef.value) return
  await bindFormRef.value.validate(async (valid) => {
    if (!valid) return
    bindSubmitLoading.value = true
    try {
      const res = await bindCoachStudent(bindForm.coachId, bindForm.memberId)
      if (res?.code === 200) {
        ElMessage.success('绑定关系创建成功')
        bindDialogVisible.value = false
        loadList()
      } else {
        ElMessage.error(res?.message || '绑定失败')
      }
    } catch (err) {
      console.error('创建绑定关系失败:', err)
      ElMessage.error('创建绑定关系失败')
    } finally {
      bindSubmitLoading.value = false
    }
  })
}

// 解绑逻辑
const handleUnbind = (row) => {
  openActionConfirm({
    title: '解绑确认',
    message: `确定要解除教练【${row.coachName}】与学员【${row.memberName}】的绑定关系吗？`,
    detail: '解绑后不会影响该学员在系统的会员身份，但该学员在排课和考勤详情中将不再关联此教练。',
    tone: 'danger',
    confirmButtonText: '确定解绑'
  }).then(async () => {
    try {
      const res = await unbindCoachStudent(row.id)
      if (res?.code === 200) {
        ElMessage.success('成功解除绑定关系')
        loadList()
      } else {
        ElMessage.error(res?.message || '解绑失败')
      }
    } catch (err) {
      console.error('解绑关系失败:', err)
      ElMessage.error('解绑操作失败')
    }
  }).catch(() => {})
}

// 详情抽屉逻辑
const detailDrawerVisible = ref(false)
const currentRelation = ref(null)
const activeTab = ref('schedule')
const detailLoading = ref(false)

const scheduleData = ref([])
const attendanceData = ref([])
const consumeData = ref([])

const detailPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const handleDetail = (row) => {
  currentRelation.value = row
  detailDrawerVisible.value = true
  activeTab.value = 'schedule'
  detailPagination.page = 1
  detailPagination.total = 0
  loadScheduleData()
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
  detailPagination.page = 1
  detailPagination.total = 0
  if (tabName === 'schedule') {
    loadScheduleData()
  } else if (tabName === 'attendance') {
    loadAttendanceData()
  } else if (tabName === 'consume') {
    loadConsumeData()
  }
}

// 排课数据加载
const loadScheduleData = async () => {
  if (!currentRelation.value) return
  detailLoading.value = true
  try {
    const params = {
      coachId: currentRelation.value.coachId,
      memberId: currentRelation.value.memberId,
      page: detailPagination.page,
      size: detailPagination.size
    }
    const res = await getStudentSchedule(params)
    if (res?.code === 200 && res.data) {
      scheduleData.value = res.data.list || []
      detailPagination.total = res.data.total || 0
    }
  } catch (err) {
    console.error('加载排课列表失败:', err)
  } finally {
    detailLoading.value = false
  }
}

// 考勤数据加载
const loadAttendanceData = async () => {
  if (!currentRelation.value) return
  detailLoading.value = true
  try {
    const params = {
      coachId: currentRelation.value.coachId,
      memberId: currentRelation.value.memberId,
      page: detailPagination.page,
      size: detailPagination.size
    }
    const res = await getStudentAttendance(params)
    if (res?.code === 200 && res.data) {
      attendanceData.value = res.data.list || []
      detailPagination.total = res.data.total || 0
    }
  } catch (err) {
    console.error('加载考勤历史失败:', err)
  } finally {
    detailLoading.value = false
  }
}

// 消费数据加载
const loadConsumeData = async () => {
  if (!currentRelation.value) return
  detailLoading.value = true
  try {
    const params = {
      coachId: currentRelation.value.coachId,
      memberId: currentRelation.value.memberId,
      page: detailPagination.page,
      size: detailPagination.size
    }
    const res = await getStudentConsumeRecords(params)
    if (res?.code === 200 && res.data) {
      consumeData.value = res.data.list || []
      detailPagination.total = res.data.total || 0
    }
  } catch (err) {
    console.error('加载消费明细失败:', err)
  } finally {
    detailLoading.value = false
  }
}

// 分页变更
const handlePageChange = (page) => {
  pagination.page = page
  loadList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadList()
}

// 格式化函数
const formatCurrency = (val) => {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

const formatDateTime = (val) => {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 19)
}

const getBookingStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return map[status] || '未知'
}

const getBookingStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: '' }
  return map[status] || 'info'
}

const getAttendanceStatusText = (status) => {
  const map = { 0: '未考勤', 1: '已签到', 2: '已出勤', 3: '缺勤' }
  return map[status] ?? '未考勤'
}

const getAttendanceStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] ?? 'info'
}

const getPaymentMethodText = (method) => {
  const map = { BALANCE: '余额支付' }
  return map[method] || method || '-'
}

onMounted(() => {
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
  loadVenueList()
  loadCoaches()
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
  transform: translateY(-6px) scale(1.01);
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
  width: 90px;
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

/* 抽屉样式 */
.drawer-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #f8fafc 0%, #eff6ff 100%);
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid var(--color-border, #e2e8f0);
}

.drawer-header-info .info-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drawer-header-info .text-block .name-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.drawer-header-info .text-block .member-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.drawer-header-info .text-block .sub-line {
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  margin-top: 4px;
}

.drawer-header-info .relation-badge {
  text-align: right;
  font-size: 14px;
}

.drawer-pagination {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

/* modern 样式适配，保持各处弹窗高度融合 */
.modern-form-item {
  margin-bottom: 20px;
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
