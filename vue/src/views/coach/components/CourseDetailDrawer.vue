<template>
  <el-drawer
    :model-value="modelValue"
    title="课程详情"
    size="520px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="detail-body">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程名称">{{ detail.courseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ detail.courseDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ formatCourseTime(detail) }}</el-descriptions-item>
          <el-descriptions-item label="场地">{{ detail.courtName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="人数">{{ formatStudentCount(detail) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="formatStatusType(detail.status, COURSE_STATUS_TYPE_MAP)" size="small">
              {{ formatStatusText(detail.status, COURSE_STATUS_TEXT_MAP) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.courseContent || '暂无备注' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button type="primary" @click="emit('viewBookings', detail)">查看该课程预约</el-button>
        </div>

        <div class="student-section">
          <div class="section-header">
            <div>
              <h3 class="section-title">学员名单</h3>
              <p class="section-subtitle">查看该课程当前预约学员与课堂备注</p>
            </div>
            <el-button link type="primary" @click="emit('reloadStudents')">刷新</el-button>
          </div>

          <div v-if="studentLoadFailed" class="error-state">
            <div class="error-title">学员名单加载失败</div>
            <div class="error-description">{{ studentErrorMessage }}</div>
            <el-button type="primary" @click="emit('reloadStudents')">重试</el-button>
          </div>

          <template v-else-if="students.length">
            <div class="student-list">
              <article v-for="student in students" :key="student.id" class="student-card">
                <div class="student-main">
                  <div class="student-name-row">
                    <div class="student-name">{{ student.memberName || '-' }}</div>
                    <el-tag :type="formatStatusType(student.status, BOOKING_STATUS_TYPE_MAP)" size="small">
                      {{ formatStatusText(student.status, BOOKING_STATUS_TEXT_MAP) }}
                    </el-tag>
                  </div>
                  <div class="student-meta">
                    <span>{{ student.bookingNo || '-' }}</span>
                    <span>{{ formatCourseTime(student) }}</span>
                  </div>
                  <div class="student-remark">{{ student.remark || '暂无备注' }}</div>
                </div>
                <div class="student-actions">
                  <el-button
                    v-if="student.memberId"
                    link
                    type="primary"
                    @click="emit('viewMember', student.memberId)"
                  >
                    查看学员历史
                  </el-button>
                </div>
              </article>
            </div>
          </template>

          <el-empty v-else-if="studentLoading" description="正在加载学员名单" />
          <el-empty v-else description="该课程暂无预约学员" />
        </div>
      </template>

      <div v-else-if="loadFailed" class="error-state">
        <div class="error-title">课程详情加载失败</div>
        <div class="error-description">{{ errorMessage }}</div>
        <el-button type="primary" @click="emit('reloadDetail')">重试</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import {
  BOOKING_STATUS_TEXT_MAP,
  BOOKING_STATUS_TYPE_MAP,
  COURSE_STATUS_TEXT_MAP,
  COURSE_STATUS_TYPE_MAP,
  formatCourseTime,
  formatStatusText,
  formatStatusType,
  formatStudentCount
} from '../coachViewUtils'

defineProps({
  modelValue: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  detail: { type: Object, default: null },
  loadFailed: { type: Boolean, default: false },
  errorMessage: { type: String, default: '请稍后重试' },
  students: { type: Array, default: () => [] },
  studentLoading: { type: Boolean, default: false },
  studentLoadFailed: { type: Boolean, default: false },
  studentErrorMessage: { type: String, default: '请稍后重试' }
})

const emit = defineEmits([
  'update:modelValue',
  'reloadDetail',
  'reloadStudents',
  'viewBookings',
  'viewMember'
])
</script>

<style scoped>
.detail-body {
  min-height: 280px;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.student-section {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.section-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.student-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.student-card {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank, #fff);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.student-main {
  flex: 1;
  min-width: 0;
}

.student-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.student-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.student-meta {
  margin-top: 8px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.student-remark {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.student-actions {
  display: flex;
  align-items: center;
}

.error-state {
  padding: 24px;
  border-radius: 16px;
  background: #fff7f7;
  border: 1px solid #f5c2c7;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.error-title {
  font-size: 16px;
  font-weight: 600;
  color: #c2410c;
}

.error-description {
  font-size: 14px;
  color: #7c2d12;
}

@media (max-width: 768px) {
  .student-card,
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
