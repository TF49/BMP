<template>
  <div class="profile-page">
    <header class="profile-page-header">
      <div class="header-icon-wrap" aria-hidden="true">
        <el-icon :size="28"><UserFilled /></el-icon>
      </div>
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">管理你的账号资料与偏好设置</p>
    </header>
    <div class="profile-layout">
      <!-- 左侧：概要信息 + 快速信息 + 快捷操作 -->
      <section class="profile-summary">
        <el-card shadow="never" class="profile-summary-card">
          <div class="summary-header">
            <el-upload
              class="avatar-uploader"
              action=""
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <el-avatar
                :size="84"
                :src="profileForm.avatar"
                :icon="UserFilled"
                class="avatar"
              />
              <span class="avatar-edit-text">更换头像</span>
            </el-upload>
            <div class="summary-main">
              <h2 class="username">{{ profileForm.username || '未登录用户' }}</h2>
              <p class="role-line">
                <el-tag :type="roleTagType" effect="dark" size="small">
                  {{ roleName }}
                </el-tag>
                <span
                  v-if="profileForm.createTime"
                  class="join-time-chip"
                  :title="'加入时间：' + formatJoinTime(profileForm.createTime)"
                >
                  <el-icon class="join-time-icon"><Clock /></el-icon>
                  <span class="join-time-text">{{ joinTimeDisplay }}</span>
                </span>
              </p>
            </div>
          </div>
          <p v-if="profileForm.signature" class="signature">
            {{ profileForm.signature }}
          </p>
        </el-card>

        <el-card shadow="never" class="profile-meta-card">
          <h3 class="section-title">账号概览</h3>
          <ul class="meta-list">
            <li class="meta-item">
              <span class="label">手机号</span>
              <span class="value">{{ profileForm.phone || '未填写' }}</span>
            </li>
          </ul>
        </el-card>

        <el-card shadow="never" class="profile-actions-card">
          <h3 class="section-title">快捷操作</h3>
          <div class="quick-actions">
            <button type="button" class="quick-action-btn" @click="handleQuickAction('password')">
              <el-icon class="quick-action-icon"><Lock /></el-icon>
              <span>修改密码</span>
            </button>
            <button type="button" class="quick-action-btn" @click="handleQuickAction('security')">
              <el-icon class="quick-action-icon"><Setting /></el-icon>
              <span>安全设置</span>
            </button>
            <button type="button" class="quick-action-btn" @click="handleQuickAction('notification')">
              <el-icon class="quick-action-icon"><Bell /></el-icon>
              <span>通知偏好</span>
            </button>
            <button type="button" class="quick-action-btn" @click="handleQuickAction('help')">
              <el-icon class="quick-action-icon"><QuestionFilled /></el-icon>
              <span>帮助反馈</span>
            </button>
          </div>
        </el-card>
      </section>

      <!-- 右侧：详细信息表单 -->
      <section class="profile-detail">
        <el-card shadow="never" class="profile-form-card">
          <template #header>
            <div class="card-header">
              <div class="title-block">
                <span class="title-text">个人信息</span>
                <span class="card-subtitle">完善信息有助于我们为你提供更好的服务</span>
              </div>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="profileForm"
            :rules="rules"
            label-width="96px"
            class="profile-form"
          >
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12">
                <el-form-item label="账号" prop="username">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="角色">
                  <el-input :model-value="roleName" disabled />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input
                    v-model="profileForm.phone"
                    placeholder="请输入手机号"
                    maxlength="20"
                  />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :sm="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio label="MALE">男</el-radio>
                    <el-radio label="FEMALE">女</el-radio>
                    <el-radio label="OTHER">其他</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>

              <el-col :xs="24">
                <el-form-item label="个性签名" prop="signature">
                  <el-input
                    v-model="profileForm.signature"
                    type="textarea"
                    :rows="3"
                    placeholder="写点什么介绍一下自己吧~"
                    maxlength="100"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <div class="form-actions">
              <el-button @click="handleReset" :disabled="loading">
                重置
              </el-button>
              <el-button
                type="primary"
                :loading="loading"
                @click="handleSubmit"
              >
                保存
              </el-button>
            </div>
          </el-form>
        </el-card>
      </section>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      class="profile-password-dialog"
      @close="resetPasswordForm"
    >
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="submitChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { UserFilled, Clock, Lock, Setting, Bell, QuestionFilled } from '@element-plus/icons-vue'
import { useUserProfile, type CurrentUserProfile } from '@/composables/useUserProfile'
import { uploadAvatar, changePassword, updateUserInfo } from '@/api/login'
import { getCurrentMember } from '@/api/member'
import { getUserInfo, setUserInfo } from '@/utils/auth'

