<template>
  <view class="venue-detail-page">
    <!-- Top Navigation -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-btn" @click="handleBack">
          <text class="material-symbols-outlined">arrow_back</text>
        </view>
        <text class="header-title">场馆管理</text>
        <view class="spacer"></view>
      </view>
    </view>

    <view v-if="loading" class="loading-state">
      <view class="spinner"></view>
      <text>加载中...</text>
    </view>

    <scroll-view v-else-if="detail" class="main-scroll" scroll-y :style="{ height: scrollHeight }">
      <!-- Hero Section -->
      <view class="hero-section">
        <image 
          class="hero-image" 
          :src="resolveImageUrl(detail.venueImage)" 
          mode="aspectFill"
          v-if="detail.venueImage"
        ></image>
        <view v-else class="hero-image-placeholder">
          <text class="material-symbols-outlined">image</text>
        </view>
        <view class="hero-gradient"></view>
        <!-- Floating Status Chip -->
        <view class="status-chip-wrap">
          <text class="status-chip" :class="{ 'status-offline': detail.status !== 1 }">
            {{ detail.status === 1 ? '营业中' : '已关闭' }}
          </text>
        </view>
      </view>

      <!-- Main Content -->
      <view class="content-body">
        <!-- Header Info -->
        <view class="venue-header-info">
          <view class="name-row">
            <text class="venue-name">{{ detail.venueName }}</text>
          </view>
          <view class="rating-row">
            <view class="rating-box">
              <text class="material-symbols-outlined fill-icon">star</text>
              <text class="rating-text">PROFESSIONAL</text>
            </view>
            <view class="dot-separator"></view>
            <text class="grade-text">ID: {{ detail.id }}</text>
          </view>
        </view>

        <!-- Bento Grid Info -->
        <view class="info-grid">
          <!-- Address -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">location_on</text>
            </view>
            <view class="card-content">
              <text class="card-label">地址</text>
              <text class="card-value">{{ detail.address || '暂无地址信息' }}</text>
            </view>
          </view>

          <!-- Contact -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">person</text>
            </view>
            <view class="card-content">
              <text class="card-label">联系人 / 电话</text>
              <view class="contact-row">
                <view class="contact-info">
                  <text class="contact-name">{{ detail.contactPerson || '管理员' }}</text>
                  <text class="contact-phone">{{ detail.contactPhone || '暂无联系电话' }}</text>
                </view>
                <view v-if="detail.contactPhone" class="call-btn" @click="handleCall(detail.contactPhone)">
                  <text class="material-symbols-outlined">call</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Hours -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">schedule</text>
            </view>
            <view class="card-content">
              <text class="card-label">营业时间</text>
              <text class="card-value-bold">{{ detail.businessHours || '08:00 - 22:30' }}</text>
            </view>
          </view>

          <!-- Status -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">settings</text>
            </view>
            <view class="card-content">
              <text class="card-label">运营状态</text>
              <text class="card-value-bold" :style="{ color: detail.status === 1 ? '#0d9488' : '#ba1a1a' }">
                {{ detail.status === 1 ? '正常营业' : '暂停服务' }}
              </text>
            </view>
          </view>
        </view>

        <!-- Description Section -->
        <view class="description-section">
          <text class="section-title">场馆描述</text>
          <view class="description-box">
            <text class="description-text">
              {{ detail.description || '暂无描述信息。' }}
            </text>
          </view>
        </view>

        <!-- Bottom Spacer -->
        <view class="bottom-safe-spacer"></view>
      </view>
    </scroll-view>
    <view v-else class="empty-state">
      <text>场馆不存在</text>
    </view>

    <!-- Bottom Navigation -->
    <view class="bottom-nav">
      <view class="bottom-nav-glass"></view>
      <view class="bottom-nav-content">
        <view class="secondary-actions">
          <view class="action-item delete-action" @click="onDelete">
            <text class="material-symbols-outlined">delete</text>
            <text class="action-label">DELETE</text>
          </view>
          <view class="action-item edit-action" @click="goEdit">
            <text class="material-symbols-outlined">edit</text>
            <text class="action-label">EDIT</text>
          </view>
          <view class="action-item" @click="handleShare">
            <text class="material-symbols-outlined">share</text>
            <text class="action-label">SHARE</text>
          </view>
        </view>
        <view class="primary-cta" @click="handleBook">
          <text class="cta-text">立即预约</text>
          <text class="material-symbols-outlined cta-icon">arrow_forward_ios</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getVenueInfo, deleteVenue, type VenueItem } from '@/api/president/venue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

// System info for custom header
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value + 44}px)`)

const id = computed(() => {
  const pages = getCurrentPages()
  const p = pages[pages.length - 1] as any
  return p?.options?.id ? Number(p.options.id) : 0
})

const loading = ref(true)
const detail = ref<VenueItem | null>(null)

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await getVenueInfo(id.value)
    detail.value = (res as VenueItem) || null
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  uni.navigateBack()
}

const handleCall = (phone: string) => {
  uni.makePhoneCall({
    phoneNumber: phone
  })
}

function goEdit() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.VENUE_FORM}?id=${id.value}` })
}

const handleBook = () => {
  uni.navigateTo({
    url: `/pages/booking/create?venueId=${detail.value?.id}`
  })
}

