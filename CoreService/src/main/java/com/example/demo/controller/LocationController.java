package com.example.demo.controller;

import com.example.demo.util.BaseResponse;
import com.example.demo.model.Location;
import com.example.demo.service.LocationService;
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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.NotificationService;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;

import java.security.Principal;
import java.time.LocalDateTime;

@Tag(name = "위치 공유", description = "채팅방 내 위치 공유 관련 API (WebSocket 및 REST API)")
@RestController
@RequestMapping("/api/core/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final TokenUtils tokenUtils;
    private final LocationService locationService;
    private final NotificationService notificationService;
    private final UserMapper userMapper;


    /**
     * WebSocket을 통한 위치 업데이트 처리
     * /app/location/{chatroomId} 로 전송된 메시지를 처리합니다.
     */
    @MessageMapping("/location/{chatroomId}")
    public void handleLocationUpdate(
            @Payload Location location,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        if (principal == null) {
            log.error("위치 업데이트 처리 오류: 인증되지 않은 사용자");
            return;
        }

        String senderEmail = principal.getName();
        log.info("위치 업데이트 수신: chatroomId={}, senderEmail={}, location={}", 
                location.getChatroomId(), senderEmail, location);
        

        try {
            // 위치 정보 저장
            location.setEmail(senderEmail);
            location.setTimestamp(LocalDateTime.now());
            locationService.saveLocation(location);

            // 위치 정보를 채팅방의 모든 구독자에게 브로드캐스트
            messagingTemplate.convertAndSend(
                "/topic/location." + location.getChatroomId(),
                location
            );
            User sender = userMapper.findByEmail(senderEmail);
            String sendNickname = sender.getNickname();

            // 알림 추가 (Redis를 통한 알림)
            String message = String.format("%s님의 위치 정보가 업데이트되었습니다!", sendNickname);
            notificationService.sendNotification(
                senderEmail,
                message,
                "LOCATION_SHARE",
                location.getChatroomId(),
                0L
            );

            log.info("위치 정보 발행 완료: chatroomId={}, email={}", 
                    location.getChatroomId(), senderEmail);
        } catch (Exception e) {
            log.error("위치 정보 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    @Operation(
            summary = "최근 위치 조회",
            description = "채팅방의 모든 사용자의 최근 위치 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "위치 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/rooms/{chatroomId}/recent")
    public ResponseEntity<BaseResponse<?>> getRecentLocations(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "채팅방 ID", required = true, example = "1")
            @PathVariable Integer chatroomId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            var locations = locationService.getRecentLocations(chatroomId);
            return ResponseEntity.ok(BaseResponse.success(locations));
        } catch (Exception e) {
            log.error("위치 정보 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "특정 사용자의 마지막 위치 조회",
            description = "채팅방 내 특정 사용자의 마지막 위치 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "위치 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/rooms/{chatroomId}/users/{email}/last")
    public ResponseEntity<BaseResponse<?>> getLastLocation(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "채팅방 ID", required = true, example = "1")
            @PathVariable Integer chatroomId,
            @Parameter(description = "사용자 이메일", required = true, example = "user@example.com")
            @PathVariable String email) {
        
        String requestEmail = tokenUtils.getEmailFromAuthHeader(token);
        
        if (requestEmail == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            var location = locationService.getLastLocation(chatroomId, email);
            return ResponseEntity.ok(BaseResponse.success(location));
        } catch (Exception e) {
            log.error("위치 정보 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }
}
