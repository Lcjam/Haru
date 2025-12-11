package com.example.demo.service;

import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.dto.auth.SignupRequest;
import com.example.demo.dto.auth.SignupResponse;
import com.example.demo.dto.hobby.HobbyRequest;
import com.example.demo.mapper.HobbyMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.UserAccountInfo;
import com.example.demo.security.JwtTokenBlacklistService;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.util.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtTokenBlacklistService jwtTokenBlacklistService;

    @Mock
    private HobbyService hobbyService;

    @Mock
    private HobbyMapper hobbyMapper;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("Test1234!@");
        signupRequest.setNickname("testuser");
        signupRequest.setName("Test User");
        signupRequest.setPhoneNumber("010-1234-5678");
        signupRequest.setBio("Test bio");
        signupRequest.setLoginMethod("일반");
        signupRequest.setHobbies(new ArrayList<>());

        existingUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testuser")
                .passwordHash("hashedPassword")
                .accountStatus("Active")
                .authority("USER")
                .loginIsLocked(false)
                .loginFailedAttempts(0)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_Success() {
        // given
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(userMapper.findByNickname(anyString())).thenReturn(null);
        when(userMapper.findByPhoneNumber(anyString())).thenReturn(null);
        Map<String, String> hashResult = new HashMap<>();
        hashResult.put("hashedPassword", "hashedPassword");
        when(passwordUtils.hashPassword(anyString(), any())).thenReturn(hashResult);
        when(userMapper.insertUser(any(User.class))).thenReturn(1);
        when(userMapper.insertUserAccountInfo(any(UserAccountInfo.class))).thenReturn(1);
        when(userMapper.initializeUserActivity(anyString(), anyInt(), anyInt())).thenReturn(1);
        when(hobbyService.isValidCategory(anyLong())).thenReturn(true);
        when(hobbyService.isValidHobby(anyLong())).thenReturn(true);
        when(hobbyService.getHobbyMapper()).thenReturn(hobbyMapper);
        when(hobbyMapper.isHobbyInCategory(anyLong(), anyLong())).thenReturn(true);

        // when
        SignupResponse response = authService.registerUser(signupRequest);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userMapper, times(1)).insertUser(any(User.class));
        verify(userMapper, times(1)).insertUserAccountInfo(any(UserAccountInfo.class));
        verify(passwordUtils, times(1)).hashPassword(anyString(), any());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerUser_Fail_DuplicateEmail() {
        // given
        when(userMapper.findByEmail(anyString())).thenReturn(existingUser);

        // when
        SignupResponse response = authService.registerUser(signupRequest);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("이미 존재하는 이메일");
        verify(userMapper, never()).insertUser(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void registerUser_Fail_DuplicateNickname() {
        // given
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(userMapper.findByNickname(anyString())).thenReturn(existingUser);

        // when
        SignupResponse response = authService.registerUser(signupRequest);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("이미 존재하는 닉네임");
        verify(userMapper, never()).insertUser(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 전화번호 중복")
    void registerUser_Fail_DuplicatePhoneNumber() {
        // given
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(userMapper.findByNickname(anyString())).thenReturn(null);
        when(userMapper.findByPhoneNumber(anyString())).thenReturn(existingUser);

        // when
        SignupResponse response = authService.registerUser(signupRequest);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("이미 등록된 전화번호");
        verify(userMapper, never()).insertUser(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 취미 정보 누락")
    void registerUser_Fail_MissingHobbyInfo() {
        // given
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(userMapper.findByNickname(anyString())).thenReturn(null);
        
        HobbyRequest hobby = new HobbyRequest();
        hobby.setCategoryId(null); // 카테고리 ID 누락
        signupRequest.setHobbies(Collections.singletonList(hobby));

        // when
        SignupResponse response = authService.registerUser(signupRequest);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("카테고리 정보가 누락");
        verify(userMapper, never()).insertUser(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Test1234!@");

        when(userMapper.findByEmail(anyString())).thenReturn(existingUser);
        when(passwordUtils.verifyPassword(anyString(), anyString(), any())).thenReturn(true);
        when(jwtTokenProvider.createToken(anyInt(), anyString(), anyList())).thenReturn("testToken");
        when(userMapper.updateLoginTime(anyString())).thenReturn(1);
        when(userMapper.updateFailedLoginAttempts(anyString(), anyInt())).thenReturn(1);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isEqualTo("testToken");
        verify(jwtTokenProvider, times(1)).createToken(anyInt(), anyString(), anyList());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_Fail_UserNotFound() {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("notfound@example.com");
        loginRequest.setPassword("Test1234!@");

        when(userMapper.findByEmail(anyString())).thenReturn(null);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("이메일 또는 비밀번호");
        verify(jwtTokenProvider, never()).createToken(anyInt(), anyString(), anyList());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("WrongPassword");

        when(userMapper.findByEmail(anyString())).thenReturn(existingUser);
        when(passwordUtils.verifyPassword(anyString(), anyString(), any())).thenReturn(false);
        when(userMapper.updateFailedLoginAttempts(anyString(), anyInt())).thenReturn(1);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("이메일 또는 비밀번호");
        verify(jwtTokenProvider, never()).createToken(anyInt(), anyString(), anyList());
    }
}

