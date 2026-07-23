import { request } from '../utils/request'

/**
 * 面向 UniApp 的 Agent 网关封装。
 *
 * 说明：
 * - 用户身份一律由请求拦截器基于 JWT 注入，请求体不携带任何 userId / role / venueId。
 * - config.BASE_URL 已包含 `/api` 前缀，故此处路径以 `/agent` 开头。
 * - request 在业务 code === 200 时直接返回 result.data（已解包），因此各函数的
 *   泛型即为业务数据类型。
 */

/** Agent 类型，对齐后端枚举。UniApp 侧仅接入预订与客服。 */
export type AgentType = 'BOOKING' | 'ANALYTICS' | 'SUPPORT'

/** 预订报价，用于确认卡片展示（金额、时长等一律由 Java 计算）。 */
export interface AgentQuote {
  venueId?: number
  date?: string
  startTime?: string
  endTime?: string
  courtIds?: number[]
  duration?: number | string
  totalAmount?: number | string
  [key: string]: unknown
}

/** 待执行 / 已执行动作。字段随 type 变化，保留原始结构供组件按需消费。 */
export interface AgentAction {
  // 预订确认：type = 'confirm_booking'
  type?: string
  prompt?: string
  quote?: AgentQuote
  idempotency_key?: string
  // 预订创建成功：type = 'booking_created'
  booking?: Record<string, unknown>
  // 客服转人工：name = 'human_handoff'
  name?: string
  status?: string
  handoff_id?: string
  [key: string]: unknown
}

/** 引用来源（客服知识库 / 实时 Java Tool）。 */
export interface AgentReference {
  title?: string
  doc_id?: string
  version?: string
  updated_at?: string
  [key: string]: unknown
}

/** 网关统一响应，映射自 AgentResponseVO。 */
export interface AgentResponse {
  conversationId: string
  response: string
  type: string
  requiresAction: boolean
  actions: AgentAction[]
  references: AgentReference[]
  traceId?: string
}

/** 会话摘要。 */
export interface AgentConversation {
  conversationId: string
  agentType: AgentType
}

/** 页面持有的对话消息状态（前端 UI 结构，非后端返回体）。 */
export interface ChatMessage {
  id: string
  role: 'user' | 'agent'
  content: string
  requiresAction?: boolean
  actions?: AgentAction[]
  references?: AgentReference[]
  traceId?: string
  error?: boolean
  // 该消息的确认动作是否已被消费（确认/取消后置 true，防重复下单）
  actionDone?: boolean
}

/** 发送消息入参（content ≤ 2000）。 */
export interface AgentMessagePayload {
  content: string
  messageId?: string
}

/**
 * 创建指定类型的会话。
 */
export function createAgentConversation(agentType: AgentType) {
  return request<AgentConversation>({
    url: '/agent/conversations',
    method: 'POST',
    data: { agentType }
  })
}

/**
 * 发送一条用户消息。
 *
 * 关闭全局错误 Toast，由页面内联展示错误与降级提示，避免与气泡重复弹出。
 * 预订 Agent 的「确认 / 取消」同样通过本接口再发一条「确认」「取消」消息触发
 * （后端以 LangGraph interrupt/resume 恢复），不要调用 /actions 端点。
 */
export function sendAgentMessage(conversationId: string, payload: AgentMessagePayload) {
  return request<AgentResponse>({
    url: `/agent/conversations/${conversationId}/messages`,
    method: 'POST',
    data: payload,
    suppressErrorToast: true
  })
}

/**
 * 查询本人会话摘要。
 */
export function getAgentConversation(conversationId: string) {
  return request<AgentConversation>({
    url: `/agent/conversations/${conversationId}`,
    method: 'GET'
  })
}

/**
 * 删除本人会话及其关联的 Checkpoint。
 */
export function deleteAgentConversation(conversationId: string) {
  return request<void>({
    url: `/agent/conversations/${conversationId}`,
    method: 'DELETE'
  })
}
