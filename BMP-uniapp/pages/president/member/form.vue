<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="member-form-page">
      <view class="status-bar-placeholder" />

      <!-- TopAppBar：返回 + 标题 + 头像（对齐原型） -->
      <view class="top-bar">
        <view class="top-bar-inner">
          <view class="top-left" @click="onBack">
            <view class="icon-hit">
              <uni-icons type="arrow-left" size="24" color="#ea580c" />
            </view>
            <text class="top-title">{{ pageTitle }}</text>
          </view>
          <image
            class="top-avatar"
            :src="presidentAvatar"
            mode="aspectFill"
          />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="section-head">
          <text class="hero-title">{{ pageTitle }}</text>
          <text class="hero-sub">Kinetic Logic V2 — 高性能身份管理系统</text>
        </view>

        <view class="form-block">
          <!-- 姓名 -->
          <view class="field-wrap">
            <text class="field-label">Full Name</text>
            <view class="input-shell">
              <uni-icons type="person" size="22" color="rgba(234,88,12,0.55)" class="input-icon" />
              <input
                class="field-input"
                v-model="form.memberName"
                placeholder="输入会员真实姓名"
                maxlength="50"
              />
            </view>
          </view>

          <!-- 手机 -->
          <view class="field-wrap">
            <text class="field-label">Mobile Number (Unique ID)</text>
            <view class="input-shell">
              <uni-icons type="phone" size="22" color="rgba(234,88,12,0.55)" class="input-icon" />
              <input
                class="field-input"
                type="number"
                v-model="form.phone"
                placeholder="11 位手机号"
                maxlength="11"
              />
            </view>
          </view>

          <!-- 性别 + 身份证 -->
          <view class="row-split">
            <view class="col-gender">
              <text class="field-label">Gender</text>
              <view class="gender-track">
                <view
                  class="gender-btn"
                  :class="{ on: form.gender === 1 }"
                  @click="form.gender = 1"
                >
                  <text class="gender-sym">♂</text>
                  <text>男</text>
                </view>
                <view
                  class="gender-btn"
                  :class="{ on: form.gender === 0 }"
                  @click="form.gender = 0"
                >
                  <text class="gender-sym">♀</text>
                  <text>女</text>
                </view>
              </view>
            </view>
            <view class="col-id">
              <text class="field-label">ID Card Number</text>
              <view class="input-shell">
                <uni-icons type="locked" size="20" color="rgba(234,88,12,0.55)" class="input-icon" />
                <input
                  class="field-input"
                  v-model="form.idCard"
                  placeholder="身份证号 (18位)"
                  maxlength="18"
                />
              </view>
            </view>
          </view>

          <!-- 会员类型 + 等级 -->
          <view class="cards-row">
            <view class="type-card">
              <text class="field-label pad">Membership Type</text>
              <view class="radio-line" @click="form.memberType = 'MEMBER'">
                <view class="radio-outer" :class="{ checked: form.memberType === 'MEMBER' }">
                  <view v-if="form.memberType === 'MEMBER'" class="radio-inner" />
                </view>
                <text class="radio-text strong">会员</text>
              </view>
              <view class="radio-line" @click="form.memberType = 'NORMAL'">
                <view class="radio-outer" :class="{ checked: form.memberType === 'NORMAL' }">
                  <view v-if="form.memberType === 'NORMAL'" class="radio-inner" />
                </view>
                <text class="radio-text">普通</text>
              </view>
            </view>

            <view class="tier-card" :class="{ dim: form.memberType === 'NORMAL' }">
              <text class="field-label pad">Tier Level</text>
              <picker
                mode="selector"
                :range="tierLabels"
                :value="tierIndex"
                :disabled="form.memberType === 'NORMAL'"
                @change="onTierPick"
              >
                <view class="tier-picker">
                  <text class="tier-value">{{ tierLabels[tierIndex] }}</text>
                  <uni-icons type="arrowdown" size="18" color="#ea580c" />
                </view>
              </picker>
            </view>
          </view>
        </view>

        <view class="info-banner">
          <uni-icons type="info" size="22" color="#a33e00" />
          <text class="info-text">
            所有会员数据将同步至 Kinetic Logic V2 云端数据库。手机号将作为该会员的全局唯一识别 ID，注册后不可更改。
          </text>
        </view>

        <view class="scroll-spacer" />
      </scroll-view>

      <view class="bottom-bar">
        <view class="submit-wrap" :class="{ disabled: submitting }" @click="onSubmit">
          <uni-icons type="personadd" size="28" color="#561d00" />
          <text class="submit-text">确认新增</text>
        </view>
      </view>

      <!-- 右上角装饰（与原型一致：橙色三角渐变） -->
      <view class="deco-corner" aria-hidden="true">
        <view class="deco-triangle" />
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { addMember } from '@/api/president/member'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeNavigateBack } from '@/utils/navigation'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const TIER_OPTIONS = [
  { label: '青铜等级', value: 1 },
  { label: '白银等级', value: 2 },
  { label: '黄金精英', value: 3 },
  { label: '钻石专业', value: 4 },
  { label: '荣耀大师', value: 5 }
] as const

