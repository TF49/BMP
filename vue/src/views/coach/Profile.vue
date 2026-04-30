<template>
  <div class="coach-profile">
    <div v-if="notBound" class="not-bound-tip">
      <el-alert type="warning" show-icon :closable="false">
        您尚未绑定教练档案，请联系管理员在「教练管理」中为您的账号关联教练档案。
      </el-alert>
    </div>
    <div v-else-if="loadFailed" class="not-bound-tip">
      <el-alert type="error" show-icon :closable="false" :title="loadErrorMessage" />
    </div>

    <template v-else>
      <div class="profile-page" v-loading="loading">
        <header class="profile-page-header">
          <div class="header-icon-wrap" aria-hidden="true">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <h1 class="page-title">我的档案</h1>
          <p class="page-subtitle">维护教练展示信息，让学员更快了解你的训练方向与带课风格</p>
        </header>

        <div class="profile-layout">
          <section class="profile-summary-column">
            <el-card shadow="never" class="profile-summary-card">
              <div class="summary-header">
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :auto-upload="false"
                  accept="image/*"
                  :disabled="avatarUploading"
                  :on-change="handleAvatarChange"
                >
                  <el-avatar :size="84" :src="previewAvatarUrl || undefined" :icon="UserFilled" class="avatar" />
                  <span class="avatar-edit-text">{{ avatarUploading ? '上传中...' : '更换头像' }}</span>
                </el-upload>

                <div class="summary-main">
                  <h2 class="summary-name">{{ coachInfo?.coachName || '未命名教练' }}</h2>
                  <p class="role-line">
                    <el-tag type="warning" effect="dark" size="small">教练档案</el-tag>
                    <span class="venue-chip">{{ coachInfo?.venueName || '未关联场馆' }}</span>
                  </p>
                </div>
              </div>
              <p v-if="coachInfo?.specialty" class="signature">{{ coachInfo.specialty }}</p>
            </el-card>

            <el-card shadow="never" class="profile-meta-card">
              <h3 class="section-title">档案概览</h3>
              <ul class="meta-list">
                <li class="meta-item">
                  <span class="label">手机号</span>
                  <span class="value">{{ coachInfo?.phone || '未填写' }}</span>
                </li>
                <li class="meta-item">
                  <span class="label">所属场馆</span>
                  <span class="value">{{ coachInfo?.venueName || '未关联' }}</span>
                </li>
                <li class="meta-item">
                  <span class="label">课时费</span>
                  <span class="value">¥{{ (coachInfo?.hourlyPrice ?? 0).toFixed(2) }}/时</span>
                </li>
                <li class="meta-item">
                  <span class="label">评分</span>
                  <span class="value">{{ coachInfo?.rating ?? 0 }}</span>
                </li>
                <li class="meta-item">
                  <span class="label">累计学员</span>
                  <span class="value">{{ coachInfo?.totalStudents ?? 0 }}</span>
                </li>
              </ul>
            </el-card>

            <el-card shadow="never" class="profile-actions-card">
              <h3 class="section-title">快捷操作</h3>
              <div class="quick-actions">
                <button type="button" class="quick-action-btn" @click="router.push('/coach/dashboard')">
                  <el-icon class="quick-action-icon"><Odometer /></el-icon>
                  <span>返回工作台</span>
                </button>
                <button type="button" class="quick-action-btn" @click="router.push('/coach/courses')">
                  <el-icon class="quick-action-icon"><Document /></el-icon>
                  <span>我的课程</span>
                </button>
                <button type="button" class="quick-action-btn" @click="router.push('/coach/schedule')">
                  <el-icon class="quick-action-icon"><Calendar /></el-icon>
                  <span>我的课表</span>
                </button>
                <button type="button" class="quick-action-btn" @click="router.push('/coach/bookings')">
                  <el-icon class="quick-action-icon"><List /></el-icon>
                  <span>预约明细</span>
                </button>
              </div>
            </el-card>
          </section>

          <section class="profile-detail">
            <el-card shadow="never" class="profile-form-card">
              <template #header>
                <div class="card-header">
                  <div class="title-block">
                    <span class="title-text">个人信息</span>
                    <span class="card-subtitle">完善教练资料后，相关展示页会同步呈现你的最新信息</span>
                  </div>
                </div>
              </template>

              <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                label-width="96px"
                class="profile-form"
                :disabled="submitting"
              >
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="姓名" prop="coachName">
                      <el-input v-model="form.coachName" maxlength="30" show-word-limit placeholder="请输入教练姓名" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="所属场馆">
                      <el-input :model-value="coachInfo?.venueName || '未关联场馆'" disabled />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="电话" prop="phone">
                      <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="课时费">
                      <el-input :model-value="`¥${(coachInfo?.hourlyPrice ?? 0).toFixed(2)}/时`" disabled />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24">
                    <el-form-item label="专业特长" prop="specialty">
                      <el-input
                        v-model="form.specialty"
                        type="textarea"
                        :rows="4"
                        maxlength="300"
                        show-word-limit
                        placeholder="写清你的擅长方向、教学风格或专项能力"
                      />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24">
                    <el-form-item label="教学经验" prop="experience">
                      <el-input
                        v-model="form.experience"
                        type="textarea"
                        :rows="5"
                        maxlength="1000"
                        show-word-limit
                        placeholder="介绍你的教学年限、带课经历、适合的学员类型等"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <div class="form-actions">
                  <el-button @click="handleReset" :disabled="submitting">重置</el-button>
                  <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
                </div>
              </el-form>
            </el-card>
          </section>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Odometer, Document, Calendar, List } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCurrentCoach, updateCurrentCoach } from '@/api/coach'
