<template>
  <div class="notifications-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">通知管理</span>
          <el-button v-if="hasPublishPermission" type="primary" size="small" @click="showPublishDialog">
            发布通知
          </el-button>
        </div>
      </template>

      <!-- 通知列表 -->
      <el-table :data="notificationList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="200"></el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip></el-table-column>
        <el-table-column prop="publisherName" label="发布者" width="120"></el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" :formatter="formatDate"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canEditNotification(row)" link type="primary" size="small" @click="editNotification(row)">
              编辑
            </el-button>
            <el-button v-if="canDeleteNotification(row)" link type="danger" size="small" @click="deleteNotification(row)">
              删除
            </el-button>
            <el-button link type="default" size="small" @click="viewNotification(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right"
        @change="loadNotifications"
      ></el-pagination>
    </el-card>

    <!-- 发布/编辑通知对话框 -->
    <el-dialog v-model="publishDialogVisible" :title="editingId ? '编辑通知' : '发布通知'" width="600px">
      <el-form :model="notificationForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="notificationForm.content"
            type="textarea"
            rows="6"
            placeholder="请输入通知内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNotification" :loading="submitting">
          {{ editingId ? '更新' : '发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看通知详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="通知详情" width="600px">
      <div v-if="viewingNotification" class="notification-detail">
        <div class="detail-item">
          <span class="label">标题：</span>
          <span class="value">{{ viewingNotification.title }}</span>
        </div>
        <div class="detail-item">
          <span class="label">内容：</span>
          <p class="value">{{ viewingNotification.content }}</p>
        </div>
        <div class="detail-item">
          <span class="label">发布者：</span>
          <span class="value">{{ viewingNotification.publisherName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">发布时间：</span>
          <span class="value">{{ formatDate(viewingNotification.createTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getInfo } from '@/api/login'

interface Notification {
  id: number
  title: string
  content: string
  publisherId: number
  publisherName: string
  venueId?: number
  createTime: string
}

const notificationList = ref<Notification[]>([])
const loading = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const publishDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const editingId = ref<number | null>(null)
const viewingNotification = ref<Notification | null>(null)

const notificationForm = ref({
  title: '',
  content: ''
})

// 获取当前用户信息
const currentUser = ref<any>(null)

// 检查是否有发布权限
const hasPublishPermission = computed(() => {
  return currentUser.value && (currentUser.value.role === 'PRESIDENT' || currentUser.value.role === 'VENUE_MANAGER')
})

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const res: any = await request({
      url: '/api/notifications',
      method: 'get',
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      const result = res.data || {}
      notificationList.value = result.data || []
      total.value = result.total || 0
    }
  } catch (error) {
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

// 获取当前用户信息
const loadCurrentUser = async () => {
  try {
    const res: any = await getInfo()
    if (res.code === 200) {
      currentUser.value = res.data || null
    }
  } catch (error) {
    console.log('加载用户信息失败')
  }
}

// 显示发布对话框
const showPublishDialog = () => {
  editingId.value = null
  notificationForm.value = { title: '', content: '' }
  publishDialogVisible.value = true
}

// 编辑通知
const editNotification = (notification: Notification) => {
  editingId.value = notification.id
  notificationForm.value = {
    title: notification.title,
    content: notification.content
  }
  publishDialogVisible.value = true
}

// 检查是否可以编辑通知
const canEditNotification = (notification: Notification) => {
  if (!hasPublishPermission.value) return false
  if (currentUser.value.role === 'PRESIDENT') return true
  if (currentUser.value.role === 'VENUE_MANAGER') {
    return notification.venueId === currentUser.value.venueId
  }
  return false
}

// 检查是否可以删除通知
const canDeleteNotification = (notification: Notification) => {
  return canEditNotification(notification)
}

// 提交通知
const submitNotification = async () => {
  if (!notificationForm.value.title.trim()) {
    ElMessage.error('请输入通知标题')
    return
  }
  if (!notificationForm.value.content.trim()) {
    ElMessage.error('请输入通知内容')
    return
  }

  submitting.value = true
  try {
    let response
    if (editingId.value) {
      // 编辑通知
      response = await request({
        url: `/api/notifications/${editingId.value}`,
        method: 'put',
        data: {
          title: notificationForm.value.title,
          content: notificationForm.value.content
        }
      })
    } else {
      // 发布新通知
      response = await request({
        url: '/api/notifications',
        method: 'post',
        data: {
          title: notificationForm.value.title,
          content: notificationForm.value.content
        }
      })
    }

    if (response.code === 200) {
      ElMessage.success(editingId.value ? '通知更新成功' : '通知发布成功')
      publishDialogVisible.value = false
      loadNotifications()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error(editingId.value ? '更新通知失败' : '发布通知失败')
  } finally {
    submitting.value = false
  }
}

// 删除通知
const deleteNotification = (notification: Notification) => {
  ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response: any = await request({
        url: `/api/notifications/${notification.id}`,
        method: 'delete'
      })
      if (response.code === 200) {
        ElMessage.success('通知删除成功')
        loadNotifications()
      } else {
        ElMessage.error(response.message)
      }
    } catch (error) {
      ElMessage.error('删除通知失败')
    }
  }).catch(() => {})
}

// 查看通知详情
const viewNotification = (notification: Notification) => {
  viewingNotification.value = notification
  viewDialogVisible.value = true
}

// 格式化日期
const formatDate = (value: string | Notification, _column?: unknown, cellValue?: string) => {
  const dateString = typeof value === 'string' ? value : cellValue
  if (!dateString) return '-'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadCurrentUser()
  loadNotifications()
})
</script>

<style scoped>
.notifications-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.notification-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  gap: 12px;
}

.detail-item .label {
  font-weight: bold;
  min-width: 80px;
  color: #606266;
}

.detail-item .value {
  flex: 1;
  color: #303133;
  word-break: break-word;
  white-space: pre-wrap;
}
</style>
