<template>
  <div class="venue-management">
    <div class="venue-management-background">
      <div class="gradient-overlay"></div>
    </div>

    <div class="venue-management-content">
      <!-- 页面标题 -->
      <div class="page-header glass-card">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32">
                <OfficeBuilding />
              </el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">场馆管理</h2>
              <p class="page-subtitle">管理羽毛球场馆信息，支持搜索、添加、编辑和删除操作</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总场馆数</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(1)">
              <span class="stat-label">营业中</span>
              <span class="stat-value">{{ statistics.operating || 0 }}</span>
            </div>
            <div class="stat-item stat-item-warning" @click="handleStatClick(2)">
              <span class="stat-label">暂停</span>
              <span class="stat-value">{{ statistics.paused || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">关闭</span>
              <span class="stat-value">{{ statistics.closed || 0 }}</span>
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
              <el-form-item label="场馆名称" class="search-item">
                <el-input v-model="searchForm.venueName" placeholder="请输入场馆名称" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="地址" class="search-item">
                <el-input v-model="searchForm.address" placeholder="请输入地址" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="search-select">
                  <el-option label="关闭" :value="0" />
                  <el-option label="营业中" :value="1" />
                  <el-option label="暂停" :value="2" />
                </el-select>
              </el-form-item>
            </div>
            <div class="search-buttons">
              <el-button type="primary" @click="handleSearch" :icon="Search" class="search-btn bmp-uiv-btn bmp-uiv-btn-primary">
                搜索
              </el-button>
              <el-button type="primary" @click="handleReset" :icon="Refresh" class="reset-btn bmp-uiv-btn bmp-uiv-btn-primary">重置</el-button>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="glass-card action-card" v-if="userRole === 'PRESIDENT'">
        <el-button type="primary" @click="handleAdd" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary">
          添加场馆
        </el-button>
      </div>

      <!-- 场馆列表表格 -->
      <div class="glass-card table-card">
        <el-table :data="venueList" v-loading="loading" element-loading-text="加载中..." style="width: 100%" stripe border
          highlight-current-row :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }" row-key="id" :height="tableHeight"
          :virtual-scroll="venueList.length > 100">
          <el-table-column
            label="序号"
            type="index"
            min-width="80"
            align="center"
            :index="(index) => (pagination.page - 1) * pagination.size + index + 1"
          />
          <el-table-column label="图片" min-width="90" align="center">
            <template #default="scope">
              <el-image v-if="scope.row.venueImage" :src="scope.row.venueImage"
                :preview-src-list="[scope.row.venueImage]" fit="cover" class="venue-thumb" preview-teleported />
              <div v-else class="venue-thumb venue-thumb--placeholder">
                <el-icon :size="18">
                  <Picture />
                </el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="venueName" label="场馆名称" min-width="150" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.venueName" size="small" type="primary">{{ scope.row.venueName }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="200" align="center" show-overflow-tooltip />
          <el-table-column prop="contactPhone" label="联系电话" min-width="130" align="center" />
          <el-table-column prop="contactPerson" label="联系人" min-width="100" align="center" />
          <el-table-column prop="businessHours" label="营业时间" min-width="120" align="center" />
          <el-table-column prop="status" label="状态" min-width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
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
                <el-button type="primary" size="small" @click="handleView(scope.row)" :icon="View" plain>
                  查看
                </el-button>
                <el-button v-if="userRole === 'PRESIDENT' || (userRole === 'VENUE_MANAGER' && scope.row.id === currentUserVenueId)" type="success" size="small" @click="handleEdit(scope.row)" :icon="Edit" plain>
                  编辑
                </el-button>
                <el-button v-if="userRole === 'PRESIDENT'" type="danger" size="small" @click="handleDelete(scope.row)" :icon="Delete" plain>
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
      class="venue-dialog modern-dialog" @close="handleDialogClose" width="700px">
      <el-form ref="venueFormRef" :model="venueForm" :rules="venueFormRules" label-width="auto" label-position="top"
        class="venue-form modern-form">
        <!-- 基本信息 -->
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">基本信息</h4>
          <el-form-item label="场馆名称" prop="venueName" class="form-item-enhanced modern-form-item">
            <el-input v-model="venueForm.venueName" placeholder="请输入场馆名称（最多100个字符）" clearable maxlength="100"
              show-word-limit class="form-input modern-form-input">
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <OfficeBuilding />
                </el-icon>
              </template>
            </el-input>
            <span class="field-hint modern-field-hint">场馆名称用于标识和展示</span>
          </el-form-item>

          <el-form-item label="场馆图片" prop="venueImage" class="form-item-enhanced modern-form-item">
            <div class="venue-images-manager">
              <!-- 主图上传 -->
              <div class="main-image-section">
                <div class="section-label">主图（用于列表展示）</div>
                <el-upload class="main-image-uploader" :show-file-list="false" accept="image/*"
                  :before-upload="beforeImageUpload" :http-request="handleMainImageUpload">
                  <div class="main-image-card" :class="{ 'is-has-image': !!mainImageUrl }">
                    <el-image v-if="mainImageUrl" :src="mainImageUrl" fit="cover" class="main-image-preview"
                      preview-teleported :preview-src-list="[mainImageUrl]" />
                    <div v-else class="main-image-placeholder">
                      <el-icon :size="24">
                        <UploadFilled />
                      </el-icon>
                      <div class="main-image-text">点击上传主图</div>
                      <div class="main-image-subtext">支持 jpg/jpeg/png/webp，≤ 2MB</div>
                    </div>
                    <div v-if="imageUploading" class="main-image-loading">
                      上传中...
                    </div>
                  </div>
                </el-upload>
                <div v-if="mainImageUrl" class="main-image-actions">
                  <el-button size="small" type="danger" plain @click="handleRemoveMainImage">
                    移除主图
                  </el-button>
                </div>
              </div>
              <!-- 详情图上传 -->
              <div class="detail-images-section">
                <div class="section-label">详情图（可上传多张）</div>
                <div class="detail-images-draggable-container">
                  <el-upload class="detail-images-uploader" action="#" list-type="picture-card"
                    :file-list="detailImagesFileList" accept="image/*" :before-upload="beforeImageUpload"
                    :http-request="handleDetailImagesUpload" :on-remove="handleRemoveDetailImage" :limit="10">
                    <el-icon>
                      <Plus />
                    </el-icon>
                    <template #file="{ file, handleRemove }">
                      <div class="detail-image-item" 
                        :draggable="true"
                        @dragstart="handleDragStart($event, file)"
                        @dragover.prevent="handleDragOver($event)"
                        @drop="handleDrop($event, file)"
                        @dragend="handleDragEnd">
                        <img :src="file.url" class="detail-image-preview" alt="" />
                        <span class="detail-image-actions">
                          <el-icon class="detail-image-preview-icon" @click="handlePreviewImage(file)">
                            <ZoomIn />
                          </el-icon>
                          <!-- 直接调用自定义删除逻辑，确保前端列表和后端数据都能同步更新 -->
                          <el-icon class="detail-image-delete-icon" @click.stop="handleDetailImageDeleteClick(file)">
                            <Delete />
                          </el-icon>
                        </span>
                      </div>
                    </template>
                  </el-upload>
                </div>
                <div class="detail-images-hint">
                  <span class="field-hint modern-field-hint">最多上传10张详情图，拖拽图片可调整顺序</span>
                </div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="地址" prop="address" class="form-item-enhanced modern-form-item">
            <el-input v-model="venueForm.address" placeholder="请输入场馆地址（最多255个字符）" clearable maxlength="255"
              show-word-limit class="form-input modern-form-input">
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <Location />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="联系电话" prop="contactPhone" class="form-item-enhanced modern-form-item">
            <el-input v-model="venueForm.contactPhone" placeholder="请输入联系电话（手机号或固定电话）" clearable maxlength="20"
              class="form-input modern-form-input">
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <Phone />
                </el-icon>
              </template>
            </el-input>
            <span class="field-hint modern-field-hint">支持手机号（11位）或固定电话格式</span>
          </el-form-item>

          <el-form-item label="联系人" prop="contactPerson" class="form-item-enhanced modern-form-item">
            <el-input v-model="venueForm.contactPerson" placeholder="请输入联系人姓名（最多50个字符）" clearable maxlength="50"
              class="form-input modern-form-input">
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <User />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="营业时间配置" prop="businessHours" class="form-item-enhanced modern-form-item">
            <div class="schedule-config-container">
              <!-- 工作日配置 -->
              <div class="schedule-item">
                <div class="schedule-header">
                  <span class="schedule-type-label">工作日</span>
                  <el-switch v-model="scheduleConfig.WORKDAY.enabled" @change="handleScheduleEnableChange('WORKDAY')" />
                </div>
                <div v-if="scheduleConfig.WORKDAY.enabled" class="schedule-time-picker">
                  <el-time-picker v-model="scheduleConfig.WORKDAY.startTime" format="HH:mm" value-format="HH:mm"
                    placeholder="开始时间" style="width: 140px" @change="handleScheduleTimeChange('WORKDAY')" />
                  <span class="time-separator">-</span>
                  <el-time-picker v-model="scheduleConfig.WORKDAY.endTime" format="HH:mm" value-format="HH:mm"
                    placeholder="结束时间" style="width: 140px" @change="handleScheduleTimeChange('WORKDAY')" />
                </div>
              </div>

              <!-- 周末配置 -->
              <div class="schedule-item">
                <div class="schedule-header">
                  <span class="schedule-type-label">周末</span>
                  <el-switch v-model="scheduleConfig.WEEKEND.enabled" @change="handleScheduleEnableChange('WEEKEND')" />
                </div>
                <div v-if="scheduleConfig.WEEKEND.enabled" class="schedule-time-picker">
                  <el-time-picker v-model="scheduleConfig.WEEKEND.startTime" format="HH:mm" value-format="HH:mm"
                    placeholder="开始时间" style="width: 140px" @change="handleScheduleTimeChange('WEEKEND')" />
                  <span class="time-separator">-</span>
                  <el-time-picker v-model="scheduleConfig.WEEKEND.endTime" format="HH:mm" value-format="HH:mm"
                    placeholder="结束时间" style="width: 140px" @change="handleScheduleTimeChange('WEEKEND')" />
                </div>
              </div>

              <!-- 节假日配置 -->
              <div class="schedule-item">
                <div class="schedule-header">
                  <span class="schedule-type-label">节假日</span>
                  <el-switch v-model="scheduleConfig.HOLIDAY.enabled" @change="handleScheduleEnableChange('HOLIDAY')" />
                </div>
                <div v-if="scheduleConfig.HOLIDAY.enabled" class="schedule-time-picker">
                  <el-time-picker v-model="scheduleConfig.HOLIDAY.startTime" format="HH:mm" value-format="HH:mm"
                    placeholder="开始时间" style="width: 140px" @change="handleScheduleTimeChange('HOLIDAY')" />
                  <span class="time-separator">-</span>
                  <el-time-picker v-model="scheduleConfig.HOLIDAY.endTime" format="HH:mm" value-format="HH:mm"
                    placeholder="结束时间" style="width: 140px" @change="handleScheduleTimeChange('HOLIDAY')" />
                </div>
              </div>

              <!-- 兼容性：保留原business_hours字段，将多时间段配置转换为字符串格式 -->
              <el-input v-model="venueForm.businessHours" type="hidden" style="display: none" />
            </div>
            <span class="field-hint modern-field-hint">可分别为工作日、周末、节假日设置不同的营业时间，开始时间必须小于结束时间（不允许跨夜）</span>
          </el-form-item>

          <el-form-item label="场馆描述" prop="description" class="form-item-enhanced modern-form-item">
            <el-input v-model="venueForm.description" type="textarea" :rows="4" placeholder="请输入场馆描述信息" maxlength="500"
              show-word-limit class="form-input modern-form-input" />
          </el-form-item>
        </div>

        <!-- 状态设置 -->
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">状态设置</h4>

          <el-form-item label="场馆状态" prop="status" class="form-item-enhanced modern-form-item">
            <div class="status-selector modern-status-selector">
              <el-radio-group v-model="venueForm.status" class="radio-group-enhanced modern-radio-group">
                <el-radio :label="1" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon enabled modern-status-icon">
                      <SuccessFilled />
                    </el-icon>
                    营业中
                  </span>
                </el-radio>
                <el-radio :label="2" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon warning modern-status-icon">
                      <Warning />
                    </el-icon>
                    暂停
                  </span>
                </el-radio>
                <el-radio :label="0" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon disabled modern-status-icon">
                      <CircleCloseFilled />
                    </el-icon>
                    关闭
                  </span>
                </el-radio>
              </el-radio-group>
            </div>
            <span class="field-hint modern-field-hint">关闭后场馆将不可用，暂停表示暂时停止服务</span>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-cancel modern-btn-cancel">
            取消
          </el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="btn-submit modern-btn-submit">
            {{ isEdit ? '更新场馆' : '创建场馆' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看场馆对话框 -->
    <el-dialog title="场馆详情" v-model="viewDialogVisible" width="700px" @close="handleViewDialogClose"
      class="modern-dialog">
      <el-tabs v-model="viewActiveTab" class="venue-view-tabs">
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="1" border :model="viewFormData" class="modern-descriptions">
            <el-descriptions-item label="场馆名称" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">场馆名称</span>
              </template>
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.venueName }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="场馆图片" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">场馆图片</span>
              </template>
              <template #default>
                <el-image v-if="viewFormData.venueImage" :src="viewFormData.venueImage"
                  :preview-src-list="[viewFormData.venueImage]" fit="cover" class="venue-detail-image"
                  preview-teleported />
                <span v-else class="modern-descriptions-value">无</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="地址" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">地址</span>
              </template>
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.address || '无' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="联系电话" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">联系电话</span>
              </template>
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.contactPhone || '无' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="联系人" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">联系人</span>
              </template>
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.contactPerson || '无' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="营业时间" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">营业时间</span>
              </template>
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.businessHours || '无' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="场馆描述" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">场馆描述</span>
              </template>
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.description || '无' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="状态" class="modern-descriptions-item">
              <template #label>
                <span class="modern-descriptions-label">状态</span>
              </template>
              <template #default>
                <el-tag :type="getStatusType(viewFormData.status)" size="small">
                  {{ getStatusText(viewFormData.status) }}
                </el-tag>
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
        </el-tab-pane>
        <el-tab-pane label="状态历史" name="statusLogs">
          <div class="status-logs-container">
            <el-timeline v-if="statusLogs.length > 0">
              <el-timeline-item v-for="log in statusLogs" :key="log.id" :timestamp="formatDateTime(log.createTime)"
                placement="top" :type="getStatusLogType(log.newStatus)">
                <el-card>
                  <h4>状态变更：{{ getStatusText(log.oldStatus) }} → {{ getStatusText(log.newStatus) }}</h4>
                  <p v-if="log.operatorName" class="log-operator">操作人：{{ log.operatorName }}</p>
                  <p v-if="log.changeReason" class="log-reason">变更原因：{{ log.changeReason }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无状态变更记录" />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <span class="dialog-footer modern-dialog-footer">
          <el-button v-if="userRole === 'PRESIDENT' || (userRole === 'VENUE_MANAGER' && viewFormData.id === currentUserVenueId)" type="success" @click="handleEditFromView(viewFormData)" class="modern-btn-primary">编辑</el-button>
          <el-button @click="viewDialogVisible = false" class="modern-btn-cancel">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  View,
  OfficeBuilding,
  Location,
  Phone,
  User,
  Clock,
  SuccessFilled,
  CircleCloseFilled,
  Warning,
  Picture,
  UploadFilled,
  ZoomIn
} from '@element-plus/icons-vue'
import {
  getVenueList,
  addVenue,
  updateVenue,
  deleteVenue,
  getVenueInfo,
  uploadVenueImage,
  uploadVenueImages,
  getVenueImages,
  bindVenueImages,
  deleteVenueImage,
  updateImageSortOrder,
  getVenueSchedules,
  saveVenueSchedules,
  deleteVenueSchedule,
  getVenueStatistics,
  getVenueStatusLogs
} from '@/api/venue'

// 搜索表单
const searchForm = reactive({
  venueName: '',
  address: '',
  status: null
})

// 场馆列表
const venueList = ref([])
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
  operating: 0,
  paused: 0,
  closed: 0
})

