package com.example.demo.controller.board;

import com.example.demo.dto.board.PostCreateRequest;
import com.example.demo.dto.board.PostResponse;
import com.example.demo.dto.board.PostUpdateRequest;
import com.example.demo.dto.board.PostSearchRequest;
import com.example.demo.dto.board.PagedPostResponse;
import com.example.demo.service.PostService;
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

@Tag(name = "게시글 관리", description = "게시판 내 게시글 작성, 조회, 수정, 삭제 및 검색 관련 API")
@RestController
@RequestMapping("/api/core/boards/{boardId}/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "게시글 작성",
            description = "게시판에 새로운 게시글을 작성합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 작성 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 작성 실패 (권한 없음, 유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BaseResponse<PostResponse>> createPost(
            Authentication authentication,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 작성 요청 정보", required = true)
            @RequestBody PostCreateRequest request) {

        request.setBoardId(boardId);
        PostResponse response = postService.createPost(authentication.getName(), request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "게시글 수정",
            description = "게시글을 수정합니다. 작성자만 수정 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 수정 실패 (권한 없음, 게시글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PutMapping("/{postId}")
    public ResponseEntity<BaseResponse<PostResponse>> updatePost(
            Authentication authentication,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @Parameter(description = "게시글 수정 요청 정보", required = true)
            @RequestBody PostUpdateRequest request) {

        PostResponse response = postService.updatePost(authentication.getName(), postId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다. 작성자만 삭제 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 삭제 실패 (권한 없음, 게시글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<BaseResponse<String>> deletePost(
            Authentication authentication,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {

        postService.deletePost(authentication.getName(), postId);
        return ResponseEntity.ok(BaseResponse.success("게시글이 삭제되었습니다."));
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 게시글의 상세 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 조회 실패 (게시글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/{postId}")
    public ResponseEntity<BaseResponse<PostResponse>> getPostById(
            Authentication authentication,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {

        PostResponse response = postService.getPostById(authentication.getName(), postId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(
            summary = "게시판 내 게시글 목록 조회",
            description = "게시판에 속한 모든 게시글 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 목록 조회 실패 (게시판이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<PostResponse>>> getPostsByBoardId(
            Authentication authentication,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId) {

        List<PostResponse> posts = postService.getPostsByBoardId(authentication.getName(), boardId);
        return ResponseEntity.ok(BaseResponse.success(posts));
    }

    @Operation(
            summary = "게시글 검색 (키워드)",
            description = "키워드로 게시글을 검색합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 검색 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 검색 실패 (게시판이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<PostResponse>>> searchPosts(
            Authentication authentication,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "검색 키워드", required = true, example = "제목")
            @RequestParam String keyword) {

        List<PostResponse> posts = postService.searchPosts(authentication.getName(), boardId, keyword);
        return ResponseEntity.ok(BaseResponse.success(posts));
    }

    @Operation(
            summary = "게시글 검색 및 필터링",
            description = "다양한 필터 조건으로 게시글을 검색합니다. 페이징도 지원합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 검색 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "게시글 검색 실패 (게시판이 없음, 유효하지 않은 필터 조건 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PagedPostResponse>> searchPostsWithFilters(
            Authentication authentication,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "검색 및 필터링 요청 정보", required = true)
            @RequestBody PostSearchRequest request) {

        request.setBoardId(boardId);
        PagedPostResponse response = postService.searchPostsWithFilters(authentication.getName(), request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