function onDelete() {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除场馆「${detail.value?.venueName}」吗？`,
    confirmColor: '#ba1a1a',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteVenue(id.value)
        uni.showToast({ title: '删除成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch (e: any) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'none' })
      }
    }
  })
}

const handleShare = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

onMounted(() => load())
</script>

<style lang="scss" scoped>
.material-symbols-outlined {
  font-family: 'Material Symbols Outlined';
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}

.fill-icon {
  font-variation-settings: 'FILL' 1;
}

.venue-detail-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
}

/* Header */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: rgba(249, 249, 249, 0.8);
  backdrop-filter: blur(10px);
}

.header-content {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.back-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6600;
  transition: transform 0.2s;
  &:active { transform: scale(0.9); }
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1c1c;
}

.spacer { width: 40px; }

/* Scroll Area */
.main-scroll {
  flex: 1;
  margin-top: calc(env(safe-area-inset-top) + 44px);
}

/* Hero Section */
.hero-section {
  position: relative;
  width: 100%;
  height: 397px;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-image-placeholder {
  width: 100%;
  height: 100%;
  background-color: #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 48px;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(25, 26, 26, 0.2) 0%, rgba(25, 26, 26, 0) 40%, rgba(249, 249, 249, 1) 100%);
}

.status-chip-wrap {
  position: absolute;
  bottom: 32px;
  left: 24px;
}

.status-chip {
  background-color: #0d9488;
  color: #fff;
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.3);
  
  &.status-offline {
    background-color: #ba1a1a;
    box-shadow: 0 4px 12px rgba(186, 26, 26, 0.3);
  }
}

/* Content Body */
.content-body {
  padding: 0 24px;
  position: relative;
  z-index: 10;
  margin-top: -20px;
}

.venue-header-info {
  margin-bottom: 32px;
}

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.venue-name {
  font-size: 28px;
  font-weight: 700;
  color: #1a1c1c;
  line-height: 1.2;
}

.rating-row {
  display: flex;
  align-items: center;
}

.rating-box {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ff6600;
}

.rating-text { font-size: 14px; font-weight: 700; }

.dot-separator {
  width: 4px;
  height: 4px;
  background-color: #e2e2e2;
  border-radius: 50%;
  margin: 0 16px;
}

.grade-text {
  font-size: 12px;
  font-weight: 600;
  color: #636262;
  text-transform: uppercase;
}

/* Bento Grid */
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.info-card {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 16px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.card-icon-box {
  background-color: #f3f3f3;
  padding: 12px;
  border-radius: 12px;
  color: #ff6600;
  display: flex;
  align-items: center;
  justify-content: center;
  .material-symbols-outlined { font-size: 20px; }
}

.card-content { flex: 1; }
.card-label { font-size: 10px; font-weight: 700; color: #636262; text-transform: uppercase; margin-bottom: 4px; display: block; }
.card-value { font-size: 14px; font-weight: 500; color: #1a1c1c; line-height: 1.5; }
.card-value-bold { font-size: 14px; font-weight: 700; color: #1a1c1c; }

.contact-row { display: flex; justify-content: space-between; align-items: center; }
.contact-info { display: flex; flex-direction: column; }
.contact-name { font-size: 14px; font-weight: 700; color: #1a1c1c; }
.contact-phone { font-size: 14px; font-weight: 500; color: #636262; }

.call-btn {
  width: 36px;
  height: 36px;
  background-color: #ff6600;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  .material-symbols-outlined { font-size: 16px; }
}

/* Description */
.description-section { margin-bottom: 32px; }
.section-title { font-size: 10px; font-weight: 700; color: #636262; text-transform: uppercase; letter-spacing: 3px; margin-bottom: 16px; display: block; }
.description-box { background-color: #f3f3f3; padding: 24px; border-radius: 20px; border-left: 4px solid #ff6600; }
.description-text { font-size: 14px; color: #5a4136; line-height: 1.6; font-weight: 500; }

.bottom-safe-spacer { height: 120px; }

/* Bottom Nav */
.bottom-nav { position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; padding-bottom: env(safe-area-inset-bottom); }
.bottom-nav-glass { position: absolute; inset: 0; background-color: rgba(255, 255, 255, 0.8); backdrop-filter: blur(20px); border-top-left-radius: 40px; border-top-right-radius: 40px; box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.05); }
.bottom-nav-content { position: relative; display: flex; justify-content: space-between; align-items: center; padding: 24px 32px 10px; }

.secondary-actions { display: flex; gap: 16px; }
.action-item { display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94a3b8; padding: 12px; .material-symbols-outlined { font-size: 24px; } }
.action-label { font-size: 8px; font-weight: 700; margin-top: 4px; }
.delete-action { &:active { color: #ba1a1a; } }
.edit-action { &:active { color: #ff6600; } }

.primary-cta {
  background: linear-gradient(45deg, #a33e00 0%, #ff6600 100%);
  padding: 16px 32px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 8px 16px rgba(163, 62, 0, 0.2);
  &:active { transform: scale(0.95); }
}
.cta-text { color: #ffffff; font-size: 14px; font-weight: 700; }
.cta-icon { color: #ffffff; font-size: 16px; }

.loading-state, .empty-state { padding: 100px 0; text-align: center; color: #636262; }
.spinner { width: 40px; height: 40px; border: 4px solid #eee; border-top-color: #ff6600; border-radius: 50%; animation: rotate 1s linear infinite; margin: 0 auto 20px; }
@keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
