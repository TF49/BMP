<template>
  <div class="coach-profile">
    <div v-if="notBound" class="not-bound-tip">
      <el-alert type="warning" show-icon :closable="false">
        您尚未绑定教练档案，请联系管理员在「教练管理」中为您的账号关联教练档案。
      </el-alert>
    </div>

    <template v-else>
      <div class="page-hero">
        <div>
          <h2 class="page-title">我的档案</h2>
          <p class="page-subtitle">编辑可维护信息，其他业务字段仍由管理员统一维护</p>
        </div>
        <el-button v-if="!editing" type="primary" @click="startEditing">编辑档案</el-button>
      </div>

      <div v-loading="loading" class="profile-layout">
        <section class="profile-panel profile-readonly">
          <div class="panel-header">
            <div>
              <h3 class="panel-title">档案概览</h3>
              <p class="panel-subtitle">这些字段会直接影响教练端展示与学员查看体验</p>
            </div>
          </div>

          <div class="profile-summary">
            <el-avatar :size="96" :src="previewAvatarUrl || undefined" :icon="UserFilled" />
            <div class="summary-main">
              <div class="summary-name">{{ coachInfo?.coachName || '-' }}</div>
              <div class="summary-meta">{{ coachInfo?.phone || '未填写手机号' }}</div>
            </div>
          </div>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="所属场馆">{{ coachInfo?.venueName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课时费">¥{{ (coachInfo?.hourlyPrice ?? 0).toFixed(2) }}/时</el-descriptions-item>
            <el-descriptions-item label="评分">{{ coachInfo?.rating ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="累计学员数">{{ coachInfo?.totalStudents ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="专业特长">{{ coachInfo?.specialty || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教学经验">{{ coachInfo?.experience || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="profile-panel profile-edit">
          <div class="panel-header">
            <div>
              <h3 class="panel-title">可编辑信息</h3>
              <p class="panel-subtitle">支持头像上传、表单校验与保存状态反馈</p>
            </div>
            <el-tag v-if="editing" type="warning" size="small">编辑中</el-tag>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="edit-form"
            :disabled="!editing || submitting"
          >
            <el-form-item label="头像">
              <div class="avatar-editor">
                <el-avatar :size="88" :src="previewAvatarUrl || undefined" :icon="UserFilled" />
                <div class="avatar-actions">
                  <el-upload
                    :show-file-list="false"
                    :auto-upload="false"
                    accept="image/*"
                    :disabled="!editing || avatarUploading"
                    :on-change="handleAvatarChange"
                  >
                    <el-button :loading="avatarUploading" :disabled="!editing">
                      {{ avatarUploading ? '上传中...' : '上传头像' }}
                    </el-button>
                  </el-upload>
                  <div class="field-help">支持常见图片格式，上传后会自动回填头像地址。</div>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="姓名" prop="coachName">
              <el-input v-model="form.coachName" maxlength="30" show-word-limit placeholder="请输入教练姓名" />
            </el-form-item>

            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" />
            </el-form-item>

            <el-form-item label="专业特长" prop="specialty">
              <el-input
                v-model="form.specialty"
                type="textarea"
                :rows="4"
                maxlength="300"
                show-word-limit
                placeholder="请输入擅长方向、教学风格或专项能力"
              />
            </el-form-item>

            <el-form-item label="教学经验" prop="experience">
              <el-input
                v-model="form.experience"
                type="textarea"
                :rows="6"
                maxlength="1000"
                show-word-limit
                placeholder="请输入教学年限、带课经验或相关说明"
              />
            </el-form-item>

            <div class="form-actions">
              <el-button type="primary" :loading="submitting" :disabled="!editing" @click="submit">保存</el-button>
              <el-button :disabled="submitting || !editing" @click="cancelEditing">取消</el-button>
            </div>
          </el-form>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCurrentCoach, updateCurrentCoach } from '@/api/coach'
import { uploadAvatar } from '@/api/login'
import { toAbsoluteAssetUrl } from './coachViewUtils'

const PHONE_PATTERN = /^1\d{10}$/

const coachInfo = ref(null)
const notBound = ref(false)
const loading = ref(false)
const editing = ref(false)
const submitting = ref(false)
const avatarUploading = ref(false)
const formRef = ref(null)
const form = ref({
  coachName: '',
  phone: '',
  specialty: '',
  experience: '',
  avatar: ''
})

const rules = {
  coachName: [
    { required: true, message: '请输入教练姓名', trigger: 'blur' },
    { min: 1, max: 30, message: '姓名长度需在1到30个字符之间', trigger: 'blur' }
  ],
  phone: [
    {
      validator: (_, value, callback) => {
        const nextValue = String(value || '').trim()
        if (!nextValue) {
          callback(new Error('请输入手机号'))
          return
        }
        if (!PHONE_PATTERN.test(nextValue)) {
          callback(new Error('请输入有效的11位手机号'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  specialty: [
    {
      validator: (_, value, callback) => {
        if (value && String(value).trim().length > 300) {
          callback(new Error('专业特长不能超过300字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  experience: [
    {
      validator: (_, value, callback) => {
        if (value && String(value).trim().length > 1000) {
          callback(new Error('教学经验不能超过1000字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const previewAvatarUrl = computed(() => toAbsoluteAssetUrl(form.value.avatar || coachInfo.value?.avatar))

const fillForm = (data) => {
  form.value = {
    coachName: data?.coachName ?? '',
    phone: data?.phone ?? '',
    specialty: data?.specialty ?? '',
    experience: data?.experience ?? '',
    avatar: data?.avatar ?? ''
  }
}

const loadCoach = async () => {
  loading.value = true
  try {
    const res = await getCurrentCoach()
    if (res?.code === 200 && res?.data) {
      coachInfo.value = res.data
      notBound.value = false
      fillForm(res.data)
    } else {
      notBound.value = true
    }
  } catch (_) {
    notBound.value = true
  } finally {
    loading.value = false
  }
}

const startEditing = () => {
  editing.value = true
  fillForm(coachInfo.value)
}

const cancelEditing = () => {
  editing.value = false
  fillForm(coachInfo.value)
  formRef.value?.clearValidate?.()
}

const handleAvatarChange = async (uploadFile) => {
  const file = uploadFile?.raw
  if (!file) return
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(file)
    if (res?.code === 200 && res?.data) {
      form.value.avatar = typeof res.data === 'string' ? res.data : (res.data.url || res.data.avatar || '')
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res?.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error(error?.message || '头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

const submit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      coachName: form.value.coachName?.trim(),
      phone: form.value.phone?.trim(),
      specialty: form.value.specialty?.trim(),
      experience: form.value.experience?.trim(),
      avatar: form.value.avatar?.trim()
    }
    const res = await updateCurrentCoach(payload)
    if (res?.code === 200) {
      ElMessage.success('保存成功')
      editing.value = false
      await loadCoach()
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCoach()
})
</script>

<style scoped>
.coach-profile {
  max-width: 1100px;
}

.not-bound-tip {
  margin-bottom: 20px;
}

.page-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.page-subtitle {
  color: var(--el-text-color-secondary);
  margin: 0;
  font-size: 14px;
}

.profile-layout {
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
  gap: 16px;
}

.profile-panel {
  padding: 20px;
  border-radius: 18px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.panel-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.profile-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  background: var(--el-fill-color-light);
  margin-bottom: 16px;
}

.summary-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.summary-meta {
  margin-top: 6px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.edit-form {
  max-width: 100%;
}

.avatar-editor {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-help {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.form-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 900px) {
  .page-hero,
  .profile-layout,
  .avatar-editor {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
