<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon"><el-icon :size="32"><Notebook /></el-icon></div>
            <div class="header-text">
              <h2 class="page-title">课程管理</h2>
              <p class="page-subtitle">创建与维护课程信息、价格与名额，为预约提供基础数据</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总课程</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-primary" @click="handleStatClick(2)">
              <span class="stat-label">进行中</span>
              <span class="stat-value">{{ statistics.running || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(1)">
              <span class="stat-label">报名中</span>
              <span class="stat-value">{{ statistics.open || 0 }}</span>
            </div>
            <div class="stat-item stat-item-info" @click="handleStatClick(3)">
              <span class="stat-label">已结束</span>
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
              <el-form-item label="课程名称" class="search-item">
                <el-input v-model="searchForm.name" placeholder="课程名称" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="教练" class="search-item">
                <el-select v-model="searchForm.coachId" placeholder="全部教练" clearable class="search-select">
                  <el-option v-for="item in coachOptions" :key="item.id" :label="item.coachName" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="场地" class="search-item">
                <el-select v-model="searchForm.courtId" placeholder="全部场地" clearable class="search-select">
                  <el-option v-for="item in courtOptions" :key="item.id" :label="item.courtName || item.courtCode" :value="item.id" />
                </el-select>
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
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">新增课程</el-button>
      </div>

      <div class="glass-card table-card">
        <el-table
          :data="courseList"
          v-loading="loading"
          border
          stripe
          :height="tableHeight"
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
        >
          <el-table-column prop="courseName" label="课程名称" min-width="150" />
          <el-table-column prop="coachName" label="教练" min-width="120" />
          <el-table-column prop="courtName" label="场地" min-width="120" />
          <el-table-column label="时间" min-width="180">
            <template #default="scope">
              <span>{{ scope.row.startTime }} ~ {{ scope.row.endTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="价格" min-width="110">
            <template #default="scope">
              <span class="price-text">¥{{ formatCurrency(scope.row.coursePrice) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="人数" min-width="130">
            <template #default="scope">
              <el-tag type="success" size="small">当前 {{ scope.row.currentStudents ?? 0 }}</el-tag>
              <el-tag type="info" size="small" style="margin-left: 6px">上限 {{ scope.row.maxStudents ?? 0 }}</el-tag>
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
          <el-table-column label="操作" min-width="280" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button type="success" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <el-button v-if="scope.row.status === 1" type="primary" size="small" plain @click="changeStatus(scope.row, 2)">开始上课</el-button>
                <el-button v-if="scope.row.status === 2" type="primary" size="small" plain @click="changeStatus(scope.row, 3)">结束</el-button>
                <el-button v-if="scope.row.status === 1 || scope.row.status === 2" type="warning" size="small" plain @click="changeStatus(scope.row, 0)">取消</el-button>
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
          <h4 class="modern-section-title">课程信息</h4>
          <el-form-item label="课程名称" prop="courseName" class="modern-form-item">
            <el-input v-model="form.courseName" placeholder="请输入课程名称" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="教练" prop="coachId" class="modern-form-item">
            <el-select v-model="form.coachId" placeholder="选择教练" filterable>
              <el-option v-for="item in coachOptions" :key="item.id" :label="item.coachName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="场地" prop="courtId" class="modern-form-item">
            <el-select v-model="form.courtId" placeholder="选择场地" filterable>
              <el-option v-for="item in courtOptions" :key="item.id" :label="item.courtName || item.courtCode" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="课程时间" prop="timeRange" class="modern-form-item">
            <el-date-picker
              v-model="form.timeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始"
              end-placeholder="结束"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="价格" prop="price" class="modern-form-item">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="最大人数" prop="maxStudents" class="modern-form-item">
            <el-input-number v-model="form.maxStudents" :min="1" :step="1" style="width: 100%" />
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
            {{ isEdit ? '更新课程' : '创建课程' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Notebook } from '@element-plus/icons-vue'
import {
  getCourseList,
  getCourseInfo,
  addCourse,
  updateCourse,
  deleteCourse,
  updateCourseStatus,
  getCourseStatistics,
  getCourseCoaches,
  getCourseCourts
} from '@/api/course'

const searchForm = reactive({
  name: '',
  coachId: null,
  courtId: null,
  status: null,
  timeRange: []
})

const courseList = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statistics = reactive({ total: 0, open: 0, running: 0, finished: 0, canceled: 0 })
const coachOptions = ref([])
const courtOptions = ref([])
const tableHeight = ref(600)

const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  courseName: '',
  coachId: null,
  courtId: null,
  price: 0,
  maxStudents: 10,
  timeRange: [],
  status: 1
})
const formRules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  courtId: [{ required: true, message: '请选择场地', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  maxStudents: [{ required: true, message: '请输入最大人数', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择时间', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const submitLoading = ref(false)

const statusOptions = [
  { label: '已取消', value: 0 },
  { label: '报名中', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已结束', value: 3 }
]

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '报名中', 2: '进行中', 3: '已结束' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'primary', 3: 'warning' }
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

const loadCoaches = async () => {
  try {
    const res = await getCourseCoaches()
    if (res.code === 200) {
      coachOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载教练下拉失败:', e)
  }
}

const loadCourts = async () => {
  try {
    const res = await getCourseCourts()
    if (res.code === 200) {
      courtOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载场地下拉失败:', e)
  }
}

const loadStatistics = async () => {
  try {
    const res = await getCourseStatistics()
    if (res.code === 200) {
      const data = res.data || {}
      // 后端返回 enrolling/ongoing/cancelled，前端展示用 open/running/canceled
      statistics.total = data.total ?? 0
      statistics.open = data.enrolling ?? 0
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
      keyword: searchForm.name || undefined,
      coachId: searchForm.coachId || undefined,
      courtId: searchForm.courtId || undefined,
      status: searchForm.status ?? undefined,
      startTime: searchForm.timeRange?.[0] || undefined,
      endTime: searchForm.timeRange?.[1] || undefined,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getCourseList(params)
    if (res.code === 200) {
      courseList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取课程列表失败')
      courseList.value = []
    }
  } catch (e) {
    console.error('加载课程失败:', e)
    ElMessage.error('加载课程失败，请稍后重试')
    courseList.value = []
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
  Object.assign(searchForm, { name: '', coachId: null, courtId: null, status: null, timeRange: [] })
  pagination.page = 1
  loadList()
}

const handleAdd = () => {
  dialogTitle.value = '新增课程'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑课程'
  isEdit.value = true
  try {
    const res = await getCourseInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      // 后端返回 coursePrice、courseDate、startTime、endTime
      const startDt = data.courseDate && data.startTime ? `${data.courseDate}T${data.startTime}` : data.startTime
      const endDt = data.courseDate && data.endTime ? `${data.courseDate}T${data.endTime}` : data.endTime
      Object.assign(form, {
        id: data.id,
        courseName: data.courseName,
        coachId: data.coachId,
        courtId: data.courtId,
        price: data.coursePrice ?? 0,
        maxStudents: data.maxStudents,
        timeRange: [startDt, endDt].filter(Boolean),
        status: data.status
      })
    }
  } catch (e) {
    console.error('加载课程详情失败:', e)
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除课程 ${row.courseName} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteCourse(row.id)
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
      const startStr = form.timeRange?.[0]
      const endStr = form.timeRange?.[1]
      const courseDate = startStr ? startStr.slice(0, 10) : null
      const startTime = startStr ? startStr.slice(11, 19) : null
      const endTime = endStr ? endStr.slice(11, 19) : null
      let courseDuration = null
      if (startStr && endStr) {
        const startMs = new Date(startStr).getTime()
        const endMs = new Date(endStr).getTime()
        courseDuration = Math.round((endMs - startMs) / 60000)
      }
      const payload = {
        id: form.id,
        courseName: form.courseName,
        coachId: form.coachId,
        courtId: form.courtId,
        coursePrice: form.price,
        courseDuration,
        maxStudents: form.maxStudents,
        courseDate,
        startTime,
        endTime,
        status: form.status
      }
      const res = isEdit.value ? await updateCourse(payload) : await addCourse(payload)
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
    const res = await updateCourseStatus(row.id, status)
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
    courseName: '',
    coachId: null,
    courtId: null,
    price: 0,
    maxStudents: 10,
    timeRange: [],
    status: 1
  })
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
  loadCoaches()
  loadCourts()
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
