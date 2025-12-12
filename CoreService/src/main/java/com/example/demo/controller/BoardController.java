package com.example.demo.controller;

import com.example.demo.dto.board.*;
import com.example.demo.model.board.BoardMember;
import com.example.demo.service.BoardService;
import com.example.demo.util.BaseResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "게시판 관리", description = "게시판 생성, 조회, 수정, 삭제 및 멤버 관리 관련 API")
@RestController
@RequestMapping("/api/core/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final TokenUtils tokenUtils;

    @Operation(
            summary = "게시판 생성",
            description = "새로운 게시판을 생성합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시판 생성 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시판 생성 실패 (유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<BaseResponse<?>> createBoard(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 생성 요청 정보", required = true)
            @RequestBody BoardCreateRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            BoardResponse response = boardService.createBoard(email, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (Exception e) {
            log.error("게시판 생성 중 오류: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage(), "400"));
        }
    }

    @Operation(
            summary = "게시판 상세 정보 조회",
            description = "게시판 ID로 게시판의 상세 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시판 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시판 조회 실패 (존재하지 않는 게시판 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<BaseResponse<?>> getBoardById(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            BoardResponse response = boardService.getBoardById(boardId, email);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("게시판 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시판 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "호스트의 게시판 목록 조회",
            description = "인증된 사용자가 호스트로 참여 중인 게시판 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시판 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/hosted")
    public ResponseEntity<BaseResponse<?>> getBoardsByHost(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            List<BoardResponse> boards = boardService.getBoardsByHost(email);
            return ResponseEntity.ok(BaseResponse.success(boards));
        } catch (Exception e) {
            log.error("호스트 게시판 목록 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "멤버로 참여 중인 게시판 목록 조회",
            description = "인증된 사용자가 멤버로 참여 중인 게시판 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시판 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/joined")
    public ResponseEntity<BaseResponse<?>> getBoardsByMember(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            List<BoardResponse> boards = boardService.getBoardsByMember(email);
            return ResponseEntity.ok(BaseResponse.success(boards));
        } catch (Exception e) {
            log.error("멤버 게시판 목록 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 정보 수정",
            description = "게시판의 정보를 수정합니다. 호스트만 수정 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시판 수정 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시판 수정 실패 (권한 없음, 유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PutMapping("/{boardId}")
    public ResponseEntity<BaseResponse<?>> updateBoard(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시판 수정 요청 정보", required = true)
            @RequestBody BoardCreateRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            BoardResponse response = boardService.updateBoard(email, boardId, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("게시판 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시판 수정 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 이미지 업로드",
            description = "게시판의 대표 이미지를 업로드합니다. 호스트만 업로드 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이미지 업로드 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미지 업로드 실패 (권한 없음, 파일 형식 오류 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping(value = "/{boardId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> uploadBoardImage(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("image") MultipartFile image) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            BoardResponse response = boardService.uploadBoardImage(email, boardId, image);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("이미지 업로드 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 멤버 초대",
            description = "게시판에 새로운 멤버를 초대합니다. 호스트만 초대 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 초대 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "멤버 초대 실패 (권한 없음, 이미 초대됨 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/{boardId}/members/invite")
    public ResponseEntity<BaseResponse<?>> inviteMember(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "멤버 초대 요청 정보 (이메일, 역할)", required = true)
            @RequestBody MemberInviteRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            // 초대 요청의 이메일과 역할을 로그로 확인
            log.info("멤버 초대 요청: boardId={}, inviteEmail={}, role={}", 
                    boardId, request.getEmail(), request.getRole());
            
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(BaseResponse.error("초대할 사용자의 이메일이 필요합니다.", "400"));
            }
            
            BoardResponse response = boardService.inviteMember(email, boardId, request.getEmail(), request.getRole());
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("멤버 초대 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("멤버 초대 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "초대 수락",
            description = "게시판 초대를 수락합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "초대 수락 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "초대 수락 실패 (초대가 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/{boardId}/members/accept")
    public ResponseEntity<BaseResponse<?>> acceptInvitation(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            BoardMember member = boardService.acceptInvitation(email, boardId);
            Map<String, Object> response = Map.of(
                "message", "초대를 수락했습니다.",
                "memberId", member.getId(),
                "status", member.getStatus(),
                "role", member.getRole()
            );
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("초대 수락 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("초대 수락 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "초대 거절",
            description = "게시판 초대를 거절합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "초대 거절 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "초대 거절 실패 (초대가 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/{boardId}/members/reject")
    public ResponseEntity<BaseResponse<String>> rejectInvitation(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            boardService.rejectInvitation(email, boardId);
            return ResponseEntity.ok(BaseResponse.success("초대를 거절했습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("초대 거절 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("초대 거절 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "멤버 추방",
            description = "게시판에서 멤버를 추방합니다. 호스트만 추방 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 추방 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "멤버 추방 실패 (권한 없음, 멤버가 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @DeleteMapping("/{boardId}/members/{memberId}")
    public ResponseEntity<BaseResponse<String>> kickMember(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "멤버 ID", required = true, example = "1")
            @PathVariable Long memberId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            boardService.kickMember(email, boardId, memberId);
            return ResponseEntity.ok(BaseResponse.success("멤버가 추방되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("멤버 추방 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("멤버 추방 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 삭제",
            description = "게시판을 삭제합니다. 호스트만 삭제 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시판 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시판 삭제 실패 (권한 없음, 게시판이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @DeleteMapping("/{boardId}")
    public ResponseEntity<BaseResponse<String>> deleteBoard(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            boardService.deleteBoard(email, boardId);
            return ResponseEntity.ok(BaseResponse.success("게시판이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("게시판 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시판 삭제 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 상태 변경",
            description = "게시판의 상태를 변경합니다. 호스트만 변경 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시판 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시판 상태 변경 실패 (권한 없음, 유효하지 않은 상태 값 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PutMapping("/{boardId}/status")
    public ResponseEntity<BaseResponse<?>> updateBoardStatus(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "상태 변경 요청 정보 (status 필드 포함)", required = true)
            @RequestBody Map<String, String> request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        String status = request.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(BaseResponse.error("상태 값이 필요합니다.", "400"));
        }
        
        try {
            BoardResponse response = boardService.updateBoardStatus(email, boardId, status);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("게시판 상태 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시판 상태 변경 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 호스트 변경",
            description = "게시판의 호스트를 변경합니다. 현재 호스트만 변경 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "호스트 변경 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "호스트 변경 실패 (권한 없음, 새 호스트가 멤버가 아님 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PutMapping("/{boardId}/host")
    public ResponseEntity<BaseResponse<?>> changeHost(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "호스트 변경 요청 정보 (newHostEmail 필드 포함)", required = true)
            @RequestBody Map<String, String> request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        String newHostEmail = request.get("newHostEmail");
        if (newHostEmail == null) {
            return ResponseEntity.badRequest().body(BaseResponse.error("새 호스트 이메일이 필요합니다.", "400"));
        }
        
        try {
            BoardResponse response = boardService.changeHost(email, boardId, newHostEmail);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("호스트 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("호스트 변경 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 멤버 목록 조회",
            description = "게시판의 멤버 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "멤버 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "멤버 목록 조회 실패 (게시판이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/{boardId}/members")
    public ResponseEntity<BaseResponse<?>> getBoardMembers(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            List<BoardMember> members = boardService.getBoardMembers(email, boardId);
            return ResponseEntity.ok(BaseResponse.success(members));
        } catch (IllegalArgumentException e) {
            log.warn("게시판 멤버 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시판 멤버 목록 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }
}
