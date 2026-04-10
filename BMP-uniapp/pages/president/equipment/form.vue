<template>
  <PresidentLayout :showTabBar="false">
    <view class="equipment-form-page">
      <view class="status-bar-placeholder" />

      <view class="top-shell">
        <view class="brand-left">
          <image class="president-avatar" :src="presidentAvatar" mode="aspectFill" />
          <text class="brand-title">KINETIC LOGIC</text>
        </view>
        <view class="notify-btn">
          <uni-icons type="notification" size="20" color="#71717a" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="head-row">
          <text class="page-title">{{ isEdit ? '编辑器材' : '添加器材' }}</text>
          <text class="page-sub">{{ isEdit ? '修改条目' : '新增条目' }} • 系统器材</text>
        </view>

        <view class="card card-basic">
          <text class="card-title">基本信息</text>
          <view class="form-grid">
            <view class="field">
              <text class="label">器材编码</text>
              <input class="input" v-model="form.equipmentCode" placeholder="KL-RAC-001" maxlength="50" />
            </view>
            <view class="field">
              <text class="label">器材名称</text>
              <input class="input" v-model="form.equipmentName" placeholder="Professional Graphite Racket" maxlength="100" />
            </view>
            <view class="field">
              <text class="label">所属分类</text>
              <picker mode="selector" :range="typeOptions" :value="typeIndex" @change="onTypeChange">
                <view class="input picker-row">
                  <text>{{ typeOptions[typeIndex] }}</text>
                  <uni-icons type="arrowdown" size="14" color="#71717a" />
                </view>
              </picker>
            </view>
            <view class="field">
              <text class="label">品牌</text>
              <input class="input" v-model="form.brand" placeholder="Yonex, Li-Ning, etc." maxlength="50" />
            </view>
          </view>
        </view>

        <view class="grid-2">
          <view class="card">
            <text class="card-title">定价信息</text>
            <view class="field">
              <text class="hint">采购价格</text>
              <view class="money-row">
                <text class="yen">¥</text>
                <input class="money-input" type="digit" v-model="form.price" />
              </view>
            </view>
            <view class="field">
              <text class="hint">租借价格</text>
              <view class="money-row">
                <text class="yen">¥</text>
                <input class="money-input" type="digit" v-model="form.rentalPrice" />
              </view>
            </view>
          </view>

          <view class="card">
            <text class="card-title">数量与状态</text>
            <view class="qty-grid">
              <view class="field">
                <text class="hint">总数量</text>
                <input class="input center" type="number" v-model="form.totalQuantity" />
              </view>
              <view class="field">
                <text class="hint">可用数量</text>
                <input class="input center" type="number" v-model="form.availableQuantity" />
              </view>
            </view>
            <view class="switch-row">
              <view>
                <text class="switch-title">系统状态</text>
                <text class="switch-sub">切换器材的预约状态</text>
              </view>
              <switch :checked="form.status === 1" color="#a33e00" @change="onStatusChange" />
            </view>
          </view>
        </view>

        <view class="card">
          <text class="card-title">器材描述</text>
          <textarea class="textarea" v-model="form.description" placeholder="请输入器材的高性能规格及使用指南..." maxlength="500" />
          <text class="card-title mtop">器材图片</text>
          <view class="img-grid">
            <view class="upload-box" @click="uploadImage">
              <uni-icons type="image" size="24" color="#71717a" />
              <text>点击上传</text>
            </view>
            <view class="preview" v-if="form.equipmentImage">
              <image class="preview-img" :src="form.equipmentImage" mode="aspectFill" />
            </view>
          </view>
        </view>

        <view class="actions">
          <view class="btn ghost" @click="onCancel">取消</view>
          <view class="btn primary" :class="{ disabled: submitting }" @click="onSubmit">
            <uni-icons type="wallet" size="16" color="#561d00" />
            <text>保存器材</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { addEquipment, getEquipmentDetail, updateEquipment } from '@/api/equipment'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const presidentAvatar = computed(() => userStore.userInfo?.avatar || '/static/placeholders/avatar.svg')

const typeOptions = ['Racket', 'Shuttlecock', 'String', 'Other']
const typeMap = ['RACKET', 'SHUTTLE', 'STRING', 'OTHER'] as const
const typeIndex = ref(0)
const submitting = ref(false)
const isEdit = ref(false)
const id = ref<number | null>(null)

const form = reactive({
  equipmentCode: '',
  equipmentName: '',
  brand: '',
  price: '',
  rentalPrice: '',
  totalQuantity: '10',
  availableQuantity: '10',
  status: 1,
  description: '',
  equipmentImage: ''
})

function onTypeChange(e: { detail: { value: string } }) {
  typeIndex.value = Number(e.detail.value)
}

function onStatusChange(e: { detail: { value: boolean } }) {
  form.status = e.detail.value ? 1 : 0
}

function onCancel() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_LIST)
}

function uploadImage() {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      form.equipmentImage = res.tempFilePaths?.[0] || ''
    }
  })
}

function validate() {
  if (!form.equipmentCode.trim()) return '请填写器材编码'
  if (!form.equipmentName.trim()) return '请填写器材名称'
  if (!form.price || Number(form.price) < 0) return '请填写有效采购价格'
  if (!form.totalQuantity || Number(form.totalQuantity) < 0) return '请填写有效总数量'
  if (!form.availableQuantity || Number(form.availableQuantity) < 0) return '请填写有效可用数量'
  return ''
}

