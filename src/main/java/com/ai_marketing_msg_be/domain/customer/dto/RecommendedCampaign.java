package com.ai_marketing_msg_be.domain.customer.dto;

import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추천 캠페인 상세 정보")
public class RecommendedCampaign {

    @Schema(description = "추천 순위 (1~3)", example = "1")
    private Integer rank;

    @Schema(description = "캠페인 ID", example = "10")
    private Long campaignId;

    @Schema(description = "캠페인명", example = "5G 프리미엄 업그레이드 특별 할인")
    private String campaignName;

    @Schema(description = "캠페인 타입", example = "UPSELLING")
    private String campaignType;

    @Schema(description = "캠페인 설명", example = "3개월간 차액 50% 할인 + 데이터 무제한 쿠폰")
    private String description;

    @Schema(description = "추천 이유", example = "현재 데이터 사용량이 요금제를 초과하고 있어...")
    private String reason;

    @Schema(description = "예상 혜택", example = "3개월간 월 7,500원 절감")
    private String expectedBenefit;

    @Schema(description = "관련도 점수 (0~100)", example = "95")
    private Integer relevanceScore;

    public static RecommendedCampaign fromCampaign(
            Campaign campaign,
            Integer rank,
            String reason,
            String expectedBenefit,
            Integer relevanceScore) {

        return RecommendedCampaign.builder()
                .rank(rank)
                .campaignId(campaign.getCampaignId())
                .campaignName(campaign.getName())
                .campaignType(campaign.getType().name())
                .description(campaign.getDescription())
                .reason(reason)
                .expectedBenefit(expectedBenefit)
                .relevanceScore(relevanceScore)
                .build();
    }
}