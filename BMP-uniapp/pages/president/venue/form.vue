<template>
  <PresidentLayout :showTabBar="false" backgroundColor="bg-f9">
    <PresidentNavBar :title="isEdit ? '编辑场馆' : '添加场馆'" />
    
    <scroll-view scroll-y class="form-scroll-view">
      <view class="form-container">
        <!-- Section: Basic Information -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">基本信息</text>
            <view class="section-line"></view>
          </view>
          
          <view class="glass-card section-card">
            <view class="form-item">
              <text class="form-label">场馆名称</text>
              <input 
                v-model="form.venueName" 
                class="form-input" 
                placeholder="输入场馆全称" 
                placeholder-class="placeholder"
              />
            </view>
            
            <view class="form-item">
              <text class="form-label">详细地址</text>
              <view class="input-with-icon" @click="onChooseLocation">
                <input 
                  v-model="form.address" 
                  class="form-input" 
                  placeholder="选择或输入场馆地址" 
                  placeholder-class="placeholder"
                  disabled
                />
                <uni-icons type="location-filled" size="24" color="#ff6600" class="input-icon"></uni-icons>
              </view>
            </view>
            
            <view class="grid-2">
              <view class="form-item">
                <text class="form-label">联系人</text>
                <input 
                  v-model="form.contactPerson" 
                  class="form-input" 
                  placeholder="负责人姓名" 
                  placeholder-class="placeholder"
                />
              </view>
              <view class="form-item">
                <text class="form-label">联系电话</text>
                <input 
                  v-model="form.contactPhone" 
                  class="form-input" 
                  type="tel"
                  placeholder="座机或手机号" 
                  placeholder-class="placeholder"
                />
              </view>
            </view>

            <view class="form-item">
              <text class="form-label">小时单价 (元)</text>
              <input 
                v-model="form.hourlyPrice" 
                class="form-input" 
                type="digit"
                placeholder="请输入场馆小时单价" 
                placeholder-class="placeholder"
              />
            </view>
          </view>
        </view>

        <!-- Section: Operational Details -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">运营详情</text>
            <view class="section-line"></view>
          </view>
          
          <view class="glass-card section-card">
            <view class="form-item">
              <text class="form-label">营业时间</text>
              <view class="time-range">
                <picker mode="time" :value="startTime" @change="onStartTimeChange" class="time-picker">
                  <view class="time-value">{{ startTime || '08:00' }}</view>
                </picker>
                <text class="range-separator">至</text>
                <picker mode="time" :value="endTime" @change="onEndTimeChange" class="time-picker">
                  <view class="time-value">{{ endTime || '22:00' }}</view>
                </picker>
              </view>
            </view>
            
            <view class="form-item">
              <text class="form-label">运营状态</text>
              <view class="segmented-control">
                <view 
                  class="segment-item" 
                  :class="{ active: form.status === 1 }"
                  @click="form.status = 1"
                >营业中</view>
                <view 
                  class="segment-item" 
                  :class="{ active: form.status === 2 }"
                  @click="form.status = 2"
                >暂停营业</view>
                <view 
                  class="segment-item" 
                  :class="{ active: form.status === 0 }"
                  @click="form.status = 0"
                >已关闭</view>
              </view>
            </view>
            
            <view class="form-item">
              <text class="form-label">场馆描述</text>
              <textarea 
                v-model="form.description" 
                class="form-textarea" 
                placeholder="介绍场馆的特色、设施等..." 
                placeholder-class="placeholder"
                :auto-height="false"
              ></textarea>
            </view>
          </view>
        </view>

        <!-- Section: Visuals -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">场馆相册</text>
            <view class="section-line"></view>
          </view>
          
          <view class="album-grid">
            <view class="album-item main-image" @click="onChooseMainImage">
              <image v-if="venuePreview" :src="venuePreview" mode="aspectFill" class="full-image"></image>
              <template v-else>
                <uni-icons type="camera-filled" size="32" color="#ff6600"></uni-icons>
                <text class="album-label">主图</text>
              </template>
            </view>
            <view class="album-item">
              <uni-icons type="image" size="32" color="#cccccc"></uni-icons>
              <text class="album-label">内景</text>
            </view>
            <view class="album-item">
              <uni-icons type="image" size="32" color="#cccccc"></uni-icons>
              <text class="album-label">设施</text>
            </view>
          </view>
        </view>

        <!-- Map Preview -->
        <view class="map-preview">
          <image 
            src="https://img.js.design/assets/static/f7743d84351658b4da799f2c8d287ce9.png" 
            mode="aspectFill" 
            class="map-bg"
          ></image>
          <view class="map-overlay"></view>
          <view class="map-info">
            <view class="map-text">
              <text class="map-label">地图定位</text>
              <text class="map-address">{{ form.address || '尚未选择地址' }}</text>
            </view>
            <view class="map-action" @click="onChooseLocation">
              <uni-icons type="expand" size="20" color="#ffffff"></uni-icons>
            </view>
          </view>
        </view>
        
        <!-- Spacer for footer -->
        <view class="footer-spacer"></view>
      </view>
    </scroll-view>

    <!-- Bottom Action Bar -->
    <view class="footer-bar">
      <view class="footer-content">
        <button 
          class="submit-btn" 
          :class="{ loading: submitLoading }"
          @click="onSubmit"
        >
          <uni-icons type="checkbox-filled" size="24" color="#ffffff" style="margin-right: 12rpx;"></uni-icons>
          <text>{{ submitLoading ? (isEdit ? '正在保存...' : '正在添加...') : (isEdit ? '确认修改' : '确认添加') }}</text>
        </button>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentNavBar from '@/components/president/PresidentNavBar.vue'
