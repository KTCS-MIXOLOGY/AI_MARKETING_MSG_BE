package com.ai_marketing_msg_be.domain.message.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetToneMannerListResponse {

    private List<ToneMannerResponse> toneManners;

    public static GetToneMannerListResponse of(List<ToneMannerResponse> toneManners) {
        return GetToneMannerListResponse.builder()
                .toneManners(toneManners)
                .build();
    }
}