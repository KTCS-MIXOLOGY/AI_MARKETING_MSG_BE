package com.ai_marketing_msg_be.domain.user.entity;

import com.ai_marketing_msg_be.common.entity.BaseEntity;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "사용자")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "department", length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void approve(UserRole role) {
        if (this.status == UserStatus.APPROVED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_APPROVED);
        }
        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }
        this.status = UserStatus.APPROVED;
        this.role = role;
    }

    public void reject(UserRole role) {
        if (this.status == UserStatus.REJECTED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_REJECTED);
        }
        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }
        this.status = UserStatus.REJECTED;
        this.role = role;
    }

    public void updateInfo(String email, String phone, String department, UserRole role) {
        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }
        if (email != null) {
            this.email = email;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (department != null) {
            this.department = department;
        }
        if (role != null) {
            this.role = role;
        }
    }

    public void softDelete() {
        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }
        this.deletedAt = LocalDateTime.now();
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}