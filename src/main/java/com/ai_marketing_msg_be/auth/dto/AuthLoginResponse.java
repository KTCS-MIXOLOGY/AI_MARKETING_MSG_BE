package com.ai_marketing_msg_be.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthLoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String name;
        private String role;
    }

    public static AuthLoginResponse of(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            Long userId,
            String username,
            String name,
            String role
    ) {
        return AuthLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(UserInfo.builder()
                        .userId(userId)
                        .username(username)
                        .name(name)
                        .role(role)
                        .build())
                .build();
    }
}