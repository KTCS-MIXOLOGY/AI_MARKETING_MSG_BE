package com.ai_marketing_msg_be.domain.customer.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchType {
    ID("고객ID"),
    PHONE("전화번호"),
    NAME("이름");

    private final String description;
}