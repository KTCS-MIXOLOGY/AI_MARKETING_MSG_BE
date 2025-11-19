package com.ai_marketing_msg_be.auth.service;

import com.ai_marketing_msg_be.auth.details.CustomUserDetails;
import com.ai_marketing_msg_be.auth.dto.AuthLoginRequest;
import com.ai_marketing_msg_be.auth.dto.AuthLoginResponse;
import com.ai_marketing_msg_be.auth.provider.JwtTokenProvider;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional(readOnly = true)
    public AuthLoginResponse login(AuthLoginRequest request) {
        try {
            CustomUserDetails userDetails = validateLoginInfo(
                    request.getUsername(),
                    request.getPassword()
            );

            String accessToken = jwtTokenProvider.generateAccessToken(
                    userDetails.getUsername(),
                    userDetails.getUserId(),
                    userDetails.getRole()
            );

            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    userDetails.getUsername(),
                    userDetails.getUserId(),
                    userDetails.getRole()
            );

            return AuthLoginResponse.of(
                    accessToken,
                    refreshToken,
                    jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                    userDetails.getUserId(),
                    userDetails.getUsername(),
                    userDetails.getName(),
                    userDetails.getRole()
            );

        } catch (BadCredentialsException e) {
            log.error("Login failed - invalid credentials for username: {}", request.getUsername());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }

    private CustomUserDetails validateLoginInfo(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return (CustomUserDetails) authentication.getPrincipal();
    }
}