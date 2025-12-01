package com.ai_marketing_msg_be.domain.message.dto;

import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveMessageRequest {

    @NotBlank(message = "Message type is required")
    private String messageType;

    private String messageGroupId;

    private SegmentFilterRequest segmentFilter;

    private Long customerId;

    @NotNull(message = "Campaign ID is required")
    private Long campaignId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String toneId;

    @NotBlank(message = "Message content is required")
    private String messageContent;

    @NotNull(message = "Message version is required")
    private Integer messageVersion;

    private String generationPrompt;

    private String aiModelUsed;
}