package com.example.demo.service;

import com.example.demo.dto.board.PostCreateRequest;
import com.example.demo.dto.board.PostUpdateRequest;
import com.example.demo.mapper.board.BoardMapper;
import com.example.demo.mapper.board.BoardMemberMapper;
import com.example.demo.mapper.board.PostMapper;
import com.example.demo.model.board.Board;
import com.example.demo.model.board.BoardMember;
import com.example.demo.model.board.Post;
import com.example.demo.model.board.PostImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 테스트")
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private BoardMemberMapper boardMemberMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PostService postService;

    private Board board;
    private BoardMember boardMember;
    private Post post;
    private PostCreateRequest postCreateRequest;
    private PostUpdateRequest postUpdateRequest;

    @BeforeEach
    void setUp() {
        board = Board.builder()
                .id(1L)
                .name("테스트 게시판")
                .hostEmail("test@example.com")
                .status("ACTIVE")
                .build();

        boardMember = BoardMember.builder()
                .id(1L)
                .boardId(1L)
                .userEmail("test@example.com")
                .role("MEMBER")
                .status("ACTIVE")
                .build();

        post = Post.builder()
                .id(1L)
                .boardId(1L)
                .authorEmail("test@example.com")
                .title("테스트 게시글")
                .content("테스트 내용")
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .build();

        postCreateRequest = new PostCreateRequest();
        postCreateRequest.setBoardId(1L);
        postCreateRequest.setTitle("테스트 게시글");
        postCreateRequest.setContent("테스트 내용");

        postUpdateRequest = new PostUpdateRequest();
        postUpdateRequest.setTitle("수정된 제목");
        postUpdateRequest.setContent("수정된 내용");
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_Success() {
        // given
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        doNothing().when(postMapper).createPost(any(Post.class));
        when(postMapper.getPostById(anyLong())).thenReturn(post);
        when(postMapper.getPostImagesByPostId(anyLong())).thenReturn(Collections.emptyList());

        // when
        var result = postService.createPost("test@example.com", postCreateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 게시글");
        assertThat(result.getAuthorEmail()).isEqualTo("test@example.com");
        verify(boardMapper, times(1)).findBoardById(1L);
        verify(postMapper, times(1)).createPost(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 게시판이 없음")
    void createPost_Fail_BoardNotFound() {
        // given
        when(boardMapper.findBoardById(999L)).thenReturn(null);

        // when & then
        postCreateRequest.setBoardId(999L);
        assertThatThrownBy(() -> postService.createPost("test@example.com", postCreateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시판을 찾을 수 없습니다");
        verify(postMapper, never()).createPost(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 멤버가 아님")
    void createPost_Fail_NotMember() {
        // given
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> postService.createPost("test@example.com", postCreateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("멤버만 게시글을 작성할 수 있습니다");
        verify(postMapper, never()).createPost(any(Post.class));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() {
        // given
        when(postMapper.getPostById(1L)).thenReturn(post);
        doNothing().when(postMapper).updatePost(any(Post.class));
        doNothing().when(postMapper).deletePostImages(1L);
        when(postMapper.getPostImagesByPostId(1L)).thenReturn(Collections.emptyList());

        Post updatedPost = Post.builder()
                .id(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .build();
        when(postMapper.getPostById(1L)).thenReturn(updatedPost);

        // when
        var result = postService.updatePost("test@example.com", 1L, postUpdateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        verify(postMapper, times(1)).updatePost(any(Post.class));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아님")
    void updatePost_Fail_NotAuthor() {
        // given
        Post otherPost = Post.builder()
                .id(1L)
                .authorEmail("other@example.com")
                .build();
        when(postMapper.getPostById(1L)).thenReturn(otherPost);

        // when & then
        assertThatThrownBy(() -> postService.updatePost("test@example.com", 1L, postUpdateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("작성자만 수정할 수 있습니다");
        verify(postMapper, never()).updatePost(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() {
        // given
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        doNothing().when(postMapper).deletePost(1L, "test@example.com");
        doNothing().when(postMapper).deletePostImages(1L);

        // when
        postService.deletePost("test@example.com", 1L);

        // then
        verify(postMapper, times(1)).deletePost(1L, "test@example.com");
        verify(postMapper, times(1)).deletePostImages(1L);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글이 없음")
    void deletePost_Fail_PostNotFound() {
        // given
        when(postMapper.getPostById(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> postService.deletePost("test@example.com", 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
        verify(postMapper, never()).deletePost(anyLong(), anyString());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getPostById_Success() {
        // given
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        when(postMapper.getPostImagesByPostId(1L)).thenReturn(Collections.emptyList());
        doNothing().when(postMapper).increaseViewCount(1L);

        // when
        var result = postService.getPostById("test@example.com", 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 게시글");
        verify(postMapper, times(1)).increaseViewCount(1L);
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 게시글이 없음")
    void getPostById_Fail_PostNotFound() {
        // given
        when(postMapper.getPostById(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> postService.getPostById("test@example.com", 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("게시판 내 게시글 목록 조회 성공")
    void getPostsByBoardId_Success() {
        // given
        List<Post> posts = Arrays.asList(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        when(postMapper.getPostsByBoardId(1L)).thenReturn(posts);
        when(postMapper.getPostImagesByPostId(1L)).thenReturn(Collections.emptyList());

        // when
        var result = postService.getPostsByBoardId("test@example.com", 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 게시글");
        verify(postMapper, times(1)).getPostsByBoardId(1L);
    }

    @Test
    @DisplayName("게시글 검색 성공")
    void searchPosts_Success() {
        // given
        List<Post> posts = Arrays.asList(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        when(postMapper.searchPosts(1L, "테스트")).thenReturn(posts);
        when(postMapper.getPostImagesByPostId(1L)).thenReturn(Collections.emptyList());

        // when
        var result = postService.searchPosts("test@example.com", 1L, "테스트");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(postMapper, times(1)).searchPosts(1L, "테스트");
    }

    @Test
    @DisplayName("게시글 검색 실패 - 멤버가 아님")
    void searchPosts_Fail_NotMember() {
        // given
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> postService.searchPosts("test@example.com", 1L, "테스트"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("멤버만 게시글을 검색할 수 있습니다");
        verify(postMapper, never()).searchPosts(anyLong(), anyString());
    }
}
