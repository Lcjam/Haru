package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenBlacklistService blacklistService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 인증이 필요 없는 공개 경로 목록
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/core/auth/signup",
            "/api/core/auth/login",
            "/api/core/auth/logout",
            "/api/core/auth/me/password/notoken",
            "/api/core/auth/me/password",
            "/api/core/auth/oauth2/**", // OAuth2 관련 경로 추가
            "/api/core/hobbies/**",
            "/api/core/profiles/user/*",
            "/api/core/market/products/all",
            "/api/core/market/products/all/filter",
            "/api/core/market/products/images/**",
            "/api/core/market/products/{id}",
            "/api/core/chat/**",
            "/api/core/chat/rooms/**",
            "/api/core/chat/rooms/{chatroomId}/read",
            "/api/core/chat/rooms/{chatroomId}/approve",
            "/api/core/chat/messages/**",
            "/api/core/boards/{boardId}/members",
            "/api/core/market/products/requests/approval-status",
            "/ws",
            "/ws/**",
            "/ws/redis/**",
            "/topic/**",
            "/app/**",
            "/swagger-ui/**", // Swagger UI 경로 추가
            "/v3/api-docs/**" // OpenAPI 문서 경로 추가
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // WebSocket 요청은 필터링 제외
        String upgradeHeader = request.getHeader("Upgrade");
        if (upgradeHeader != null && "websocket".equalsIgnoreCase(upgradeHeader)) {
            log.debug("WebSocket 요청으로 필터링 제외: {}", path);
            return true;
        }

        // 공개 경로인 경우 필터링 제외
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
        if (isPublic) {
            log.info("✅ 공개 경로로 필터링 제외: {}", path);
        } else {
            log.info("🔒 인증 필요 경로: {}", path);
        }
        return isPublic;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);
        String path = request.getRequestURI();

        // 로깅 추가
        log.info("JwtAuthenticationFilter 실행 - 경로: {}, 토큰 존재: {}", path, token != null);

        if (token != null) {
            try {
                boolean isValid = jwtTokenProvider.validateToken(token);
                boolean isBlacklisted = blacklistService.isBlacklisted(token);

                log.debug("Token validation: isValid={}, isBlacklisted={}", isValid, isBlacklisted);

                if (isValid && !isBlacklisted) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Authentication successful for user: {}", auth.getName());
                } else if (isBlacklisted) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token has been invalidated");
                    log.warn("Attempt to use blacklisted token for path: {}", path);
                    return;
                } else {
                    log.warn("Invalid token presented for path: {}", path);
                }
            } catch (Exception e) {
                log.error("JWT Authentication Error: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized access: Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출합니다.
     * "Authorization: Bearer [token]" 형식에서 토큰 부분만 반환합니다.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