// 表格高度（用于虚拟滚动）
const tableHeight = ref(600)

// 对话框
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const viewActiveTab = ref('info')
const dialogTitle = ref('添加场馆')
const isEdit = ref(false)
const submitLoading = ref(false)
const venueFormRef = ref(null)
const imageUploading = ref(false)

// 状态变更日志
const statusLogs = ref([])
const statusLogsLoading = ref(false)

// 多图片管理
const mainImageUrl = ref('')
const detailImagesFileList = ref([])
const detailImages = ref([]) // 存储详情图数据
const mainImageRecordId = ref(null) // 主图在图片表中的记录ID（用于删除持久化）

const buildBusinessHoursDisplayFromSchedules = (schedules) => {
  const normalizeTime = (t) => {
    if (!t) return ''
    if (typeof t === 'string') return t.substring(0, 5)
    if (typeof t === 'object' && t.hour !== undefined) {
      const hour = String(t.hour).padStart(2, '0')
      const minute = String(t.minute).padStart(2, '0')
      return `${hour}:${minute}`
    }
    return String(t).substring(0, 5)
  }

  const labelMap = {
    WORKDAY: '工作日',
    WEEKEND: '周末',
    HOLIDAY: '节假日'
  }

  const parts = (Array.isArray(schedules) ? schedules : [])
    .filter(s => (s?.isActive === 1 || s?.isActive === true) && s?.scheduleType)
    .map(s => {
      const start = normalizeTime(s.startTime)
      const end = normalizeTime(s.endTime)
      if (!start || !end) return ''
      return `${labelMap[s.scheduleType] || s.scheduleType}:${start}-${end}`
    })
    .filter(Boolean)

  return parts.join('; ')
}

