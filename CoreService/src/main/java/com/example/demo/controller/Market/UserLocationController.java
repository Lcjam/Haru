package com.example.demo.controller.Market;

import com.example.demo.dto.Market.LocationRequest;
import com.example.demo.model.Market.UserLocation;
import com.example.demo.service.Market.UserLocationService;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.util.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 위치 관리", description = "마켓플레이스 사용자 위치 정보 업데이트 및 조회 관련 API")
@RestController
@RequestMapping("/api/core/market/users")
public class UserLocationController {
    private final UserLocationService userLocationService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserLocationController(UserLocationService userLocationService, JwtTokenProvider jwtTokenProvider) {
        this.userLocationService = userLocationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(
            summary = "사용자 위치 정보 업데이트",
            description = "사용자의 위치 정보를 등록하거나 수정합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "위치 정보 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 누락 또는 유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/location")
    public ResponseEntity<BaseResponse<String>> updateUserLocation(
            @Parameter(description = "위치 정보 요청 (위도, 경도, 위치명)", required = true)
            @RequestBody LocationRequest request,
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰 검증
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(BaseResponse.error("토큰이 누락되었습니다. 인증이 필요합니다."));
        }

        // JWT에서 이메일 추출
        String email;
        try {
            email = jwtTokenProvider.getUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(BaseResponse.error("유효하지 않은 토큰입니다."));
        }

        // 위치 정보 객체 생성 및 저장
        UserLocation location = new UserLocation();
        location.setEmail(email);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setLocationName(request.getLocationName());

        userLocationService.updateUserLocation(location);

        return ResponseEntity.ok(new BaseResponse<>("사용자 위치 업데이트 완료"));
    }

    @Operation(
            summary = "사용자 최신 위치 정보 조회",
            description = "인증된 사용자의 최신 위치 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "위치 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 누락 또는 유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/location/latest")
    public ResponseEntity<BaseResponse<UserLocation>> getUserLatestLocation(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 토큰 검증
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(BaseResponse.error("토큰이 누락되었습니다. 인증이 필요합니다."));
        }

        // JWT에서 이메일 추출
        String email;
        try {
            email = jwtTokenProvider.getUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(BaseResponse.error("유효하지 않은 토큰입니다."));
        }

        // 최신 위치 정보 가져오기
        UserLocation latestLocation = userLocationService.getUserLatestLocation(email);

        // 사용자의 위치 정보가 없는 경우 예외 처리
        if (latestLocation == null) {
            return ResponseEntity.ok(new BaseResponse<>(null, "사용자의 위치 정보가 존재하지 않습니다."));
        }

        return ResponseEntity.ok(new BaseResponse<>(latestLocation, "사용자의 최신 위치 조회 완료"));
    }
}
