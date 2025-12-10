// http://localhost:8081 — 구현 해야하는 code서버 (백엔드 DB, redis, 웹소켓)

package com.example.demo.controller;

import com.example.demo.dto.auth.*;
import com.example.demo.service.UserService;
import com.example.demo.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        SignupResponse response = userService.registerUser(signupRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(BaseResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(BaseResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<LogoutResponse>> logout(@RequestHeader("Authorization") String token) {
        LogoutResponse response = userService.logout(token);
        if (response.isSuccess()) {
            return ResponseEntity.ok(BaseResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }
    }

    /**
     * 회원 탈퇴
     */
    @PostMapping("/me/withdrawal")
    public ResponseEntity<BaseResponse<WithdrawalResponse>> withdraw(
            @RequestHeader("Authorization") String token,
            @RequestBody WithdrawalRequest request) {

        WithdrawalResponse response = userService.withdrawUserByToken(token, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response, "400"));
        }

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * 비밀번호 변경
     */
    // 로그인한 유저를 위한 비밀번호 변경(토큰 존재할때)
    @PutMapping("/me/password")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody PasswordChangeRequest request) {
        // 서비스 호출
        PasswordChangeResponse response = userService.changePasswordByToken(token, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/me/password/notoken")
    public ResponseEntity<PasswordChangeResponse> changePassword(

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