// 获取当前用户角色
const userRole = computed(() => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr && userInfoStr !== 'null') {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo.role || null
    }
  } catch (e) {
    console.error('获取用户角色失败:', e)
  }
  return null
})

// 当前用户所属场馆ID（用于判断场馆管理员是否为当前行场馆的管理员）
const currentUserVenueId = computed(() => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr && userInfoStr !== 'null') {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo.venueId || null
    }
  } catch (e) {
    console.error('获取当前用户场馆ID失败:', e)
  }
  return null
})

// 营业时间配置
const scheduleConfig = reactive({
  WORKDAY: {
    enabled: false,
    startTime: null,
    endTime: null
  },
  WEEKEND: {
    enabled: false,
    startTime: null,
    endTime: null
  },
  HOLIDAY: {
    enabled: false,
    startTime: null,
    endTime: null
  }
})

// 上传前校验
const beforeImageUpload = (file) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  const isAllowed = allowedTypes.includes(file.type)
  if (!isAllowed) {
    ElMessage.error('图片格式不支持，请上传 jpg/jpeg/png/webp 格式')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 <= 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 自定义上传（单图片 - 向后兼容）
const handleImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  imageUploading.value = true
  try {
    const res = await uploadVenueImage(file, venueForm.id || null)
    // request.js 已保证 code===200 才会返回
    const imageData = res.data || res
    venueForm.venueImage = imageData.url || imageData
    mainImageUrl.value = imageData.url || imageData
    ElMessage.success('图片上传成功')
    onSuccess && onSuccess(res)
  } catch (e) {
    console.error('上传图片失败:', e)
    ElMessage.error(e?.message || '图片上传失败')
    onError && onError(e)
  } finally {
    imageUploading.value = false
  }
}

// 主图上传
const handleMainImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  imageUploading.value = true
  try {
    const res = await uploadVenueImage(file, venueForm.id || null)
    const imageData = res.data || res
    const url = imageData.url || imageData
    mainImageUrl.value = url
    venueForm.venueImage = url // 保持向后兼容
    ElMessage.success('主图上传成功')
    onSuccess && onSuccess(res)
  } catch (e) {
    console.error('上传主图失败:', e)
    ElMessage.error(e?.message || '上传主图失败')
    onError && onError(e)
  } finally {
    imageUploading.value = false
  }
}

// 详情图上传
const handleDetailImagesUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const files = [file]
    // 如果有场馆ID，直接关联保存；否则先上传，后续在提交时保存
    const venueId = venueForm.id || null
    const res = await uploadVenueImages(files, venueId, 'DETAIL')
    if (res.code === 200 && res.data && res.data.images && res.data.images.length > 0) {
      const imageInfo = res.data.images[0]
      const url = imageInfo.url || imageInfo.imageUrl
      const thumbUrl = imageInfo.thumbnailUrl || imageInfo.thumbUrl || url
      const fileItem = {
        uid: imageInfo.id || Date.now() + Math.random(),
        name: file.name,
        url,
        thumbnailUrl: thumbUrl,
        response: imageInfo
      }
      detailImagesFileList.value.push(fileItem)
      detailImages.value.push({
        id: imageInfo.id,
        url,
        imageType: 'DETAIL',
        sortOrder: detailImages.value.length
      })
      ElMessage.success('详情图上传成功')
      onSuccess && onSuccess(res)
    } else {
      throw new Error('上传失败')
    }
  } catch (e) {
    console.error('上传详情图失败:', e)
    ElMessage.error(e?.message || '上传详情图失败')
    onError && onError(e)
  }
}

// 移除主图
const handleRemoveMainImage = () => {
  // 编辑态：如果主图在图片表中有记录，删除以确保持久化
  if (venueForm.id && mainImageRecordId.value) {
    deleteVenueImage(mainImageRecordId.value).catch((e) => {
      console.error('删除主图记录失败:', e)
    })
  }
  mainImageRecordId.value = null
  mainImageUrl.value = ''
  venueForm.venueImage = ''
}

// 移除详情图
const handleRemoveDetailImage = async (file, fileList) => {
  // 有后端记录ID则调用删除接口，避免刷新后“复活”
  const id = file?.response?.id ?? file?.response?.imageId ?? (typeof file?.uid === 'number' ? file.uid : null)
  if (venueForm.id && id) {
    try {
      await deleteVenueImage(id)
      ElMessage.success('图片已删除')
    } catch (e) {
      console.error('删除详情图失败:', e)
      ElMessage.error(e?.message || '删除图片失败')
      // 删除失败时不更新本地列表，避免 UI 与后端不一致
      return
    }
  }

  detailImagesFileList.value = fileList
  detailImages.value = detailImages.value.filter(img => (img.id ? img.id !== id : img.url !== file.url))
}

