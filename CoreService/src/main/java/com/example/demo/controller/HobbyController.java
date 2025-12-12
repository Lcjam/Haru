package com.example.demo.controller;

import com.example.demo.util.BaseResponse;
import com.example.demo.dto.hobby.HobbyRequest;
import com.example.demo.model.Category;
import com.example.demo.model.Hobby;
import com.example.demo.model.UserHobby;
import com.example.demo.service.HobbyService;
import com.example.demo.util.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "취미 및 카테고리 관리", description = "취미 목록 조회, 카테고리 조회, 사용자 취미 등록/수정 관련 API")
@RestController
@RequestMapping("/api/core/hobbies")
@RequiredArgsConstructor
@Slf4j
public class HobbyController {

    private final HobbyService hobbyService;
    private final TokenUtils tokenUtils;

    @Operation(
            summary = "모든 취미 목록 조회 (카테고리 정보 포함)",
            description = "모든 취미 목록을 조회합니다. 각 취미에 속한 카테고리 정보도 함께 반환됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "취미 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<Hobby>>> getAllHobbies() {
        List<Hobby> hobbies = hobbyService.getAllHobbiesWithCategories();
        return ResponseEntity.ok(BaseResponse.success(hobbies));
    }
    
    @Operation(
            summary = "모든 취미 목록 조회 (카테고리 정보 미포함)",
            description = "모든 취미 목록을 조회합니다. 카테고리 정보는 포함되지 않습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "취미 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/simple")
    public ResponseEntity<BaseResponse<List<Hobby>>> getAllHobbiesSimple() {
        List<Hobby> hobbies = hobbyService.getAllHobbies();
        return ResponseEntity.ok(BaseResponse.success(hobbies));
    }
    
    @Operation(
            summary = "모든 카테고리 목록 조회",
            description = "등록된 모든 카테고리 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/categories")
    public ResponseEntity<BaseResponse<List<Category>>> getAllCategories() {
        List<Category> categories = hobbyService.getAllCategories();
        return ResponseEntity.ok(BaseResponse.success(categories));
    }
    
    @Operation(
            summary = "특정 카테고리에 속한 취미 목록 조회",
            description = "카테고리 ID를 입력받아 해당 카테고리에 속한 취미 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "취미 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<BaseResponse<List<Hobby>>> getHobbiesByCategoryId(
            @Parameter(description = "카테고리 ID", required = true, example = "1")
            @PathVariable Long categoryId) {
        List<Hobby> hobbies = hobbyService.getHobbiesByCategoryId(categoryId);
        return ResponseEntity.ok(BaseResponse.success(hobbies));
    }

    @Operation(
            summary = "사용자의 취미 목록 조회",
            description = "인증된 사용자의 취미 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 취미 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/user")
    public ResponseEntity<?> getUserHobbies(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "인증되지 않은 요청입니다.");
            return ResponseEntity.status(401).body(BaseResponse.error(errorData, "401"));
        }
        
        List<UserHobby> userHobbies = hobbyService.getUserHobbies(email);
        return ResponseEntity.ok(BaseResponse.success(userHobbies));
    }

    @Operation(
            summary = "사용자의 취미 등록/수정",
            description = "인증된 사용자의 취미를 등록하거나 수정합니다. 기존 취미는 모두 삭제되고 새로운 취미로 교체됩니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "취미 등록/수정 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "취미 등록/수정 실패 (유효하지 않은 취미/카테고리 정보 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/user")
    public ResponseEntity<?> updateUserHobbies(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "등록할 취미 목록 (hobbyId, categoryId 포함)", required = true)
            @RequestBody List<HobbyRequest> hobbies) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "인증되지 않은 요청입니다.");
            return ResponseEntity.status(401).body(BaseResponse.error(errorData, "401"));
        }
        
        try {
            hobbyService.registerUserHobbies(email, hobbies);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "취미 정보가 업데이트되었습니다.");
            responseData.put("count", hobbies.size());
            
            return ResponseEntity.ok(BaseResponse.success(responseData));
            
        } catch (Exception e) {
            log.error("취미 업데이트 중 오류 발생: {}", e.getMessage());
            
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "취미 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(BaseResponse.error(errorData, "400"));
        }
    }
}