<template>
  <MobileLayout>
    <view class="profile-info-page">
      <view class="topbar">
        <view class="topbar-inner">
          <view class="icon-btn" @click="handleBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">个人资料</text>
            <text class="topbar-sub">PROFILE EDITOR</text>
          </view>
          <view class="icon-btn ghost" @click="handleChangePassword">
            <uni-icons type="locked" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
        <view class="hero-card">
          <view class="hero-glow" />
          <image class="hero-avatar" :src="avatarPreview" mode="aspectFill" />
          <text class="hero-name">{{ profile.username || '未设置用户名' }}</text>
          <text class="hero-copy">维护当前后端已支持的头像、手机号、性别与个性签名信息。</text>
          <view class="hero-tags">
            <text class="hero-tag">{{ genderText }}</text>
            <text class="hero-tag">{{ profile.phone || '未绑定手机号' }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Avatar</text>
              <text class="section-title">头像与展示</text>
            </view>
          </view>

          <view class="avatar-panel">
            <image class="avatar-preview" :src="avatarPreview" mode="aspectFill" />
            <view class="avatar-copy">
              <text class="avatar-title">当前头像</text>
              <text class="avatar-desc">选择一张图片后会先上传，再同步写回个人资料。</text>
            </view>
            <button class="avatar-btn" :disabled="uploadingAvatar" @click="handleAvatarPick">
              {{ uploadingAvatar ? '上传中...' : '更换头像' }}
            </button>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Editable</text>
              <text class="section-title">当前可编辑项</text>
            </view>
          </view>

          <view class="field-grid">
            <view class="field-card">
              <text class="field-label">手机号</text>
              <input
                class="field-input"
                type="number"
                :value="profile.phone"
                placeholder="请输入手机号"
                @input="onPhoneChange"
              />
            </view>

            <view class="field-card picker-card">
              <text class="field-label">性别</text>
              <picker mode="selector" :range="genders" :value="profile.gender" @change="onGenderChange">
                <view class="picker-value">
                  <text>{{ genderText }}</text>
                  <uni-icons type="right" size="14" color="#8e7164" />
                </view>
              </picker>
            </view>

            <view class="field-card full">
              <text class="field-label">个性签名</text>
              <textarea
                class="field-textarea"
                :value="profile.signature"
                maxlength="80"
                placeholder="写一句展示在个人资料中的签名"
                @input="onSignatureChange"
              />
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Security</text>
              <text class="section-title">账号安全</text>
            </view>
          </view>

          <view class="quick-row" @click="handleChangePassword">
            <view class="quick-left">
              <view class="quick-icon" style="background: rgba(0, 98, 161, 0.12);">
                <uni-icons type="locked" size="18" color="#0062a1" />
              </view>
              <view class="quick-copy">
                <text class="quick-title">修改密码</text>
                <text class="quick-desc">前往安全设置更新登录密码与保护项</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
          </view>

          <view class="quick-row" @click="goSettings">
            <view class="quick-left">
              <view class="quick-icon" style="background: rgba(255, 102, 0, 0.12);">
                <uni-icons type="gear" size="18" color="#ff6600" />
              </view>
              <view class="quick-copy">
                <text class="quick-title">更多设置</text>
                <text class="quick-desc">查看通知、隐私、安全等完整设置项</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
          </view>
        </view>

        <view class="save-panel">
          <button class="save-btn" :disabled="saving" @click="handleSave">
            <text class="save-btn-top">Save Profile</text>
            <text class="save-btn-bottom">{{ saving ? '保存中...' : '保存资料' }}</text>
          </button>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser, updateUserInfo, uploadAvatar } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'
import { applyProfileToStoredUser, createEditableProfileState, toProfileUpdatePayload } from '@/utils/profileSync'
import { getAvatarImage } from '@/utils/displayImage'

const userStore = useUserStore()
const genders = ['保密', '男', '女']
const profile = reactive(createEditableProfileState())
const saving = ref(false)
const uploadingAvatar = ref(false)

const genderText = computed(() => genders[profile.gender] || '保密')
const avatarPreview = computed(() => getAvatarImage(profile.avatar))

const syncStoredUser = () => {
  const updated = applyProfileToStoredUser(userStore.userInfo, profile)
  if (updated) {
    userStore.userInfo = updated
    uni.setStorageSync('userInfo', updated)
  }
}

const loadUserInfo = async () => {
  try {
    const user = await getCurrentUser()
    Object.assign(profile, createEditableProfileState(user))
    const updated = applyProfileToStoredUser(userStore.userInfo, createEditableProfileState(user))
    if (updated) {
      userStore.userInfo = updated
      uni.setStorageSync('userInfo', updated)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载用户信息失败',
      icon: 'none'
    })
  }
}

const onPhoneChange = (e: any) => {
  profile.phone = e.detail?.value ?? e.target?.value ?? ''
}