// 详情图删除按钮点击（卡片内垃圾桶图标）
// 说明：
// - 某些情况下，Element Plus 内部的 handleRemove 与 :on-remove 回调联动不稳定
// - 这里显式计算“删除后的文件列表”，并复用 handleRemoveDetailImage，保证：
//   1）UI 列表数量立即减少；2）已持久化的图片会调用后端删除接口
const handleDetailImageDeleteClick = (file) => {
  const newFileList = detailImagesFileList.value.filter(item => item.uid !== file.uid)
  handleRemoveDetailImage(file, newFileList)
}

// 拖拽排序相关
const dragStartIndex = ref(-1)
const dragOverIndex = ref(-1)

// 开始拖拽
const handleDragStart = (event, file) => {
  const index = detailImagesFileList.value.findIndex(item => item.uid === file.uid)
  dragStartIndex.value = index
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', '')
  // 添加拖拽样式
  if (event.target.closest) {
    const target = event.target.closest('.detail-image-item')
    if (target) {
      target.style.opacity = '0.5'
    }
  }
}

// 拖拽经过
const handleDragOver = (event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'move'
}

// 放置
const handleDrop = async (event, file) => {
  event.preventDefault()
  const dropIndex = detailImagesFileList.value.findIndex(item => item.uid === file.uid)
  
  if (dragStartIndex.value === -1 || dragStartIndex.value === dropIndex) {
    return
  }
  
  // 更新列表顺序
  const newFileList = [...detailImagesFileList.value]
  const [movedItem] = newFileList.splice(dragStartIndex.value, 1)
  newFileList.splice(dropIndex, 0, movedItem)
  detailImagesFileList.value = newFileList
  
  // 同步更新 detailImages 的顺序
  const newDetailImages = [...detailImages.value]
  const [movedImage] = newDetailImages.splice(dragStartIndex.value, 1)
  newDetailImages.splice(dropIndex, 0, movedImage)
  detailImages.value = newDetailImages
  
  // 如果已有场馆ID，立即更新后端排序
  if (venueForm.id) {
    try {
      // 重新获取图片列表以获取最新的ID
      const imagesResponse = await getVenueImages(venueForm.id)
      if (imagesResponse.code === 200) {
        const allImages = imagesResponse.data || []
        const detailImagesFromDB = allImages.filter(img => img.imageType === 'DETAIL')
        
        // 按照新的顺序更新排序
        for (let i = 0; i < detailImagesFromDB.length; i++) {
          const img = detailImagesFromDB[i]
          const fileItem = newFileList.find(f => f.url === img.imageUrl)
          if (fileItem && img.sortOrder !== i) {
            try {
              await updateImageSortOrder(img.id, i)
            } catch (error) {
              console.error(`更新图片${img.id}排序失败:`, error)
            }
          }
        }
      }
    } catch (error) {
      console.error('更新图片排序失败:', error)
    }
  }
}

// 拖拽结束
const handleDragEnd = (event) => {
  dragStartIndex.value = -1
  dragOverIndex.value = -1
  // 恢复样式
  if (event.target.closest) {
    const target = event.target.closest('.detail-image-item')
    if (target) {
      target.style.opacity = '1'
    }
  }
}

// 预览图片
const handlePreviewImage = (file) => {
  const imageUrl = file.url || file.thumbnailUrl
  if (imageUrl) {
    // 使用Element Plus的图片预览功能
    window.open(imageUrl, '_blank')
  }
}

// 移除图片（向后兼容）
const handleRemoveImage = () => {
  handleRemoveMainImage()
}

// 查看表单数据
const viewFormData = reactive({
  id: null,
  venueName: '',
  venueImage: '',
  address: '',
  contactPhone: '',
  contactPerson: '',
  businessHours: '',
  description: '',
  status: 1,
  createTime: ''
})

// 场馆表单
const venueForm = reactive({
  id: null,
  venueName: '',
  venueImage: '',
  address: '',
  contactPhone: '',
  contactPerson: '',
  businessHours: '',
  description: '',
  status: 1
})

// 营业时间配置变化处理
const handleScheduleEnableChange = (type) => {
  if (!scheduleConfig[type].enabled) {
    scheduleConfig[type].startTime = null
    scheduleConfig[type].endTime = null
  }
  updateBusinessHoursString()
  // businessHours 为隐藏字段，主动清理校验提示避免残留
  venueFormRef.value?.clearValidate?.(['businessHours'])
}

const handleScheduleTimeChange = (type) => {
  const config = scheduleConfig[type]
  // 验证开始时间必须小于结束时间
  if (config.startTime && config.endTime) {
    const start = parseTimeString(config.startTime)
    const end = parseTimeString(config.endTime)
    if (start >= end) {
      ElMessage.warning(`${type === 'WORKDAY' ? '工作日' : type === 'WEEKEND' ? '周末' : '节假日'}：开始时间必须小于结束时间（不允许跨夜）`)
      config.endTime = null
      updateBusinessHoursString()
      venueFormRef.value?.clearValidate?.(['businessHours'])
      return
    }
  }
  updateBusinessHoursString()
  venueFormRef.value?.clearValidate?.(['businessHours'])
}

// 解析时间字符串为Date对象用于比较
const parseTimeString = (timeStr) => {
  if (!timeStr) return null
  const [hours, minutes] = timeStr.split(':').map(Number)
  return new Date(2000, 0, 1, hours, minutes)
}

// 更新businessHours字符串（向后兼容）
const updateBusinessHoursString = () => {
  const parts = []
  if (scheduleConfig.WORKDAY.enabled && scheduleConfig.WORKDAY.startTime && scheduleConfig.WORKDAY.endTime) {
    parts.push(`工作日:${scheduleConfig.WORKDAY.startTime}-${scheduleConfig.WORKDAY.endTime}`)
  }
  if (scheduleConfig.WEEKEND.enabled && scheduleConfig.WEEKEND.startTime && scheduleConfig.WEEKEND.endTime) {
    parts.push(`周末:${scheduleConfig.WEEKEND.startTime}-${scheduleConfig.WEEKEND.endTime}`)
  }
  if (scheduleConfig.HOLIDAY.enabled && scheduleConfig.HOLIDAY.startTime && scheduleConfig.HOLIDAY.endTime) {
    parts.push(`节假日:${scheduleConfig.HOLIDAY.startTime}-${scheduleConfig.HOLIDAY.endTime}`)
  }
  venueForm.businessHours = parts.join('; ') || ''
}

// 生成提交给后端的旧版 businessHours（后端只认 HH:mm-HH:mm）
const buildLegacyBusinessHoursForSubmit = () => {
  const pick = (cfg) =>
    cfg.enabled && cfg.startTime && cfg.endTime ? `${cfg.startTime}-${cfg.endTime}` : ''

  // 优先级：工作日 > 周末 > 节假日
  return pick(scheduleConfig.WORKDAY) || pick(scheduleConfig.WEEKEND) || pick(scheduleConfig.HOLIDAY) || ''
}

