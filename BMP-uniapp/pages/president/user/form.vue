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
        <view class="hero-panel">
          <view>
            <text class="hero-title">{{ isEdit ? '调整账号权限与状态' : '创建新的系统账号' }}</text>
            <text class="hero-subtitle">设置清晰的角色边界，让账号开通一步到位。</text>
          </view>
          <view class="hero-badge">
            <text class="hero-badge-label">{{ isEdit ? 'EDIT MODE' : 'NEW USER' }}</text>
          </view>
        </view>

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

        <view class="permission-panel">
          <view class="panel-header">
            <view class="panel-accent" />
            <view>
              <text class="panel-title">权限与状态</text>
              <text class="panel-subtitle">请选择最适合该账号的职责范围</text>
            </view>
          </view>

          <view class="form-item role-block">
            <text class="form-label required">用户角色</text>
            <view class="role-grid">
              <view
                v-for="option in roleOptions"
                :key="option.value"
                class="role-option"
                :class="[option.value, { active: form.role === option.value, disabled: option.value === 'PRESIDENT' && presidentExists }]"
                @click="selectRole(option.value)"
              >
                <view class="role-radio">
                  <view class="role-radio-dot" />
                </view>
                <view class="role-icon-box">
                  <text class="role-icon">{{ option.icon }}</text>
                </view>
                <view class="role-copy">
                  <text class="role-title">{{ option.label }}</text>
                  <text class="role-desc">{{ option.description }}</text>
                  <text
                    v-if="option.tip"
                    class="role-tip"
                    :class="{ warn: option.value === 'PRESIDENT' && presidentExists }"
                  >
                    {{ option.value === 'PRESIDENT' && presidentExists ? '系统中已存在协会会长' : option.tip }}
                  </text>
                </view>
              </view>
            </view>
            <view class="panel-footnote">
              <text>{{ selectedRoleDescription }}</text>
            </view>
            <text class="form-warn" v-if="form.role === 'PRESIDENT' && presidentExists">
              系统中已存在协会会长，请先修改现有会长的角色
            </text>
          </view>

          <view class="form-item status-block">
            <text class="form-label">账号状态</text>
            <view class="status-switcher">
              <view
                class="status-option"
                :class="{ active: form.status === 1 }"
                @click="setStatus(1)"
              >
                <text class="status-option-title">启用</text>
                <text class="status-option-desc">允许登录并使用对应端能力</text>
              </view>
              <view
                class="status-option"
                :class="{ active: form.status === 0 }"
                @click="setStatus(0)"
              >
                <text class="status-option-title">禁用</text>
                <text class="status-option-desc">保留资料，但暂时停止账号访问</text>
              </view>
            </view>
          </view>
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
  {
    label: '协会会长',
    value: 'PRESIDENT',
    icon: '🏆',
    description: '最高权限，管理整个平台与关键决策',
    tip: '系统唯一角色'
  },
  {
    label: '场馆管理者',
    value: 'VENUE_MANAGER',
    icon: '🎫',
    description: '管理自己场馆的场地、课程与订单',
    tip: '适合门店运营'
  },
  {
    label: '教练',
    value: 'COACH',
    icon: '👤',
    description: '登录教练端，查看排课、学员与教学安排',
    tip: '适合执教人员'
  },
  {
    label: '普通用户',
    value: 'USER',
    icon: '◌',
    description: '基础查看权限，可进行预约和个人资料操作',
    tip: '默认推荐角色'
  }
]

const id = computed(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const idStr = page?.options?.id
  return idStr ? Number(idStr) : 0
})
const isEdit = computed(() => id.value > 0)
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

const selectedRoleDescription = computed(() => {
  const current = roleOptions.find((item) => item.value === form.value.role)
  return current?.description || '请选择账号角色'
})

function selectRole(role: string) {
  if (role === 'PRESIDENT' && presidentExists.value) return
  form.value.role = role
}

