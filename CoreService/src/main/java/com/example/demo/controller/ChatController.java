package com.example.demo.controller;

import com.example.demo.dto.chat.ChatRoomRequest;
import com.example.demo.dto.chat.ChatRoomResponse;
import com.example.demo.util.BaseResponse;
import com.example.demo.service.ChatService;
import com.example.demo.service.NotificationService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅방 관리", description = "채팅방 생성, 조회 및 함께하기 승인 관련 API")
@RestController
@RequestMapping("/api/core/chat/rooms")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final NotificationService notificationService;

    @Operation(
            summary = "채팅방 생성/조회",
            description = "상품에 대한 채팅방을 생성하거나 기존 채팅방을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 생성/조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "채팅방 생성/조회 실패 (상품이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BaseResponse<?>> createChatRoom(
            Authentication authentication,
            @Parameter(description = "채팅방 생성 요청 정보 (productId 포함)", required = true)
            @RequestBody ChatRoomRequest request) {

        String email = authentication.getName();
        ChatRoomResponse response = chatService.createOrGetChatRoom(email, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response.getMessage(), "400"));
        }

        String message = String.format("\"%s\" 상품에 대한 채팅방이 생성되었습니다!", request.getProductId());
        notificationService.sendNotification(email, message, "CHAT_MESSAGE", response.getChatroomId(), request.getProductId());

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "사용자의 채팅방 목록 조회",
            description = "인증된 사용자가 참여 중인 모든 채팅방 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping
    public ResponseEntity<BaseResponse<?>> getChatRoomsByUser(Authentication authentication) {
        ChatRoomResponse response = chatService.getChatRoomsByUser(authentication.getName());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "활성 채팅방 목록 조회",
            description = "인증된 사용자의 모집 중이거나 승인된 채팅방 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성 채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<BaseResponse<?>> getActiveChatRoomsByUser(Authentication authentication) {
        ChatRoomResponse response = chatService.getActiveChatRoomsByUser(authentication.getName());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "채팅방 상세 정보 조회",
            description = "채팅방 ID로 채팅방의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "채팅방 조회 실패 (권한 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/{chatroomId}")
    public ResponseEntity<BaseResponse<?>> getChatRoomDetail(
            Authentication authentication,
            @Parameter(description = "채팅방 ID", required = true, example = "1")
            @PathVariable Integer chatroomId) {

        ChatRoomResponse response = chatService.getChatRoomDetail(authentication.getName(), chatroomId);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response.getMessage(), "400"));
        }

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "함께하기 요청 승인",
            description = "상품 등록자가 구매 요청을 승인합니다. 상품 등록자만 승인 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 승인 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (상품 등록자가 아님)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방 또는 요청을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{chatroomId}/approve")
    public ResponseEntity<BaseResponse<?>> approveChatMember(
            Authentication authentication,
            @Parameter(description = "채팅방 ID", required = true, example = "1")
            @PathVariable Integer chatroomId) {

        String email = authentication.getName();
        ChatRoomResponse response = chatService.approveChatMember(email, chatroomId);

        String message = String.format("\"%s\" 상품에 대한 함께하기 요청이 승인되었습니다!", response.getProductId());
        notificationService.sendNotification(response.getRequestEmail(), message, "CHAT_MESSAGE",
                chatroomId, response.getProductId());

        return ResponseEntity.ok(BaseResponse.success(response.getMessage()));
    }

    @Operation(
            summary = "상품 ID로 채팅방 ID 조회",
            description = "상품 ID를 통해 해당 상품의 채팅방 ID를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 ID 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<BaseResponse<?>> getChatRoomIdByProductId(
            Authentication authentication,
            @Parameter(description = "상품 ID", required = true, example = "1")
            @PathVariable Long productId) {

        ChatRoomResponse response = chatService.getChatRoomIdByProductId(authentication.getName(), productId);
        return ResponseEntity.ok(BaseResponse.success(response.getChatroomId()));
    }
}
