package com.badminton.bmp.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 统一推送服务
 * - 按用户推送：/user/{userId}/queue/notifications
 * - 管理端广播：/topic/admin/todo、/topic/admin/dashboard
 */
@Service
public class WebSocketPushService {

    public static final String QUEUE_NOTIFICATIONS = "/queue/notifications";
    public static final String TOPIC_ADMIN_TODO = "/topic/admin/todo";
    public static final String TOPIC_ADMIN_DASHBOARD = "/topic/admin/dashboard";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 向指定用户推送消息（点对点）
     */
    public void sendToUser(Long userId, String destination, Object payload) {
        if (userId == null) return;
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
    }

    /**
     * 向管理端广播（/topic/admin/...）
     */
    public void sendToAdmins(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }

    /**
     * 推送订单/预约状态变更给对应用户
     */
    public void pushOrderStatusToUser(Long userId, String orderType, Long orderId, Integer status, String statusText, String title) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ORDER_STATUS");
        payload.put("orderType", orderType);
        payload.put("orderId", orderId);
        payload.put("status", status);
        payload.put("statusText", statusText);
        if (title != null) payload.put("title", title);
        sendToUser(userId, QUEUE_NOTIFICATIONS, payload);
    }

    /**
     * 推送待办更新给管理端（数量 + 列表）
     */
    public void pushTodoUpdate(Map<String, Object> count, Object list) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "TODO_UPDATE");
        payload.put("count", count != null ? count : new HashMap<>());
        if (list != null) payload.put("list", list);
        sendToAdmins(TOPIC_ADMIN_TODO, payload);
    }

    /**
     * 推送看板刷新给管理端/大屏
     */
    public void pushDashboardRefresh() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DASHBOARD_REFRESH");
        sendToAdmins(TOPIC_ADMIN_DASHBOARD, payload);
    }

    /**
     * 订单状态变更后：推给用户 + 推待办与看板给管理端
     */
    public void onOrderStatusChanged(Long userId, String orderType, Long orderId, Integer status, String statusText, String title,
                                    Map<String, Object> todoCount, Object todoList) {
        if (userId != null) {
            pushOrderStatusToUser(userId, orderType, orderId, status, statusText, title);
        }
        if (todoCount != null || todoList != null) {
            pushTodoUpdate(todoCount, todoList);
        }
        pushDashboardRefresh();
    }
}
