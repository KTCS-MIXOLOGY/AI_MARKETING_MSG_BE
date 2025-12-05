package com.ai_marketing_msg_be.domain.user.controller;

import com.ai_marketing_msg_be.common.dto.ApiResponse;
import com.ai_marketing_msg_be.common.dto.PageResponse;
import com.ai_marketing_msg_be.domain.user.dto.ApproveUserRequest;
import com.ai_marketing_msg_be.domain.user.dto.ApproveUserResponse;
import com.ai_marketing_msg_be.domain.user.dto.DeleteUserResponse;
import com.ai_marketing_msg_be.domain.user.dto.GetUserDetailResponse;
import com.ai_marketing_msg_be.domain.user.dto.GetUserListResponse;
import com.ai_marketing_msg_be.domain.user.dto.RejectUserRequest;
import com.ai_marketing_msg_be.domain.user.dto.RejectUserResponse;
import com.ai_marketing_msg_be.domain.user.dto.UpdateUserRequest;
import com.ai_marketing_msg_be.domain.user.dto.UpdateUserResponse;
import com.ai_marketing_msg_be.domain.user.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResponse<GetUserListResponse>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        log.info("Admin user list request: page={}, size={}", page, size);
        PageResponse<GetUserListResponse> response = adminUserService.getUserList(page, size);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @GetMapping("/{userId}")
    public ApiResponse<GetUserDetailResponse> getUserDetail(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        log.info("Admin user detail request: userId={}", userId);
        GetUserDetailResponse response = adminUserService.getUserDetail(userId);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @PatchMapping("/{userId}/approve")
    public ApiResponse<ApproveUserResponse> approveUser(
            @PathVariable Long userId,
            @Valid @RequestBody ApproveUserRequest request,
            HttpServletRequest httpRequest) {
        log.info("Admin user approve request: userId={}, role={}", userId, request.getRole());
        ApproveUserResponse response = adminUserService.approveUser(userId, request);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @PatchMapping("/{userId}/reject")
    public ApiResponse<RejectUserResponse> rejectUser(
            @PathVariable Long userId,
            @Valid @RequestBody RejectUserRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Admin user reject request: userId={}, role={}", userId, request.getRole());
        RejectUserResponse response = adminUserService.rejectUser(userId, request);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @PatchMapping("/{userId}")
    public ApiResponse<UpdateUserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        log.info("Admin user update request: userId={}", userId);
        UpdateUserResponse response = adminUserService.updateUser(userId, request);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<DeleteUserResponse> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        log.info("Admin user delete request: userId={}", userId);
        DeleteUserResponse response = adminUserService.deleteUser(userId);
        return ApiResponse.ok(response, httpRequest.getRequestURI());
    }
}