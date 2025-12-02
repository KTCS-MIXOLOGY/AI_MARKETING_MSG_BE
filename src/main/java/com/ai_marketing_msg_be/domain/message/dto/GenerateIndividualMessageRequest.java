package com.ai_marketing_msg_be.domain.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "개별 고객 메시지 생성 요청")
public class GenerateIndividualMessageRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "고객 ID", example = "1")
    private Long customerId;

    @NotNull(message = "Campaign ID is required")
    @Schema(description = "캠페인 ID", example = "1")
    private Long campaignId;

    @NotNull(message = "Product ID is required")
    @Schema(description = "상품 ID", example = "100")
    private Long productId;

    @Schema(description = "톤앤매너 ID", example = "TONE001")
    private String toneId;

    @Schema(description = "추가 컨텍스트", example = "이전 구매 이력 기반 추천")
    private String additionalContext;
}