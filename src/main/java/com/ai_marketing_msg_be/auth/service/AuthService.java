package com.ai_marketing_msg_be.auth.service;

import com.ai_marketing_msg_be.auth.details.CustomUserDetails;
import com.ai_marketing_msg_be.auth.dto.AuthLoginRequest;
import com.ai_marketing_msg_be.auth.dto.AuthLoginResponse;
import com.ai_marketing_msg_be.auth.dto.AuthRefreshRequest;
import com.ai_marketing_msg_be.auth.dto.AuthRefreshResponse;
import com.ai_marketing_msg_be.auth.dto.AuthRegisterRequest;
import com.ai_marketing_msg_be.auth.dto.AuthRegisterResponse;
import com.ai_marketing_msg_be.auth.provider.JwtTokenProvider;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.user.entity.User;
import com.ai_marketing_msg_be.domain.user.entity.UserRole;
import com.ai_marketing_msg_be.domain.user.entity.UserStatus;
import com.ai_marketing_msg_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthLoginResponse login(AuthLoginRequest request) {
        try {
            CustomUserDetails userDetails = validateLoginInfo(
                    request.getUsername(),
                    request.getPassword()
            );

            log.debug("User authenticated successfully: userId={}, role={}",
                    userDetails.getUserId(), userDetails.getRole());

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

            log.info("Login successful for username: {}", request.getUsername());

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

    @Transactional
    public AuthRegisterResponse register(AuthRegisterRequest request) {
        validateRegisterInfo(request);

        log.debug("Validation passed. Encoding password for username: {}", request.getUsername());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .role(UserRole.EXECUTOR)
                .status(UserStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: userId={}, username={}, status={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getStatus());

        return AuthRegisterResponse.of(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getStatus().name(),
                savedUser.getCreatedAt().toString()
        );
    }

    @Transactional
    public AuthRefreshResponse refresh(AuthRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        validateRefreshToken(refreshToken);

        String accessToken = jwtTokenProvider.generateAccessToken(
                jwtTokenProvider.getUsernameFromToken(refreshToken),
                jwtTokenProvider.getUserIdFromToken(refreshToken),
                jwtTokenProvider.getRoleFromToken(refreshToken)
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                jwtTokenProvider.getUsernameFromToken(refreshToken),
                jwtTokenProvider.getUserIdFromToken(refreshToken),
                jwtTokenProvider.getRoleFromToken(refreshToken)
        );

        return AuthRefreshResponse.create(accessToken, refreshToken,
                jwtTokenProvider.getAccessTokenExpirationInSeconds());
    }

    private CustomUserDetails validateLoginInfo(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return (CustomUserDetails) authentication.getPrincipal();
    }

    private void validateRegisterInfo(AuthRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - duplicate username: {}", request.getUsername());
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - duplicate email: {}", request.getEmail());
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        log.debug("Validation successful for username: {}", request.getUsername());
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}