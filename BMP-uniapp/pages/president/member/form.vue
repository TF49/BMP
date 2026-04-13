<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-inner">
          <view class="top-left" @click="onBack">
            <view class="icon-btn">
              <uni-icons type="arrow-left" size="22" color="#ea580c" />
            </view>
            <text class="title">{{ pageTitle }}</text>
          </view>
          <image class="avatar" :src="presidentAvatar" mode="aspectFill" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="hero-card">
          <view class="hero-main">
            <text class="hero-label">真实会员档案</text>
            <text class="hero-title">{{ pageTitle }}</text>
            <text class="hero-sub">
            {{ isEdit ? '修改会员基础信息，并保留已有余额与消费数据' : '录入新会员基础信息' }}
            </text>
          </view>
          <view class="hero-side">
            <text class="hero-side-label">模式</text>
            <text class="hero-side-value">{{ isEdit ? '编辑' : '新增' }}</text>
          </view>
        </view>

        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>正在加载会员信息...</text>
        </view>

        <template v-else>
          <view class="card">
            <text class="card-title">基础信息</text>
            <view class="field">
              <text class="label">姓名</text>
              <input
                class="input"
                v-model="form.memberName"
                maxlength="50"
                placeholder="请输入会员姓名"
              />
            </view>

            <view class="field">
              <text class="label">手机号</text>
              <input
                class="input"
                v-model="form.phone"
                type="number"
                maxlength="11"
                placeholder="请输入 11 位手机号"
              />
            </view>

            <view class="split-grid">
              <view class="field">
                <text class="label">性别</text>
                <view class="segmented">
                  <view
                    class="segment-btn"
                    :class="{ active: form.gender === 1 }"
                    @click="form.gender = 1"
                  >
                    男
                  </view>
                  <view
                    class="segment-btn"
                    :class="{ active: form.gender === 0 }"
                    @click="form.gender = 0"
                  >
                    女
                  </view>
                </view>
              </view>

              <view class="field">
                <text class="label">身份证号</text>
                <input
                  class="input"
                  v-model="form.idCard"
                  maxlength="18"
                  placeholder="选填，15 或 18 位"
                />
              </view>
            </view>
          </view>

          <view class="card">
            <text class="card-title">会员配置</text>
            <view class="field">
              <text class="label">会员类型</text>
              <view class="segmented">
                <view
                  class="segment-btn"
                  :class="{ active: form.memberType === 'MEMBER' }"
                  @click="form.memberType = 'MEMBER'"
                >
                  会员
                </view>
                <view
                  class="segment-btn"
                  :class="{ active: form.memberType === 'NORMAL' }"
                  @click="form.memberType = 'NORMAL'"
                >
                  普通用户
                </view>
              </view>
            </view>

            <view class="field">
              <text class="label">会员等级</text>
              <picker
                mode="selector"
                :range="tierLabels"
                :value="tierIndex"
                :disabled="form.memberType === 'NORMAL'"
                @change="onTierPick"
              >
                <view class="input picker-row" :class="{ disabled: form.memberType === 'NORMAL' }">
                  <text>{{ form.memberType === 'NORMAL' ? '普通用户无需等级' : tierLabels[tierIndex] }}</text>
                  <uni-icons type="arrowdown" size="16" color="#71717a" />
                </view>
              </picker>
            </view>
          </view>

          <view class="notice">
            <uni-icons type="info" size="18" color="#9a3412" />
            <text class="notice-text">
              编辑时会自动保留原有的 userId、注册时间、到期时间、累计消费与余额，避免被后端全量更新接口覆盖。
            </text>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>

      <view class="footer">
        <view class="footer-btn ghost" @click="onBack">取消</view>
        <view class="footer-btn primary" :class="{ disabled: submitting || loading }" @click="onSubmit">
          {{ submitting ? '提交中...' : isEdit ? '保存修改' : '确认新增' }}
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { addMember, getMemberInfo, updateMember, type MemberListItem } from '@/api/president/member'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const userStore = useUserStore()

const TIER_OPTIONS = [
  { label: '青铜等级', value: 1 },
  { label: '白银等级', value: 2 },
  { label: '黄金精英', value: 3 },
  { label: '钻石专业', value: 4 },
  { label: '荣耀大师', value: 5 }
] as const

const form = reactive({
  memberName: '',
  phone: '',
  gender: 1 as 0 | 1,
  idCard: '',
  memberType: 'MEMBER' as 'MEMBER' | 'NORMAL'
})

const tierLabels = TIER_OPTIONS.map(item => item.label)
const tierIndex = ref(0)
const editId = ref<number | null>(null)
const originalMember = ref<MemberListItem | null>(null)
const loading = ref(false)
const submitting = ref(false)

const presidentAvatar = computed(() => userStore.userInfo?.avatar || '/static/placeholders/avatar.svg')
const isEdit = computed(() => editId.value !== null)
const pageTitle = computed(() => (isEdit.value ? '编辑会员' : '新增会员'))

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.MEMBER_LIST)
}

function onTierPick(e: { detail: { value: string } }) {
  tierIndex.value = Number(e.detail.value)
}

function validatePhone(phone: string) {
  return /^1[3-9]\d{9}$/.test(phone)
}

function validateIdCard(idCard: string) {
  if (!idCard) return true
  const rule15 = /^[1-9]\d{14}$/
  const rule18 = /^[1-9]\d{16}[\dXx]$/
  return rule15.test(idCard) || rule18.test(idCard)
}

