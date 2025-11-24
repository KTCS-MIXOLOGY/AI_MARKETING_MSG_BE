package com.ai_marketing_msg_be.auth.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AuthRefreshRequest {
    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}