// 从businessHours字符串加载到配置
const loadBusinessHoursFromString = (businessHours) => {
  if (!businessHours) return

  // 尝试解析多时间段格式
  const parts = businessHours.split(';')
  parts.forEach(part => {
    const trimmed = part.trim()
    if (trimmed.includes('工作日:')) {
      const timePart = trimmed.replace('工作日:', '')
      const [start, end] = timePart.split('-')
      if (start && end) {
        scheduleConfig.WORKDAY.enabled = true
        scheduleConfig.WORKDAY.startTime = start.trim()
        scheduleConfig.WORKDAY.endTime = end.trim()
      }
    } else if (trimmed.includes('周末:')) {
      const timePart = trimmed.replace('周末:', '')
      const [start, end] = timePart.split('-')
      if (start && end) {
        scheduleConfig.WEEKEND.enabled = true
        scheduleConfig.WEEKEND.startTime = start.trim()
        scheduleConfig.WEEKEND.endTime = end.trim()
      }
    } else if (trimmed.includes('节假日:')) {
      const timePart = trimmed.replace('节假日:', '')
      const [start, end] = timePart.split('-')
      if (start && end) {
        scheduleConfig.HOLIDAY.enabled = true
        scheduleConfig.HOLIDAY.startTime = start.trim()
        scheduleConfig.HOLIDAY.endTime = end.trim()
      }
    } else if (trimmed.includes('-')) {
      // 兼容旧格式：直接是时间范围
      const [start, end] = trimmed.split('-')
      if (start && end) {
        scheduleConfig.WORKDAY.enabled = true
        scheduleConfig.WORKDAY.startTime = start.trim()
        scheduleConfig.WORKDAY.endTime = end.trim()
      }
    }
  })
}