const formRef = ref<FormInstance>()
const router = useRouter()
const passwordFormRef = ref<FormInstance>()

const {
  loading,
  userProfile,
  loadProfile,
  resetProfile,
  saveProfile
} = useUserProfile()

const profileForm = reactive<CurrentUserProfile>({
  username: '',
  role: 'USER',
  phone: '',
  gender: '',
  avatar: '',
  signature: '',
  createTime: ''
})

const currentMemberType = ref('')
const currentMemberLevel = ref(0)

// 加入时间格式化为 yyyy-MM-dd HH:mm（用于 title 等完整展示）
const formatJoinTime = (dateTime: string | undefined): string => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return dateTime
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

// 加入时间展示：相对时间 + 友好格式，悬停 title 显示完整时间
const joinTimeDisplay = computed(() => {
  const raw = profileForm.createTime
  if (!raw) return ''
  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return raw
  const now = Date.now()
  const diff = now - date.getTime()
  const day = 24 * 60 * 60 * 1000
  if (diff < 60 * 1000) return '刚刚加入'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)} 分钟前加入`
  if (diff < day) return `${Math.floor(diff / (60 * 60 * 1000))} 小时前加入`
  if (diff < 7 * day) return `${Math.floor(diff / day)} 天前加入`
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${y}年${m}月${d}日 加入`
})

// 从 userProfile 同步到本地表单
watch(
  userProfile,
  (val) => {
    if (!val) return
    profileForm.username = val.username
    profileForm.role = val.role
    profileForm.phone = val.phone || ''
    profileForm.gender = (val.gender || '') as any
    profileForm.avatar = val.avatar || ''
    profileForm.signature = val.signature || ''
    profileForm.createTime = val.createTime || ''
  },
  { immediate: true, deep: true }
)

// 角色中文名称（使用公共工具函数）
import { getRoleName } from '@/utils/roleHelper'
const getMemberRoleLabel = (memberType: string, level: number) => {
  if (memberType === 'NORMAL' || !memberType) {
    return '普通用户'
  }
  const safeLevel = Number(level) || 0
  if (safeLevel >= 3) {
    return 'SVIP用户'
  }
  if (safeLevel > 0) {
    return `会员 Lv.${safeLevel}`
  }
  return '会员'
}

const roleName = computed(() => {
  if (currentMemberType.value) {
    return getMemberRoleLabel(currentMemberType.value, currentMemberLevel.value)
  }
  return getRoleName(profileForm.role)
})

// 角色对应的标签样式
const roleTagType = computed<'danger' | 'warning' | 'success' | ''>(() => {
  if (currentMemberType.value === 'NORMAL') return 'success'
  if (currentMemberType.value === 'MEMBER') {
    return currentMemberLevel.value >= 3 ? 'warning' : 'success'
  }
  if (profileForm.role === 'PRESIDENT') return 'danger'
  if (profileForm.role === 'VENUE_MANAGER') return 'warning'
  return 'success'
})

const loadCurrentMember = async () => {
  try {
    const res: any = await getCurrentMember()
    if (res?.code === 200 && res?.data) {
      currentMemberType.value = res.data.memberType || ''
      currentMemberLevel.value = Number(res.data.memberLevel ?? 0)
    }
  } catch (error) {
    console.error('获取当前会员信息失败:', error)
  }
}

// 表单校验规则
const rules: FormRules = {
  phone: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        const reg = /^1[3-9]\d{9}$/
        if (!reg.test(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 头像上传前仅做校验，阻止默认上传
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

// 自定义上传：调用接口后自动保存到后端
const avatarUploading = ref(false)
const handleAvatarUpload = async (options: { file: File }) => {
  try {
    avatarUploading.value = true
    const res: any = await uploadAvatar(options.file)

    if (res && res.code === 200 && res.data && res.data.url) {
      // 更新表单数据
      profileForm.avatar = res.data.url

      // 自动保存到后端
      await updateUserInfo({ avatar: res.data.url })

      // 更新本地用户信息
      userProfile.value = { ...userProfile.value, avatar: res.data.url }
      // 同步到 localStorage，以便右上角头像立即更新
      const current = getUserInfo()
      if (current) {
        setUserInfo({ ...current, avatar: res.data.url })
        window.dispatchEvent(new CustomEvent('userInfoUpdated'))
      }

      ElMessage.success('头像更新成功')
    } else {
      throw new Error(res?.message || '上传失败')
    }
  } catch (error: any) {
    console.error('头像上传失败:', error)
    const errorMessage = error.response?.data?.msg || error.message || '头像上传失败，请重试'
    ElMessage.error(errorMessage)
  } finally {
    avatarUploading.value = false
  }
}

// 修改密码弹窗
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) callback(new Error('两次输入不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}
const submitChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordLoading.value = true
    try {
      const res: any = await changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      if (res && res.code === 200) {
        ElMessage.success('密码修改成功，即将跳转登录页')
        passwordDialogVisible.value = false
        // 清除所有认证信息
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        // 延迟跳转，让用户看到成功提示
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } else {
        ElMessage.error(res?.message || '修改失败')
      }
    } catch (error) {
      console.error('密码修改失败:', error)
      const errorMessage = error.response?.data?.msg || error.message || '密码修改失败，请重试'
      ElMessage.error(errorMessage)
    } finally {
      passwordLoading.value = false
    }
  })
}

// 提交表单
const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return

    userProfile.value = {
      ...userProfile.value,
      phone: profileForm.phone,
      gender: profileForm.gender as any,
      avatar: profileForm.avatar,
      signature: profileForm.signature
    }

    await saveProfile()
  })
}

