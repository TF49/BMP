<template>
  <view class="coach-profile-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="profileAvatar"
          brand="KINETIC LOGIC"
          action-icon="notification-filled"
          @action="handleNotice"
        />

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-title">正在加载教练档案</text>
        </view>

        <view v-else-if="loadFailed" class="state-card">
          <text class="state-title">档案加载失败</text>
          <text class="state-desc">{{ errorMessage }}</text>
          <view class="state-action" @tap="loadProfile">
            <text>重新加载</text>
          </view>
        </view>

        <template v-else>
          <view class="hero-card">
            <view class="hero-avatar-wrap">
              <image class="hero-avatar" :src="profileAvatar" mode="aspectFill" />
              <view class="verified-badge">
                <uni-icons type="checkbox-filled" size="16" color="#ffffff" />
              </view>
            </view>

            <text class="hero-name">{{ form.coachName || '未命名教练' }}</text>
            <text class="hero-role">{{ profile?.venueName || '未绑定场馆' }}</text>

            <view class="rating-row">
              <uni-icons type="star-filled" size="16" color="#ff6600" />
              <text class="rating-value">{{ ratingText }}</text>
              <text class="rating-count">({{ studentCountText }})</text>
            </view>

            <button class="edit-btn" @tap="toggleEditing">
              <text>{{ editing ? '收起编辑' : '编辑资料' }}</text>
            </button>
          </view>

          <view class="stats-card">
            <view class="stat-block">
              <text class="stat-label">TOTAL STUDENTS</text>
              <text class="stat-value">{{ Number(profile?.totalStudents || 0) }}</text>
            </view>

            <view class="stat-divider" />

            <view class="stat-block">
              <text class="stat-label">HOURLY PRICE</text>
              <text class="stat-value stat-value-primary">¥{{ Number(profile?.hourlyPrice || 0).toFixed(0) }}</text>
            </view>
          </view>

          <view class="content-card">
            <view class="section-head">
              <uni-icons type="compose" size="18" color="#ff6600" />
              <text class="section-title">Professional Bio</text>
            </view>
            <text class="section-copy">{{ form.experience || '请补充教学经验。' }}</text>
          </view>

          <view class="content-card">
            <view class="section-head">
              <uni-icons type="lightning" size="18" color="#ff6600" />
              <text class="section-title">Specialties</text>
            </view>
            <text class="section-copy">{{ form.specialty || '请补充专业特长。' }}</text>
          </view>

          <view v-if="editing" class="editor-card">
            <view class="editor-head">
              <text class="editor-title">编辑教练档案</text>
              <text class="editor-subtitle">仅支持修改头像、姓名、电话、专业特长和教学经验</text>
            </view>

            <view class="form-group">
              <text class="form-label">头像</text>
              <view class="avatar-editor">
                <image class="editor-avatar" :src="profileAvatar" mode="aspectFill" />
                <view class="upload-btn" @tap="chooseAvatar">
                  <text>{{ uploading ? '上传中...' : '更换头像' }}</text>
                </view>
              </view>
            </view>

            <view class="form-group">
              <text class="form-label">姓名</text>
              <input v-model.trim="form.coachName" class="form-input" maxlength="30" placeholder="请输入教练姓名" />
            </view>

            <view class="form-group">
              <text class="form-label">电话</text>
              <input v-model.trim="form.phone" class="form-input" maxlength="11" type="number" placeholder="请输入11位手机号" />
            </view>

            <view class="form-group">
              <text class="form-label">专业特长</text>
              <textarea
                v-model="form.specialty"
                class="form-textarea"
                maxlength="300"
                placeholder="请输入擅长方向、教学风格或专项能力"
              />
            </view>

            <view class="form-group">
              <text class="form-label">教学经验</text>
              <textarea
                v-model="form.experience"
                class="form-textarea form-textarea-large"
                maxlength="1000"
                placeholder="请输入教学年限、带课经验或相关说明"
              />
            </view>

            <view class="editor-actions">
              <view class="editor-btn editor-btn-primary" @tap="saveProfile">
                <text>{{ saving ? '保存中...' : '保存资料' }}</text>
              </view>
              <view class="editor-btn" @tap="cancelEditing">
                <text>取消</text>
              </view>
            </view>
          </view>

          <view class="menu-card">
            <view class="menu-item" @tap="handleNotice">
              <view class="menu-left">
                <view class="menu-icon-wrap">
                  <uni-icons type="notification" size="20" color="#5f5e5e" />
                </view>
                <text class="menu-label">通知入口暂未开放</text>
              </view>
            </view>

            <view class="menu-item" @tap="handleHelp">
              <view class="menu-left">
                <view class="menu-icon-wrap">
                  <uni-icons type="help" size="20" color="#5f5e5e" />
                </view>
                <text class="menu-label">帮助中心暂未开放</text>
              </view>
            </view>

            <view class="menu-item danger" @tap="logout">
              <view class="menu-left">
                <view class="menu-icon-wrap">
                  <uni-icons type="redo" size="20" color="#ba1a1a" />
                </view>
                <text class="menu-label">退出登录</text>
              </view>
            </view>
          </view>
        </template>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="profile" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'
