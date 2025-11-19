package com.ai_marketing_msg_be.auth.provider;

import com.ai_marketing_msg_be.common.exception.BusinessException;
import com.ai_marketing_msg_be.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateAccessToken(String username, Long userId, String role) {
        return generateToken(username, userId, role, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String username, Long userId, String role) {
        return generateToken(username, userId, role, REFRESH_TOKEN_EXPIRATION);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException exception) {
            log.error("Invalid JWT signature: {}", exception.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException exception) {
            log.error("Expired JWT token: {}", exception.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (Exception exception) {
            log.error("Invalid JWT token: {}", exception.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        if (username == null || role == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Collection<? extends GrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));

        UserDetails userDetails = User.builder()
                .username(username)
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public long getAccessTokenExpirationInSeconds() {
        return ACCESS_TOKEN_EXPIRATION / 1000;
    }

    private String generateToken(String username, Long userId, String role, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}