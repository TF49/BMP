<template>
  <div class="help-page">
    <header class="help-header">
      <h1 class="page-title">帮助与反馈</h1>
    </header>
    <el-card class="help-card" shadow="hover">
      <h2>常见问题</h2>
      <el-collapse>
        <el-collapse-item title="如何预约场地？" name="1">
          登录后进入「场地预订」，选择场馆与时间即可提交预约，支付后完成预订。
        </el-collapse-item>
        <el-collapse-item title="如何修改个人资料？" name="2">
          在「个人中心」或「系统设置」-「个人信息」中可修改手机号、性别、个性签名与头像。
        </el-collapse-item>
        <el-collapse-item title="如何修改密码？" name="3">
          在「个人中心」点击「修改密码」，或进入「系统设置」-「安全设置」进行修改。
        </el-collapse-item>
        <el-collapse-item title="充值后余额未到账怎么办？" name="4">
          请稍候刷新页面；若仍未到账，请通过下方反馈表单联系客服。
        </el-collapse-item>
      </el-collapse>
    </el-card>
    <el-card class="feedback-card" shadow="hover">
      <h2>意见反馈</h2>
      <el-form :model="feedbackForm" label-width="80px" @submit.prevent="submitFeedback">
        <el-form-item label="反馈内容" required>
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="4"
            placeholder="请描述您的问题或建议"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="feedbackForm.contact" placeholder="选填，便于我们回复您" maxlength="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="feedbackLoading" @click="submitFeedback">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { submitFeedback as submitFeedbackApi } from '@/api/login'

const feedbackLoading = ref(false)
const feedbackForm = reactive({
  content: '',
  contact: ''
})

const submitFeedback = async () => {
  if (!feedbackForm.content.trim()) {
    ElMessage.warning('请填写反馈内容')
    return
  }
  feedbackLoading.value = true
  try {
    const res = await submitFeedbackApi({
      content: feedbackForm.content.trim(),
      contact: feedbackForm.contact.trim() || undefined
    })
    if (res && res.code === 200) {
      ElMessage.success('提交成功，感谢您的反馈')
      feedbackForm.content = ''
      feedbackForm.contact = ''
    } else {
      ElMessage.error(res?.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    feedbackLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.help-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 0 32px;
}
.help-header {
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #111827);
}
.help-card, .feedback-card {
  border-radius: 16px;
  margin-bottom: 16px;
  padding: 24px;
  h2 {
    margin: 0 0 16px 0;
    font-size: 16px;
  }
}
</style>
