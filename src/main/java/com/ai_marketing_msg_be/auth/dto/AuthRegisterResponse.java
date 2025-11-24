package com.ai_marketing_msg_be.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthRegisterResponse {

    private Long userId;
    private String username;
    private String email;
    private String role;
    private String status;
    private String createdAt;
    private String message;

    public static AuthRegisterResponse of(
            Long userId,
            String username,
            String email,
            String role,
            String status,
            String createdAt
    ) {
        return AuthRegisterResponse.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .role(role)
                .status(status)
                .createdAt(createdAt)
                .message("회원가입이 완료되었습니다. 관리자 승인 후 로그인 가능합니다.")
                .build();
    }
}