// 表单验证规则
const venueFormRules = {
  venueName: [
    { required: true, message: '请输入场馆名称', trigger: 'blur' },
    { max: 100, message: '场馆名称长度不能超过100个字符', trigger: 'blur' }
  ],
  venueImage: [
    { max: 255, message: '图片URL长度不能超过255个字符', trigger: 'blur' }
  ],
  contactPerson: [
    {
      validator: (rule, value, callback) => {
        const contactPerson = value
        const contactPhone = venueForm.contactPhone
        if ((!contactPerson || contactPerson.trim() === '') && (!contactPhone || contactPhone.trim() === '')) {
          callback(new Error('联系人和联系电话至少填写一个'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  contactPhone: [
    {
      validator: (rule, value, callback) => {
        const contactPerson = venueForm.contactPerson
        const contactPhone = value
        if ((!contactPerson || contactPerson.trim() === '') && (!contactPhone || contactPhone.trim() === '')) {
          callback(new Error('联系人和联系电话至少填写一个'))
          return
        }
        if (!contactPhone || contactPhone.trim() === '') {
          callback()
          return
        }
        const mobilePattern = /^1[3-9]\d{9}$/
        const landlinePattern = /^0\d{2,3}-?\d{7,8}$/
        if (mobilePattern.test(contactPhone) || landlinePattern.test(contactPhone)) {
          callback()
        } else {
          callback(new Error('请输入正确的手机号或固定电话'))
        }
      },
      trigger: 'blur'
    }
  ],
  businessHours: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.trim() === '') {
          callback()
          return
        }
        /**
         * 兼容两种营业时间字符串：
         * - 旧格式：HH:mm-HH:mm（如：09:00-22:00）
         * - 新格式：工作日:HH:mm-HH:mm; 周末:HH:mm-HH:mm; 节假日:HH:mm-HH:mm（分号分隔，可有空格）
         */
        const timeRangePattern = /^([01]\d|2[0-3]):[0-5]\d-([01]\d|2[0-3]):[0-5]\d$/
        const labeledPattern = /^(工作日|周末|节假日):([01]\d|2[0-3]):[0-5]\d-([01]\d|2[0-3]):[0-5]\d$/

        const assertStartBeforeEnd = (startTime, endTime) => {
          const start = parseTimeString(startTime)
          const end = parseTimeString(endTime)
          return !!(start && end && start < end)
        }

        const raw = value.trim()

        // 旧格式直接通过
        if (timeRangePattern.test(raw)) {
          const [startTime, endTime] = raw.split('-').map(s => s.trim())
          if (!assertStartBeforeEnd(startTime, endTime)) {
            callback(new Error('开始时间必须小于结束时间（不允许跨夜）'))
            return
          }
          callback()
          return
        }

        // 新格式：分号分段
        const parts = raw
          .split(';')
          .map(s => s.trim())
          .filter(Boolean)

        if (parts.length === 0) {
          callback()
          return
        }

        for (const part of parts) {
          if (!labeledPattern.test(part)) {
            callback(new Error('营业时间格式不正确，格式应为：HH:mm-HH:mm（如：09:00-22:00）'))
            return
          }
          const timePart = part.replace(/^(工作日|周末|节假日):/, '')
          const [startTime, endTime] = timePart.split('-').map(s => s.trim())
          if (!assertStartBeforeEnd(startTime, endTime)) {
            callback(new Error('开始时间必须小于结束时间（不允许跨夜）'))
            return
          }
        }

        callback()
      },
      // 由开关/时间选择器驱动，使用 change 更稳定
      trigger: 'change'
    }
  ],
  description: [
    { max: 500, message: '描述字段长度不能超过500个字符', trigger: 'blur' }
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '关闭',
    1: '营业中',
    2: '暂停'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 加载场馆列表
const loadVenueList = async () => {
  loading.value = true
  try {
    const params = {
      venueName: searchForm.venueName || null,
      address: searchForm.address || null,
      status: searchForm.status,
      page: pagination.page,
      size: pagination.size
    }
    const response = await getVenueList(params)
    if (response.code === 200) {
      // 确保 venueList 始终是数组，防止 el-table 报错 "data2 is not iterable"
      const data = response?.data?.data
      venueList.value = Array.isArray(data) ? data : (Array.isArray(response?.data) ? response.data : [])
      pagination.total = response?.data?.total || 0
    } else {
      ElMessage.error(response.message || '获取场馆列表失败')
      venueList.value = []
    }
  } catch (error) {
    console.error('加载场馆列表失败:', error)
    ElMessage.error('加载场馆列表失败，请稍后重试')
    venueList.value = []  // 出错时重置为空数组
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await getVenueStatistics()
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
  loadVenueList()
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadVenueList()
}

// 重置
const handleReset = () => {
  searchForm.venueName = ''
  searchForm.address = ''
  searchForm.status = null
  pagination.page = 1
  loadVenueList()
}

// 添加场馆
const handleAdd = () => {
  dialogTitle.value = '添加场馆'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑场馆
const handleEdit = async (row) => {
  dialogTitle.value = '编辑场馆'
  isEdit.value = true
  Object.assign(venueForm, {
    id: row.id,
    venueName: row.venueName || '',
    venueImage: row.venueImage || '',
    address: row.address || '',
    contactPhone: row.contactPhone || '',
    contactPerson: row.contactPerson || '',
    businessHours: row.businessHours || '',
    description: row.description || '',
    status: row.status !== undefined ? row.status : 1
  })

  // 加载主图
  mainImageUrl.value = row.venueImage || ''
  mainImageRecordId.value = null

  // 加载详情图
  try {
    const imagesResponse = await getVenueImages(row.id)
    if (imagesResponse.code === 200) {
      const images = imagesResponse.data || []
      const mainImages = images.filter(img => img.imageType === 'MAIN')
      const detailImagesList = images.filter(img => img.imageType === 'DETAIL')

      // 如果有主图，使用第一个主图
      if (mainImages.length > 0 && !mainImageUrl.value) {
        mainImageUrl.value = mainImages[0].imageUrl
        venueForm.venueImage = mainImages[0].imageUrl
      }
      // 保存主图记录ID，方便后续删除
      if (mainImages.length > 0) {
        const matched = mainImages.find(m => m.imageUrl === mainImageUrl.value) || mainImages[0]
        mainImageRecordId.value = matched?.id ?? null
      }

      // 加载详情图列表
      detailImages.value = detailImagesList
      detailImagesFileList.value = detailImagesList.map((img, index) => ({
        uid: img.id || Date.now() + index,
        name: `image_${img.id}.jpg`,
        url: img.imageUrl,
        thumbnailUrl: img.imageUrl.replace(/(\.[^.]+)$/, '_thumb$1'),
        response: img
      }))
    }
  } catch (error) {
    console.error('加载图片失败:', error)
  }

  // 加载营业时间配置
  try {
    const venueId = row.id
    if (!venueId) {
      console.warn('当前场馆ID为空，跳过营业时间配置加载')
    } else {
      const schedulesResponse = await getVenueSchedules(venueId)
      if (schedulesResponse.code === 200) {
        const schedules = schedulesResponse.data || []
      // 重置配置
      scheduleConfig.WORKDAY.enabled = false
      scheduleConfig.WORKDAY.startTime = null
      scheduleConfig.WORKDAY.endTime = null
      scheduleConfig.WEEKEND.enabled = false
      scheduleConfig.WEEKEND.startTime = null
      scheduleConfig.WEEKEND.endTime = null
      scheduleConfig.HOLIDAY.enabled = false
      scheduleConfig.HOLIDAY.startTime = null
      scheduleConfig.HOLIDAY.endTime = null

        // 加载配置
        schedules.forEach(schedule => {
          const type = schedule.scheduleType
          if (scheduleConfig[type]) {
            scheduleConfig[type].enabled = schedule.isActive === 1
            if (schedule.startTime) {
              // 处理LocalTime格式：可能是字符串或对象
              let startTimeStr = schedule.startTime
              if (typeof startTimeStr === 'object' && startTimeStr.hour !== undefined) {
                // 如果是LocalTime对象，转换为字符串
                const hour = String(startTimeStr.hour).padStart(2, '0')
                const minute = String(startTimeStr.minute).padStart(2, '0')
                startTimeStr = `${hour}:${minute}`
              } else if (typeof startTimeStr === 'string') {
                // 如果已经是字符串，确保格式正确
                startTimeStr = startTimeStr.substring(0, 5)
              }
              scheduleConfig[type].startTime = startTimeStr
            }
            if (schedule.endTime) {
              // 处理LocalTime格式
              let endTimeStr = schedule.endTime
              if (typeof endTimeStr === 'object' && endTimeStr.hour !== undefined) {
                const hour = String(endTimeStr.hour).padStart(2, '0')
                const minute = String(endTimeStr.minute).padStart(2, '0')
                endTimeStr = `${hour}:${minute}`
              } else if (typeof endTimeStr === 'string') {
                endTimeStr = endTimeStr.substring(0, 5)
              }
              scheduleConfig[type].endTime = endTimeStr
            }
          }
        })

        // 如果没有加载到任何配置，从businessHours字符串加载（向后兼容）
        if (schedules.length === 0) {
          loadBusinessHoursFromString(row.businessHours || '')
        }
      } else {
        // 如果没有新格式的配置，从businessHours字符串加载
        loadBusinessHoursFromString(row.businessHours || '')
      }
    }
  } catch (error) {
    console.error('加载营业时间配置失败:', error)
    // 失败时从businessHours字符串加载
    loadBusinessHoursFromString(row.businessHours || '')
  }

  dialogVisible.value = true
}

// 从查看对话框切换到编辑对话框
const handleEditFromView = (row) => {
  try {
    viewDialogVisible.value = false
    // 复用编辑逻辑
    handleEdit(row)
  } catch (e) {
    console.error('从查看切换到编辑失败:', e)
  }
}

// 查看场馆
const handleView = async (row) => {
  try {
    const response = await getVenueInfo(row.id)
    if (response.code === 200) {
      Object.assign(viewFormData, {
        id: response.data.id,
        venueName: response.data.venueName || '',
        venueImage: response.data.venueImage || '',
        address: response.data.address || '',
        contactPhone: response.data.contactPhone || '',
        contactPerson: response.data.contactPerson || '',
        businessHours: response.data.businessHours || '',
        description: response.data.description || '',
        status: response.data.status !== undefined ? response.data.status : 1,
        createTime: response.data.createTime || ''
      })
      viewActiveTab.value = 'info'
      viewDialogVisible.value = true
      // schedules 优先：保证“获取营业时间”展示能随配置更新
      try {
        const venueId = response.data.id
        if (venueId) {
          const schedulesResp = await getVenueSchedules(venueId)
          if (schedulesResp.code === 200) {
            const display = buildBusinessHoursDisplayFromSchedules(schedulesResp.data || [])
            if (display) viewFormData.businessHours = display
          }
        } else {
          console.warn('查看场馆时ID为空，跳过营业时间配置展示加载')
        }
      } catch (e) {
        console.error('加载营业时间配置用于展示失败:', e)
      }
      // 加载状态变更历史
      loadStatusLogs(response.data.id)
    } else {
      ElMessage.error(response.message || '获取场馆详情失败')
    }
  } catch (error) {
    console.error('获取场馆详情失败:', error)
    ElMessage.error('获取场馆详情失败，请稍后重试')
  }
}

// 加载状态变更历史
const loadStatusLogs = async (venueId) => {
  statusLogsLoading.value = true
  try {
    const response = await getVenueStatusLogs(venueId)
    if (response.code === 200) {
      statusLogs.value = response.data || []
    }
  } catch (error) {
    console.error('加载状态变更历史失败:', error)
  } finally {
    statusLogsLoading.value = false
  }
}

// 获取状态日志类型（用于时间线样式）
const getStatusLogType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 删除场馆
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除场馆 "${row.venueName}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteVenue(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadVenueList()
        loadStatistics() // 刷新统计数据
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除场馆失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => { })
}

// 提交表单
const handleSubmit = async () => {
  if (!venueFormRef.value) return

  await venueFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 更新主图URL（向后兼容）
        if (mainImageUrl.value) {
          venueForm.venueImage = mainImageUrl.value
        }

        // 更新营业时间字符串（前端展示/兼容字段）
        updateBusinessHoursString()

        // 注意：后端的 businessHours 仍只接受旧格式 HH:mm-HH:mm
        // 三段配置会通过 /schedules 接口另存，这里仅为通过后端对 businessHours 的校验
        const formData = { ...venueForm, businessHours: buildLegacyBusinessHoursForSubmit() }

        // 提交场馆基本信息
        const response = isEdit.value
          ? await updateVenue(formData)
          : await addVenue(formData)

        if (response.code === 200) {
          let venueId = null

          if (isEdit.value) {
            // 编辑时使用表单中的ID
            venueId = formData.id
          } else {
            // 新增时从响应中获取ID（后端已返回）
            if (response.data && response.data.id) {
              venueId = response.data.id
            } else if (formData.id) {
              // 如果响应中没有ID，使用表单中的ID（后端设置）
              venueId = formData.id
            } else {
              // 如果还是没有，通过场馆名称查询获取ID（临时方案）
              try {
                const listResponse = await getVenueList({
                  venueName: formData.venueName,
                  page: 1,
                  size: 1
                })
                if (listResponse.code === 200 && listResponse.data && listResponse.data.data && listResponse.data.data.length > 0) {
                  venueId = listResponse.data.data[0].id
                }
              } catch (error) {
                console.error('获取场馆ID失败:', error)
              }
            }
          }

          // 保存图片和营业时间配置
          if (venueId) {
            try {
              await saveVenueImages(venueId)
              await saveVenueSchedulesConfig(venueId)
            } catch (error) {
              console.error('保存图片或营业时间配置失败:', error)
              // 不影响主流程，只记录错误
            }
          }

          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadVenueList()
          loadStatistics() // 刷新统计数据
        } else {
          ElMessage.error(response.message || (isEdit.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 保存场馆图片配置
const saveVenueImages = async (venueId) => {
  try {
    // 获取现有图片列表
    const imagesResponse = await getVenueImages(venueId)
    const existingImages = imagesResponse.code === 200 ? (imagesResponse.data || []) : []
    const existingMainImages = existingImages.filter(img => img.imageType === 'MAIN')
    const existingDetailImages = existingImages.filter(img => img.imageType === 'DETAIL')

    // 保存主图到图片表（如果存在且与venue_image字段一致）
    if (mainImageUrl.value) {
      // 检查主图是否已存在于图片表中
      const mainImageExists = existingMainImages.some(img => img.imageUrl === mainImageUrl.value)
      if (!mainImageExists) {
        // 如果主图不存在于图片表，需要保存
        // 注意：由于主图文件已上传，这里需要通过API保存图片记录
        // 简化处理：主图主要保存在venue_image字段，图片表作为补充
        // 如果需要完整功能，可以添加专门的保存图片记录接口
      }
    }

    // 保存详情图到图片表：
    // - 编辑场馆时，上传接口会直接带上 venueId，图片会立刻入库，这里主要负责排序
    // - 新增场馆时，上传时还没有 venueId，只上传物理文件并返回 URL，这里需要根据 URL 补充入库绑定
    if (detailImages.value.length > 0) {
      // 1）先检查数据库中已经存在的详情图，找出哪些是“还没入库但在前端列表里”的图片
      const existingDetailUrls = existingDetailImages.map(img => img.imageUrl)
      const pendingBindImages = detailImages.value
        .map((img, index) => ({
          imageUrl: img.url || img.imageUrl,
          imageType: img.imageType || 'DETAIL',
          sortOrder: index
        }))
        .filter(img => img.imageUrl && !existingDetailUrls.includes(img.imageUrl))

      // 对于新增场馆：所有详情图都不在 existingDetailUrls 中，会在这里一次性绑定入库
      if (pendingBindImages.length > 0) {
        try {
          await bindVenueImages(venueId, pendingBindImages)
        } catch (error) {
          console.error('绑定场馆详情图失败:', error)
        }
      }

      // 2）重新加载图片列表以获取最新的ID，并统一更新排序
      const updatedImagesResponse = await getVenueImages(venueId)
      if (updatedImagesResponse.code === 200) {
        const allImages = updatedImagesResponse.data || []
        const detailImagesFromDB = allImages.filter(img => img.imageType === 'DETAIL')

        // 更新排序：以数据库中的顺序为准，从 0 开始重排
        for (let i = 0; i < detailImagesFromDB.length; i++) {
          const img = detailImagesFromDB[i]
          if (img.sortOrder !== i) {
            try {
              await updateImageSortOrder(img.id, i)
            } catch (error) {
              console.error(`更新图片${img.id}排序失败:`, error)
            }
          }
        }
      }
    }
  } catch (error) {
    console.error('保存图片配置失败:', error)
    // 不影响主流程，只记录错误
  }
}

// 保存场馆营业时间配置
const saveVenueSchedulesConfig = async (venueId) => {
  try {
    if (!venueId) {
      console.warn('保存营业时间配置时场馆ID为空，已跳过')
      return
    }
    const schedules = []

    if (scheduleConfig.WORKDAY.enabled && scheduleConfig.WORKDAY.startTime && scheduleConfig.WORKDAY.endTime) {
      schedules.push({
        scheduleType: 'WORKDAY',
        startTime: scheduleConfig.WORKDAY.startTime,
        endTime: scheduleConfig.WORKDAY.endTime,
        isActive: 1
      })
    }

    if (scheduleConfig.WEEKEND.enabled && scheduleConfig.WEEKEND.startTime && scheduleConfig.WEEKEND.endTime) {
      schedules.push({
        scheduleType: 'WEEKEND',
        startTime: scheduleConfig.WEEKEND.startTime,
        endTime: scheduleConfig.WEEKEND.endTime,
        isActive: 1
      })
    }

    if (scheduleConfig.HOLIDAY.enabled && scheduleConfig.HOLIDAY.startTime && scheduleConfig.HOLIDAY.endTime) {
      schedules.push({
        scheduleType: 'HOLIDAY',
        startTime: scheduleConfig.HOLIDAY.startTime,
        endTime: scheduleConfig.HOLIDAY.endTime,
        isActive: 1
      })
    }

    if (schedules.length > 0) {
      await saveVenueSchedules(venueId, schedules)
      return
    }

    // schedules 为空：视为“删除营业时间配置”，需要清理后端持久化记录
    const existingResp = await getVenueSchedules(venueId)
    if (existingResp.code === 200) {
      const existing = existingResp.data || []
      const deletions = existing
        .filter(s => s?.id)
        .map(s => deleteVenueSchedule(s.id).catch((e) => console.error(`删除营业时间记录 ${s.id} 失败:`, e)))
      await Promise.all(deletions)
    }
  } catch (error) {
    console.error('保存营业时间配置失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(venueForm, {
    id: null,
    venueName: '',
    venueImage: '',
    address: '',
    contactPhone: '',
    contactPerson: '',
    businessHours: '',
    description: '',
    status: 1
  })

  // 重置图片
  mainImageUrl.value = ''
  mainImageRecordId.value = null
  detailImagesFileList.value = []
  detailImages.value = []

  // 重置营业时间配置
  scheduleConfig.WORKDAY.enabled = false
  scheduleConfig.WORKDAY.startTime = null
  scheduleConfig.WORKDAY.endTime = null
  scheduleConfig.WEEKEND.enabled = false
  scheduleConfig.WEEKEND.startTime = null
  scheduleConfig.WEEKEND.endTime = null
  scheduleConfig.HOLIDAY.enabled = false
  scheduleConfig.HOLIDAY.startTime = null
  scheduleConfig.HOLIDAY.endTime = null

  if (venueFormRef.value) {
    venueFormRef.value.resetFields()
  }
}

// 关闭查看对话框
const handleViewDialogClose = () => {
  Object.assign(viewFormData, {
    id: null,
    venueName: '',
    venueImage: '',
    address: '',
    contactPhone: '',
    contactPerson: '',
    businessHours: '',
    description: '',
    status: 1,
    createTime: ''
  })
  statusLogs.value = []
  viewActiveTab.value = 'info'
}

// 关闭编辑对话框
const handleDialogClose = () => {
  resetForm()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadVenueList()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  loadVenueList()
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
  // 根据窗口高度动态计算表格高度
  const windowHeight = window.innerHeight
  tableHeight.value = Math.max(400, windowHeight - 400)
}

// 初始化
onMounted(() => {
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
  loadVenueList()
  loadStatistics()
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('resize', calculateTableHeight)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');

.venue-management {
  padding: 20px;
  background-color: var(--color-background, #f0f2f5);
  height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
}

.venue-management::-webkit-scrollbar {
  width: 8px;
}

.venue-management::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.venue-management::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 4px;
}

.venue-management::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted, #94A3B8);
}

.venue-management-background {
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

.venue-management-content {
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
  width: 80px;
  min-width: 80px;
  max-width: 80px;
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
  width: 200px;
}

.search-select {
  width: 150px;
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

/**
 * 按钮悬停上浮效果（与场地管理页面保持一致的交互感）
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
}

.table-card :deep(.el-table td) {
  border-bottom: 1px solid var(--color-border, #E2E8F0) !important;
  padding: 14px 0 !important;
  color: var(--color-text-primary, #1E293B);
}

.table-card :deep(.el-table tr:hover > td) {
  background: var(--color-background, #F8FAFC) !important;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}

.venue-thumb {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  border: 1px solid var(--color-border, #E2E8F0);
  overflow: hidden;
  background: var(--color-card-bg, #FFFFFF);
}

.venue-thumb--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background, #F8FAFC);
  color: var(--color-text-muted, #94A3B8);
}

.venue-image-field {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.venue-upload-card {
  width: 100%;
  max-width: 420px;
  height: 180px;
  border-radius: 14px;
  border: 2px dashed var(--color-primary, rgba(59, 130, 246, 0.25));
  background: var(--color-background, #F8FAFC);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.venue-upload-card:hover {
  border-color: var(--color-primary, rgba(59, 130, 246, 0.45));
}

.venue-upload-preview {
  width: 100%;
  height: 100%;
}

.venue-upload-placeholder {
  text-align: center;
  color: var(--color-text-secondary, #64748B);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.venue-upload-text {
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.venue-upload-subtext {
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
}

.venue-upload-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(3px);
  font-weight: 600;
  color: var(--color-primary, #2563EB);
}

.venue-upload-actions {
  display: flex;
  gap: 10px;
}

.venue-detail-image {
  width: 220px;
  height: 140px;
  border-radius: 12px;
  border: 1px solid var(--color-border, #E2E8F0);
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
.venue-dialog :deep(.el-dialog) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
}

.venue-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  padding: 20px 24px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 16px 16px 0 0;
}

.venue-dialog :deep(.el-dialog__title) {
  color: #ffffff;
  font-family: 'Fira Sans', sans-serif;
  font-size: 18px;
  font-weight: 600;
}

.modern-form-section {
  margin-bottom: 28px;
  padding: 28px;
  background: linear-gradient(135deg, var(--color-card-bg, #ffffff) 0%, var(--color-background, rgba(248, 250, 252, 0.95)) 100%);
  border-radius: 16px;
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
}

.modern-section-title {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  display: flex;
  align-items: center;
  gap: 12px;
}

.modern-section-title::before {
  content: '';
  width: 5px;
  height: 24px;
  background: linear-gradient(180deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 3px;
}

.modern-form-item {
  margin-bottom: 24px !important;
}

.modern-form-input {
  width: 100%;
}

.modern-input-icon {
  color: var(--color-primary, #3B82F6);
  font-size: 18px;
}

.modern-field-hint {
  display: block;
  margin-top: 10px;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  font-style: italic;
}

.modern-status-selector {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: var(--color-background, rgba(248, 250, 252, 0.9));
  border-radius: 12px;
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
  padding: 16px 24px;
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
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.modern-status-icon {
  font-size: 22px;
}

.modern-status-icon.enabled {
  color: var(--color-success, #10b981);
}

.modern-status-icon.warning {
  color: var(--color-warning, #f59e0b);
}

.modern-status-icon.disabled {
  color: var(--color-danger, #ef4444);
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 24px 0 0 0;
}

.modern-btn-cancel {
  padding: 14px 32px;
  font-weight: 600;
  border-radius: 12px;
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
  background: var(--color-card-bg, #ffffff);
  color: var(--color-text-secondary, #64748b);
}

.modern-btn-submit {
  padding: 14px 36px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border: none;
  color: #ffffff;
}

.modern-descriptions {
  border-radius: 16px;
  overflow: hidden;
}

.modern-descriptions-item {
  padding: 20px 24px;
}

.modern-descriptions-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-secondary, #64748b);
}

.modern-descriptions-value {
  font-size: 15px;
  color: var(--color-text-primary, #1e293b);
  font-weight: 500;
}

.venue-view-tabs {
  margin-top: 10px;
}

.status-logs-container {
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}

.log-operator {
  margin: 8px 0 4px 0;
  color: var(--color-text-secondary, #64748b);
  font-size: 14px;
}

.log-reason {
  margin: 4px 0;
  color: var(--color-text-primary, #1e293b);
  font-size: 14px;
}

/* 多图片上传样式 */
.venue-images-manager {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.main-image-section,
.detail-images-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-images-draggable-container {
  position: relative;
}

.detail-image-item {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: move;
  transition: all 0.3s ease;
}

.detail-image-item:hover {
  opacity: 0.8;
}

.detail-image-item.dragging {
  opacity: 0.5;
  cursor: grabbing;
}

.detail-image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.detail-image-actions {
  position: absolute;
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  cursor: default;
  opacity: 0;
  font-size: 20px;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  transition: opacity 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.detail-image-item:hover .detail-image-actions {
  opacity: 1;
}

.detail-image-preview-icon,
.detail-image-delete-icon {
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.detail-image-preview-icon:hover,
.detail-image-delete-icon:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin-bottom: 8px;
}

.main-image-card {
  width: 100%;
  max-width: 420px;
  height: 240px;
  border-radius: 14px;
  border: 2px dashed rgba(59, 130, 246, 0.25);
  background: var(--color-background, #F8FAFC);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.main-image-card:hover {
  border-color: rgba(59, 130, 246, 0.45);
  background: var(--color-card-bg-hover, #EFF6FF);
}

.main-image-card.has-image {
  border: 2px solid rgba(59, 130, 246, 0.3);
}

.main-image-preview {
  width: 100%;
  height: 100%;
}

.main-image-placeholder {
  text-align: center;
  color: var(--color-text-secondary, #64748B);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.main-image-text {
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-size: 14px;
}

.main-image-subtext {
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
}

.main-image-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(3px);
  font-weight: 600;
  color: var(--color-primary, #2563EB);
  z-index: 10;
}

.main-image-actions {
  margin-top: 12px;
}

.detail-images-hint {
  margin-top: 8px;
}

/* 向后兼容的旧样式 */
.upload-placeholder {
  text-align: center;
  color: var(--color-text-secondary, #64748B);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-text {
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-size: 14px;
}

.upload-subtext {
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
}

.upload-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(3px);
  font-weight: 600;
  color: var(--color-primary, #2563EB);
}

.upload-tip {
  margin-top: 8px;
  color: var(--color-text-muted, #94A3B8);
  font-size: 12px;
  text-align: center;
}

.image-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

/* 营业时间配置样式 */
.schedule-config-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: rgba(248, 250, 252, 0.9);
  border-radius: 12px;
}

.schedule-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: var(--color-card-bg, #ffffff);
  border-radius: 10px;
  border: 1px solid var(--color-border, #E2E8F0);
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.schedule-type-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.schedule-time-picker {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-separator {
  color: var(--color-text-secondary, #64748b);
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-stats {
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .stat-item {
    width: 72px;
    min-width: 72px;
    max-width: 72px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-stats {
    width: 100%;
    justify-content: space-between;
  }

  .stat-item {
    width: 68px;
    min-width: 68px;
    max-width: 68px;
  }

  .search-container {
    flex-direction: column;
    align-items: stretch;
  }

  .search-fields {
    flex-direction: column;
  }

  .search-input,
  .search-select {
    width: 100%;
  }

  .operation-buttons {
    flex-direction: column;
  }
}
</style>
