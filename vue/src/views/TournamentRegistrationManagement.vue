<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon"><el-icon :size="32"><Medal /></el-icon></div>
            <div class="header-text">
              <h2 class="page-title">赛事报名</h2>
              <p class="page-subtitle">管理赛事报名单、费用与搭档信息，支持状态流转</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总报名</span>
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
              <span class="stat-label">已参赛</span>
              <span class="stat-value">{{ statistics.participated || 0 }}</span>
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
              <el-form-item label="报名单号" class="search-item">
                <el-input v-model="searchForm.registrationNo" placeholder="TR 开头" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="赛事" class="search-item">
                <el-select v-model="searchForm.tournamentId" placeholder="全部赛事" clearable class="search-select">
                  <el-option v-for="item in tournamentOptions" :key="item.id" :label="item.tournamentName" :value="item.id" />
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
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">新增报名</el-button>
      </div>

      <div class="glass-card table-card">
        <el-table
          :data="registrationList"
          v-loading="loading"
          border
          stripe
          :height="tableHeight"
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
        >
          <el-table-column prop="registrationNo" label="报名单号" min-width="150" />
          <el-table-column prop="tournamentName" label="赛事" min-width="150" />
          <el-table-column prop="memberName" label="会员" min-width="120" />
          <el-table-column prop="partnerName" label="搭档" min-width="120" />
          <el-table-column label="费用" min-width="110">
            <template #default="scope">
              <span class="price-text">¥{{ formatCurrency(scope.row.entryFee) }}</span>
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
          <el-table-column label="操作" min-width="320" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button v-if="isAdmin" type="success" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <!-- 待支付显示支付，已支付显示退款（同一位置互斥） -->
                <el-button v-if="isAdmin && scope.row.status === 1" type="primary" size="small" plain @click="handlePayment(scope.row)">支付</el-button>
                <el-button v-else-if="isAdmin && scope.row.status === 2" type="warning" size="small" plain @click="handleRefund(scope.row)">退款</el-button>
                <el-button v-if="isAdmin" type="primary" size="small" plain @click="changeStatus(scope.row, 3)">标记参赛</el-button>
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
      width="700px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" class="modern-form">
        <div class="modern-form-section">
          <h4 class="modern-section-title">报名信息</h4>
          <el-form-item label="报名单号" class="modern-form-item">
            <el-input :model-value="form.registrationNo || '提交后自动生成'" disabled />
          </el-form-item>
          <el-form-item label="赛事" prop="tournamentId" class="modern-form-item">
            <el-select v-model="form.tournamentId" placeholder="选择赛事" filterable @change="onTournamentChange">
              <el-option v-for="item in tournamentOptions" :key="item.id" :label="item.tournamentName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="场馆" class="modern-form-item">
            <el-input :model-value="selectedVenueName" placeholder="选择赛事后自动显示" disabled />
          </el-form-item>
          <el-form-item label="会员" prop="memberId" class="modern-form-item">
            <el-input
              v-model="memberKeyword"
              placeholder="输入姓名/手机号搜索"
              @change="loadMembers"
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
          <el-form-item :label="form.tournamentId && isDoublesOrMixed(selectedTournamentType) ? '搭档（必填）' : '搭档（可选）'" class="modern-form-item">
            <el-input
              v-model="partnerKeyword"
              placeholder="输入姓名/手机号搜索"
              @change="loadPartnerMembers"
              clearable
            />
            <el-select
              v-model="form.partnerId"
              :placeholder="form.tournamentId && isDoublesOrMixed(selectedTournamentType) ? '双打/混双请选择搭档' : '选择搭档'"
              filterable
              style="width: 100%; margin-top: 8px"
              @change="handlePartnerChange"
            >
              <el-option v-for="item in partnerOptions" :key="item.id" :label="item.memberName" :value="item.id">
                <span>{{ item.memberName }}</span>
                <span style="margin-left: 8px; color: var(--color-text-secondary, #64748B)">{{ item.phone }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="费用" prop="fee" class="modern-form-item">
            <el-input-number
              :model-value="form.tournamentId ? selectedTournamentFee : form.fee"
              :min="0"
              :precision="2"
              :step="10"
              style="width: 100%"
              :disabled="!!form.tournamentId"
              placeholder="选择赛事后自动带出赛事报名费"
            />
            <div v-if="form.tournamentId" class="form-item-hint">与赛事管理一致；双打/混双为单人报名费×2（一对总价），由主报名人一次性支付，搭档不扣款；不可修改</div>
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
            {{ isEdit ? '更新报名' : '创建报名' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="余额支付"
      width="500px"
      :close-on-click-modal="false"
      class="modern-dialog"
    >
      <el-form :model="paymentForm" label-position="top" class="modern-form">
        <el-form-item label="报名单号">
          <el-input :model-value="paymentForm.registrationNo" disabled />
        </el-form-item>
        <el-form-item label="会员">
          <el-input :model-value="paymentForm.memberName" disabled />
        </el-form-item>
        <el-form-item label="费用">
          <el-input :model-value="'¥' + formatCurrency(paymentForm.fee)" disabled />
        </el-form-item>
        <el-form-item label="支付方式" required>
          <el-radio-group v-model="paymentForm.paymentMethod">
            <el-radio label="BALANCE">余额支付</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button class="modern-btn-cancel" @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" class="modern-btn-submit" :loading="paymentLoading" @click="handlePaymentSubmit">
            确认支付
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Medal } from '@element-plus/icons-vue'
import {
  getTournamentRegistrationList,
  getTournamentRegistrationInfo,
  addTournamentRegistration,
  updateTournamentRegistration,
  deleteTournamentRegistration,
  updateTournamentRegistrationStatus,
  getTournamentRegistrationStatistics,
  getTournamentRegistrationTournaments,
  getTournamentRegistrationMembers,
  processTournamentRegistrationPayment,
  processTournamentRegistrationRefund
} from '@/api/tournamentRegistration'

const searchForm = reactive({
  registrationNo: '',
  tournamentId: null,
  memberKeyword: '',
  status: null,
  timeRange: []
})

const registrationList = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statistics = reactive({ total: 0, pending: 0, paid: 0, participated: 0, cancelled: 0 })
const tournamentOptions = ref([])
const memberOptions = ref([])
const partnerOptions = ref([])
const memberKeyword = ref('')
const partnerKeyword = ref('')
const tableHeight = ref(600)
const { isAdmin, isUser } = useAuth()

// 当前选中赛事关联的场馆名称（只读展示）
const selectedVenueName = computed(() => {
  if (!form.tournamentId) return ''
  const t = tournamentOptions.value.find((item) => item.id === form.tournamentId)
  return t?.venueName ?? ''
})

// 双打/混双时报名费为赛事单人费×2
const isDoublesOrMixed = (type) => type === 'DOUBLE' || type === 'MIXED'

// 当前选中赛事的类型（用于双打/混双必填搭档等）
const selectedTournamentType = computed(() => {
  if (!form.tournamentId) return null
  const t = tournamentOptions.value.find((item) => item.id === form.tournamentId)
  return t?.tournamentType ?? null
})
// 当前选中赛事的报名费（与赛事管理一致；双打/混双自动×2，只读）
const selectedTournamentFee = computed(() => {
  if (!form.tournamentId) return null
  const t = tournamentOptions.value.find((item) => item.id === form.tournamentId)
  if (t == null || t.entryFee == null) return 0
  let num = Number(t.entryFee)
  if (Number.isNaN(num) || num < 0) num = 0
  if (isDoublesOrMixed(t.tournamentType)) num *= 2
  return Math.round(num * 100) / 100
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增报名')
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  registrationNo: '',
  tournamentId: null,
  memberId: null,
  partnerId: null,
  fee: 0,
  status: 1
})
const formRules = {
  tournamentId: [{ required: true, message: '请选择赛事', trigger: 'change' }],
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  fee: [{ required: true, message: '请输入费用', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const submitLoading = ref(false)

// 支付对话框相关
const paymentDialogVisible = ref(false)
const paymentForm = reactive({
  registrationId: null,
  registrationNo: '',
  memberName: '',
  fee: 0,
  paymentMethod: 'BALANCE'
})
const paymentLoading = ref(false)

const statusOptions = [
  { label: '已取消', value: 0 },
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '已参赛', value: 3 },
  { label: '已退赛', value: 4 }
]

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '已参赛', 4: '已退赛' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'warning' }
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

const loadTournaments = async () => {
  try {
    const res = await getTournamentRegistrationTournaments()
    if (res.code === 200) {
      tournamentOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载赛事下拉失败:', e)
  }
}

// 选择赛事时，费用与赛事管理一致（双打/混双×2）；清空赛事时恢复可编辑
const onTournamentChange = (tournamentId) => {
  if (!tournamentId) {
    form.fee = 0
    return
  }
  const t = tournamentOptions.value.find((item) => item.id === tournamentId)
  if (t != null) {
    let num = Number(t.entryFee)
    if (Number.isNaN(num) || num < 0) num = 0
    if (isDoublesOrMixed(t.tournamentType)) num *= 2
    form.fee = Math.round(num * 100) / 100
  }
}

// 会员搜索（与课程预约模块一致：@change 失焦后请求，keyword 仅按姓名筛选）
const loadMembers = async () => {
  if (!memberKeyword.value) {
    memberOptions.value = []
    return
  }
  try {
    const res = await getTournamentRegistrationMembers(memberKeyword.value)
    if (res.code === 200) {
      memberOptions.value = res.data || []
    }
  } catch (e) {
    console.error('搜索会员失败:', e)
  }
}

// 选择会员后同步关键字为当前会员姓名，避免输入框与下拉显示不一致
const handleMemberChange = (memberId) => {
  const target = memberOptions.value.find((m) => m.id === memberId)
  if (target) {
    memberKeyword.value = target.memberName
  }
}

// 搭档搜索
const loadPartnerMembers = async () => {
  if (!partnerKeyword.value) {
    partnerOptions.value = []
    return
  }
  try {
    const res = await getTournamentRegistrationMembers(partnerKeyword.value)
    if (res.code === 200) {
      partnerOptions.value = res.data || []
    }
  } catch (e) {
    console.error('搜索搭档失败:', e)
  }
}

// 选择搭档后同步关键字
const handlePartnerChange = (partnerId) => {
  const target = partnerOptions.value.find((m) => m.id === partnerId)
  if (target) {
    partnerKeyword.value = target.memberName
  }
}

const loadStatistics = async () => {
  try {
    const res = await getTournamentRegistrationStatistics()
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
      registrationNo: searchForm.registrationNo || null,
      tournamentId: searchForm.tournamentId,
      memberKeyword: searchForm.memberKeyword || null,
      status: searchForm.status,
      startTime: searchForm.timeRange?.[0] || null,
      endTime: searchForm.timeRange?.[1] || null,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getTournamentRegistrationList(params)
    if (res.code === 200) {
      registrationList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取报名列表失败')
      registrationList.value = []
    }
  } catch (e) {
    console.error('加载报名列表失败:', e)
    ElMessage.error('加载报名列表失败，请稍后重试')
    registrationList.value = []
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
    registrationNo: '',
    tournamentId: null,
    memberKeyword: '',
    status: null,
    timeRange: []
  })
  pagination.page = 1
  loadList()
}

const handleAdd = () => {
  dialogTitle.value = '新增报名'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑报名'
  isEdit.value = true
  try {
    const res = await getTournamentRegistrationInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      Object.assign(form, {
        id: data.id,
        registrationNo: data.registrationNo,
        tournamentId: data.tournamentId,
        memberId: data.memberId,
        partnerId: data.partnerId,
        fee: data.entryFee,
        status: data.status
      })
      // 编辑时回显会员、搭档下拉，避免只显示 id 或空白
      if (data.memberId && data.memberName) {
        memberOptions.value = [{ id: data.memberId, memberName: data.memberName, phone: data.memberPhone || '' }]
        memberKeyword.value = data.memberName
      } else {
        memberOptions.value = []
        memberKeyword.value = ''
      }
      if (data.partnerId && data.partnerName) {
        partnerOptions.value = [{ id: data.partnerId, memberName: data.partnerName, phone: data.partnerPhone || '' }]
        partnerKeyword.value = data.partnerName
      } else {
        partnerOptions.value = []
        partnerKeyword.value = ''
      }
    }
  } catch (e) {
    console.error('加载报名详情失败:', e)
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除报名 ${row.registrationNo} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteTournamentRegistration(row.id)
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
    if (form.tournamentId && isDoublesOrMixed(selectedTournamentType.value) && !form.partnerId) {
      ElMessage.warning('双打/混双赛事必须选择搭档')
      return
    }
    submitLoading.value = true
    try {
      const payload = {
        id: form.id,
        tournamentId: form.tournamentId,
        memberId: form.memberId,
        partnerId: form.partnerId,
        entryFee: form.tournamentId ? selectedTournamentFee.value : form.fee,
        status: form.status
      }
      const res = isEdit.value ? await updateTournamentRegistration(payload) : await addTournamentRegistration(payload)
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
      const msg = e.response?.data?.message || e.message || '提交失败，请稍后重试'
      ElMessage.error(msg)
    } finally {
      submitLoading.value = false
    }
  })
}

const changeStatus = async (row, status) => {
  try {
    const res = await updateTournamentRegistrationStatus(row.id, status)
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

const handlePayment = (row) => {
  Object.assign(paymentForm, {
    registrationId: row.id,
    registrationNo: row.registrationNo,
    memberName: row.memberName,
    fee: row.entryFee,
    paymentMethod: 'BALANCE'
  })
  paymentDialogVisible.value = true
}

const handlePaymentSubmit = async () => {
  if (!paymentForm.paymentMethod) {
    ElMessage.warning('请确认余额支付')
    return
  }
  paymentLoading.value = true
  try {
    const res = await processTournamentRegistrationPayment(paymentForm.registrationId, paymentForm.paymentMethod)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      paymentDialogVisible.value = false
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (e) {
    console.error('支付失败:', e)
    const errorMessage = e.response?.data?.message || e.message || '支付失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    paymentLoading.value = false
  }
}

const handleRefund = (row) => {
  ElMessageBox.confirm(`确定对报名 ${row.registrationNo} 进行退款吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await processTournamentRegistrationRefund(row.id)
        if (res.code === 200) {
          ElMessage.success('退款成功')
          loadList()
          loadStatistics()
        } else {
          ElMessage.error(res.message || '退款失败')
        }
      } catch (e) {
        console.error('退款失败:', e)
        const errorMessage = e.response?.data?.message || e.message || '退款失败，请稍后重试'
        ElMessage.error(errorMessage)
      }
    })
    .catch(() => {})
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    registrationNo: '',
    tournamentId: null,
    memberId: null,
    partnerId: null,
    fee: 0,
    status: 1
  })
  memberKeyword.value = ''
  partnerKeyword.value = ''
  memberOptions.value = []
  partnerOptions.value = []
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
  loadTournaments()
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

.form-item-hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
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
</style>
