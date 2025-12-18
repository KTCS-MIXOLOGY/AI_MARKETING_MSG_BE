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

        String path = request.getRequestURI();
        log.info("=== JWT Filter 실행 - 경로: {} ===", path);  // ← 추가

        final String token = resolveToken(request);
        log.info("추출된 토큰: {}", token != null ? "있음 (길이: " + token.length() + ")" : "없음");  // ← 추가

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                log.info("토큰 검증 성공");  // ← 추가

                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info("SecurityContext에 인증 정보 설정 완료 - username: {}",
                        jwtTokenProvider.getUsernameFromToken(token));  // ← info로 변경
            } else {
                log.warn("토큰이 없거나 유효하지 않음");  // ← 추가
            }
        } catch (BusinessException exception) {
            SecurityContextHolder.clearContext();
            request.setAttribute("exception", exception.getErrorCode());
            log.error("JWT 인증 실패 (BusinessException): {}", exception.getMessage());
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            log.error("JWT 인증 중 예상치 못한 오류: {}", exception.getMessage(), exception);  // ← 스택트레이스 추가
        }

        log.info("=== JWT Filter 종료 ===");  // ← 추가
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_STRING);

        log.info("Authorization 헤더: {}",
                header != null ? header.substring(0, Math.min(20, header.length())) + "..." : "없음");  // ← 추가

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}