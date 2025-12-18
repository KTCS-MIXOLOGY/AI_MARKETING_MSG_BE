package com.ai_marketing_msg_be.domain.customer.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipLevel {
    BASIC("일반"),
    WHITE("화이트"),
    SILVER("실버"),
    GOLD("골드"),
    VIP("VIP"),
    VVIP("VVIP");

    private final String description;
}