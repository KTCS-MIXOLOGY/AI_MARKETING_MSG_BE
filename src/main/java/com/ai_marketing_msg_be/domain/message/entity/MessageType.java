package com.ai_marketing_msg_be.domain.message.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    SEGMENT("세그먼트", "특정 조건의 고객 그룹 대상"),
    INDIVIDUAL("개별", "특정 고객 1명 대상");

    private final String description;
    private final String detail;
}