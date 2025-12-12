package com.example.demo.service;

import com.example.demo.dto.board.CommentCreateRequest;
import com.example.demo.dto.board.CommentUpdateRequest;
import com.example.demo.mapper.board.BoardMapper;
import com.example.demo.mapper.board.BoardMemberMapper;
import com.example.demo.mapper.board.CommentMapper;
import com.example.demo.mapper.board.PostMapper;
import com.example.demo.model.board.Board;
import com.example.demo.model.board.BoardMember;
import com.example.demo.model.board.Comment;
import com.example.demo.model.board.Post;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private BoardMemberMapper boardMemberMapper;

    @InjectMocks
    private CommentService commentService;

    private Board board;
    private BoardMember boardMember;
    private Post post;
    private Comment comment;
    private Comment parentComment;
    private CommentCreateRequest commentCreateRequest;
    private CommentUpdateRequest commentUpdateRequest;

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
                .authorEmail("author@example.com")
                .title("테스트 게시글")
                .content("테스트 내용")
                .build();

        comment = Comment.builder()
                .id(1L)
                .postId(1L)
                .parentId(null)
                .authorEmail("test@example.com")
                .content("테스트 댓글")
                .replyCount(0)
                .depth(0)
                .build();

        parentComment = Comment.builder()
                .id(2L)
                .postId(1L)
                .parentId(null)
                .authorEmail("other@example.com")
                .content("부모 댓글")
                .replyCount(0)
                .depth(0)
                .build();

        commentCreateRequest = new CommentCreateRequest();
        commentCreateRequest.setPostId(1L);
        commentCreateRequest.setContent("테스트 댓글");

        commentUpdateRequest = new CommentUpdateRequest();
        commentUpdateRequest.setContent("수정된 댓글");
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_Success() {
        // given
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        doNothing().when(commentMapper).createComment(any(Comment.class));
        when(commentMapper.countCommentsByPostId(1L)).thenReturn(1);
        doNothing().when(commentMapper).updatePostCommentCount(1L, 1);
        when(commentMapper.getCommentById(anyLong())).thenReturn(comment);

        // when
        var result = commentService.createComment("test@example.com", commentCreateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("테스트 댓글");
        assertThat(result.getAuthorEmail()).isEqualTo("test@example.com");
        verify(commentMapper, times(1)).createComment(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 게시글이 없음")
    void createComment_Fail_PostNotFound() {
        // given
        when(postMapper.getPostById(999L)).thenReturn(null);
        commentCreateRequest.setPostId(999L);

        // when & then
        assertThatThrownBy(() -> commentService.createComment("test@example.com", commentCreateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
        verify(commentMapper, never()).createComment(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 멤버가 아님")
    void createComment_Fail_NotMember() {
        // given
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> commentService.createComment("test@example.com", commentCreateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("멤버만 댓글을 작성할 수 있습니다");
        verify(commentMapper, never()).createComment(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 생성 성공")
    void createReply_Success() {
        // given
        commentCreateRequest.setParentId(2L);
        Comment reply = Comment.builder()
                .id(3L)
                .postId(1L)
                .parentId(2L)
                .authorEmail("test@example.com")
                .content("대댓글")
                .build();

        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        when(commentMapper.getCommentById(2L)).thenReturn(parentComment);
        doNothing().when(commentMapper).createComment(any(Comment.class));
        when(commentMapper.countRepliesByParentId(2L)).thenReturn(1);
        doNothing().when(commentMapper).updateReplyCount(2L, 1);
        when(commentMapper.countCommentsByPostId(1L)).thenReturn(2);
        doNothing().when(commentMapper).updatePostCommentCount(1L, 2);
        when(commentMapper.getCommentById(anyLong())).thenReturn(reply);

        // when
        var result = commentService.createComment("test@example.com", commentCreateRequest);

        // then
        assertThat(result).isNotNull();
        verify(commentMapper, times(1)).createComment(any(Comment.class));
        verify(commentMapper, times(1)).updateReplyCount(2L, 1);
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 부모 댓글이 없음")
    void createReply_Fail_ParentNotFound() {
        // given
        commentCreateRequest.setParentId(999L);
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        when(commentMapper.getCommentById(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> commentService.createComment("test@example.com", commentCreateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부모 댓글을 찾을 수 없습니다");
        verify(commentMapper, never()).createComment(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        when(commentMapper.getCommentById(1L)).thenReturn(comment);
        doNothing().when(commentMapper).updateComment(any(Comment.class));
        Comment updatedComment = Comment.builder()
                .id(1L)
                .content("수정된 댓글")
                .build();
        when(commentMapper.getCommentById(1L)).thenReturn(updatedComment);

        // when
        var result = commentService.updateComment("test@example.com", 1L, commentUpdateRequest);

        // then
        assertThat(result).isNotNull();
        verify(commentMapper, times(1)).updateComment(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자가 아님")
    void updateComment_Fail_NotAuthor() {
        // given
        Comment otherComment = Comment.builder()
                .id(1L)
                .authorEmail("other@example.com")
                .build();
        when(commentMapper.getCommentById(1L)).thenReturn(otherComment);

        // when & then
        assertThatThrownBy(() -> commentService.updateComment("test@example.com", 1L, commentUpdateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("작성자만 수정할 수 있습니다");
        verify(commentMapper, never()).updateComment(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        when(commentMapper.getCommentById(1L)).thenReturn(comment);
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        doNothing().when(commentMapper).deleteComment(1L, "test@example.com");
        when(commentMapper.countCommentsByPostId(1L)).thenReturn(0);
        doNothing().when(commentMapper).updatePostCommentCount(1L, 0);

        // when
        commentService.deleteComment("test@example.com", 1L);

        // then
        verify(commentMapper, times(1)).deleteComment(1L, "test@example.com");
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void deleteComment_Fail_NoPermission() {
        // given
        Comment otherComment = Comment.builder()
                .id(1L)
                .postId(1L)
                .authorEmail("other@example.com")
                .build();
        Board otherBoard = Board.builder()
                .id(1L)
                .hostEmail("other@example.com")
                .build();
        when(commentMapper.getCommentById(1L)).thenReturn(otherComment);
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(otherBoard);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment("test@example.com", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("삭제할 수 있습니다");
        verify(commentMapper, never()).deleteComment(anyLong(), anyString());
    }

    @Test
    @DisplayName("게시글의 댓글 목록 조회 성공")
    void getCommentsByPostId_Success() {
        // given
        List<Comment> comments = Arrays.asList(comment);
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        when(commentMapper.getCommentsByPostId(1L)).thenReturn(comments);
        when(commentMapper.getRepliesByParentId(1L)).thenReturn(Collections.emptyList());

        // when
        var result = commentService.getCommentsByPostId("test@example.com", 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("테스트 댓글");
        verify(commentMapper, times(1)).getCommentsByPostId(1L);
    }

    @Test
    @DisplayName("대댓글 목록 조회 성공")
    void getRepliesByParentId_Success() {
        // given
        Comment reply = Comment.builder()
                .id(3L)
                .postId(1L)
                .parentId(2L)
                .authorEmail("test@example.com")
                .content("대댓글")
                .build();
        List<Comment> replies = Arrays.asList(reply);

        when(commentMapper.getCommentById(2L)).thenReturn(parentComment);
        when(postMapper.getPostById(1L)).thenReturn(post);
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findBoardMemberByEmailAndBoardId("test@example.com", 1L))
                .thenReturn(boardMember);
        when(commentMapper.getRepliesByParentId(2L)).thenReturn(replies);

        // when
        var result = commentService.getRepliesByParentId("test@example.com", 2L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(commentMapper, times(1)).getRepliesByParentId(2L);
    }
}
