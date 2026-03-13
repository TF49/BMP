<template>
  <div class="user-course">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">课程预约</h1>
      <p class="page-subtitle">预约专业课程，提升运动技能</p>
    </div>

    <!-- Tab切换：预约课程 / 我的课程 -->
    <el-tabs v-model="activeTab" class="course-tabs">
      <el-tab-pane label="预约课程" name="book">
        <!-- 步骤指示器 -->
        <div class="step-indicator" v-if="step > 0">
          <div class="step-indicator-container">
            <div 
              v-for="(stepItem, index) in steps"
              :key="index"
              class="step-item"
              :class="{ 
                active: step > index + 1,
                current: step === index + 1,
                completed: step > index + 1
              }"
            >
              <div class="step-number">
                <el-icon v-if="step > index + 1"><CircleCheck /></el-icon>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="step-label">{{ stepItem }}</div>
              <div class="step-line" v-if="index < steps.length - 1"></div>
            </div>
          </div>
        </div>

        <!-- 步骤1: 选择课程 -->
        <div v-if="step === 1" class="course-step">
          <h3 class="step-title">选择课程</h3>
          <!-- 课程卡片网格 -->
          <div class="course-grid">
            <div
              v-for="course in courseList"
              :key="course.id"
              class="course-card"
              :class="{ 
                unavailable: course.status !== 1,
                selected: selectedCourse?.id === course.id
              }"
              @click="selectCourse(course)"
            >
              <div class="course-image">
                <el-icon :size="48"><Tickets /></el-icon>
              </div>
              <div class="course-info">
                <h4 class="course-name">{{ course.courseName }}</h4>
                <div class="course-meta">
                  <span class="course-coach">教练：{{ course.coachName || '未分配' }}</span>
                  <span class="course-duration">时长：{{ course.duration || 60 }}分钟</span>
                </div>
                <div class="course-details">
                  <div class="detail-item">
                    <span class="detail-label">价格</span>
                    <span class="detail-value price">¥{{ formatCurrency(course.price) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">状态</span>
                    <el-tag :type="course.status === 1 ? 'success' : 'info'" size="small">
                      {{ course.status === 1 ? '可预约' : '已满员' }}
                    </el-tag>
                  </div>
                </div>
                <p class="course-desc" v-if="course.description">{{ course.description }}</p>
              </div>
            </div>
          </div>
          <el-empty v-if="courseList.length === 0" description="暂无可用课程" :image-size="120" />
        </div>

        <!-- 步骤2: 选择时间 -->
        <div v-if="step === 2" class="course-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 1" text>返回选择课程</el-button>
            <h3 class="step-title">选择课程时间</h3>
          </div>
          <div class="time-section">
            <div class="selected-info-card">
              <div class="info-item">
                <span class="info-label">课程名称</span>
                <span class="info-value">{{ selectedCourse?.courseName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">教练</span>
                <span class="info-value">{{ selectedCourse?.coachName || '未分配' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">时长</span>
                <span class="info-value">{{ selectedCourse?.duration || 60 }}分钟</span>
              </div>
              <div class="info-item">
                <span class="info-label">价格</span>
                <span class="info-value price">¥{{ formatCurrency(selectedCourse?.price) }}</span>
              </div>
            </div>
            <el-form :model="bookingForm" label-width="120px" class="time-form">
              <el-form-item label="课程时间">
                <el-date-picker
                  v-model="bookingForm.courseTime"
                  type="datetime"
                  placeholder="选择课程时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
            </el-form>
            <div class="step-actions">
              <el-button @click="step = 1" size="large">返回</el-button>
              <el-button 
                type="primary" 
                @click="step = 3" 
                size="large"
              >
                下一步
              </el-button>
            </div>
          </div>
        </div>

        <!-- 步骤3: 确认预约 -->
        <div v-if="step === 3" class="course-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 2" text>返回选择时间</el-button>
            <h3 class="step-title">确认预约信息</h3>
          </div>
          <div class="confirm-section">
            <div class="confirm-card">
              <h4 class="confirm-title">预约信息</h4>
              <div class="confirm-info">
                <div class="confirm-item">
                  <span class="confirm-label">课程名称</span>
                  <span class="confirm-value">{{ selectedCourse?.courseName }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">教练</span>
                  <span class="confirm-value">{{ selectedCourse?.coachName || '未分配' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">课程时间</span>
                  <span class="confirm-value">{{ bookingForm.courseTime }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">价格</span>
                  <span class="confirm-value price">¥{{ formatCurrency(selectedCourse?.price) }}</span>
                </div>
              </div>
              <div class="confirm-actions">
                <el-button @click="step = 2" size="large">返回修改</el-button>
                <el-button type="primary" :loading="submitting" @click="submitBooking" size="large">确认预约</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的课程" name="my-courses">
        <div class="my-courses-section">
          <div class="filter-bar">
            <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px" @change="loadMyCourses">
              <el-option label="待支付" value="1" />
              <el-option label="已支付" value="2" />
              <el-option label="进行中" value="3" />
              <el-option label="已完成" value="4" />
              <el-option label="已取消" value="0" />
            </el-select>
          </div>

          <div v-if="myCourses.length > 0" class="courses-list">
            <div
              v-for="course in myCourses"
              :key="course.id"
              class="course-item-card"
            >
              <div class="course-item-main">
                <div class="course-item-header">
                  <span class="course-booking-no">{{ course.bookingNo }}</span>
                  <el-tag :type="getStatusType(course.status)" size="small">
                    {{ getStatusText(course.status) }}
                  </el-tag>
                </div>
                <div class="course-item-details">
                  <p class="course-item-name">{{ course.courseName }}</p>
                  <p class="course-item-coach">教练：{{ course.coachName || '未分配' }}</p>
                  <p class="course-item-time">时间：{{ formatCourseTime(course) }}</p>
                  <p class="course-item-amount">¥{{ formatCurrency(course.orderAmount) }}</p>
                </div>
              </div>
              <div class="course-item-actions">
                <el-button
                  v-if="course.status === 1"
                  type="primary"
                  size="small"
                  @click="handlePay(course)"
                >
                  立即支付
                </el-button>
                <el-button
                  v-if="course.status === 2 || course.status === 3"
                  type="warning"
                  size="small"
                  @click="handleCancel(course)"
                >
                  取消预约
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无课程记录" :image-size="120" />
        </div>
      </el-tab-pane>
    </el-tabs>

  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tickets, CircleCheck, ArrowLeft } from '@element-plus/icons-vue'
import { getCourseList } from '@/api/course'
import { getCourseBookingList, addCourseBooking, updateCourseBookingStatus } from '@/api/courseBooking'

const router = useRouter()
const activeTab = ref('book')
const step = ref(1) // 1-选择课程, 2-选择时间, 3-确认预约

// 步骤配置
const steps = ['选择课程', '选择时间', '确认预约']

const filterStatus = ref(null)
const courseList = ref([])
const myCourses = ref([])
const selectedCourse = ref(null)
const submitting = ref(false)

const bookingForm = ref({
  courseId: null,
  courseTime: ''
})

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
  return num.toFixed(2)
}

const formatTimeRange = (start, end) => {
  if (!start || !end) return '-'
  const startDate = new Date(start)
  const endDate = new Date(end)
  const dateStr = `${startDate.getMonth() + 1}月${startDate.getDate()}日`
  const startTime = `${String(startDate.getHours()).padStart(2, '0')}:${String(startDate.getMinutes()).padStart(2, '0')}`
  const endTime = `${String(endDate.getHours()).padStart(2, '0')}:${String(endDate.getMinutes()).padStart(2, '0')}`
  return `${dateStr} ${startTime}-${endTime}`
}

/** 课程预约列表项：用 courseDate + courseStartTime/courseEndTime 格式化显示 */
const formatCourseTime = (course) => {
  if (!course?.courseDate) return '-'
  const dateStr = course.courseDate
  const start = course.courseStartTime ? `${dateStr}T${course.courseStartTime}` : null
  const end = course.courseEndTime ? `${dateStr}T${course.courseEndTime}` : null
  return formatTimeRange(start, end)
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: '' }
  return map[status] || 'info'
}

const loadCourses = async () => {
  try {
    const res = await getCourseList({ page: 1, size: 100, status: 1 })
    if (res.code === 200) {
      courseList.value = res.data?.data || res.data?.records || res.data || []
    }
  } catch (e) {
    console.error('加载课程列表失败:', e)
    ElMessage.error('加载课程列表失败')
  }
}

const selectCourse = (course) => {
  if (course.status !== 1) {
    ElMessage.warning('该课程暂不可预约')
    return
  }
  selectedCourse.value = course
  bookingForm.value = {
    courseId: course.id,
    courseTime: ''
  }
  step.value = 2
}

const resetBookingForm = () => {
  selectedCourse.value = null
  bookingForm.value = {
    courseId: null,
    courseTime: ''
  }
  step.value = 1
}

const submitBooking = async () => {
  if (!selectedCourse.value?.id) {
    ElMessage.warning('请选择课程')
    return
  }
  
  submitting.value = true
  try {
    const res = await addCourseBooking({
      courseId: bookingForm.value.courseId,
      orderAmount: selectedCourse.value.coursePrice ?? selectedCourse.value.price
    })
    
    if (res.code === 200) {
      ElMessage.success('预约成功！')
      resetBookingForm()
      loadCourses()
      activeTab.value = 'my-courses'
      loadMyCourses()
    } else {
      ElMessage.error(res.message || '预约失败')
    }
  } catch (e) {
    console.error('提交预约失败:', e)
    ElMessage.error('预约失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const loadMyCourses = async () => {
  try {
    const params = { page: 1, size: 20 }
    if (filterStatus.value) {
      params.status = parseInt(filterStatus.value)
    }
    const res = await getCourseBookingList(params)
    if (res.code === 200) {
      myCourses.value = res.data?.data || []
    }
  } catch (e) {
    console.error('加载课程列表失败:', e)
  }
}

const handlePay = (course) => {
  router.push(`/user/recharge?pay=${course.id}`)
}

const handleCancel = async (course) => {
  try {
    await ElMessageBox.confirm('确定要取消这个课程预约吗？', '提示', {
      type: 'warning'
    })
    
    const res = await updateCourseBookingStatus(course.id, 0)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadMyCourses()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消预约失败:', e)
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'my-courses') {
    loadMyCourses()
  }
})

onMounted(() => {
  loadCourses()
  if (activeTab.value === 'my-courses') {
    loadMyCourses()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-course {
  padding: 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 减少动画（尊重用户的偏好设置） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 标题动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes titleShimmer {
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.page-header {
  margin-bottom: 32px;
  padding: 24px 0;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, 
    var(--color-primary, #2563EB) 0%, 
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.6;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    var(--color-border, rgba(226, 232, 240, 0.5)) 50%, 
    transparent 100%);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  font-family: 'Poppins', 'Inter', sans-serif;
  position: relative;
  padding-left: 16px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  
  /* 渐变文字效果 */
  background: linear-gradient(135deg, 
    var(--color-text-primary, #1E293B) 0%, 
    var(--color-primary, #2563EB) 50%,
    var(--color-secondary, #8B5CF6) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  
  /* 文字阴影（作为后备） */
  text-shadow: 0 2px 8px rgba(37, 99, 235, 0.15);
  
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, 
    var(--color-primary, #2563EB) 0%, 
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.8;
}

.page-title:hover {
  transform: translateX(4px);
  background-position: 100% 50%;
}

.page-subtitle {
  font-size: 17px;
  margin: 0;
  padding-left: 16px;
  line-height: 1.6;
  letter-spacing: 0.3px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s forwards;
  opacity: 0;
  
  /* 渐变文字效果 */
  background: linear-gradient(135deg, 
    var(--color-text-secondary, #64748B) 0%, 
    var(--color-primary, #2563EB) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: var(--color-text-secondary, #64748B);
  
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-subtitle:hover {
  transform: translateX(4px);
  opacity: 1;
}

/* 深色模式适配 */
html.theme-dark-mode .page-title {
  background: linear-gradient(135deg, 
    var(--color-text-primary, #F1F5F9) 0%, 
    var(--color-primary, #60A5FA) 50%,
    var(--color-secondary, #A78BFA) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 8px rgba(96, 165, 250, 0.2);
}

html.theme-dark-mode .page-subtitle {
  background: linear-gradient(135deg, 
    var(--color-text-secondary, #94A3B8) 0%, 
    var(--color-primary, #60A5FA) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 无障碍：减少动画 */
@media (prefers-reduced-motion: reduce) {
  .page-title,
  .page-subtitle {
    animation: none;
    opacity: 1;
  }
  
  .page-title:hover,
  .page-subtitle:hover {
    transform: none;
  }
}

.course-tabs {
  margin-top: 24px;
}

/* 步骤指示器 */
.step-indicator {
  margin-bottom: 32px;
  padding: 24px;
  background: var(--color-card-bg, #FFFFFF);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.step-indicator-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  max-width: 800px;
  margin: 0 auto;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background, #F1F5F9);
  border: 3px solid var(--color-border, #E2E8F0);
  color: var(--color-text-secondary, #64748B);
  font-weight: 600;
  font-size: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.step-item.current .step-number {
  background: var(--color-primary, #2563EB);
  border-color: var(--color-primary, #2563EB);
  color: #ffffff;
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  animation: pulseStep 2s ease-in-out infinite;
}

.step-item.completed .step-number {
  background: var(--color-success, #10B981);
  border-color: var(--color-success, #10B981);
  color: #ffffff;
  transform: scale(1.05);
}

.step-item.completed .step-number .el-icon {
  font-size: 20px;
}

@keyframes pulseStep {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  }
  50% {
    box-shadow: 0 4px 24px rgba(37, 99, 235, 0.6);
  }
}

.step-label {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: center;
}

.step-item.current .step-label {
  color: var(--color-primary, #2563EB);
  font-weight: 600;
}

.step-item.completed .step-label {
  color: var(--color-success, #10B981);
}

.step-line {
  position: absolute;
  top: 20px;
  left: calc(50% + 20px);
  right: calc(-50% + 20px);
  height: 3px;
  background: var(--color-border, #E2E8F0);
  z-index: 0;
  transition: all 0.4s ease;
}

.step-item.completed + .step-item .step-line {
  background: var(--color-success, #10B981);
}

.step-item.current .step-line {
  background: linear-gradient(90deg, var(--color-success, #10B981) 0%, var(--color-border, #E2E8F0) 100%);
}

/* 步骤内容 */
.course-step {
  padding: 24px 0;
  animation: fadeInUp 0.4s ease-out;
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 24px 0;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.step-header .step-title {
  margin: 0;
}

.time-section,
.confirm-section {
  max-width: 800px;
  margin: 0 auto;
}

.selected-info-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
}

.info-value {
  font-size: 16px;
  color: var(--color-text-primary, #1E293B);
  font-weight: 600;
}

.info-value.price {
  color: var(--color-primary, #2563EB);
  font-size: 18px;
}

.time-form {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
}

.confirm-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-primary, #2563EB);
  border-radius: 16px;
  padding: 32px;
  animation: scaleIn 0.4s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.confirm-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 24px 0;
  text-align: center;
}

.confirm-info {
  margin-bottom: 32px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.confirm-item:last-child {
  border-bottom: none;
}

.confirm-label {
  font-size: 15px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
}

.confirm-value {
  font-size: 16px;
  color: var(--color-text-primary, #1E293B);
  font-weight: 600;
}

.confirm-value.price {
  color: var(--color-primary, #2563EB);
  font-size: 20px;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.course-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 20px;
  padding: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.course-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.05), transparent);
  transition: left 0.5s ease;
}

.course-card:hover:not(.unavailable) {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.course-card.selected {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg, #FFFFFF) 0%, rgba(37, 99, 235, 0.05) 100%);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
  animation: selectedPulse 2s ease-in-out infinite;
}

@keyframes selectedPulse {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
  }
  50% {
    box-shadow: 0 4px 28px rgba(37, 99, 235, 0.3);
  }
}

.course-card:hover:not(.unavailable)::before {
  left: 100%;
}

.course-card.unavailable {
  opacity: 0.5;
  filter: grayscale(0.6);
  cursor: not-allowed;
}

.course-image {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #8B5CF6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  margin: 0 auto;
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.course-card:hover:not(.unavailable) .course-image {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.4);
}

.course-card.unavailable .course-image {
  background: linear-gradient(135deg, #94A3B8 0%, #64748B 100%);
  filter: grayscale(0.8);
}

.course-info {
  text-align: center;
}

.course-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 12px 0;
}

.course-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
}

.course-details {
  display: flex;
  justify-content: space-around;
  gap: 16px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.detail-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.detail-value.price {
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
  font-size: 20px;
  font-weight: 700;
  transition: all 0.3s ease;
}

.course-card:hover:not(.unavailable) .detail-value.price {
  transform: scale(1.1);
}

.course-desc {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 12px 0 0 0;
  text-align: left;
  line-height: 1.5;
}

.course-actions {
  display: flex;
  justify-content: center;
}

.my-courses-section {
  padding: 24px 0;
}

.filter-bar {
  margin-bottom: 20px;
}

.courses-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-item-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.course-item-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--color-primary, #2563EB);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.course-item-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(37, 99, 235, 0.15);
  border-color: var(--color-primary-light, #60A5FA);
  transform: translateX(4px);
}

.course-item-card:hover::before {
  transform: scaleY(1);
}

.course-item-main {
  flex: 1;
}

.course-item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.course-booking-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Code', monospace;
}

.course-item-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-item-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.course-item-coach {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

.course-item-time {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

.course-item-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  margin: 4px 0 0 0;
  font-family: 'Poppins', sans-serif;
}

.course-item-actions {
  display: flex;
  gap: 8px;
}

.estimated-cost {
  display: flex;
  align-items: baseline;
}

.cost-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
}

/* 响应式 - 移动端优化 */
@media (max-width: 768px) {
  .course-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .course-card {
    padding: 20px;
  }
  
  .course-item-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .course-item-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  /* 移动端触摸优化 */
  .course-card,
  .course-item-card {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}
</style>