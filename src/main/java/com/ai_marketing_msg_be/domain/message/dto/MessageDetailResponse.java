package com.ai_marketing_msg_be.domain.message.dto;

import com.ai_marketing_msg_be.domain.message.entity.Message;
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
public class MessageDetailResponse {

    private Long messageId;
    private String messageGroupId;
    private String messageType;
    private String messageContent;
    private Integer messageVersion;
    private Integer characterCount;

    private String campaignName;
    private String productName;
    private String tone;
    private String toneId;

    private SegmentInfo segmentInfo;

    private String customerName;
    private Long customerId;

    private String aiModelUsed;
    private String generationPrompt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static MessageDetailResponse from(Message message) {
        MessageDetailResponseBuilder builder = MessageDetailResponse.builder()
                .messageId(message.getMessageId())
                .messageGroupId(message.getMessageGroupId())
                .messageType(message.getMessageType().name())
                .messageContent(message.getMessageContent())
                .messageVersion(message.getMessageVersion())
                .characterCount(message.getCharacterCount())
                .campaignName(message.getCampaignName())
                .productName(message.getProductName())
                .tone(message.getToneName())
                .toneId(message.getToneId())
                .aiModelUsed(message.getAiModelUsed())
                .generationPrompt(message.getGenerationPrompt())
                .createdAt(message.getCreatedAt());

        if (message.isSegmentMessage() && message.getSegment() != null) {
            builder.segmentInfo(SegmentInfo.from(message.getSegment()));
        }

        if (message.isIndividualMessage() && message.getCustomer() != null) {
            builder.customerName(message.getCustomerName())
                    .customerId(message.getCustomer().getCustomerId());
        }

        return builder.build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentInfo {
        private Long segmentId;
        private String description;
        private Integer targetCustomerCount;

        public static SegmentInfo from(com.ai_marketing_msg_be.domain.message.entity.Segment segment) {
            return SegmentInfo.builder()
                    .segmentId(segment.getSegmentId())
                    .description(segment.getDescription())
                    .targetCustomerCount(segment.getTargetCustomerCount())
                    .build();
        }
    }
}
