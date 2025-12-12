package com.example.demo.dto.auth;

import com.example.demo.dto.hobby.HobbyRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(description = "회원가입 요청 정보")
@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {
    @Schema(description = "이름", example = "홍길동", required = true)
    private String name;
    
    @Schema(description = "전화번호", example = "010-1234-5678", required = true)
    private String phoneNumber;
    
    @Schema(description = "이메일", example = "user@example.com", required = true)
    private String email;
    
    @Schema(description = "닉네임", example = "홍길동123", required = true)
    private String nickname;
    
    @Schema(description = "비밀번호", example = "password123!", required = true)
    private String password;
    
    @Schema(description = "자기소개", example = "안녕하세요!")
    private String bio;
    
    @Schema(description = "로그인 방법", example = "email")
    private String loginMethod;
    
    @Schema(description = "소셜 로그인 제공자", example = "google")
    private String socialProvider;
    
    @Schema(description = "취미 목록 (선택사항)")
    private List<HobbyRequest> hobbies;
}
