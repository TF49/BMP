import { ref } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getInfo, updateUserInfo } from '@/api/login'

/**
 * 当前登录用户个人资料类型
 */
export interface CurrentUserProfile {
  id?: number | string
  username: string
  role: string
  phone?: string
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | ''
  avatar?: string
  signature?: string
  createTime?: string
}

interface UseUserProfileReturn {
  loading: Ref<boolean>
  userProfile: Ref<CurrentUserProfile>
  originalProfile: Ref<CurrentUserProfile>
  loadProfile: () => Promise<void>
  resetProfile: () => void
  saveProfile: () => Promise<void>
}

/**
 * 封装当前登录用户个人资料的获取与更新逻辑
 * - 统一从 /api/auth/current 获取当前用户信息
 * - 统一通过 /api/auth/update 更新当前用户信息
 */
export function useUserProfile(): UseUserProfileReturn {
  const loading = ref(false)

  const userProfile = ref<CurrentUserProfile>({
    username: '',
    role: 'USER',
    phone: '',
    gender: '',
    avatar: '',
    signature: '',
    createTime: ''
  })

  const originalProfile = ref<CurrentUserProfile>({ ...userProfile.value })

  /**
   * 将接口返回的用户信息映射到前端使用的字段
   */
  const mapServerUserToProfile = (data: any): CurrentUserProfile => {
    if (!data) {
      return { ...userProfile.value }
    }
    return {
      id: data.id ?? data.userId,
      username: data.username ?? data.account ?? '',
      role: data.role ?? data.roleKey ?? 'USER',
      phone: data.phone ?? data.mobile ?? '',
      gender: data.gender ?? data.sex ?? '',
      avatar: data.avatar ?? '',
      signature: data.signature ?? data.intro ?? '',
      createTime: data.createTime ?? data.createdAt ?? ''
    }
  }

  /**
   * 将表单中的字段映射为后端需要的结构（仅可编辑字段：phone、gender、avatar、signature）
   */
  const mapProfileToServerPayload = (profile: CurrentUserProfile): any => {
    return {
      phone: profile.phone ?? '',
      gender: profile.gender ?? '',
      avatar: profile.avatar ?? '',
      signature: profile.signature ?? ''
    }
  }

  /**
   * 加载当前用户资料
   */
  const loadProfile = async () => {
    loading.value = true
    try {
      const res: any = await getInfo()
      if (res && res.code === 200) {
        const raw = res.data || res.user || res.userInfo || res
        const mapped = mapServerUserToProfile(raw)
        userProfile.value = mapped
        originalProfile.value = { ...mapped }
      } else {
        ElMessage.error(res?.msg || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置为最近一次从服务器拉取的资料
   */
  const resetProfile = () => {
    userProfile.value = { ...originalProfile.value }
  }

  /**
   * 保存当前用户资料
   */
  const saveProfile = async () => {
    loading.value = true
    try {
      const payload = mapProfileToServerPayload(userProfile.value)
      const res: any = await updateUserInfo(payload)
      if (res && res.code === 200) {
        ElMessage.success('保存成功')
        const updated = res.data
        if (updated && typeof updated === 'object') {
          const mapped = mapServerUserToProfile(updated)
          userProfile.value = mapped
          originalProfile.value = { ...mapped }
        } else {
          originalProfile.value = { ...userProfile.value }
        }
        try {
          const stored = localStorage.getItem('userInfo')
          const merged = stored && stored !== 'null' ? { ...JSON.parse(stored), ...payload } : payload
          localStorage.setItem('userInfo', JSON.stringify(merged))
        } catch (e) {
          console.warn('更新本地用户缓存失败:', e)
        }
      } else {
        ElMessage.error(res?.msg || '保存失败，请稍后重试')
      }
    } catch (error) {
      console.error('更新用户信息失败:', error)
      ElMessage.error('保存失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    userProfile,
    originalProfile,
    loadProfile,
    resetProfile,
    saveProfile
  }
}

