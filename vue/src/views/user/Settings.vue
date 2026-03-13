<template>
  <div class="settings-container">
    <header class="settings-page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-subtitle">集中管理账户信息、安全与通知偏好</p>
    </header>
    <!-- 账户信息卡片 -->
    <el-card class="account-card" shadow="hover">
      <div class="account-header">
        <el-avatar
          :size="80"
          :src="userInfo.avatar || undefined"
          :icon="UserFilled"
          class="user-avatar"
        />
        <div class="account-info">
          <h3 class="account-name">{{ userInfo.username || '未登录' }}</h3>
          <p class="account-level">{{ getRoleName(userInfo.role) || '普通用户' }}</p>
        </div>
      </div>
    </el-card>

    <!-- 设置内容 -->
    <el-card class="settings-card" shadow="hover">
      <template #header>
        <div class="settings-header">
          <span>账户设置</span>
          <span class="settings-subtitle">管理个人信息、安全和通知偏好</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 个人信息 Tab：嵌入精简版 Profile 表单 -->
        <el-tab-pane label="个人信息" name="profile">
          <div class="tab-inner">
            <small-profile />
          </div>
        </el-tab-pane>

        <!-- 安全设置 Tab -->
        <el-tab-pane label="安全设置" name="security">
          <div class="tab-inner">
            <el-form label-width="120px" class="settings-form">
              <el-form-item label="登录提醒">
                <el-switch v-model="securityOptions.loginAlert" @change="saveSettingsDebounced" />
                <span class="item-desc">登录成功后通过站内消息进行提示。</span>
              </el-form-item>
              <el-form-item label="异常设备登录">
                <el-switch v-model="securityOptions.strangeDevice" @change="saveSettingsDebounced" />
                <span class="item-desc">检测到新的设备登录时进行额外提醒。</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 通知设置 Tab -->
        <el-tab-pane label="通知设置" name="notification">
          <div class="tab-inner">
            <el-form label-width="120px" class="settings-form">
              <el-form-item label="站内消息">
                <el-switch v-model="notificationOptions.siteMessage" @change="saveSettingsDebounced" />
                <span class="item-desc">在系统内展示重要通知（小铃铛及登录弹窗）。</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 其他设置 -->
    <el-card class="settings-card" shadow="hover">
      <template #header>
        <span>其他设置</span>
      </template>
      <el-menu>
        <el-menu-item @click="goAbout">
          <el-icon><InfoFilled /></el-icon>
          <span>关于我们</span>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </el-menu-item>
        <el-menu-item @click="goHelp">
          <el-icon><QuestionFilled /></el-icon>
          <span>帮助与反馈</span>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </el-menu-item>
        <el-menu-item @click="handleDeleteAccount" style="color: #ef4444;">
          <el-icon><Delete /></el-icon>
          <span>注销账号</span>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </el-menu-item>
      </el-menu>
    </el-card>

    <!-- 退出登录 -->
    <div class="logout-container">
      <el-button
        type="danger"
        size="large"
        :icon="SwitchButton"
        @click="handleLogout"
        style="width: 100%;"
      >
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled,
  InfoFilled,
  QuestionFilled,
  SwitchButton,
  ArrowRight,
  Delete
} from '@element-plus/icons-vue'
import { getInfo, logout, getSettings, updateSettings } from '@/api/login'
import { deleteAccount } from '@/api/user'
import { useSiteMessageSetting } from '@/composables/useSiteMessageSetting'
import { getRoleName } from '@/utils/roleHelper'
import SmallProfile from '@/components/SmallProfile.vue'

const router = useRouter()
const route = useRoute()

const userInfo = ref({})
const activeTab = ref('profile')

const securityOptions = ref({
  loginAlert: true,
  strangeDevice: true
})

const notificationOptions = ref({
  siteMessage: true
})

const settingsLoading = ref(false)
const { setValue: setSiteMessageEnabled } = useSiteMessageSetting()
const saveSettingsTimer = ref<number | null>(null)

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getInfo()
    if (res.code === 200) {
      userInfo.value = res.data || res.user || {}
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载设置（安全/通知）
const loadSettings = async () => {
  settingsLoading.value = true
  try {
    const res = await getSettings()
    if (res && res.code === 200 && res.data) {
      const d = res.data
      if (d.loginAlert !== undefined) securityOptions.value.loginAlert = d.loginAlert
      if (d.strangeDevice !== undefined) securityOptions.value.strangeDevice = d.strangeDevice
      if (d.siteMessage !== undefined) {
        notificationOptions.value.siteMessage = d.siteMessage
        setSiteMessageEnabled(d.siteMessage)
      }
    }
  } catch (e) {
    console.error('加载设置失败:', e)
  } finally {
    settingsLoading.value = false
  }
}

const saveSettingsDebounced = () => {
  if (saveSettingsTimer.value) {
    clearTimeout(saveSettingsTimer.value)
  }
  saveSettingsTimer.value = setTimeout(async () => {
    try {
      await updateSettings({
        loginAlert: securityOptions.value.loginAlert,
        strangeDevice: securityOptions.value.strangeDevice,
        siteMessage: notificationOptions.value.siteMessage
      })
      setSiteMessageEnabled(notificationOptions.value.siteMessage)
      ElMessage.success('设置已保存')
    } catch (error) {
      console.error('保存设置失败:', error)
      const errorMessage = error.response?.data?.msg || error.message || '保存设置失败，请重试'
      ElMessage.error(errorMessage)
    }
  }, 300)
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (saveSettingsTimer.value) {
    clearTimeout(saveSettingsTimer.value)
  }
})

