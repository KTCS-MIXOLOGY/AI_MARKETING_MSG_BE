package com.ai_marketing_msg_be.domain.message.dto;

import com.ai_marketing_msg_be.domain.message.entity.ToneManner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToneMannerResponse {

    private String toneId;
    private String toneName;
    private String description;
    private String example;

    public static ToneMannerResponse from(ToneManner toneManner) {
        return ToneMannerResponse.builder()
                .toneId(toneManner.getToneId())
                .toneName(toneManner.getToneName())
                .description(toneManner.getDescription())
                .example(toneManner.getExample())
                .build();
    }
}