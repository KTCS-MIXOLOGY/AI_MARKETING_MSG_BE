package com.ai_marketing_msg_be.domain.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCampaignResponse {

    private Long campaignId;
    private boolean deleted;
    private LocalDateTime deletedAt;
    private Long deletedBy;

    public static DeleteCampaignResponse of(Long campaignId, Long deletedBy) {
        return DeleteCampaignResponse.builder()
                .campaignId(campaignId)
                .deleted(true)
                .deletedAt(LocalDateTime.now())
                .deletedBy(deletedBy)
                .build();
    }
}
