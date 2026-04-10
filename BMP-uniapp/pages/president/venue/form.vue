<template>
  <PresidentLayout :showTabBar="false">
    <view class="kinetic-header">
      <view class="back-btn" @click="onBack">
        <uni-icons type="left" size="24" color="#ff6600"></uni-icons>
      </view>
      <text class="header-title">{{ isEdit ? '编辑场馆' : '添加场馆' }}</text>
    </view>
    
    <scroll-view scroll-y class="kinetic-scroll-view">
      <view class="kinetic-container">
        <!-- Section: Basic Information -->
        <view class="kinetic-section">
          <view class="section-header">
            <text class="section-badge">基本信息</text>
            <view class="section-divider"></view>
          </view>
          
          <view class="kinetic-card">
            <view class="form-field">
              <text class="field-label">场馆名称</text>
              <input 
                v-model="form.venueName" 
                class="field-input" 
                placeholder="输入场馆全称" 
                placeholder-class="kinetic-placeholder"
              />
            </view>
            
            <view class="form-field">
              <text class="field-label">详细地址</text>
              <view class="input-group" @click="onChooseLocation">
                <input 
                  v-model="form.address" 
                  class="field-input pr-80" 
                  placeholder="选点或定位后自动填入" 
                  placeholder-class="kinetic-placeholder"
                  disabled
                />
                <uni-icons type="location-filled" size="22" color="#ff6600" class="group-icon"></uni-icons>
              </view>
              <view class="addr-toolbar">
                <view class="addr-tool" @click.stop="onChooseLocation">地图选点</view>
                <view
                  class="addr-tool addr-tool--primary"
                  :class="{ 'addr-tool--disabled': locating }"
                  @click.stop="onUseCurrentLocation"
                >
                  {{ locating ? '定位中…' : '定位当前' }}
                </view>
              </view>
            </view>
            
            <view class="field-grid">
              <view class="form-field">
                <text class="field-label">联系人</text>
                <input 
                  v-model="form.contactPerson" 
                  class="field-input" 
                  placeholder="负责人姓名" 
                  placeholder-class="kinetic-placeholder"
                />
              </view>
              <view class="form-field">
                <text class="field-label">联系电话</text>
                <input 
                  v-model="form.contactPhone" 
                  class="field-input" 
                  type="tel"
                  placeholder="座机或手机号" 
                  placeholder-class="kinetic-placeholder"
                />
              </view>
            </view>

            <view class="form-field">
              <text class="field-label">小时单价 (元)</text>
              <input 
                v-model="form.hourlyPrice" 
                class="field-input" 
                type="digit"
                placeholder="请输入场馆小时单价" 
                placeholder-class="kinetic-placeholder"
              />
            </view>
          </view>
        </view>

        <!-- Section: Operational Details -->
        <view class="kinetic-section">
          <view class="section-header">
            <text class="section-badge">运营详情</text>
            <view class="section-divider"></view>
          </view>
          
          <view class="kinetic-card">
            <view class="form-field">
              <text class="field-label mb-16">营业时间</text>
              <view class="time-selector">
                <picker mode="time" :value="startTime" @change="onStartTimeChange" class="time-picker-box">
                  <view class="time-box-content">
                    <text class="time-text">{{ startTime || '08:00' }}</text>
                    <uni-icons type="paperplane" size="16" color="#94a3b8"></uni-icons>
                  </view>
                </picker>
                <text class="to-text">至</text>
                <picker mode="time" :value="endTime" @change="onEndTimeChange" class="time-picker-box">
                  <view class="time-box-content">
                    <text class="time-text">{{ endTime || '22:00' }}</text>
                    <uni-icons type="paperplane" size="16" color="#94a3b8"></uni-icons>
                  </view>
                </picker>
              </view>
            </view>
            
            <view class="form-field">
              <text class="field-label mb-16">运营状态</text>
              <view class="status-tabs">
                <view 
                  class="tab-item" 
                  :class="{ active: form.status === 1 }"
                  @click="form.status = 1"
                >营业中</view>
                <view 
                  class="tab-item" 
                  :class="{ active: form.status === 2 }"
                  @click="form.status = 2"
                >暂停营业</view>
                <view 
                  class="tab-item" 
                  :class="{ active: form.status === 0 }"
                  @click="form.status = 0"
                >已关闭</view>
              </view>
            </view>
            
            <view class="form-field">
              <text class="field-label">场馆描述</text>
              <textarea 
                v-model="form.description" 
                class="field-textarea" 
                placeholder="介绍场馆的特色、设施等..." 
                placeholder-class="kinetic-placeholder"
                :auto-height="false"
              ></textarea>
            </view>
          </view>
        </view>

        <!-- Section: Visuals -->
        <view class="kinetic-section">
          <view class="section-header">
            <text class="section-badge">场馆相册</text>
            <view class="section-divider"></view>
          </view>
          
          <view class="photo-grid">
            <!-- Main Photo -->
            <view class="photo-slot primary-slot" @click="onChooseMainImage">
              <image v-if="venuePreview" :src="venuePreview" mode="aspectFill" class="slot-img"></image>
              <view v-else class="slot-placeholder">
                <uni-icons type="camera-filled" size="32" color="#ff6600"></uni-icons>
                <text class="slot-text">主图</text>
              </view>
            </view>
            <!-- Interior Photo -->
            <view class="photo-slot">
              <view class="slot-placeholder">
                <uni-icons type="image" size="32" color="#cccccc"></uni-icons>
                <text class="slot-text">内景</text>
              </view>
            </view>
            <!-- Facility Photo -->
            <view class="photo-slot">
              <view class="slot-placeholder">
                <uni-icons type="image" size="32" color="#cccccc"></uni-icons>
                <text class="slot-text">设施</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Map Preview：有坐标时展示真实地图，否则用占位图 -->
        <view class="map-card">
          <map
            v-if="hasMapCenter && mapLatitude != null && mapLongitude != null"
            class="map-real"
            :latitude="mapLatitude"
            :longitude="mapLongitude"
            :markers="mapMarkers"
            :scale="16"
            :show-location="false"
          />
          <view v-else class="map-placeholder" />
          <view class="map-gradient"></view>
          <view class="map-content">
            <view class="map-meta">
              <text class="meta-label">地图定位</text>
              <text class="meta-value">{{ form.address || '尚未选择场馆地址' }}</text>
            </view>
            <view class="map-btn" @click.stop="onChooseLocation">
              <uni-icons type="expand-filled" size="18" color="#ffffff"></uni-icons>
            </view>
          </view>
        </view>
        
        <view class="safe-area-spacer"></view>
      </view>
    </scroll-view>

    <!-- Floating Action Footer -->
    <view class="action-footer">
      <view class="action-wrapper">
        <button 
          class="kinetic-submit-btn" 
          :class="{ loading: submitLoading }"
          @click="onSubmit"
        >
          <uni-icons type="checkbox-filled" size="24" color="#ffffff"></uni-icons>
          <text class="btn-text">{{ isEdit ? (submitLoading ? '保存中...' : '确认修改') : (submitLoading ? '添加中...' : '确认添加') }}</text>
        </button>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getVenueInfo, addVenue, updateVenue } from '@/api/president/venue'
