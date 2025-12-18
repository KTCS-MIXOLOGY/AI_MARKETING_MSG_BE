package com.ai_marketing_msg_be.domain.campaign.dto;

import com.ai_marketing_msg_be.domain.campaign.entity.Campaign;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignStatus;
import com.ai_marketing_msg_be.domain.campaign.entity.CampaignType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignRequest {

    @NotBlank(message = "캠페인명은 필수입니다")
    @Size(max = 100, message = "캠페인명은 100자를 초과할 수 없습니다")
    private String name;

    @NotNull(message = "캠페인 타입은 필수입니다")
    private CampaignType type;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private CampaignStatus status;

    public Campaign toEntity(Long userId) {
        return Campaign.builder()
                .userId(userId)
                .name(this.name)
                .type(this.type)
                .description(this.description)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .status(this.status != null ? this.status : CampaignStatus.DRAFT)
                .build();
    }
}