function setStatus(status: 0 | 1) {
  form.value.status = status
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
.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  padding: 28rpx;
  margin-bottom: 32rpx;
  border-radius: 24rpx;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(247, 250, 255, 0.92)),
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.16), transparent 42%);
  border: 1rpx solid rgba(191, 219, 254, 0.8);
  box-shadow: 0 18rpx 44rpx rgba(59, 130, 246, 0.08);
}
.hero-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}
.hero-subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #64748b;
}
.hero-badge {
  flex-shrink: 0;
  align-self: flex-start;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.1);
  border: 1rpx solid rgba(96, 165, 250, 0.35);
}
.hero-badge-label {
  font-size: 20rpx;
  font-weight: 800;
  color: #2563eb;
  letter-spacing: 0.12em;
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
.form-label.required::before {
  content: '* ';
  color: #ef4444;
}
.form-input, .form-picker {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: rgba(255,255,255,0.6);
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  border: 1rpx solid rgba(226, 232, 240, 0.9);
}
.form-picker {
  display: flex;
  align-items: center;
}
.permission-panel {
  margin-bottom: 32rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 252, 0.96));
  border: 1rpx solid rgba(226, 232, 240, 0.95);
  box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.8);
}
.panel-header {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.panel-accent {
  width: 10rpx;
  height: 44rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #2563eb, #60a5fa);
  box-shadow: 0 8rpx 18rpx rgba(37, 99, 235, 0.24);
}
.panel-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}
.panel-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #64748b;
}
.role-block,
.status-block {
  margin-bottom: 0;
}
.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx;
}
.role-option {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18rpx;
  min-height: 170rpx;
  padding: 24rpx 24rpx 24rpx 68rpx;
  border-radius: 24rpx;
  background: #ffffff;
  border: 2rpx solid rgba(191, 219, 254, 0.9);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.04);
}
.role-option.active {
  border-color: #3b82f6;
  box-shadow: 0 18rpx 36rpx rgba(59, 130, 246, 0.15);
}
.role-option.disabled {
  opacity: 0.72;
}
.role-option.PRESIDENT.active {
  background: linear-gradient(135deg, rgba(255, 247, 237, 0.98), rgba(255, 255, 255, 0.98));
}
.role-option.VENUE_MANAGER.active {
  background: linear-gradient(135deg, rgba(255, 251, 235, 0.98), rgba(255, 255, 255, 0.98));
}
.role-option.COACH.active {
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.98), rgba(255, 255, 255, 0.98));
}
.role-option.USER.active {
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.98), rgba(255, 255, 255, 0.98));
}
.role-radio {
  position: absolute;
  left: 22rpx;
  top: 50%;
  width: 28rpx;
  height: 28rpx;
  margin-top: -14rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(148, 163, 184, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}
.role-option.active .role-radio {
  border-color: #2563eb;
}
.role-radio-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 999rpx;
  background: transparent;
}
.role-option.active .role-radio-dot {
  background: #2563eb;
}
.role-icon-box {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 246, 255, 0.88);
  border: 1rpx solid rgba(147, 197, 253, 0.42);
  flex-shrink: 0;
}
.role-option.PRESIDENT .role-icon-box {
  background: rgba(254, 226, 226, 0.58);
  border-color: rgba(252, 165, 165, 0.5);
}
.role-option.VENUE_MANAGER .role-icon-box {
  background: rgba(254, 243, 199, 0.7);
  border-color: rgba(253, 224, 71, 0.45);
}
.role-option.COACH .role-icon-box {
  background: rgba(209, 250, 229, 0.74);
  border-color: rgba(52, 211, 153, 0.45);
}
.role-option.USER .role-icon-box {
  background: rgba(219, 234, 254, 0.8);
  border-color: rgba(96, 165, 250, 0.42);
}
.role-icon {
  font-size: 40rpx;
  line-height: 1;
}
.role-copy {
  min-width: 0;
  flex: 1;
}
.role-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}
.role-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.5;
  color: #475569;
}
.role-tip {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #64748b;
}
.role-tip.warn {
  color: #f97316;
  font-weight: 700;
}
.panel-footnote {
  margin-top: 18rpx;
  padding: 18rpx 22rpx;
  border-radius: 18rpx;
  background: rgba(248, 250, 252, 0.92);
  color: #64748b;
  font-size: 24rpx;
  line-height: 1.6;
}
.status-switcher {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}
.status-option {
  padding: 24rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 2rpx solid rgba(226, 232, 240, 0.95);
}
.status-option.active {
  border-color: #3b82f6;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.95), rgba(255, 255, 255, 0.98));
  box-shadow: 0 14rpx 28rpx rgba(59, 130, 246, 0.12);
}
.status-option-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}
.status-option-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.5;
  color: #64748b;
}
.form-warn {
  display: block;
  margin-top: 16rpx;
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

@media (max-width: 720rpx) {
  .hero-panel {
    flex-direction: column;
  }

  .hero-badge {
    align-self: flex-start;
  }

  .role-grid,
  .status-switcher {
    grid-template-columns: 1fr;
  }
}
</style>