import { reverseGeocode } from '@/api/map'
import { VENUE_STATUS } from '@/utils/constant'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { safeNavigateBack } from '@/utils/navigation'

type VenueStatus = (typeof VENUE_STATUS)[keyof typeof VENUE_STATUS]

type VenueFormState = {
  venueName: string
  address: string
  contactPerson: string
  contactPhone: string
  businessHours: string
  description: string
  status: VenueStatus
  hourlyPrice: number
  venueImage: string
}

// --- State & Computed ---
const id = computed(() => {
  const pages = getCurrentPages()
  const p = pages[pages.length - 1] as any
  return p?.options?.id ? Number(p.options.id) : 0
})
const isEdit = computed(() => id.value > 0)
const submitLoading = ref(false)

const startTime = ref('08:00')
const endTime = ref('22:00')
const venuePreview = ref('')
const tempImagePath = ref('')
const locating = ref(false)
const mapLatitude = ref<number | null>(null)
const mapLongitude = ref<number | null>(null)

const ADDRESS_MAX = 200

const hasMapCenter = computed(
  () => mapLatitude.value != null && mapLongitude.value != null
)

const mapMarkers = computed(() => {
  if (mapLatitude.value == null || mapLongitude.value == null) return []
  return [
    {
      id: 1,
      latitude: mapLatitude.value,
      longitude: mapLongitude.value,
      iconPath: '/static/map-pin.png',
      width: 28,
      height: 28,
      anchor: { x: 0.5, y: 1 }
    }
  ]
})

const form = ref<VenueFormState>({
  venueName: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  businessHours: '08:00-22:00',
  description: '',
  status: VENUE_STATUS.OPEN,
  hourlyPrice: 0,
  venueImage: ''
})

// --- Lifecycle ---
onMounted(() => {
  if (isEdit.value) loadDetail()
})

// --- Methods ---
function onBack() {
  safeNavigateBack('/pages/president/venue/list')
}
function onStartTimeChange(e: any) {
  startTime.value = e.detail.value
  updateBusinessHours()
}

function onEndTimeChange(e: any) {
  endTime.value = e.detail.value
  updateBusinessHours()
}

function updateBusinessHours() {
  form.value.businessHours = `${startTime.value}-${endTime.value}`
}

