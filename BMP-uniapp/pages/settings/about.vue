<template>
  <MobileLayout className="about-shell">
    <view class="about-page">
      <scroll-view scroll-y class="page-scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-card">
            <view class="hero-top">
              <view class="hero-brand" @tap="handleBack">
                <uni-icons type="left" size="18" color="#a33e00" />
                <text class="hero-brand-text">KINETIC LOGIC</text>
              </view>
              <text class="hero-side-label">ABOUT THE PRODUCT</text>
            </view>

            <view class="hero-copy">
              <text class="hero-eyebrow">CLUB EXPERIENCE PLATFORM</text>
              <text class="hero-title">让场馆、课程、赛事和服务连接成一套体验</text>
              <text class="hero-subtitle">
                从预订、课程、充值到穿线服务，我们希望把运动场景里的高频操作都压缩成顺手、统一且有品牌感的移动端流程。
              </text>
            </view>

            <view class="brand-panel">
              <view class="brand-mark">
                <uni-icons type="medal" size="32" color="#ffffff" />
              </view>
              <view class="brand-copy">
                <text class="brand-name">BMP CLUB</text>
                <text class="brand-version">Version {{ appVersion }}</text>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">核心能力</text>
                <text class="panel-subtitle">覆盖用户日常使用频率最高的四条服务链路。</text>
              </view>
              <text class="panel-tag">PRODUCT STACK</text>
            </view>

            <view class="feature-grid">
              <view v-for="feature in features" :key="feature.title" class="feature-card">
                <text class="feature-kicker">{{ feature.kicker }}</text>
                <text class="feature-title">{{ feature.title }}</text>
                <text class="feature-desc">{{ feature.description }}</text>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">品牌信息</text>
                <text class="panel-subtitle">基础信息、定位和服务覆盖范围一目了然。</text>
              </view>
              <text class="panel-tag">CLUB PROFILE</text>
            </view>

            <view class="info-list">
              <view class="info-row">
                <text class="info-label">品牌名称</text>
                <text class="info-value">BMP 羽毛球俱乐部</text>
              </view>
              <view class="info-row">
                <text class="info-label">成立时间</text>
                <text class="info-value">2024 年</text>
              </view>
              <view class="info-row">
                <text class="info-label">服务范围</text>
                <text class="info-value">场馆预订、课程预约、赛事报名、器材与穿线服务</text>
              </view>
              <view class="info-row">
                <text class="info-label">覆盖区域</text>
                <text class="info-value">全国重点城市羽毛球用户</text>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">联系我们</text>
                <text class="panel-subtitle">服务支持、邮箱联系和地图定位统一收纳。</text>
              </view>
              <text class="panel-tag">CONTACT</text>
            </view>

            <view class="action-list">
              <view class="action-row" @tap="handleCall">
                <view class="action-copy">
                  <text class="action-title">客服电话</text>
                  <text class="action-desc">400-888-8888</text>
                </view>
                <uni-icons type="phone" size="18" color="#ea580c" />
              </view>

              <view class="action-row" @tap="handleEmail">
                <view class="action-copy">
                  <text class="action-title">客服邮箱</text>
                  <text class="action-desc">support@bmp.com</text>
                </view>
                <uni-icons type="email" size="18" color="#ea580c" />
              </view>

              <view class="action-row" @tap="handleAddress">
                <view class="action-copy">
                  <text class="action-title">公司地址</text>
                  <text class="action-desc">北京市朝阳区 xxx 大厦</text>
                </view>
                <uni-icons type="location" size="18" color="#ea580c" />
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">品牌渠道</text>
                <text class="panel-subtitle">保持和用户的持续触达与内容更新。</text>
              </view>
              <text class="panel-tag">CHANNELS</text>
            </view>

            <view class="channel-grid">
              <view class="channel-card" @tap="handleWechat">
                <text class="channel-title">微信公众号</text>
                <text class="channel-desc">获取活动、课程与场馆更新</text>
              </view>
              <view class="channel-card" @tap="handleWeibo">
                <text class="channel-title">官方微博</text>
                <text class="channel-desc">查看赛事和运营动态</text>
              </view>
              <view class="channel-card" @tap="handleDouyin">
                <text class="channel-title">抖音号</text>
                <text class="channel-desc">短视频内容与俱乐部日常</text>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">法律与版本</text>
                <text class="panel-subtitle">隐私文件、协议条款与当前版本信息。</text>
              </view>
              <text class="panel-tag">LEGAL</text>
            </view>

            <view class="action-list">
              <view class="action-row" @tap="handlePrivacyPolicy">
                <view class="action-copy">
                  <text class="action-title">隐私政策</text>
                  <text class="action-desc">了解平台如何处理与保护用户数据。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>

              <view class="action-row" @tap="handleUserAgreement">
                <view class="action-copy">
                  <text class="action-title">用户协议</text>
                  <text class="action-desc">查看使用本产品时的条款说明。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>

              <view class="action-row" @tap="handleLicenses">
                <view class="action-copy">
                  <text class="action-title">开源许可</text>
                  <text class="action-desc">查看第三方组件与许可信息。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>
            </view>

            <view class="update-cta" @tap="handleCheckUpdate">
              <text class="update-cta-text">检查更新</text>
              <uni-icons type="reload" size="16" color="#ffffff" />
            </view>
          </view>

          <view class="copyright">
            <text class="copyright-text">© 2024 BMP 羽毛球俱乐部 版权所有</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

