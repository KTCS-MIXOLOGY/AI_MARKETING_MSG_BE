package com.ai_marketing_msg_be.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "create")
public class AuthRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
