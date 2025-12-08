package com.example.demo.controller.board;

import com.example.demo.dto.board.PostCreateRequest;
import com.example.demo.dto.board.PostResponse;
import com.example.demo.dto.board.PostUpdateRequest;
import com.example.demo.dto.board.PostSearchRequest;
import com.example.demo.dto.board.PagedPostResponse;
import com.example.demo.service.PostService;
import com.example.demo.util.BaseResponse;
import com.example.demo.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/boards/{boardId}/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final TokenUtils tokenUtils;

    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<BaseResponse<?>> createPost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long boardId,
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

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<BaseResponse<?>> updatePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long boardId,
            @PathVariable Long postId,
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

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<BaseResponse<String>> deletePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long boardId,
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

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<BaseResponse<?>> getPostById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long boardId,
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

    /**
     * 게시판 내 게시글 목록 조회
     */
    @GetMapping
    public ResponseEntity<BaseResponse<?>> getPostsByBoardId(
            @RequestHeader("Authorization") String token,
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

    /**
     * 게시글 검색
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<?>> searchPosts(
            @RequestHeader("Authorization") String token,
            @PathVariable Long boardId,
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

    /**
     * 게시글 검색 및 필터링
     */
    @PostMapping("/search")
    public ResponseEntity<BaseResponse<?>> searchPostsWithFilters(
            @RequestHeader("Authorization") String token,
            @PathVariable Long boardId,
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
