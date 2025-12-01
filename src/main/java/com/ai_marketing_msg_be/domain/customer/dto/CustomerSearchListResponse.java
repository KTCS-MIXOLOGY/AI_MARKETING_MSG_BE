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
public class CustomerSearchListResponse {

    private List<CustomerSearchResponse> customers;
    private Integer totalCount;

    public static CustomerSearchListResponse of(List<CustomerSearchResponse> customers) {
        return CustomerSearchListResponse.builder()
                .customers(customers)
                .totalCount(customers.size())
                .build();
    }
}