function formatAddressLine(address: string, name?: string | null) {
  const line = address + (name ? `(${name})` : '')
  return line.length > ADDRESS_MAX ? line.slice(0, ADDRESS_MAX) : line
}

function applyGeoResult(data: { address: string; title?: string | null; latitude: number; longitude: number }) {
  form.value.address = formatAddressLine(data.address, data.title ?? undefined)
  mapLatitude.value = data.latitude
  mapLongitude.value = data.longitude
}

function onChooseLocation() {
  uni.chooseLocation({
    success: (res) => {
      form.value.address = formatAddressLine(res.address, res.name)
      mapLatitude.value = res.latitude
      mapLongitude.value = res.longitude
    },
    fail: (err) => {
      const msg = (err as any)?.errMsg || ''
      if (msg.includes('cancel')) return
      console.error('选择位置失败', err)
      if (!msg.includes('fail auth deny')) {
        uni.showToast({ title: '打开地图选点失败', icon: 'none' })
      }
    }
  })
}

function onUseCurrentLocation() {
  if (locating.value) return
  locating.value = true
  uni.showLoading({ title: '定位中...' })
  uni.getLocation({
    type: 'gcj02',
    isHighAccuracy: true,
    success: async (loc) => {
      try {
        const data = await reverseGeocode(loc.latitude, loc.longitude)
        applyGeoResult(data)
        uni.showToast({ title: '已填入当前位置', icon: 'success' })
      } catch {
        // request 内已对业务/网络错误弹出提示
      } finally {
        uni.hideLoading()
        locating.value = false
      }
    },
    fail: (err) => {
      uni.hideLoading()
      locating.value = false
      const msg = (err as any)?.errMsg || ''
      if (msg.includes('requiredPrivateInfos')) {
        uni.showToast({
          title: '请重新编译小程序并勾选「地理位置」隐私协议',
          icon: 'none',
          duration: 3500
        })
        console.warn('getLocation: 请在 manifest 的 requiredPrivateInfos 中声明 getLocation 后重新编译')
        return
      }
      console.error('getLocation fail', err)
      if (msg.includes('auth deny')) {
        uni.showToast({ title: '请在设置中允许位置权限', icon: 'none' })
      } else {
        uni.showToast({ title: '获取定位失败', icon: 'none' })
      }
    }
  })
}

function onChooseMainImage() {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      tempImagePath.value = res.tempFilePaths[0]
      venuePreview.value = res.tempFilePaths[0]
      // Simulating immediate upload if backend expects URL
    }
  })
}

