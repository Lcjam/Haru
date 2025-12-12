package com.example.demo.controller;

import com.example.demo.dto.board.BoardCreateRequest;
import com.example.demo.dto.board.BoardResponse;
import com.example.demo.service.BoardService;
import com.example.demo.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@DisplayName("BoardController 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private TokenUtils tokenUtils;

    private BoardCreateRequest boardCreateRequest;
    private BoardResponse boardResponse;
    private List<BoardResponse> boardResponses;
    private String validToken = "Bearer testToken";

    @BeforeEach
    void setUp() {
        boardCreateRequest = new BoardCreateRequest();
        boardCreateRequest.setName("테스트 게시판");
        boardCreateRequest.setDescription("테스트 설명");

        boardResponse = BoardResponse.builder()
                .id(1L)
                .name("테스트 게시판")
                .description("테스트 설명")
                .hostEmail("test@example.com")
                .hostName("testuser")
                .memberCount(1)
                .status("ACTIVE")
                .build();

        boardResponses = Arrays.asList(boardResponse);
    }

    @Test
    @DisplayName("게시판 생성 성공 - 200 OK")
    void createBoard_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.createBoard("test@example.com", boardCreateRequest))
                .thenReturn(boardResponse);

        // when & then
        mockMvc.perform(post("/api/core/boards")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("테스트 게시판"));
        verify(boardService, times(1)).createBoard("test@example.com", boardCreateRequest);
    }

    @Test
    @DisplayName("게시판 생성 실패 - 인증 실패 - 401 Unauthorized")
    void createBoard_Fail_Unauthorized() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/core/boards")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardCreateRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
        verify(boardService, never()).createBoard(anyString(), any());
    }

    @Test
    @DisplayName("게시판 상세 정보 조회 성공 - 200 OK")
    void getBoardById_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.getBoardById(1L, "test@example.com")).thenReturn(boardResponse);

        // when & then
        mockMvc.perform(get("/api/core/boards/1")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1L));
        verify(boardService, times(1)).getBoardById(1L, "test@example.com");
    }

    @Test
    @DisplayName("게시판 상세 정보 조회 실패 - 게시판이 없음 - 400 Bad Request")
    void getBoardById_Fail_BoardNotFound() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.getBoardById(999L, "test@example.com"))
                .thenThrow(new IllegalArgumentException("존재하지 않는 게시판입니다."));

        // when & then
        mockMvc.perform(get("/api/core/boards/999")
                        .header("Authorization", validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("호스트의 게시판 목록 조회 성공 - 200 OK")
    void getBoardsByHost_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.getBoardsByHost("test@example.com")).thenReturn(boardResponses);

        // when & then
        mockMvc.perform(get("/api/core/boards/hosted")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
        verify(boardService, times(1)).getBoardsByHost("test@example.com");
    }

    @Test
    @DisplayName("멤버로 참여 중인 게시판 목록 조회 성공 - 200 OK")
    void getBoardsByMember_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.getBoardsByMember("test@example.com")).thenReturn(boardResponses);

        // when & then
        mockMvc.perform(get("/api/core/boards/joined")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
        verify(boardService, times(1)).getBoardsByMember("test@example.com");
    }

    @Test
    @DisplayName("게시판 정보 수정 성공 - 200 OK")
    void updateBoard_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.updateBoard("test@example.com", 1L, boardCreateRequest))
                .thenReturn(boardResponse);

        // when & then
        mockMvc.perform(put("/api/core/boards/1")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        verify(boardService, times(1)).updateBoard("test@example.com", 1L, boardCreateRequest);
    }

    @Test
    @DisplayName("게시판 정보 수정 실패 - 권한 없음 - 400 Bad Request")
    void updateBoard_Fail_NoPermission() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.updateBoard("test@example.com", 1L, boardCreateRequest))
                .thenThrow(new IllegalArgumentException("게시판 수정 권한이 없습니다."));

        // when & then
        mockMvc.perform(put("/api/core/boards/1")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("게시판 삭제 성공 - 200 OK")
    void deleteBoard_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        doNothing().when(boardService).deleteBoard("test@example.com", 1L);

        // when & then
        mockMvc.perform(delete("/api/core/boards/1")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        verify(boardService, times(1)).deleteBoard("test@example.com", 1L);
    }

    @Test
    @DisplayName("게시판 상태 변경 성공 - 200 OK")
    void updateBoardStatus_Success() throws Exception {
        // given
        Map<String, String> statusRequest = new HashMap<>();
        statusRequest.put("status", "ARCHIVED");
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.updateBoardStatus("test@example.com", 1L, "ARCHIVED"))
                .thenReturn(boardResponse);

        // when & then
        mockMvc.perform(put("/api/core/boards/1/status")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        verify(boardService, times(1)).updateBoardStatus("test@example.com", 1L, "ARCHIVED");
    }

    @Test
    @DisplayName("멤버 초대 성공 - 200 OK")
    void inviteMember_Success() throws Exception {
        // given
        Map<String, String> inviteRequest = new HashMap<>();
        inviteRequest.put("email", "invite@example.com");
        inviteRequest.put("role", "MEMBER");
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.inviteMember("test@example.com", 1L, "invite@example.com", "MEMBER"))
                .thenReturn(boardResponse);

        // when & then
        mockMvc.perform(post("/api/core/boards/1/members/invite")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        verify(boardService, times(1))
                .inviteMember("test@example.com", 1L, "invite@example.com", "MEMBER");
    }

    @Test
    @DisplayName("초대 수락 성공 - 200 OK")
    void acceptInvitation_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.acceptInvitation("test@example.com", 1L))
                .thenReturn(com.example.demo.model.board.BoardMember.builder()
                        .id(1L)
                        .status("ACTIVE")
                        .role("MEMBER")
                        .build());

        // when & then
        mockMvc.perform(post("/api/core/boards/1/members/accept")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        verify(boardService, times(1)).acceptInvitation("test@example.com", 1L);
    }

    @Test
    @DisplayName("게시판 멤버 목록 조회 성공 - 200 OK")
    void getBoardMembers_Success() throws Exception {
        // given
        when(tokenUtils.getEmailFromAuthHeader(validToken)).thenReturn("test@example.com");
        when(boardService.getBoardMembers("test@example.com", 1L))
                .thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/core/boards/1/members")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
        verify(boardService, times(1)).getBoardMembers("test@example.com", 1L);
    }
}
