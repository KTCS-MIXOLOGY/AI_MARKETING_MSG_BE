package com.ai_marketing_msg_be.domain.campaign.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CampaignStatus {

    DRAFT("DRAFT", "초안"),
    ACTIVE("ACTIVE", "활성"),
    COMPLETED("COMPLETED", "완료"),
    CANCELLED("CANCELLED", "취소");

    private final String code;
    private final String displayName;

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static CampaignStatus from(String value) {
        for (CampaignStatus status : CampaignStatus.values()) {
            if (status.code.equals(value) || status.name().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid campaign status: " + value);
    }
}