async function loadDetail() {
  if (!id.value) return
  uni.showLoading({ title: '加载中...' })
  try {
    const res = await getVenueInfo(id.value) as any
    Object.assign(form.value, {
      venueName: res.venueName ?? '',
      address: res.address ?? '',
      contactPerson: res.contactPerson ?? '',
      contactPhone: res.contactPhone ?? '',
      description: res.description ?? '',
      status: res.status ?? VENUE_STATUS.OPEN,
      hourlyPrice: res.hourlyPrice ?? 0,
      venueImage: res.venueImage ?? ''
    })
    
    if (res.venueImage) {
      venuePreview.value = resolveImageUrl(res.venueImage)
    }

    if (res.businessHours) {
      const parts = res.businessHours.split('-')
      if (parts.length === 2) {
        startTime.value = parts[0]
        endTime.value = parts[1]
      }
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载详情失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

async function onSubmit() {
  if (!form.value.venueName.trim()) return uni.showToast({ title: '请输入场馆名称', icon: 'none' })
  if (!form.value.address.trim()) return uni.showToast({ title: '请选择场馆地址', icon: 'none' })
  
  submitLoading.value = true
  try {
    const payload: any = { ...form.value }
    if (isEdit.value) payload.id = id.value
    
    // In a real scenario, handle image upload here
    
    if (isEdit.value) {
      await updateVenue(payload)
    } else {
      await addVenue(payload)
    }
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    const eventChannel = (getCurrentPages()[getCurrentPages().length - 1] as any).getOpenerEventChannel()
    if (eventChannel?.emit) eventChannel.emit('refresh')
    
    setTimeout(() => safeNavigateBack('/pages/president/venue/list'), 1000)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'none' })
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.kinetic-scroll-view {
  height: 100vh;
  background-color: #f9f9f9;
}

.kinetic-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 24rpx 32rpx;
  padding-top: calc(80rpx + env(safe-area-inset-top));
  display: flex;
  align-items: center;
  background-color: #f9f9f9;
}

.back-btn {
  padding: 12rpx;
  margin-right: 12rpx;
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 38rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.kinetic-container {
  padding: 32rpx;
  padding-top: calc(200rpx + env(safe-area-inset-top));
  padding-bottom: calc(240rpx + env(safe-area-inset-bottom));
}

.kinetic-section {
  margin-bottom: 48rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-badge {
  font-size: 20rpx;
  font-weight: 800;
  text-transform: uppercase;
  color: #7c2e00; // Primary dark variant
  opacity: 0.6;
  letter-spacing: 4rpx;
}

.section-divider {
  flex: 1;
  height: 2rpx;
  background-color: #e3bfb1;
  margin-left: 24rpx;
  opacity: 0.2;
}

.kinetic-card {
  background-color: #ffffff;
  border-radius: 32rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.02);
}

.form-field {
  margin-bottom: 40rpx;
  &:last-child { margin-bottom: 0; }
}

.field-label {
  display: block;
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  text-transform: uppercase;
  margin-bottom: 12rpx;
  letter-spacing: 2rpx;
  
  &.mb-16 { margin-bottom: 24rpx; }
}

.field-input {
  width: 100%;
  height: 100rpx;
  background-color: #f3f3f3;
  border-radius: 20rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-sizing: border-box;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:focus {
    background-color: #ffffff;
    box-shadow: 0 0 0 4rpx rgba(255, 102, 0, 0.1);
  }
}

.field-textarea {
  width: 100%;
  height: 240rpx;
  background-color: #f3f3f3;
  border-radius: 24rpx;
  padding: 32rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.kinetic-placeholder {
  color: #cbd5e1;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.group-icon {
  position: absolute;
  right: 32rpx;
}

.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
}

.pr-80 {
  padding-right: 80rpx;
}

/* Time Selector */
.time-selector {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.time-picker-box {
  flex: 1;
  height: 100rpx;
  background-color: #f3f3f3;
  border-radius: 20rpx;
}

.time-box-content {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
}

.time-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.to-text {
  font-size: 24rpx;
  color: #94a3b8;
  font-weight: 800;
}

/* Status Tabs (Segmented Control) */
.status-tabs {
  background-color: #f3f3f3;
  padding: 8rpx;
  border-radius: 24rpx;
  display: flex;
  gap: 8rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 26rpx;
  font-weight: 600;
  color: #5f5e5e;
  border-radius: 16rpx;
  transition: all 0.3s;
  
  &.active {
    background-color: #ff6600;
    color: #ffffff;
    box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.2);
  }
}

/* Photo Grid */
.photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
}

.photo-slot {
  aspect-ratio: 1/1;
  background-color: #ffffff;
  border: 4rpx dashed rgba(227, 191, 177, 0.3);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  transition: all 0.2s;
  
  &:active { transform: scale(0.96); }
  
  &.primary-slot {
    border-color: #ff6600;
  }
}

.slot-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.slot-text {
  font-size: 18rpx;
  font-weight: 800;
  color: #94a3b8;
  text-transform: uppercase;
}

.slot-img {
  width: 100%;
  height: 100%;
}

/* Map Card */
.map-card {
  position: relative;
  height: 380rpx;
  border-radius: 56rpx;
  overflow: hidden;
  margin-top: 48rpx;
  box-shadow: 0 20rpx 50rpx -15rpx rgba(0, 0, 0, 0.15);
}

.map-real {
  width: 100%;
  height: 100%;
  display: block;
}

.map-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, #dfe6ef 0%, #c5d0e0 45%, #e8edf4 100%);
}

.addr-toolbar {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.addr-tool {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 24rpx;
  font-weight: 700;
  color: #5f5e5e;
  background-color: #f3f3f3;
  border-radius: 16rpx;
}

.addr-tool--primary {
  color: #ffffff;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.2);
}

.addr-tool--disabled {
  opacity: 0.55;
  pointer-events: none;
}

.map-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(26, 28, 28, 0.7), transparent 60%);
}

.map-content {
  position: absolute;
  bottom: 40rpx;
  left: 48rpx;
  right: 48rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.map-meta {
  flex: 1;
  margin-right: 24rpx;
}

.meta-label {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 4rpx;
  margin-bottom: 8rpx;
}

.meta-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
}

.map-btn {
  width: 80rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12rpx);
  -webkit-backdrop-filter: blur(12rpx);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Action Footer */
.action-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32rpx 48rpx;
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
  z-index: 1000;
  pointer-events: none;
  box-sizing: border-box;
}

.action-wrapper {
  max-width: 100%;
  margin: 0 auto;
  pointer-events: auto;
  display: flex;
  justify-content: center;
}

.kinetic-submit-btn {
  width: 100%;
  height: 110rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  border-radius: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 20rpx 40rpx rgba(255, 102, 0, 0.3);
  border: none;
  transition: all 0.3s;
  
  &:active { transform: scale(0.97); opacity: 0.95; }
}

.btn-text {
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}

.loading {
  opacity: 0.7;
}

.safe-area-spacer {
  height: 20rpx;
}
</style>
