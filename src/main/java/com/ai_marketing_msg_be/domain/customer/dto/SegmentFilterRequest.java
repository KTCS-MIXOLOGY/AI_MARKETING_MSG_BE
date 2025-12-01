package com.ai_marketing_msg_be.domain.customer.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentFilterRequest {

    private AgeRange ageRange;
    private String gender;
    private List<String> regions;
    private String membershipLevel;
    private Integer recencyMaxDays;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeRange {
        private Integer min;
        private Integer max;
    }
}