// 重置为服务器数据
const handleReset = () => {
  resetProfile()
}

const settingsPath = computed(() => (router.currentRoute.value.path.startsWith('/user') ? '/user/settings' : '/settings'))
const helpPath = computed(() => (router.currentRoute.value.path.startsWith('/user') ? '/user/help' : '/help'))
const handleQuickAction = (type: 'password' | 'security' | 'notification' | 'help') => {
  if (type === 'password') {
    passwordDialogVisible.value = true
    return
  }
  if (type === 'security') {
    router.push({ path: settingsPath.value, query: { tab: 'security' } }).catch(() => {})
    return
  }
  if (type === 'notification') {
    router.push({ path: settingsPath.value, query: { tab: 'notification' } }).catch(() => {})
    return
  }
  if (type === 'help') {
    router.push(helpPath.value).catch(() => {})
  }
}

onMounted(() => {
  loadProfile()
  loadCurrentMember()
})
</script>

<style scoped lang="scss">
/* ========== 设计说明 ==========
 * 参考 Galaxy 的卡片阴影与按钮填充动效、React Bits 的微动效节奏，
 * 采用柔和玻璃感 + 极简层次：多层阴影、渐变 CTA、统一 200ms 过渡。
 */

$card-radius: 18px;
$shadow-sm: 0 1px 2px rgba(15, 23, 42, 0.04), 0 4px 12px rgba(15, 23, 42, 0.06);
$shadow-md: 0 4px 6px rgba(15, 23, 42, 0.05), 0 10px 24px rgba(15, 23, 42, 0.08);
$shadow-hover: 0 8px 16px rgba(15, 23, 42, 0.06), 0 16px 40px rgba(249, 115, 22, 0.12);
$transition-base: 0.2s ease;