import { uploadAvatar } from '@/api/login'
import {
  COACH_UNBOUND_MESSAGE,
  emitCoachProfileUpdated,
  isCoachUnboundError,
  toAbsoluteAssetUrl
} from './coachViewUtils'

const PHONE_PATTERN = /^1\d{10}$/
const router = useRouter()

const coachInfo = ref(null)
const notBound = ref(false)
const loading = ref(false)
const loadFailed = ref(false)
const loadErrorMessage = ref('档案加载失败，请稍后重试')
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
  loadFailed.value = false
  loadErrorMessage.value = '档案加载失败，请稍后重试'
  try {
    const res = await getCurrentCoach()
    if (res?.code === 200 && res?.data) {
      coachInfo.value = res.data
      notBound.value = false
      fillForm(res.data)
    } else {
      coachInfo.value = null
      notBound.value = true
    }
  } catch (error) {
    coachInfo.value = null
    notBound.value = isCoachUnboundError(error)
    loadFailed.value = !notBound.value
    loadErrorMessage.value = error?.message || '档案加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
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
      await loadCoach()
      emitCoachProfileUpdated(coachInfo.value)
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error(isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '保存失败'))
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
  width: 100%;
  margin: 0 auto;
}

.not-bound-tip {
  margin-bottom: 20px;
}