import { getCurrentCoach, updateCurrentCoach, type CoachProfile } from '@/api/coachSelf'
import { uploadAvatar } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { COACH_UNBOUND_PATH, isCoachUnboundError, resolveCoachAvatar } from '@/utils/coachAccess'
import { safeReLaunch } from '@/utils/safeRoute'

const PHONE_PATTERN = /^1\d{10}$/
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const userStore = useUserStore()
const loading = ref(true)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const editing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const profile = ref<CoachProfile | null>(null)
const form = ref({
  coachName: '',
  phone: '',
  specialty: '',
  experience: '',
  avatar: ''
})

const profileAvatar = computed(() => resolveCoachAvatar(form.value.avatar || profile.value?.avatar))
const ratingText = computed(() => {
  const rating = Number(profile.value?.rating ?? 0)
  return rating > 0 ? rating.toFixed(1) : '暂无评分'
})
const studentCountText = computed(() => `${Number(profile.value?.totalStudents || 0)} 位学员`)

function fillForm(data: CoachProfile | null) {
  form.value = {
    coachName: data?.coachName || '',
    phone: data?.phone || '',
    specialty: data?.specialty || '',
    experience: data?.experience || '',
    avatar: data?.avatar || ''
  }
}

async function loadProfile() {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const result = await getCurrentCoach()
    profile.value = result
    fillForm(result)
  } catch (error) {
    console.error('加载教练档案失败:', error)
    if (isCoachUnboundError(error)) {
      safeReLaunch(COACH_UNBOUND_PATH, COACH_UNBOUND_PATH)
      return
    }
    loadFailed.value = true
    errorMessage.value = error instanceof Error ? error.message : '请稍后重试'
  } finally {
    loading.value = false
  }
}

function toggleEditing() {
  editing.value = !editing.value
  if (editing.value) {
    fillForm(profile.value)
  }
}

function cancelEditing() {
  editing.value = false
  fillForm(profile.value)
}

async function chooseAvatar() {
  if (uploading.value) return
  const result = await uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera']
  }).catch(() => null)
  const filePath = result?.tempFilePaths?.[0]
  if (!filePath) return
  uploading.value = true
  try {
    const uploaded = await uploadAvatar(filePath)
    form.value.avatar = uploaded.url
    uni.showToast({
      title: '头像上传成功',
      icon: 'success'
    })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '头像上传失败',
      icon: 'none'
    })
  } finally {
    uploading.value = false
  }
}

async function saveProfile() {
  if (saving.value) return
  if (!form.value.coachName.trim()) {
    uni.showToast({
      title: '请输入教练姓名',
      icon: 'none'
    })
    return
  }
  if (!PHONE_PATTERN.test(form.value.phone.trim())) {
    uni.showToast({
      title: '请输入有效的11位手机号',
      icon: 'none'
    })
    return
  }

  saving.value = true
  try {
    await updateCurrentCoach({
      coachName: form.value.coachName.trim(),
      phone: form.value.phone.trim(),
      specialty: form.value.specialty.trim(),
      experience: form.value.experience.trim(),
      avatar: form.value.avatar.trim()
    })
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })
    editing.value = false
    await loadProfile()
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '保存失败',
      icon: 'none'
    })
  } finally {
    saving.value = false
  }
}

function handleNotice() {
  uni.showToast({
    title: '通知入口暂未开放',
    icon: 'none'
  })
}

function handleHelp() {
  uni.showToast({
    title: '帮助中心暂未开放',
    icon: 'none'
  })
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确认退出当前教练账号吗？',
    confirmColor: '#ff6600',
    success: (result) => {
      if (!result.confirm) return
      userStore.logout()
      safeReLaunch('/pages/login/login', '/pages/login/login')
    }
  })
}

