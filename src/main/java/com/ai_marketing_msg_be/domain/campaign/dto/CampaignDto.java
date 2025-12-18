package com.ai_marketing_msg_be.domain.campaign.dto;

import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignStatus;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {

    private Long campaignId;
    private String name;
    private CampaignType type;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CampaignDto from(Campaign campaign) {
        return CampaignDto.builder()
                .campaignId(campaign.getCampaignId())
                .name(campaign.getName())
                .type(campaign.getType())
                .description(campaign.getDescription())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .status(campaign.getStatus())
                .createdBy(campaign.getUserId())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}
