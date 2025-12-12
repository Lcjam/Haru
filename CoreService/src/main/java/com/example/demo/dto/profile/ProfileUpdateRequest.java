package com.example.demo.dto.profile;

import com.example.demo.dto.hobby.HobbyRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "프로필 수정 요청 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    @Schema(description = "이름", example = "홍길동")
    private String name;
    
    @Schema(description = "닉네임", example = "홍길동123")
    private String nickname;
    
    @Schema(description = "자기소개", example = "안녕하세요!")
    private String bio;
    
    @Schema(description = "취미 목록")
    private List<HobbyRequest> hobbies;
}
