<template>
  <PresidentLayout :showTabBar="false">
    <PresidentNavBar :title="isEdit ? '编辑场馆' : '添加场馆'" />
    <view class="form-content">
      <view class="form-card glass-card">
        <view class="form-item">
          <text class="form-label">场馆名称</text>
          <input v-model="form.venueName" class="form-input" placeholder="请输入场馆名称" />
        </view>
        <view class="form-item">
          <text class="form-label">地址</text>
          <input v-model="form.address" class="form-input" placeholder="选填" />
        </view>
        <view class="form-item">
          <text class="form-label">联系人</text>
          <input v-model="form.contactPerson" class="form-input" placeholder="选填" />
        </view>
        <view class="form-item">
          <text class="form-label">联系电话</text>
          <input v-model="form.contactPhone" class="form-input" type="number" placeholder="选填" />
        </view>
        <view class="form-item">
          <text class="form-label">营业时间</text>
          <input v-model="form.businessHours" class="form-input" placeholder="如 8:00-22:00" />
        </view>
        <view class="form-item">
          <text class="form-label">状态</text>
          <picker mode="selector" :range="['营业中', '关闭']" :value="form.status === 1 ? 0 : 1" @change="onStatusChange">
            <view class="form-picker">{{ form.status === 1 ? '营业中' : '关闭' }}</view>
          </picker>
        </view>
        <view class="btn-row">
          <view class="btn-submit" @click="onSubmit" :class="{ loading: submitLoading }">{{ submitLoading ? '提交中...' : '保存' }}</view>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentNavBar from '@/components/president/PresidentNavBar.vue'
import { getVenueInfo, addVenue, updateVenue } from '@/api/president/venue'

const id = computed(() => {
  const pages = getCurrentPages()
  const p = pages[pages.length - 1] as any
  return p?.options?.id ? Number(p.options.id) : 0
})
const isEdit = computed(() => id.value > 0)
const submitLoading = ref(false)
const form = ref({
  venueName: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  businessHours: '',
  status: 1
})

function onStatusChange(e: any) {
  form.value.status = e.detail?.value === '0' ? 1 : 0
}

async function loadDetail() {
  if (!id.value) return
  try {
    const res = await getVenueInfo(id.value) as any
    form.value.venueName = res.venueName ?? ''
    form.value.address = res.address ?? ''
    form.value.contactPerson = res.contactPerson ?? ''
    form.value.contactPhone = res.contactPhone ?? ''
    form.value.businessHours = res.businessHours ?? ''
    form.value.status = res.status ?? 1
  } catch (e) {
    console.error(e)
  }
}

async function onSubmit() {
  if (!form.value.venueName.trim()) {
    uni.showToast({ title: '请输入场馆名称', icon: 'none' })
    return
  }
  submitLoading.value = true
  try {
    const payload: any = { ...form.value }
    if (isEdit.value) payload.id = id.value
    if (isEdit.value) await updateVenue(payload)
    else await addVenue(payload)
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch (e: any) {
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
.form-content { padding: 24rpx; padding-top: calc(120rpx + env(safe-area-inset-top)); }
.form-card { padding: 32rpx; border-radius: 24rpx; }
.form-item { margin-bottom: 32rpx; }
.form-label { display: block; font-size: 28rpx; color: #475569; margin-bottom: 12rpx; }
.form-input, .form-picker {
  width: 100%; height: 80rpx; padding: 0 24rpx; background: rgba(255,255,255,0.6);
  border-radius: 12rpx; font-size: 28rpx; box-sizing: border-box;
}
.form-picker { display: flex; align-items: center; }
.btn-row { margin-top: 48rpx; }
.btn-submit {
  width: 100%; height: 88rpx; line-height: 88rpx; text-align: center;
  background: linear-gradient(135deg, #3cc51f, #4ade80); color: #fff;
  border-radius: 16rpx; font-size: 32rpx;
}
.btn-submit.loading { opacity: 0.8; }
</style>
