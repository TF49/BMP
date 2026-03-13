<template>
  <div class="user-management">
    <div class="user-management-background">
      <div class="gradient-overlay"></div>
    </div>

    <div class="user-management-content">
      <!-- 页面标题 -->
      <div class="page-header glass-card">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32"><UserFilled /></el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">用户管理</h2>
              <p class="page-subtitle">管理系统用户信息，支持搜索、添加、编辑和删除操作</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item">
              <span class="stat-label">总用户数</span>
              <span class="stat-value">{{ pagination.total }}</span>
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
              <el-form-item label="用户名" class="search-item">
                <el-input
                  v-model="searchForm.username"
                  placeholder="请输入用户名"
                  clearable
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="身份证号" class="search-item">
                <el-input
                  v-model="searchForm.idCard"
                  placeholder="请输入身份证号"
                  clearable
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="角色" class="search-item">
                <el-select
                  v-model="searchForm.role"
                  placeholder="请选择角色"
                  clearable
                  class="search-select"
                >
                  <el-option label="全部" :value="null" />
                  <el-option label="协会会长" value="PRESIDENT" />
                  <el-option label="场馆管理者" value="VENUE_MANAGER" />
                  <el-option label="教练" value="COACH" />
                  <el-option label="普通用户" value="USER" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select
                  v-model="searchForm.status"
                  placeholder="请选择状态"
                  clearable
                  class="search-select"
                >
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
            </div>
            <div class="search-buttons">
              <el-button type="primary" @click="handleSearch" :icon="Search" class="search-btn bmp-uiv-btn bmp-uiv-btn-primary">
                搜索
              </el-button>
              <el-button
                type="primary"
                @click="handleReset"
                :icon="Refresh"
                class="reset-btn bmp-uiv-btn bmp-uiv-btn-primary"
              >
                重置
              </el-button>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="glass-card action-card">
        <el-button type="primary" @click="handleAdd" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary">
          添加用户
        </el-button>
      </div>

      <!-- 用户列表表格 -->
      <div class="glass-card table-card">
        <el-table
          :data="userList"
          v-loading="loading"
          element-loading-text="加载中..."
          style="width: 100%"
          stripe
          border
          highlight-current-row
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
        >
          <template #empty>
            <div class="bmp-uiv-card-empty bmp-uiv-empty-inline">
              <p class="bmp-uiv-empty-title">暂无数据</p>
              <p class="bmp-uiv-empty-desc">尝试调整搜索条件或添加新用户</p>
            </div>
          </template>
          <el-table-column
            label="序号"
            type="index"
            min-width="80"
            align="center"
            :index="(index) => (pagination.page - 1) * pagination.size + index + 1"
          />
          <el-table-column prop="username" label="用户名" min-width="120" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.username" size="small">{{ scope.row.username }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="130" align="center" />
          <el-table-column prop="idCard" label="身份证号" min-width="220" align="center" show-overflow-tooltip>
            <template #default="scope">
              <span class="id-card-text">{{ scope.row.idCard || '无' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" min-width="120" align="center">
            <template #default="scope">
              <el-tag 
                :type="getRoleTagType(scope.row.role)" 
                size="small"
                :effect="scope.row.role === 'PRESIDENT' ? 'dark' : 'plain'"
              >
                {{ getRoleText(scope.row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="venueId" label="所属场馆" min-width="150" align="center">
            <template #default="scope">
              <span v-if="scope.row.role === 'VENUE_MANAGER'">
                {{ getVenueName(scope.row.venueId) }}
              </span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                @change="handleStatusChange(scope.row)"
                :active-color="'var(--color-success, #13ce66)'"
                :inactive-color="'var(--color-danger, #ff4d4f)'"
                size="small"
                :active-value="1"
                :inactive-value="0"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="280" fixed="right" align="center">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleView(scope.row)"
                  :icon="View"
                  plain
                >
                  查看
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  @click="handleEdit(scope.row)"
                  :icon="Edit"
                  plain
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDelete(scope.row)"
                  :icon="Delete"
                  plain
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="pagination"
          />
        </div>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :close-on-click-modal="false"
      class="user-dialog modern-dialog"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="auto"
        label-position="top"
        class="user-form modern-form"
      >
        <!-- 用户名字段 -->
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">基本信息</h4>
          <el-form-item label="用户名" prop="username" class="form-item-enhanced modern-form-item">
            <el-input
              v-model="userForm.username"
              placeholder="请输入用户名（3-20个字符）"
              :disabled="isEdit"
              clearable
              maxlength="20"
              show-word-limit
              class="form-input modern-form-input"
            >
              <template #prefix>
                <el-icon class="input-icon modern-input-icon"><User /></el-icon>
              </template>
            </el-input>
            <span class="field-hint modern-field-hint">{{ isEdit ? '编辑模式下用户名不可修改' : '用户名用于登录系统' }}</span>
          </el-form-item>

          <!-- 密码字段 -->
          <el-form-item label="密码" prop="password" v-if="!isEdit" class="form-item-enhanced modern-form-item">
            <el-input
              v-model="userForm.password"
              type="password"
              placeholder="请输入密码（6-20个字符）"
              show-password
              clearable
              maxlength="20"
              show-word-limit
              class="form-input modern-form-input"
            >
              <template #prefix>
                <el-icon class="input-icon modern-input-icon"><Lock /></el-icon>
              </template>
            </el-input>
            <span class="field-hint modern-field-hint">密码需要至少6个字符，包含大小写字母和数字</span>
          </el-form-item>

          <!-- 手机号字段 -->
          <el-form-item label="手机号" prop="phone" class="form-item-enhanced modern-form-item">
            <el-input
              v-model="userForm.phone"
              placeholder="请输入手机号（11位）"
              clearable
              maxlength="11"
              class="form-input modern-form-input"
            >
              <template #prefix>
                <el-icon class="input-icon modern-input-icon"><Phone /></el-icon>
              </template>
            </el-input>
            <span class="field-hint modern-field-hint">用于接收系统通知和验证</span>
          </el-form-item>

          <!-- 身份证号字段 -->
          <el-form-item label="身份证号" prop="idCard" class="form-item-enhanced modern-form-item">
            <el-input
              v-model="userForm.idCard"
              placeholder="请输入身份证号（18位）"
              clearable
              maxlength="18"
              class="form-input modern-form-input"
            >
              <template #prefix>
                <el-icon class="input-icon modern-input-icon"><DocumentCopy /></el-icon>
              </template>
            </el-input>
            <span class="field-hint modern-field-hint">用于身份验证和实名认证</span>
          </el-form-item>
        </div>

        <!-- 权限和状态 -->
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">权限与状态</h4>

          <!-- 角色字段 -->
          <el-form-item label="用户角色" prop="role" class="form-item-enhanced modern-form-item">
            <div class="role-selector modern-role-selector">
              <el-radio-group v-model="userForm.role" class="role-group modern-role-group" @change="handleRoleChange">
                <el-radio label="PRESIDENT" class="role-item modern-role-item" :disabled="presidentExists && userForm.role !== 'PRESIDENT'">
                  <div class="role-content">
                    <div class="role-icon-wrapper president-icon">
                      <el-icon class="role-icon"><Trophy /></el-icon>
                    </div>
                    <div class="role-info">
                      <h3 class="role-name">协会会长</h3>
                      <p class="role-desc">最高权限，管理所有模块</p>
                      <p v-if="presidentExists && userForm.role !== 'PRESIDENT'" class="role-warning" style="color: #f56c6c; font-size: 12px; margin-top: 4px;">
                        ⚠️ 系统中已存在协会会长
                      </p>
                    </div>
                    <div class="role-check">
                      <el-icon class="check-icon"><CircleCheckFilled /></el-icon>
                    </div>
                  </div>
                </el-radio>
                <el-radio label="VENUE_MANAGER" class="role-item modern-role-item">
                  <div class="role-content">
                    <div class="role-icon-wrapper manager-icon">
                      <el-icon class="role-icon"><Management /></el-icon>
                    </div>
                    <div class="role-info">
                      <h3 class="role-name">场馆管理者</h3>
                      <p class="role-desc">管理自己场馆的业务</p>
                    </div>
                    <div class="role-check">
                      <el-icon class="check-icon"><CircleCheckFilled /></el-icon>
                    </div>
                  </div>
                </el-radio>
                <el-radio label="COACH" class="role-item modern-role-item">
                  <div class="role-content">
                    <div class="role-icon-wrapper coach-icon">
                      <el-icon class="role-icon"><Avatar /></el-icon>
                    </div>
                    <div class="role-info">
                      <h3 class="role-name">教练</h3>
                      <p class="role-desc">登录教练端，查看我的课程与预约</p>
                    </div>
                    <div class="role-check">
                      <el-icon class="check-icon"><CircleCheckFilled /></el-icon>
                    </div>
                  </div>
                </el-radio>
                <el-radio label="USER" class="role-item modern-role-item">
                  <div class="role-content">
                    <div class="role-icon-wrapper user-icon">
                      <el-icon class="role-icon"><User /></el-icon>
                    </div>
                    <div class="role-info">
                      <h3 class="role-name">普通用户</h3>
                      <p class="role-desc">基础查看权限</p>
                    </div>
                    <div class="role-check">
                      <el-icon class="check-icon"><CircleCheckFilled /></el-icon>
                    </div>
                  </div>
                </el-radio>
              </el-radio-group>
            </div>
            <span class="field-hint modern-field-hint">
              <span v-if="userForm.role === 'PRESIDENT'">协会会长拥有系统最高权限，系统中只能存在一个</span>
              <span v-else-if="userForm.role === 'VENUE_MANAGER'">场馆管理者只能管理自己场馆的业务，需要选择所属场馆</span>
              <span v-else-if="userForm.role === 'COACH'">教练需在「教练管理」中关联教练档案后即可登录教练端</span>
              <span v-else>普通用户仅可查看和操作自己的数据</span>
            </span>
          </el-form-item>

          <!-- 场馆选择字段（仅场馆管理者需要） -->
          <el-form-item 
            v-if="userForm.role === 'VENUE_MANAGER'" 
            label="所属场馆" 
            prop="venueId" 
            class="form-item-enhanced modern-form-item"
          >
            <el-select 
              v-model="userForm.venueId" 
              placeholder="请选择所属场馆" 
              filterable
              style="width: 100%"
              class="form-select"
            >
              <el-option 
                v-for="venue in venueOptions" 
                :key="venue.id" 
                :label="venue.venueName" 
                :value="venue.id"
                :disabled="venue.status === 0"
              >
                <span>{{ venue.venueName }}</span>
                <el-tag v-if="venue.status === 0" type="danger" size="small" style="margin-left: 8px">关闭</el-tag>
                <el-tag v-else-if="venue.status === 2" type="warning" size="small" style="margin-left: 8px">暂停</el-tag>
              </el-option>
            </el-select>
            <span class="field-hint modern-field-hint">场馆管理者必须关联一个场馆</span>
          </el-form-item>

          <!-- 状态字段 -->
          <el-form-item label="账户状态" prop="status" class="form-item-enhanced modern-form-item">
            <div class="status-selector modern-status-selector">
              <el-radio-group v-model="userForm.status" class="radio-group-enhanced modern-radio-group">
                <el-radio :label="1" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon enabled modern-status-icon"><SuccessFilled /></el-icon>
                    启用
                  </span>
                </el-radio>
                <el-radio :label="0" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon disabled modern-status-icon"><CircleCloseFilled /></el-icon>
                    禁用
                  </span>
                </el-radio>
              </el-radio-group>
            </div>
            <span class="field-hint modern-field-hint">禁用后用户将无法登录系统</span>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-cancel modern-btn-cancel bmp-uiv-btn bmp-uiv-btn-secondary">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitLoading"
            class="btn-submit modern-btn-submit bmp-uiv-btn bmp-uiv-btn-primary"
          >
            {{ isEdit ? '更新用户' : '创建用户' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看用户对话框 -->
    <el-dialog
      title="用户详情"
      v-model="viewDialogVisible"
      width="600px"
      @close="handleViewDialogClose"
      class="modern-dialog"
    >
      <el-descriptions :column="1" border :model="viewFormData" class="modern-descriptions">
        <el-descriptions-item label="用户名" class="modern-descriptions-item">
          <template #label>
            <span class="modern-descriptions-label">用户名</span>
          </template>
          <template #default>
            <span class="modern-descriptions-value">{{ viewFormData.username }}</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="手机号" class="modern-descriptions-item">
          <template #label>
            <span class="modern-descriptions-label">手机号</span>
          </template>
          <template #default>
            <span class="modern-descriptions-value">{{ viewFormData.phone || '无' }}</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="身份证号" class="modern-descriptions-item">
          <template #label>
            <span class="modern-descriptions-label">身份证号</span>
          </template>
          <template #default>
            <span class="modern-descriptions-value">{{ viewFormData.idCard || '无' }}</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="角色" class="modern-descriptions-item">
          <template #label>
            <span class="modern-descriptions-label">角色</span>
          </template>
          <template #default>
            <span class="modern-descriptions-value">{{ getRoleText(viewFormData.role) }}</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="状态" class="modern-descriptions-item">
          <template #label>
            <span class="modern-descriptions-label">状态</span>
          </template>
          <template #default>
            <span class="modern-descriptions-value">{{ viewFormData.status === 1 ? '启用' : '禁用' }}</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" class="modern-descriptions-item">
          <template #label>
            <span class="modern-descriptions-label">创建时间</span>
          </template>
          <template #default>
            <span class="modern-descriptions-value">{{ formatDateTime(viewFormData.createTime) }}</span>
          </template>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer modern-dialog-footer">
          <el-button @click="viewDialogVisible = false" class="modern-btn-cancel bmp-uiv-btn bmp-uiv-btn-secondary">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  View,
  UserFilled,
  User,
  Lock,
  Phone,
  DocumentCopy,
  Management,
  SuccessFilled,
  CircleCloseFilled,
  CircleCheckFilled,
  Trophy,
  Avatar
} from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, findUserByRole } from '@/api/user'
import { getVenueOptions } from '@/api/court'

// 搜索表单
const searchForm = reactive({
  username: '',
  idCard: '',
  role: null,
  status: null
})

// 用户列表
const userList = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const isEdit = ref(false)
const submitLoading = ref(false)
const userFormRef = ref(null)

// 查看表单数据
const viewFormData = reactive({
  id: null,
  username: '',
  phone: '',
  idCard: '',
  role: 'USER',
  status: 1,
  createTime: ''
})

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  phone: '',
  idCard: '',
  role: 'USER',
  venueId: null, // 场馆ID（仅场馆管理者需要）
  status: 1
})

// 场馆选项
const venueOptions = ref([])

// 协会会长唯一性检查
const presidentExists = ref(false)

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  venueId: [
    { 
      validator: (rule, value, callback) => {
        if (userForm.role === 'VENUE_MANAGER' && !value) {
          callback(new Error('场馆管理者必须选择所属场馆'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
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

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const params = {
      username: searchForm.username || null,
      idCard: searchForm.idCard || null,
      role: searchForm.role || null,
      status: searchForm.status,
      page: pagination.page,
      size: pagination.size
    }
    const response = await getUserList(params)
    if (response.code === 200) {
      userList.value = response.data.data || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载场馆选项
const loadVenueOptions = async () => {
  try {
    const res = await getVenueOptions()
    if (res.code === 200) {
      venueOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载场馆选项失败', e)
  }
}

// 检查是否已存在协会会长
const checkPresidentExists = async () => {
  try {
    const res = await findUserByRole('PRESIDENT')
    if (res.code === 200 && res.data) {
      presidentExists.value = true
    } else {
      presidentExists.value = false
    }
  } catch (e) {
    console.error('检查协会会长失败', e)
    presidentExists.value = false
  }
}

// 角色显示文本
const getRoleText = (role) => {
  if (role === 'PRESIDENT') return '协会会长'
  if (role === 'VENUE_MANAGER') return '场馆管理者'
  if (role === 'COACH') return '教练'
  return '普通用户'
}

// 根据场馆ID获取场馆名称
const getVenueName = (venueId) => {
  if (!venueId) return '-'
  const venue = venueOptions.value.find(v => v.id === venueId)
  return venue ? venue.venueName : '-'
}

// 角色标签样式
const getRoleTagType = (role) => {
  if (role === 'PRESIDENT') return 'danger'
  if (role === 'VENUE_MANAGER') return 'warning'
  if (role === 'COACH') return 'success'
  return 'info'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.idCard = ''
  searchForm.role = null
  searchForm.status = null
  pagination.page = 1
  loadUserList()
}

// 添加用户
const handleAdd = async () => {
  // 检查协会会长是否存在
  await checkPresidentExists()
  // 加载场馆选项
  await loadVenueOptions()
  dialogTitle.value = '添加用户'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = async (row) => {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  // 加载场馆选项
  await loadVenueOptions()
  // 检查协会会长唯一性
  await checkPresidentExists()
  
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    password: '',
    phone: row.phone || '',
    idCard: row.idCard || '',
    role: row.role,
    venueId: row.venueId || null,
    status: row.status
  })
  dialogVisible.value = true
}

// 查看用户
const handleView = (row) => {
  Object.assign(viewFormData, {
    id: row.id,
    username: row.username,
    phone: row.phone || '',
    idCard: row.idCard || '',
    role: row.role,
    status: row.status,
    createTime: row.createTime || ''
  })
  viewDialogVisible.value = true
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    const response = await updateUser(row)
    if (response.code !== 200) {
      // 如果更新失败，恢复原状态
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error('更新用户状态失败：' + (response.message || '未知错误'))
    } else {
      ElMessage.success('更新用户状态成功')
    }
  } catch (error) {
    console.error('更新用户状态失败：', error)
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新用户状态失败：' + error.message)
  }
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteUser(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadUserList()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      // 检查协会会长唯一性
      if (userForm.role === 'PRESIDENT') {
        await checkPresidentExists()
        if (presidentExists.value) {
          ElMessage.warning('系统中已存在协会会长，请先修改现有协会会长的角色')
          return
        }
      }
      
      // 检查场馆管理者必须选择场馆
      if (userForm.role === 'VENUE_MANAGER' && !userForm.venueId) {
        ElMessage.warning('场馆管理者必须选择所属场馆')
        return
      }
      
      // 普通用户和协会会长的venueId必须为空
      if (userForm.role !== 'VENUE_MANAGER') {
        userForm.venueId = null
      }
      
      submitLoading.value = true
      try {
        const formData = { ...userForm }
        // 编辑时不需要密码
        if (isEdit.value && !formData.password) {
          delete formData.password
        }
        
        const response = isEdit.value
          ? await updateUser(formData)
          : await addUser(formData)
        
        if (response.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadUserList()
        } else {
          ElMessage.error(response.message || (isEdit.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error('提交失败:', error)
        const errorMessage = error.response?.data?.message || error.message || '操作失败，请稍后重试'
        ElMessage.error(errorMessage)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    phone: '',
    idCard: '',
    role: 'USER',
    status: 1
  })
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

// 关闭查看对话框
const handleViewDialogClose = () => {
  Object.assign(viewFormData, {
    id: null,
    username: '',
    phone: '',
    idCard: '',
    role: 'USER',
    status: 1,
    createTime: ''
  })
}

// 关闭编辑对话框
const handleDialogClose = () => {
  resetForm()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadUserList()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  loadUserList()
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

// 初始化
onMounted(() => {
  loadUserList()
  loadVenueOptions()
  checkPresidentExists()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');

.user-management {
  padding: 20px;
  background-color: var(--color-background, #f0f2f5);
  height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
}

.user-management::-webkit-scrollbar {
  width: 8px;
}

.user-management::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.user-management::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 4px;
}

.user-management::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted, #94A3B8);
}

.user-management-background {
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

.user-management-content {
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
  background: #EFF6FF;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.header-icon:hover {
  background: #DBEAFE;
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
  animation: slideInLeft 0.6s ease-out 0.1s both;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.page-subtitle {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
  animation: slideInLeft 0.6s ease-out 0.2s both;
}

.header-stats {
  display: flex;
  gap: 24px;
  align-items: center;
}

.stat-item {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--gradient-members, #EFF6FF);
  border-radius: 12px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.stat-item:hover {
  background: #DBEAFE;
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary, #3b82f6);
  font-family: 'Fira Code', monospace;
}

.search-card {
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-form {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  flex: 0 1 auto;
  animation: slideInLeft 0.6s ease-out both;
}

.search-item:nth-child(1) {
  animation-delay: 0.15s;
}

.search-item:nth-child(2) {
  animation-delay: 0.2s;
}

.search-item:nth-child(3) {
  animation-delay: 0.25s;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 150px;
}

.search-form :deep(.el-form-item__label) {
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
}

.search-form :deep(.el-input__wrapper) {
  position: relative;
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  box-shadow: none;
  transition: all 0.3s ease;
}

.search-form :deep(.el-input__wrapper:hover) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-border-hover, #CBD5E1);
}

.search-form :deep(.el-input__wrapper:focus-within) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.search-form :deep(.el-input__wrapper:focus-within)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary-light, #60A5FA), var(--color-primary, #3B82F6));
  border-radius: 0 0 6px 6px;
}

.search-form :deep(.el-input__inner) {
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Sans', sans-serif;
}

.search-form :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted, #94A3B8);
}

.search-form :deep(.el-select__wrapper) {
  position: relative;
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  box-shadow: none;
  transition: all 0.3s ease;
}

.search-form :deep(.el-select__wrapper:hover) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-border-hover, #CBD5E1);
}

.search-form :deep(.el-select__wrapper:focus-within) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.search-form :deep(.el-select__wrapper:focus-within)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary-light, #60A5FA), var(--color-primary, #3B82F6));
  border-radius: 0 0 6px 6px;
}

.search-form :deep(.el-select__placeholder) {
  color: var(--color-text-muted, #94A3B8);
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  animation: slideInRight 0.6s ease-out 0.3s both;
  flex-shrink: 0;
  white-space: nowrap;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.search-btn {
  min-width: 100px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.reset-btn {
  min-width: 80px;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  transform: translateY(-2px);
}

.action-card {
  padding: 16px 24px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.table-card {
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.3s both;
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
  height: 50px;
  transition: all 0.3s ease;
}

.table-card :deep(.el-table td) {
  border-bottom: 1px solid var(--color-border, #E2E8F0) !important;
  padding: 14px 0 !important;
  height: 50px;
  transition: all 0.3s ease;
  color: var(--color-text-primary, #1E293B);
}

.table-card :deep(.el-table tr) {
  background: transparent !important;
  transition: all 0.3s ease;
}

.table-card :deep(.el-table tr:hover > td) {
  background: var(--color-background, #F8FAFC) !important;
  box-shadow: inset 0 0 10px rgba(59, 130, 246, 0.05);
}

.table-card :deep(.el-table--border) {
  border: 1px solid var(--color-border, #E2E8F0);
}

.table-card :deep(.el-table--border::after) {
  background-color: var(--color-border, #E2E8F0);
}

.table-card :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: var(--color-background, #F8FAFC) !important;
}

.table-card :deep(.el-table__body-wrapper) {
  scrollbar-width: thin;
  scrollbar-color: rgba(59, 130, 246, 0.3) transparent;
}

.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background: transparent;
}

.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 4px;
}

.table-card :deep(.el-table__body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: var(--color-text-muted, #94A3B8);
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  width: 100%;
  min-height: 32px;
}

.operation-buttons :deep(.el-button) {
  margin: 0;
  padding: 6px 12px;
  font-size: 12px;
  height: 32px;
  line-height: 32px;
  white-space: nowrap;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  flex: 0 1 auto;
}

.operation-buttons :deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.operation-buttons :deep(.el-button:hover::before) {
  width: 300px;
  height: 300px;
}

.operation-buttons :deep(.el-button--small) {
  padding: 6px 10px;
  height: 32px;
}

.operation-buttons :deep(.el-button span) {
  display: flex;
  align-items: center;
  gap: 4px;
  position: relative;
  z-index: 1;
}

.operation-buttons :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.operation-buttons :deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}

.operation-buttons :deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.4);
}

/* 身份证号样式 */
.id-card-text {
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  color: var(--color-text-primary, #1E293B);
  letter-spacing: 0.5px;
}

.table-card :deep(.el-loading-mask) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
}

.table-card :deep(.el-loading-text) {
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
}

.table-card :deep(.circular) {
  animation: spin 2s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 表格标签样式 */
.table-card :deep(.el-tag) {
  border: none;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  transition: all 0.3s ease;
  animation: tagFadeIn 0.4s ease-out;
}

@keyframes tagFadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.table-card :deep(.el-tag:hover) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.table-card :deep(.el-tag--success) {
  background: rgba(16, 185, 129, 0.15);
  color: var(--color-success, #10b981);
}

.table-card :deep(.el-tag--info) {
  background: rgba(59, 130, 246, 0.15);
  color: var(--color-primary, #3b82f6);
}

.table-card :deep(.el-tag--primary) {
  background: rgba(59, 130, 246, 0.15);
  color: var(--color-primary, #3b82f6);
}

/* 开关样式 */
.table-card :deep(.el-switch) {
  --el-switch-on-color: var(--color-success, #13ce66);
  --el-switch-off-color: var(--color-danger, #ff4d4f);
  transition: all 0.3s ease;
}

.table-card :deep(.el-switch:hover) {
  transform: scale(1.1);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #E2E8F0);
  animation: fadeInUp 0.6s ease-out 0.4s both;
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

.user-dialog :deep(.el-dialog) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  box-shadow: var(--shadow-hover, 0 20px 60px rgba(0, 0, 0, 0.08));
  animation: dialogSlideIn 0.3s ease-out;
}

@keyframes dialogSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.user-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  padding: 20px 24px;
  background: var(--color-card-bg-hover, #F8FAFC);
}

.user-dialog :deep(.el-dialog__title) {
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Sans', sans-serif;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.user-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--color-text-muted, #94A3B8);
  transition: all 0.3s ease;
  font-size: 18px;
}

.user-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: var(--color-text-primary, #1E293B);
  transform: rotate(90deg);
}

.user-dialog :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
  background: var(--color-card-bg, #FFFFFF);
}

.user-dialog :deep(.el-dialog__body::-webkit-scrollbar) {
  width: 6px;
}

.user-dialog :deep(.el-dialog__body::-webkit-scrollbar-track) {
  background: var(--color-background, #F1F5F9);
}

.user-dialog :deep(.el-dialog__body::-webkit-scrollbar-thumb) {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 3px;
}

.user-dialog :deep(.el-dialog__body::-webkit-scrollbar-thumb:hover) {
  background: var(--color-text-muted, #94A3B8);
}

.user-dialog :deep(.el-form-item) {
  margin-bottom: 20px;
  animation: slideInLeft 0.4s ease-out both;
}

.user-dialog :deep(.el-form-item:nth-child(1)) {
  animation-delay: 0.05s;
}

.user-dialog :deep(.el-form-item:nth-child(2)) {
  animation-delay: 0.1s;
}

.user-dialog :deep(.el-form-item:nth-child(3)) {
  animation-delay: 0.15s;
}

.user-dialog :deep(.el-form-item:nth-child(4)) {
  animation-delay: 0.2s;
}

.user-dialog :deep(.el-form-item:nth-child(5)) {
  animation-delay: 0.25s;
}

.user-dialog :deep(.el-form-item__label) {
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Sans', sans-serif;
  font-weight: 500;
  font-size: 14px;
  letter-spacing: 0.3px;
}

.user-dialog :deep(.el-form-item__error) {
  color: var(--color-danger, #ff6b6b);
  font-size: 13px;
  margin-top: 4px;
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-5px);
  }
  75% {
    transform: translateX(5px);
  }
}

.user-dialog :deep(.el-input__wrapper) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  transition: all 0.3s ease;
  border-radius: 6px;
}

.user-dialog :deep(.el-input__wrapper:hover) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-border-hover, #CBD5E1);
}

.user-dialog :deep(.el-input__wrapper:focus-within) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.user-dialog :deep(.el-input__inner) {
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
}

.user-dialog :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted, #94A3B8);
}

.user-dialog :deep(.el-input.is-disabled .el-input__wrapper) {
  background: var(--color-background, #F1F5F9);
  border-color: var(--color-border, #E2E8F0);
  cursor: not-allowed;
}

.user-dialog :deep(.el-input.is-disabled .el-input__inner) {
  color: var(--color-text-muted, #CBD5E1);
}

.user-dialog :deep(.el-select__wrapper) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  transition: all 0.3s ease;
  border-radius: 6px;
}

.user-dialog :deep(.el-select__wrapper:hover) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-border-hover, #CBD5E1);
}

.user-dialog :deep(.el-select__wrapper:focus-within) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.user-dialog :deep(.el-select__placeholder) {
  color: var(--color-text-muted, #94A3B8);
}

.user-dialog :deep(.el-radio-group) {
  display: flex;
  gap: 16px;
  background: transparent;
}

.user-dialog :deep(.el-radio) {
  transition: all 0.3s ease;
}

.user-dialog :deep(.el-radio:hover) {
  transform: translateX(2px);
}

.user-dialog :deep(.el-radio__label) {
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Sans', sans-serif;
  transition: all 0.3s ease;
}

.user-dialog :deep(.el-radio:hover .el-radio__label) {
  color: var(--color-text-primary, #1E293B);
}

.user-dialog :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: var(--color-primary, #3B82F6);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.4);
}

.user-dialog :deep(.dialog-footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid var(--color-border, #E2E8F0);
  background: var(--color-card-bg-hover, #F8FAFC);
  border-radius: 0 0 16px 16px;
}

.user-dialog :deep(.el-button) {
  transition: all 0.3s ease;
  border-radius: 6px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.user-dialog :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

.user-dialog :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--color-primary-light, #3b82f6) 0%, var(--color-primary, #2563eb) 100%);
  border: none;
}

.user-dialog :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, var(--color-primary, #2563eb) 0%, var(--color-primary-dark, #1d4ed8) 100%);
}

/* 增强的表单样式 */
.user-form {
  padding: 8px 0;
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 16px 0;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Fira Sans', sans-serif;
  letter-spacing: 0.5px;
}

.form-item-enhanced {
  margin-bottom: 18px !important;
  animation: slideInLeft 0.4s ease-out both;
}

.form-item-enhanced:nth-child(1) {
  animation-delay: 0.05s;
}

.form-item-enhanced:nth-child(2) {
  animation-delay: 0.1s;
}

.form-item-enhanced:nth-child(3) {
  animation-delay: 0.15s;
}

.form-item-enhanced:nth-child(4) {
  animation-delay: 0.2s;
}

.form-item-enhanced:nth-child(5) {
  animation-delay: 0.25s;
}

.user-dialog :deep(.form-item-enhanced .el-form-item__label) {
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
  font-size: 13px;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.form-input {
  width: 100%;
}

.user-dialog :deep(.form-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.user-dialog :deep(.form-input .el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.09);
  border-color: rgba(59, 130, 246, 0.4);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1), 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.user-dialog :deep(.form-input .el-input__wrapper:focus-within) {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(59, 130, 246, 0.7);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1), 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.user-dialog :deep(.form-input .el-input__inner) {
  color: #ffffff;
  font-size: 14px;
  font-family: 'Fira Sans', sans-serif;
}

.user-dialog :deep(.form-input .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.35);
}

.input-icon {
  color: rgba(59, 130, 246, 0.6);
  transition: all 0.3s ease;
  font-size: 16px;
}

.user-dialog :deep(.form-input:hover .input-icon) {
  color: rgba(59, 130, 246, 0.8);
}

.user-dialog :deep(.form-input:focus-within .input-icon) {
  color: rgba(59, 130, 246, 1);
}

.field-hint {
  display: block;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 6px;
  font-family: 'Fira Sans', sans-serif;
  letter-spacing: 0.2px;
}

.form-select {
  width: 100%;
}

.user-dialog :deep(.form-select .el-select__wrapper) {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.user-dialog :deep(.form-select .el-select__wrapper:hover) {
  background: rgba(255, 255, 255, 0.09);
  border-color: rgba(59, 130, 246, 0.4);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1), 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.user-dialog :deep(.form-select .el-select__wrapper:focus-within) {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(59, 130, 246, 0.7);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1), 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.user-dialog :deep(.form-select .el-select__placeholder) {
  color: rgba(255, 255, 255, 0.35);
}

.option-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
}

.option-icon {
  font-size: 16px;
  color: rgba(59, 130, 246, 0.7);
}

.status-selector {
  padding: 12px;
  background: var(--color-background, #F8FAFC);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.status-selector:hover {
  background: #F1F5F9;
  border-color: #CBD5E1;
}

.radio-group-enhanced {
  display: flex;
  gap: 20px;
}

.radio-item {
  flex: 1;
  transition: all 0.3s ease;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1E293B;
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
}

.status-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.status-icon.enabled {
  color: #10b981;
}

.status-icon.disabled {
  color: #ef4444;
}

.user-dialog :deep(.el-radio:hover .radio-label) {
  color: #1E293B;
}

.user-dialog :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: #3B82F6;
  border-color: #3B82F6;
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.4);
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #E2E8F0;
  background: #F8FAFC;
  border-radius: 0 0 16px 16px;
  animation: slideInUp 0.3s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-dialog :deep(.btn-cancel) {
  transition: all 0.3s ease;
  border-radius: 6px;
  font-weight: 500;
}

.user-dialog :deep(.btn-cancel) {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  color: #64748B;
}

.user-dialog :deep(.btn-cancel:hover) {
  background: #F1F5F9;
  border-color: #CBD5E1;
  color: #1E293B;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-submit {
  transition: all 0.3s ease;
  border-radius: 6px;
  font-weight: 500;
  min-width: 100px;
}

.user-dialog :deep(.btn-submit) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  color: #ffffff;
}

.user-dialog :deep(.btn-submit:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.user-dialog :deep(.btn-submit:active) {
  transform: translateY(0);
}

/* 现代对话框样式 */
.modern-dialog {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
}

.modern-dialog :deep(.el-dialog) {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 20px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
}

.modern-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  padding: 24px 28px;
  border-radius: 20px 20px 0 0;
}

.modern-dialog :deep(.el-dialog__title) {
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.modern-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 20px;
  transition: all 0.3s ease;
}

.modern-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #ffffff;
  transform: rotate(90deg);
}

.modern-dialog :deep(.el-dialog__body) {
  padding: 28px;
  background: transparent;
}

.modern-dialog :deep(.el-dialog__footer) {
  padding: 20px 28px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 0 0 20px 20px;
  border-top: 1px solid rgba(226, 232, 240, 0.8);
}

/* 现代表单样式 */
.modern-form {
  padding: 0;
}

.modern-form-section {
  margin-bottom: 28px;
  padding: 28px;
  background: linear-gradient(135deg, #ffffff 0%, rgba(248, 250, 252, 0.95) 100%);
  border-radius: 16px;
  border: 2px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.modern-form-section:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.modern-section-title {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Inter', 'Fira Sans', sans-serif;
}

.modern-section-title::before {
  content: '';
  width: 5px;
  height: 24px;
  background: linear-gradient(180deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 3px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.modern-form-item {
  margin-bottom: 24px !important;
}

.modern-form-item:last-child {
  margin-bottom: 0 !important;
}

.modern-dialog :deep(.modern-form-item .el-form-item__label) {
  color: #1e293b;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 10px;
  padding: 0;
  letter-spacing: 0.3px;
}

.modern-form-input,
.modern-form-select {
  width: 100%;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modern-dialog :deep(.modern-form-input .el-input__wrapper),
.modern-dialog :deep(.modern-form-select .el-select__wrapper) {
  background: #ffffff;
  border: 2px solid rgba(226, 232, 240, 0.8);
  border-radius: 12px;
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modern-dialog :deep(.modern-form-input .el-input__wrapper:hover),
.modern-dialog :deep(.modern-form-select .el-select__wrapper:hover) {
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.modern-dialog :deep(.modern-form-input .el-input__wrapper:focus-within),
.modern-dialog :deep(.modern-form-select .el-select__wrapper:focus-within) {
  border-color: #3B82F6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15);
}

.modern-dialog :deep(.modern-form-input .el-input__inner) {
  color: #1e293b;
  font-size: 15px;
  font-weight: 500;
  height: auto;
}

.modern-dialog :deep(.modern-form-input .el-input__inner::placeholder) {
  color: rgba(148, 163, 184, 0.8);
}

.modern-dialog :deep(.modern-form-select .el-select__placeholder) {
  color: rgba(148, 163, 184, 0.8);
  font-size: 15px;
}

.modern-input-icon {
  color: #3B82F6;
  font-size: 18px;
  transition: all 0.3s ease;
}

.modern-dialog :deep(.modern-form-input:hover .modern-input-icon) {
  color: #2563EB;
  transform: scale(1.1);
}

.modern-field-hint {
  display: block;
  margin-top: 10px;
  font-size: 13px;
  color: #64748b;
  font-style: italic;
  padding-left: 4px;
  letter-spacing: 0.2px;
  line-height: 1.5;
}

.modern-option-label {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.modern-option-label:hover {
  background: rgba(59, 130, 246, 0.08);
}

.modern-option-icon {
  font-size: 17px;
  color: #3B82F6;
}

.modern-status-selector {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: rgba(248, 250, 252, 0.9);
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.modern-radio-group {
  display: flex;
  gap: 32px;
  width: 100%;
}

.modern-radio-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 24px;
  border-radius: 12px;
  background: #ffffff;
  border: 2px solid rgba(226, 232, 240, 0.8);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modern-radio-item:hover {
  border-color: rgba(59, 130, 246, 0.4);
  background: rgba(59, 130, 246, 0.03);
}

.modern-dialog :deep(.modern-radio-item .el-radio__input.is-checked + .el-radio__label) {
  color: #3B82F6;
}

.modern-dialog :deep(.modern-radio-item .el-radio__input.is-checked .el-radio__inner) {
  background: #3B82F6;
  border-color: #3B82F6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2);
}

.modern-radio-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
}

.modern-status-icon {
  font-size: 22px;
  transition: all 0.3s ease;
}

.modern-status-icon.enabled {
  color: #10b981;
  filter: drop-shadow(0 2px 4px rgba(16, 185, 129, 0.3));
}

.modern-status-icon.disabled {
  color: #ef4444;
  filter: drop-shadow(0 2px 4px rgba(239, 68, 68, 0.3));
}

.modern-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 24px 0 0 0;
  border-top: none;
}

.modern-btn-cancel {
  padding: 14px 32px;
  font-weight: 600;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(226, 232, 240, 0.8);
  background: #ffffff;
  color: #64748b;
  letter-spacing: 0.3px;
}

.modern-btn-cancel:hover {
  border-color: #3B82F6;
  color: #3B82F6;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
}

.modern-btn-submit {
  padding: 14px 36px;
  font-weight: 600;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border: none;
  color: #ffffff;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
}

.modern-btn-submit:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.4);
}

.modern-btn-submit:active {
  transform: translateY(-1px);
}

/* 现代详情页样式 */
.modern-descriptions {
  border-radius: 16px;
  overflow: hidden;
  border: 2px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.modern-descriptions-item {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  transition: all 0.2s ease;
}

.modern-descriptions-item:hover {
  background: rgba(248, 250, 252, 0.8);
}

.modern-descriptions-item:last-child {
  border-bottom: none;
}

.modern-descriptions-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.modern-descriptions-label::before {
  content: '';
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}

.modern-descriptions-value {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* 卡片式角色选择器样式 */
.role-selector {
  display: flex;
  gap: 16px;
  padding: 0;
}

.modern-role-selector {
  width: 100%;
}

.role-group {
  display: flex;
  gap: 16px;
  width: 100%;
}

.modern-role-group {
  flex-direction: row;
}

.role-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0;
  margin: 0;
  border-radius: 16px;
  background: #ffffff;
  border: 2px solid #E2E8F0;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.modern-role-item {
  height: auto;
  border-color: rgba(59, 130, 246, 0.2);
}

.role-item:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
}

.role-item:active {
  transform: translateY(-1px);
}

.modern-dialog :deep(.role-item .el-radio__input) {
  display: none;
}

.modern-dialog :deep(.role-item .el-radio__label) {
  padding: 0;
  width: 100%;
  cursor: pointer;
}

.modern-dialog :deep(.role-item .el-radio__input.is-checked + .el-radio__label .role-item) {
  border-color: #3B82F6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.03) 0%, rgba(37, 99, 235, 0.02) 100%);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
}

.role-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  width: 100%;
  position: relative;
}

.role-icon-wrapper {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.president-icon {
  background: linear-gradient(135deg, #FEE2E2 0%, #FEF2F2 100%);
  border: 1px solid #FECACA;
}

.manager-icon {
  background: linear-gradient(135deg, #FEF3C7 0%, #FFFBEB 100%);
  border: 1px solid #FDE68A;
}

.user-icon {
  background: linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%);
  border: 1px solid #BFDBFE;
}

.president-icon {
  background: linear-gradient(135deg, #FEE2E2 0%, #FEF2F2 100%);
  border: 1px solid #FECACA;
}

.manager-icon {
  background: linear-gradient(135deg, #FEF3C7 0%, #FFFBEB 100%);
  border: 1px solid #FDE68A;
}

.coach-icon {
  background: linear-gradient(135deg, #CCFBF1 0%, #F0FDFA 100%);
  border: 1px solid #99F6E4;
}

.admin-icon {
  background: linear-gradient(135deg, #F3E8FF 0%, #FAF5FF 100%);
  border: 1px solid #E9D5FF;
}

.role-icon {
  font-size: 24px;
  transition: all 0.3s ease;
}

.user-icon .role-icon {
  color: #3b82f6;
}

.user-icon .role-icon {
  color: #3b82f6;
}

.president-icon .role-icon {
  color: #DC2626;
}

.manager-icon .role-icon {
  color: #D97706;
}

.coach-icon .role-icon {
  color: #0D9488;
}

.admin-icon .role-icon {
  color: #667eea;
}

.role-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.role-name {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.5px;
}

.role-desc {
  font-size: 13px;
  color: #64748b;
  font-weight: 400;
}

.role-check {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #F1F5F9;
  border: 2px solid #E2E8F0;
  transition: all 0.3s ease;
  flex-shrink: 0;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.check-icon {
  font-size: 16px;
  color: #94A3B8;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: scale(0.5);
}

/* 选中状态 */
.modern-dialog :deep(.role-item.is-checked),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .role-item) {
  border-color: #3B82F6;
  background: linear-gradient(135deg, #EFF6FF 0%, #F0F9FF 100%);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
}

.modern-dialog :deep(.role-item.is-checked:hover),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .role-item:hover) {
  border-color: #2563EB;
  box-shadow: 0 12px 32px rgba(59, 130, 246, 0.2);
}

.modern-dialog :deep(.role-item.is-checked .user-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .user-icon) {
  background: linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%);
  border-color: #BFDBFE;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.modern-dialog :deep(.role-item.is-checked .president-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .president-icon) {
  background: linear-gradient(135deg, #FEE2E2 0%, #FEF2F2 100%);
  border-color: #FECACA;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}

.modern-dialog :deep(.role-item.is-checked .manager-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .manager-icon) {
  background: linear-gradient(135deg, #FEF3C7 0%, #FFFBEB 100%);
  border-color: #FDE68A;
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.2);
}

.modern-dialog :deep(.role-item.is-checked .coach-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .coach-icon) {
  background: linear-gradient(135deg, #CCFBF1 0%, #F0FDFA 100%);
  border-color: #99F6E4;
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.2);
}

.modern-dialog :deep(.role-item.is-checked .admin-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .admin-icon) {
  background: linear-gradient(135deg, #F3E8FF 0%, #FAF5FF 100%);
  border-color: #E9D5FF;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.modern-dialog :deep(.role-item.is-checked .role-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .role-icon) {
  transform: scale(1.1);
}

.modern-dialog :deep(.role-item.is-checked .user-icon .role-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .user-icon .role-icon) {
  color: #3b82f6;
  filter: drop-shadow(0 2px 6px rgba(59, 130, 246, 0.3));
}

.modern-dialog :deep(.role-item.is-checked .president-icon .role-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .president-icon .role-icon) {
  color: #DC2626;
  filter: drop-shadow(0 2px 6px rgba(220, 38, 38, 0.3));
}

.modern-dialog :deep(.role-item.is-checked .manager-icon .role-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .manager-icon .role-icon) {
  color: #D97706;
  filter: drop-shadow(0 2px 6px rgba(217, 119, 6, 0.3));
}

.modern-dialog :deep(.role-item.is-checked .coach-icon .role-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .coach-icon .role-icon) {
  color: #0D9488;
  filter: drop-shadow(0 2px 6px rgba(13, 148, 136, 0.3));
}

.modern-dialog :deep(.role-item.is-checked .admin-icon .role-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .admin-icon .role-icon) {
  color: #667eea;
  filter: drop-shadow(0 2px 6px rgba(102, 126, 234, 0.3));
}

.modern-dialog :deep(.role-item.is-checked .role-check),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .role-check) {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border-color: #3B82F6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.modern-dialog :deep(.role-item.is-checked .check-icon),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .check-icon) {
  color: #ffffff;
  opacity: 1;
  transform: scale(1);
}

.modern-dialog :deep(.role-item.is-checked .role-name),
.modern-dialog :deep(.el-radio__input.is-checked + .el-radio__label .role-name) {
  color: #1e293b;
}

@media (max-width: 1200px) {
  .header-stats {
    flex-direction: column;
    gap: 12px;
  }

  .stat-item {
    width: 100%;
    padding: 10px 16px;
  }

  .search-fields {
    gap: 12px;
  }

  .search-input {
    width: 160px;
  }

  .search-select {
    width: 120px;
  }

  .operation-buttons :deep(.el-button) {
    padding: 5px 10px;
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .user-management-content {
    padding: 12px;
  }

  .glass-card {
    margin-bottom: 16px;
    border-radius: 12px;
  }

  .page-header {
    padding: 16px;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
  }

  .header-left {
    width: 100%;
  }

  .header-stats {
    width: 100%;
    justify-content: space-around;
  }

  .page-title {
    font-size: 20px;
  }

  .page-subtitle {
    font-size: 12px;
  }

  .search-card {
    padding: 16px;
  }

  .search-header {
    margin-bottom: 12px;
    padding-bottom: 8px;
  }

  .search-title {
    font-size: 14px;
  }

  .search-form {
    gap: 12px;
  }

  .search-container {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-fields {
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .search-item {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .search-select {
    width: 100%;
  }

  .search-buttons {
    width: 100%;
    flex-direction: column;
    gap: 10px;
    margin-top: 4px;
  }

  .search-btn,
  .reset-btn {
    width: 100%;
    min-height: 44px;
    font-size: 15px;
  }

  .search-form :deep(.el-form-item__label) {
    font-size: 13px;
    margin-bottom: 6px;
  }

  .search-form :deep(.el-input__wrapper),
  .search-form :deep(.el-select__wrapper) {
    min-height: 44px;
  }

  .action-card {
    padding: 12px 16px;
  }

  .action-card :deep(.el-button) {
    width: 100%;
  }

  .table-card {
    padding: 12px;
  }

  .table-card :deep(.el-table) {
    font-size: 12px;
  }

  .table-card :deep(.el-table th) {
    padding: 8px 0 !important;
    height: 40px;
    font-size: 12px;
  }

  .table-card :deep(.el-table td) {
    padding: 8px 0 !important;
    height: 40px;
    font-size: 12px;
  }

  .operation-buttons {
    flex-direction: column;
    gap: 2px;
    min-height: auto;
  }

  .operation-buttons :deep(.el-button) {
    width: 100%;
    padding: 4px 8px;
    height: 28px;
    line-height: 28px;
    font-size: 11px;
  }

  .operation-buttons :deep(.el-button span) {
    gap: 2px;
  }

  .pagination-wrapper {
    margin-top: 12px;
    justify-content: center;
  }

  .pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
  }

  .pagination :deep(.el-pagination__total),
  .pagination :deep(.el-pagination__jump) {
    font-size: 13px;
    line-height: 40px;
  }

  .pagination :deep(.btn-prev),
  .pagination :deep(.btn-next),
  .pagination :deep(.el-pager li) {
    min-width: 40px;
    height: 40px;
    line-height: 40px;
    padding: 0 8px;
    font-size: 13px;
  }

  .pagination :deep(.el-select__wrapper) {
    padding: 8px 10px;
    min-height: 40px;
  }

  .pagination :deep(.el-input__wrapper) {
    min-height: 40px;
  }

  .pagination :deep(.el-input__inner) {
    font-size: 13px;
  }

  .user-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto !important;
  }

  .user-dialog :deep(.el-dialog__header) {
    padding: 16px;
  }

  .user-dialog :deep(.el-dialog__body) {
    padding: 16px;
  }

  .form-section {
    margin-bottom: 20px;
    padding-bottom: 16px;
  }

  .section-title {
    font-size: 13px;
    margin-bottom: 12px;
  }

  .form-item-enhanced {
    margin-bottom: 14px !important;
  }

  .user-dialog :deep(.form-item-enhanced .el-form-item__label) {
    font-size: 12px;
    margin-bottom: 6px;
  }

  .field-hint {
    font-size: 12px;
    margin-top: 4px;
  }

  .user-dialog :deep(.form-input .el-input__inner),
  .user-dialog :deep(.form-select .el-select__wrapper) {
    font-size: 13px;
  }

  .radio-group-enhanced {
    gap: 16px;
  }

  .radio-label {
    font-size: 13px;
  }

  .status-icon {
    font-size: 16px;
  }

  .dialog-footer-enhanced {
    flex-direction: column;
    gap: 8px;
    padding: 16px;
  }

  .user-dialog :deep(.btn-cancel),
  .user-dialog :deep(.btn-submit) {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .user-management-content {
    padding: 8px;
  }

  .glass-card {
    margin-bottom: 12px;
    border-radius: 8px;
  }

  .page-header {
    padding: 12px;
  }

  .header-icon {
    width: 40px;
    height: 40px;
  }

  .header-icon :deep(.el-icon) {
    font-size: 20px;
  }

  .page-title {
    font-size: 18px;
  }

  .page-subtitle {
    font-size: 11px;
  }

  .stat-label {
    font-size: 10px;
  }

  .stat-value {
    font-size: 18px;
  }

  .search-card {
    padding: 12px;
  }

  .search-title {
    font-size: 13px;
  }

  .search-form {
    gap: 10px;
  }

  .search-fields {
    gap: 10px;
  }

  .table-card {
    padding: 8px;
  }

  .table-card :deep(.el-table) {
    font-size: 11px;
  }

  .table-card :deep(.el-table th) {
    padding: 6px 0 !important;
    height: 36px;
    font-size: 11px;
  }

  .table-card :deep(.el-table td) {
    padding: 6px 0 !important;
    height: 36px;
    font-size: 11px;
  }

  .operation-buttons :deep(.el-button) {
    padding: 3px 6px;
    height: 24px;
    line-height: 24px;
    font-size: 10px;
  }

  .id-card-text {
    font-size: 11px;
  }

  .pagination-wrapper {
    margin-top: 10px;
  }

  .pagination :deep(.el-pagination) {
    gap: 8px;
  }

  .pagination :deep(.el-pagination__total),
  .pagination :deep(.el-pagination__jump) {
    font-size: 12px;
    line-height: 40px;
  }

  .pagination :deep(.btn-prev),
  .pagination :deep(.btn-next),
  .pagination :deep(.el-pager li) {
    min-width: 40px;
    height: 40px;
    line-height: 40px;
    padding: 0 6px;
    font-size: 12px;
    border-radius: 6px;
  }

  .pagination :deep(.el-select__wrapper) {
    padding: 8px 8px;
    font-size: 12px;
    min-height: 40px;
  }

  .pagination :deep(.el-input__inner) {
    font-size: 12px;
    padding: 8px 6px;
  }

  .pagination :deep(.el-input__wrapper) {
    min-height: 40px;
  }

  .user-dialog :deep(.el-dialog) {
    width: 95% !important;
  }

  .user-dialog :deep(.el-dialog__title) {
    font-size: 16px;
  }

  .form-section {
    margin-bottom: 16px;
    padding-bottom: 12px;
  }

  .section-title {
    font-size: 12px;
    margin-bottom: 10px;
  }

  .form-item-enhanced {
    margin-bottom: 12px !important;
  }

  .user-dialog :deep(.form-item-enhanced .el-form-item__label) {
    font-size: 11px;
    margin-bottom: 4px;
  }

  .field-hint {
    font-size: 11px;
    margin-top: 3px;
  }

  .user-dialog :deep(.form-input .el-input__inner),
  .user-dialog :deep(.form-select .el-select__wrapper) {
    font-size: 12px;
  }

  .input-icon {
    font-size: 14px;
  }

  .option-icon {
    font-size: 14px;
  }

  .radio-group-enhanced {
    gap: 12px;
    flex-direction: column;
  }

  .modern-role-group {
    flex-direction: column;
    gap: 12px;
  }

  .radio-item {
    flex: none;
  }

  .radio-label {
    font-size: 12px;
  }

  .status-icon {
    font-size: 16px;
  }

  .dialog-footer-enhanced {
    flex-direction: column;
    gap: 8px;
    padding: 12px;
  }

  .user-dialog :deep(.btn-cancel),
  .user-dialog :deep(.btn-submit) {
    width: 100%;
    font-size: 13px;
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
