package com.example.demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 응답 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    @Schema(description = "로그인 성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "사용자 닉네임", example = "홍길동123")
    private String nickname;
    
    @Schema(description = "응답 메시지", example = "로그인에 성공했습니다.")
    private String message;
}
