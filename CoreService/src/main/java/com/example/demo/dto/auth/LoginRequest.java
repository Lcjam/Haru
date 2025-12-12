package com.example.demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "로그인 요청 정보")
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    @Schema(description = "이메일", example = "user@example.com", required = true)
    private String email;
    
    @Schema(description = "비밀번호", example = "password123!", required = true)
    private String password;
}
