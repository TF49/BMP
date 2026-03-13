<template>
  <div class="small-profile">
    <el-form
      ref="formRef"
      :model="profileForm"
      :rules="rules"
      label-width="96px"
      class="small-profile-form"
    >
      <div class="small-profile-row avatar-row">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader small"
            action=""
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <el-avatar
              :size="56"
              :src="profileForm.avatar || undefined"
              :icon="UserFilled"
              class="avatar"
            />
            <span class="avatar-edit-text">更换</span>
          </el-upload>
        </el-form-item>
      </div>
      <el-row :gutter="16">
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
              :rows="2"
              placeholder="写点什么介绍一下自己吧~"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-actions">
        <el-button size="small" @click="handleReset" :disabled="loading">重置</el-button>
        <el-button size="small" type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserProfile, type CurrentUserProfile } from '@/composables/useUserProfile'
import { uploadAvatar, updateUserInfo } from '@/api/login'
import { getUserInfo, setUserInfo } from '@/utils/auth'

const formRef = ref<FormInstance>()
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

const handleAvatarUpload = async (options: { file: File }) => {
  try {
    const res: any = await uploadAvatar(options.file)
    if (res && res.code === 200 && res.data && res.data.url) {
      profileForm.avatar = res.data.url
      await updateUserInfo({ avatar: res.data.url })
      userProfile.value = { ...userProfile.value, avatar: res.data.url }
      const current = getUserInfo()
      if (current) {
        setUserInfo({ ...current, avatar: res.data.url })
        window.dispatchEvent(new CustomEvent('userInfoUpdated'))
      }
      ElMessage.success('头像更新成功')
    } else {
      throw new Error(res?.msg || '上传失败')
    }
  } catch (error: any) {
    const msg = error.response?.data?.msg || error.message || '头像上传失败，请重试'
    ElMessage.error(msg)
  }
}

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

const handleReset = () => {
  resetProfile()
}

loadProfile()
</script>

<style scoped lang="scss">
.small-profile {
  .small-profile-form {
    max-width: 520px;
  }

  .avatar-row {
    margin-bottom: 8px;
  }

  .avatar-uploader.small {
    display: inline-flex;
    align-items: center;
    gap: 10px;

    .avatar {
      background: linear-gradient(135deg, var(--color-success, #22c55e), var(--color-primary, #2563eb));
    }

    .avatar-edit-text {
      font-size: 12px;
      color: var(--color-primary, #2563eb);
      cursor: pointer;
    }
  }

  .form-actions {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--color-border, #e5e7eb);
  }
}
</style>
