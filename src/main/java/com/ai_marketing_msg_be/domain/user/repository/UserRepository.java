package com.ai_marketing_msg_be.domain.user.repository;

import com.ai_marketing_msg_be.domain.user.entity.User;
import com.ai_marketing_msg_be.domain.user.entity.UserRole;
import com.ai_marketing_msg_be.domain.user.entity.UserStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deletedAt IS NULL AND u.id != :userId")
    boolean existsByEmailAndNotDeletedExcludingUser(@Param("email") String email, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.phone = :phone AND u.deletedAt IS NULL AND u.id != :userId")
    boolean existsByPhoneAndNotDeletedExcludingUser(@Param("phone") String phone, @Param("userId") Long userId);
}