async function onSubmit() {
  if (submitting.value) return
  const err = validate()
  if (err) {
    uni.showToast({ title: err, icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const payload = {
      id: id.value ?? undefined,
      equipmentCode: form.equipmentCode.trim(),
      equipmentName: form.equipmentName.trim(),
      equipmentType: typeMap[typeIndex.value],
      brand: form.brand.trim(),
      price: Number(form.price),
      rentalPrice: form.rentalPrice ? Number(form.rentalPrice) : 0,
      totalQuantity: Number(form.totalQuantity),
      availableQuantity: Number(form.availableQuantity),
      status: form.status,
      description: form.description.trim(),
      equipmentImage: form.equipmentImage
    } as const
    if (isEdit.value) {
      await updateEquipment(payload)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await addEquipment(payload)
      uni.showToast({ title: '新增成功', icon: 'success' })
    }
    setTimeout(() => onCancel(), 500)
  } catch {
    // request 已统一toast
  } finally {
    submitting.value = false
  }
}

onLoad(async (q?: Record<string, string | undefined>) => {
  const raw = q?.id
  const n = raw ? parseInt(String(raw), 10) : NaN
  if (Number.isFinite(n) && n > 0) {
    id.value = n
    isEdit.value = true
    try {
      const info = await getEquipmentDetail(n)
      form.equipmentCode = info.equipmentCode || ''
      form.equipmentName = info.equipmentName || ''
      form.brand = info.brand || ''
      form.price = String(info.price ?? '')
      form.rentalPrice = String(info.rentalPrice ?? '')
      form.totalQuantity = String(info.totalQuantity ?? 0)
      form.availableQuantity = String(info.availableQuantity ?? 0)
      form.status = info.status ?? 1
      form.description = info.description || ''
      form.equipmentImage = (info as any).equipmentImage || ''
      const idx = typeMap.findIndex((t) => t === (info.equipmentType as any))
      typeIndex.value = idx >= 0 ? idx : 0
    } catch {
      // ignore
    }
  }
})
</script>

<style lang="scss" scoped>
.equipment-form-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-shell { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 24rpx 8rpx; }
.brand-left { display: flex; align-items: center; gap: 12rpx; }
.president-avatar { width: 48rpx; height: 48rpx; border-radius: 9999px; }
.brand-title { font-size: 34rpx; color: #ea580c; font-weight: 800; letter-spacing: -0.02em; }
.notify-btn { width: 52rpx; height: 52rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; }
.scroll { height: calc(100vh - var(--status-bar-height) - 84rpx); padding: 14rpx 24rpx 30rpx; box-sizing: border-box; }
.head-row { display: flex; justify-content: space-between; align-items: baseline; margin: 6rpx 0 18rpx; }
.page-title { font-size: 54rpx; font-weight: 900; letter-spacing: -0.03em; }
.page-sub { color: #a33e00; font-size: 18rpx; font-weight: 800; }
.card { background: #fff; border-radius: 20rpx; padding: 20rpx; box-shadow: 0 4rpx 14rpx rgba(2,6,23,.04); margin-bottom: 14rpx; }
.card-basic { border-left: 6rpx solid #a33e00; }
.card-title { display: block; margin-bottom: 16rpx; color: #5a4136; font-size: 18rpx; font-weight: 800; letter-spacing: .08em; text-transform: uppercase; }
.mtop { margin-top: 16rpx; }
.form-grid { display: grid; grid-template-columns: 1fr; gap: 14rpx; }
.grid-2 { display: grid; grid-template-columns: 1fr; gap: 14rpx; }
@media screen and (min-width: 768px) {
  .form-grid { grid-template-columns: 1fr 1fr; }
  .grid-2 { grid-template-columns: 1fr 1fr; }
}
.field { display: flex; flex-direction: column; gap: 8rpx; }
.label { font-size: 16rpx; font-weight: 700; color: #5a4136; letter-spacing: .08em; text-transform: uppercase; }
.hint { font-size: 20rpx; color: #71717a; }
.input, .textarea {
  background: #f3f3f3; border-radius: 12rpx; min-height: 74rpx; padding: 0 16rpx; font-size: 24rpx;
  display: flex; align-items: center;
}
.picker-row { justify-content: space-between; }
.center { text-align: center; font-weight: 800; }
.money-row { display: flex; align-items: center; gap: 10rpx; border-bottom: 2rpx solid #e3bfb1; padding-bottom: 8rpx; }
.yen { color: #a33e00; font-size: 30rpx; font-weight: 900; }
.money-input { flex: 1; font-size: 38rpx; font-weight: 900; }
.qty-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10rpx; }
.switch-row { margin-top: 12rpx; background: #f3f3f3; border-radius: 14rpx; padding: 14rpx; display: flex; justify-content: space-between; align-items: center; }
.switch-title { display: block; font-size: 24rpx; font-weight: 800; }
.switch-sub { display: block; font-size: 18rpx; color: #71717a; margin-top: 2rpx; }
.textarea { min-height: 160rpx; padding: 12rpx 16rpx; }
.img-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10rpx; }
.upload-box, .preview {
  aspect-ratio: 1; border-radius: 14rpx; background: #f3f3f3; display: flex; align-items: center; justify-content: center; flex-direction: column;
}
.upload-box { border: 2rpx dashed #e3bfb1; color: #71717a; }
.upload-box text { font-size: 16rpx; font-weight: 700; margin-top: 2rpx; }
.preview-img { width: 100%; height: 100%; border-radius: 14rpx; }
.actions { display: grid; grid-template-columns: 1fr 1.4fr; gap: 10rpx; margin-top: 14rpx; }
.btn {
  height: 86rpx; border-radius: 14rpx; display: flex; align-items: center; justify-content: center; gap: 8rpx; font-size: 26rpx; font-weight: 800;
}
.btn.ghost { background: #e5e7eb; color: #1f2937; }
.btn.primary { background: #ff6600; color: #561d00; }
.btn.primary.disabled { opacity: .6; pointer-events: none; }
</style>
