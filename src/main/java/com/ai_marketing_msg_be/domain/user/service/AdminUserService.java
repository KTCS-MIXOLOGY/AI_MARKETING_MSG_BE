package com.ai_marketing_msg_be.domain.user.service;

import com.ai_marketing_msg_be.common.dto.PageResponse;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.user.dto.ApproveUserRequest;
import com.ai_marketing_msg_be.domain.user.dto.ApproveUserResponse;
import com.ai_marketing_msg_be.domain.user.dto.DeleteUserResponse;
import com.ai_marketing_msg_be.domain.user.dto.GetUserDetailResponse;
import com.ai_marketing_msg_be.domain.user.dto.GetUserListResponse;
import com.ai_marketing_msg_be.domain.user.dto.UpdateUserRequest;
import com.ai_marketing_msg_be.domain.user.dto.UpdateUserResponse;
import com.ai_marketing_msg_be.domain.user.entity.User;
import com.ai_marketing_msg_be.domain.user.entity.UserRole;
import com.ai_marketing_msg_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetUserListResponse> getUserList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findAllActive(pageable);

        Page<GetUserListResponse> responsePage = userPage.map(GetUserListResponse::from);

        log.info("Retrieved user list: page={}, size={}, totalElements={}",
                page, size, responsePage.getTotalElements());

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public GetUserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        log.info("Retrieved user detail: userId={}, username={}", userId, user.getUsername());

        return GetUserDetailResponse.from(user);
    }

    @Transactional
    public ApproveUserResponse approveUser(Long userId, ApproveUserRequest request) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserRole role = parseUserRole(request.getRole());

        user.approve(role);

        log.info("User approved: userId={}, username={}, role={}",
                userId, user.getUsername(), role);

        return ApproveUserResponse.from(user);
    }

    @Transactional
    public UpdateUserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndNotDeletedExcludingUser(request.getEmail(), userId)) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhoneAndNotDeletedExcludingUser(request.getPhone(), userId)) {
                throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
            }
        }

        UserRole role = request.getRole() != null ? parseUserRole(request.getRole()) : null;

        user.updateInfo(
                request.getEmail(),
                request.getPhone(),
                request.getDepartment(),
                role
        );

        log.info("User updated: userId={}, username={}", userId, user.getUsername());

        return UpdateUserResponse.from(user);
    }

    @Transactional
    public DeleteUserResponse deleteUser(Long userId) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.softDelete();

        log.info("User deleted (soft): userId={}, username={}", userId, user.getUsername());

        return DeleteUserResponse.of(userId, user.getDeletedAt().toString());
    }

    private UserRole parseUserRole(String roleStr) {
        try {
            return UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid user role: {}", roleStr);
            throw new BusinessException(ErrorCode.INVALID_USER_ROLE);
        }
    }
}