const isUserLayout = () => route.path.startsWith('/user')
const goAbout = () => {
  router.push(isUserLayout() ? '/user/about' : '/about').catch(() => {})
}
const goHelp = () => {
  router.push(isUserLayout() ? '/user/help' : '/help').catch(() => {})
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    try {
      // 调用退出登录接口
      await logout()
    } catch (error) {
      console.error('退出登录接口调用失败:', error)
      // 即使接口失败也继续清除本地数据
    } finally {
      // 统一在 finally 中清除本地数据
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/login')
    }
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

// 账号注销
const handleDeleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '注销后账户及关联数据将被删除且无法恢复，确定要继续吗？',
      '注销账号',
      {
        confirmButtonText: '继续注销',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const { value: password } = await ElMessageBox.prompt('请输入当前密码以确认注销', '确认密码', {
      confirmButtonText: '确认注销',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '当前登录密码'
    })
    if (!password?.trim()) {
      ElMessage.warning('请输入密码')
      return
    }
    await deleteAccount({ password: password.trim() })
    ElMessage.success('账户已注销')
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    router.push('/login')
  } catch (error) {
    if (error === 'cancel') return
    const msg = error.response?.data?.msg || error.message || '注销失败'
    ElMessage.error(msg)
  }
}

onMounted(() => {
  loadUserInfo()
  loadSettings()
  const tab = route.query.tab
  if (tab === 'security' || tab === 'notification' || tab === 'profile') {
    activeTab.value = tab
  }
})

watch(() => route.query.tab, (tab) => {
  if (tab === 'security' || tab === 'notification' || tab === 'profile') {
    activeTab.value = tab
  }
})
</script>

<style scoped lang="scss">
.settings-container {
  padding: 20px 0 32px;
  max-width: 1040px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--color-background, #f8fafc);
  border-radius: 24px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.06));
}

.settings-page-header {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #111827);
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #6b7280);
}

.account-card {
  border-radius: 16px;
  background: var(--color-card-bg, #ffffff);
  border: 1px solid var(--color-border, #e5edff);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.06));

  .account-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
  }

  .user-avatar {
    background: conic-gradient(
      from 180deg,
      var(--color-success, #22c55e),
      var(--color-primary, #2563eb),
      var(--color-accent, #f97316)
    );
    box-shadow:
      0 10px 30px rgba(15, 23, 42, 0.25),
      0 0 0 4px rgba(255, 255, 255, 0.7);
  }

  .account-info {
    flex: 1;
  }

  .account-name {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text-primary, #111827);
    margin: 0 0 4px 0;
  }

  .account-level {
    font-size: 13px;
    color: var(--color-text-secondary, #6b7280);
    margin: 0;
  }
}

.settings-card {
  border-radius: 16px;
  background: var(--color-card-bg, #ffffff);
  border: 1px solid var(--color-border, #e5edff);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.06));

  .settings-header {
    display: flex;
    flex-direction: column;
    gap: 4px;

    span:first-child {
      font-size: 15px;
      font-weight: 600;
      color: var(--color-text-primary, #111827);
    }

    .settings-subtitle {
      font-size: 12px;
      color: var(--color-text-secondary, #9ca3af);
    }
  }

  :deep(.el-tabs__nav-wrap) {
    padding-left: 4px;
  }

  :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 13px;

    &.is-active {
      color: var(--color-primary, #f97316);
    }
  }

  .tab-inner {
    padding: 12px 4px 10px;
  }

  :deep(.el-menu) {
    border: none;
  }

  :deep(.el-menu-item) {
    display: flex;
    align-items: center;
    height: 56px;
    padding: 0 20px;
    cursor: pointer;
    transition: background-color 0.25s ease, transform 0.2s ease;

    &:hover {
      background-color: var(--color-card-bg-hover, #fff7ed);
      transform: translateY(-1px);
    }

    .el-icon {
      margin-right: 12px;
      font-size: 18px;
    }

    .menu-arrow {
      margin-left: auto;
      margin-right: 0;
      color: #c0c4cc;
    }
  }
}

.logout-container {
  margin-top: 16px;
  padding: 0 4px;

  :deep(.el-button) {
    border-radius: 999px;
    border: none;
    background: linear-gradient(
      135deg,
      var(--color-primary, #f97316),
      var(--color-primary-light, #fb923c)
    );
    box-shadow: 0 14px 30px rgba(0, 0, 0, 0.25);

    &:hover {
      background: linear-gradient(
        135deg,
        var(--color-primary-dark, #ea580c),
        var(--color-primary, #f97316)
      );
      box-shadow: 0 18px 36px rgba(0, 0, 0, 0.32);
    }
  }
}

.settings-form {
  .item-desc {
    margin-left: 12px;
    font-size: 12px;
    color: #9ca3af;
  }
}

@media (max-width: 768px) {
  .settings-container {
    padding: 12px 0 20px;
    border-radius: 16px;
  }

  .account-card .account-header {
    padding: 14px 16px;
  }
}
</style>
