<template>
  <view class="coach-unbound-page">
    <view class="ambient ambient-top-right" />
    <view class="ambient ambient-bottom-left" />

    <view class="content-shell">
      <view class="card">
        <view class="icon-wrap">
          <view class="icon-ring" />
          <uni-icons type="locked" size="64" color="#5f5e5e" />
          <view class="warn-badge">
            <uni-icons type="info-filled" size="18" color="#561d00" />
          </view>
        </view>

        <text class="title">未绑定个人资料</text>
        <text class="desc">请联系管理员绑定您的专业资质，以开始管理课程。</text>

        <button class="primary-btn" @tap="handleContactAdmin">
          <uni-icons type="headphones" size="20" color="#561d00" />
          <text>联系管理员</text>
        </button>

        <view class="switch-link" @tap="handleSwitchAccount">
          <text>切换账号</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
function handleContactAdmin() {
  uni.showModal({
    title: '联系管理员',
    content: '请联系系统管理员为当前账号绑定教练档案后再继续使用。',
    showCancel: false,
    confirmText: '我知道了',
    confirmColor: '#ff6600'
  })
}

function handleSwitchAccount() {
  uni.reLaunch({
    url: '/pages/login/login'
  })
}
</script>

<style lang="scss" scoped>
.coach-unbound-page {
  min-height: 100vh;
  background: #f9f9f9;
  position: relative;
  overflow: hidden;
}

.ambient {
  position: absolute;
  border-radius: 999rpx;
  filter: blur(10rpx);
  opacity: 0.45;
}

.ambient-top-right {
  width: 520rpx;
  height: 520rpx;
  top: -120rpx;
  right: -120rpx;
  background: rgba(255, 181, 150, 0.45);
}

.ambient-bottom-left {
  width: 420rpx;
  height: 420rpx;
  left: -140rpx;
  bottom: -120rpx;
  background: rgba(226, 226, 226, 0.75);
}

.content-shell {
  min-height: 100vh;
  padding: 0 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.card {
  width: 100%;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 12rpx 34rpx rgba(26, 28, 28, 0.06);
  padding: 58rpx 38rpx 46rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.icon-wrap {
  position: relative;
  width: 220rpx;
  height: 220rpx;
  border-radius: 999rpx;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-ring {
  position: absolute;
  inset: -10rpx;
  border-radius: 999rpx;
  border: 4rpx dashed rgba(227, 191, 177, 0.5);
}

.warn-badge {
  position: absolute;
  right: -8rpx;
  bottom: -2rpx;
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: #ff6600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 22rpx rgba(255, 102, 0, 0.26);
}

.title {
  display: block;
  margin-top: 54rpx;
  font-size: 68rpx;
  line-height: 1.12;
  font-weight: 900;
  color: #1a1c1c;
  letter-spacing: -2rpx;
}

.desc {
  display: block;
  margin-top: 34rpx;
  max-width: 520rpx;
  font-size: 22px;
  line-height: 1.8;
  color: #5f5e5e;
}

.primary-btn {
  width: 100%;
  height: 92rpx;
  margin-top: 56rpx;
  border: none;
  border-radius: 16rpx;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
  color: #561d00;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 20px;
  font-weight: 900;
  box-shadow: 0 14rpx 30rpx rgba(255, 102, 0, 0.18);
}

.switch-link {
  margin-top: 42rpx;
  padding: 8rpx 12rpx;
  color: #5f5e5e;
  font-size: 18px;
  font-weight: 700;
}
</style>
