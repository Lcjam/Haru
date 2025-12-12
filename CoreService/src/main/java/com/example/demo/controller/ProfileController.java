package com.example.demo.controller;

import com.example.demo.dto.profile.*;
import com.example.demo.dto.auth.*;
import com.example.demo.dto.hobby.*;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.UserService;
import com.example.demo.util.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "프로필 관리", description = "사용자 프로필 조회, 수정, 이미지 업로드/삭제, 비밀번호 변경 등 프로필 관련 API")
@RestController
@RequestMapping("/api/core/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    
    private final UserService userService;
    private final FileStorageService fileStorageService;
    
    @Operation(
            summary = "자신의 프로필 조회",
            description = "인증된 사용자의 프로필 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "프로필 조회 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ProfileResponse>> getMyProfile(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        ProfileResponse profile = userService.getUserProfileByToken(token);
        
        if (!profile.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(profile, "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(profile));
    }
    
    @Operation(
            summary = "닉네임으로 다른 사용자의 프로필 조회",
            description = "닉네임을 입력받아 해당 사용자의 공개 프로필 정보를 조회합니다. 인증이 필요하지 않습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "프로필 조회 실패 (존재하지 않는 닉네임 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/user/{nickname}")
    public ResponseEntity<BaseResponse<ProfileResponse>> getUserProfile(
            @Parameter(description = "조회할 사용자의 닉네임", required = true, example = "홍길동")
            @PathVariable String nickname) {
        ProfileResponse profile = userService.getPublicProfile(nickname);
        
        if (!profile.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(profile, "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(profile));
    }
    
    @Operation(
            summary = "이메일로 사용자 프로필 조회 (관리자 기능)",
            description = "이메일을 입력받아 사용자의 프로필 정보를 조회합니다. 관리자 권한이 필요합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "프로필 조회 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/admin/user/{email}")
    public ResponseEntity<BaseResponse<ProfileResponse>> getAdminUserProfile(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "조회할 사용자의 이메일", required = true, example = "user@example.com")
            @PathVariable String email) {
        
        // 주의: 실제 구현에서는 여기에 관리자 권한 검증 필요
        ProfileResponse profile = userService.getUserProfile(email);
        
        if (!profile.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(profile, "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(profile));
    }
    
    @Operation(
            summary = "자신의 프로필 수정",
            description = "인증된 사용자의 프로필 정보를 수정합니다. 닉네임, 취미 등 정보를 업데이트할 수 있습니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 수정 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "프로필 수정 실패 (유효성 검증 실패, 중복된 닉네임 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PutMapping("/me")
    public ResponseEntity<BaseResponse<ProfileUpdateResponse>> updateMyProfile(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "프로필 수정 요청 정보", required = true)
            @RequestBody ProfileUpdateRequest request) {
        
        // 취미 정보의 기본 유효성 검증
        if (request.getHobbies() != null && !request.getHobbies().isEmpty()) {
            for (HobbyRequest hobby : request.getHobbies()) {
                if (hobby.getCategoryId() == null || hobby.getHobbyId() == null) {
                    return ResponseEntity.badRequest().body(BaseResponse.error(
                        ProfileUpdateResponse.builder()
                            .success(false)
                            .message("카테고리 또는 취미 정보가 누락되었습니다.")
                            .build(),
                        "400"
                    ));
                }
            }
        }
        
        ProfileUpdateResponse response = userService.updateProfileByToken(token, request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @Operation(
            summary = "비밀번호 변경",
            description = "인증된 사용자의 비밀번호를 변경합니다. 현재 비밀번호와 새 비밀번호를 입력받습니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "비밀번호 변경 실패 (현재 비밀번호 불일치, 유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PutMapping("/me/password")
    public ResponseEntity<BaseResponse<PasswordChangeResponse>> changePassword(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "비밀번호 변경 요청 정보 (현재 비밀번호, 새 비밀번호)", required = true)
            @RequestBody PasswordChangeRequest request) {
        
        PasswordChangeResponse response = userService.changePasswordByToken(token, request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @Operation(
            summary = "프로필 이미지 업로드",
            description = "인증된 사용자의 프로필 이미지를 업로드합니다. 이미지 파일만 업로드 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이미지 업로드 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미지 업로드 실패 (파일이 비어있음, 잘못된 파일 형식 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/me/image")
    public ResponseEntity<BaseResponse<ProfileImageResponse>> uploadProfileImage(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {
        
        // 파일 기본 검증
        if (file.isEmpty()) {
            ProfileImageResponse errorResponse = ProfileImageResponse.builder()
                    .success(false)
                    .message("업로드할 파일이 비어있습니다.")
                    .build();
            return ResponseEntity.badRequest().body(BaseResponse.error(errorResponse, "400"));
        }
        
        try {
            ProfileImageResponse response = userService.uploadProfileImageByToken(token, file);
            
            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
            }
            
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            // 파일 형식이나 크기 검증 실패 시
            ProfileImageResponse errorResponse = ProfileImageResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(BaseResponse.error(errorResponse, "400"));
        } catch (Exception e) {
            log.error("이미지 업로드 중 예상치 못한 오류 발생: {}", e.getMessage());
            ProfileImageResponse errorResponse = ProfileImageResponse.builder()
                    .success(false)
                    .message("이미지 업로드 중 서버 오류가 발생했습니다.")
                    .build();
            return ResponseEntity.status(500).body(BaseResponse.error(errorResponse, "500"));
        }
    }
    
    @Operation(
            summary = "프로필 이미지 삭제",
            description = "인증된 사용자의 프로필 이미지를 삭제합니다. 기본 이미지로 변경됩니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이미지 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미지 삭제 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @DeleteMapping("/me/image")
    public ResponseEntity<BaseResponse<ProfileImageResponse>> deleteProfileImage(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        
        ProfileImageResponse response = userService.deleteProfileImageByToken(token);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @Operation(
            summary = "프로필 이미지 정보 조회",
            description = "인증된 사용자의 프로필 이미지 URL을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이미지 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미지 정보 조회 실패 (유효하지 않은 토큰 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/me/image-info")
    public ResponseEntity<BaseResponse<Map<String, String>>> getMyProfileImageInfo(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        
        String tokenWithoutBearer = token;
        if (token.startsWith("Bearer ")) {
            tokenWithoutBearer = token.substring(7);
        }
        
        if (!userService.tokenUtils.isTokenValid(tokenWithoutBearer)) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "유효하지 않은 인증 토큰입니다.");
            return ResponseEntity.badRequest().body(BaseResponse.error(errorData, "400"));
        }
        
        String email = userService.tokenUtils.getEmailFromToken(tokenWithoutBearer);
        String imageUrl = userService.getProfileImageUrl(email);
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(BaseResponse.success(responseData));
    }
    
    /**
     * 프로필 이미지 파일 제공 
     * 정적 리소스로 접근하도록 리다이렉션 처리
     */
    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<?> getProfileImage(@PathVariable String filename) {
        try {
            // 파일 경로 생성
            java.nio.file.Path imagePath = java.nio.file.Paths.get(System.getProperty("user.dir") + "/src/main/resources/static/profile-images/" + filename);
            java.io.File file = imagePath.toFile();
            log.debug("프로필 이미지 파일 경로: {}", imagePath);
            
            // 파일이 존재하는지 확인
            if (!file.exists()) {
                log.error("프로필 이미지 파일을 찾을 수 없음: {}", filename);
                return ResponseEntity.notFound().build();
            }
            
            // 파일 내용 읽기
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toURI());
            
            // MIME 타입 감지
            String contentType = java.nio.file.Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 이미지 직접 반환
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            log.error("프로필 이미지 제공 실패: {}", ex.getMessage());
            return ResponseEntity.status(500).body("이미지 로드 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 기본 프로필 이미지 반환
     * 정적 리소스로 접근하도록 리다이렉션 처리
     */
    @GetMapping("/image/default")
    public ResponseEntity<?> getDefaultProfileImage() {
        try {
            String defaultImageName = fileStorageService.getDefaultProfileImageName();
            
            // 파일 경로 생성
            java.nio.file.Path imagePath = java.nio.file.Paths.get(System.getProperty("user.dir") + "/src/main/resources/static/profile-images/" + defaultImageName);
            java.io.File file = imagePath.toFile();
            
            // 파일이 존재하는지 확인
            if (!file.exists()) {
                log.error("기본 프로필 이미지 파일을 찾을 수 없음: {}", defaultImageName);
                return ResponseEntity.notFound().build();
            }
            
            // 파일 내용 읽기
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toURI());
            
            // MIME 타입 감지
            String contentType = java.nio.file.Files.probeContentType(imagePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 이미지 직접 반환
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            log.error("기본 프로필 이미지 제공 실패: {}", ex.getMessage());
            return ResponseEntity.status(500).body("이미지 로드 중 오류가 발생했습니다.");
        }
    }
    
    @Operation(
            summary = "마이페이지 정보 조회",
            description = "인증된 사용자의 마이페이지 정보를 조회합니다. 프로필, 활동 내역 등 종합 정보를 반환합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "마이페이지 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "마이페이지 정보 조회 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/mypage")
    public ResponseEntity<BaseResponse<MyPageResponse>> getMyPageInfo(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        MyPageResponse myPage = userService.getMyPageInfoByToken(token);
        
        if (!myPage.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(myPage, "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(myPage));
    }
}
