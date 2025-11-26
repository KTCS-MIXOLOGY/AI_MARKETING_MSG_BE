package com.ai_marketing_msg_be.domain.messagelog.dto;

import com.ai_marketing_msg_be.domain.messagelog.entity.MessageLog;
import com.ai_marketing_msg_be.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageLogDetailResponse {

    private Long messageId;
    private Long campaignId;
    private Long segmentId;
    private Long customerId;
    private String messageContent;
    private Integer messageVersion;
    private String messageType;
    private Integer characterCount;
    private String aiModelUsed;
    private String generationPrompt;
    private String tone;
    private Long createdBy;
    private LocalDateTime createdAt;
    private ExecutorInfo executor;
    private TargetInfo target;

    public static MessageLogDetailResponse from(MessageLog messageLog, User user) {
        ExecutorInfo executorInfo = new ExecutorInfo(
                user.getId(),
                user.getName(),
                user.getDepartment()
        );

        TargetInfo targetInfo = new TargetInfo(
                messageLog.getTargetType().name(),
                messageLog.getSegmentName()
        );

        return new MessageLogDetailResponse(
                messageLog.getMessageId(),
                messageLog.getCampaignId(),
                messageLog.getSegmentId(),
                messageLog.getCustomerId(),
                messageLog.getMessageContent(),
                messageLog.getMessageVersion(),
                messageLog.getMessageType().name(),
                messageLog.getCharacterCount(),
                messageLog.getAiModelUsed(),
                messageLog.getGenerationPrompt(),
                messageLog.getTone(),
                messageLog.getCreatedBy(),
                messageLog.getCreatedAt(),
                executorInfo,
                targetInfo
        );
    }
}
