package com.ai_marketing_msg_be.domain.messagelog.dto;

import com.ai_marketing_msg_be.domain.messagelog.entity.MessageLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageLogListResponse {

    private Long messageId;
    private Long campaignId;
    private Long segmentId;
    private Long customerId;
    private String messageType;
    private Integer messageVersion;
    private String tone;
    private Integer characterCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String summary;

    public static MessageLogListResponse from(MessageLog messageLog) {
        return new MessageLogListResponse(
                messageLog.getMessageId(),
                messageLog.getCampaignId(),
                messageLog.getSegmentId(),
                messageLog.getCustomerId(),
                messageLog.getMessageType().name(),
                messageLog.getMessageVersion(),
                messageLog.getTone(),
                messageLog.getCharacterCount(),
                messageLog.getCreatedBy(),
                messageLog.getCreatedAt(),
                messageLog.getSummary()
        );
    }
}