const tierLabels = TIER_OPTIONS.map((t) => t.label)
const tierIndex = ref(0)

const pages = getCurrentPages()
const current = pages?.[pages.length - 1] as { options?: { id?: string } }
const editId = computed(() => current?.options?.id)
const isEdit = computed(() => Boolean(editId.value))
const pageTitle = computed(() => (isEdit.value ? '编辑会员' : '新增会员'))

const defaultAvatar =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuC9N3HCVl_ghNA66MSGEYwWYFt9IoNidmahM3qHLo7hnPTEznOjRo2JsWVx7VLgkv3HR8V5_y7iPcsTZ7_y2X_qoYLYgLeUHaUw8qlnnkn0-UtyYvTfn0WRJsP7bgKRd9HxA6XkQc7gvBvpk_066TtuXxs1P4Z2uH3aXkAGCJzqq44P4ZrS0hhc2msKDYksWnlI13wuE-z8NGnNvn4LzY0itoMiFUzX5mm5e03EwDRd_EJxFQUdUwMQujbdUOphRp2T7dr9a1Waz0KR'

const presidentAvatar = computed(() => userStore.userInfo?.avatar || defaultAvatar)

const form = reactive({
  memberName: '',
  phone: '',
  gender: 1 as 0 | 1,
  idCard: '',
  memberType: 'MEMBER' as 'MEMBER' | 'NORMAL'
})

const submitting = ref(false)

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.MEMBER_LIST)
}

function onTierPick(e: { detail: { value: string } }) {
  tierIndex.value = Number(e.detail.value)
}

function validatePhone(phone: string) {
  return /^1[3-9]\d{9}$/.test(phone.trim())
}

function validateIdCard(v: string) {
  const s = v.trim()
  if (!s) return true
  const p15 = /^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$/
  const p18 = /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9Xx])$/
  return p15.test(s) || p18.test(s)
}

async function onSubmit() {
  if (submitting.value) return

  const name = form.memberName.trim()
  if (!name) {
    uni.showToast({ title: '请填写会员姓名', icon: 'none' })
    return
  }
  const phone = form.phone.trim()
  if (!phone) {
    uni.showToast({ title: '请填写手机号', icon: 'none' })
    return
  }
  if (!validatePhone(phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  if (form.idCard.trim() && !validateIdCard(form.idCard)) {
    uni.showToast({ title: '身份证号格式不正确', icon: 'none' })
    return
  }

  const payload: Record<string, unknown> = {
    memberName: name,
    phone,
    gender: form.gender,
    memberType: form.memberType,
    status: 1
  }
  const idc = form.idCard.trim()
  if (idc) payload.idCard = idc.toUpperCase()
  if (form.memberType === 'MEMBER') {
    payload.memberLevel = TIER_OPTIONS[tierIndex.value].value
  }

  submitting.value = true
  try {
    await addMember(payload)
    uni.showToast({ title: '新增成功', icon: 'success' })
    setTimeout(() => onBack(), 500)
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : '提交失败'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.member-form-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
  position: relative;
  overflow: hidden;
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
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}

.top-bar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 48rpx 20rpx;
  min-height: 96rpx;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-width: 0;
}

.icon-hit {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background: rgba(0, 0, 0, 0.06);
  }
}

.top-title {
  font-size: 36rpx;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: #18181b;
}

.top-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999px;
  border: 4rpx solid rgba(255, 237, 213, 0.9);
  flex-shrink: 0;
}

