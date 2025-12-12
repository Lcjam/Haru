package com.example.demo.controller.board;

import com.example.demo.dto.board.CommentCreateRequest;
import com.example.demo.dto.board.CommentResponse;
import com.example.demo.dto.board.CommentUpdateRequest;
import com.example.demo.service.CommentService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글 관리", description = "게시글 댓글 작성, 조회, 수정, 삭제 관련 API")
@RestController
@RequestMapping("/api/core/boards")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final TokenUtils tokenUtils;

    @Operation(
            summary = "댓글 생성",
            description = "게시글에 댓글을 작성합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "댓글 작성 실패 (게시글이 없음, 유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<BaseResponse<?>> createComment(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @Parameter(description = "댓글 작성 요청 정보", required = true)
            @RequestBody CommentCreateRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            // postId 설정
            request.setPostId(postId);
            
            CommentResponse response = commentService.createComment(email, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("댓글 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("댓글 작성 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "대댓글 생성",
            description = "댓글에 대댓글을 작성합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "대댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "대댓글 작성 실패 (댓글이 없음, 유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/posts/{postId}/comments/{commentId}/replies")
    public ResponseEntity<BaseResponse<?>> createReply(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true, example = "1")
            @PathVariable Long commentId,
            @Parameter(description = "대댓글 작성 요청 정보", required = true)
            @RequestBody CommentCreateRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            // postId와 parentId 설정
            request.setPostId(postId);
            request.setParentId(commentId);
            
            CommentResponse response = commentService.createComment(email, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("대댓글 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("대댓글 작성 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "댓글 수정",
            description = "댓글을 수정합니다. 작성자만 수정 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "댓글 수정 실패 (권한 없음, 댓글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<BaseResponse<?>> updateComment(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true, example = "1")
            @PathVariable Long commentId,
            @Parameter(description = "댓글 수정 요청 정보", required = true)
            @RequestBody CommentUpdateRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            CommentResponse response = commentService.updateComment(email, commentId, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("댓글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("댓글 수정 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다. 작성자만 삭제 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "댓글 삭제 실패 (권한 없음, 댓글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<BaseResponse<String>> deleteComment(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true, example = "1")
            @PathVariable Long commentId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            commentService.deleteComment(email, commentId);
            return ResponseEntity.ok(BaseResponse.success("댓글이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("댓글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("댓글 삭제 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시글의 댓글 목록 조회",
            description = "게시글에 작성된 모든 댓글 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "댓글 목록 조회 실패 (게시글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<BaseResponse<?>> getCommentsByPostId(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            List<CommentResponse> comments = commentService.getCommentsByPostId(email, postId);
            return ResponseEntity.ok(BaseResponse.success(comments));
        } catch (IllegalArgumentException e) {
            log.warn("댓글 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("댓글 목록 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "대댓글 목록 조회",
            description = "댓글에 작성된 모든 대댓글 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "대댓글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "대댓글 목록 조회 실패 (댓글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/posts/{postId}/comments/{commentId}/replies")
    public ResponseEntity<BaseResponse<?>> getRepliesByParentId(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true, example = "1")
            @PathVariable Long commentId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            List<CommentResponse> replies = commentService.getRepliesByParentId(email, commentId);
            return ResponseEntity.ok(BaseResponse.success(replies));
        } catch (IllegalArgumentException e) {
            log.warn("대댓글 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("대댓글 목록 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }
}