async function loadMemberDetail(id: number) {
  loading.value = true
  try {
    const info = await getMemberInfo(id)
    originalMember.value = info
    form.memberName = String(info.memberName || '')
    form.phone = String(info.phone || '')
    form.gender = Number(info.gender ?? 1) === 0 ? 0 : 1
    form.idCard = String(info.idCard || '')
    form.memberType = info.memberType === 'NORMAL' ? 'NORMAL' : 'MEMBER'

    const level = Number(info.memberLevel || 1)
    const matchedIndex = TIER_OPTIONS.findIndex(item => item.value === level)
    tierIndex.value = matchedIndex >= 0 ? matchedIndex : 0
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (loading.value || submitting.value) return

  const memberName = form.memberName.trim()
  const phone = form.phone.trim()
  const idCard = form.idCard.trim()

  if (!memberName) {
    uni.showToast({ title: '请填写会员姓名', icon: 'none' })
    return
  }
  if (!phone) {
    uni.showToast({ title: '请填写手机号', icon: 'none' })
    return
  }
  if (!validatePhone(phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  if (!validateIdCard(idCard)) {
    uni.showToast({ title: '身份证号格式不正确', icon: 'none' })
    return
  }

  const memberLevel = form.memberType === 'MEMBER' ? TIER_OPTIONS[tierIndex.value].value : null

  submitting.value = true
  try {
    if (isEdit.value) {
      const current = originalMember.value
      if (!current?.id) {
        throw new Error('会员原始数据缺失，无法保存')
      }

      await updateMember({
        id: current.id,
        userId: typeof current.userId === 'number' ? current.userId : undefined,
        memberName,
        gender: form.gender,
        phone,
        idCard: idCard ? idCard.toUpperCase() : '',
        memberType: form.memberType,
        memberLevel,
        registerTime:
          typeof current.registerTime === 'string'
            ? current.registerTime
            : typeof current.createTime === 'string'
              ? current.createTime
              : undefined,
        expireTime: typeof current.expireTime === 'string' ? current.expireTime : null,
        status: Number(current.status ?? 1),
        totalConsumption: Number(current.totalConsumption ?? 0),
        balance: Number(current.balance ?? 0)
      })

      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await addMember({
        memberName,
        gender: form.gender,
        phone,
        idCard: idCard ? idCard.toUpperCase() : undefined,
        memberType: form.memberType,
        memberLevel: memberLevel ?? undefined,
        status: 1
      })

      uni.showToast({ title: '新增成功', icon: 'success' })
    }

    setTimeout(() => onBack(), 500)
  } catch (error) {
    const message = error instanceof Error ? error.message : '提交失败'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad(async (query?: Record<string, string | undefined>) => {
  const id = Number(query?.id || 0)
  if (!Number.isFinite(id) || id <= 0) return

  editId.value = id
  try {
    await loadMemberDetail(id)
  } catch (error) {
    const message = error instanceof Error ? error.message : '会员信息加载失败'
    uni.showToast({ title: message, icon: 'none' })
    setTimeout(() => onBack(), 500)
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f8fafc;
  color: #111827;
  padding-bottom: 132rpx;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f8fafc;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 40;
  background: rgba(248, 250, 252, 0.92);
  backdrop-filter: blur(12px);
}

.top-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 38rpx;
  font-weight: 800;
}

.avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 9999px;
}

.scroll {
  height: calc(100vh - var(--status-bar-height) - 108rpx - 132rpx);
  padding: 0 24rpx;
  box-sizing: border-box;
}

.hero-card {
  margin-top: 12rpx;
  padding: 30rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #fff3eb 0%, #ffffff 100%);
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
}

.hero-main {
  flex: 1;
  min-width: 0;
}

.hero-label {
  display: block;
  font-size: 22rpx;
  color: #9ca3af;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-title {
  display: block;
  margin-top: 10rpx;
  font-size: 56rpx;
  font-weight: 900;
}

.hero-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.6;
}

.hero-side {
  min-width: 150rpx;
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: inset 0 0 0 1rpx rgba(234, 88, 12, 0.08);
}

.hero-side-label {
  display: block;
  font-size: 20rpx;
  color: #9ca3af;
}

.hero-side-value {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 800;
  color: #c2410c;
}

.state-wrap {
  min-height: 420rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  color: #6b7280;
}

.spinner {
  width: 44rpx;
  height: 44rpx;
  border: 4rpx solid #e5e7eb;
  border-top-color: #ea580c;
  border-radius: 9999px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.card {
  margin-top: 18rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.card-title {
  display: block;
  margin-bottom: 20rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #111827;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.field:last-child {
  margin-bottom: 0;
}

.label {
  font-size: 24rpx;
  font-weight: 800;
  color: #374151;
}

.input {
  min-height: 88rpx;
  border-radius: 18rpx;
  background: #f3f4f6;
  padding: 0 24rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
}

.picker-row {
  justify-content: space-between;
}

.picker-row.disabled {
  color: #9ca3af;
}

.split-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 18rpx;
}

@media screen and (min-width: 768px) {
  .split-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.segmented {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
}

.segment-btn {
  height: 88rpx;
  border-radius: 18rpx;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #4b5563;
}

.segment-btn.active {
  background: #ffedd5;
  color: #c2410c;
}

.notice {
  margin-top: 18rpx;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: #fff7ed;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.04);
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.notice-text {
  flex: 1;
  font-size: 24rpx;
  color: #9a3412;
  line-height: 1.7;
}

.bottom-space {
  height: 36rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 12rpx;
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
}

.footer-btn {
  height: 88rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
}

.footer-btn.ghost {
  background: #e5e7eb;
  color: #111827;
}

.footer-btn.primary {
  background: #ea580c;
  color: #ffffff;
}

.footer-btn.disabled {
  opacity: 0.6;
  pointer-events: none;
}
</style>