.profile-page {
  max-width: 1040px;
  margin: 0 auto;
  padding: 24px 0 40px;
  background: linear-gradient(
    165deg,
    var(--color-background, #fafaf9) 0%,
    var(--color-background, #f8fafc) 40%,
    color-mix(in srgb, var(--color-primary, #f97316) 8%, #ffffff) 100%
  );
  border-radius: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.profile-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 20% 20%, color-mix(in srgb, var(--color-primary, #f97316) 8%, transparent) 0%, transparent 50%);
  pointer-events: none;
}

.profile-page-header {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.header-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, color-mix(in srgb, var(--color-primary, #f97316) 28%, #ffffff) 0%, var(--color-primary, #f97316) 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px color-mix(in srgb, var(--color-primary, #f97316) 35%, transparent);
  margin-bottom: 4px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.header-icon-wrap:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px color-mix(in srgb, var(--color-primary, #f97316) 40%, transparent);
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text-primary, #0f172a);
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  font-weight: 400;
}

.profile-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 24px;
  align-items: flex-start;
  position: relative;
  z-index: 1;
}

.profile-summary-column {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profile-summary-card,
.profile-meta-card,
.profile-actions-card,
.profile-form-card {
  border-radius: 18px;
  background: var(--color-card-bg, #ffffff);
  border: 1px solid var(--color-border, rgba(226, 232, 240, 0.9));
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 4px 12px rgba(15, 23, 42, 0.06);
  transition: box-shadow 0.2s ease, transform 0.2s ease, border-color 0.2s ease;
}

.profile-summary-card:hover,
.profile-meta-card:hover,
.profile-actions-card:hover,
.profile-form-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.06), 0 16px 40px color-mix(in srgb, var(--color-primary, #f97316) 12%, transparent);
  border-color: color-mix(in srgb, var(--color-primary, #f97316) 28%, var(--color-border, rgba(226, 232, 240, 0.9)));
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 18px;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.avatar-uploader:hover {
  opacity: 0.9;
}

.avatar {
  background: conic-gradient(
    from 180deg,
    var(--color-success, #22c55e),
    var(--color-primary, #f97316),
    var(--color-secondary, #f59e0b)
  );
  margin-bottom: 8px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.2), 0 0 0 4px rgba(255, 255, 255, 0.9);
}

.avatar-edit-text {
  font-size: 12px;
  color: var(--color-text-muted, #94a3b8);
  transition: color 0.2s ease;
}

.avatar-uploader:hover .avatar-edit-text {
  color: var(--color-primary, #f97316);
}

.summary-main {
  flex: 1;
}

.summary-name {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary, #0f172a);
}

.role-line {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.venue-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  background: var(--color-background, #f1f5f9);
  border: 1px solid var(--color-border, #e2e8f0);
}

.signature {
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1px dashed var(--color-border, #e2e8f0);
  font-size: 14px;
  color: var(--color-text-secondary, #475569);
  line-height: 1.5;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #0f172a);
}

.meta-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 13px;
  color: var(--color-text-secondary, #475569);
}

.meta-item:not(:last-child) {
  border-bottom: 1px dashed var(--color-border, #f1f5f9);
}

.meta-item .label {
  color: var(--color-text-muted, #94a3b8);
}

.meta-item .value {
  max-width: 60%;
  text-align: right;
  word-break: break-all;
  font-weight: 500;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1.5px solid var(--color-primary, #f97316);
  background: transparent;
  color: var(--color-primary, #f97316);
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.quick-action-icon {
  font-size: 16px;
}

.quick-action-btn:hover {
  background: linear-gradient(135deg, color-mix(in srgb, var(--color-primary, #f97316) 14%, #ffffff), var(--color-primary, #f97316));
  color: #fff;
  box-shadow: 0 4px 14px color-mix(in srgb, var(--color-primary, #f97316) 35%, transparent);
}

.quick-action-btn:active {
  transform: scale(0.98);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-text {
  font-weight: 700;
  font-size: 16px;
  color: var(--color-text-primary, #0f172a);
}

.card-subtitle {
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
}

.profile-form :deep(.el-input__wrapper),
.profile-form :deep(.el-textarea__inner) {
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.profile-form :deep(.el-input__wrapper:focus-within),
.profile-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-primary, #f97316) 25%, transparent);
}

.form-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #f1f5f9);
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.profile-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 500;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.profile-form :deep(.el-button:not(.el-button--primary)) {
  border: 1px solid var(--color-border, #e2e8f0);
  background: var(--color-card-bg, #fff);
}

.profile-form :deep(.el-button:not(.el-button--primary):hover) {
  background: var(--color-background, #f8fafc);
  border-color: var(--color-border-hover, #cbd5e1);
}

.profile-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--color-primary, #f97316), color-mix(in srgb, var(--color-primary, #f97316) 78%, #7c2d12));
  border: none;
  box-shadow: 0 4px 14px color-mix(in srgb, var(--color-primary, #f97316) 40%, transparent);
}

.profile-form :deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px color-mix(in srgb, var(--color-primary, #f97316) 45%, transparent);
}

@media (max-width: 960px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 16px 0 24px;
    border-radius: 16px;
  }

  .page-title {
    font-size: 20px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .profile-page *,
  .profile-summary-card,
  .profile-meta-card,
  .profile-actions-card,
  .profile-form-card,
  .header-icon-wrap {
    transition: none !important;
  }
}
</style>
