package com.ai_marketing_msg_be.domain.message.dto;

import com.ai_marketing_msg_be.domain.message.entity.Message;
import com.ai_marketing_msg_be.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMessageLogDetailResponse {

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private ExecutorInfo executor;
    private TargetInfo target;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutorInfo {
        private Long userId;
        private String name;
        private String department;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetInfo {
        private String type;
        private String segmentName;
    }

    public static GetMessageLogDetailResponse from(Message message, User executor) {
        ExecutorInfo executorInfo = ExecutorInfo.builder()
                .userId(executor.getId())
                .name(executor.getName())
                .department(executor.getDepartment())
                .build();

        TargetInfo targetInfo = TargetInfo.builder()
                .type(message.getMessageType().name())
                .segmentName(message.getSegmentDescription())
                .build();

        return GetMessageLogDetailResponse.builder()
                .messageId(message.getMessageId())
                .campaignId(message.getCampaign() != null ? message.getCampaign().getCampaignId() : null)
                .segmentId(message.getSegment() != null ? message.getSegment().getSegmentId() : null)
                .customerId(message.getCustomer() != null ? message.getCustomer().getCustomerId() : null)
                .messageContent(message.getMessageContent())
                .messageVersion(message.getMessageVersion())
                .messageType(message.getMessageType().name())
                .characterCount(message.getCharacterCount())
                .aiModelUsed(message.getAiModelUsed())
                .generationPrompt(message.getGenerationPrompt())
                .tone(message.getToneName())
                .createdBy(executor.getId())
                .createdAt(message.getCreatedAt())
                .executor(executorInfo)
                .target(targetInfo)
                .build();
    }
}