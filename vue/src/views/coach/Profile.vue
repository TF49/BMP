<template>
  <div class="coach-profile">
    <div v-if="notBound" class="not-bound-tip">
      <el-alert type="warning" show-icon :closable="false">
        您尚未绑定教练档案，请联系管理员在「教练管理」中为您的账号关联教练档案。
      </el-alert>
    </div>
    <template v-else>
      <h2 class="page-title">我的档案</h2>
      <p class="page-subtitle">以下为可编辑信息，其余由管理员维护</p>

      <el-card v-if="coachInfo" class="profile-card">
        <template v-if="!editing">
          <div class="profile-view">
            <div class="avatar-row">
              <el-avatar :size="80" :src="avatarUrl" :icon="UserFilled" />
            </div>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="姓名">{{ coachInfo.coachName }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ coachInfo.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="专业特长">{{ coachInfo.specialty || '-' }}</el-descriptions-item>
              <el-descriptions-item label="教学经验">{{ coachInfo.experience || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属场馆">{{ coachInfo.venueName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="课时费">¥{{ (coachInfo.hourlyPrice ?? 0).toFixed(2) }}/时</el-descriptions-item>
              <el-descriptions-item label="评分">{{ coachInfo.rating ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="累计学员数">{{ coachInfo.totalStudents ?? 0 }}</el-descriptions-item>
            </el-descriptions>
            <div class="actions">
              <el-button type="primary" @click="editing = true">编辑</el-button>
            </div>
          </div>
        </template>
        <template v-else>
          <el-form ref="formRef" :model="form" label-width="100px" style="max-width: 480px">
            <el-form-item label="姓名">
              <el-input v-model="form.coachName" placeholder="姓名" />
            </el-form-item>
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="电话" />
            </el-form-item>
            <el-form-item label="专业特长">
              <el-input v-model="form.specialty" type="textarea" :rows="2" placeholder="专业特长" />
            </el-form-item>
            <el-form-item label="教学经验">
              <el-input v-model="form.experience" type="textarea" :rows="3" placeholder="教学经验" />
            </el-form-item>
            <el-form-item label="头像URL">
              <el-input v-model="form.avatar" placeholder="头像URL（可选）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submit">保存</el-button>
              <el-button @click="editing = false">取消</el-button>
            </el-form-item>
          </el-form>
        </template>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCurrentCoach, updateCurrentCoach } from '@/api/coach'

const coachInfo = ref(null)
const notBound = ref(false)
const editing = ref(false)
const formRef = ref(null)
const form = ref({
  coachName: '',
  phone: '',
  specialty: '',
  experience: '',
  avatar: ''
})

const avatarUrl = computed(() => {
  const url = coachInfo.value?.avatar
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return base ? (base.replace(/\/$/, '') + url) : url
})

const loadCoach = async () => {
  try {
    const res = await getCurrentCoach()
    if (res?.code === 200 && res?.data) {
      coachInfo.value = res.data
      notBound.value = false
      form.value = {
        coachName: res.data.coachName ?? '',
        phone: res.data.phone ?? '',
        specialty: res.data.specialty ?? '',
        experience: res.data.experience ?? '',
        avatar: res.data.avatar ?? ''
      }
    } else {
      notBound.value = true
    }
  } catch (_) {
    notBound.value = true
  }
}

const submit = async () => {
  try {
    const res = await updateCurrentCoach(form.value)
    if (res?.code === 200) {
      ElMessage.success('保存成功')
      editing.value = false
      await loadCoach()
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  }
}

onMounted(() => loadCoach())
</script>

<style scoped>
.coach-profile { max-width: 720px; }
.not-bound-tip { margin-bottom: 20px; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px 0; }
.page-subtitle { color: var(--el-text-color-secondary); margin: 0 0 16px 0; font-size: 14px; }
.profile-card { margin-top: 16px; }
.profile-view .avatar-row { margin-bottom: 16px; }
.profile-view .actions { margin-top: 16px; }
</style>
