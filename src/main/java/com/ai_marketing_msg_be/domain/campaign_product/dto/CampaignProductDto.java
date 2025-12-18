package com.ai_marketing_msg_be.domain.campaign_product.dto;

import com.ai_marketing_msg_be.domain.campaign_product.entity.CampaignProduct;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캠페인-상품 매핑 조회 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignProductDto {
    private Long campaignId;
    private String campaignName;
    private Long productId;
    private String productName;
    private String productCategory;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static CampaignProductDto from(CampaignProduct campaignProduct) {
        return CampaignProductDto.builder()
                .campaignId(campaignProduct.getCampaign().getCampaignId())
                .campaignName(campaignProduct.getCampaign().getName())
                .productId(campaignProduct.getProduct().getProductId())
                .productName(campaignProduct.getProduct().getName())
                .productCategory(campaignProduct.getProduct().getCategory())
                .createdAt(campaignProduct.getCreatedAt())
                .build();
    }
}
