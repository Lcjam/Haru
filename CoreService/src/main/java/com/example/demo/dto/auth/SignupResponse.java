package com.example.demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 응답 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    @Schema(description = "회원가입 성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "가입한 사용자의 이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "응답 메시지", example = "회원가입이 완료되었습니다.")
    private String message;
    
    @Schema(description = "초기 도파민 수치", example = "50")
    private Integer initialDopamine;
    
    @Schema(description = "초기 활동 포인트", example = "0")
    private Integer initialPoints;
}
