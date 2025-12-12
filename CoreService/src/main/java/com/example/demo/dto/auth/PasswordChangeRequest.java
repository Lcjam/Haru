package com.example.demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "비밀번호 변경 요청 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {
    @Schema(description = "토큰 사용 여부", example = "true")
    private String isToken;
    
    @Schema(description = "이메일 (토큰 미사용 시)", example = "user@example.com")
    private String email;
    
    @Schema(description = "전화번호 (토큰 미사용 시)", example = "010-1234-5678")
    private String phoneNumber;
    
    @Schema(description = "현재 비밀번호", example = "oldPassword123!", required = true)
    private String currentPassword;
    
    @Schema(description = "새 비밀번호", example = "newPassword123!", required = true)
    private String newPassword;
    
    @Schema(description = "새 비밀번호 확인", example = "newPassword123!", required = true)
    private String confirmPassword;
}
