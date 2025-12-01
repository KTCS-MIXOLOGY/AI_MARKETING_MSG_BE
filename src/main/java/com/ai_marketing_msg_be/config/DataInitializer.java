package com.ai_marketing_msg_be.config;

import com.ai_marketing_msg_be.domain.user.entity.User;
import com.ai_marketing_msg_be.domain.user.entity.UserRole;
import com.ai_marketing_msg_be.domain.user.entity.UserStatus;
import com.ai_marketing_msg_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.phone:010-0000-0000}")
    private String adminPhone;

    @Value("${app.admin.department:IT팀}")
    private String adminDepartment;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminAccount();
    }

    private void initializeAdminAccount() {
        if (userRepository.existsByUsername(adminUsername)) {
            log.info("ADMIN account '{}' already exists. Skipping initialization.", adminUsername);
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .name(adminName)
                .phone(adminPhone)
                .department(adminDepartment)
                .role(UserRole.ADMIN)
                .status(UserStatus.APPROVED)
                .build();

        userRepository.save(admin);

        log.info("========================================");
        log.info("✅ Default ADMIN account created");
        log.info("   Username: {}", adminUsername);
        log.info("   Email: {}", adminEmail);
        log.info("   ⚠️  Please change the password after first login");
        log.info("========================================");
    }
}