.scroll {
  height: calc(100vh - var(--status-bar-height) - 200rpx);
  padding: 0 48rpx 0;
  box-sizing: border-box;
}

.section-head {
  padding-top: 32rpx;
  margin-bottom: 48rpx;
}

.hero-title {
  display: block;
  font-size: 56rpx;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin-bottom: 12rpx;
}

.hero-sub {
  font-size: 26rpx;
  color: #5f5e5e;
  font-weight: 600;
}

.form-block {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.field-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #5f5e5e;
  margin: 0 0 16rpx 24rpx;
}

.field-label.pad {
  margin-bottom: 24rpx;
}

.input-shell {
  position: relative;
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 48rpx;
  min-height: 120rpx;
  padding: 0 32rpx 0 112rpx;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.06);
}

.input-icon {
  position: absolute;
  left: 40rpx;
  top: 50%;
  transform: translateY(-50%);
}

.field-input {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.row-split {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

@media screen and (min-width: 768px) {
  .row-split {
    flex-direction: row;
    align-items: flex-start;
  }
  .col-gender {
    width: 34%;
  }
  .col-id {
    flex: 1;
  }
}

.gender-track {
  display: flex;
  background: #f3f3f3;
  border-radius: 48rpx;
  padding: 8rpx;
  min-height: 120rpx;
  box-sizing: border-box;
}

.gender-btn {
  flex: 1;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #5f5e5e;
  transition: background 0.2s, color 0.2s, transform 0.15s;
  &:active {
    transform: scale(0.98);
  }
}

.gender-btn.on {
  background: #ff6600;
  color: #561d00;
}

.gender-sym {
  font-size: 28rpx;
}

.cards-row {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  padding-top: 16rpx;
}

@media screen and (min-width: 768px) {
  .cards-row {
    flex-direction: row;
  }
  .type-card,
  .tier-card {
    flex: 1;
  }
}

.type-card {
  background: #ffffff;
  border-radius: 36rpx;
  padding: 36rpx;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);
  border-left: 8rpx solid #ff6600;
}

.tier-card {
  background: #ffffff;
  border-radius: 36rpx;
  padding: 36rpx;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);
}

.tier-card.dim {
  opacity: 0.55;
}

.radio-line {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
  &:last-child {
    margin-bottom: 0;
  }
}

.radio-outer {
  width: 36rpx;
  height: 36rpx;
  border-radius: 9999px;
  border: 4rpx solid #e4e4e7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radio-outer.checked {
  border-color: #a33e00;
}

.radio-inner {
  width: 18rpx;
  height: 18rpx;
  border-radius: 9999px;
  background: #a33e00;
}

.radio-text {
  font-size: 28rpx;
  color: #5f5e5e;
  font-weight: 600;
}

.radio-text.strong {
  color: #1a1c1c;
  font-weight: 800;
}

.tier-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #eeeeee;
  border-radius: 20rpx;
  padding: 20rpx 28rpx;
  min-height: 72rpx;
}

.tier-value {
  font-size: 28rpx;
  font-weight: 800;
  color: #ea580c;
}

.info-banner {
  margin-top: 48rpx;
  padding: 36rpx;
  background: rgba(163, 62, 0, 0.06);
  border-radius: 36rpx;
  display: flex;
  gap: 24rpx;
  align-items: flex-start;
}

.info-text {
  flex: 1;
  font-size: 24rpx;
  font-weight: 700;
  color: #7c2e00;
  line-height: 1.65;
}

.scroll-spacer {
  height: 200rpx;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  padding: 24rpx 48rpx calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
}

.submit-wrap {
  max-width: 1400rpx;
  margin: 0 auto;
  height: 112rpx;
  border-radius: 9999px;
  background: #ff6600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 16rpx 40rpx rgba(234, 88, 12, 0.25);
  &:active {
    transform: scale(0.98);
  }
}

.submit-wrap.disabled {
  opacity: 0.65;
  pointer-events: none;
}

.submit-text {
  font-size: 34rpx;
  font-weight: 800;
  color: #561d00;
}

.deco-corner {
  position: fixed;
  top: 0;
  right: 0;
  width: 400rpx;
  height: 400rpx;
  z-index: 0;
  pointer-events: none;
  opacity: 0.1;
}

.deco-triangle {
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 400rpx 400rpx 0;
  border-color: transparent #ff6600 transparent transparent;
  opacity: 0.85;
}
</style>