const appVersion = ref('1.0.0')

const features = [
  {
    kicker: 'VENUE',
    title: '场馆预订',
    description: '按时段快速查找、预订并管理场地资源。'
  },
  {
    kicker: 'COURSE',
    title: '课程预约',
    description: '查看课程排期、教练信息与预约状态。'
  },
  {
    kicker: 'GEAR',
    title: '器材与穿线',
    description: '统一连接器材租借、穿线预约和服务查询。'
  },
  {
    kicker: 'EVENT',
    title: '赛事报名',
    description: '把赛事发现、详情浏览和报名流程串成闭环。'
  }
]

function handleCall() {
  uni.makePhoneCall({
    phoneNumber: '4008888888',
    fail: () => {
      uni.showToast({
        title: '拨打失败',
        icon: 'none'
      })
    }
  })
}

function handleEmail() {
  uni.setClipboardData({
    data: 'support@bmp.com',
    success: () => {
      uni.showToast({
        title: '邮箱已复制',
        icon: 'success'
      })
    }
  })
}

function handleAddress() {
  uni.openLocation({
    latitude: 39.9042,
    longitude: 116.4074,
    name: 'BMP羽毛球俱乐部',
    address: '北京市朝阳区xxx大厦',
    fail: () => {
      uni.showToast({
        title: '打开地图失败',
        icon: 'none'
      })
    }
  })
}

function handleWechat() {
  uni.showModal({
    title: '关注微信公众号',
    content: '请在微信中搜索 “BMP羽毛球” 关注我们',
    showCancel: false
  })
}

function handleWeibo() {
  uni.showModal({
    title: '官方微博',
    content: '当前移动端暂未提供微博跳转入口，可通过客服渠道获取最新赛事与运营动态。',
    showCancel: false
  })
}

function handleDouyin() {
  uni.showModal({
    title: '抖音号',
    content: '当前移动端暂未提供抖音直达入口，如需内容更新可关注官方客服通知。',
    showCancel: false
  })
}

function handlePrivacyPolicy() {
  uni.showModal({
    title: '隐私政策',
    content: '当前小程序端暂未提供独立隐私政策页面，请以系统内实际已开放功能为准进行资料维护与设置。',
    showCancel: false
  })
}

function handleUserAgreement() {
  uni.showModal({
    title: '用户协议',
    content: '当前小程序端暂未提供独立用户协议页面，如需了解服务规则可先查看帮助与反馈页中的能力说明。',
    showCancel: false
  })
}

function handleLicenses() {
  uni.showModal({
    title: '开源许可',
    content: '第三方依赖许可信息暂未在小程序端单独展示，不影响当前已接入功能使用。',
    showCancel: false
  })
}

function handleCheckUpdate() {
  uni.showLoading({
    title: '检查中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '已是最新版本',
      icon: 'success'
    })
  }, 1200)
}

