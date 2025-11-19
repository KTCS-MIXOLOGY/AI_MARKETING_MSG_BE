package com.ai_marketing_msg_be.auth.controller;

import com.ai_marketing_msg_be.auth.dto.AuthLoginRequest;
import com.ai_marketing_msg_be.auth.dto.AuthLoginResponse;
import com.ai_marketing_msg_be.auth.dto.AuthRegisterRequest;
import com.ai_marketing_msg_be.auth.dto.AuthRegisterResponse;
import com.ai_marketing_msg_be.auth.service.AuthService;
import com.ai_marketing_msg_be.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request,
                                                HttpServletRequest httpRequest) {
        log.info("Login attempt for username: {}", request.getUsername());
        AuthLoginResponse response = authService.login(request);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @PostMapping("/register")
    public ApiResponse<AuthRegisterResponse> register(@Valid @RequestBody AuthRegisterRequest request,
                                                      HttpServletRequest httpRequest) {
        log.info("Register attempt for username: {}", request.getUsername());
        AuthRegisterResponse response = authService.register(request);
        return ApiResponse.created(response, httpRequest.getRequestURI());
    }
}