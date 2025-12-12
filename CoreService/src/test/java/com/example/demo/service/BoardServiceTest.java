package com.example.demo.service;

import com.example.demo.dto.board.BoardCreateRequest;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.board.BoardMapper;
import com.example.demo.mapper.board.BoardMemberMapper;
import com.example.demo.model.User;
import com.example.demo.model.board.Board;
import com.example.demo.model.board.BoardMember;
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
@DisplayName("BoardService 테스트")
class BoardServiceTest {

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private BoardMemberMapper boardMemberMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private BoardService boardService;

    private User user;
    private Board board;
    private BoardMember boardMember;
    private BoardCreateRequest boardCreateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testuser")
                .build();

        board = Board.builder()
                .id(1L)
                .name("테스트 게시판")
                .description("테스트 설명")
                .hostEmail("test@example.com")
                .status("ACTIVE")
                .memberCount(1)
                .build();

        boardMember = BoardMember.builder()
                .id(1L)
                .boardId(1L)
                .userEmail("test@example.com")
                .role("HOST")
                .status("ACTIVE")
                .build();

        boardCreateRequest = new BoardCreateRequest();
        boardCreateRequest.setName("테스트 게시판");
        boardCreateRequest.setDescription("테스트 설명");
    }

    @Test
    @DisplayName("게시판 생성 성공")
    void createBoard_Success() {
        // given
        when(userMapper.findByEmail("test@example.com")).thenReturn(user);
        when(boardMapper.findBoardById(anyLong())).thenReturn(board);
        doNothing().when(boardMapper).createBoard(any(Board.class));
        doNothing().when(boardMemberMapper).addMember(any(BoardMember.class));

        // when
        var result = boardService.createBoard("test@example.com", boardCreateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트 게시판");
        assertThat(result.getHostEmail()).isEqualTo("test@example.com");
        verify(userMapper, times(1)).findByEmail("test@example.com");
        verify(boardMapper, times(1)).createBoard(any(Board.class));
        verify(boardMemberMapper, times(1)).addMember(any(BoardMember.class));
    }

    @Test
    @DisplayName("게시판 생성 실패 - 사용자를 찾을 수 없음")
    void createBoard_Fail_UserNotFound() {
        // given
        when(userMapper.findByEmail("test@example.com")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> boardService.createBoard("test@example.com", boardCreateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
        verify(userMapper, times(1)).findByEmail("test@example.com");
        verify(boardMapper, never()).createBoard(any(Board.class));
    }

    @Test
    @DisplayName("게시판 상세 정보 조회 성공")
    void getBoardById_Success() {
        // given
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, "test@example.com"))
                .thenReturn(boardMember);

        // when
        var result = boardService.getBoardById(1L, "test@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 게시판");
        verify(boardMapper, times(1)).findBoardById(1L);
        verify(boardMemberMapper, times(1))
                .findMemberByBoardIdAndUserEmail(1L, "test@example.com");
    }

    @Test
    @DisplayName("게시판 상세 정보 조회 실패 - 게시판이 없음")
    void getBoardById_Fail_BoardNotFound() {
        // given
        when(boardMapper.findBoardById(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> boardService.getBoardById(999L, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 게시판");
        verify(boardMapper, times(1)).findBoardById(999L);
    }

    @Test
    @DisplayName("게시판 상세 정보 조회 실패 - 접근 권한 없음")
    void getBoardById_Fail_NoAccess() {
        // given
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, "test@example.com"))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> boardService.getBoardById(1L, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("접근 권한이 없습니다");
        verify(boardMapper, times(1)).findBoardById(1L);
        verify(boardMemberMapper, times(1))
                .findMemberByBoardIdAndUserEmail(1L, "test@example.com");
    }

    @Test
    @DisplayName("호스트의 게시판 목록 조회 성공")
    void getBoardsByHost_Success() {
        // given
        List<Board> boards = Arrays.asList(board);
        when(boardMapper.findBoardsByHostEmail("test@example.com")).thenReturn(boards);

        // when
        var result = boardService.getBoardsByHost("test@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("테스트 게시판");
        verify(boardMapper, times(1)).findBoardsByHostEmail("test@example.com");
    }

    @Test
    @DisplayName("멤버인 게시판 목록 조회 성공")
    void getBoardsByMember_Success() {
        // given
        List<Board> boards = Arrays.asList(board);
        when(boardMapper.findBoardsByMemberEmail("test@example.com")).thenReturn(boards);

        // when
        var result = boardService.getBoardsByMember("test@example.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(boardMapper, times(1)).findBoardsByMemberEmail("test@example.com");
    }

    @Test
    @DisplayName("게시판 정보 수정 성공")
    void updateBoard_Success() {
        // given
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        doNothing().when(boardMapper).updateBoard(any(Board.class));
        when(boardMapper.findBoardById(1L)).thenReturn(board);

        // when
        var result = boardService.updateBoard("test@example.com", 1L, boardCreateRequest);

        // then
        assertThat(result).isNotNull();
        verify(boardMapper, times(2)).findBoardById(1L);
        verify(boardMapper, times(1)).updateBoard(any(Board.class));
    }

    @Test
    @DisplayName("게시판 정보 수정 실패 - 권한 없음")
    void updateBoard_Fail_NoPermission() {
        // given
        Board otherBoard = Board.builder()
                .id(1L)
                .hostEmail("other@example.com")
                .build();
        when(boardMapper.findBoardById(1L)).thenReturn(otherBoard);

        // when & then
        assertThatThrownBy(() -> boardService.updateBoard("test@example.com", 1L, boardCreateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수정 권한이 없습니다");
        verify(boardMapper, times(1)).findBoardById(1L);
        verify(boardMapper, never()).updateBoard(any(Board.class));
    }

    @Test
    @DisplayName("멤버 초대 성공")
    void inviteMember_Success() {
        // given
        String inviteEmail = "invite@example.com";
        User inviteUser = User.builder()
                .email(inviteEmail)
                .build();
        BoardMember inviterMember = BoardMember.builder()
                .role("HOST")
                .build();

        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(userMapper.findByEmail(inviteEmail)).thenReturn(inviteUser);
        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, "test@example.com"))
                .thenReturn(inviterMember);
        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, inviteEmail))
                .thenReturn(null);
        doNothing().when(boardMemberMapper).addMember(any(BoardMember.class));

        // when
        var result = boardService.inviteMember("test@example.com", 1L, inviteEmail, "MEMBER");

        // then
        assertThat(result).isNotNull();
        verify(userMapper, times(2)).findByEmail(anyString());
        verify(boardMemberMapper, times(1)).addMember(any(BoardMember.class));
    }

    @Test
    @DisplayName("멤버 초대 실패 - 이미 멤버임")
    void inviteMember_Fail_AlreadyMember() {
        // given
        String inviteEmail = "invite@example.com";
        User inviteUser = User.builder()
                .email(inviteEmail)
                .build();
        BoardMember inviterMember = BoardMember.builder()
                .role("HOST")
                .build();
        BoardMember existingMember = BoardMember.builder()
                .status("ACTIVE")
                .build();

        when(boardMapper.findBoardById(1L)).thenReturn(board);
        when(userMapper.findByEmail(inviteEmail)).thenReturn(inviteUser);
        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, "test@example.com"))
                .thenReturn(inviterMember);
        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, inviteEmail))
                .thenReturn(existingMember);

        // when & then
        assertThatThrownBy(() -> boardService.inviteMember("test@example.com", 1L, inviteEmail, "MEMBER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 게시판 멤버입니다");
        verify(boardMemberMapper, never()).addMember(any(BoardMember.class));
    }

    @Test
    @DisplayName("초대 수락 성공")
    void acceptInvitation_Success() {
        // given
        BoardMember pendingMember = BoardMember.builder()
                .id(1L)
                .status("PENDING")
                .build();
        BoardMember activeMember = BoardMember.builder()
                .id(1L)
                .status("ACTIVE")
                .build();

        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, "test@example.com"))
                .thenReturn(pendingMember);
        doNothing().when(boardMemberMapper).updateMemberStatus(1L, "ACTIVE");
        when(boardMemberMapper.findMemberById(1L)).thenReturn(activeMember);

        // when
        var result = boardService.acceptInvitation("test@example.com", 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(boardMemberMapper, times(1)).updateMemberStatus(1L, "ACTIVE");
    }

    @Test
    @DisplayName("초대 거절 성공")
    void rejectInvitation_Success() {
        // given
        BoardMember pendingMember = BoardMember.builder()
                .id(1L)
                .status("PENDING")
                .build();

        when(boardMemberMapper.findMemberByBoardIdAndUserEmail(1L, "test@example.com"))
                .thenReturn(pendingMember);
        doNothing().when(boardMemberMapper).deleteMember(1L);

        // when
        boardService.rejectInvitation("test@example.com", 1L);

        // then
        verify(boardMemberMapper, times(1)).deleteMember(1L);
    }

    @Test
    @DisplayName("게시판 삭제 성공")
    void deleteBoard_Success() {
        // given
        when(boardMapper.findBoardById(1L)).thenReturn(board);
        doNothing().when(boardMapper).deleteBoard(1L);

        // when
        boardService.deleteBoard("test@example.com", 1L);

        // then
        verify(boardMapper, times(1)).deleteBoard(1L);
    }

    @Test
    @DisplayName("게시판 삭제 실패 - 권한 없음")
    void deleteBoard_Fail_NoPermission() {
        // given
        Board otherBoard = Board.builder()
                .id(1L)
                .hostEmail("other@example.com")
                .build();
        when(boardMapper.findBoardById(1L)).thenReturn(otherBoard);

        // when & then
        assertThatThrownBy(() -> boardService.deleteBoard("test@example.com", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("삭제 권한이 없습니다");
        verify(boardMapper, never()).deleteBoard(anyLong());
    }
}
