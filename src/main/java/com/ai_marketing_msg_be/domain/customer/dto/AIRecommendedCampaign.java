package com.ai_marketing_msg_be.domain.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendedCampaign {
    private Integer rank;
    private Long campaignId;
    private String reason;
    private String expectedBenefit;
    private Integer relevanceScore;
}