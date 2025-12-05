package com.ai_marketing_msg_be.domain.user.controller;


import com.ai_marketing_msg_be.auth.details.CustomUserDetails;
import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.domain.user.dto.GetUserDetailResponse;
import com.ai_marketing_msg_be.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<GetUserDetailResponse> me(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            HttpServletRequest request
    ) {
        log.info("customUserDetails: {}", customUserDetails);  // ← 추가

        Long userId = customUserDetails.getUserId();

        log.info("My profile request - userId: {}", userId);

        GetUserDetailResponse response = userService.me(userId);

        return ApiResponse.ok(response, request.getRequestURI());
    }

}
