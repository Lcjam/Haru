package com.example.demo.dto.profile;

import com.example.demo.model.UserHobby;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "프로필 응답 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    @Schema(description = "조회 성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "응답 메시지", example = "프로필 조회에 성공했습니다.")
    private String message;
    
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "이름", example = "홍길동")
    private String name;
    
    @Schema(description = "닉네임", example = "홍길동123")
    private String nickname;
    
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;
    
    @Schema(description = "프로필 이미지 URL", example = "/api/core/profiles/image/profile.jpg")
    private String profileImageUrl;
    
    @Schema(description = "자기소개", example = "안녕하세요!")
    private String bio;
    
    @Schema(description = "로그인 방법", example = "email")
    private String loginMethod;
    
    @Schema(description = "계정 상태", example = "ACTIVE")
    private String accountStatus;
    
    @Schema(description = "가입일시", example = "2024-01-01T00:00:00")
    private LocalDateTime signupDate;
    
    @Schema(description = "마지막 로그인 시간", example = "2024-12-11T12:00:00")
    private LocalDateTime lastLoginTime;
    
    @Schema(description = "도파민 수치", example = "50")
    private Integer dopamine;
    
    @Schema(description = "활동 포인트", example = "100")
    private Integer points;
    
    @Schema(description = "사용자 취미 정보 목록")
    private List<HobbyInfo> hobbies;
    
    @Schema(description = "취미 정보")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HobbyInfo {
        @Schema(description = "취미 ID", example = "1")
        private Long hobbyId;
        
        @Schema(description = "취미 이름", example = "축구")
        private String hobbyName;
        
        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryId;
        
        @Schema(description = "카테고리 이름", example = "스포츠")
        private String categoryName;
        
        // UserHobby 모델 객체로부터 HobbyInfo 생성
        public static HobbyInfo fromUserHobby(UserHobby userHobby) {
            return HobbyInfo.builder()
                    .hobbyId(userHobby.getHobbyId())
                    .hobbyName(userHobby.getHobbyName())
                    .categoryId(userHobby.getCategoryId())
                    .categoryName(userHobby.getCategoryName())
                    .build();
        }
    }
}
