package com.badminton.bmp.modules.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 发送用户消息请求。
 */
public class AgentMessageRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容长度不能超过 2000")
    private String content;

    /** 客户端消息 ID，用于幂等与去重，可选。 */
    @Size(max = 64, message = "messageId 长度不能超过 64")
    private String messageId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
