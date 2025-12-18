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
public class MessageLogItem {

    private Long messageId;
    private Long campaignId;
    private Long segmentId;
    private Long customerId;
    private String messageType;
    private Integer messageVersion;
    private String tone;
    private Integer characterCount;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String summary;

    public static MessageLogItem from(Message message) {
        return MessageLogItem.builder()
                .messageId(message.getMessageId())
                .campaignId(message.getCampaign() != null ? message.getCampaign().getCampaignId() : null)
                .segmentId(message.getSegment() != null ? message.getSegment().getSegmentId() : null)
                .customerId(message.getCustomer() != null ? message.getCustomer().getCustomerId() : null)
                .messageType(message.getMessageType().name())
                .messageVersion(message.getMessageVersion())
                .tone(message.getToneName())
                .characterCount(message.getCharacterCount())
                .createdBy(message.getUser().getId())
                .createdAt(message.getCreatedAt())
                .summary(message.getContentPreview())
                .build();
    }
}