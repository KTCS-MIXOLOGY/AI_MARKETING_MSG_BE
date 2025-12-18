package com.ai_marketing_msg_be.domain.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCountResponse {

    private Long totalCount;
    private SegmentFilterRequest filters;

    public static CustomerCountResponse of(Long totalCount, SegmentFilterRequest filters) {
        return CustomerCountResponse.builder()
                .totalCount(totalCount)
                .filters(filters)
                .build();
    }
}