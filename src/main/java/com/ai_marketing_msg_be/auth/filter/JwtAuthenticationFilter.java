package com.ai_marketing_msg_be.auth.filter;

import com.ai_marketing_msg_be.auth.provider.JwtTokenProvider;
import com.ai_marketing_msg_be.common.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_STRING = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String token = resolveToken(request);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("Set Authentication in SecurityContext for username: {}",
                        jwtTokenProvider.getUsernameFromToken(token));
            }
        } catch (BusinessException exception) {
            SecurityContextHolder.clearContext();
            request.setAttribute("exception", exception.getErrorCode());
            log.error("JWT authentication failed: {}", exception.getMessage());
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            log.error("Unexpected error during JWT authentication: {}", exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_STRING);

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}