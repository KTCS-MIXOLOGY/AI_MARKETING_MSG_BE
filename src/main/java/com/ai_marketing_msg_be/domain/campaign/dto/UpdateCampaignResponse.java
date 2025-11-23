package com.ai_marketing_msg_be.domain.campaign.dto;

import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampaignResponse {

    private Long campaignId;
    private String name;
    private CampaignStatus status;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UpdateCampaignResponse from(Campaign campaign, Long updatedBy) {
        return UpdateCampaignResponse.builder()
                .campaignId(campaign.getCampaignId())
                .name(campaign.getName())
                .status(campaign.getStatus())
                .createdBy(campaign.getUserId())
                .updatedBy(updatedBy)
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}
