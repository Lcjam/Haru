package com.example.demo.controller;

import com.example.demo.dto.auth.*;
import com.example.demo.service.UserService;
import com.example.demo.util.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private SignupResponse signupResponse;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("Test1234!@");
        signupRequest.setNickname("testuser");
        signupRequest.setName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Test1234!@");

        signupResponse = SignupResponse.builder()
                .success(true)
                .email("test@example.com")
                .message("회원가입에 성공하였습니다.")
                .build();

        loginResponse = LoginResponse.builder()
                .success(true)
                .token("testToken")
                .email("test@example.com")
                .message("로그인에 성공하였습니다.")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 - 200 OK")
    void signup_Success() throws Exception {
        // given
        when(userService.registerUser(any(SignupRequest.class))).thenReturn(signupResponse);

        // when & then
        mockMvc.perform(post("/api/core/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("회원가입 실패 - 400 Bad Request")
    void signup_Fail() throws Exception {
        // given
        SignupResponse failResponse = SignupResponse.builder()
                .success(false)
                .message("이미 존재하는 이메일입니다.")
                .build();
        when(userService.registerUser(any(SignupRequest.class))).thenReturn(failResponse);

        // when & then
        mockMvc.perform(post("/api/core/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.data.success").value(false));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패")
    void signup_ValidationFail() throws Exception {
        // given
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setEmail("invalid-email"); // 잘못된 이메일 형식

        // when & then
        mockMvc.perform(post("/api/core/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 - 200 OK")
    void login_Success() throws Exception {
        // given
        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // when & then
        mockMvc.perform(post("/api/core/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.token").value("testToken"));
    }

    @Test
    @DisplayName("로그인 실패 - 400 Bad Request")
    void login_Fail() throws Exception {
        // given
        LoginResponse failResponse = LoginResponse.builder()
                .success(false)
                .message("이메일 또는 비밀번호가 올바르지 않습니다.")
                .build();
        when(userService.login(any(LoginRequest.class))).thenReturn(failResponse);

        // when & then
        mockMvc.perform(post("/api/core/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.data.success").value(false));
    }

    @Test
    @DisplayName("로그아웃 성공 - 200 OK")
    void logout_Success() throws Exception {
        // given
        LogoutResponse logoutResponse = LogoutResponse.builder()
                .success(true)
                .message("로그아웃에 성공하였습니다.")
                .build();
        when(userService.logout(anyString())).thenReturn(logoutResponse);

        // when & then
        mockMvc.perform(post("/api/core/auth/logout")
                        .header("Authorization", "Bearer testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 변경 성공 - 200 OK")
    void changePassword_Success() throws Exception {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("OldPassword123!@");
        request.setNewPassword("NewPassword123!@");

        PasswordChangeResponse response = PasswordChangeResponse.builder()
                .success(true)
                .message("비밀번호가 변경되었습니다.")
                .build();
        when(userService.changePasswordByToken(anyString(), any(PasswordChangeRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/core/auth/me/password")
                        .header("Authorization", "Bearer testToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

