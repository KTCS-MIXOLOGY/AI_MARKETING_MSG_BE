package com.ai_marketing_msg_be.domain.campaign_product.dto;

import com.ai_marketing_msg_be.domain.campaign_product.entity.CampaignProduct;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캠페인에 상품 추가 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToCampaignResponse {
    private Long campaignId;
    private Long productId;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    public static AddProductToCampaignResponse from(CampaignProduct campaignProduct) {
        return AddProductToCampaignResponse.builder()
                .campaignId(campaignProduct.getId().getCampaignId())
                .productId(campaignProduct.getId().getProductId())
                .message("상품이 캠페인에 추가되었습니다")
                .createdAt(campaignProduct.getCreatedAt())
                .build();
    }
}
