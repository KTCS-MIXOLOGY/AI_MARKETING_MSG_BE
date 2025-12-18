package com.ai_marketing_msg_be.domain.customer.dto;

import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchResponse {

    private Long customerId;
    private String name;
    private String phoneNumber;
    private String email;
    private String membershipLevel;

    public static CustomerSearchResponse from(Customer customer) {
        return CustomerSearchResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .phoneNumber(customer.getPhone())
                .membershipLevel(customer.getMembershipLevel() != null
                        ? customer.getMembershipLevel().name()
                        : null)
                .build();
    }
}