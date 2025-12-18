package com.ai_marketing_msg_be.domain.message.dto;

import com.ai_marketing_msg_be.domain.customer.dto.SegmentFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "세그먼트 메시지 생성 요청")
public class GenerateSegmentMessageRequest {

    @NotNull(message = "Segment filter is required")
    @Valid
    @Schema(description = "세그먼트 필터 조건")
    private SegmentFilterRequest segmentFilter;

    @NotNull(message = "Campaign ID is required")
    @Schema(description = "캠페인 ID", example = "1")
    private Long campaignId;

    @NotNull(message = "Product ID is required")
    @Schema(description = "상품 ID", example = "100")
    private Long productId;

    @Schema(description = "톤앤매너 ID", example = "TONE001")
    private String toneId;

    @Schema(description = "추가 컨텍스트", example = "20대 남성 타겟, 최신 스마트폰 구매 유도")
    private String additionalContext;
}