.profile-page {
  max-width: 1040px;
  margin: 0 auto;
  padding: 24px 0 40px;
  background: linear-gradient(
    165deg,
    var(--color-background, #fafaf9) 0%,
    var(--color-background, #f8fafc) 40%,
    rgba(255, 247, 237, 0.6) 100%
  );
  border-radius: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: radial-gradient(circle at 20% 20%, rgba(249, 115, 22, 0.03) 0%, transparent 50%);
    pointer-events: none;
  }
}

.profile-page-header {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
  z-index: 1;

  .header-icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    background: linear-gradient(
      135deg,
      var(--color-primary-light, #ffedd5) 0%,
      var(--color-primary, #f97316) 100%
    );
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 14px rgba(249, 115, 22, 0.35);
    margin-bottom: 4px;
    transition: transform $transition-base, box-shadow $transition-base;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(249, 115, 22, 0.4);
    }
  }
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

.profile-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 通用卡片：多层阴影 + hover 上浮，无 scale 避免布局抖动 */
.profile-summary-card,
.profile-meta-card,
.profile-actions-card {
  border-radius: $card-radius;
  background: var(--color-card-bg, #ffffff);
  border: 1px solid var(--color-border, rgba(226, 232, 240, 0.9));
  box-shadow: $shadow-sm;
  transition: box-shadow $transition-base, transform $transition-base, border-color $transition-base;
  cursor: default;
}

.profile-summary-card:hover,
.profile-meta-card:hover,
.profile-actions-card:hover {
  transform: translateY(-2px);
  box-shadow: $shadow-hover;
  border-color: var(--color-border-hover, rgba(253, 186, 116, 0.5));
}

.profile-summary-card {
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
    transition: opacity $transition-base;

    &:hover {
      opacity: 0.9;
    }

    .avatar {
      background: conic-gradient(
        from 180deg,
        var(--color-success, #22c55e),
        var(--color-primary, #2563eb),
        var(--color-accent, #f97316)
      );
      margin-bottom: 8px;
      box-shadow:
        0 10px 30px rgba(15, 23, 42, 0.2),
        0 0 0 4px rgba(255, 255, 255, 0.9);
      transition: box-shadow $transition-base;
    }

    .avatar-edit-text {
      font-size: 12px;
      color: var(--color-text-muted, #94a3b8);
      transition: color $transition-base;
    }

    &:hover .avatar-edit-text {
      color: var(--color-primary, #f97316);
    }
  }

  .summary-main {
    flex: 1;

    .username {
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

    .join-time-chip {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: 999px;
      font-size: 12px;
      color: var(--color-text-secondary, #64748b);
      background: var(--color-background, #f1f5f9);
      border: 1px solid var(--color-border, #e2e8f0);
      transition: background $transition-base, border-color $transition-base, color $transition-base;

      .join-time-icon {
        font-size: 14px;
        color: var(--color-primary, #f97316);
      }

      .join-time-text {
        font-weight: 500;
      }
    }
  }

  .signature {
    margin: 14px 0 0;
    padding-top: 12px;
    border-top: 1px dashed var(--color-border, #e2e8f0);
    font-size: 14px;
    color: var(--color-text-secondary, #475569);
    line-height: 1.5;
  }
}

.profile-meta-card {
  .section-title {
    margin: 0 0 10px 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text-primary, #0f172a);
  }

  .meta-list {
    list-style: none;
    padding: 0;
    margin: 0;

    .meta-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
      font-size: 13px;
      color: var(--color-text-secondary, #475569);

      &:not(:last-child) {
        border-bottom: 1px dashed var(--color-border, #f1f5f9);
      }

      .label {
        color: var(--color-text-muted, #94a3b8);
      }

      .value {
        max-width: 60%;
        text-align: right;
        word-break: break-all;
        font-weight: 500;
      }
    }
  }
}

/* 快捷操作：图标 + 文字，描边胶囊按钮，hover 填充 */
.profile-actions-card {
  .section-title {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-text-primary, #0f172a);
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
    transition: background-color $transition-base, color $transition-base, box-shadow $transition-base, transform $transition-base;

    .quick-action-icon {
      font-size: 16px;
    }

    &:hover {
      background: linear-gradient(
        135deg,
        var(--color-primary-light, #ffedd5),
        var(--color-primary, #f97316)
      );
      color: #fff;
      box-shadow: 0 4px 14px rgba(249, 115, 22, 0.35);
    }

    &:active {
      transform: scale(0.98);
    }
  }
}

.profile-detail {
  .profile-form-card {
    border-radius: $card-radius;
    background: var(--color-card-bg, #ffffff);
    border: 1px solid var(--color-border, rgba(226, 232, 240, 0.9));
    box-shadow: $shadow-sm;
    transition: box-shadow $transition-base, transform $transition-base, border-color $transition-base;

    &:hover {
      box-shadow: $shadow-md;
      border-color: var(--color-border-hover, rgba(253, 186, 116, 0.4));
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

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
    }
  }
}

/* 表单：主按钮渐变 CTA，次按钮描边，focus 可见 */
.profile-form {
  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    transition: box-shadow $transition-base, border-color $transition-base;
  }

  :deep(.el-input__wrapper:focus-within),
  :deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.25);
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

  :deep(.el-button) {
    border-radius: 10px;
    font-weight: 500;
    transition: transform $transition-base, box-shadow $transition-base, background-color $transition-base;
    cursor: pointer;
  }

  :deep(.el-button:not(.el-button--primary)) {
    border: 1px solid var(--color-border, #e2e8f0);
    background: var(--color-card-bg, #fff);

    &:hover {
      background: var(--color-background, #f8fafc);
      border-color: var(--color-border-hover, #cbd5e1);
    }
  }

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, var(--color-primary, #f97316), #ea580c);
    border: none;
    box-shadow: 0 4px 14px rgba(249, 115, 22, 0.4);

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(249, 115, 22, 0.45);
    }
  }
}

/* 修改密码弹窗与页面风格统一 */
:deep(.profile-password-dialog) {
  .el-dialog {
    border-radius: $card-radius;
    box-shadow: $shadow-md;
  }
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
  .profile-detail .profile-form-card,
  .header-icon-wrap {
    transition: none !important;
  }
}
</style>
