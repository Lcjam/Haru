package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.demo.util.BaseResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "OAuth2 소셜 로그인", description = "구글, 네이버, 카카오 소셜 로그인 관련 API")
@RestController
@RequestMapping("/api/core/auth/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    @Operation(
            summary = "구글 로그인",
            description = "구글 소셜 로그인 페이지로 리다이렉트합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "구글 로그인 페이지로 리다이렉트"
            )
    })
    @GetMapping("/google")
    public RedirectView googleLogin() {
        log.info("구글 로그인 요청");
        return new RedirectView("/api/core/auth/oauth2/authorize/google?redirect_uri=http://localhost:3000/oauth2/redirect");
    }

    @Operation(
            summary = "네이버 로그인",
            description = "네이버 소셜 로그인 페이지로 리다이렉트합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "네이버 로그인 페이지로 리다이렉트"
            )
    })
    @GetMapping("/naver")
    public RedirectView naverLogin() {
        log.info("네이버 로그인 요청");
        return new RedirectView("/api/core/auth/oauth2/authorize/naver?redirect_uri=http://localhost:3000/oauth2/redirect");
    }

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 소셜 로그인 페이지로 리다이렉트합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "카카오 로그인 페이지로 리다이렉트"
            )
    })
    @GetMapping("/kakao")
    public RedirectView kakaoLogin() {
        log.info("카카오 로그인 요청");
        return new RedirectView("/api/core/auth/oauth2/authorize/kakao?redirect_uri=http://localhost:3000/oauth2/redirect");
    }

    @Operation(
            summary = "OAuth2 서비스 상태 확인",
            description = "OAuth2 인증 서비스의 상태 및 지원하는 제공자를 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "서비스 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/status")
    public BaseResponse<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OAuth2 인증 서비스가 활성화되어 있습니다.");
        response.put("providers", new String[]{"google", "naver", "kakao"});
        
        return BaseResponse.success(response);
    }
}
