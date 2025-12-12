package com.example.demo.controller.board;

import com.example.demo.dto.board.PostCreateRequest;
import com.example.demo.dto.board.PostResponse;
import com.example.demo.dto.board.PostUpdateRequest;
import com.example.demo.dto.board.PostSearchRequest;
import com.example.demo.dto.board.PagedPostResponse;
import com.example.demo.service.PostService;
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

@Tag(name = "게시글 관리", description = "게시판 내 게시글 작성, 조회, 수정, 삭제 및 검색 관련 API")
@RestController
@RequestMapping("/api/core/boards/{boardId}/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final TokenUtils tokenUtils;

    @Operation(
            summary = "게시글 작성",
            description = "게시판에 새로운 게시글을 작성합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 작성 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 작성 실패 (권한 없음, 유효성 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<BaseResponse<?>> createPost(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 작성 요청 정보", required = true)
            @RequestBody PostCreateRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            // boardId 설정
            request.setBoardId(boardId);
            
            PostResponse response = postService.createPost(email, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("게시글 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시글 작성 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시글 수정",
            description = "게시글을 수정합니다. 작성자만 수정 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 수정 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 수정 실패 (권한 없음, 게시글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PutMapping("/{postId}")
    public ResponseEntity<BaseResponse<?>> updatePost(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId,
            @Parameter(description = "게시글 수정 요청 정보", required = true)
            @RequestBody PostUpdateRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            PostResponse response = postService.updatePost(email, postId, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("게시글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시글 수정 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다. 작성자만 삭제 가능합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 삭제 실패 (권한 없음, 게시글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<BaseResponse<String>> deletePost(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            postService.deletePost(email, postId);
            return ResponseEntity.ok(BaseResponse.success("게시글이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("게시글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시글 삭제 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 게시글의 상세 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 조회 실패 (게시글이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/{postId}")
    public ResponseEntity<BaseResponse<?>> getPostById(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "게시글 ID", required = true, example = "1")
            @PathVariable Long postId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            PostResponse response = postService.getPostById(email, postId);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("게시글 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시글 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시판 내 게시글 목록 조회",
            description = "게시판에 속한 모든 게시글 목록을 조회합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 목록 조회 실패 (게시판이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<BaseResponse<?>> getPostsByBoardId(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            List<PostResponse> posts = postService.getPostsByBoardId(email, boardId);
            return ResponseEntity.ok(BaseResponse.success(posts));
        } catch (IllegalArgumentException e) {
            log.warn("게시글 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시글 목록 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시글 검색 (키워드)",
            description = "키워드로 게시글을 검색합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 검색 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 검색 실패 (게시판이 없음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<?>> searchPosts(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "검색 키워드", required = true, example = "제목")
            @RequestParam String keyword) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            List<PostResponse> posts = postService.searchPosts(email, boardId, keyword);
            return ResponseEntity.ok(BaseResponse.success(posts));
        } catch (IllegalArgumentException e) {
            log.warn("게시글 검색 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시글 검색 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }

    @Operation(
            summary = "게시글 검색 및 필터링",
            description = "다양한 필터 조건으로 게시글을 검색합니다. 페이징도 지원합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 검색 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "게시글 검색 실패 (게시판이 없음, 유효하지 않은 필터 조건 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/search")
    public ResponseEntity<BaseResponse<?>> searchPostsWithFilters(
            @Parameter(description = "JWT 토큰 (Bearer {token} 형식)", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "게시판 ID", required = true, example = "1")
            @PathVariable Long boardId,
            @Parameter(description = "검색 및 필터링 요청 정보", required = true)
            @RequestBody PostSearchRequest request) {
        
        String email = tokenUtils.getEmailFromAuthHeader(token);
        
        if (email == null) {
            return ResponseEntity.status(401).body(BaseResponse.error("인증되지 않은 요청입니다.", "401"));
        }
        
        try {
            // 요청에 게시판 ID 설정
            request.setBoardId(boardId);
            
            PagedPostResponse response = postService.searchPostsWithFilters(email, request);
            return ResponseEntity.ok(BaseResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("게시글 검색 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.error(e.getMessage(), "400"));
        } catch (Exception e) {
            log.error("게시글 검색 중 오류: {}", e.getMessage());
            return ResponseEntity.status(500).body(BaseResponse.error("서버 오류가 발생했습니다.", "500"));
        }
    }
}
