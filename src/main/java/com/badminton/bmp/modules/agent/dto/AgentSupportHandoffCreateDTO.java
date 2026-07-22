package com.badminton.bmp.modules.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Agent Tool：创建人工客服转接单 请求 DTO。
 */
public class AgentSupportHandoffCreateDTO {

    @NotBlank(message = "场馆ID不能为空")
    @JsonProperty("venue_id")
    private String venueId;

    @NotBlank(message = "咨询主题不能为空")
    private String topic;

    private String description;

    @JsonProperty("conversation_id")
    private String conversationId;

    public AgentSupportHandoffCreateDTO() {
    }

    public AgentSupportHandoffCreateDTO(String venueId, String topic, String description, String conversationId) {
        this.venueId = venueId;
        this.topic = topic;
        this.description = description;
        this.conversationId = conversationId;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
