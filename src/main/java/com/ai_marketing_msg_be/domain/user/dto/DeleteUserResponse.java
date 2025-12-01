package com.ai_marketing_msg_be.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserResponse {

    private Long userId;
    private String message;
    private String deletedAt;

    public static DeleteUserResponse of(Long userId, String deletedAt) {
        return DeleteUserResponse.builder()
                .userId(userId)
                .message("User deleted successfully")
                .deletedAt(deletedAt)
                .build();
    }
}