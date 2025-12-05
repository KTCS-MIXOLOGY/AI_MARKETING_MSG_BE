package com.ai_marketing_msg_be.domain.user.service;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import com.ai_marketing_msg_be.domain.user.dto.GetUserDetailResponse;
import com.ai_marketing_msg_be.domain.user.dto.UpdateMyInfoRequest;
import com.ai_marketing_msg_be.domain.user.dto.UpdateUserResponse;
import com.ai_marketing_msg_be.domain.user.entity.User;
import com.ai_marketing_msg_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public GetUserDetailResponse me(Long userId) {
        User user = getUser(userId);
        return GetUserDetailResponse.from(user);
    }

    @Transactional
    public UpdateUserResponse update(Long userId, UpdateMyInfoRequest request) {
        User user = getUser(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmailDuplication(request.getEmail(), userId);
        }

        user.updateInfo(
                request.getEmail(),
                request.getPhone(),
                request.getDepartment(),
                null
        );

        log.info("User info updated - userId: {}, email: {}, phone: {}, department: {}",
                userId, user.getEmail(), user.getPhone(), user.getDepartment());

        return UpdateUserResponse.from(user);

    }

    private void validateEmailDuplication(String email, Long userId) {
        userRepository.findByEmail(email)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
                    }
                });
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
