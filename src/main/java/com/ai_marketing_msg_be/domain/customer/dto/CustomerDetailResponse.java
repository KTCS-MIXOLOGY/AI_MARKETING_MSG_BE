package com.ai_marketing_msg_be.domain.customer.dto;

import com.ai_marketing_msg_be.domain.customer.entity.Customer;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDetailResponse {

    private Long customerId;
    private String name;
    private Integer age;
    private String gender;
    private String phoneNumber;
    private String email;
    private String region;
    private String membershipLevel;

    private String currentPlan;
    private String currentDevice;
    private String contractEndDate;
    private BigDecimal avgDataUsageGb;

    private String joinDate;
    private String lastPurchaseDate;
    private Integer recencyDays;

    private List<RecentPurchaseDto> recentPurchases;
    private List<String> preferredCategories;

    public static CustomerDetailResponse from(Customer customer) {
        return CustomerDetailResponse.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .age(customer.getAge())
                .gender(customer.getGender() != null ? customer.getGender().name() : null)
                .phoneNumber(customer.getPhone())
                .region(customer.getRegion() != null ? customer.getRegion().name() : null)
                .membershipLevel(customer.getMembershipLevel() != null
                        ? customer.getMembershipLevel().name()
                        : null)
                .currentPlan(customer.getCurrentPlan())
                .currentDevice(customer.getCurrentDevice())
                .contractEndDate(customer.getContractEndDate() != null
                        ? customer.getContractEndDate().toString()
                        : null)
                .avgDataUsageGb(customer.getAvgDataUsageGb())
                .joinDate(customer.getJoinDate() != null
                        ? customer.getJoinDate().toString()
                        : null)
                .lastPurchaseDate(customer.getLastPurchaseDate() != null
                        ? customer.getLastPurchaseDate().toString()
                        : null)
                .recencyDays(customer.getRecencyDays())
                .recentPurchases(Collections.emptyList())
                .preferredCategories(Collections.emptyList())  // 추후 구현
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentPurchaseDto {
        private String productId;
        private String productName;
        private String purchaseDate;
        private Long amount;
    }
}