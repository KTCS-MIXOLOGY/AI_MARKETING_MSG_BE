package com.ai_marketing_msg_be.domain.message.dto;

import com.ai_marketing_msg_be.domain.message.entity.Message;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageListItemResponse {

    private Long messageId;
    private String messageType;
    private String contentPreview;
    private Integer messageVersion;
    private Integer characterCount;
    private String campaignName;
    private String productName;
    private String tone;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String aiModelUsed;

    public static MessageListItemResponse from(Message message) {
        return MessageListItemResponse.builder()
                .messageId(message.getMessageId())
                .messageType(message.getMessageType().name())
                .contentPreview(message.getContentPreview())
                .messageVersion(message.getMessageVersion())
                .characterCount(message.getCharacterCount())
                .campaignName(message.getCampaignName())
                .productName(message.getProductName())
                .tone(message.getToneName())
                .createdAt(message.getCreatedAt())
                .aiModelUsed(message.getAiModelUsed())
                .build();
    }
}