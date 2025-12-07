package com.ai_marketing_msg_be.auth.dto;

import static com.ai_marketing_msg_be.domain.user.entity.UserRole.ADMIN;

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
    private String department;
    private String status;
    private String createdAt;
    private String message;

    public static AuthRegisterResponse of(
            Long userId,
            String username,
            String email,
            String role,
            String department,
            String status,
            String createdAt
    ) {
        String message = "회원가입이 완료되었습니다.";
        String executorMessage = "관리자 승인 후 로그인 가능합니다.";

        return AuthRegisterResponse.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .role(role)
                .department(department)
                .status(status)
                .createdAt(createdAt)
                .message(message + (role.equals(ADMIN.toString()) ? "" : executorMessage))
                .build();
    }
}