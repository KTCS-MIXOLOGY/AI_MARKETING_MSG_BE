package com.ai_marketing_msg_be.domain.campaign.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CampaignType {

    NEW_CUSTOMER("신규유치", "new_customer"),
    RETENTION("고객유지", "retention"),
    UPSELLING("업셀링", "upselling"),
    CROSS_SELLING("크로스셀링", "cross_selling"),
    CHURN_PREVENTION("이탈방지", "churn_prevention");

    private final String displayName;
    private final String value;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static CampaignType from(String value) {
        for (CampaignType type : CampaignType.values()) {
            if (type.displayName.equals(value) || type.value.equals(value) || type.name().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid campaign type: " + value);
    }
}
