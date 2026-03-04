package com.example.demo.config;

/**
 * 인증 없이 접근 가능한 공개 경로의 단일 정의 목록.
 * SecurityConfig와 JwtAuthenticationFilter가 동일한 목록을 참조합니다.
 */
public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String[] PUBLIC_PATHS = {
            // 정적 리소스
            "/profile-images/**",
            "/chat-images/**",
            "/uploads/**",
            "/board-files/**",

            // 인증 — 공개
            "/api/core/auth/signup",
            "/api/core/auth/login",
            "/api/core/auth/logout",
            "/api/core/auth/me/password/notoken",
            "/api/core/auth/oauth2/**",

            // 취미 — 전체 공개
            "/api/core/hobbies",
            "/api/core/hobbies/simple",
            "/api/core/hobbies/categories",
            "/api/core/hobbies/*/categories",
            "/api/core/hobbies/categories/*",

            // 프로필 — 공개 조회
            "/api/core/profiles/user/*",

            // 마켓 — 공개 조회
            "/api/core/market/products/all",
            "/api/core/market/products/all/filter",
            "/api/core/market/products/images/**",
            "/api/core/market/products/requests/approved",
            "/api/core/market/products/requests/approval-status",
            "/api/core/market/products/*",

            // WebSocket
            "/ws",
            "/ws/**",
            "/topic/**",
            "/app/**",

            // Swagger / OpenAPI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };
}
