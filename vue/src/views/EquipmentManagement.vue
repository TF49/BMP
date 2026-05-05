<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon"><el-icon :size="32"><Box /></el-icon></div>
            <div class="header-text">
              <h2 class="page-title">器材管理</h2>
              <p class="page-subtitle">管理器材信息、库存与状态，支撑租借业务</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总数量</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(1)">
              <span class="stat-label">可用</span>
              <span class="stat-value">{{ statistics.normal || 0 }}</span>
            </div>
            <div class="stat-item stat-item-warning" @click="handleStatClick(2)">
              <span class="stat-label">维护</span>
              <span class="stat-value">{{ statistics.maintenance || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">停用</span>
              <span class="stat-value">{{ statistics.disabled || 0 }}</span>
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
              <el-form-item label="关键词" class="search-item">
                <el-input v-model="searchForm.keyword" placeholder="编号/名称" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="类型" class="search-item">
                <el-select v-model="searchForm.type" placeholder="全部类型" clearable class="search-select">
                  <el-option v-for="item in typeOptions" :key="item" :label="getEquipmentTypeLabel(item)" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="search-select-small">
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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

      <div class="glass-card action-card">
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">新增器材</el-button>
      </div>

      <div class="glass-card table-card">
        <el-table
          :data="equipmentList"
          v-loading="loading"
          border
          stripe
          :height="tableHeight"
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
        >
          <el-table-column prop="equipmentCode" label="编号" min-width="120" />
          <el-table-column prop="equipmentName" label="名称" min-width="140" />
          <el-table-column prop="equipmentType" label="类型" min-width="120">
            <template #default="scope">
              {{ getEquipmentTypeLabel(scope.row.equipmentType) }}
            </template>
          </el-table-column>
          <el-table-column label="库存" min-width="150">
            <template #default="scope">
              <el-tag type="success" size="small">可用 {{ scope.row.availableQuantity ?? 0 }}</el-tag>
              <el-tag type="info" size="small" style="margin-left: 6px">总数 {{ scope.row.totalQuantity ?? 0 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="租价/押金" min-width="180">
            <template #default="scope">
              <span>¥{{ formatCurrency(scope.row.rentalPrice) }}</span>
              <el-tag type="info" size="small" style="margin-left: 6px">
                押金 ¥{{ formatCurrency(scope.row.rentalDeposit) }}
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
          <el-table-column prop="rentedQuantity" label="租借中" min-width="100">
            <template #default="scope">
              {{ getRentedQuantity(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160">
            <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" min-width="220" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button type="success" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="warning" size="small" plain @click="changeStatus(scope.row, 2)">维护</el-button>
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
      width="640px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" class="modern-form">
        <div class="modern-form-section">
          <h4 class="modern-section-title">器材信息</h4>
          <el-form-item label="所属场馆" prop="venueId" class="modern-form-item">
            <el-select v-model="form.venueId" placeholder="选择场馆" filterable style="width: 100%" :disabled="isEdit">
              <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="器材编号" prop="equipmentCode" class="modern-form-item">
            <el-input v-model="form.equipmentCode" placeholder="唯一编号" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="器材名称" prop="equipmentName" class="modern-form-item">
            <el-input v-model="form.equipmentName" placeholder="输入名称" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="器材类型" prop="equipmentType" class="modern-form-item">
            <el-select v-model="form.equipmentType" placeholder="选择类型" filterable>
              <el-option v-for="item in typeOptions" :key="item" :label="getEquipmentTypeLabel(item)" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="采购价格（元）" prop="price" class="modern-form-item">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="租借单价（元/小时）" prop="rentalPrice" class="modern-form-item">
            <el-input-number v-model="form.rentalPrice" :min="0" :precision="2" :step="5" style="width: 100%" />
          </el-form-item>
          <el-form-item label="押金（元）" prop="rentalDeposit" class="modern-form-item">
            <el-input-number v-model="form.rentalDeposit" :min="0" :precision="2" :step="50" style="width: 100%" />
          </el-form-item>
          <el-form-item label="总数量" prop="totalQuantity" class="modern-form-item">
            <el-input-number v-model="form.totalQuantity" :min="0" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="可用数量" prop="availableQuantity" class="modern-form-item">
            <el-input-number v-model="form.availableQuantity" :min="0" :step="1" style="width: 100%" />
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
            {{ isEdit ? '更新器材' : '创建器材' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Box } from '@element-plus/icons-vue'
import {
  getEquipmentList,
  getEquipmentInfo,
  addEquipment,
  updateEquipment,
  deleteEquipment,
  updateEquipmentStatus,
  getEquipmentStatistics,
  getEquipmentTypes,
  getEquipmentVenues
} from '@/api/equipment'

const searchForm = reactive({
  keyword: '',
  type: null,
  status: null
})

const equipmentList = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statistics = reactive({ total: 0, normal: 0, maintenance: 0, disabled: 0 })
const typeOptions = ref([])
const venueOptions = ref([])
const tableHeight = ref(600)

const dialogVisible = ref(false)
const dialogTitle = ref('新增器材')
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  equipmentCode: '',
  equipmentName: '',
  equipmentType: '',
  venueId: null,
  price: 0,
  rentalPrice: 0,
  rentalDeposit: 0,
  totalQuantity: 0,
  availableQuantity: 0,
  status: 1
})
const formRules = {
  equipmentCode: [{ required: true, message: '请输入器材编号', trigger: 'blur' }],
  equipmentName: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  equipmentType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  venueId: [{ required: true, message: '请选择所属场馆', trigger: 'change' }],
  price: [{ required: true, message: '请输入采购价格', trigger: 'blur' }],
  rentalPrice: [{ required: false, message: '请输入租借单价', trigger: 'blur' }],
  rentalDeposit: [{ required: false, message: '请输入押金', trigger: 'blur' }],
  totalQuantity: [{ required: true, message: '请输入总数量', trigger: 'blur' }],
  availableQuantity: [{ required: true, message: '请输入可用数量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const submitLoading = ref(false)

const statusOptions = [
  { label: '停用', value: 0 },
  { label: '正常', value: 1 },
  { label: '维护中', value: 2 }
]

const equipmentTypeLabelMap = {
  RACKET: '球拍',
  SHUTTLE: '羽毛球',
  STRING: '球线'
}

const getEquipmentTypeLabel = (type) => {
  if (!type) return '-'
  return equipmentTypeLabelMap[type] || type
}

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

const getStatusText = (status) => {
  const map = { 0: '停用', 1: '正常', 2: '维护中' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'danger', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0.00'
  const num = Number(value) || 0
  return num.toFixed(2)
}

const getRentedQuantity = (row) => {
  if (!row) return 0
  const total = row.totalQuantity ?? 0
  const available = row.availableQuantity ?? 0
  const rented = total - available
  return rented > 0 ? rented : 0
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

const loadTypes = async () => {
  try {
    const res = await getEquipmentTypes()
    if (res.code === 200) {
      typeOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载类型失败:', e)
  }
}

const loadVenues = async () => {
  try {
    const res = await getEquipmentVenues()
    if (res.code === 200) {
      venueOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载场馆失败:', e)
  }
}

const loadStatistics = async () => {
  try {
    const res = await getEquipmentStatistics()
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
      keyword: searchForm.keyword || null,
      equipmentType: searchForm.type,
      status: searchForm.status,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getEquipmentList(params)
    if (res.code === 200) {
      equipmentList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取器材列表失败')
      equipmentList.value = []
    }
  } catch (e) {
    console.error('加载器材失败:', e)
    ElMessage.error('加载器材失败，请稍后重试')
    equipmentList.value = []
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
  Object.assign(searchForm, { keyword: '', type: null, status: null })
  pagination.page = 1
  loadList()
}

const handleAdd = () => {
  dialogTitle.value = '新增器材'
  isEdit.value = false
  resetForm()
  loadVenues()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑器材'
  isEdit.value = true
  try {
    const res = await getEquipmentInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      Object.assign(form, {
        id: data.id,
        equipmentCode: data.equipmentCode,
        equipmentName: data.equipmentName,
        equipmentType: data.equipmentType,
        venueId: data.venueId,
        price: data.price ?? 0,
        rentalPrice: data.rentalPrice ?? 0,
        rentalDeposit: data.rentalDeposit ?? 0,
        totalQuantity: data.totalQuantity,
        availableQuantity: data.availableQuantity,
        status: data.status
      })
    }
  } catch (e) {
    console.error('加载详情失败:', e)
  }
  loadVenues()
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除器材 ${row.equipmentName || row.equipmentCode} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteEquipment(row.id)
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
      const normalizedPrice = Number(form.price ?? 0)
      const normalizedRentalPrice = Number(form.rentalPrice ?? 0)
      const normalizedRentalDeposit = Number(form.rentalDeposit ?? 0)
      const normalizedTotalQuantity = Number(form.totalQuantity ?? 0)
      const normalizedAvailableQuantity = Number(form.availableQuantity ?? 0)

      if (Number.isNaN(normalizedPrice) || normalizedPrice < 0) {
        ElMessage.error('采购价格不能为空，且必须为非负数')
        return
      }

      const payload = {
        id: form.id,
        equipmentCode: form.equipmentCode,
        equipmentName: form.equipmentName,
        equipmentType: form.equipmentType,
        venueId: form.venueId,
        price: normalizedPrice,
        rentalPrice: Number.isNaN(normalizedRentalPrice) ? 0 : normalizedRentalPrice,
        rentalDeposit: Number.isNaN(normalizedRentalDeposit) ? 0 : normalizedRentalDeposit,
        totalQuantity: Number.isNaN(normalizedTotalQuantity) ? 0 : normalizedTotalQuantity,
        availableQuantity: Number.isNaN(normalizedAvailableQuantity) ? 0 : normalizedAvailableQuantity,
        status: form.status
      }
      const res = isEdit.value ? await updateEquipment(payload) : await addEquipment(payload)
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
      // 业务错误时 request 拦截器已用 res.message 提示过，仅无具体信息时再提示
      const msg = e?.message
      if (!msg || msg === 'Error') {
        ElMessage.error('提交失败，请稍后重试')
      }
    } finally {
      submitLoading.value = false
    }
  })
}

const changeStatus = async (row, status) => {
  try {
    const res = await updateEquipmentStatus(row.id, status)
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
    equipmentCode: '',
    equipmentName: '',
    equipmentType: '',
    venueId: null,
    price: 0,
    rentalPrice: 0,
    rentalDeposit: 0,
    totalQuantity: 0,
    availableQuantity: 0,
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
  loadTypes()
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
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
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
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 12px;
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
