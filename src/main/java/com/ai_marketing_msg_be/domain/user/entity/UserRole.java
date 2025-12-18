package com.ai_marketing_msg_be.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("관리자"),
    EXECUTOR("실행자");

    private final String description;
}