onShow(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.coach-profile-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.page-scroll {
  min-height: 100vh;
}

.page-shell {
  padding: 0 14rpx;
}

.hero-card,
.stats-card,
.content-card,
.editor-card,
.menu-card,
.state-card {
  margin-top: 22rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.05);
}

.hero-card {
  margin-top: 42rpx;
  background: linear-gradient(180deg, rgba(255, 219, 205, 0.28) 0%, #ffffff 100%);
  padding: 42rpx 28rpx 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.hero-avatar-wrap {
  position: relative;
  width: 208rpx;
  height: 208rpx;
}

.hero-avatar {
  width: 100%;
  height: 100%;
  border-radius: 999rpx;
  border: 6rpx solid #ffffff;
  box-shadow: 0 12rpx 28rpx rgba(26, 28, 28, 0.08);
}

.verified-badge {
  position: absolute;
  right: 0;
  bottom: 6rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 999rpx;
  background: #ff6600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 18rpx rgba(255, 102, 0, 0.26);
}

.hero-name {
  margin-top: 28rpx;
  font-size: 64rpx;
  line-height: 1.05;
  font-weight: 900;
}

.hero-role {
  margin-top: 14rpx;
  font-size: 20px;
  color: #5f5e5e;
}

.rating-row {
  margin-top: 18rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.rating-value {
  color: #ff6600;
  font-size: 18px;
  font-weight: 900;
}

.rating-count {
  color: #5f5e5e;
  font-size: 16px;
}

.edit-btn {
  width: 100%;
  height: 74rpx;
  margin-top: 28rpx;
  border: none;
  border-radius: 10rpx;
  background: #ff6600;
  color: #561d00;
  font-size: 18px;
  font-weight: 900;
  box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.22);
}

.stats-card {
  padding: 28rpx 30rpx;
}

.stat-label {
  display: block;
  font-size: 16px;
  color: #5f5e5e;
  letter-spacing: 2rpx;
}

.stat-value {
  display: block;
  margin-top: 20rpx;
  font-size: 78rpx;
  line-height: 1;
  font-weight: 900;
}

.stat-value-primary {
  color: #ff6600;
}

.stat-divider {
  height: 2rpx;
  margin: 28rpx 0;
  background: #f3f3f3;
}

.content-card,
.editor-card {
  padding: 28rpx 28rpx 30rpx;
}

.section-head,
.menu-left,
.avatar-editor {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.section-title,
.editor-title {
  font-size: 21px;
  font-weight: 900;
}

.section-copy {
  display: block;
  margin-top: 24rpx;
  font-size: 18px;
  line-height: 1.8;
  color: #474746;
}

.editor-subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #5f5e5e;
}

.form-group {
  margin-top: 24rpx;
}

.form-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 22rpx;
  color: #5f5e5e;
  font-weight: 700;
}

.form-input,
.form-textarea {
  width: 100%;
  border-radius: 18rpx;
  background: #f3f3f3;
  padding: 20rpx 22rpx;
  font-size: 26rpx;
  color: #1a1c1c;
}

.form-textarea {
  min-height: 180rpx;
}

.form-textarea-large {
  min-height: 240rpx;
}

.editor-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 999rpx;
}

.upload-btn,
.editor-btn,
.state-action {
  min-width: 200rpx;
  height: 78rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  background: #f3f3f3;
  color: #1a1c1c;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
}

.editor-actions {
  margin-top: 28rpx;
  display: flex;
  gap: 12rpx;
}

.editor-btn {
  flex: 1;
}

.editor-btn-primary,
.upload-btn,
.state-action {
  background: #ff6600;
  color: #ffffff;
}

.menu-card {
  overflow: hidden;
}

.menu-item {
  min-height: 106rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2rpx solid #f3f3f3;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item.danger {
  color: #ba1a1a;
}

.menu-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 19px;
  font-weight: 500;
}

.state-card {
  margin-top: 42rpx;
  padding: 80rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.spinner {
  width: 52rpx;
  height: 52rpx;
  border: 5rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 999rpx;
  animation: spin 0.8s linear infinite;
}

.state-title {
  margin-top: 22rpx;
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.state-desc {
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #5f5e5e;
}

.bottom-space {
  height: 170rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