function handleBack() {
  safeNavigateBack('/pages/settings/index')
}

onMounted(() => {
  try {
    const accountInfo = uni.getAccountInfoSync()
    if (accountInfo?.miniProgram) {
      appVersion.value = accountInfo.miniProgram.version || '1.0.0'
    }
  } catch (error) {
    console.log('获取版本信息失败')
  }
})
</script>

<style lang="scss" scoped>
.about-shell {
  background:
    radial-gradient(circle at top left, rgba(255, 170, 112, 0.28), transparent 28%),
    radial-gradient(circle at top right, rgba(251, 146, 60, 0.16), transparent 22%),
    linear-gradient(180deg, #fff7ed 0%, #f8fafc 36%, #eef2ff 100%);
}

.about-page {
  min-height: 100vh;
}

.page-scroll {
  height: 100vh;
}

.content {
  padding: 30rpx 24rpx 48rpx;
}

.hero-card,
.panel-card {
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24rpx 60rpx rgba(15, 23, 42, 0.08);
}

.hero-card {
  position: relative;
  overflow: hidden;
  padding: 32rpx;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 237, 0.98));
}

.hero-card::before {
  content: '';
  position: absolute;
  right: -84rpx;
  top: -80rpx;
  width: 250rpx;
  height: 250rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.22), rgba(251, 146, 60, 0));
}

.hero-top,
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-top {
  position: relative;
  z-index: 1;
  margin-bottom: 36rpx;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 237, 213, 0.86);
}

.hero-brand-text,
.hero-side-label,
.hero-eyebrow,
.panel-tag,
.feature-kicker {
  letter-spacing: 0.18em;
}

.hero-brand-text {
  font-size: 20rpx;
  color: #9a3412;
  font-weight: 800;
}

.hero-side-label {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 700;
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  display: block;
  margin-bottom: 16rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.hero-title {
  display: block;
  font-size: 54rpx;
  line-height: 1.16;
  color: #0f172a;
  font-weight: 800;
}

.hero-subtitle {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.65;
  color: #64748b;
}

.brand-panel {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 18rpx;
  margin-top: 28rpx;
  padding: 22rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  box-shadow: 0 16rpx 32rpx rgba(234, 88, 12, 0.24);
}

.brand-mark {
  width: 84rpx;
  height: 84rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-copy {
  flex: 1;
}

.brand-name,
.brand-version {
  display: block;
  color: #ffffff;
}

.brand-name {
  font-size: 32rpx;
  font-weight: 800;
}

.brand-version {
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.86;
}

.panel-card {
  margin-top: 24rpx;
  padding: 28rpx;
}

.panel-title {
  display: block;
  font-size: 32rpx;
  color: #0f172a;
  font-weight: 800;
}

.panel-subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.panel-tag {
  font-size: 18rpx;
  color: #94a3b8;
  font-weight: 700;
}

.feature-grid,
.channel-grid {
  display: grid;
  gap: 16rpx;
  margin-top: 22rpx;
}

.feature-grid {
  grid-template-columns: repeat(2, 1fr);
}

.channel-grid {
  grid-template-columns: 1fr;
}

.feature-card,
.channel-card {
  padding: 22rpx;
  border-radius: 26rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.16);
}

.feature-kicker {
  display: block;
  margin-bottom: 12rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.feature-title,
.channel-title,
.action-title {
  display: block;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 800;
}

.feature-desc,
.channel-desc,
.action-desc,
.info-value {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.info-list,
.action-list {
  margin-top: 20rpx;
}

.info-row,
.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.82);
}

.info-row:last-child,
.action-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-label {
  flex: 0 0 156rpx;
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 700;
}

.info-value {
  flex: 1;
  margin-top: 0;
  text-align: right;
}

.action-copy {
  flex: 1;
}

.update-cta {
  margin-top: 24rpx;
  height: 88rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  box-shadow: 0 16rpx 32rpx rgba(234, 88, 12, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.update-cta-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 800;
}

.copyright {
  padding: 12rpx 0 8rpx;
  text-align: center;
}

.copyright-text {
  font-size: 22rpx;
  color: #94a3b8;
}
</style>
