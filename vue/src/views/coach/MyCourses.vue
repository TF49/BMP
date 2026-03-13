<template>
  <div class="coach-my-courses">
    <h2 class="page-title">我的课程</h2>
    <p class="page-subtitle">仅展示您担任教练的课程</p>

    <div class="toolbar">
      <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 120px" @change="loadList">
        <el-option label="报名中" :value="1" />
        <el-option label="进行中" :value="2" />
        <el-option label="已结束" :value="3" />
        <el-option label="已取消" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadList">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="courseName" label="课程名称" min-width="140" />
      <el-table-column prop="courtName" label="场地" width="100" />
      <el-table-column label="时间" min-width="180">
        <template #default="{ row }">
          {{ row.courseDate }} {{ row.startTime || '' }} - {{ row.endTime || '' }}
        </template>
      </el-table-column>
      <el-table-column label="人数" width="100">
        <template #default="{ row }">{{ row.currentStudents ?? 0 }} / {{ row.maxStudents ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : row.status === 3 ? 'info' : 'info'" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyCourses } from '@/api/course'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const searchStatus = ref(null)

const statusText = (s) => {
  const m = { 0: '已取消', 1: '报名中', 2: '进行中', 3: '已结束' }
  return m[s] ?? '未知'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getMyCourses({ page: page.value, size: size.value, status: searchStatus.value ?? undefined })
    if (res?.code === 200 && res?.data) {
      list.value = res.data.data ?? []
      total.value = res.data.total ?? 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch (_) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => loadList())
</script>

<style scoped>
.coach-my-courses { max-width: 100%; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px 0; }
.page-subtitle { color: var(--el-text-color-secondary); margin: 0 0 16px 0; font-size: 14px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
