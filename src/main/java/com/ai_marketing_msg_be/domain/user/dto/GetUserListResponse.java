package com.ai_marketing_msg_be.domain.user.dto;

import com.ai_marketing_msg_be.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserListResponse {

    private Long userId;
    private String username;
    private String email;
    private String name;
    private String phone;
    private String department;
    private String role;
    private String status;
    private String createdAt;

    public static GetUserListResponse from(User user) {
        return GetUserListResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}