package com.ai_marketing_msg_be.domain.campaign_product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캠페인에 상품 추가 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "캠페인에 상품 추가 요청")
public class AddProductToCampaignRequest {

    @NotNull(message = "상품 ID는 필수입니다")
    @Schema(description = "상품 ID", example = "1")
    private Long productId;
}
