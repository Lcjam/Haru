package com.example.demo.controller.board;

import com.example.demo.service.BoardService;
import com.example.demo.service.FileStorageService;
import com.example.demo.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/core/boards/{boardId}/posts/images")
@RequiredArgsConstructor
@Slf4j
public class PostImageController {

    private final FileStorageService fileStorageService;
    private final BoardService boardService;

    /**
     * 게시글 이미지 업로드
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> uploadPostImage(
            Authentication authentication,
            @PathVariable Long boardId,
            @RequestParam("image") MultipartFile image) {

        String email = authentication.getName();
        boardService.checkActiveMembership(email, boardId);

        if (image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        String fileName = image.getOriginalFilename();
        String imagePath = fileStorageService.storeBoardFile(image, boardId, "image");

        if (imagePath == null) {
            throw new IllegalArgumentException("이미지 저장에 실패했습니다.");
        }

        String imageUrl = "/api/core/boards/images/" + boardId + "/image/" +
                          imagePath.substring(imagePath.lastIndexOf("/") + 1);

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        response.put("originalFileName", fileName);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * 게시글 이미지 삭제
     */
    @DeleteMapping
    public ResponseEntity<BaseResponse<String>> deletePostImage(
            Authentication authentication,
            @PathVariable Long boardId,
            @RequestParam("imageUrl") String imageUrl) {

        String email = authentication.getName();
        boardService.checkActiveMembership(email, boardId);

        // 경로 순회 공격 차단
        if (imageUrl.contains("..")) {
            throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다.");
        }

        if (!imageUrl.startsWith("/board-files/board_" + boardId + "/")) {
            throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다.");
        }

        boolean deleted = fileStorageService.deleteBoardFile(imageUrl);
        if (!deleted) {
            throw new IllegalArgumentException("이미지 삭제에 실패했습니다.");
        }

        return ResponseEntity.ok(BaseResponse.success("이미지가 삭제되었습니다."));
    }
}