const onGenderChange = (e: any) => {
  profile.gender = Number(e.detail.value)
}

const onSignatureChange = (e: any) => {
  profile.signature = e.detail?.value ?? e.target?.value ?? ''
}

const handleAvatarPick = () => {
  if (uploadingAvatar.value) return
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (result) => {
      const filePath = result.tempFilePaths?.[0]
      if (!filePath) return
      try {
        uploadingAvatar.value = true
        uni.showLoading({ title: '上传头像...' })
        const uploaded = await uploadAvatar(filePath)
        profile.avatar = uploaded.url
        await updateUserInfo({
          avatar: uploaded.url
        })
        syncStoredUser()
        uni.hideLoading()
        uni.showToast({ title: '头像已更新', icon: 'success' })
      } catch (error) {
        console.error('上传头像失败:', error)
        uni.hideLoading()
        uni.showToast({ title: error instanceof Error ? error.message : '头像上传失败', icon: 'none' })
      } finally {
        uploadingAvatar.value = false
      }
    }
  })
}

const handleSave = async () => {
  if (saving.value) return
  try {
    saving.value = true
    uni.showLoading({ title: '保存中...' })
    await updateUserInfo(toProfileUpdatePayload(profile))
    syncStoredUser()
    uni.hideLoading()
    uni.showToast({
      title: '资料已保存',
      icon: 'success'
    })
  } catch (error) {
    console.error('保存用户信息失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  } finally {
    saving.value = false
  }
}

const handleChangePassword = () => {
  uni.navigateTo({
    url: '/pages/settings/security'
  })
}

const goSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/index'
  })
}

const handleBack = () => {
  safeNavigateBack()
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }

  await loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-info-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%),
    #f9f9f9;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(249, 249, 249, 0.82);
  backdrop-filter: blur(20rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);

  &.ghost {
    background: rgba(255, 241, 234, 0.9);
  }
}

.topbar-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.topbar-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.topbar-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.page-scroll {
  height: calc(100vh - 196rpx);
  padding: 0 24rpx 40rpx;
  box-sizing: border-box;
}

.hero-card {
  position: relative;
  margin-top: 12rpx;
  padding: 36rpx 28rpx;
  border-radius: 36rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 18rpx 40rpx rgba(163, 62, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.hero-glow {
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(36rpx);
}

.hero-avatar,
.hero-name,
.hero-copy,
.hero-tags {
  position: relative;
  z-index: 1;
}

.hero-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
  background: rgba(255, 255, 255, 0.12);
}

.hero-name {
  margin-top: 18rpx;
  font-size: 38rpx;
  font-weight: 900;
  color: #ffffff;
}

.hero-copy {
  margin-top: 14rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.86);
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12rpx;
  margin-top: 22rpx;
}

.hero-tag {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
}

.section-card {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.section-head {
  margin-bottom: 20rpx;

  &.compact {
    margin-bottom: 14rpx;
  }
}

.section-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 3rpx;
  text-transform: uppercase;
}

.section-title {
  display: block;
  margin-top: 8rpx;
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.avatar-panel {
  display: grid;
  grid-template-columns: 112rpx 1fr;
  gap: 18rpx;
  align-items: center;
}

.avatar-preview {
  width: 112rpx;
  height: 112rpx;
  border-radius: 999rpx;
  background: #faf8f6;
}

.avatar-copy {
  min-width: 0;
}

.avatar-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.avatar-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.avatar-btn {
  grid-column: 1 / -1;
  margin-top: 8rpx;
  border: none;
  border-radius: 20rpx;
  background: #fff1e8;
  color: #a33e00;
  font-size: 26rpx;
  font-weight: 800;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.field-card {
  padding: 22rpx 20rpx;
  border-radius: 28rpx;
  background: #faf8f6;

  &.full {
    grid-column: 1 / -1;
  }
}

.field-label {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.field-input,
.picker-value {
  margin-top: 16rpx;
  min-height: 56rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.field-input {
  width: 100%;
}

.field-textarea {
  width: 100%;
  min-height: 140rpx;
  margin-top: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
  color: #1a1c1c;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.quick-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 22rpx 0;
  border-bottom: 2rpx solid #f4efec;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.quick-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  flex: 1;
  min-width: 0;
}

.quick-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-copy {
  flex: 1;
  min-width: 0;
}

.quick-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.quick-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.save-panel {
  margin-top: 28rpx;
  padding-bottom: 32rpx;
}

.save-btn {
  width: 100%;
  border: none;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  box-shadow: 0 14rpx 30rpx rgba(255, 102, 0, 0.22);
}

.save-btn-top,
.save-btn-bottom {
  color: #ffffff;
}

.save-btn-top {
  font-size: 20rpx;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.save-btn-bottom {
  font-size: 30rpx;
  font-weight: 900;
}
</style>
