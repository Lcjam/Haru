package com.example.demo.controller;

import com.example.demo.dto.chat.ChatRoomRequest;
import com.example.demo.dto.chat.ChatRoomResponse;
import com.example.demo.util.BaseResponse;
import com.example.demo.mapper.ChatRoomMapper;
import com.example.demo.mapper.Market.ProductMapper;
import com.example.demo.mapper.Market.ProductRequestMapper;
import com.example.demo.model.chat.ChatRoom;
import com.example.demo.service.ChatService;
import com.example.demo.service.NotificationService;
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

@Tag(name = "채팅방 관리", description = "채팅방 생성, 조회 및 함께하기 승인 관련 API")
@RestController
@RequestMapping("/api/core/chat/rooms")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final TokenUtils tokenUtils;
    private final ChatRoomMapper chatRoomMapper;
    private final ProductMapper productMapper;
    private final ProductRequestMapper productRequestMapper;
    private final NotificationService notificationService;

    @Operation(
            summary = "채팅방 생성/조회",
            description = "상품에 대한 채팅방을 생성하거나 기존 채팅방을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 생성/조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "채팅방 생성/조회 실패 (상품이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<BaseResponse<?>> createChatRoom(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "채팅방 생성 요청 정보 (productId 포함)", required = true)
            @RequestBody ChatRoomRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        ChatRoomResponse response = chatService.createOrGetChatRoom(email, request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response.getMessage(), "400"));
        }
        
        // 알림 추가
        String message = String.format("\"%s\" 상품에 대한 채팅방이 생성되었습니다!", request.getProductId());
        notificationService.sendNotification(email, message, "CHAT_MESSAGE", response.getChatroomId(), request.getProductId());

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "사용자의 채팅방 목록 조회",
            description = "인증된 사용자가 참여 중인 모든 채팅방 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<BaseResponse<?>> getChatRoomsByUser(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        ChatRoomResponse response = chatService.getChatRoomsByUser(email);
        
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "활성 채팅방 목록 조회",
            description = "인증된 사용자의 모집 중이거나 승인된 채팅방 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "활성 채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/active")
    public ResponseEntity<BaseResponse<?>> getActiveChatRoomsByUser(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        ChatRoomResponse response = chatService.getActiveChatRoomsByUser(email);
        
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "채팅방 상세 정보 조회",
            description = "채팅방 ID로 채팅방의 상세 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "채팅방 조회 실패 (채팅방이 없음, 권한 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/{chatroomId}")
    public ResponseEntity<BaseResponse<?>> getChatRoomDetail(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "채팅방 ID", required = true, example = "1")
            @PathVariable Integer chatroomId) {
        
        String tokenWithoutBearer = tokenUtils.extractTokenWithoutBearer(token);
        
        if (!tokenUtils.isTokenValid(tokenWithoutBearer)) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        String email = tokenUtils.getEmailFromToken(tokenWithoutBearer);
        ChatRoomResponse response = chatService.getChatRoomDetail(email, chatroomId);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(BaseResponse.error(response.getMessage(), "400"));
        }
        
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "함께하기 요청 승인",
            description = "상품 등록자가 구매 요청을 승인합니다. 상품 등록자만 승인 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "요청 승인 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 승인 실패 (권한 없음, 요청이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (상품 등록자가 아님)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/{chatroomId}/approve")
    public ResponseEntity<BaseResponse<?>> approveChatMember(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "채팅방 ID", required = true, example = "1")
            @PathVariable Integer chatroomId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            // 채팅방 정보 조회
            ChatRoom chatRoom = chatRoomMapper.findChatRoomById(chatroomId, email);
            
            // 채팅방이 없거나 요청자가 등록자가 아닌 경우
            if (chatRoom == null) {
                return ResponseEntity.status(404).body(BaseResponse.error("채팅방을 찾을 수 없습니다.", "404"));
            }
            
            // 상품 등록자만 승인 가능
            if (!chatRoom.getSellerEmail().equals(email)) {
                return ResponseEntity.status(403).body(BaseResponse.error("상품 등록자만 승인할 수 있습니다.", "403"));
            }
            
            // ProductRequests 테이블에서 해당 요청 찾기
            Long productId = chatRoom.getProductId();
            String requesterEmail = chatRoom.getRequestEmail();
            
            // 요청 정보 조회
            Long requestId = productRequestMapper.findRequestId(productId, requesterEmail);
            
            if (requestId == null) {
                return ResponseEntity.status(404).body(BaseResponse.error("해당 요청을 찾을 수 없습니다.", "404"));
            }
            
            // 요청 승인 처리
            productMapper.updateRequestApprovalStatus(requestId, "승인");
            
            // 현재 모집 인원 증가
            productMapper.increaseCurrentParticipants(productId);
            
            // 모집 인원 충족 시 모집 마감 처리
            productMapper.updateProductVisibility(productId);

            // 알림 추가
            String message = String.format("\"%s\" 상품에 대한 함께하기 요청이 승인되었습니다!", productId);
            notificationService.sendNotification(requesterEmail, message, "CHAT_MESSAGE", chatroomId, productId);
            
            return ResponseEntity.ok(BaseResponse.success("요청이 승인되었습니다."));
        } catch (Exception e) {
            log.error("요청 승인 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("요청 처리 중 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "상품 ID로 채팅방 ID 조회",
            description = "상품 ID를 통해 해당 상품의 채팅방 ID를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 ID 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "채팅방 ID 조회 실패 (채팅방이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<BaseResponse<?>> getChatRoomIdByProductId(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "상품 ID", required = true, example = "1")
            @PathVariable Long productId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            // 상품 ID와 사용자 이메일로 채팅방 조회
            ChatRoom chatRoom = chatRoomMapper.findChatRoomByProductIdAndEmail(productId, email);
            
            if (chatRoom == null) {
                return ResponseEntity.status(404).body(BaseResponse.error("채팅방을 찾을 수 없습니다.", "404"));
            }
            
            return ResponseEntity.ok(BaseResponse.success(chatRoom.getChatroomId()));
        } catch (Exception e) {
            log.error("채팅방 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("채팅방 조회 중 오류가 발생했습니다.", "500"));
        }
    }
}
