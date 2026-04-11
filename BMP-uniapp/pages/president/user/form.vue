<template>
  <PresidentLayout :showTabBar="false">
    <view class="form-content">
      <view class="status-bar-placeholder" />
      <view class="form-toolbar">
        <view class="back-wrap" @click="onBack">
          <uni-icons type="arrow-left" size="24" color="#ff6600" />
        </view>
        <text class="toolbar-title">{{ isEdit ? '编辑用户' : '新增用户' }}</text>
      </view>
      <view class="form-card glass-card">
        <view class="form-item">
          <text class="form-label">用户名</text>
          <input
            v-model="form.username"
            class="form-input"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </view>
        <view class="form-item" v-if="!isEdit">
          <text class="form-label">密码</text>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
          />
        </view>
        <view class="form-item" v-if="isEdit">
          <text class="form-label">新密码（不填则不修改）</text>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="留空表示不修改"
          />
        </view>
        <view class="form-item">
          <text class="form-label">角色</text>
          <picker
            mode="selector"
            :range="roleOptions"
            range-key="label"
            :value="roleIndex"
            @change="onRoleChange"
          >
            <view class="form-picker">{{ roleOptions[roleIndex]?.label }}</view>
          </picker>
        </view>
        <view class="form-item" v-if="form.role === 'PRESIDENT' && presidentExists">
          <text class="form-warn">系统中已存在协会会长，请先修改现有会长的角色</text>
        </view>
        <view class="form-item">
          <text class="form-label">状态</text>
          <picker
            mode="selector"
            :range="['启用', '禁用']"
            :value="form.status === 1 ? 0 : 1"
            @change="onStatusChange"
          >
            <view class="form-picker">{{ form.status === 1 ? '启用' : '禁用' }}</view>
          </picker>
        </view>
        <view class="form-item">
          <text class="form-label">身份证号</text>
          <input
            v-model="form.idCard"
            class="form-input"
            placeholder="选填"
          />
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input
            v-model="form.phone"
            class="form-input"
            type="number"
            placeholder="选填"
          />
        </view>
        <view class="btn-row">
          <view class="btn-submit" @click="onSubmit" :class="{ loading: submitLoading }">
            {{ submitLoading ? '提交中...' : '保存' }}
          </view>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getUserInfo, addUser, updateUser, checkPresidentExists } from '@/api/president/user'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeNavigateBack } from '@/utils/navigation'

const roleOptions = [
  { label: '协会会长', value: 'PRESIDENT' },
  { label: '场馆管理者', value: 'VENUE_MANAGER' },
  { label: '普通用户', value: 'USER' }
]

const id = computed(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const idStr = page?.options?.id
  return idStr ? Number(idStr) : 0
})
const isEdit = computed(() => id.value > 0)
const roleIndex = ref(2) // USER
const presidentExists = ref(false)
const submitLoading = ref(false)

const form = ref({
  id: null as number | null,
  username: '',
  password: '',
  phone: '',
  idCard: '',
  role: 'USER',
  status: 1
})

watch(() => form.value.role, async (role) => {
  roleIndex.value = roleOptions.findIndex(o => o.value === role)
  if (role === 'PRESIDENT') {
    try {
      presidentExists.value = await checkPresidentExists(isEdit.value ? id.value : undefined)
    } catch {
      presidentExists.value = false
    }
  } else {
    presidentExists.value = false
  }
})

function onRoleChange(e: any) {
  const i = Number(e.detail?.value ?? 0)
  roleIndex.value = i
  form.value.role = roleOptions[i]?.value ?? 'USER'
}

function onStatusChange(e: any) {
  form.value.status = e.detail?.value === '0' ? 1 : 0
}

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.USER_LIST)
}

async function loadDetail() {
  if (!id.value) return
  try {
    const res = await getUserInfo(id.value) as any
    form.value.id = res.id
    form.value.username = res.username ?? ''
    form.value.phone = res.phone ?? ''
    form.value.idCard = res.idCard ?? ''
    form.value.role = res.role ?? 'USER'
    form.value.status = res.status ?? 1
    roleIndex.value = roleOptions.findIndex(o => o.value === form.value.role)
  } catch (e) {
    console.error(e)
  }
}

async function onSubmit() {
  if (!form.value.username.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!isEdit.value && !form.value.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  if (form.value.role === 'PRESIDENT' && presidentExists.value) {
    uni.showToast({ title: '系统中已存在协会会长', icon: 'none' })
    return
  }
  submitLoading.value = true
  try {
    const payload: any = {
      username: form.value.username.trim(),
      role: form.value.role,
      status: form.value.status,
      idCard: form.value.idCard || undefined,
      phone: form.value.phone || undefined
    }
    if (form.value.password) payload.password = form.value.password
    if (isEdit.value) payload.id = id.value
    if (isEdit.value) {
      await updateUser(payload)
    } else {
      await addUser(payload)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      safeNavigateBack(PRESIDENT_PAGES.USER_LIST)
    }, 800)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'none' })
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  if (isEdit.value) loadDetail()
})
</script>

<style lang="scss" scoped>
.form-content {
  padding: 24rpx;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
}
.form-toolbar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.back-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.back-wrap:active {
  background: rgba(255, 102, 0, 0.12);
}
.toolbar-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #1e293b;
}
.form-card {
  padding: 32rpx;
  border-radius: 24rpx;
}
.form-item {
  margin-bottom: 32rpx;
}
.form-label {
  display: block;
  font-size: 28rpx;
  color: var(--color-text-secondary, #475569);
  margin-bottom: 12rpx;
}
.form-input, .form-picker {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: rgba(255,255,255,0.6);
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.form-picker {
  display: flex;
  align-items: center;
}
.form-warn {
  font-size: 24rpx;
  color: var(--color-warning, #f59e0b);
}
.btn-row { margin-top: 48rpx; }
.btn-submit {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: linear-gradient(135deg, #c2410c, #ea580c);
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
}
.btn-submit.loading { opacity: 0.8; }
</style>
