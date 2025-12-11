package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Haru API")
                        .description("""
                                Haru 애플리케이션의 API 문서입니다.
                                
                                ## 주요 기능
                                - 사용자 인증 및 회원 관리
                                - 취미 기반 마켓플레이스
                                - 실시간 채팅
                                - 게시판 및 커뮤니티
                                - 위치 기반 서비스
                                
                                ## 인증
                                대부분의 API는 JWT 토큰 인증이 필요합니다.
                                로그인 후 받은 토큰을 `Authorization` 헤더에 `Bearer {token}` 형식으로 전달하세요.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Haru Team")
                                .email("haruteam@example.com")
                                .url("https://github.com/Lcjam/Haru"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8081")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://sunbee.world")
                                .description("프로덕션 서버")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 토큰을 입력하세요. 형식: Bearer {token}")));
    }
}
