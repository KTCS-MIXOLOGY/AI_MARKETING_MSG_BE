package com.ai_marketing_msg_be.auth.details;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String role;
    private final boolean isApproved;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(
            Long userId,
            String username,
            String password,
            String role,
            boolean isApproved
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isApproved = isApproved;
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    public static CustomUserDetails from(com.ai_marketing_msg_be.domain.user.entity.User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().name(),
                user.getStatus().name().equals("APPROVED")
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isApproved;
    }
}