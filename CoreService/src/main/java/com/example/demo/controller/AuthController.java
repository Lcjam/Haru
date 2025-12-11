// http://localhost:8081 — 구현 해야하는 code서버 (백엔드 DB, redis, 웹소켓)

package com.example.demo.controller;

import com.example.demo.dto.auth.*;
import com.example.demo.service.UserService;
import com.example.demo.util.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "사용자 인증 관련 API (회원가입, 로그인, 로그아웃, 비밀번호 변경 등)")
public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 이메일, 비밀번호, 닉네임 등 필수 정보와 선택적 취미 정보를 입력받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "회원가입 실패 (이메일 중복, 닉네임 중복, 유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponse>> signup(
            @Parameter(description = "회원가입 요청 정보", required = true)
            @Valid @RequestBody SignupRequest signupRequest) {
        SignupResponse response = userService.registerUser(signupRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(BaseResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 사용하여 로그인합니다. 성공 시 JWT 토큰을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "로그인 실패 (이메일/비밀번호 불일치, 계정 잠금 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @Parameter(description = "로그인 요청 정보 (이메일, 비밀번호)", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(BaseResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
    }

    @Operation(
            summary = "로그아웃",
            description = "JWT 토큰을 블랙리스트에 추가하여 로그아웃합니다. 인증이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "로그아웃 실패 (유효하지 않은 토큰 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<LogoutResponse>> logout(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        LogoutResponse response = userService.logout(token);
        if (response.isSuccess()) {
            return ResponseEntity.ok(BaseResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "사용자 계정을 탈퇴 처리합니다. 인증이 필요하며, 탈퇴 사유를 입력받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "회원 탈퇴 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/me/withdrawal")
    public ResponseEntity<BaseResponse<WithdrawalResponse>> withdraw(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "탈퇴 요청 정보", required = true)
            @RequestBody WithdrawalRequest request) {

        WithdrawalResponse response = userService.withdrawUserByToken(token, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "비밀번호 변경 (인증 필요)",
            description = "로그인한 사용자의 비밀번호를 변경합니다. 현재 비밀번호와 새 비밀번호를 입력받습니다. 인증이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공",
                    content = @Content(schema = @Schema(implementation = PasswordChangeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "비밀번호 변경 실패 (현재 비밀번호 불일치, 새 비밀번호 정책 위반 등)",
                    content = @Content(schema = @Schema(implementation = PasswordChangeResponse.class))
            )
    })
    @PutMapping("/me/password")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "비밀번호 변경 요청 정보", required = true)
            @RequestBody PasswordChangeRequest request) {
        // 서비스 호출
        PasswordChangeResponse response = userService.changePasswordByToken(token, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "비밀번호 변경 (인증 불필요)",
            description = "이메일과 전화번호 인증을 통해 비밀번호를 변경합니다. 토큰 인증이 필요하지 않습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공",
                    content = @Content(schema = @Schema(implementation = PasswordChangeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "비밀번호 변경 실패 (이메일/전화번호 불일치, 새 비밀번호 정책 위반 등)",
                    content = @Content(schema = @Schema(implementation = PasswordChangeResponse.class))
            )
    })
    @PutMapping("/me/password/notoken")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @Parameter(description = "비밀번호 변경 요청 정보 (이메일, 전화번호, 새 비밀번호)", required = true)
            @RequestBody PasswordChangeRequest request) {
        log.debug("/me/password/notoken에 접근했습니다. 요청: {}", request);
        PasswordChangeResponse response = userService.changePassword(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 존재 여부 검증
     */

    /**
     * 이메일과 전화번호 검증증
     */
}