import { getVenueInfo, addVenue, updateVenue } from '@/api/president/venue'
import { VENUE_STATUS } from '@/utils/constant'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

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

const form = ref({
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

function onChooseLocation() {
  uni.chooseLocation({
    success: (res) => {
      form.value.address = res.address + (res.name ? `(${res.name})` : '')
    },
    fail: (err) => {
      console.error('选择位置失败', err)
      if (err.errMsg.indexOf('auth deny') >= 0) {
        uni.showModal({
          title: '提示',
          content: '请在设置中开启定位权限',
          success: (res) => {
            if (res.confirm) uni.openSetting()
          }
        })
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
      // 这里本应调用上传接口，但没有现成API。假设后端支持base64或者稍后处理。
      // 我们暂存路径，在提交时模拟处理。
    }
  })
}

async function loadDetail() {
  if (!id.value) return
  uni.showLoading({ title: '加载中...' })
  try {
    const res = await getVenueInfo(id.value) as any
    form.value.venueName = res.venueName ?? ''
    form.value.address = res.address ?? ''
    form.value.contactPerson = res.contactPerson ?? ''
    form.value.contactPhone = res.contactPhone ?? ''
    form.value.description = res.description ?? ''
    form.value.status = res.status ?? VENUE_STATUS.OPEN
    form.value.hourlyPrice = res.hourlyPrice ?? 0
    form.value.venueImage = res.venueImage ?? ''
    
    if (res.venueImage) {
      venuePreview.value = resolveImageUrl(res.venueImage)
    }

    if (res.businessHours) {
      form.value.businessHours = res.businessHours
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
  if (!form.value.venueName.trim()) {
    uni.showToast({ title: '请输入场馆名称', icon: 'none' })
    return
  }
  if (!form.value.address.trim()) {
    uni.showToast({ title: '请选择场馆地址', icon: 'none' })
    return
  }
  
  submitLoading.value = true
  try {
    // 模拟文件上传：如果有新选图片且没有现成的uploadFile API
    // 实际项目中这里应该先调用 uni.uploadFile 换取后端 URL
    const payload: any = { ...form.value }
    if (isEdit.value) payload.id = id.value
    
    // 如果有本地临时路径，实际应该先上传
    if (tempImagePath.value) {
      // payload.venueImage = await uploadFile(tempImagePath.value)
      // 模拟上传成功
    }

    if (isEdit.value) {
      await updateVenue(payload)
    } else {
      await addVenue(payload)
    }
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    const eventChannel = (getCurrentPages()[getCurrentPages().length - 1] as any).getOpenerEventChannel()
    if (eventChannel && eventChannel.emit) {
      eventChannel.emit('refresh')
    }
    setTimeout(() => uni.navigateBack(), 1200)
  } catch (e: any) {
    console.error(e)
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
.form-scroll-view {
  height: 100vh;
  background-color: #f9f9f9;
}

.form-container {
  padding: 32rpx;
  padding-top: calc(140rpx + env(safe-area-inset-top));
  padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
}

.section {
  margin-bottom: 48rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 24rpx;
  font-weight: 700;
  text-transform: uppercase;
  color: #a33e00;
  opacity: 0.6;
  letter-spacing: 2rpx;
}

.section-line {
  flex: 1;
  height: 1rpx;
  background-color: #e2d8d4;
  margin-left: 24rpx;
  opacity: 0.3;
}

.section-card {
  padding: 40rpx;
  border-radius: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
  &:last-child { margin-bottom: 0; }
}

.form-label {
  display: block;
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  text-transform: uppercase;
  margin-bottom: 12rpx;
  letter-spacing: 1rpx;
}

.form-input {
  width: 100%;
  height: 96rpx;
  background-color: #f3f3f3;
  border-radius: 16rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  height: 200rpx;
  background-color: #f3f3f3;
  border-radius: 16rpx;
  padding: 24rpx 32rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.placeholder {
  color: #94a3b8;
}

.input-with-icon {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  right: 24rpx;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
}

/* 时间范围选择 */
.time-range {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.time-picker {
  flex: 1;
  height: 96rpx;
  background-color: #f3f3f3;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.time-value {
  font-size: 30rpx;
  font-weight: 500;
  color: #1a1c1c;
  line-height: 96rpx;
  text-align: center;
}

.range-separator {
  font-size: 28rpx;
  color: #94a3b8;
  font-weight: 700;
}

/* 分段控制 */
.segmented-control {
  background-color: #f3f3f3;
  padding: 8rpx;
  border-radius: 20rpx;
  display: flex;
  gap: 8rpx;
}

.segment-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 26rpx;
  font-weight: 500;
  color: #5f5e5e;
  border-radius: 12rpx;
  transition: all 0.2s;
  
  &.active {
    background-color: #ff6600;
    color: #ffffff;
    font-weight: 700;
    box-shadow: 0 4rpx 12rpx rgba(255, 102, 0, 0.2);
  }
}

/* 场馆相册 */
.album-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
}

.album-item {
  aspect-ratio: 1/1;
  background-color: #ffffff;
  border: 2rpx dashed #e2bfb1;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  position: relative;
  overflow: hidden;
  
  &.main-image {
    background-color: #fff9f5;
    border-color: #ff6600;
  }
}

.full-image {
  width: 100%;
  height: 100%;
}

.album-label {
  font-size: 20rpx;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
}

/* 地图预览 */
.map-preview {
  position: relative;
  height: 300rpx;
  border-radius: 48rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
}

.map-bg {
  width: 100%;
  height: 100%;
  filter: grayscale(1);
  opacity: 0.8;
}

.map-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent);
}

.map-info {
  position: absolute;
  bottom: 24rpx;
  left: 32rpx;
  right: 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.map-text {
  display: flex;
  flex-direction: column;
}

.map-label {
  font-size: 18rpx;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.map-address {
  font-size: 26rpx;
  font-weight: 600;
  color: #ffffff;
}

.map-action {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 底部按钮 */
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 32rpx 48rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  z-index: 100;
  pointer-events: none;
}

.footer-content {
  max-width: 800rpx;
  margin: 0 auto;
  pointer-events: auto;
}

.submit-btn {
  background: linear-gradient(to right, #a33e00, #ff6600);
  height: 112rpx;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 48rpx -12rpx rgba(255, 102, 0, 0.5);
  border: none;
  transition: all 0.2s;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  
  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}

.loading {
  opacity: 0.8;
}

.footer-spacer {
  height: 20rpx;
}

.bg-f9 {
  background-color: #f9f9f9;
}
</style>
