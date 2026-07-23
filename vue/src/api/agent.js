import request from '@/utils/request'

/**
 * 面向前端的 Agent 网关封装。
 * 用户身份一律由 JWT 拦截器注入，请求体不携带任何 userId / role / venueId。
 * 说明：request 拦截器在 code === 200 时返回整个 { code, message, data }，
 * 调用方通过 .data 获取业务数据。
 */

/**
 * 创建指定类型的会话。
 * @param {'BOOKING'|'ANALYTICS'|'SUPPORT'} agentType Agent 类型枚举
 */
export function createAgentConversation(agentType) {
  return request({
    url: '/api/agent/conversations',
    method: 'post',
    data: { agentType }
  })
}

/**
 * 发送一条用户消息。
 * @param {string} conversationId 会话 ID
 * @param {{ content: string, messageId?: string }} payload 消息体（content ≤ 2000）
 * 由页面内联展示错误与降级，故关闭全局 Toast。
 */
export function sendAgentMessage(conversationId, payload) {
  return request({
    url: `/api/agent/conversations/${conversationId}/messages`,
    method: 'post',
    data: payload,
    skipErrorMessage: true
  })
}

/**
 * 确认一次待执行动作。
 * @param {string} conversationId 会话 ID
 * @param {string} actionId 动作 ID
 * @param {string} [remark] 可选备注
 */
export function confirmAgentAction(conversationId, actionId, remark) {
  return request({
    url: `/api/agent/conversations/${conversationId}/actions/${actionId}/confirm`,
    method: 'post',
    data: { remark },
    skipErrorMessage: true
  })
}

/**
 * 拒绝一次待执行动作。
 * @param {string} conversationId 会话 ID
 * @param {string} actionId 动作 ID
 * @param {string} [remark] 可选拒绝原因
 */
export function rejectAgentAction(conversationId, actionId, remark) {
  return request({
    url: `/api/agent/conversations/${conversationId}/actions/${actionId}/reject`,
    method: 'post',
    data: { remark },
    skipErrorMessage: true
  })
}

/**
 * 查询本人会话摘要。
 * @param {string} conversationId 会话 ID
 */
export function getAgentConversation(conversationId) {
  return request({
    url: `/api/agent/conversations/${conversationId}`,
    method: 'get'
  })
}

/**
 * 删除本人会话。
 * @param {string} conversationId 会话 ID
 */
export function deleteAgentConversation(conversationId) {
  return request({
    url: `/api/agent/conversations/${conversationId}`,
    method: 'delete'
  })
}
