package com.example.demo.controller.board;

import com.example.demo.dto.board.PostReactionRequest;
import com.example.demo.dto.board.PostReactionResponse;
import com.example.demo.service.PostReactionService;
import com.example.demo.util.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "게시글 반응 관리", description = "게시글 좋아요 및 반응 추가, 조회, 삭제 관련 API")
@RestController
@RequestMapping("/api/core/boards")
@RequiredArgsConstructor
public class PostReactionController {

    private final PostReactionService postReactionService;

    @Operation(
            summary = "게시글 반응 추가/변경",
            description = "게시글에 반응(좋아요, 싫어요 등)을 추가하거나 변경합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "반응 추가/변경 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 없음, 유효하지 않은 반응 타입 등",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/posts/{postId}/reactions")
    public ResponseEntity<BaseResponse<PostReactionResponse>> addReaction(
            Authentication authentication,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @RequestBody Map<String, String> body) {

        String email = authentication.getName();
        PostReactionRequest request = new PostReactionRequest(body.get("reactionType"));
        PostReactionResponse response = postReactionService.reactToPost(email, postId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "게시글 반응 삭제",
            description = "게시글에 추가한 반응을 삭제합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "반응 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "반응 없음 등",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @DeleteMapping("/posts/{postId}/reactions")
    public ResponseEntity<BaseResponse<PostReactionResponse>> deleteReaction(
            Authentication authentication,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {

        String email = authentication.getName();
        PostReactionResponse response = postReactionService.removeReaction(email, postId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "게시글 반응 조회",
            description = "게시글의 반응 통계 및 사용자의 반응을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "반응 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 없음 등",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/posts/{postId}/reactions")
    public ResponseEntity<BaseResponse<PostReactionResponse>> getPostReaction(
            Authentication authentication,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {

        String email = authentication.getName();
        PostReactionResponse response = postReactionService.getPostReaction(email, postId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "게시글 반응 목록 조회",
            description = "게시글에 반응한 사용자 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "반응 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 없음 등",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/posts/{postId}/reactions/list")
    public ResponseEntity<BaseResponse<List<PostReactionResponse>>> getPostReactions(
            Authentication authentication,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {

        String email = authentication.getName();
        List<PostReactionResponse> responses = postReactionService.getPostReactions(email, postId);
        return ResponseEntity.ok(BaseResponse.success(responses));
    }

    @Operation(
            summary = "게시글 좋아요 토글",
            description = "게시글의 좋아요를 추가하거나 취소합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 토글 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 없음 등",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{postId}/like")
    public ResponseEntity<BaseResponse<PostReactionResponse>> toggleLike(
            Authentication authentication,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {

        String email = authentication.getName();
        // reactToPost toggles: if LIKE already exists it removes it, otherwise adds it
        PostReactionResponse response = postReactionService.reactToPost(
                email, postId, new PostReactionRequest("LIKE"));
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
