package com.ai_marketing_msg_be.domain.campaign_product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캠페인에서 상품 제거 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveProductFromCampaignResponse {
    private Long campaignId;
    private Long productId;
    private boolean removed;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime removedAt;

    public static RemoveProductFromCampaignResponse of(Long campaignId, Long productId) {
        return RemoveProductFromCampaignResponse.builder()
                .campaignId(campaignId)
                .productId(productId)
                .removed(true)
                .message("상품이 캠페인에서 제거되었습니다")
                .removedAt(LocalDateTime.now())
                .build();
    }
}
